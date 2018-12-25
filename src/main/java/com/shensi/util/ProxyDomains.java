package com.shensi.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.shensi.crt.CertUtil;
import com.shensi.server.CaAndPrivateKey;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by shensi on 2018-12-20
 * 加载本地证书(证书是域名下的合法证书)
 */
public class ProxyDomains {

    private static Map<String, CaAndPrivateKey> config = Maps.newHashMap();

    static {
        ClassLoader classLoader = ProxyDomains.class.getClassLoader();
        //加载代理配置
        InputStream resourceAsStream = classLoader.getResourceAsStream("config.yml");
        Yaml yaml = new Yaml();
        Iterable<Object> objects = yaml.loadAll(resourceAsStream);

        if (objects.iterator().hasNext()){
            Object next = objects.iterator().next();

            @SuppressWarnings("unchecked")
            ArrayList<LinkedHashMap<String,String>> proxy =
                    (ArrayList<LinkedHashMap<String,String>>)((LinkedHashMap) next).get("proxy");

            proxy.forEach(item -> {
                String domains = item.get("domains");
                String cert = item.get("cert");
                String privateKey = item.get("privateKey");

                try {
                    X509Certificate ca = CertUtil.loadCert(classLoader.getResourceAsStream(cert));
                    PrivateKey pk = CertUtil.loadPriKey(Objects.requireNonNull(classLoader.getResourceAsStream(privateKey)));
                    CaAndPrivateKey caAndPrivateKey = new CaAndPrivateKey(ca, pk);

                    Iterable<String> split = Splitter.on(",").omitEmptyStrings().trimResults().split(domains);
                    split.forEach(domain -> config.put(domain, caAndPrivateKey));
                } catch (CertificateException e) {
                    e.printStackTrace();
                    throw new RuntimeException("invalid cert");
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                    throw new RuntimeException("invalid privateKey");
                }

            });
        }
    }

    public static CaAndPrivateKey domainFilter(String domain){
        return config.get(domain);
    }
}
