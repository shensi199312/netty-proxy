package com.shensi.util;

import com.shensi.crt.CertUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
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

    public static String sslRecordDecode(String recordCode){
        byte[] bytes = ByteBufUtil.decodeHexDump(recordCode);
        return new String(bytes, Charset.forName("utf8"));
    }


    public static void main(String[] args) {
        String s = SslContextUtil.sslRecordDecode("434f4e4e454354206c6f63616c686f73743a34343320485454502f312e310d0a486f73743a206c6f63616c686f73743a3434330d0a50726f78792d436f6e6e656374696f6e3a206b6565702d616c6976650d0a557365722d4167656e743a204d6f7a696c6c612f352e3020284d6163696e746f73683b20496e74656c204d6163204f5320582031305f31345f3029204170706c655765624b69742f3533372e333620284b48544d4c2c206c696b65204765636b6f29204368726f6d652f37312e302e333537382e3938205361666172692f3533372e33360d0a0d0a");
        System.out.println(s);
    }
}
