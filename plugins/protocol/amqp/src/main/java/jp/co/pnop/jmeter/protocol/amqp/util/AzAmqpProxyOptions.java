package jp.co.pnop.jmeter.protocol.amqp.util;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import org.apache.jmeter.util.JMeterUtils;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzAmqpProxyOptions {
    ProxyOptions options;
    private static final Logger log = LoggerFactory.getLogger(AzAmqpProxyOptions.class);

    public AzAmqpProxyOptions() {
        final String proxyHost = JMeterUtils.getPropDefault("https.proxyHost", "").trim();
        final int proxyPort = Integer.parseInt(JMeterUtils.getPropDefault("https.proxyPort", "3128").trim());
        final String proxyUser = JMeterUtils.getPropDefault("http.proxyUser", null);
        final String proxyPass = JMeterUtils.getPropDefault("http.proxyPass", null);

        ProxyAuthenticationType proxyAuthenticationType = ProxyAuthenticationType.NONE;
        Proxy proxy = Proxy.NO_PROXY;
        if (proxyHost.length() > 0) {

            InetSocketAddress address = new InetSocketAddress(proxyHost, proxyPort);
            proxy = new Proxy(Proxy.Type.HTTP, address);
            
            if (Objects.toString(proxyUser, "").length() > 0) {
                proxyAuthenticationType = ProxyAuthenticationType.BASIC;
            }
        }
        options = new ProxyOptions(proxyAuthenticationType, proxy, proxyUser, proxyPass);
    }

    public ProxyOptions ProxyOptions() {
        return options;
    }
}
