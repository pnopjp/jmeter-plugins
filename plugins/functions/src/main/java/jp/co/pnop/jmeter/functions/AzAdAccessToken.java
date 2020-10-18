package jp.co.pnop.jmeter.functions;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzAdAccessToken extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(AzAdAccessToken.class);

    private static final List<String> desc = new ArrayList<String>();
    private static final String KEY = "__AzAdAccessToken";

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 6;
    private static final int MAX_PARAMETER_COUNT = 9;

    static {
        desc.add("Azure AD Tenant ID");
        desc.add("Grant type");
        desc.add("Application (Client) ID");
        desc.add("Client secret of Azure AD application");
        desc.add("Username");
        desc.add("Password");
        desc.add("Acess Token Scope (optional)");
        desc.add("Azure AD version (optional)");
        desc.add("Name of variable in which to store the result (optional)");
    }

    private CompoundVariable[] values;

    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String tenantId = values[0].execute().trim();
        String grantType = values[1].execute().trim();
        String clientId = values[2].execute().trim();
        String clientSecret = values[3].execute().trim();
        String username = values[4].execute().trim();
        String password = values[5].execute();
        String scope = "";
        if (values.length > 6) {
            scope = values[6].execute().trim();
        }
        String aadVersion = "";
        if (values.length > 7) {
            aadVersion = values[7].execute().trim();
            if (aadVersion.length() > 0) {
                aadVersion = "/" + aadVersion;
            }
        }

        String accessToken = null;

        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost request = new HttpPost("https://login.microsoftonline.com/" + URLEncoder.encode(tenantId, "UTF-8").replace("+", "%20") + "/oauth2" + aadVersion + "/token");

            request.setHeader("Content-Type", "application/x-www-form-urlencoded");

            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("grant_type", grantType));
            parameters.add(new BasicNameValuePair("client_id", clientId));
            parameters.add(new BasicNameValuePair("client_secret", clientSecret));
            if (aadVersion.length() == 0) {
                parameters.add(new BasicNameValuePair("resource", clientId));
            }
            parameters.add(new BasicNameValuePair("username", username));
            parameters.add(new BasicNameValuePair("password", password));
            if (scope.length() > 0) {
                parameters.add(new BasicNameValuePair("scope", scope));
            }

            String body = "";
            for (NameValuePair parameter: parameters) {
                body += "&" + parameter.getName() + "=" + URLEncoder.encode(parameter.getValue(), "UTF-8").replace("+", "%20");
            }
            request.setEntity(new StringEntity(body.substring(1)));


            CloseableHttpResponse response = httpclient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(responseMessage);
            if (status == HttpStatus.SC_OK) {
                accessToken = node.get("access_token").textValue();
                addVariableValue(accessToken, values, 8);
            } else {
                String errorDescription = node.get("error_description").textValue();
                log.warn("Warn calling {} Azure AD request, {}: {}", KEY, response.getStatusLine().toString(), errorDescription);
                log.info(responseMessage);
            }
        } catch (IllegalArgumentException e) {
            log.error(
                    "Error calling {} function with Tenant ID {}, grant_type {}, client_id {}, client_secret {}, username {}, ",
                    KEY, tenantId, grantType, clientId, clientSecret, username, e);
        } catch (UnsupportedEncodingException e) {
            log.error("Error calling {}, ", KEY, e);
        } catch (ClientProtocolException e) {
            log.error("Error calling {}, ", KEY, e);
        } catch (IOException e) {
            log.error("Error calling {}, ", KEY, e);
        }

        return accessToken;
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray(new CompoundVariable[parameters.size()]);
    }

    @Override
    public String getReferenceKey() {
        return KEY;
    }

    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }
}
