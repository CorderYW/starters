package com.yewei.service;

import com.yewei.common.utils.CommonHttpClientUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class ChinaSmsService {
    private String url;
    private String account;
    private String password;
    private String smsCode;

    //微网短信接口
    public Object sendSms(String mobile,String content){
        log.info("sendSms start,mobile:{},content:{}",mobile,content);
        try {
            final Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            final Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("sname", account);
            paramsMap.put("spwd", password);
            paramsMap.put("sdst", mobile);
            paramsMap.put("smsg", content);
            paramsMap.put("scorpid", "");
            paramsMap.put("sprdid", smsCode);
            String result = CommonHttpClientUtils.postFrom(url, paramsMap, headerMap);
            result = result.replace("\r\n", "");
            Document document = DocumentHelper.parseText(result);
            Element root = document.getRootElement();
            return root;
        } catch (Exception e) {
            return null;
        }
    }
}
