package jp.co.pnop.jmeter.functions;

import org.apache.commons.codec.binary.Base64;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzCosmosDbAuthZ extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(AzCosmosDbAuthZ.class);

    private static final List<String> desc = new ArrayList<String>();
    private static final String KEY = "__AzCosmosDbAuthZ";

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 5;
    private static final int MAX_PARAMETER_COUNT = 6;

    private static final String algorithm = "HmacSHA256";

    static {
        desc.add("CosmosDB master key.");
        desc.add("Name of variable in which to store for x-ms-date header.");
        desc.add("HTTP Request method");
        desc.add("Type of resource that the request is for, Eg. \"dbs\", \"colls\", \"docs\".");
        desc.add("Identity property of the resource that the request is directed at. ResourceLink must maintain its case for the ID of the resource. Example, for a collection it looks like: \"dbs/MyDatabase/colls/MyCollection\".");
        desc.add("Version of the token. (optional) currently 1.0.");
    }

    private CompoundVariable[] values;

    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String cosmosdbKey = values[0].execute().trim();
        String requestMethod = values[2].execute().trim();
        String resourceType = values[3].execute().trim();
        String resourceLink = values[4].execute().trim();
        String tokenVersion = values.length > 5 ? values[5].execute().trim() : "1.0";

        String authzHeader = null;
        String sourceString = null;

        try {
            String dateTimeString = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz").format(ZonedDateTime.now(ZoneId.of("GMT")));
            sourceString = requestMethod.toLowerCase() + "\n" + resourceType + "\n" + resourceLink + "\n" + dateTimeString.toLowerCase() + "\n\n";
	        Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(Base64.decodeBase64(cosmosdbKey.getBytes()), algorithm));
            authzHeader = URLEncoder.encode("type=master&ver=" + tokenVersion + "&sig=" + new String(Base64.encodeBase64(mac.doFinal(sourceString.getBytes()))), "UTF-8");;
            addVariableValue(dateTimeString, values, 1);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error calling {} function with ResourceType {}, ResourceLink {}, hash algorithm {}, ", KEY, resourceType, resourceLink, algorithm, e);
        } catch (InvalidKeyException e) {
            log.error("Error calling {} function with ResourceType {}, ResourceLink {}, CosmosDB key {}, ", KEY, resourceType, resourceLink, cosmosdbKey, e);
        } catch (IllegalArgumentException e) {
            log.error("Error calling {} function with ResourceType {}, ResourceLink {}, CosmosDB key {}, ", KEY, resourceType, resourceLink, cosmosdbKey, e);
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("Error calling {} function, ", KEY, e);
        }

        return authzHeader;
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
