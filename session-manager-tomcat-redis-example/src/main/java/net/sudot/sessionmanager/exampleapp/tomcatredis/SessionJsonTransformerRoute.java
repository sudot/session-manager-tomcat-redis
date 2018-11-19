package net.sudot.sessionmanager.exampleapp.tomcatredis;

import spark.ResponseTransformer;
import spark.Session;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SessionJsonTransformerRoute implements ResponseTransformer {

    @Override
    public String render(Object object) {
        if (object instanceof Object[]) {
            Object[] tuple = (Object[]) object;
            Session sparkSession = (Session) tuple[0];
            HttpSession session = sparkSession.raw();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.putAll((Map<String, Object>) tuple[1]);
            map.put("sessionId", session.getId());
            return JsonUtils.toJson(map);
        } else if (object instanceof Session) {
            Session sparkSession = (Session) object;
            HashMap<String, Object> sessionMap = new HashMap<String, Object>();
            if (null != sparkSession) {
                HttpSession session = sparkSession.raw();
                sessionMap.put("sessionId", session.getId());
                HashMap<String, Object> attributesMap = new HashMap<String, Object>();
                for (String key : Collections.list(session.getAttributeNames())) {
                    attributesMap.put(key, session.getAttribute(key));
                }
                sessionMap.put("attributes", attributesMap);
            }
            return JsonUtils.toJson(sessionMap);
        } else {
            return "{}";
        }
    }

}