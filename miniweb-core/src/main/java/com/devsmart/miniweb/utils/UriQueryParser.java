package com.devsmart.miniweb.utils;


import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriQueryParser {

    private static final String PATH_PATTERN = "([^?#]+)";

    private static final String QUERY_PATTERN = "([^#]*)";

    private static final String LAST_PATTERN = "(.*)";

    public static final Pattern URI_PATTERN = Pattern.compile(PATH_PATTERN + "(\\?" + QUERY_PATTERN + ")?" + "(#" + LAST_PATTERN + ")?");

    private static final Pattern QUERY_PARAM_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");

    public static String getUrlPath(String uri){
        String retval = null;
        Matcher m = URI_PATTERN.matcher(uri);
        if(m.find()){
            retval = m.group(1);
        }
        return retval;
    }

    public static String getQuery(String uri) {
        int start = uri.indexOf("?");
        if(start >= 0) {
            return uri.substring(start+1);
        } else {
            return "";
        }

    }

    public static Map<String, List<String>> getUrlParameters(String url) {
        try {

            final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
            final String[] pairs = getQuery(url).split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<String>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                query_pairs.get(key).add(value);
            }
            return query_pairs;

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
