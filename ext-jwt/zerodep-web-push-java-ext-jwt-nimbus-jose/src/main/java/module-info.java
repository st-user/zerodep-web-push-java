import com.zerodeplibs.webpush.ext.jwt.nimbusjose.NimbusJoseVAPIDJWTGeneratorFactory;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGeneratorFactory;

module com.zerodeplibs.webpush.ext.jwt.nimbusjose {
    requires com.zerodeplibs.webpush;
    requires com.nimbusds.jose.jwt;
    exports com.zerodeplibs.webpush.ext.jwt.nimbusjose;
    provides VAPIDJWTGeneratorFactory with NimbusJoseVAPIDJWTGeneratorFactory;
}
