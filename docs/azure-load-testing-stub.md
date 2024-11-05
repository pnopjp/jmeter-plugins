# Stub function for Azure Load Testing

This plugin is a Stub that allows custom functions that are only available in Azure Load Testing to work in pure Apache JMeter™.  
Enables Apache JMeter™ to use custom functions when creating and testing test scripts to be uploaded to Azure Load Testing.  

Currently, Azure Load Testing only provides 'GetSecret' custom function.

## How to install

Download jmeter-plugins-azure-load-testing-stub-?.?.?.jar file from [latest release](https://github.com/pnopjp/jmeter-plugins/releases/latest) and put it into lib/ext directory of JMeter \(e.g. /usr/local/jmeter/lib/ext\), then restart JMeter.

## __GetSecret

Azure Load Testing retrieves secret values from Azure Key Vault or secret store of CI/CD workflow.  
This stub retrieve the secret value stored in one of the environment variables, jmeter.properties, user.properties, or Azure Key Vault.

### Parameters

|Attribute|Required|
|----|----|
|Secret name|Yes|

### Setting up a secret store

Configure the following settings according to the type of secret store.

- [Environment variables](#get-secret-value-from-environment-variables)
- [jmeter.properties or user.properties](#get-secret-value-from-jmeterproperties-or-userproperties)
- [Azure Key Vault](#get-secret-value-from-Azure-Key-Vault)

#### Get secret value from environment variables

1. Set 'environment_variable' as the secret store type in jmeter.properties or user.properties.  
    **azure_load_testing_stub.get_secret.{SECRET_NAME}.store_type=environment_variable**  

    Replace {SECRET_NAME} with the secret name you specify as the argument of GetSecret custom function.  

    e.g. **${__GetSecret(secretName)}**

    ```txt
    azure_load_testing_stub.get_secret.secretName.store_type=environment_variable
    ```

1. Set secret value to environment variables.  
    **AZURE_LOAD_TESTING_STUB_GET_SECRET_{SECERT_NAME}={SECRET_VALUE}**  

    Replace {SECRET_NAME} with the secret name you specify as the argument of GetSecret custom function.  

    e.g. **${__GetSecret(secretName)}**

    ```sh
    AZURE_LOAD_TESTING_STUB_GET_SECRET_secretName=************
    ```

1. Start or restart the Apache JMeter™.

#### Get secret value from jmeter.properties or user.properties

1. Set the secret store type and secret value in jmeter.properties or user.properties.
    - Secret store type is 'jmeter_properties'  
        **azure_load_testing_stub.get_secret.{SECRET_NAME}.store_type=jmeter_properties**
    - Secret value  
        **azure_load_testing_stub.get_secret.{SECRET_NAME}.value={SECRET_VALUE}**

    Replace {SECRET_NAME} with the secret name you specify as the argument of GetSecret custom function.  

    e.g. **${__GetSecret(secretName)}**

    ```txt
    azure_load_testing_stub.get_secret.secretName.store_type=jmeter_properties
    azure_load_testing_stub.get_secret.secretName.value=************
    ```

1. Start or restart the Apache JMeter™.

#### Get secret value from Azure Key Vault

1. [Add the secret to Azure Key Vault](https://docs.microsoft.com/en-us/azure/key-vault/secrets/quick-create-portal#add-a-secret-to-key-vault), if it doesn't exist yet.

1. Allow access to the Key Vault to the appropriate objects \(User, Service principal, Azure resource\) according to the authentication method.  
The authentication method can be selected from the following.

    - Client secret  
        Authenticates the service principal through its client secret.
    - Client certificate  
        Authenticates the service principal through its client certificate.
    - Environment variables  
        Authenticates with environment varialbes
    - Managed ID  
        Authenticating with system-assigned or user-assigned managed identity
    - Interactive brwoser  
        Interactively authenticates a user with the default system web browser
    - Azure CLI  
        Authenticates with the enabled user or service principal in Azure CLI
    - Visual Studio Code  
        Authenticate with the user information logged in from the VS Code IDE by the VS Code Azure Account extension.

1. Set the parameters for authentication to the Key Vault in jmeter.properties or user.properties.  
    The parameters to be set are different for each authentication method.  
    It should be written in the following format.  
    **azure_load_testing_stub.get_secret.{SECRET_NAME}.{PARAMETER}**
    - **Client secret**
        |Parameter|Description|Required|
        |-|-|-|
        |store_type|Specify 'keyvault'|Yes|
        |auth_type|Specify 'client_secret'|Yes|
        |tenant_id|AAD tenant ID of the AAD application.<br />e.g.<br />youredomain<span></span>.onmicrosoft.com<br />01234567-89ab-cdef-0123-467-89abcdef0123|Yes|
        |client_id|Client ID of the AAD application<br />e.g.<br />01234567-89ab-cdef-0123-467-89abcdef0123|Yes|
        |client_secret|Secret value of the AAD application|Yes|
        |authority_host|AAD endpoint to acquire tokens.<br />Specify one of the following, or uri.<ul><li>AzurePublicCloud</li><li>AzureGovernment</li><li>AzureChina</li><li>AzureGermany</li></ul>|No|
    - **Client certificate**
        |Parameter|Description|Required|
        |-|-|-|
        |store_type|Specify 'keyvault'|Yes|
        |auth_type|Specify 'client_certificate'|Yes|
        |tenant_id|AAD tenant ID of the AAD application<br />e.g.<br />youredomain<span></span>.onmicrosoft.com<br />01234567-89ab-cdef-0123-467-89abcdef0123|Yes|
        |client_id|Client ID of the AAD application<br />e.g.<br />01234567-89ab-cdef-0123-467-89abcdef0123|Yes|
        |certificate_type|Certificate file format<br />Specify 'PFX' or 'PEM'|Yes|
        |certificate_file|Path of the certificate file for authenticating to AAD.<br />If the directory separator is '\\', replace it to '\\\\'.<br />e.g. C:\\\\work\\\\cert.pem |Yes|
        |certificate_password|Password protecting the PFX file|No|
        |authority_host|AAD endpoint to acquire tokens.<br />Specify one of the following, or uri.<ul><li>AzurePublicCloud</li><li>AzureGovernment</li><li>AzureChina</li><li>AzureGermany</li></ul>|No|
    - **Environment variables**
        |Parameter|Description|Required|
        |-|-|-|
        |store_type|Specify 'keyvault'|Yes|
        |auth_type|Specify 'environment_variables'|Yes|
        |authority_host|AAD endpoint to acquire tokens.<br />Specify one of the following, or uri.<ul><li>AzurePublicCloud</li><li>AzureGovernment</li><li>AzureChina</li><li>AzureGermany</li></ul>|No|

        In addition, set the environment variables as follows.  
        <https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-azure-hosted-auth#environment-variables>
    - **Managed ID**
        |Parameter|Description|Required|
        |-|-|-|
        |store_type|Specify 'keyvault'|Yes|
        |auth_type|Specify 'managed_id'|Yes|
        |client_id|Client ID of user assigned or system assigned identity|No|
    - **Interactive browser**
        |Parameter|Description|Required|
        |-|-|-|
        |store_type|Specify 'keyvault'|Yes|
        |auth_type|Specify 'interactive_browser'|Yes|
        |authority_host|AAD endpoint to acquire tokens.<br />Specify one of the following, or uri.<ul><li>AzurePublicCloud</li><li>AzureGovernment</li><li>AzureChina</li><li>AzureGermany</li></ul>|No|
    - **Azure CLI**
        |Parameter|Description|Required|
        |-|-|-|
        |store_type|Specify 'keyvault'|Yes|
        |auth_type|Specify 'azure_cli'|Yes|
    - **Visual Studio Code**
        |Parameter|Description|Required|
        |-|-|-|
        |store_type|Specify 'keyvault'|Yes|
        |auth_type|Specify 'vs_code'|Yes|
        |tenant_id|Tenant ID of the AAD application|No|

1. Set the parameters to identify the secret.
    |Parameter|Description|Required|
    |-|-|-|
    |vault_uri|Vault URI of Azure Key Vault<br />e.g. https:/<span></span>/yourekeyvault.vault.azure.net|Yes|
    |secret_name|Secret name in Azure Key Vault|Yes|
    |secret_version|Secret version in Azure Key Vault|No|

1. Start or restart the Apache JMeter™.

### jmeter.properties/user.properties Reference

|Parameter|Description|store_type|Required|
|-|-|-|-
|store_type|Where to store the secret<ul><li>environment_variables</li><li>jmeter_properties</li><li>keyvault</li></ol>|environment_variables<br />jmeter_properties<br />keyvalut|Yes|
|value|Secret value when store_type is jmeter_properties|jmeter_properties|No|
|auth_type|Authentication method when store_type is keyvault<ul><li>client_secret</li><li>client_certificate</li><li>environment_variables</li><li>managed_id</li><li>interactive_browser</li><li>azure_cli</li><li>vs_code</li></ul>|keyvault|No|
|tenant_id|AAD tenant ID of the AAD application.<br />e.g.<br />youredomain<span></span>.onmicrosoft.com<br />01234567-89ab-cdef-0123-467-89abcdef0123|keyvault|No|
|client_id|Client ID of the AAD application<br />e.g.<br />01234567-89ab-cdef-0123-467-89abcdef0123|keyvault|No|
|client_secret|Secret value of the AAD application|keyvault|No|
|certificate_type|Certificate file format<br />Specify 'PFX' or 'PEM'|keyvault|No|
|certificate_file|Path of the certificate file for authenticating to AAD|keyvault|No|
|certificate_password|Password protecting the PFX file|keyvault|No|
|authority_host|AAD endpoint to acquire tokens.<br />Specify one of the following, or uri.<ul><li>AzurePublicCloud</li><li>AzureGovernment</li><li>AzureChina</li><li>AzureGermany</li></ul>|keyvault|No|
|secret_name|Secret name in Azure Key Vault|environment_variables<br />jmeter_properties<br />keyvalut|Yes|
|secret_version|Secret version in Azure Key Vault|environment_variables<br />jmeter_properties<br />keyvalut|No|

e.g. **${__GetSecret(secretName)}**

```txt
azure_load_testing_stub.get_secret.secretName.store_type=keyvault
azure_load_testing_stub.get_secret.secretName.auth_type=client_certificate
azure_load_testing_stub.get_secret.secretName.certificate_type=PEM
azure_load_testing_stub.get_secret.secretName.certificate_file=C:\\work\\cert.pem
azure_load_testing_stub.get_secret.secretName.tenant_id=01234567-89ab-cdef-0123-4567890abcde
azure_load_testing_stub.get_secret.secretName.client_id=f0123456-789a-bcde-f012-34567890abcd
azure_load_testing_stub.get_secret.secretName.vault_uri=https://yourkeyavault.vault.azure.net
azure_load_testing_stub.get_secret.secretName.secret_name=KeyVaultSecretName
azure_load_testing_stub.get_secret.secretName.secret_version=01234567890abcdef
```

### Notes

The GetSecret custom function seems to be available only in user-defined variables.  
If you use it with other samplers or user parameters (Pre Processors), it will return an empty string.  
Therefore, even in this stub, the value is returned only when it is used in user-defined variables.  
If you find a component that returns values other than user-defined valiables, please report it from the [issue](https://github.com/pnopjp/jmeter-plugins/issues).
