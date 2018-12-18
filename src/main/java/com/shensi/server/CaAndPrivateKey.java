package com.shensi.server;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Created by shensi 2018-12-16
 */
public class CaAndPrivateKey {
    private X509Certificate x509Certificate;

    private PrivateKey privateKey;

    public CaAndPrivateKey(X509Certificate x509Certificate, PrivateKey privateKey) {
        this.x509Certificate = x509Certificate;
        this.privateKey = privateKey;
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

//    X509Certificate getCACert() throws Exception;
//
//    PrivateKey getCAPriKey() throws Exception;
}
