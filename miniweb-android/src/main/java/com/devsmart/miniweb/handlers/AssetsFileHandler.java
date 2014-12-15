package com.devsmart.miniweb.handlers;


import android.content.Context;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import com.devsmart.miniweb.utils.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssetsFileHandler implements HttpRequestHandler {

    Logger logger = LoggerFactory.getLogger(AssetsFileHandler.class);

    private final Context mContext;
    private final String mRoot;
    private final Pattern mPrefix;

    public AssetsFileHandler(Context context, String root, String prefix) {
        mContext = context;
        mRoot = root;
        mPrefix = Pattern.compile(prefix != null ? "^" + prefix.trim() : "^");
    }

    public AssetsFileHandler(Context context, String root) {
        this(context, root, "/");
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        try {
            if (RequestMethod.GET.name().equals(httpRequest.getRequestLine().getMethod())) {

                URI url = new URI(httpRequest.getRequestLine().getUri());
                String path = url.getPath();
                if("/".equals(path)){
                    handleRoot(httpRequest, response, httpContext);
                    return;
                }
                Matcher m = mPrefix.matcher(path);
                if(m.find()){
                    path = path.substring(m.end(), path.length());
                }

                String filePath = mRoot + "/" + path;
                String filename = filePath.substring(filePath.lastIndexOf("/")+1, filePath.length());
                attachAssetToResponse(response, getAssetFile(filePath), filename);



            } else {
                //method not allowed
                response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            logger.error("", e);
            response.setStatusCode(500);
        }

    }

    private InputStream getAssetFile(String path) throws IOException {
        return mContext.getAssets().open(path);
    }

    private void attachAssetToResponse(HttpResponse response, InputStream inputStream, String filename) throws IOException {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(inputStream);
        body.setChunked(true);
        response.setEntity(body);
    }

    final String[] sIndexFiles = new String[]{
            "index.html",
            "index.htm"
    };

    private void handleRoot(HttpRequest request, HttpResponse response, HttpContext context) {
        for(String filename : sIndexFiles){
            try {
                attachAssetToResponse(response, getAssetFile(mRoot + "/" + filename), filename);
                return;
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        response.setStatusCode(HttpStatus.SC_NOT_FOUND);
    }
}
