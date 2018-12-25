package com.shensi.server;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

/**
 * Created by shensi 2018-12-16
 */
public class HttpProxyServerConfig {
    //认证机构
    private String issuer;
    //证书起始时间
    private Date caNotBefore;
    //证书结束时间
    private Date caNotAfter;
    private PrivateKey caPriKey;
    private PrivateKey serverPriKey;
    private PublicKey serverPubKey;


    private int bossGroupThreads;
    private int workerGroupThreads;
    private int proxyGroupThreads;
//    //是否支持ssl
//    private boolean supportSsl;


    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Date getCaNotBefore() {
        return caNotBefore;
    }

    public void setCaNotBefore(Date caNotBefore) {
        this.caNotBefore = caNotBefore;
    }

    public Date getCaNotAfter() {
        return caNotAfter;
    }

    public void setCaNotAfter(Date caNotAfter) {
        this.caNotAfter = caNotAfter;
    }

    public PrivateKey getCaPriKey() {
        return caPriKey;
    }

    public void setCaPriKey(PrivateKey caPriKey) {
        this.caPriKey = caPriKey;
    }

    public PrivateKey getServerPriKey() {
        return serverPriKey;
    }

    public void setServerPriKey(PrivateKey serverPriKey) {
        this.serverPriKey = serverPriKey;
    }

    public PublicKey getServerPubKey() {
        return serverPubKey;
    }

    public void setServerPubKey(PublicKey serverPubKey) {
        this.serverPubKey = serverPubKey;
    }


//    public boolean isSupportSsl() {
//        return supportSsl;
//    }
//
//    public void setSupportSsl(boolean supportSsl) {
//        this.supportSsl = supportSsl;
//    }

    public int getBossGroupThreads() {
        return bossGroupThreads;
    }

    public void setBossGroupThreads(int bossGroupThreads) {
        this.bossGroupThreads = bossGroupThreads;
    }

    public int getWorkerGroupThreads() {
        return workerGroupThreads;
    }

    public void setWorkerGroupThreads(int workerGroupThreads) {
        this.workerGroupThreads = workerGroupThreads;
    }

    public int getProxyGroupThreads() {
        return proxyGroupThreads;
    }

    public void setProxyGroupThreads(int proxyGroupThreads) {
        this.proxyGroupThreads = proxyGroupThreads;
    }
}
