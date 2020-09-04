# Functions

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

returns **qLGOpn6DRZMiXdjA2RLctf5Ya0ZPtgGP97ZkBkCW3xg=**

### Examples of usage scenarios

- Creating Authorization header for requesting to the CosmosDB via REST API  
    <!--use case: <https://blog.pnop.co.jp/jmeter-azure-cosmosdb_en/>-->
