package com.example.geocaching1.utils;


import android.content.Context;
import android.util.Log;

import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class ApiClient {
    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // 创建一个不验证 SSL 证书的 TrustManager
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[] {};
                        }
                    }
            };

            // 创建 SSLContext，忽略证书验证
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // 创建不验证 Hostname 的 HostnameVerifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // 创建 OkHttpClient
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(allHostsValid);

            builder.followRedirects(false);    // 禁用HTTP重定向
            builder.followSslRedirects(false); // 禁用SSL重定向
//            builder.followRedirects(true);    // 允许 HTTP 重定向
//            builder.followSslRedirects(true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

