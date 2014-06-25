package org.devsmart.miniweb;

import org.apache.http.*;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class Server {

    public final Logger logger = LoggerFactory.getLogger(Server.class);

    protected int port;
    protected HttpRequestHandlerMapper requestHandlerMapper;
    protected ConnectionPolicy connectionPolity;

    private final ExecutorService mWorkerThreads = Executors.newCachedThreadPool();

    private Thread mListenThread;
    private boolean mRunning = false;
    private HttpCoreContext mContext;

    public void start() throws IOException {

        if(mRunning){
            throw new IOException("server already running");
        }

        final ServerSocket mServerSocket = new ServerSocket(port);
        logger.info("Server started listening on {}", mServerSocket.getLocalSocketAddress());
        mServerSocket.setSoTimeout(1000);
        mRunning = true;

        mListenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Set up the HTTP protocol processor
                HttpProcessor httpproc = HttpProcessorBuilder.create()
                        .add(new ResponseDate())
                        .add(new ResponseServer("MiniWeb/0.1"))
                        .add(new ResponseContent())
                        .add(new ResponseConnControl()).build();

                mContext = HttpCoreContext.create();
                HttpService httpService = new HttpService(httpproc, requestHandlerMapper);

                while(mRunning){
                    try {
                        Socket socket = mServerSocket.accept();
                        if(connectionPolity.accept(socket)){
                            logger.debug("accepting connection from: {}", socket.getRemoteSocketAddress());
                            DefaultBHttpServerConnection connection = DefaultBHttpServerConnectionFactory.INSTANCE.createConnection(socket);
                            RemoteConnection remoteConnection = new RemoteConnection(socket.getInetAddress(), connection);
                            connectionPolity.connectionOpened(remoteConnection);
                            mWorkerThreads.execute(new WorkerTask(httpService, remoteConnection));
                        } else {
                            logger.debug("rejecting connection from: {}", socket);

                            try {
                                DefaultBHttpServerConnection connection = DefaultBHttpServerConnectionFactory.INSTANCE.createConnection(socket);
                                HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse
                                        (HttpVersion.HTTP_1_0, HttpStatus.SC_SERVICE_UNAVAILABLE,
                                                mContext);
                                connection.sendResponseHeader(response);
                                connection.sendResponseEntity(response);
                                connection.flush();
                                connection.close();
                            } catch (Exception e) {}
                        }

                    } catch(SocketTimeoutException e) {
                    } catch (IOException e){
                        logger.error("", e);
                        mRunning = false;
                    }
                }

            }
        });
        mListenThread.setName("MiniWeb Listen " + mServerSocket.getLocalSocketAddress());
        mListenThread.start();

    }

    public void shutdown() {
        if(mRunning){
            mRunning = false;
            try {
                mListenThread.join();
                mListenThread = null;
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            logger.info("Server shutdown");
        }
    }

    private class WorkerTask implements Runnable {

        private final HttpService httpservice;
        private final RemoteConnection remoteConnection;

        public WorkerTask(HttpService service, RemoteConnection connection){
            httpservice = service;
            remoteConnection = connection;
        }


        @Override
        public void run() {

            try {
                while(mRunning && remoteConnection.connection.isOpen()) {
                    httpservice.handleRequest(remoteConnection.connection, mContext);
                }
            } catch (ConnectionClosedException e) {
                logger.debug("client closed connection {}", remoteConnection.connection);

            } catch(SocketTimeoutException e){
                logger.debug("timing out connection {}", remoteConnection.connection);
                try {
                    HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse
                            (HttpVersion.HTTP_1_0, HttpStatus.SC_GATEWAY_TIMEOUT,
                                    mContext);
                    remoteConnection.connection.sendResponseHeader(response);
                    remoteConnection.connection.sendResponseEntity(response);
                    remoteConnection.connection.flush();
                } catch (Exception ex){
                    logger.error("", e);
                }

            } catch (IOException e) {
                logger.warn("IO error: " + e.getMessage());

            } catch (HttpException e) {
                logger.warn("Unrecoverable HTTP protocol violation: " + e.getMessage());

            } finally {
                shutdown();
            }


        }

        public void shutdown() {
            try {
                remoteConnection.connection.shutdown();
            } catch (IOException e){

            }
            finally {
                connectionPolity.connectionClosed(remoteConnection);
            }

        }
    }

}
