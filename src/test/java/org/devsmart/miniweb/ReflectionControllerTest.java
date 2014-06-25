package org.devsmart.miniweb;


import org.devsmart.miniweb.handlers.controller.Controller;
import org.devsmart.miniweb.handlers.controller.RequestMapping;
import org.devsmart.miniweb.handlers.controller.RequestMethod;
import org.junit.Test;

public class ReflectionControllerTest {


    @Controller("stuff/")
    public static class MyController {

        @RequestMapping(value = "test", method = RequestMethod.Get)
        public void handleGet() {

        }
    }

    @Test
    public void test() {

        MyController controller = new MyController();

        ControllerBuilder builder = new ControllerBuilder();
        builder.addController(controller);
    }
}
