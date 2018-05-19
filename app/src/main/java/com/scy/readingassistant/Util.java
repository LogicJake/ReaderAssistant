package com.scy.readingassistant;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    public static String backup(Context context) {
        JSONArray jsonArray = new JSONArray(getAllBook(context));
        Log.e(TAG,jsonArray.toString());
        return jsonArray.toString();
    }

    public static void rebulid(Context context,String data){
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> set = new HashSet<String>(sharedPreferences.getStringSet("list", new HashSet<String>()));         //获取所有书籍

        try {
            JSONArray jsonArray = new JSONArray(data);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void updatePath(Context context,String uuid,String path){
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("path_"+uuid,path).commit();
        System.out.println(sharedPreferences.getString("path_"+uuid,"as"));
    }

    public static void MultPermission(final Context context){
        RxPermissions rxPermissions = new RxPermissions((Activity) context);
        rxPermissions.requestEach(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)//权限名称，多个权限之间逗号分隔开
                .subscribe(new io.reactivex.functions.Consumer<Permission>(){
                    @Override
                    public void accept(Permission permission){
                        if(permission.name.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !permission.granted){
                            Log.e("MainActivity","权限被拒绝");
                            PremissionDialog.showMissingPermissionDialog(context,context.getString(R.string.LACK_RECORD_AUDIO));
                        }
                        if(permission.name.equals(Manifest.permission.READ_EXTERNAL_STORAGE) && !permission.granted){
                            Log.e("MainActivity","权限被拒绝");
                            PremissionDialog.showMissingPermissionDialog(context,context.getString(R.string.LACK_RECORD_AUDIO));
                        }
                    }
                });
    }

    public static String timedate(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressWarnings("unused")
        int i = Integer.parseInt(time);
        String times = sdr.format(new Date(i * 1000L));
        return times;

    }
}
