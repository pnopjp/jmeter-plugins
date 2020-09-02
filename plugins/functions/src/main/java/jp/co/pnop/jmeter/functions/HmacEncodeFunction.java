package jp.co.pnop.jmeter.functions;

import org.apache.commons.codec.binary.Base64;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HmacEncodeFunction extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(HmacEncodeFunction.class);

    private static final List<String> desc = new LinkedList<String>();
    private static final String KEY = "__hmac";

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 3;
    private static final int MAX_PARAMETER_COUNT = 4;

    static {
        desc.add("Hash algorithm");
        desc.add("String to be HMAC hashed");
        desc.add("Private Key to be used for hashing");
        desc.add("Name of variable in which to store the result (optional)");
    }

    private CompoundVariable[] values;

    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String algorithm = values[0].execute();
        String sourceString = values[1].execute();
        String privateKey = values[2].execute();
        String hashedString = null;

        try {
	        Mac mac = Mac.getInstance(algorithm);
	        mac.init(new SecretKeySpec(Base64.decodeBase64(privateKey.getBytes()), algorithm));
	        hashedString = new String(Base64.encodeBase64(mac.doFinal(sourceString.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            log.error("Error calling {} function with value {}, hash algorithm {}, ", KEY, sourceString, algorithm, e);
        } catch (InvalidKeyException e) {
            log.error("Error calling {} function with value {}, private key {}, ", KEY, sourceString, privateKey, e);
        } catch (IllegalArgumentException e) {
            log.error("Error calling {} function with value {}, hash algorithm {}, private key {}, ", KEY, sourceString, algorithm, privateKey, e);
        }
        
        if (values.length > 3) {
            String variableName = values[3].execute();
            if (variableName.length() > 0) {// Allow for empty name
                final JMeterVariables variables = getVariables();
                if (variables != null) {
                    variables.put(variableName, hashedString);
                }
            }
        }
        return hashedString;
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
