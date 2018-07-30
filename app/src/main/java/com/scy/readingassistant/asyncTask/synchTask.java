package com.scy.readingassistant.asyncTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.scy.readingassistant.myInterface.RequestNetwork;
import com.scy.readingassistant.util.CosService;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;

import static com.scy.readingassistant.util.BookTask.backup;

public class synchTask extends AsyncTask<Void,Integer,Integer>{
    private Context context;
    private SharedPreferences sharedPreferences;
    private JSONArray remoteData;
    public synchTask(Context context){
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        final String filePath = "/storage/emulated/0/ReaderAssistant/";        //备份路径
        SharedPreferences sharedPreferences = context.getSharedPreferences("uid", Context.MODE_PRIVATE);
        final String uid = sharedPreferences.getString("uid",null);
        CosService cosService = null;
        if (uid != null) {
            cosService = new CosService(context);
            cosService.download(filePath, uid);
            cosService.setRequestNetwork(new RequestNetwork() {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void fail(String errmsg) {

                }
            });
        }
        //获取本地
        //        SharedPreferences sharedPreferences = context.getSharedPreferences("uid", Context.MODE_PRIVATE);
//        String uid = sharedPreferences.getString("uid",null);
//        CosService cosService = null;
//        if (uid != null){
//            cosService =  new CosService(context);
//            cosService.upload(filePath,uid);
//        }
//        if (cosService!=null) {
//            cosService.setRequestNetwork(new RequestNetwork() {
//                @Override
//                public void success() {
//                    Message message = new Message();
//                    message.what = 2;
//                    handler.sendMessage(message);
//                }
//
//                @Override
//                public void fail(String errmsg) {
//                    Message message = new Message();
//                    message.what = 3;
//                    handler.sendMessage(message);
//                }
//            });
//        }
        return null;
    }

    private void merge(){
        try {
            JSONArray localData = new JSONArray(backup(context));
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

    }

    @Override
    protected void onPostExecute(Integer integer) {
    }


}
