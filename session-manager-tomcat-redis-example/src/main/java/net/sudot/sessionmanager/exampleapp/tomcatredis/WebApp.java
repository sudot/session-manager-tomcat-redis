package net.sudot.sessionmanager.exampleapp.tomcatredis;

import net.sudot.sessionmanager.tomcat.redis.RedisSessionManager;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;
import spark.utils.IOUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

public class WebApp implements spark.servlet.SparkApplication {

    private final Log log = LogFactory.getLog(getClass());

    protected String redisHost = Protocol.DEFAULT_HOST;
    protected int redisPort = Protocol.DEFAULT_PORT;
    protected int redisDatabase = Protocol.DEFAULT_DATABASE;
    protected int redisTimeout = Protocol.DEFAULT_TIMEOUT;
    protected String redisPassword = null;
    protected JedisPool redisConnectionPool;

    private void initializeJedisConnectionPool() {
        try {
            redisConnectionPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort, redisTimeout, redisPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Jedis acquireConnection() {
        if (null == redisConnectionPool) {
            initializeJedisConnectionPool();
        }
        Jedis jedis = redisConnectionPool.getResource();

        if (redisDatabase != 0) {
            jedis.select(redisDatabase);
        }

        return jedis;
    }

    protected void returnConnection(Jedis jedis, Boolean error) {
//        if (error) {
//            redisConnectionPool.returnBrokenResource(jedis);
//        } else {
//            redisConnectionPool.returnResource(jedis);
//        }
        jedis.close();
    }

    protected void returnConnection(Jedis jedis) {
        returnConnection(jedis, false);
    }

    protected RedisSessionManager getRedisSessionManager(Request request) {
        RedisSessionManager sessionManager = null;
        ApplicationContextFacade appContextFacadeObj = (ApplicationContextFacade) request.session().raw().getServletContext();
        try {
            Field applicationContextField = appContextFacadeObj.getClass().getDeclaredField("context");
            applicationContextField.setAccessible(true);
            ApplicationContext appContextObj = (ApplicationContext) applicationContextField.get(appContextFacadeObj);
            Field standardContextField = appContextObj.getClass().getDeclaredField("context");
            standardContextField.setAccessible(true);
            StandardContext standardContextObj = (StandardContext) standardContextField.get(appContextObj);
            sessionManager = (RedisSessionManager) standardContextObj.getManager();
        } catch (Exception e) {
            log.error("", e);
        }
        return sessionManager;
    }

    protected void updateSessionFromQueryParamsMap(Session session, QueryParamsMap queryParamsMap) {
        for (Entry<String, String[]> kv : queryParamsMap.toMap().entrySet()) {
            String key = kv.getKey();
            QueryParamsMap subParamsMap = queryParamsMap.get(kv.getKey());
            if (subParamsMap.hasKeys()) {
                Object currentValue = session.attribute(key);
                Map<String, Object> subMap;
                if (currentValue instanceof Map) {
                    subMap = (Map<String, Object>) currentValue;
                } else {
                    subMap = new HashMap<String, Object>();
                    session.attribute(key, subMap);
                }
                updateMapFromQueryParamsMap(subMap, subParamsMap);
            } else if (subParamsMap.hasValue()) {
                Object value = subParamsMap.value();
                //log.info("found key " + key + " and value " + (null == value ? "`null`" : value.toString()));
                session.attribute(key, value);
            }
        }
    }

    protected void updateMapFromQueryParamsMap(Map map, QueryParamsMap queryParamsMap) {
        for (Entry<String, String[]> kv : queryParamsMap.toMap().entrySet()) {
            String key = kv.getKey();
            QueryParamsMap subParamsMap = queryParamsMap.get(kv.getKey());
            if (subParamsMap.hasKeys()) {
                Object currentValue = map.get(key);
                Map<String, Object> subMap;
                if (currentValue instanceof Map) {
                    subMap = (Map<String, Object>) currentValue;
                } else {
                    subMap = new HashMap<String, Object>();
                    map.put(key, subMap);
                }
                updateMapFromQueryParamsMap(subMap, subParamsMap);
            } else if (subParamsMap.hasValue()) {
                Object value = subParamsMap.value();
                //log.info("found key " + key + " and value " + (null == value ? "`null`" : value.toString()));
                map.put(key, value);
            }
        }
    }

    @Override
    public void init() {
        get("/", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("../../index.jsp")) {
                    return IOUtils.toString(resource);
                }
            }
        });

        // /session

        SessionJsonTransformerRoute sessionJsonTransformerRoute = new SessionJsonTransformerRoute();
        get("/session", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                Session session = request.session(false);
                return session;
            }
        }, sessionJsonTransformerRoute);


        put("/session", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                Session session = request.session();
                QueryParamsMap queryMap = request.queryMap();
                updateSessionFromQueryParamsMap(session, queryMap);
                return session;
            }
        }, sessionJsonTransformerRoute);

        post("/session", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                Session session = request.session();
                QueryParamsMap queryMap = request.queryMap();
                updateSessionFromQueryParamsMap(session, queryMap);
                return session;
            }
        }, sessionJsonTransformerRoute);

        delete("/session", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                request.session().raw().invalidate();
                return null;
            }
        }, sessionJsonTransformerRoute);


        // /session/attributes

        get("/session/attributes", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("keys", request.session().attributes());
                return new Object[]{request.session(), map};
            }
        }, sessionJsonTransformerRoute);

        get("/session/attributes/:key", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                String key = request.params(":key");
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", key);
                map.put("value", request.session().attribute(key));
                return new Object[]{request.session(), map};
            }
        }, sessionJsonTransformerRoute);

        post("/session/attributes/:key", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                String key = request.params(":key");
                String oldValue = request.session().attribute(key);
                request.session().attribute(key, request.queryParams("value"));
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", key);
                map.put("value", request.session().attribute(key));
                map.put("oldValue", oldValue);
                if (null != request.queryParams("sleep")) {
                    try {
                        java.lang.Thread.sleep(Integer.parseInt(request.queryParams("sleep")));
                    } catch (InterruptedException e) {}
                }
                return new Object[]{request.session(), map};
            }
        }, sessionJsonTransformerRoute);

        delete("/session/attributes/:key", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                String key = request.params(":key");
                String oldValue = request.session().attribute(key);
                request.session().raw().removeAttribute(key);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", key);
                map.put("value", request.session().attribute(key));
                map.put("oldValue", oldValue);
                return new Object[]{request.session(), map};
            }
        }, sessionJsonTransformerRoute);


        // /sessions
        JsonTransformerRoute jsonTransformerRoute = new JsonTransformerRoute();
        get("/sessions", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                Jedis jedis = null;
                Boolean error = true;
                try {
                    jedis = acquireConnection();
                    Set<String> keySet = jedis.keys("*");
                    error = false;
                    return keySet.toArray(new String[keySet.size()]);
                } finally {
                    if (jedis != null) {
                        returnConnection(jedis, error);
                    }
                }
            }
        }, jsonTransformerRoute);

        delete("/sessions", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                Jedis jedis = null;
                Boolean error = true;
                try {
                    jedis = acquireConnection();
                    jedis.flushDB();
                    Set<String> keySet = jedis.keys("*");
                    error = false;
                    return keySet.toArray(new String[keySet.size()]);
                } finally {
                    if (jedis != null) {
                        returnConnection(jedis, error);
                    }
                }
            }
        }, jsonTransformerRoute);


        // /settings

        get("/settings/:key", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                String key = request.params(":key");
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", key);

                RedisSessionManager manager = getRedisSessionManager(request);
                if (null != manager) {
                    if (key.equals("sessionPersistPolicies")) {
                        map.put("value", manager.getSessionPersistPolicies());
                    } else if (key.equals("maxInactiveInterval")) {
                        map.put("value", new Integer(manager.getMaxInactiveInterval()));
                    }
                } else {
                    map.put("error", new Boolean(true));
                }

                return new Object[]{request.session(), map};
            }
        }, sessionJsonTransformerRoute);

        post("/settings/:key", "application/json", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                String key = request.params(":key");
                String value = request.queryParams("value");
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", key);

                RedisSessionManager manager = getRedisSessionManager(request);
                if (null != manager) {
                    if (key.equals("sessionPersistPolicies")) {
                        manager.setSessionPersistPolicies(value);
                        map.put("value", manager.getSessionPersistPolicies());
                    } else if (key.equals("maxInactiveInterval")) {
                        manager.setMaxInactiveInterval(Integer.parseInt(value));
                        map.put("value", new Integer(manager.getMaxInactiveInterval()));
                    }
                } else {
                    map.put("error", new Boolean(true));
                }

                return new Object[]{request.session(), map};
            }
        }, sessionJsonTransformerRoute);

    }

}
