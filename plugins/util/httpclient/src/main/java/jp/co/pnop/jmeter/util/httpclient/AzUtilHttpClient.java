package jp.co.pnop.jmeter.util.httpclient;

import java.net.InetSocketAddress;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;

import org.apache.jmeter.util.JMeterUtils;

public class AzUtilHttpClient {
    private static final String proxyHost = JMeterUtils.getPropDefault("https.proxyHost", "").trim();
    private static final int proxyPort = Integer.parseInt(JMeterUtils.getPropDefault("https.proxyPort", "3128").trim());

    public static HttpClient httpClientBase() {
        InetSocketAddress address = new InetSocketAddress(proxyHost, proxyPort);
        String nonProxyHosts = JMeterUtils.getPropDefault("https.nonProxyHosts", "").trim();
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, address).setNonProxyHosts(nonProxyHosts);
        final String proxyUser = JMeterUtils.getPropDefault("http.proxyUser", "").trim();
        final String proxyPass = JMeterUtils.getPropDefault("http.proxyPass", "");

        nonProxyHosts = nonProxyHosts.concat("|169.254.169.254").replaceFirst("^|", "");

        if (proxyHost.length() > 0) {
            proxyOptions.setNonProxyHosts(nonProxyHosts);
            if (proxyUser.length() > 0) {
                proxyOptions.setCredentials(proxyUser, proxyPass);
            }
            return (HttpClient) new NettyAsyncHttpClientBuilder().proxy(proxyOptions).build();
        }
        return HttpClient.createDefault();
    }
}