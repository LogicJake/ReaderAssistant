package com.scy.readingassistant;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class MyAdapter extends BaseAdapter {
    private Context context;
    List<HashMap<String, Object>> list;

    public MyAdapter(Context context, List<HashMap<String, Object>> list) {
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
            convertView = View.inflate(context, R.layout.item_list, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.item_name);
            holder.path = (TextView) convertView.findViewById(R.id.item_path);
            holder.current_page = (TextView) convertView.findViewById(R.id.item_current_page);
            holder.total_page = (TextView) convertView.findViewById(R.id.item_total_page);
            holder.author = (TextView) convertView.findViewById(R.id.item_author);
            holder.add_time = (TextView) convertView.findViewById(R.id.item_add_time);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText((String)list.get(position).get("name"));
        holder.path.setText((String)list.get(position).get("path"));
        holder.current_page.setText(Integer.toString((int)list.get(position).get("current_page")));
        holder.total_page.setText(Integer.toString((int)list.get(position).get("total_page")));
        holder.author.setText((String)list.get(position).get("author"));
        holder.add_time.setText(Long.toString((long)list.get(position).get("add_time")));

        return convertView;
    }

    public class ViewHolder {
        private TextView name;
        private TextView path;
        private TextView current_page;
        private TextView total_page;
        private TextView author;
        private TextView add_time;
    }

}
