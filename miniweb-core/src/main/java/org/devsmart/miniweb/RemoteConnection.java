package org.devsmart.miniweb;


import org.apache.http.HttpServerConnection;

import java.net.InetAddress;

public class RemoteConnection {

    public final InetAddress remoteAddress;
    public final HttpServerConnection connection;


    public RemoteConnection(InetAddress remoteAddress, HttpServerConnection connection) {
        this.remoteAddress = remoteAddress;
        this.connection = connection;
    }
}
