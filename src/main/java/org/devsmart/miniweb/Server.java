package org.devsmart.miniweb;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {

    public final Logger logger = LoggerFactory.getLogger(Server.class);

    public final int port;
    private final ExecutorService mWorkerThreads = new ThreadPoolExecutor(1, 5, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));
    private Thread mListenThread;
    private boolean mRunning = false;
    private HttpCoreContext mContext;

    public Server(int port){
        this.port = port;


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
                HttpService httpService = new HttpService(httpproc, null);

                while(mRunning){
                    try {
                        Socket socket = mServerSocket.accept();
                        DefaultBHttpServerConnection connection = DefaultBHttpServerConnectionFactory.INSTANCE.createConnection(socket);
                        mWorkerThreads.execute(new WorkerTask(httpService, connection));
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
        private final HttpServerConnection conn;

        public WorkerTask(HttpService service, HttpServerConnection connection){
            httpservice = service;
            this.conn = connection;
        }


        @Override
        public void run() {

            if(mRunning && conn.isOpen()){
                try {
                    httpservice.handleRequest(conn, mContext);
                    mWorkerThreads.execute(this);
                } catch (ConnectionClosedException e){
                    logger.info("client closed connection");
                    shutdown();
                } catch (IOException e) {
                    logger.warn("IO error: " + e.getMessage());
                    shutdown();
                } catch (HttpException e) {
                    logger.warn("Unrecoverable HTTP protocol violation: " + e.getMessage());
                    shutdown();
                }
            }

        }

        private void shutdown() {
            try {
                conn.shutdown();
            } catch (IOException e){}

        }
    }

}
