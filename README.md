MiniWeb
==========

MiniWeb is a Http server library written in Java targeted for embedded projects. MiniWeb's goals are to be small
and lightweight as possible while providing simple and easy to use API. MiniWeb uses the very mature and robust
[Apache HttpComponents](http://hc.apache.org) at its core. MiniWeb only supports what is necessary to get an
embedded web project up and running quickly (no servlets).


Example Usage
--------------
Create a server that serves from the filesystem:

```java

File fsRoot = new File("public");

Server server = new ServerBuilder()
        .port(9000)
        .mapLocation("/*", fsRoot)
        .create();

server.start();

...

server.shutdown();

```

Handle request programmatically is very simple with Controllers
```java
@Controller
class MyController {

    @RequestMapping("hello")
    public void handleHello(HttpRequest request, HttpResponse response) throws Exception {
        StringEntity retval = new StringEntity("itworked");
        response.setEntity(retval);
    }
}

File fsRoot = new File("public");

Server server = new ServerBuilder()
        .port(9000)
        .mapController("/cgi/*", new MyController())
        .mapDirectory("/*", fsRoot)
        .create();

server.start();
...
server.shutdown();
```
