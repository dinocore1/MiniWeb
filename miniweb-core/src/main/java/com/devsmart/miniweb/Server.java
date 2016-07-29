package com.devsmart.miniweb;

import org.apache.http.*;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class Server {

    public final Logger logger = LoggerFactory.getLogger(Server.class);

    protected int port;
    protected HttpRequestHandlerResolver requestHandlerResolver;
    protected ConnectionPolicy connectionPolity;

    private final ExecutorService mWorkerThreads = Executors.newCachedThreadPool();

    private Thread mListenThread;
    private boolean mRunning = false;
    private BasicHttpContext mContext;

    public void start() throws IOException {

        if(mRunning){
            throw new IOException("server already running");
        }

        final ServerSocket mServerSocket = new ServerSocket(port);
        logger.info("Server started listening on {}", mServerSocket.getLocalSocketAddress());
        //mServerSocket.setSoTimeout(1000);
        mRunning = true;

        mListenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Set up the HTTP protocol processor

                mContext = new BasicHttpContext();
                BasicHttpProcessor httpproc = new BasicHttpProcessor();
                httpproc.addResponseInterceptor(new ResponseDate());
                httpproc.addResponseInterceptor(new ResponseServer());
                httpproc.addResponseInterceptor(new ResponseContent());
                httpproc.addResponseInterceptor(new ResponseConnControl());

                DefaultHttpResponseFactory responseFactory = new DefaultHttpResponseFactory();
                HttpParams params = new BasicHttpParams();
                HttpService httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(), responseFactory);
                httpService.setHandlerResolver(requestHandlerResolver);
                httpService.setParams(params);

                while(mRunning){
                    try {
                        Socket socket = mServerSocket.accept();


                        if(connectionPolity.accept(socket)){
                            logger.debug("accepting connection from: {}", socket.getRemoteSocketAddress());

                            DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
                            connection.bind(socket, new BasicHttpParams());
                            RemoteConnection remoteConnection = new RemoteConnection(socket.getInetAddress(), connection);
                            connectionPolity.connectionOpened(remoteConnection);
                            mWorkerThreads.execute(new WorkerTask(httpService, remoteConnection));
                        } else {
                            logger.debug("rejecting connection from: {}", socket);

                            try {
                                DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
                                connection.bind(socket, new BasicHttpParams());
                                HttpResponse response = responseFactory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_0, HttpStatus.SC_SERVICE_UNAVAILABLE, ""), mContext);
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

            } catch (IOException e) {
                logger.warn("IO error: " + e.getMessage());

            } catch (HttpException e) {
                logger.warn("Unrecoverable HTTP protocol violation: " + e.getMessage());

            } catch (Exception e){
                logger.warn("unknown error: {}", e);
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
