import com.zerodeplibs.webpush.ext.jwt.auth0.Auth0VAPIDJWTGeneratorFactory;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGeneratorFactory;

@SuppressWarnings("module")
module com.zerodeplibs.webpush.ext.jwt.auth0 {
    requires com.zerodeplibs.webpush;
    requires com.auth0.jwt;
    exports com.zerodeplibs.webpush.ext.jwt.auth0;
    provides VAPIDJWTGeneratorFactory with Auth0VAPIDJWTGeneratorFactory;
}
