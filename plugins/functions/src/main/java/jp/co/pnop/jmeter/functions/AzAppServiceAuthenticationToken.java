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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzAppServiceAuthenticationToken extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(AzAppServiceAuthenticationToken.class);

    private static final List<String> desc = new ArrayList<String>();
    private static final String KEY = "__AzAppServiceAuthenticationToken";

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 2;
    private static final int MAX_PARAMETER_COUNT = 5;

    static {
        desc.add("Azure App Service hostname");
        desc.add("Provider");
        desc.add("access_token or id_token");
        desc.add("twitter access_token_secret");
        desc.add("Name of variable in which to store the result (optional)");
    }

    private CompoundVariable[] values;

    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String appServiceHost = values[0].execute().trim();
        String provider = values[1].execute().trim().toLowerCase();
        String token = values[2].execute().trim();
        String tokenSecret = values.length > 3 ? values[3].execute().trim() : null;

        String authenticationToken = null;

        try {
            HttpPost request = new HttpPost("https://" + appServiceHost + "/.auth/login/" + provider);
            request.setHeader("Content-Type", "application/json");

            String body = null;
            switch (provider) {
            case "aad":
            case "microsoftaccount":
            case "facebook":
                body = "{\"access_token\":\"" + token + "\"}";
                break;
            case "google":
                body = "{\"id_token\":\"" + token + "\"}";
                break;
            case "twitter":
                body = "{\"access_token\":\"" + token + "\",\"access_token_secret\":\"" + tokenSecret + "\"}";
                break;
            default:
                log.error("Error calling {} function with provider {}. You can use either \"aad\", \"microsoftaccount\", \"google\", \"facebook\", \"twitter\"", KEY, provider);
                return "";
            }
            request.setEntity(new StringEntity(body, "UTF-8"));
            
            CloseableHttpClient httpclient = common.setProxy(appServiceHost).build();
            CloseableHttpResponse response = httpclient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (status == HttpStatus.SC_OK){
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(responseMessage);
                authenticationToken = node.get("authenticationToken").textValue();
                addVariableValue(authenticationToken, values, 4);
            } else {
                log.warn("Warn calling {} Azure App Service authorizationToken, {}: {}", KEY, response.getStatusLine().toString(), responseMessage);
            }
        } catch (IllegalArgumentException e) {
            log.error(
                    "Error calling {} function with hostname {}, provider {}, token {}, secret {}, ",
                    KEY, appServiceHost, provider, token, tokenSecret, e);
        } catch (UnsupportedEncodingException e) {
            log.error("Error calling {}, ", KEY, e);
        } catch (ClientProtocolException e) {
            log.error("Error calling {}, ", KEY, e);
        } catch (IOException e) {
            log.error("Error calling {}, ", KEY, e);
        }

        return authenticationToken;
    }
    
    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
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
