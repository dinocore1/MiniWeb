package org.devsmart.miniweb;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.devsmart.miniweb.handlers.ReflectionControllerRequestHandler;
import org.devsmart.miniweb.handlers.controller.Controller;
import org.devsmart.miniweb.handlers.controller.RequestParam;
import org.devsmart.miniweb.handlers.controller.RequestMapping;
import org.devsmart.miniweb.handlers.controller.RequestMethod;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class ReflectionControllerTest {


    @Controller("stuff/")
    public static class MyController {

        public void notAHandler() {

        }

        @RequestMapping(value = "test", method = RequestMethod.Get)
        public void handleGet(HttpRequest request, HttpResponse response, @RequestParam("param1") String[] param1) throws Exception {

            System.out.println("handleGet invoked");

            assertEquals("itworked", param1[0]);
            assertEquals("cool", param1[1]);

            StringEntity retval = new StringEntity(param1[0]);
            response.setEntity(retval);
        }
    }

    @Test
    public void test() throws Exception {

        MyController controller = new MyController();

        ReflectionControllerRequestHandler handler = new ControllerBuilder()
            .addController(controller)
            .withPathPrefix("/")
            .create();


        HttpCoreContext context = HttpCoreContext.create();
        HttpRequest request = DefaultHttpRequestFactory.INSTANCE.newHttpRequest("GET", "/stuff/test?param1=itworked&param1=cool");
        HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, context);

        handler.handle(request, response, context);

        assertTrue(response.getStatusLine().getStatusCode() == 200);
        String resultBody = EntityUtils.toString(response.getEntity());
        assertTrue("itworked".equals(resultBody));



    }
}
