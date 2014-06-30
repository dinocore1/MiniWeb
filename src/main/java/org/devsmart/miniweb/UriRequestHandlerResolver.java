package org.devsmart.miniweb;

import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerResolver;
import org.devsmart.miniweb.utils.UriQueryParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UriRequestHandlerResolver implements HttpRequestHandlerResolver {


    private static class Endpoint {

        public final HttpRequestHandler mHandler;
        private final Pattern mPattern;

        public Endpoint(String pattern, HttpRequestHandler handler) {
            mPattern = Pattern.compile(pattern);
            mHandler = handler;
        }

        public boolean matches(String path) {
            Matcher m = mPattern.matcher(path);
            return m.find();
        }
    }

    private List<Endpoint> mEndpoints = new ArrayList<Endpoint>();


    @Override
    public HttpRequestHandler lookup(String requestURI) {
        String path = UriQueryParser.getUrlPath(requestURI);

        for(Endpoint ep : mEndpoints){
            if(ep.matches(path)){
                return ep.mHandler;
            }
        }

        return null;
    }

    public void register(String pattern, HttpRequestHandler handler) {
        mEndpoints.add(new Endpoint(pattern, handler));
    }
}
