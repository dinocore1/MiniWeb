package org.devsmart.miniweb.handlers;


import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.devsmart.miniweb.handlers.controller.ControllerInvoker;
import org.devsmart.miniweb.utils.UriQueryParser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReflectionControllerRequestHandler implements HttpRequestHandler {

    public static final String PATH_VARS = "path_vars";

    private final String mPrefix;
    private final List<ControllerInvoker> mInvokers;

    public ReflectionControllerRequestHandler(LinkedList<ControllerInvoker> invokers, String prefix) {
        mInvokers = invokers;
        mPrefix = prefix;
    }


    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {

        final String requestMethod = request.getRequestLine().getMethod();
        final String uri = request.getRequestLine().getUri();
        String path = UriQueryParser.getUrlPath(uri);

        if(!path.startsWith(mPrefix)){
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
            return;
        }

        path = path.substring(mPrefix.length());

        boolean handled = false;
        for(ControllerInvoker invoker : mInvokers){

            if(path.startsWith(invoker.pathPrefix) && requestMethod.equals(invoker.requestMethod.name())){
                String endpoint = path.substring(invoker.pathPrefix.length());
                if(invoker.pathEndpoint.matches(endpoint)){
                    Map<String, String> pathVars = invoker.pathEndpoint.parseUri(endpoint);
                    request.getParams().setParameter(PATH_VARS, pathVars);
                    invoker.handle(request, response, context);
                    handled = true;
                    break;
                }
            }
        }

        if(!handled){
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        }

    }
}
