package org.devsmart.miniweb;


import org.apache.http.protocol.HttpRequestHandlerMapper;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.devsmart.miniweb.handlers.FileSystemRequestHandler;
import org.devsmart.miniweb.impl.DefaultConnectionPolicy;

import java.io.File;
import java.util.Objects;

public class ServerBuilder {

    private int mPort = 8080;
    private ConnectionPolicy mConnectionPolicy = new DefaultConnectionPolicy(30);
    private HttpRequestHandlerMapper mRequestMapper;
    private UriHttpRequestHandlerMapper mUriMapper = new UriHttpRequestHandlerMapper();

    public ServerBuilder port(int port) {
        mPort = port;
        return this;
    }

    public ServerBuilder connectionPolicy(ConnectionPolicy policy){
        mConnectionPolicy = policy;
        return this;
    }

    private String trimPattern(String pattern){
        String retval = pattern;
        int i = pattern.lastIndexOf('*');
        if(i > 0){
            retval = pattern.substring(0, i);
        }
        if(retval.endsWith("/")){
            retval = retval.substring(0, retval.length()-1);
        }
        return retval;

    }

    public ServerBuilder mapController(String pattern, Object... controllers) {
        ControllerBuilder builder = new ControllerBuilder();
        builder.withPathPrefix(trimPattern(pattern));
        for(Object controller : controllers){
            builder.addController(controller);
        }
        mUriMapper.register(pattern, builder.create());
        return this;
    }

    public ServerBuilder mapDirectory(String pattern, File fsRoot) {
        mapDirectory(pattern, fsRoot, "");
        return this;
    }

    public ServerBuilder mapDirectory(String pattern, File fsRoot, String prefix) {
        mUriMapper.register(pattern, new FileSystemRequestHandler(fsRoot, prefix));
        return this;
    }

    public ServerBuilder requestHandlerMapper(HttpRequestHandlerMapper mapper) {
        mRequestMapper = mapper;
        return this;
    }

    public Server create() {
        Server server = new Server();
        server.port = mPort;
        server.connectionPolity = mConnectionPolicy;
        if(mRequestMapper == null){
            mRequestMapper = mUriMapper;
        }
        server.requestHandlerMapper = mRequestMapper;

        return server;
    }

}
