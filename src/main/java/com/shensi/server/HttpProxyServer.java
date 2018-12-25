package com.shensi.server;

import com.shensi.crt.CertCache;
import com.shensi.crt.CertUtil;
import com.shensi.exception.HttpProxyExceptionHandler;
import com.shensi.handler.HttpProxyServerHandler;
import com.shensi.intercept.HttpProxyInterceptInitializer;
import com.shensi.proxy.ProxyConfig;
import com.shensi.util.ChannelAttributeUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * Created by shensi 2018-12-16
 */
public class HttpProxyServer {
    //证书&私钥
    private CaAndPrivateKey caAndPrivateKey;
    //服务端配置
    private HttpProxyServerConfig serverConfig;
    //拦截器
    private HttpProxyInterceptInitializer proxyInterceptInitializer;
    //异常处理
    private HttpProxyExceptionHandler httpProxyExceptionHandler;
    //代理配置
    private ProxyConfig proxyConfig;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private void init() {
        if (serverConfig == null) {
            serverConfig = new HttpProxyServerConfig();
        }

//        if (serverConfig.isSupportSsl()) {
            try {
                //读取classpath下的ca和私钥
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                X509Certificate caCert;
                PrivateKey caPriKey;
                if (caAndPrivateKey == null) {
                    caCert = CertUtil.loadCert(classLoader.getResourceAsStream("ca.crt"));
                    caPriKey = CertUtil.loadPriKey(Objects.requireNonNull(classLoader.getResourceAsStream("ca_private.der")));
                    caAndPrivateKey = new CaAndPrivateKey(caCert, caPriKey);
                } else {
                    caCert = caAndPrivateKey.getX509Certificate();
                    caPriKey = caAndPrivateKey.getPrivateKey();
                }


                //读取CA证书颁发者
                serverConfig.setIssuer(CertUtil.getSubject(caCert));
                //读取CA证书有效时段
                serverConfig.setCaNotBefore(caCert.getNotBefore());
                serverConfig.setCaNotAfter(caCert.getNotAfter());
                //CA私钥用于给动态生成的网站SSL证书签证
                serverConfig.setCaPriKey(caPriKey);

                //生产一对随机公私钥用于网站SSL证书动态创建
                KeyPair keyPair = CertUtil.genKeyPair();
                serverConfig.setServerPriKey(keyPair.getPrivate());
                serverConfig.setServerPubKey(keyPair.getPublic());
            }catch (Exception e){
                e.printStackTrace();
//                serverConfig.setSupportSsl(false);
            }
//        }
        if (proxyInterceptInitializer == null) {
            proxyInterceptInitializer = new HttpProxyInterceptInitializer();
        }
        if (httpProxyExceptionHandler == null) {
            httpProxyExceptionHandler = new HttpProxyExceptionHandler();
        }
    }


    public void start(int port) {
        init();
        bossGroup = new NioEventLoopGroup(serverConfig.getBossGroupThreads());
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkerGroupThreads());
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1000) //请求队列长度
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(
                        new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel ch) {
                                ChannelAttributeUtil.setProxyStatusToChannel(ch, 0);

                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast("httpCodec", new HttpServerCodec()) //http编解码
                                        .addLast("serverHandler",
                                        new HttpProxyServerHandler(serverConfig, proxyInterceptInitializer, proxyConfig,
                                                httpProxyExceptionHandler));
                            }
                        }
                    );
            ChannelFuture f = b.bind(port).sync();
            f.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()){
                    System.out.println("proxy server start...");
                }else {
                    System.out.println("proxy server start failed");
                }
            }).channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("proxy start failed");
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void close() {
//        serverConfig.getProxyLoopGroup().shutdownGracefully();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        CertCache.clear();
    }

    /**
     * 设置代理服务器配置
     * @param serverConfig
     * @return
     */
    public HttpProxyServer serverConfig(HttpProxyServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        return this;
    }

    /**
     * 设置拦截器配置
     * @param proxyInterceptInitializer
     * @return
     */
    public HttpProxyServer proxyInterceptInitializer(
            HttpProxyInterceptInitializer proxyInterceptInitializer) {
        this.proxyInterceptInitializer = proxyInterceptInitializer;
        return this;
    }

    /**
     * 设置异常处理
     * @param httpProxyExceptionHandler
     * @return
     */
    public HttpProxyServer httpProxyExceptionHandle(
            HttpProxyExceptionHandler httpProxyExceptionHandler) {
        this.httpProxyExceptionHandler = httpProxyExceptionHandler;
        return this;
    }

    /**
     * 设置代理配置
     * @param proxyConfig
     * @return
     */
    public HttpProxyServer proxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
        return this;
    }

    /**
     * 设置ca和私钥
     * @param caAndPrivateKey
     * @return
     */
    public HttpProxyServer caAndPrivateKey(CaAndPrivateKey caAndPrivateKey) {
        this.caAndPrivateKey = caAndPrivateKey;
        return this;
    }

}
