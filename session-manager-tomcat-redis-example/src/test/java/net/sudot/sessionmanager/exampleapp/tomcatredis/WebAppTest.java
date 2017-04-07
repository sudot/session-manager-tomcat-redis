package net.sudot.sessionmanager.exampleapp.tomcatredis;


import net.sudot.sessionmanager.exampleapp.tomcatredis.utils.HttpUtils;
import org.junit.Test;

/**
 * Created by tangjialin on 2017-04-07 0007.
 */
public class WebAppTest {
    private String baseUrl = "http://127.0.0.1:8080";
    private String sessionCookie = "JSESSIONID=97F2F5A1F0D4F195136C15031DE96BBF";
    @Test
    public void sessionGetTest() throws Exception {
        String send = HttpUtils.utils.send(baseUrl + "/session", null, HttpUtils.METHOD_GET, sessionCookie);
        System.out.println(send);
    }
    @Test
    public void sessionPostTest() throws Exception {
        String send = HttpUtils.utils.send(baseUrl + "/session", "abcd=1234", HttpUtils.METHOD_POST, sessionCookie);
        System.out.println(send);
    }
    @Test
    public void sessionPutTest() throws Exception {
        String send = HttpUtils.utils.send(baseUrl + "/session", "abcd=1234", HttpUtils.METHOD_PUT, sessionCookie);
        System.out.println(send);
    }
    @Test
    public void sessionAttributesGetTest() throws Exception {
        String send = HttpUtils.utils.send(baseUrl + "/session/attributes/qwe", "", HttpUtils.METHOD_GET, sessionCookie);
        System.out.println(send);
    }
    @Test
    public void sessionAttributesPostTest() throws Exception {
        String send = HttpUtils.utils.send(baseUrl + "/session/attributes/qwe", "value=1234", HttpUtils.METHOD_POST, sessionCookie);
        System.out.println(send);
    }

}