package net.sudot.sessionmanager.exampleapp.tomcatredis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import spark.ResponseTransformerRoute;

public abstract class JsonTransformerRoute extends ResponseTransformerRoute {

    protected JsonTransformerRoute(String path) {
        super(path);
    }

    protected JsonTransformerRoute(String path, String acceptType) {
        super(path, acceptType);
    }

    @Override
    public String render(Object jsonObject) {
        return JSON.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
    }

}