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

Server server = new ServerBuilder();

```
