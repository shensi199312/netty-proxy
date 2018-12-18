package com.shensi.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by shensi on 2018-12-18
 */
public class SslChannelInitializer extends ChannelInitializer<Channel> {
    private SslContext sslContext;
    //if created by server set false,if created client set true
    private boolean client;
    private boolean startTls;


    public SslChannelInitializer(SslContext sslContext, boolean client, boolean startTls) {
        this.sslContext = sslContext;
        this.client = client;
        this.startTls = startTls;
    }

    @Override
    protected void initChannel(Channel channel) {
        //every channel will create a new SSLEngine
        SSLEngine sslEngine = sslContext.newEngine(channel.alloc());
        sslEngine.setUseClientMode(client);


        //every SSLEngine will create a new SslHandler instance
        SslHandler sslHandler = new SslHandler(sslEngine, startTls);
        sslHandler.setHandshakeTimeoutMillis(3000);
        sslHandler.handshakeFuture().addListener(future -> System.out.println("handshake success!"));

        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addFirst("sslHandler", sslHandler)
                .addLast("codec", client ? new HttpClientCodec() : new HttpServerCodec())
                // TODO: 2018-12-18 avoid dos attack
                .addLast("aggegator", new HttpObjectAggregator(512 * 1024))
                .addLast("fullContentHandler", new FullRequestHandler());
    }
}
