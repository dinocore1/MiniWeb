package org.devsmart.miniweb.handlers.controllers;


import org.devsmart.miniweb.handlers.controller.PathVarCapture;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

public class PathVarCaptureTest {

    @Test
    public void testPathVarCapture() {

        PathVarCapture pathCapture = new PathVarCapture("/result/{id}/{rad}");

        Map<String, String> pathVars = pathCapture.parseUri("/result/32/awesome");
        assertEquals(2, pathVars.size());
        assertEquals("32", pathVars.get("id"));
        assertEquals("awesome", pathVars.get("rad"));


    }
}
