package com.devsmart.miniweb.handlers.controller;


import com.google.gson.Gson;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import com.devsmart.miniweb.handlers.ReflectionControllerRequestHandler;
import com.devsmart.miniweb.utils.UriQueryParser;
import com.google.gson.stream.JsonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamHandlerFactory {

    public final Logger logger = LoggerFactory.getLogger(ParamHandlerFactory.class);
    private Gson mGson;

    public ParamHandler createParamHandler(final Class<?> paramType, Annotation[] annotations, Gson gson){

        mGson = gson;

        if(HttpRequest.class.equals(paramType)){
            return new ParamHandler() {
                @Override
                public Object createParam(HttpRequest request, HttpResponse response, HttpContext context, ControllerInvoker controllerInvoker) {
                    return request;
                }
            };
        } else if(HttpResponse.class.equals(paramType)){
            return new ParamHandler() {
                @Override
                public Object createParam(HttpRequest request, HttpResponse response, HttpContext context, ControllerInvoker controllerInvoker) {
                    return response;
                }
            };
        } else if(HttpContext.class.equals(paramType)){
            return new ParamHandler() {
                @Override
                public Object createParam(HttpRequest request, HttpResponse response, HttpContext context, ControllerInvoker controllerInvoker) {
                    return context;
                }
            };
        } else if(JsonWriter.class.equals(paramType)){
            return new ParamHandler() {
                @Override
                public Object createParam(HttpRequest request, HttpResponse response, HttpContext context, ControllerInvoker controllerInvoker) {
                    return controllerInvoker.createJsonWriter(response);
                }
            };

        } else if(annotations != null){
            if(annotations != null) {
                final RequestParam paramKey = getAnnotationType(annotations, RequestParam.class);
                if(paramKey != null) {
                    return queryParam(paramType, paramKey);
                }

                final Body body = getAnnotationType(annotations, Body.class);
                if(body != null){
                    return bodyParam(paramType, body);
                }

                final PathVariable pathVar = getAnnotationType(annotations, PathVariable.class);
                if(pathVar != null){
                    return pathVar(paramType, pathVar);
                }
            }
        }

        return null;
    }

    private ParamHandler pathVar(Class<?> paramType, final PathVariable pathVar) {
        return new ParamHandler() {
            @Override
            public Object createParam(HttpRequest request, HttpResponse response, HttpContext context, ControllerInvoker controllerInvoker) {
                Map<String, String> pathParams = (Map<String, String>) request.getParams().getParameter(ReflectionControllerRequestHandler.PATH_VARS);
                return pathParams.get(pathVar.value());
            }
        };
    }

    private ParamHandler bodyParam(final Class<?> paramType, Body body) {
        return new ParamHandler() {

            boolean isJsonType(HttpEntityEnclosingRequest request) {
                HttpEntity entityBody = request.getEntity();

                Header bodyContentTypeHeader = entityBody.getContentType();
                if(bodyContentTypeHeader != null && bodyContentTypeHeader.getValue() != null &&
                        bodyContentTypeHeader.getValue().contains("json")){
                    return true;
                }

                Header contentTypeHeader = request.getFirstHeader(HTTP.CONTENT_TYPE);
                if(contentTypeHeader != null && contentTypeHeader.getValue() != null &&
                        contentTypeHeader.getValue().contains("json")){
                    return true;
                }

                return false;
            }

            @Override
            public Object createParam(HttpRequest request, HttpResponse response, HttpContext context, ControllerInvoker controllerInvoker) {
                try {
                    if(request instanceof HttpEntityEnclosingRequest){
                        HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
                        if(isJsonType(entityRequest)) {
                            String resultBody = EntityUtils.toString(entityRequest.getEntity());
                            return mGson.fromJson(resultBody, paramType);
                        }
                    }
                    return null;
                } catch(Exception e){
                    logger.error("", e);
                    return null;
                }
            }
        };
    }


    private ParamHandler queryParam(final Class<?> paramType, final RequestParam paramKey) {
        return new ParamHandler() {
            @Override
            public Object createParam(HttpRequest request, HttpResponse response, HttpContext context, ControllerInvoker controllerInvoker) {

                String uri = request.getRequestLine().getUri();
                Map<String, List<String>> params = UriQueryParser.getUrlParameters(uri);

                List<String> values = params.get(paramKey.value());
                if(values != null) {
                    if (paramType.isArray()) {
                        String[] retval = new String[values.size()];
                        int i = 0;
                        for (String value : values) {
                            retval[i++] = value;
                        }
                        return retval;
                    } else if (paramType.isAssignableFrom(List.class)) {
                        return values;
                    } else if (paramType.isAssignableFrom(Set.class)) {
                        HashSet<String> retval = new HashSet<String>(values);
                        return retval;
                    } else if (paramType.isAssignableFrom(String.class) && !values.isEmpty()){
                        return values.get(0);
                    } else if(paramType.isAssignableFrom(double.class) && !values.isEmpty()) {
                        return Double.parseDouble(values.get(0));
                    } else if (paramType.isAssignableFrom(int.class) && !values.isEmpty()) {
                        return Integer.parseInt(values.get(0));
                    } else if (paramType.isAssignableFrom(long.class) && !values.isEmpty()) {
                        return Long.parseLong(values.get(0));
                    } else if (paramType.isAssignableFrom(boolean.class) && !values.isEmpty()) {
                        return Boolean.parseBoolean(values.get(0));
                    }
                }

                return null;

            }
        };
    }

    private static <T extends Annotation> T getAnnotationType(Annotation[] annotations, Class<T> type){
        T retval = null;
        for(Annotation a : annotations){
            if(type.isAssignableFrom(a.getClass())){
                retval = (T)a;
                break;
            }
        }
        return retval;
    }

}
