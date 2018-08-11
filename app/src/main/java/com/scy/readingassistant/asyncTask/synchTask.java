package com.scy.readingassistant.asyncTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.scy.readingassistant.myInterface.RequestNetwork;
import com.scy.readingassistant.util.CosService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.scy.readingassistant.util.BookTask.backup;
import static com.scy.readingassistant.util.BookTask.rebulid;
import static com.scy.readingassistant.util.Util.MultPermission;

public class synchTask extends AsyncTask<Void,Integer,Integer>{
    private static String TAG = "synchTask";
    private Context context;
    CosService cosService;
    String uid;
    private boolean isSuccess = false;
    private JSONArray remoteData;
    private JSONArray newData = new JSONArray();
    public synchTask(Context context,String uid){
        this.context = context;
        cosService = new CosService(context);
        this.uid = uid;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        final String filePath = "/storage/emulated/0/ReaderAssistant/";        //备份路径
         cosService.download(filePath, uid);
        cosService.setDownloadRequestNetwork(new RequestNetwork() {
            @Override
            public void success() {
                try {
                    File file = new File(filePath+"/"+uid+".txt");
                    FileInputStream inputStream = new FileInputStream(file);
                    byte temp[] = new byte[1024];
                    StringBuilder sb = new StringBuilder("");
                    int len = 0;
                    while ((len = inputStream.read(temp)) > 0) {
                        sb.append(new String(temp, 0, len));
                    }
                    remoteData = new JSONArray(sb.toString());
                    inputStream.close();
                    merge();
                    upload();
                } catch (Exception e) {
                    isSuccess = false;
                    e.printStackTrace();
                }
            }

            @Override
            public void fail(String errmsg) {

            }
        });
        return null;
    }

    private void merge(){
        try {
            JSONArray localData = new JSONArray(backup(context));
            HashMap<String,JSONObject> remoteMap = new HashMap<>();
            HashMap<String,JSONObject> localMap = new HashMap<>();

            for (int i = 0; i < remoteData.length(); i++) {
                JSONObject tmp = remoteData.getJSONObject(i);
                remoteMap.put(tmp.getString("uid"),tmp);
            }

            for (int i = 0; i < localData.length(); i++) {
                JSONObject tmp = localData.getJSONObject(i);
                localMap.put(tmp.getString("uid"),tmp);
            }

            Iterator<Map.Entry<String, JSONObject>> it = remoteMap.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, JSONObject> itEntry = it.next();
                String itKey = itEntry.getKey();
                if (localMap.containsKey(itKey)){
                    itEntry.setValue(localMap.get(itKey));
                }else {
                    it.remove();
                }
            }

            Iterator<Map.Entry<String, JSONObject>> it2 = localMap.entrySet().iterator();
            while(it2.hasNext()){
                Map.Entry<String, JSONObject> itEntry = it2.next();
                String itKey = itEntry.getKey();
                JSONObject itValue = itEntry.getValue();
                if (!remoteMap.containsKey(itKey)) {
                    remoteMap.put(itKey,itValue);
                }
            }

            for (Map.Entry<String, JSONObject> entry : remoteMap.entrySet()) {
                newData.put(entry.getValue());
            }

            Log.i(TAG, "merge: "+newData);

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

    }

    private void upload(){
//        MultPermission(context);
        String filePath = "/storage/emulated/0/ReaderAssistant/newbak.txt";        //备份路径
        File file = new File(filePath);
        try {
            if (file.exists()) {
                Log.w(TAG,"The directory [ " + filePath + " ] has already exists");
            }
            else
                file.createNewFile();
            String data = newData.toString();
            rebulid(context,data);
            byte[] buffer = data.getBytes();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer, 0, buffer.length);
            fos.flush();
            fos.close();
            cosService.upload(filePath,uid);
            isSuccess = true;
        }catch (Exception e) {
            Log.e(TAG, "upload: ", e);
            isSuccess = false;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (isSuccess)
            Toast.makeText(context, "同步成功", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "同步失败", Toast.LENGTH_SHORT).show();
    }
}
