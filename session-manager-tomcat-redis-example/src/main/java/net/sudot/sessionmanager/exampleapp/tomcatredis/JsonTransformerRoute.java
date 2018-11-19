package net.sudot.sessionmanager.exampleapp.tomcatredis;

import spark.ResponseTransformer;

public class JsonTransformerRoute implements ResponseTransformer {

    @Override
    public String render(Object jsonObject) {
        return JsonUtils.toJson(jsonObject);
    }

}