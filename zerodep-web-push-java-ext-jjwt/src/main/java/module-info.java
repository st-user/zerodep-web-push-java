import com.zerodeplibs.webpush.ext.jwt.jjwt.JavaJwtVAPIDJWTGeneratorFactory;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGeneratorFactory;

module com.zerodeplibs.webpush.ext.jwt.jjwt {
    requires com.zerodeplibs.webpush;
    requires jjwt.api;
    exports com.zerodeplibs.webpush.ext.jwt.jjwt;
    provides VAPIDJWTGeneratorFactory with JavaJwtVAPIDJWTGeneratorFactory;
}
