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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ReflectionControllerRequestHandler implements HttpRequestHandler {

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
        final String path = UriQueryParser.getUrlPath(uri);

        boolean handled = false;
        for(ControllerInvoker invoker : mInvokers){
            String invokerPath = mPrefix + invoker.pathEndpoint;
            if(requestMethod.equals(invoker.requestMethod.name()) && path.equals(invokerPath)){
                invoker.handle(request, response, context);
                handled = true;
                break;
            }
        }

        if(!handled){
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        }

    }
}
