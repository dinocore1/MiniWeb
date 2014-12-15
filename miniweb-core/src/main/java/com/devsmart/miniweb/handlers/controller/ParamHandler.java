package com.devsmart.miniweb.handlers.controller;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public interface ParamHandler {

    public Object createParam(HttpRequest request, HttpResponse response, HttpContext context);
}
