package org.devsmart.miniweb.handlers.controller;


import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;

public class ControllerInvoker implements HttpRequestHandler {

    public final Logger logger = LoggerFactory.getLogger(ControllerInvoker.class);

    public String pathEndpoint;
    public Object instance;
    public Method method;
    public ParamHandler[] paramHandlers;

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {

        Object[] params = new Object[paramHandlers.length];
        for(int i=0;i<params.length;i++){
            params[i] = paramHandlers[i].createParam(request, response, context);
        }

        try {
            Object retval = method.invoke(instance, params);
        } catch (Exception e) {
            logger.error("error invoking controller method: {}:{}", instance.getClass().getName(), method.getName(), e);
            throw new HttpException();
        }
    }



}
