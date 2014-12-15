package com.devsmart.miniweb.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;


public class UriQueryParserTest {

    @Test
    public void test() {

        String uri = "/stuff/test?param1=itworked&param2=rad&param1=cool";

        Map<String, List<String>> params = UriQueryParser.getUrlParameters(uri);

        assertNotNull(params);
        assertEquals(2, params.get("param1").size());
        assertEquals("itworked", params.get("param1").get(0));
        assertEquals("cool", params.get("param1").get(1));

        assertEquals(1, params.get("param2").size());
        assertEquals("rad", params.get("param2").get(0));

    }
}
