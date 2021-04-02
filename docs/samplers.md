# Samplers

- [Azure Event Hubs](#azure-event-hubs)

## Azure Event Hubs

This sampler lets you send an AMQP request to an Azure Event Hubs.  

Currently, this sampler does not support all Azure Event Hubs features. For example, send properties, etc are not supported.  
I will prioritize the implementation of the ones that are most frequently requested by everyone.

### How to install

Download jmeter-plugins-azure-event-hubs.?.?.?.jar file from [latest release](https://github.com/pnopjp/jmeter-plugins/releases/latest) and put it into lib/ext directory of JMeter \(ex. /usr/local/jmeter/lib/ext\), then restart JMeter.

### Parameters

|Attribute|Description|Required|
|-----|-----|-----|
|Name|Descriptive name for this sampler that is shown in the tree|No|
|Event Hubs Namespace|Azure Event Hubs namespace name to send messages to.<br />(e.g. YOUREVENTHUBS<span></span>.servicebus.windows.net\)|Yes|
|Event Hub|Event Hub name to send messages to.|Yes|
|Partition|Partition to send message to. You can select either Not specified, Partition Key, or Partition ID.|No|
|Auth Type|Authorization type to use when sending messages to Azure Event Hubs.<br />If you select "Azure AD credential", also define the Azure AD Credential Config Element.|Yes|
|Variable Name of credential declared in Azure AD Crednetial|\(Set only if "Azure AD credential" is selected for Auth type.\)<br />Variable name of credential declared in Azure AD Credential.|Yes|
|Shared Access Policy|\(Set only if "Shared access signature" is selected for Auth type.\)<br />Shared access policy name of the Event Hubs namespace or Event Hub.|Yes|
|Shared Access Key|\(Set only if "Shared access signature" is selected for Auth type.\)<br />Shared access keys for the shared access policy|Yes|
|Event data|List of messages to be sent in batches. Select "String" to send a UTF-8 string, "Base64 encoded binary" to send a Base64 encoded binary, or "File" to send a file as binary.|No|

### Sample files

- [AzEventHubsSampler.jmx](../samples/AzEventHubsSampler.jmx)

### Tutorial

- [How to request to Azure Event Hubs with Apache JMeter™](https://blog.pnop.co.jp/jmeter-azure-event-hubs_en/)
