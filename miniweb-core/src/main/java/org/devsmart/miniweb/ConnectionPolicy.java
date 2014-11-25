package org.devsmart.miniweb;


import java.net.Socket;

public interface ConnectionPolicy {

    public boolean accept(Socket socket);
    public void connectionOpened(RemoteConnection connection);
    public void connectionClosed(RemoteConnection connection);

}
