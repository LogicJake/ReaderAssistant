package com.scy.readingassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Util {

    private static final String TAG = "Util";

    public static void createMyDir(){          //pdf统一存储位置
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

    public static String getUUID(){
        UUID uuid=UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr=str.replace("-", "");
        return uuidStr;
    }

    public static void deleteBook(Context context,String uuid){
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> set = new HashSet<String>(sharedPreferences.getStringSet("list", new HashSet<String>()));         //获取所有书籍
        set.remove(uuid);

        editor.remove("name_"+uuid);
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
        SharedPreferences.Editor editor = sharedPreferences.edit();

        List<HashMap<String, Object>> booklist = new ArrayList<HashMap<String, Object>>();

        Set<String> set = new HashSet<String>(sharedPreferences.getStringSet("list", new HashSet<String>()));         //获取所有书籍
        for (String uid : set) {
            System.out.println(uid);
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
}
