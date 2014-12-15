package com.devsmart.miniweb.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriQueryParser {

    private static final String PATH_PATTERN = "([^?#]*)";

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

    public static Map<String, List<String>> getUrlParameters(String uri) {

        Map<String, List<String>> params = new HashMap<String, List<String>>();

        Matcher m = URI_PATTERN.matcher(uri);
        if(m.find()){
            String query = m.group(2);
            query = query.substring(1);

            if (query != null) {
                m = QUERY_PARAM_PATTERN.matcher(query);
                while (m.find()) {
                    String name = m.group(1);
                    String eq = m.group(2);
                    String value = m.group(3);

                    List<String> values = params.get(name);
                    if(values == null){
                        values = new ArrayList<String>();
                        params.put(name, values);
                    }
                    values.add(value == null ? "" : value);
                }
            }
        }



        return params;
    }
}
