package org.devsmart.miniweb.handlers.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathVarCapture {

    public static final Pattern VarPattern = Pattern.compile("\\{([^\\}/]+)\\}");

    private final Pattern mPathRegex;
    private String[] mId;
    private final String mName;

    public PathVarCapture(String pathTemplate) {
        mName = pathTemplate;

        ArrayList<String> ids = new ArrayList<String>();
        StringBuilder path = new StringBuilder();

        int start = 0;
        boolean found = false;
        Matcher m = VarPattern.matcher(pathTemplate);
        while(m.find()){
            path.append(pathTemplate.substring(start, m.start(1)-1));
            path.append("([^/]+)");
            start = m.end(1)+1;
            String id = m.group(1);
            ids.add(id);
            found = true;
        }

        if(found) {
            mPathRegex = Pattern.compile(path.toString()+"$");
        } else {
            mPathRegex = Pattern.compile(pathTemplate+"$");
        }
        mId = ids.toArray(new String[ids.size()]);
    }

    @Override
    public String toString() {
        return mName;
    }

    public boolean matches(String uri){
        return mPathRegex.matcher(uri).find();
    }

    public Map<String, String> parseUri(String uri){
        HashMap<String, String> retval = new HashMap<String, String>();

        Matcher m = mPathRegex.matcher(uri);
        if(m.find()){
            for(int i=0;i<mId.length;i++){
                String key = mId[i];
                String value = m.group(i+1);
                retval.put(key, value);
            }
        }
        return retval;
    }


}
