package org.devsmart.miniweb.handlers.controller;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathVarCapture {

    public static final Pattern VarPattern = Pattern.compile("{([^}/]+)}");

    private final String mPathRegex;
    private String[] mId;

    public PathVarCapture(String pathTemplate) {

        ArrayList<String> ids = new ArrayList<String>();
        StringBuilder path = new StringBuilder();

        int start = 0;

        Matcher m = VarPattern.matcher(pathTemplate);
        while(m.find()){
            path.append(pathTemplate.substring(start, m.regionStart()));
            path.append("([^/]+)");
            start = m.regionEnd();
            String id = m.group(1);
            ids.add(id);
        }

        mPathRegex = path.toString();

    }


}
