package com.shensi.crt;

import com.shensi.server.HttpProxyServerConfig;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shensi 2018-12-16
 * 动态自签证书缓存
 */
public class CertCache {

    private static Map<Integer, Map<String, X509Certificate>> certCache = new ConcurrentHashMap<>(100);


    public static X509Certificate getCert(Integer port, String host, HttpProxyServerConfig serverConfig)
            throws Exception {
        X509Certificate cert = null;
        if (host != null) {
            Map<String, X509Certificate> portCertCache = certCache.computeIfAbsent(port, k -> new HashMap<>());
            String key = host.trim().toLowerCase();
            if (portCertCache.containsKey(key)) {
                return portCertCache.get(key);
            } else {
                cert = CertUtil.genCert(serverConfig.getIssuer(), serverConfig.getCaPriKey(),
                        serverConfig.getCaNotBefore(), serverConfig.getCaNotAfter(),
                        serverConfig.getServerPubKey(), key);
                portCertCache.put(key, cert);
            }
        }
        return cert;
    }

    public static void clear() {
        certCache.clear();
    }
}
