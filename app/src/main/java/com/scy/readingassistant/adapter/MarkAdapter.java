package com.scy.readingassistant.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scy.readingassistant.R;

import java.util.HashMap;
import java.util.List;

import static com.scy.readingassistant.util.Util.timedate;

public class MarkAdapter extends BaseAdapter {
    private Context context;
    List<HashMap<String, Object>> list;

    public MarkAdapter(Context context, List<HashMap<String, Object>> list) {
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
            convertView = View.inflate(context, R.layout.mark_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.mark_name);
            holder.progress = (TextView)convertView.findViewById(R.id.mark_progress);
            holder.add_time = (TextView) convertView.findViewById(R.id.add_time);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText((String)list.get(position).get("markName"));
        int currentPage = (int)list.get(position).get("page");
        int totalPage = (int)list.get(position).get("total_page");
        String progressText = String.format("%d/%d (%.2f%%)",currentPage,totalPage,(float)currentPage/totalPage*100);
        holder.progress.setText(progressText);
        holder.add_time.setText(timedate(Long.toString((long)list.get(position).get("addTime"))));

        return convertView;
    }

    public class ViewHolder {
        private TextView name;
        private TextView progress;
        private TextView add_time;
    }
}
