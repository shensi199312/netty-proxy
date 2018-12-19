package com.shensi.util;

import com.shensi.crt.CertUtil;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * Created by shensi on 2018-12-18
 */
public class SslContextUtil {
    private SslContextUtil (){throw new IllegalAccessError("util can't be instance");}

    public static SslContext getSslContextByFilePath(String certPath, String privateKeyPath){
        File certFile = new File(certPath);
        File privateKeyFile = new File(privateKeyPath);

        try {
            return SslContextBuilder.forServer(privateKeyFile, certFile)
                    .build();
        } catch (SSLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static SslContext getSslContextByClassPath(String cert, String privateKey){
        ClassLoader classLoader = SslContextUtil.class.getClassLoader();

        InputStream certInputStream = classLoader.getResourceAsStream(cert);
        InputStream privateKeyStream = classLoader.getResourceAsStream(privateKey);

        try {
            X509Certificate x509Certificate = CertUtil.loadCert(certInputStream);
            PrivateKey pk = CertUtil.loadPriKey(Objects.requireNonNull(privateKeyStream));

            return SslContextBuilder.forServer(pk, x509Certificate)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    public static SslContext getSslContextByInputStream(InputStream certIs, InputStream privateKeyIs){
        try {
            return SslContextBuilder.forServer(privateKeyIs, certIs)
                    .build();
        } catch (SSLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
