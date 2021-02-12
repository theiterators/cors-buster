# cors-buster

<img src="https://raw.githubusercontent.com/theiterators/cors-buster/master/logo.jpg" width="200">


cors-buster is a simple reverse proxy that provides all the basic CORS headers. Use it only for development!

It's in a very early stage, so please report bugs through GitHub issues or via email.

## Usage

cors-buster requires Java 8 to work properly.

```
wget https://github.com/theiterators/cors-buster/releases/download/1.0.1/cors-buster-1.0.1.jar
java -jar cors-buster-1.0.1.jar proxyHost proxyPort serverHost serverPort
```

For example
```
java -jar cors-buster-1.0.1.jar 0.0.0.0 8080 localhost 9000
```
will set up a proxy listening on 0.0.0.0:8080 and will forward all requests to server running on localhost:9000.

## Author & license

If you have any questions regarding this project contact:

Łukasz Sowa <lukasz@iterato.rs> from [Iterators](https://iterato.rs).

For licensing info see LICENSE file in project's root directory.
