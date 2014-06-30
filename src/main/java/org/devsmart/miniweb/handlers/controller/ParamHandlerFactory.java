package org.devsmart.miniweb.handlers.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.devsmart.miniweb.utils.UriQueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamHandlerFactory {

    public final Logger logger = LoggerFactory.getLogger(ParamHandlerFactory.class);

    public ParamHandler createParamHandler(final Class<?> paramType, Annotation[] annotations){

        if(HttpRequest.class.equals(paramType)){
            return new ParamHandler() {
                @Override
                public Object createParam(HttpRequest request, HttpResponse response, HttpContext context) {
                    return request;
                }
            };
        } else if(HttpResponse.class.equals(paramType)){
            return new ParamHandler() {
                @Override
                public Object createParam(HttpRequest request, HttpResponse response, HttpContext context) {
                    return response;
                }
            };
        } else if(HttpContext.class.equals(paramType)){
            return new ParamHandler() {
                @Override
                public Object createParam(HttpRequest request, HttpResponse response, HttpContext context) {
                    return context;
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
            }
        }

        return null;
    }

    private ParamHandler bodyParam(final Class<?> paramType, Body body) {
        return new ParamHandler() {
            @Override
            public Object createParam(HttpRequest request, HttpResponse response, HttpContext context) {
                try {
                    if(request instanceof HttpEntityEnclosingRequest){
                        HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
                        HttpEntity entityBody = entityRequest.getEntity();
                        Header contentTypeHeader = entityBody.getContentType();

                        if(contentTypeHeader != null && contentTypeHeader.getValue().contains("json")){
                            String resultBody = EntityUtils.toString(entityBody);
                            Gson gson = new GsonBuilder().create();
                            return gson.fromJson(resultBody, paramType);
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
            public Object createParam(HttpRequest request, HttpResponse response, HttpContext context) {

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
