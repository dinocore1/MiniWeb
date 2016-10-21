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

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    int port;
    HttpRequestHandlerResolver requestHandlerResolver;

    private final ExecutorService mWorkerThreads = Executors.newCachedThreadPool();

    private ServerSocket mServerSocket;
    private SocketListener mListenThread;
    private boolean mRunning = false;
    private BasicHttpContext mContext = new BasicHttpContext();

    public void start() throws IOException {
        if(mRunning){
            LOGGER.warn("server already running");
            return;
        }

        mServerSocket = new ServerSocket(port);
        LOGGER.info("Server started listening on {}", mServerSocket.getLocalSocketAddress());

        mRunning = true;

        mListenThread = new SocketListener();
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
                LOGGER.error("", e);
            }
            LOGGER.info("Server shutdown");
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
                LOGGER.debug("client closed connection {}", remoteConnection.connection);

            } catch (IOException e) {
                LOGGER.warn("IO error: " + e.getMessage());

            } catch (HttpException e) {
                LOGGER.warn("Unrecoverable HTTP protocol violation: " + e.getMessage());

            } catch (Exception e){
                LOGGER.warn("unknown error: {}", e);
            } finally {
                shutdown();
            }


        }

        public void shutdown() {
            try {
                remoteConnection.connection.shutdown();
            } catch (IOException e){
                LOGGER.error("", e);
            }

        }
    }

    private class SocketListener extends Thread {

        @Override
        public void run() {
            // Set up the HTTP protocol processor
            BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
            httpProcessor.addResponseInterceptor(new ResponseDate());
            httpProcessor.addResponseInterceptor(new ResponseServer());
            httpProcessor.addResponseInterceptor(new ResponseContent());
            httpProcessor.addResponseInterceptor(new ResponseConnControl());

            DefaultHttpResponseFactory responseFactory = new DefaultHttpResponseFactory();
            HttpParams params = new BasicHttpParams();
            HttpService httpService = new HttpService(httpProcessor, new DefaultConnectionReuseStrategy(), responseFactory);
            httpService.setHandlerResolver(requestHandlerResolver);
            httpService.setParams(params);

            while(mRunning){
                try {
                    Socket socket = mServerSocket.accept();

                    LOGGER.info("accepting connection from: {}", socket.getRemoteSocketAddress());

                    DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
                    connection.bind(socket, new BasicHttpParams());
                    RemoteConnection remoteConnection = new RemoteConnection(socket.getInetAddress(), connection);

                    mWorkerThreads.execute(new WorkerTask(httpService, remoteConnection));

                } catch(SocketTimeoutException e) {
                } catch (IOException e){
                    LOGGER.error("", e);
                    mRunning = false;
                }
            }
        }
    }

}
