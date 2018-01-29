package com.hxh.component.basicore.util.convert;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * Created by hxh on 2017/12/21.
 */

public class JSONConvert implements IJson {
    @Override
    public String toJson(Object obj) {

        return JSON.toJSONString(obj);
    }

    @Override
    public <T>T toObj(String json, Class<T> classzz) {
        return JSON.parseObject(json,classzz);

    }

    @Override
    public <T> List<T> toArray(String json, Class<T> classzz)
    {
        return JSON.parseArray(json,classzz);

    }
}
