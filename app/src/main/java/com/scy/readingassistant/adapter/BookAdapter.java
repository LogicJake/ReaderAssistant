package com.scy.readingassistant.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.scy.readingassistant.R;

import java.util.HashMap;
import java.util.List;

import static com.scy.readingassistant.util.Util.timedate;

public class BookAdapter extends BaseAdapter {
    private Context context;
    List<HashMap<String, Object>> list;

    public BookAdapter(Context context, List<HashMap<String, Object>> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.book_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.item_name);
            holder.path = (TextView) convertView.findViewById(R.id.item_path);
            holder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
            holder.progress = (TextView) convertView.findViewById(R.id.progress);
            holder.author = (TextView) convertView.findViewById(R.id.item_author);
            holder.add_time = (TextView) convertView.findViewById(R.id.item_add_time);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText((String)list.get(position).get("name"));
        holder.path.setText((String)list.get(position).get("path"));
        float num = (float) (int)list.get(position).get("current_page")/(int)list.get(position).get("total_page");
        holder.progressBar.setProgress((int)(num*100));
        holder.progress.setText(Integer.toString((int)list.get(position).get("current_page"))+"/"+Integer.toString((int)list.get(position).get("total_page")));
        holder.author.setText("作者："+(String)list.get(position).get("author"));
        holder.add_time.setText(timedate(Long.toString((long)list.get(position).get("add_time"))));

        return convertView;
    }

    public class ViewHolder {
        private TextView name;
        private TextView path;
        private ProgressBar progressBar;
        private TextView progress;

        private TextView author;
        private TextView add_time;
    }

}
