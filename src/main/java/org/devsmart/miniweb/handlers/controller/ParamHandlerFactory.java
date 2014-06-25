package org.devsmart.miniweb.handlers.controller;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.devsmart.miniweb.utils.UriQueryParser;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamHandlerFactory {

    public ParamHandler createParamHandler(final Class<?> paramType, Annotation[] annotation){

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
        } else if(annotation != null){
            if(annotation != null) {
                final RequestParam paramKey = getRequestParam(annotation);
                if(paramKey != null) {
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
                                }
                            }

                            return null;

                        }
                    };
                }
            }
        }

        return null;
    }

    private static RequestParam getRequestParam(Annotation[] annotations){
        RequestParam retval = null;
        for(Annotation a : annotations){
            if(a instanceof RequestParam){
                retval = (RequestParam) a;
                break;
            }
        }
        return retval;
    }
}
