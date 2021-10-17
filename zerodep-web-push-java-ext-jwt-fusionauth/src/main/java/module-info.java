import com.zerodeplibs.webpush.ext.jwt.fusionauth.FusionAuthVAPIDJWTGeneratorFactory;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGeneratorFactory;

module com.zerodeplibs.webpush.ext.fusionauth {
    requires com.zerodeplibs.webpush;
    requires io.fusionauth;
    exports com.zerodeplibs.webpush.ext.jwt.fusionauth;
    provides VAPIDJWTGeneratorFactory with FusionAuthVAPIDJWTGeneratorFactory;
}
