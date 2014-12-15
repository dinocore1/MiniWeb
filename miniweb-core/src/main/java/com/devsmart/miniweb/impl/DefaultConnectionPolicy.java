package com.devsmart.miniweb.impl;


import com.devsmart.miniweb.ConnectionPolicy;
import com.devsmart.miniweb.RemoteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DefaultConnectionPolicy implements ConnectionPolicy {

    public final Logger logger = LoggerFactory.getLogger(DefaultConnectionPolicy.class);

    int mMaxConnectionPerClient;

    private HashMap<InetAddress, ArrayList<RemoteConnection>> mCurrentConnections = new HashMap<InetAddress, ArrayList<RemoteConnection>>();

    public DefaultConnectionPolicy(int maxConnectionsPerClient) {
        mMaxConnectionPerClient = maxConnectionsPerClient;
    }

    @Override
    public boolean accept(Socket socket) {
        InetAddress remoteAddress = socket.getInetAddress();
        ArrayList<RemoteConnection> currentConnections = mCurrentConnections.get(remoteAddress);
        if(currentConnections == null || currentConnections.size() < mMaxConnectionPerClient){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void connectionOpened(RemoteConnection connection) {
        ArrayList<RemoteConnection> currentConnections = mCurrentConnections.get(connection.remoteAddress);
        if(currentConnections == null){
            currentConnections = new ArrayList<RemoteConnection>(mMaxConnectionPerClient);
            mCurrentConnections.put(connection.remoteAddress, currentConnections);
        }
        currentConnections.add(connection);

        int timeout = connection.connection.getSocketTimeout();
        connection.connection.setSocketTimeout(20000);

        logger.debug("num connections: {}", getNumLiveConnections());

    }

    @Override
    public synchronized void connectionClosed(RemoteConnection connection) {
        ArrayList<RemoteConnection> currentConnections = mCurrentConnections.get(connection.remoteAddress);
        if(currentConnections != null){
            currentConnections.remove(connection);
            if(currentConnections.size() == 0){
                mCurrentConnections.remove(connection.remoteAddress);
            }
        }

        logger.debug("num connections: {}", getNumLiveConnections());

    }

    public int getNumLiveConnections() {
        int retval = 0;
        for(Map.Entry<InetAddress, ArrayList<RemoteConnection>> entry : mCurrentConnections.entrySet()){
            retval += entry.getValue().size();
        }
        return retval;
    }
}
