package com.shensi.handler;

import com.shensi.exception.HttpProxyExceptionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by shensi on 2018-12-24
 * 透传
 */
public class TunnelHandler extends ChannelInboundHandlerAdapter {
    private Channel clientChannel;

    public TunnelHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx0, Object msg0) throws Exception {
        clientChannel.writeAndFlush(msg0);
    }

    /**
     * 当与目标服务器断开连接,与客户端也断开连接
     * @param ctx0
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx0) throws Exception {
        ctx0.channel().close();
        clientChannel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx0, Throwable cause) throws Exception {
        ctx0.channel().close();
        clientChannel.close();
        HttpProxyExceptionHandler exceptionHandle = ((HttpProxyServerHandler) clientChannel.pipeline()
                .get("serverHandler")).getExceptionHandle();
        exceptionHandle.afterCatch(clientChannel, ctx0.channel(), cause);
    }
}
