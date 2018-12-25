package com.shensi.util;

import com.shensi.crt.CertUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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

    public static String sslRecordDecode(String recordCode){
        byte[] bytes = ByteBufUtil.decodeHexDump(recordCode);
        return new String(bytes, Charset.forName("utf8"));
    }


    public static void main(String[] args) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {


        InputStream store = SslContextUtil.class.getClassLoader().getResourceAsStream("store.jks");

        char[] keyPassword = "g5373d41f72m52s".toCharArray();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(store, keyPassword);
        String alias = keyStore.aliases().nextElement();
        X509Certificate certificate = (X509Certificate)keyStore.getCertificate(alias);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword);


        SslContextBuilder.forServer(privateKey, certificate).build();
    }
}
