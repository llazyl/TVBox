package com.github.tvbox.osc.util;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Json {


    public static Map<String, String> toMap(JSONObject jsonobj) {
        if (jsonobj == null) return new HashMap<>();
        Map<String, Object> map = toObjectMap(jsonobj);
        Map<String, String> result = new HashMap<String, String>();
        for(String key : map.keySet()) {
            result.put(key, map.get(key).toString());
        }
        return result;
    }

    public static Map<String, Object> toObjectMap(JSONObject jsonobj) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Iterator<String> keys = jsonobj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonobj.get(key);
                if (value instanceof JSONArray) {
                    value = toObjectList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = toObjectMap((JSONObject) value);
                }
                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static List<Object> toObjectList(JSONArray array) {
        List<Object> list = new ArrayList<Object>();
        try {
            for (int i = 0; i < array.length(); i++) {
                Object value = array.get(i);
                if (value instanceof JSONArray) {
                    value = toObjectList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = toObjectMap((JSONObject) value);
                }
                list.add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
