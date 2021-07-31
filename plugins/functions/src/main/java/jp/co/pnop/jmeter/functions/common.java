package jp.co.pnop.jmeter.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

import org.apache.jmeter.util.JMeterUtils;

public class common {
    protected static int NON_PROXY_HOST_SUFFIX_SIZE;
    protected static final Set<String> nonProxyHostFull = new HashSet<>();
    protected static final List<String> nonProxyHostSuffix = new ArrayList<>();

    public static HttpClientBuilder setProxy(String host) {
        HttpClientBuilder httpclientbuilder = HttpClientBuilder.create();

        String proxyHost = JMeterUtils.getPropDefault("https.proxyHost", "").trim();
        int proxyPort = Integer.parseInt(JMeterUtils.getPropDefault("https.proxyPort", "3128").trim());
        String nonProxyHosts = JMeterUtils.getPropDefault("https.nonProxyHosts", "").trim();
        String proxyUser = JMeterUtils.getPropDefault("http.proxyUser", "").trim();
        String proxyPass = JMeterUtils.getPropDefault("http.proxyPass", "");
        if (nonProxyHosts.length() > 0) {
            StringTokenizer s = new StringTokenizer(nonProxyHosts, "|");
            while (s.hasMoreTokens()) {
                String t = s.nextToken();
                if (t.indexOf('*') ==0) {
                    nonProxyHostSuffix.add(t.substring(1));
                } else {
                    nonProxyHostFull.add(t);
                }
            }
        }
        NON_PROXY_HOST_SUFFIX_SIZE = nonProxyHostSuffix.size();

        if (proxyHost.length() > 0 && !nonProxyHostFull.contains(host) && !isPartialMatch(host)) {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpclientbuilder = httpclientbuilder.setRoutePlanner(routePlanner);

            if (proxyUser.length() > 0) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(proxyUser, proxyPass));
                httpclientbuilder = httpclientbuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
        return httpclientbuilder;
    }

    protected static boolean isPartialMatch(String host) {
        for (int i = 0;i < NON_PROXY_HOST_SUFFIX_SIZE; i ++){
            if (host.endsWith(nonProxyHostSuffix.get(i))) {
                return true;
            }
        }
        return false;
    }
}
