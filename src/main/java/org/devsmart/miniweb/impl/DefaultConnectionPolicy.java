package org.devsmart.miniweb.impl;


import org.devsmart.miniweb.ConnectionPolicy;
import org.devsmart.miniweb.RemoteConnection;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class DefaultConnectionPolicy implements ConnectionPolicy {

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

    }
}
