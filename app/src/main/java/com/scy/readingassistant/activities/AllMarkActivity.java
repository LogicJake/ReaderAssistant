package com.scy.readingassistant.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
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
import com.scy.readingassistant.adapter.MarkAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.scy.readingassistant.util.BookTask.deleteBookMark;
import static com.scy.readingassistant.util.BookTask.getAllMarksByUuid;
import static com.scy.readingassistant.util.BookTask.updateBookMarkName;

public class AllMarkActivity extends AppCompatActivity {
    private String uuid;
    private final String TAG = "AllMarkActivity";
    private ListView remarklist;
    private Context context;
    private MarkAdapter myAdapter;
    private List<HashMap<String, Object>> mListData;
    private int totalPage;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Collections.sort(mListData, new Order());
                    myAdapter = new MarkAdapter(context, mListData);
                    Log.d(TAG, "handleMessage: "+mListData);
                    remarklist.setAdapter(myAdapter);
                    setListViewHeightBasedOnChildren(remarklist);
                    break;
            }
        }
    };

    public class Order implements Comparator<HashMap<String, Object>> {

        @Override
        public int compare(HashMap<String, Object> lhs, HashMap<String, Object> rhs) {
            //按照时间顺序最新的在上面
            int a = (int) lhs.get("page");
            int b = (int) rhs.get("page");
            int c = a - b;
            if (c < 0) {
                return 1;
            } else if (c == 0) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_mark);

        context = this;
        Intent intent = getIntent();
        uuid = intent.getStringExtra("uuid");
        totalPage = Integer.parseInt(intent.getStringExtra("total_page"));
        Log.i(TAG, "onCreate: "+uuid);
        initListView();     //设置书本listview
        getMarkInfo();
    }

    public void initListView(){
        remarklist = (ListView) findViewById(R.id.mark_list_view);

        remarklist.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(0, 0, 0, "重命名");
                contextMenu.add(0, 1, 0, "删除");
            }
        });

        remarklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("jump_page",(int)mListData.get(position).get("page"));
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        final int i = (int) info.id;
        switch (item.getItemId()) {
            case 0:
                View view = getLayoutInflater().inflate(R.layout.dialog_jump, null);
                final EditText dialog_edit = (EditText) view.findViewById(R.id.dialog_edit);
                dialog_edit.setText((String)mListData.get(i).get("markName"));
                TextView textView = (TextView)view.findViewById(R.id.pageNum);
                dialog_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                InputFilter[] filters = {new InputFilter.LengthFilter(10)};
                dialog_edit.setFilters(filters);
                textView.setVisibility(View.GONE);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("重命名(最长10字符)")
                        .setView(view)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = dialog_edit.getText().toString();
                                if (newName.length() != 0) {
                                    int page = (int)mListData.get(i).get("page");
                                    updateBookMarkName(context,uuid,page,newName);
                                }else {
                                    Toast.makeText(context,"不能为空",Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
                break;
            case 1:
                deleteBookMark(context, uuid, (int)mListData.get(i).get("page"));
                getMarkInfo();
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void getMarkInfo(){
        mListData = getAllMarksByUuid(context,uuid);
        for (HashMap<String, Object> tmp : mListData){
            tmp.put("total_page",totalPage);
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
