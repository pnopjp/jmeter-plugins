# Functions

- **[AzAdAccessToken](#__azadaccesstoken)**  
    Get an access token from Microsoft Entra ID.
- **[AzAppServiceAuthenticationToken](#__AzAppServiceAuthenticationToken)**  
    Get an authentication token for Azure App Service
- **[AzCosmosDbAuthZ](#__azcosmosdbauthz)**  
    Generate a string to be specified in the Authorization header for accessing Cosmos DB.
- **[hmac](#__hmac)**  
    Generate a HMAC in the specific hash algorithm with the key and variable name.

## How to install

Download jmeter-plugins-functions-azure-?.?.?.jar file from [latest release](https://github.com/pnopjp/jmeter-plugins/releases/latest) and put it into lib/ext directory of JMeter \(e.g. /usr/local/jmeter/lib/ext\), then restart JMeter.

## __AzAdAccessToken

This function gets the access token for the Microsoft Entra ID application from the Microsoft Entra ID token endpoint with a password flow.

### Parameters

|Attribute|Description|Required|
|----|----|----|
|Microsoft Entra ID tenant ID|Tenant ID of Microsoft Entra ID.|Yes|
|Grant type|Only "password" can be used.|Yes|
|Application \(Client\) ID|The client identifier of Microsoft Entra ID application.|Yes|
|Client secret|Client secret of Microsoft Entra ID application.|Yes|
|Username|Access user account name. \(e.g. name<span></span>@example.onmicrosoft.com\)|Yes|
|Password|Access user password.|Yes|
|Scope|Acess Token Scope.|No|
|Resource|App ID URI of the receiving web service.<br />Valid only for Microsoft Entra ID version 1.<br />If you omit this, the "Application ID" value is applied.|No|
|Microsoft Entra ID version|v2.0 or empty string.|No|
|Microsoft Entra ID endpoint URI|Specify when using an Microsoft Entra ID endpoint, such as Azure Government or Azure China.<br />\(e.g. login<span></span>.partner.microsoftonline.cn\)<br />Or you can use some abbreviations. \(us / cn / de\)<br />The default is Azure global endpoint.<br />\* But I haven't been able to test with Azure Govement, Azure China or Azure Germany, because I don't have subscriptions there. If you use these Azure, I would be happy to have you report your results to [here](https://github.com/pnopjp/jmeter-plugins/issues).|No|
|Name of variable|The name of the variable to set.|No|

### Examples

```text
${__AzAdAccessToken(example.onmicrosoft.com,password,01234567-89ab-cdef-0123-456789abcdef,TX2********pJ-,user1@example.onmicrosoft.com,<PASSWORD>)}
```

**returns** eyJ\*\*\*\*\*\*\*\*\*\*yJ9.eyJ\*\*\*\*\*\*\*\*\*\*CJ9.px9\*\*\*\*\*\*\*\*\*\*S5O

### Sample files

- [AzAdAccessToken.jmx](../samples/AzAdAccessToken.jmx)
- [AzAdAccessToken_users.csv](../samples/AzAdAccessToken_users.csv)

### Examples of usage scenarios

- Creating Authorization header for requesting to the Azure App Service with Easy Auth configured.

### Tutorial

- [Using Apache JMeter™ to perform load testing for the Azure App Service that requires Azure AD authentication](https://blog.pnop.co.jp/jmeter-webapps-azuread-auth_en/)

## __AzAppServiceAuthenticationToken

AzAppServiceAuthenticationToken gets the authentication token that should be specified in X-ZUMO-AUTH when accessing the Easy Auth-rated Azure App Service.

### Parameters

|Attribute|Description|Required|
|----|----|----|
|Azure App Service hostname|Your Azure App Service hosname \(e.g. example<span></span>.azurewebsites.net, www<span></span>.example.com\)|Yes|
|Provider|Authentication provider. \(aad, google, facebook, twitter\)|Yes|
|access_token or id_token|Access token (aad, facebook, twitter) or id_token (google) obtained from the provider.|Yes|
|twitter access_token_secret|In the case of twitter, set Access token secret.|No|
|Name of variable|The name of the variable to set.|No|

### Examples

```text
${__AzAppServiceAuthenticationToken(example.azurewebsites.net,facebook,EAA****WQM)}
${__AzAppServiceAuthenticationToken(www.example.com,twitter,423****o3G,JmG****2V1)}
```

**returns** eyJ\*\*\*\*\*\*\*\*lZw

### Sample files

- [AzAppServiceAuthenticationToken.jmx](../samples/AzAppServiceAuthenticationToken.jmx)

### Examples of usage scenarios

- Creating X-ZUMO-AUTH header for requesting to the Azrue App Service with Easy Auth configured.

## __AzCosmosDbAuthZ

AzCosmosDbAuthZ frunction returns a string to be specified in the Authorization header for accessing Cosmos DB.

### Parameters

|Attribute|Description|Required|
|----|----|----|
|Cosmosdb Key|Cosmos DB master key|Yes|
|x-ms-date header variable name|Name of variable in which to store for x-ms-date header.|Yes|
|HTTP Request method|The string is the HTTP verb, such as GET, POST, or PUT.|Yes|
|ResourceType|Type of resource that the request is for, Eg. "dbs", "colls", "docs".|Yes|
|ResourceLink|Identity property of the resource that the request is directed at. ResourceLink must maintain its case for the ID of the resource. Example, for a collection it looks like: "dbs/MyDatabase/colls/MyCollection".|Yes|
|TokenVersion|The version of the token.(optinal) currently 1.0.|No|

### Examples

```text
${__AzCosmosDbAuthZ(dxN************79w==,headers.x-ms-date,GET,docs,dbs/SampleDB/colls/Persons)}
```

**returns** type%3Dmaster%26ver%3D1.0%26sig%3DEeW\*\*\*\*\*\*\*\*\*\*\*\*sW4%3D  
headers.x-ms-date=Mon, 07 Sep 2020 00:34:57 GMT

### Sample files

- [AzCosmosDbAuthZ.jmx](../samples/AzCosmosDbAuthZ.jmx)

### Tutorial

- [Load test with Apache JMeter™ against Azure Cosmos DB \(SQL API\)](https://blog.pnop.co.jp/jmeter-azure-cosmosdb_en/)

## __hmac

The hmac function returns a HMAC in the specific hash algorithm with the key and variable name.

### Parameters

|Attribute|Description|Required|
|----|----|----|
|Algorithm|The algorithm to be used to hash For possible algorithms See Mac in [StandardNames](https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac) <ul><li>HmacMD5</li><li>HmacSHA1</li><li>HmacSHA224</li><li>HmacSHA256</li><li>HmacSHA384</li><li>HmacSHA512</li></ul>|Yes|
|String to HMAC|The String that will be HMAC|Yes|
|Private key|Base64 encoded private key to be used for hashing|Yes|
|Name of variable|The name of the variable to set|No|

### Examples

```text
${__hmac(HmacSHA256,Hello World,SGVsbG8gQXp1cmUh,)}
```

**returns** qLGOpn6DRZMiXdjA2RLctf5Ya0ZPtgGP97ZkBkCW3xg=

### Sample files

- [HmacEncodeFunction.jmx](../samples/HmacEncodeFunction.jmx)

### Examples of usage scenarios

- Creating Authorization header for requesting to the CosmosDB via REST API  
