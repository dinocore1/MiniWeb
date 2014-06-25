package org.devsmart.miniweb;


import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.devsmart.miniweb.handlers.controller.Controller;
import org.devsmart.miniweb.handlers.controller.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ControllerBuilder {

    public final Logger logger = LoggerFactory.getLogger(ControllerBuilder.class);

    private class Caller implements HttpRequestHandler {
        String pathSegment;
        Object instance;
        Method method;


        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {

        }
    }

    private HashMap<String, Caller> mMapping = new HashMap<String, Caller>();

    private static String trimPath(String path) {
        path = path.trim();
        int i = path.lastIndexOf('/');
        if(i > 0) {
            path = path.substring(0, i);
        }

        return path;
    }

    public ControllerBuilder addController(Object controller) {
        final Class<? extends Object> controllerClass = controller.getClass();

        String prefix = "";
        Controller c = controllerClass.getAnnotation(Controller.class);
        if(c != null){
            prefix += c.value();
        }

        prefix = trimPath(prefix);


        for(Method method : controllerClass.getMethods()){
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);

            for(String pathsegment : mapping.value()){
                pathsegment = trimPath(pathsegment);

                Caller caller = new Caller();
                caller.pathSegment = prefix + "/" + pathsegment;
                caller.instance = controller;
                caller.method = method;



                mMapping.put(caller.pathSegment, caller);
            }


        }

        return this;

    }
}
