package net.sudot.sessionmanager.tomcat.redis;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.servlet.ServletException;
import java.io.IOException;


public class RedisSessionHandlerValve extends ValveBase {
    private final Log log = LogFactory.getLog(getClass());
    private RedisSessionManager manager;

    public void setRedisSessionManager(RedisSessionManager manager) {
        this.manager = manager;
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            getNext().invoke(request, response);
        } finally {
            manager.afterRequest();
        }
    }
}
