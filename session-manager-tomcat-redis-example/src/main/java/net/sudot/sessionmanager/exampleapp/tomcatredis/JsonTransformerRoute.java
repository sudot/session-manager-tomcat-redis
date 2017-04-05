package net.sudot.sessionmanager.exampleapp.tomcatredis;

import com.google.gson.Gson;
import spark.ResponseTransformerRoute;

public abstract class JsonTransformerRoute extends ResponseTransformerRoute {

    private Gson gson = new Gson();

    protected JsonTransformerRoute(String path) {
      super(path);
    }

    protected JsonTransformerRoute(String path, String acceptType) {
      super(path, acceptType);
    }

    @Override
    public String render(Object jsonObject) {
      return gson.toJson(jsonObject);
    }

}