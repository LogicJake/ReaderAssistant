package com.scy.readingassistant.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scy.readingassistant.R;
import com.scy.readingassistant.adapter.MyAdapter;
import com.scy.readingassistant.asyncTask.synchTask;
import com.scy.readingassistant.domain.BookInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import static com.scy.readingassistant.util.BookTask.addBook;
import static com.scy.readingassistant.util.BookTask.backup;
import static com.scy.readingassistant.util.BookTask.deleteBook;
import static com.scy.readingassistant.util.BookTask.getAllBook;
import static com.scy.readingassistant.util.BookTask.rebulid;
import static com.scy.readingassistant.util.BookTask.updateNameAndAuthor;
import static com.scy.readingassistant.util.BookTask.updatePath;
import static com.scy.readingassistant.util.FileUtils.getFilePathByUri;
import static com.scy.readingassistant.util.Util.MultPermission;
import static com.scy.readingassistant.util.Util.createMyDir;
import static com.scy.readingassistant.util.Util.timedate;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,View.OnClickListener {
    private Context context = this;
    private static final String TAG = "MainActivity";
    private List<HashMap<String, Object>> mListData;
    List<HashMap<String, Object>> recentList;
    private ListView booklist;
    private MyAdapter myAdapter;
    private Tencent mTencent;
    private String uid;
    private NavigationView navigationView;
    private TextView updateTime;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public class Order implements Comparator<HashMap<String, Object>> {

        @Override
        public int compare(HashMap<String, Object> lhs, HashMap<String, Object> rhs) {
            // TODO Auto-generated method stub
            //按照时间顺序最新的在上面
            long a = (Long) lhs.get("add_time");
            long b = (Long) rhs.get("add_time");
            long c = a - b;
            if (c < 0) {
                return 1;
            } else if (c == 0) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Collections.sort(mListData, new Order());
                    System.out.println(mListData);
                    if (mListData.size() > 5)
                        recentList = mListData.subList(0, 5);
                    else
                        recentList = mListData;
                    myAdapter = new MyAdapter(context, recentList);
                    booklist.setAdapter(myAdapter);
                    setListViewHeightBasedOnChildren(booklist);
                    break;
                case 2:
                    Toast.makeText(context, "备份成功", Toast.LENGTH_SHORT).show();
                    String updateTimeString = timedate(System.currentTimeMillis());
                    updateTime.setText("上次同步时间：\n" + updateTimeString);
                    editor.putString("lastUpdateTime", updateTimeString).commit();
                    break;
                case 3:
                    Toast.makeText(context, "备份失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createMyDir();
        initListView();     //设置书本listview
        getBookInfo();
    }

    public void initListView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("最近阅读");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        sp = getSharedPreferences("setting",MODE_PRIVATE);
        editor = sp.edit();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        updateTime = headerView.findViewById(R.id.updateTime);
        updateTime.setText("上次同步时间：\n"+sp.getString("lastUpdateTime","未备份"));
        Log.d(TAG, "initListView: "+updateTime);

        if (sp.getString("uid",null) != null){
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
        }

        booklist = (ListView) findViewById(R.id.list_view);

        booklist.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(0, 0, 0, "修改");
                contextMenu.add(0, 1, 0, "删除");
            }
        });

        booklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File file = new File((String) mListData.get(i).get("path"));
                if (!file.exists()) {
                    uid = (String) mListData.get(i).get("uid");
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("文件不存在");
                    builder.setMessage("文件不存在，按导入按钮重新选择文件位置");
                    builder.setNegativeButton("取消",null);
                    builder.setPositiveButton("导入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MultPermission(context);
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(intent, 3);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, PdfViwerActivity.class);
                intent.putExtra("name", (String) mListData.get(i).get("name"));
                intent.putExtra("path", (String) mListData.get(i).get("path"));
                intent.putExtra("current_page", (int) mListData.get(i).get("current_page"));
                intent.putExtra("uid", (String) mListData.get(i).get("uid"));
                startActivity(intent);
            }
        });
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int i = (int) info.id;
        switch (item.getItemId()) {
            case 1:
                deleteBook(context, (String) mListData.get(i).get("uid"));
                getBookInfo();
                break;
            case 0:
                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.dialog_update,(ViewGroup) findViewById(R.id.dialog));
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                final EditText editText_name = (EditText) layout.findViewById(R.id.name);
                final EditText editText_author = (EditText) layout.findViewById(R.id.author);
                editText_name.setText((String) mListData.get(i).get("name"));
                editText_author.setText((String) mListData.get(i).get("author"));
                final String uuid = (String) mListData.get(i).get("uid");
                builder.setTitle("修改书籍信息")
                        .setView(layout)
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = editText_name.getText().toString();
                                String author = editText_author.getText().toString();
                                updateNameAndAuthor(context,uuid,name,author);
                                getBookInfo();
                            }
                        })
                        .show();;
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void getBookInfo(){
        mListData = getAllBook(context);

        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                MultPermission(context);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
                break;
            default:
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.REQUEST_LOGIN)
            Tencent.onActivityResultData(requestCode, resultCode, data, new BaseUiListener());
        else
        {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                String path = getFilePathByUri(this, uri);
                if (path == null || !new File(path).exists()) {
                    Log.e(TAG, path);
                    Toast.makeText(this, "获取文件路径失败，请用系统文件管理器打开！", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e(TAG, path);
                String[] aa = path.split("/");
                if (requestCode == 1) {
                    aa = aa[aa.length - 1].split("\\.");
                    String name = aa[0];
                    if (!aa[aa.length - 1].equals("pdf")) {
                        Toast.makeText(context, "只支持添加pdf文件", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BookInfo book = new BookInfo(System.currentTimeMillis(), name, path, 1, 1, "未知");
                    String uid = addBook(context, book);          //存储
                    HashMap map = new HashMap<String, Object>();

                    map.put("uid", uid);
                    map.put("add_time", book.getAddTime());                       //添加时间
                    map.put("name", book.getName());                       //添加时间
                    map.put("path", book.getPath());
                    map.put("current_page", book.getCurrentPage());
                    map.put("total_page", book.getTotalPage());
                    map.put("author", book.getAuthor());

                    mListData.add(map);
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                } else if (requestCode == 2) {
                    try {
                        File file = new File(path);
                        FileInputStream inputStream = new FileInputStream(file);
                        byte temp[] = new byte[1024];
                        StringBuilder sb = new StringBuilder("");
                        int len = 0;
                        while ((len = inputStream.read(temp)) > 0) {
                            sb.append(new String(temp, 0, len));
                        }
                        Log.d("msg", "readSaveFile: \n" + sb.toString());
                        rebulid(context, sb.toString());
                        inputStream.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "导入备份成功", Toast.LENGTH_SHORT).show();
                    getBookInfo();
                } else if (requestCode == 3) {
                    updatePath(context, uid, path);
                    getBookInfo();
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_local) {
            Intent intent = new Intent(MainActivity.this,LocalBook.class);
            startActivity(intent);
        } else if (id == R.id.nav_import) {      //导入备份
            MultPermission(context);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent,2);
        } else if (id == R.id.nav_backup) {         //备份
            backupFunction();
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(MainActivity.this,AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            send();
        }else if (id == R.id.nav_login){
            mTencent = Tencent.createInstance("1106974967",getApplicationContext());
            mTencent.login(MainActivity.this,"all",new BaseUiListener());
        }else if (id == R.id.nav_logout){
            editor.remove("uid").commit();
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
        }else if (id == R.id.nav_synch){
            synch();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void synch(){
        if (sp.getString("uid",null) == null){
            Toast.makeText(context,"请先登陆",Toast.LENGTH_SHORT).show();
            return;
        }
        new synchTask(context).executeOnExecutor(Executors.newCachedThreadPool());
    }

    private void send(){
        String filePath = "/storage/emulated/0/ReaderAssistant/bak.txt";        //备份路径
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(context,"请先备份",Toast.LENGTH_SHORT).show();
        }
        else {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    "com.scy.readingassistant.fileprovider",
                    file);
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("application/txt");
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(sendIntent, file.getName()));
        }
    }

    private void backupFunction(){
        MultPermission(context);
        String filePath = "/storage/emulated/0/ReaderAssistant/bak.txt";        //备份路径
        File file = new File(filePath);
        try {
            if (file.exists()) {
                Log.w(TAG,"The directory [ " + filePath + " ] has already exists");
            }
            else
                file.createNewFile();
            String data = backup(context);
            byte[] buffer = data.getBytes();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer, 0, buffer.length);
            fos.flush();
            fos.close();
            Toast.makeText(context, "备份成功", Toast.LENGTH_SHORT).show();
        }catch (Exception e) {
            Log.e(TAG, "onNavigationItemSelected: ", e);
            Toast.makeText(context, "备份失败", Toast.LENGTH_SHORT).show();
        }
    }

    private class BaseUiListener implements IUiListener {
        public void onComplete(Object response) {
            Log.v("----TAG--", "-------------"+response.toString());
            try {
                String openid = ((JSONObject)response).getString("openid");
                editor.putString("uid","qq"+openid);
                editor.commit();
                navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {
            Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), "onCancel", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRestart(){
        Log.e(TAG,"onRestart");
        getBookInfo();
        super.onRestart();
    }
}
