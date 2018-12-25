package com.shensi.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.proxy.ProxyHandler;

/**
 * Created by shensi 2018-12-16
 * 透传
 */
public class TunnelProxyInitializer extends ChannelInitializer {

    private Channel clientChannel;
    private ProxyHandler proxyHandler;

    public TunnelProxyInitializer(Channel clientChannel,
                                  ProxyHandler proxyHandler) {
        this.clientChannel = clientChannel;
        this.proxyHandler = proxyHandler;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (proxyHandler != null) {
            ch.pipeline().addLast(proxyHandler);
        }
        ch.pipeline().addLast(new TunnelHandler(clientChannel));
    }
}
