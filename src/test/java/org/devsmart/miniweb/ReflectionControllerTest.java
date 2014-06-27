package org.devsmart.miniweb;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.devsmart.miniweb.handlers.ReflectionControllerRequestHandler;
import org.devsmart.miniweb.handlers.controller.Body;
import org.devsmart.miniweb.handlers.controller.Controller;
import org.devsmart.miniweb.handlers.controller.RequestParam;
import org.devsmart.miniweb.handlers.controller.RequestMapping;
import org.devsmart.miniweb.utils.RequestMethod;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

public class ReflectionControllerTest {


    @Controller("stuff/")
    public static class MyController {

        public void notAHandler() {

        }

        @RequestMapping(value = "test", method = RequestMethod.Get)
        public void handleGet(HttpRequest request, HttpResponse response,
                              @RequestParam("param1") String[] param1,
                              @RequestParam("param2") String param2) throws Exception {

            System.out.println("handleGet invoked");

            assertEquals("itworked", param1[0]);
            assertEquals("cool", param1[1]);

            assertEquals("rad", param2);

            StringEntity retval = new StringEntity(param1[0]);
            response.setEntity(retval);
        }

        @RequestMapping(value = "testInt")
        public void handleTestInt(HttpResponse response,
                                  @RequestParam("apples") int numApples,
                                  @RequestParam("bananas") double numBannans) throws Exception {

            assertEquals(5, numApples);
            assertEquals(4.3, numBannans, 0.000001);
        }

        private static class TestObj {
            String name;
        }

        @RequestMapping(value = "putthis", method = RequestMethod.Put)
        public void handlePutthis(@Body TestObj obj, HttpResponse response) throws Exception {

            assertNotNull(obj);

            StringEntity retval = new StringEntity("itworked");
            response.setEntity(retval);
        }

        @RequestMapping(value = "getjson", method = RequestMethod.Get)
        @Body
        public TestObj handleGetJson() {
            TestObj retval = new TestObj();
            retval.name = "Jack";
            return retval;
        }
    }

    @Test
    public void testStringQueryParam() throws Exception {

        MyController controller = new MyController();

        ReflectionControllerRequestHandler handler = new ControllerBuilder()
            .addController(controller)
            .withPathPrefix("/")
            .create();


        HttpCoreContext context = HttpCoreContext.create();
        HttpRequest request = DefaultHttpRequestFactory.INSTANCE.newHttpRequest("GET", "/stuff/test?param1=itworked&param2=rad&param1=cool");
        HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, context);

        handler.handle(request, response, context);

        assertTrue(response.getStatusLine().getStatusCode() == 200);
        String resultBody = EntityUtils.toString(response.getEntity());
        assertTrue("itworked".equals(resultBody));

    }

    @Test
    public void testIntQueryParam() throws Exception {
        MyController controller = new MyController();

        ReflectionControllerRequestHandler handler = new ControllerBuilder()
                .addController(controller)
                .withPathPrefix("/")
                .create();


        HttpCoreContext context = HttpCoreContext.create();
        HttpRequest request = DefaultHttpRequestFactory.INSTANCE.newHttpRequest("GET", "/stuff/testInt?apples=5&bananas=4.3");
        HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, context);

        handler.handle(request, response, context);

        assertTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
    }

    @Test
    public void testJsonBody() throws Exception {
        MyController controller = new MyController();

        ReflectionControllerRequestHandler handler = new ControllerBuilder()
                .addController(controller)
                .withPathPrefix("/")
                .create();


        HttpCoreContext context = HttpCoreContext.create();
        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("PUT", "/stuff/putthis");
        request.setEntity(new StringEntity("{ \"name\": \"paul\" }", ContentType.APPLICATION_JSON));


        HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, context);

        handler.handle(request, response, context);

        assertTrue(response.getStatusLine().getStatusCode() == 200);
        String resultBody = EntityUtils.toString(response.getEntity());
        assertTrue("itworked".equals(resultBody));
    }

    @Test
    public void testJsonResponse() throws Exception {
        MyController controller = new MyController();

        ReflectionControllerRequestHandler handler = new ControllerBuilder()
                .addController(controller)
                .withPathPrefix("/")
                .create();


        HttpCoreContext context = HttpCoreContext.create();
        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("GET", "/stuff/getjson");
        HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, context);

        handler.handle(request, response, context);

        assertTrue(response.getStatusLine().getStatusCode() == 200);
        String resultBody = EntityUtils.toString(response.getEntity());

        Gson gson = new GsonBuilder().create();
        MyController.TestObj retobj = gson.fromJson(resultBody, MyController.TestObj.class);

        assertNotNull(retobj);
        assertEquals("Jack", retobj.name);
    }
}
