package com.shensi;

import com.shensi.server.HttpProxyServer;
import com.shensi.server.HttpProxyServerConfig;

public class HandelSslHttpProxyServer {

    public static void main(String[] args) throws Exception {
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setSupportSsl(true);
        new HttpProxyServer()
                .serverConfig(config)
                .start(9999);
    }
}
