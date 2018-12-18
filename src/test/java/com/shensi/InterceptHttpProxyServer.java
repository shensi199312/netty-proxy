package com.shensi;

import com.shensi.exception.HttpProxyExceptionHandler;
import com.shensi.intercept.HttpProxyIntercept;
import com.shensi.intercept.HttpProxyInterceptInitializer;
import com.shensi.intercept.HttpProxyInterceptPipeline;
import com.shensi.intercept.common.CertDownIntercept;
import com.shensi.server.HttpProxyServer;
import com.shensi.server.HttpProxyServerConfig;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

public class InterceptHttpProxyServer {

    public static void main(String[] args) throws Exception {
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setSupportSsl(true);
        new HttpProxyServer()
                .serverConfig(config)
                .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
                    @Override
                    public void init(HttpProxyInterceptPipeline pipeline) {
//                        pipeline.addLast(new CertDownIntercept());
//                        pipeline.addLast(new HttpProxyIntercept(){
//                            @Override
//                            public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
//                                System.out.println(httpRequest);
//                                pipeline.beforeRequest(clientChannel, httpRequest);
//                            }
//                        });
                        pipeline.addLast(new HttpProxyIntercept() {
                            @Override
                            public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                                                      HttpProxyInterceptPipeline pipeline) throws Exception {
                                httpRequest.headers().add("cus_header", "fucking_header");
                                //转到下一个拦截器处理
                                pipeline.beforeRequest(clientChannel, httpRequest);
                            }
                        });
                    }
                })
                .httpProxyExceptionHandle(new HttpProxyExceptionHandler() {
                    @Override
                    public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
                        cause.printStackTrace();
                    }

                    @Override
                    public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
                            throws Exception {
                        cause.printStackTrace();
                    }
                })
                .start(9999);
    }
}
