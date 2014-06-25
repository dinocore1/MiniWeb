package org.devsmart.miniweb;

import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.devsmart.miniweb.handlers.FileSystemRequestHandler;
import org.devsmart.miniweb.impl.DefaultConnectionPolicy;
import org.junit.Test;

import java.io.File;

public class ServerTest {


    @Test
    public void test1() throws Exception {

        UriHttpRequestHandlerMapper mapper = new UriHttpRequestHandlerMapper();
        mapper.register("*", new FileSystemRequestHandler(new File("test"), null));

        Server server = new ServerBuilder()
                .port(9000)
                .requestHandlerMapper(mapper)
                .create();

        server.start();


        //let it start up
        Thread.sleep(500);

        /*
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet("http://localhost:9000/");

        System.out.println("Executing request " + httpget.getRequestLine());
        // Create a custom response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

            public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
        String responseBody = httpclient.execute(httpget, responseHandler);
        System.out.println("----------------------------------------");
        System.out.println(responseBody);
        */
        boolean running = true;
        while(running) {
            Thread.sleep(1000);
        }

        server.shutdown();

    }
}
