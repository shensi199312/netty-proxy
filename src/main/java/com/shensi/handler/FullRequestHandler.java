package com.shensi.handler;

import com.shensi.util.ProtoUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.resolver.NoopAddressResolverGroup;

import java.net.InetSocketAddress;

/**
 * Created by shensi on 2018-12-18
 */
public class FullRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        ProtoUtil.RequestProto requestProto = ProtoUtil.getRequestProto(fullHttpRequest);

        fullHttpRequest.headers().add("cus-header", "fucking_header");
        String uri = fullHttpRequest.uri();

        boolean isSsl = uri.indexOf("https") == 0 || uri.indexOf("https") == 0;
        Channel clientChannel = channelHandlerContext.channel();


        InetSocketAddress inetSocketAddress = new InetSocketAddress(requestProto.getHost(),
                requestProto.getPort());
        HttpProxyHandler httpProxyHandler = new HttpProxyHandler(inetSocketAddress);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(isSsl ? new HttpProxyChannelInitializer(clientChannel, requestProto, httpProxyHandler)
                        : new TunnelProxyInitializer(clientChannel, httpProxyHandler))
                //代理服务器解析DNS和连接
                .resolver(NoopAddressResolverGroup.INSTANCE);

        ChannelFuture connect = bootstrap.connect(requestProto.getHost(), requestProto.getPort());
        connect.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                future.channel().writeAndFlush(fullHttpRequest);
            } else {
                future.channel().writeAndFlush(new HttpResponseStatus(500, "代理访问远端失败"));
            }
        });

    }
}
