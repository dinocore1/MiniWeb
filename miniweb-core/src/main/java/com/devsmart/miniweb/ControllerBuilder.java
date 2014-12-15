package com.devsmart.miniweb;


import com.devsmart.miniweb.handlers.ReflectionControllerRequestHandler;
import com.devsmart.miniweb.handlers.controller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;

public class ControllerBuilder {

    public final Logger logger = LoggerFactory.getLogger(ControllerBuilder.class);

    private ParamHandlerFactory mParamFactory = new ParamHandlerFactory();

    private LinkedList<ControllerInvoker> mInvokers = new LinkedList<ControllerInvoker>();
    private String mPrefix;


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
            RequestMapping mapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
            if(mapping != null){

                for(String pathsegment : mapping.value()){
                    ControllerInvoker caller = new ControllerInvoker();
                    caller.requestMethod = mapping.method();
                    caller.pathPrefix = prefix + "/";
                    caller.pathEndpoint = new PathVarCapture(pathsegment);
                    caller.instance = controller;
                    caller.method = method;
                    caller.serializeRetval = AnnotationUtils.findAnnotation(method, Body.class) != null;


                    Class<?>[] paramTypes = method.getParameterTypes();
                    caller.paramHandlers = new ParamHandler[paramTypes.length];

                    boolean allHandlersResolved = true;

                    for(int i=0;i<caller.paramHandlers.length;i++){
                        Class<?> paramClass = paramTypes[i];
                        Annotation[] annotations = method.getParameterAnnotations()[i];
                        caller.paramHandlers[i] = mParamFactory.createParamHandler(paramClass, annotations);

                        if(caller.paramHandlers[i] == null){
                            allHandlersResolved = false;
                            break;
                        }

                    }

                    if(!allHandlersResolved){
                        logger.warn("Could not resolve handler {}:{}", controllerClass.getName(), method.getName());
                    } else {
                        logger.info("Mapped {} {}{} --> {}:{}", caller.requestMethod, caller.pathPrefix, caller.pathEndpoint, controllerClass.getName(), method.getName());
                        mInvokers.add(caller);

                    }


                }

            }

        }

        return this;

    }

    public ControllerBuilder withPathPrefix(String prefix) {
        mPrefix = prefix;
        return this;
    }

    public ReflectionControllerRequestHandler create() {
        return new ReflectionControllerRequestHandler(mInvokers, mPrefix);
    }


}
