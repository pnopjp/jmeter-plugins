# Functions

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

**returns** type%3Dmaster%26ver%3D1.0%26sig%3DEeW************sW4%3D  
headers.x-ms-date=Mon, 07 Sep 2020 00:34:57 GMT

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

### Examples of usage scenarios

- Creating Authorization header for requesting to the CosmosDB via REST API  
    <!--use case: <https://blog.pnop.co.jp/jmeter-azure-cosmosdb_en/>-->
