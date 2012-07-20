package com.livestrong.myplate.constants;

public class BuildValues {
    public static final boolean IS_DEBUG = @app.debug@;
    public static final boolean IS_STAGING = @app.api.staging@;
    public static final boolean IGNORE_INVALID_SSL_CERTS = @app.api.ignore_invalid_certs@;   
    public static final String PROXY_HOST = "@app.proxy.host@";
    public static final int PROXY_PORT = @app.proxy.port@;
}