package org.devsmart.miniweb.handlers;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.devsmart.miniweb.utils.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;


public class FileSystemRequestHandler implements HttpRequestHandler {

    public final Logger logger = LoggerFactory.getLogger(FileSystemRequestHandler.class);

    private final File mRoot;
    private final String mPrefix;

    public FileSystemRequestHandler(File root, String prefix) {
        this.mRoot = root;
        this.mPrefix = prefix;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {

        try {
            if (RequestMethod.Get.name.equals(request.getRequestLine().getMethod())) {

                URI url = new URI(request.getRequestLine().getUri());
                String path = url.getPath();
                if(mPrefix != null && mPrefix.trim().length() > 0){
                    path = path.substring(0, mPrefix.length());
                }
                String[] pathsegments = path.split("/");

                File file = new File(mRoot, "");
                for (String part : pathsegments) {
                    file = new File(file, part);
                }

                if (file.exists() && file.isFile()) {
                    logger.debug("sent {}", file.getPath());
                    FileEntity body = new FileEntity(file, "");
                    response.setEntity(body);
                } else {
                    response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                }

            } else {
                //method not allowed
                response.setStatusCode(405);
            }
        } catch (Exception e) {
            logger.error("", e);
            response.setStatusCode(500);
        }



    }
}
