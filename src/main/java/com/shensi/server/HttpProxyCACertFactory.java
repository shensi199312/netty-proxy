package com.shensi.server;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Created by shensi 2018-12-16
 */
public interface HttpProxyCACertFactory {

    X509Certificate getCACert() throws Exception;

    PrivateKey getCAPriKey() throws Exception;
}
