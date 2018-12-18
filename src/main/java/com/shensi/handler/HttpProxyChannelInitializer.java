package com.shensi.handler;

import com.shensi.util.ProtoUtil.RequestProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;

/**
 * Created by shensi 2018-12-16
 * HTTP代理，转发解码后的HTTP报文
 */
public class HttpProxyChannelInitializer extends ChannelInitializer {

    private Channel clientChannel;
    private RequestProto requestProto;
    private ProxyHandler proxyHandler;

    public HttpProxyChannelInitializer(Channel clientChannel, RequestProto requestProto,
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
            ch.pipeline().addLast(
                    ((HttpProxyServerHandler) clientChannel.pipeline().get("serverHandle")).getServerConfig()
                            .getClientSslCtx()
                            .newHandler(ch.alloc(), requestProto.getHost(), requestProto.getPort()));
        }
        ch.pipeline().addLast("httpCodec", new HttpClientCodec());
        ch.pipeline().addLast("proxyClientHandle", new HttpProxyClientHandler(clientChannel));
    }
}
