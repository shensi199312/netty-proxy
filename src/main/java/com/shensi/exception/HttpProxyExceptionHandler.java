package com.shensi.exception;

import io.netty.channel.Channel;

/**
 * Created by shensi 2018-12-16
 */
public class HttpProxyExceptionHandler {

    public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
        throw new Exception(cause);
    }

    public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
            throws Exception {
        throw new Exception(cause);
    }
}
