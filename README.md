MiniWeb
==========

MiniWeb is a Http server library written in Java targeted for embedded projects. MiniWeb's goals are to be small
and lightweight as possible while providing simple and easy to use API. MiniWeb uses the very mature and robust
[Apache HttpComponents](http://hc.apache.org) at its core. MiniWeb only supports what is necessary to get an
embedded web project up and running quickly (no servlets).


Example Usage
--------------
Create a server that serves files from "public" folder:
```java
Server server = new ServerBuilder()
    .port(9000)
    .mapLocation("/*", new File("public"))
    .create();

server.start();
```

### Controllers
Handle request programmatically is very simple with Controllers. The following example will respond the a request for `/cgi/hello` with the text `world`.

```java
@Controller
class MyController {

@RequestMapping("hello")
public void handleHello(HttpRequest request, HttpResponse response) throws Exception {
    StringEntity retval = new StringEntity("world");
    response.setEntity(retval);
    }
}

Server server = new ServerBuilder()
    .port(9000)
    .mapController("/cgi/*", new MyController())
    .mapDirectory("/*", new File("public"))
    .create();

server.start();
```

#### Query params
Controllers can even handle URL query params. For example:
```java
@Controller
class MyController {

@RequestMapping("hello")
public void handleHello(@RequestParam("param1") String param1Value,
         @RequestParam("bananas") int numBananas) {
    System.out.println("look param1 value is: " + param1);
    System.out.println("how many bananas? " + numBananas);
    }
}
```
Sending a `/cgi/hello?param1=cool&amp;bananas=5` and the value of `param1Value` would be "cool" and `numBananas` would be 5 as expected.

You can also use parts of the url path as parameters like so:
```java
@RequestMapping(value = "request/{id}")
public void pathParam(@PathVariable("id") String myId, HttpResponse response) throws Exception {
    StringEntity retval = new StringEntity(myId);
    response.setEntity(retval);
}
```

#### JSON REST Requests
Controllers can also be used to handle REST requests without all the overhead of parsing JSON manually. For example:
```java
class MyModel {
    String name;
}

@Controller
class MyController {

@RequestMapping("hello", method = RequestMethod.Post)
public void handleHello(@Body MyModel obj) {
    System.out.println("automatically parsing json in the body and creating a POJO: " + obj);
    }
}
```
performing a POST to this endpoint with json: `{ "name": "awesome!" }`. Note: the request must have a Content-Header header value set to application/json.

#### JSON REST Response
sending a response with JSON is also easy. Just add the `@Body` annotation to your handle methods.
```java
class MyModel {
String name;
}

@Controller
class MyController {

@RequestMapping("hello")
@Body
public MyModel handleHello() {
    //just create a new POJO and return it. MiniWeb will handle serializing it to JSON
    MyModel retval = new MyModel();
    retval.name = "rockin";
    return retval;
  }
}
```