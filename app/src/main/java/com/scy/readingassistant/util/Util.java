package com.scy.readingassistant.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.scy.readingassistant.R;
import com.scy.readingassistant.view.PremissionDialog;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static String formatJson(String content) {
        StringBuffer sb = new StringBuffer();
        int index = 0;
        int count = 0;
        while(index < content.length()){
            char ch = content.charAt(index);
            if(ch == '{' || ch == '['){
                sb.append(ch);
                sb.append('\n');
                count++;
                for (int i = 0; i < count; i++) {
                    sb.append('\t');
                }
            }
            else if(ch == '}' || ch == ']'){
                sb.append('\n');
                count--;
                for (int i = 0; i < count; i++) {
                    sb.append('\t');
                }
                sb.append(ch);
            }
            else if(ch == ','){
                sb.append(ch);
                sb.append('\n');
                for (int i = 0; i < count; i++) {
                    sb.append('\t');
                }
            }
            else {
                sb.append(ch);
            }
            index ++;
        }
        return sb.toString();
    }
}
