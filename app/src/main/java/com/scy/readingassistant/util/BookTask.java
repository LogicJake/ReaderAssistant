package com.scy.readingassistant.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.scy.readingassistant.domain.BookInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.scy.readingassistant.util.Util.getUUID;

public class BookTask {

    private static final String TAG = "BookTask";


    public static void updateNameAndAuthor(Context context, String uuid, String name, String author) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        System.out.println(name);
        editor.putString("name_"+uuid,name);
        editor.putString("author_"+uuid,author);
        editor.commit();
    }

    public static void updatePath(Context context,String uuid,String path){
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("path_"+uuid,path).commit();
    }

    public static void rebulid(Context context,String data) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> set = new HashSet<String>(sharedPreferences.getStringSet("list", new HashSet<String>()));         //获取所有书籍
        JSONArray jsonArray = new JSONArray(data);
        Log.e(TAG, "rebulid: "+jsonArray);
        for(int i = 0; i<jsonArray.length(); i++){
            JSONObject tmp = jsonArray.getJSONObject(i);
            String uuid = tmp.getString("uid");

            editor.putString("name_"+uuid,tmp.getString("name"));
            editor.putLong("add_time_"+uuid,tmp.getLong("add_time"));
            editor.putString("path_"+uuid,tmp.getString("path"));
            editor.putString("author_"+uuid,tmp.getString("author"));
            editor.putInt("current_page_"+uuid,tmp.getInt("current_page"));
            editor.putInt("total_page_"+uuid,tmp.getInt("total_page"));
            set.add(uuid);
        }
        editor.putStringSet("list",set);
        editor.commit();
    }

    public static String backup(Context context) {
        JSONArray jsonArray = new JSONArray(getAllBook(context));
        Log.e(TAG,jsonArray.toString());
        return jsonArray.toString();
    }

    public static void deleteBook(Context context,String uuid){
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> set = new HashSet<String>(sharedPreferences.getStringSet("list", new HashSet<String>()));         //获取所有书籍
        set.remove(uuid);

        editor.remove("name_"+uuid);
        editor.remove("add_time_"+uuid);
        editor.remove("path_"+uuid);
        editor.remove("author_"+uuid);
        editor.remove("current_page_"+uuid);
        editor.remove("total_page_"+uuid);
        editor.putStringSet("list",set);

        editor.commit();
    }

    public static List<HashMap<String, Object>> getAllBook(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);

        List<HashMap<String, Object>> booklist = new ArrayList<HashMap<String, Object>>();

        Set<String> set = new HashSet<String>(sharedPreferences.getStringSet("list", new HashSet<String>()));         //获取所有书籍
        for (String uid : set) {
            HashMap map = new HashMap<String,Object>();
            map.put("uid",uid);                       //添加时间
            map.put("add_time",sharedPreferences.getLong("add_time_"+uid,0));                       //添加时间
            map.put("name",sharedPreferences.getString("name_"+uid,"书名没了？"));                       //添加时间
            map.put("path",sharedPreferences.getString("path_"+uid,"路径没了？"));
            map.put("current_page",sharedPreferences.getInt("current_page_"+uid,0));
            map.put("total_page",sharedPreferences.getInt("total_page_"+uid,0));
            map.put("author",sharedPreferences.getString("author_"+uid,"作者名没了？"));
            booklist.add(map);
        }

        return booklist;
    }

    public static String addBook(Context context,BookInfo book){
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
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
        return uuid;
    }

    public static void updatePage(Context context,String uuid,int currentpage,int totalpage){
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("current_page_"+uuid,currentpage);
        editor.putInt("total_page_"+uuid,totalpage);
        editor.putLong("add_time_"+uuid,System.currentTimeMillis());
        editor.commit();
    }

}
