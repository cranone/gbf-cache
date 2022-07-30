package com.shadego.gbf.ssl;

import com.github.monkeywie.proxyee.server.HttpProxyCACertFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Component
public class PfxCertFactory implements HttpProxyCACertFactory {
    @Value("${cache.ssl.password}")
    private String password;
    @Value("${cache.ssl.cert}")
    private String certPath;

    @Override
    public X509Certificate getCACert() throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        try (InputStream is=certPath.contains("classpath")?this.getClasspathFile():new FileInputStream(certPath)){
            keystore.load(is,password.toCharArray());
        }
        return (X509Certificate) keystore.getCertificate(keystore.aliases().nextElement());
    }

    @Override
    public PrivateKey getCAPriKey() throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        try (InputStream is=certPath.contains("classpath")?this.getClasspathFile():new FileInputStream(certPath)){
            keystore.load(is,password.toCharArray());
        }
        return (PrivateKey) keystore.getKey(keystore.aliases().nextElement(), password.toCharArray());
    }

    private InputStream getClasspathFile(){
        return this.getClass().getResourceAsStream(certPath.replace("classpath:","/"));
    }
}
