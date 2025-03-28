package com.yuan.api.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON工具类
 */
public class JsonUtils {

    /**
     * 判断字符串是否可以被解析为json格式
     * @param jsonString
     * @return
     */
    public static boolean isJSONString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            System.err.println(jsonObject);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}
