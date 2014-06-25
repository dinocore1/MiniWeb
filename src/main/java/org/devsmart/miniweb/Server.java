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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {

    public final Logger logger = LoggerFactory.getLogger(Server.class);

    public final int port;
    private final ExecutorService mWorkerThreads = new ThreadPoolExecutor(1, 10, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));
    private final HttpRequestHandlerMapper requestHandlerMapper;
    private Thread mListenThread;
    private boolean mRunning = false;
    private HttpCoreContext mContext;
    private ConnectionPolicy mRemoteConnectionPolity;
    private HashMap<InetAddress, ArrayList<HttpServerConnection>> mConnectionMap = new HashMap<InetAddress, ArrayList<HttpServerConnection>>();

    public Server(int port, HttpRequestHandlerMapper requestHandlerMapper){
        this.port = port;
        this.requestHandlerMapper = requestHandlerMapper;
    }

    public void setConnectionPolicy(ConnectionPolicy policy){
        mRemoteConnectionPolity = policy;
    }

    public void start() throws IOException {

        if(mRunning){
            throw new IOException("server already running");
        }

        final ServerSocket mServerSocket = new ServerSocket(port);
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
                        if(mRemoteConnectionPolity.accept(socket)){
                            logger.debug("accepting connection from: " + socket.getInetAddress());
                            DefaultBHttpServerConnection connection = DefaultBHttpServerConnectionFactory.INSTANCE.createConnection(socket);
                            RemoteConnection remoteConnection = new RemoteConnection(socket.getInetAddress(), connection);
                            mRemoteConnectionPolity.connectionOpened(remoteConnection);
                            mWorkerThreads.execute(new WorkerTask(httpService, remoteConnection));
                        } else {
                            logger.debug("rejecting connection from: " + socket.getInetAddress());

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
                logger.info("client closed connection");

            } catch(SocketTimeoutException e){
                logger.debug("timing out connection");
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
                mRemoteConnectionPolity.connectionClosed(remoteConnection);
            }

        }
    }

}
