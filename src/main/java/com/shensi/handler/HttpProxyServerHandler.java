package com.shensi.handler;

import com.shensi.crt.CertCache;
import com.shensi.exception.HttpProxyExceptionHandler;
import com.shensi.intercept.HttpProxyIntercept;
import com.shensi.intercept.HttpProxyInterceptInitializer;
import com.shensi.intercept.HttpProxyInterceptPipeline;
import com.shensi.proxy.ProxyConfig;
import com.shensi.proxy.ProxyHandleFactory;
import com.shensi.server.HttpProxyServer;
import com.shensi.server.HttpProxyServerConfig;
import com.shensi.util.ProtoUtil;
import com.shensi.util.ProtoUtil.RequestProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shensi 2018-12-16
 */
public class HttpProxyServerHandler extends ChannelInboundHandlerAdapter {
    //代理服务器配置
    private HttpProxyServerConfig serverConfig;
    //代理配置
    private ProxyConfig proxyConfig;
    //强化pipeline的函数方法
    private HttpProxyInterceptInitializer interceptInitializer;
    //异常处理器
    private HttpProxyExceptionHandler exceptionHandle;


    private String desHost;
    private int desPort;
    private boolean isSsl = false;

    private ChannelFuture cf;
    private int status = 0;


    //http interceptor pipeline
    private HttpProxyInterceptPipeline interceptPipeline;
    //请求队列
    private List requestList;
    private boolean isConnect;

    public HttpProxyServerConfig getServerConfig() {
        return serverConfig;
    }

    public HttpProxyInterceptPipeline getInterceptPipeline() {
        return interceptPipeline;
    }

    public HttpProxyExceptionHandler getExceptionHandle() {
        return exceptionHandle;
    }

    public HttpProxyServerHandler(HttpProxyServerConfig serverConfig,
                                  HttpProxyInterceptInitializer interceptInitializer,
                                  ProxyConfig proxyConfig, HttpProxyExceptionHandler exceptionHandle) {
        this.serverConfig = serverConfig;
        this.proxyConfig = proxyConfig;
        this.interceptInitializer = interceptInitializer;
        this.exceptionHandle = exceptionHandle;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        //处理请求头
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            //第一次建立连接取host和端口号和处理代理握手
            RequestProto requestProto;
            if (status == 0) {
                try {
                    requestProto = ProtoUtil.getRequestProto(request);
                }catch (Exception e){
                    e.printStackTrace();
                    ctx.channel().close();
                    return;
                }
                status = 1;
                this.desHost = requestProto.getHost();
                this.desPort = requestProto.getPort();

                if (HttpMethod.CONNECT == request.method()) {//建立代理握手
                    status = 2;
                    //proxy say hello
                    HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpProxyServer.SUCCESS);
                    ctx.writeAndFlush(response);
                    ctx.channel().pipeline().remove("httpCodec");
                    return;
                }
            }

            interceptPipeline = buildPipeline();
            interceptPipeline.setRequestProto(new RequestProto(desHost, desPort, isSsl));

            if (request.uri().indexOf("/") != 0) {
                URL url = new URL(request.uri());
                request.setUri(url.getFile());
            }
            //开始拦截处理httpRequest
            interceptPipeline.beforeRequest(ctx.channel(), request);
        } else if (msg instanceof HttpContent) { //处理请求体
            if (status != 2) {
                interceptPipeline.beforeRequest(ctx.channel(), (HttpContent) msg);
            } else {
                ReferenceCountUtil.release(msg);
                status = 1;
            }
        } else {
            //ssl握手处理
            if (serverConfig.isSupportSsl()) {
                ByteBuf byteBuf = (ByteBuf) msg;
                if (byteBuf.getByte(0) == 22) {//ssl握手
                    isSsl = true;
                    int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();


                    SslContext sslCtx = SslContextBuilder
                            .forServer(serverConfig.getServerPriKey(), CertCache.getCert(port, this.desHost, serverConfig))
                            .build();

                    ctx.pipeline().addFirst("httpCodec", new HttpServerCodec());
                    ctx.pipeline().addFirst("sslHandle", sslCtx.newHandler(ctx.alloc()));
                    //重新过一遍pipeline，拿到解密后的的http报文
                    ctx.pipeline().fireChannelRead(msg);
                    return;
                }
            }
            handleProxyData(ctx.channel(), msg, false);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (cf != null) {
            cf.channel().close();
        }
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cf != null) {
            cf.channel().close();
        }
        ctx.channel().close();
        exceptionHandle.beforeCatch(ctx.channel(), cause);
    }

    private void handleProxyData(Channel channel, Object msg, boolean isHttp)
            throws Exception {
        if (cf == null) {
            if (isHttp && !(msg instanceof HttpRequest)) {  //connection异常 还有HttpContent进来，不转发
                return;
            }
            ProxyHandler proxyHandler = ProxyHandleFactory.build(proxyConfig);
            RequestProto requestProto = new RequestProto(desHost, desPort, isSsl);
            ChannelInitializer channelInitializer =
                    isHttp ? new HttpProxyChannelInitializer(channel, requestProto, proxyHandler)
                            : new TunnelProxyInitializer(channel, proxyHandler);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(serverConfig.getProxyLoopGroup()) // 注册线程池
                    .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                    .handler(channelInitializer);
            if (proxyConfig != null) {
                //代理服务器解析DNS和连接
                bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
            }
            requestList = new LinkedList();

            cf = bootstrap.connect(desHost, desPort);

            cf.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    future.channel().writeAndFlush(msg);
                    synchronized (requestList) {
                        requestList.forEach(obj -> future.channel().writeAndFlush(obj));
                        requestList.clear();
                        isConnect = true;
                    }
                } else {
                    requestList.forEach(obj -> ReferenceCountUtil.release(obj));
                    requestList.clear();
                    future.channel().close();
                    channel.close();
                }
            });
        } else {
            synchronized (requestList) {
                if (isConnect) {
                    cf.channel().writeAndFlush(msg);
                } else {
                    requestList.add(msg);
                }
            }
        }
    }

    //构建pipeline并调用interceptInitializer.init强化管道
    private HttpProxyInterceptPipeline buildPipeline() {
        HttpProxyInterceptPipeline interceptPipeline = new HttpProxyInterceptPipeline(
                new HttpProxyIntercept() {
                    @Override
                    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                                              HttpProxyInterceptPipeline pipeline) throws Exception {
                        handleProxyData(clientChannel, httpRequest, true);
                    }

                    @Override
                    public void beforeRequest(Channel clientChannel, HttpContent httpContent,
                                              HttpProxyInterceptPipeline pipeline) throws Exception {
                        handleProxyData(clientChannel, httpContent, true);
                    }

                    @Override
                    public void afterResponse(Channel clientChannel, Channel proxyChannel,
                                              HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
                        clientChannel.writeAndFlush(httpResponse);
                        if (HttpHeaderValues.WEBSOCKET.toString()
                                .equals(httpResponse.headers().get(HttpHeaderNames.UPGRADE))) {
                            //websocket转发原始报文
                            proxyChannel.pipeline().remove("httpCodec");
                            clientChannel.pipeline().remove("httpCodec");
                        }
                    }

                    @Override
                    public void afterResponse(Channel clientChannel, Channel proxyChannel,
                                              HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
                        clientChannel.writeAndFlush(httpContent);
                    }
                });
        interceptInitializer.init(interceptPipeline);
        return interceptPipeline;
    }
}
