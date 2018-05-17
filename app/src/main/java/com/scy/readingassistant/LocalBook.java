package com.scy.readingassistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.scy.readingassistant.Util.addBook;
import static com.scy.readingassistant.Util.deleteBook;
import static com.scy.readingassistant.Util.getAllBook;

public class LocalBook extends AppCompatActivity implements View.OnClickListener{
    private Context context = this;
    private static final String TAG = "LocalBook";
    private List<HashMap<String, Object>> mListData;
    private SwipeMenuListView booklist;
    private MyAdapter myAdapter;

    @Override
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
                    Collections.sort(mListData,new LocalBook.Order());

                    myAdapter = new MyAdapter(context,mListData);
                    booklist.setAdapter(myAdapter);
                    setListViewHeightBasedOnChildren(booklist);
                    break;

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_book);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        initListView();     //设置书本listview
        getBookInfo();
    }

    public void initListView(){
        booklist = (SwipeMenuListView) findViewById(R.id.list_view);

        SwipeMenuCreator creater = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(context);
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(200);
                deleteItem.setIcon(R.drawable.ic_delete);
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        booklist.setMenuCreator(creater);

        booklist.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        deleteBook(context,(String)mListData.get(position).get("uid"));
                        mListData.remove(position);
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                }
                return false;
            }
        });

        booklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(LocalBook.this, PdfViwerActivity.class);
                intent.putExtra("name", (String) mListData.get(i).get("name"));
                intent.putExtra("path", (String) mListData.get(i).get("path"));
                intent.putExtra("current_page",(int)mListData.get(i).get("current_page"));
                intent.putExtra("uid",(String)mListData.get(i).get("uid"));
                startActivity(intent);
            }
        });
    }

    public void getBookInfo(){
        mListData = getAllBook(context);

        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    public void onRestart(){
        Log.e(TAG,"onRestart");
        getBookInfo();
        super.onRestart();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                String path = uri.getPath().toString();
                String[] aa = path.split("/");

                path = "/storage/emulated/0"+path.substring(path.indexOf("/",1));
                aa = aa[aa.length-1].split("\\.");
                String name = aa[0];
                BookInfo book = new BookInfo(System.currentTimeMillis(),name,path,1,1,"未知");

                String uid = addBook(context,book);          //存储

                HashMap map = new HashMap<String,Object>();

                map.put("uid",uid);
                map.put("add_time",book.getAddTime());                       //添加时间
                map.put("name",book.getName());                       //添加时间
                map.put("path",book.getPath());
                map.put("current_page",book.getCurrentPage());
                map.put("total_page",book.getTotalPage());
                map.put("author",book.getAuthor());

                mListData.add(map);
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }
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
}
