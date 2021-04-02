# Configurations

- **[Azure AD Credential](#azure-ad-credential)**  
    Configuration for authentication and authorization by Azure AD.

## Azure AD Credential

This is the configuration for authentication and authorization by Azure AD.

### How to install

Download jmeter-plugins-azure-ad.?.?.?.jar file from [latest release](https://github.com/pnopjp/jmeter-plugins/releases/latest) and put it into lib/ext directory of JMeter \(ex. /usr/local/jmeter/lib/ext\), then restart JMeter.
> Some plugins include the classes contained in this jar file.

### Common parameters

|Attribute|Description|Required|
|-----|-----|-----|
|Name|Descriptive name for this sampler that is shown in the tree|No|
|Variable Name for created credential|Variable name bound to credential. This needs to uniquely identify the configuration. It is used by the Samplers to identify the configuration to be used.|Yes|
|Credential type|Types of credentail.|Yes|
|Authority host|Specifies the Azure Active Directory endpoint to acquire tokens.<br />For general Microsoft Azure, select "login<span></span>.microsoft.online.com". For the Azure Government or Azure China 21Vianet, etc., select the appropriate endpoint.|Yes|

### Parameters for each credential type

The parameters to be set for each credential type are different.

#### Client certificate

|Attribute|Description|Required|
|-----|-----|-----|
|Tenant Id|Azure AD tenant Id.|Yes|
|Client Id|Client \(Application\) Id of Azure AD application.|Yes|
|File type|Certification file type. \(PEM or PFX\)|Yes|
|Filename|Certification file name.|Yes|
|Password|Password for certification file.|No|

#### Client secret

|Attribute|Description|Required|
|-----|-----|-----|
|Tenant Id|Azure AD tenant Id.|Yes|
|Client Id|Client \(Application\) Id of Azure AD application.|Yes|
|Client Secret|Client secret for Azure AD application.|Yes|

#### Managed identity

|Attribute|Description|Required|
|-----|-----|-----|
|Client Id|Client \(Application\) Id of Azure AD application.|No|

### Sample files

- [AzEventHubsSampler.jmx](../samples/AzEventHubsSampler.jmx)  
    Use in testing to Azure Event Hubs
