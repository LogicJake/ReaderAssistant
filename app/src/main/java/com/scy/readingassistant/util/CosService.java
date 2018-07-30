package com.scy.readingassistant.util;

import android.content.Context;
import android.util.Log;

import com.scy.readingassistant.myInterface.RequestNetwork;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.GetObjectRequest;
import com.tencent.cos.xml.model.object.PutObjectRequest;

public class CosService {
    String appid = "1253955620";
    String region = "ap-shanghai";
    Context context;
    String secretId = "AKIDd4LN9fjBQSFn6A8bx4xby71g6HDwtMlv";
    String secretKey ="5wXt1xd3ECvNZVAYp5f094lCPN6lCPN6";
    long keyDuration = 600;                         //SecretKey 的有效时间，单位秒
    CosXmlService cosXmlService;

    public void setRequestNetwork(RequestNetwork requestNetwork) {
        this.requestNetwork = requestNetwork;
    }

    private RequestNetwork requestNetwork;

    public CosService(Context context){
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .builder();
        this.context = context;
        //创建获取签名类(请参考下面的生成签名示例，或者参考 sdk中提供的ShortTimeCredentialProvider类）
        LocalCredentialProvider localCredentialProvider = new LocalCredentialProvider(secretId, secretKey, keyDuration);
        //创建 CosXmlService 对象，实现对象存储服务各项操作.
        cosXmlService = new CosXmlService(context,serviceConfig, localCredentialProvider);
    }

    public void upload(String srcPath,String uid){
        String bucket = "pdf-1253955620"; // cos v5 的 bucket格式为：xxx-appid, 如 test-1253960454
        String cosPath = "/"+uid+".txt";
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, cosPath, srcPath);
        putObjectRequest.setSign(keyDuration,null,null); //若不调用，则默认使用sdk中sign duration（60s）

        cosXmlService.putObjectAsync(putObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                Log.w("TEST","success =" + result.accessUrl);
                requestNetwork.success();
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException clientException, CosXmlServiceException serviceException)  {
                String errorMsg = clientException != null ? clientException.toString() : serviceException.toString();
                Log.w("TEST",errorMsg);
                requestNetwork.fail(errorMsg);
            }
        });
    }

    public void download(String savePath,String uid){
        String bucket = "pdf-1253955620"; // cos v5 的 bucket格式为：xxx-appid, 如 test-1253960454
        String cosPath = "/"+uid+".txt";

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, cosPath, savePath);
        getObjectRequest.setSign(keyDuration,null,null);

        cosXmlService.getObjectAsync(getObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                Log.w("TEST","success");
                requestNetwork.success();
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException clientException, CosXmlServiceException serviceException)  {
                String errorMsg = clientException != null ? clientException.toString() : serviceException.toString();
                Log.w("TEST",errorMsg);
                requestNetwork.fail(errorMsg);
            }
        });
    }
}
