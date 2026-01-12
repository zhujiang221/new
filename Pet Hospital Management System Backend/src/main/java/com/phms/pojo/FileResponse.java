package com.phms.pojo;

import com.alibaba.druid.support.json.JSONUtils;

import java.util.HashMap;
import java.util.Map;

public class FileResponse extends HashMap<String, Object> {

    Map<String,Object> msgMap = new HashMap<>();

    public String error(int code, String msg){
        FileResponse result = new FileResponse();
        msgMap.put("message",msg);
        result.put("uploaded",code);
        result.put("error",msgMap);
        return JSONUtils.toJSONString(result);
    }

    public String success(int code, String fileName,String url,String msg){
        FileResponse result = new FileResponse();
        if(msg != null && !msg.trim().isEmpty()){
            msgMap.put("message",msg);
            result.put("error",msgMap);
        }
        result.put("uploaded",code);
        result.put("fileName",fileName);
        result.put("url",url);
        return JSONUtils.toJSONString(result);
    }
}
