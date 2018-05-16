package com.scy.readingassistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,View.OnClickListener {

    private static final String TAG = "MainActivity";
    private List<HashMap<String, Object>> mListData = new ArrayList<HashMap<String, Object>>();
    private SharedPreferences sharedPreferences ;
    private SharedPreferences.Editor editor;
    private ListView booklist;
    private SimpleAdapter mSchedule;
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
            List<HashMap<String, Object>> recentList;
            switch (msg.what) {
                case 1:
                    Collections.sort(mListData,new Order());
                    recentList = mListData.subList(0,5);
                    String[] from = { "name", "path", "current_page", "total_page","author" ,"add_time"};
                    // 列表项组件Id 数组
                    int[] to = { R.id.item_name, R.id.item_path, R.id.item_current_page,
                            R.id.item_total_page,R.id.item_author ,R.id.item_add_time};
                    mSchedule = new SimpleAdapter(MainActivity.this,
                            recentList,//数据来源
                            R.layout.item_list,//ListItem的XML实现
                            from,
                            to);
                    booklist.setAdapter(mSchedule);
                    setListViewHeightBasedOnChildren(booklist);
                    break;
                case 2:
                    Collections.sort(mListData,new Order());
                    recentList = mListData.subList(0,5);
                    mSchedule.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(booklist);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("最近阅读");

        sharedPreferences = getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        booklist = (ListView) findViewById(R.id.list_view);
        getBookInfo();

        createMyDir();
    }

    public void getBookInfo(){
        Set<String> set = new HashSet<String>(sharedPreferences.getStringSet("list", new HashSet<String>()));         //获取所有书籍
        for (String uid : set) {
            System.out.println(uid);
            HashMap map = new HashMap<String,Object>();
            map.put("add_time",sharedPreferences.getLong("add_time_"+uid,0));                       //添加时间
            map.put("name",sharedPreferences.getString("name_"+uid,"书名没了？"));                       //添加时间
            map.put("path",sharedPreferences.getString("path_"+uid,"路径没了？"));
            map.put("current_page",sharedPreferences.getInt("current_page_"+uid,0));
            map.put("total_page",sharedPreferences.getInt("total_page_"+uid,0));
            map.put("author",sharedPreferences.getString("author_"+uid,"作者名没了？"));

            mListData.add(map);
        }
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
            // listAdapter.getCount()返回数据项的数目
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

    public static String getUUID(){
        UUID uuid=UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr=str.replace("-", "");
        return uuidStr;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
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
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                String path = uri.getPath().toString();
                String[] aa = path.split("/");
                aa = aa[aa.length-1].split("\\.");
                String name = aa[0];
                BookInfo book = new BookInfo(System.currentTimeMillis(),name,path,0,0,"未知");
                Set<String> set = new HashSet<String>(sharedPreferences.getStringSet("list", new HashSet<String>()));
                String uuid = getUUID();
                set.add(uuid);
                editor.putString("name_"+uuid,book.getName());
                editor.putLong("add_time_"+uuid,book.getAddTime());
                editor.putString("path_"+uuid,book.getPath());
                editor.putString("author_"+uuid,book.getAuthor());
                editor.putInt("current_page_"+uuid,book.getCurrentPage());
                editor.putInt("total_page_"+uuid,book.getTotalPage());
                editor.putStringSet("list",set);
                editor.commit();

                HashMap map = new HashMap<String,Object>();
                map.put("add_time",book.getAddTime());                       //添加时间
                map.put("name",book.getName());                       //添加时间
                map.put("path",book.getPath());
                map.put("current_page",book.getCurrentPage());
                map.put("total_page",book.getTotalPage());
                map.put("author",book.getAuthor());


                mListData.add(map);
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.local) {
            Intent intent = new Intent(MainActivity.this,PdfViwerActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void createMyDir(){          //pdf统一存储位置
        String dirPath = "/storage/emulated/0/ReaderAssistant/book";
        File dir = new File(dirPath);
        if (dir.exists()) {
            Log.w(TAG,"The directory [ " + dirPath + " ] has already exists");
            return ;
        }
        if (!dirPath.endsWith(File.separator)) {//不是以 路径分隔符 "/" 结束，则添加路径分隔符 "/"
            dirPath = dirPath + File.separator;
        }
        //创建文件夹
        if (dir.mkdirs()) {
            Log.d(TAG,"create directory [ "+ dirPath + " ] success");
            return;
        }
        Log.e(TAG,"create directory [ "+ dirPath + " ] failed");
        return;
    }
}
