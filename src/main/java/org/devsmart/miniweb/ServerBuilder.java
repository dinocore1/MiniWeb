package org.devsmart.miniweb;


import org.apache.http.protocol.HttpRequestHandlerMapper;
import org.devsmart.miniweb.impl.DefaultConnectionPolicy;

public class ServerBuilder {

    private int mPort = 8080;
    private ConnectionPolicy mConnectionPolicy = new DefaultConnectionPolicy(3);
    private HttpRequestHandlerMapper mRequestMapper;

    public ServerBuilder port(int port) {
        mPort = port;
        return this;
    }

    public ServerBuilder connectionPolicy(ConnectionPolicy policy){
        mConnectionPolicy = policy;
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
        server.requestHandlerMapper = mRequestMapper;

        return server;
    }

}
