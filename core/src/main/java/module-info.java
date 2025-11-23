module com.zerodeplibs.webpush {
    exports com.zerodeplibs.webpush.header;
    exports com.zerodeplibs.webpush.httpclient;
    exports com.zerodeplibs.webpush.jwt;
    exports com.zerodeplibs.webpush.key;
    exports com.zerodeplibs.webpush;

    requires static java.net.http;
    requires static okhttp3;
    requires static org.apache.httpcomponents.client5.httpclient5;
    requires static org.apache.httpcomponents.core5.httpcore5;
    requires static org.eclipse.jetty.client;
    requires static io.vertx.core;
    requires static io.vertx.web.client;

    uses com.zerodeplibs.webpush.jwt.VAPIDJWTGeneratorFactory;
}
