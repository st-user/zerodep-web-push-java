package com.zerodeplibs.webpush.ext.jwt.vertx;

import io.vertx.ext.auth.PubSecKeyOptions;

class Vertx4Support {
    static PubSecKeyOptions createOptions(String privateKey) {
        return new PubSecKeyOptions()
            .setAlgorithm("ES256")
            .setBuffer(privateKey);
    }
}
