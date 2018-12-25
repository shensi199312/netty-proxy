package com.shensi.handler;

import com.shensi.util.ProtoUtil.RequestProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * Created by shensi 2018-12-16
 */
public class HttpMITMChannelInitializer extends ChannelInitializer {

    private Channel clientChannel;
    private RequestProto requestProto;
    private ProxyHandler proxyHandler;

    public HttpMITMChannelInitializer(Channel clientChannel, RequestProto requestProto,
                                      ProxyHandler proxyHandler) {
        this.clientChannel = clientChannel;
        this.requestProto = requestProto;
        this.proxyHandler = proxyHandler;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (proxyHandler != null) {
            ch.pipeline().addLast(proxyHandler);
        }
        if (requestProto.getSsl()) {
            SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), requestProto.getHost(), requestProto.getPort()));
        }
        ch.pipeline().addLast("httpCodec", new HttpClientCodec());
        ch.pipeline().addLast("proxyClientHandler", new HttpMITMClientHandler(clientChannel));
    }
}
