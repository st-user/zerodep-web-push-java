import com.zerodeplibs.webpush.ext.jwt.jose4j.Jose4jVAPIDJWTGeneratorFactory;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGeneratorFactory;

module com.zerodeplibs.webpush.ext.jwt.jose4j {
    requires com.zerodeplibs.webpush;
    requires org.jose4j;
    exports com.zerodeplibs.webpush.ext.jwt.jose4j;
    provides VAPIDJWTGeneratorFactory with Jose4jVAPIDJWTGeneratorFactory;
}

