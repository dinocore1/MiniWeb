package com.devsmart.miniweb.handlers.controller;


import com.google.gson.Gson;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import com.devsmart.miniweb.utils.RequestMethod;
import com.google.gson.stream.JsonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;

public class ControllerInvoker implements HttpRequestHandler {

    public final Logger logger = LoggerFactory.getLogger(ControllerInvoker.class);

    public PathVarCapture pathEndpoint;
    public Object instance;
    public Method method;
    public ParamHandler[] paramHandlers;
    public boolean serializeRetval;
    private Gson mGson;
    public RequestMethod requestMethod;
    public String pathPrefix;

    public ControllerInvoker(Gson gson) {
        mGson = gson;
    }

    private Gson getGson() {
        return mGson;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Object[] params = new Object[paramHandlers.length];
        for(int i=0;i<params.length;i++){
            params[i] = paramHandlers[i].createParam(request, response, context, this);
        }

        try {
            Object retval = method.invoke(instance, params);
            if(serializeRetval){
                Gson gson = getGson();
                String respStr = gson.toJson(retval);
                StringEntity respEntity = new StringEntity(respStr, HTTP.UTF_8);
                respEntity.setContentType("application/json");
                response.setEntity(respEntity);
            }
        } catch (Exception e) {
            logger.error("error invoking controller method: {}:{}", instance.getClass().getName(), method.getName(), e);
            throw new HttpException();
        }
    }

    public JsonWriter createJsonWriter(HttpResponse response) {
        PipedOutputStream out = new PipedOutputStream();
        final JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out));

        try {
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContentType("application/json");
            entity.setContent(new PipedInputStream(out));
            response.setEntity(entity);
        } catch (IOException e){
            logger.error("error sending json response: {}", e);
        }

        return jsonWriter;
    }



}
