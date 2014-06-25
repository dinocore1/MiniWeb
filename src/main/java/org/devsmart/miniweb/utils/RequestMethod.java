package org.devsmart.miniweb.utils;


public enum RequestMethod {
    Get("GET"),
    Post("POST"),
    Put("PUT"),
    Delete("DELETE"),
    Head("HEAD");

    public final String name;

    private RequestMethod(String name) {
        this.name = name;
    }
}
