# Samplers

- [Azure Event Hubs](#azureeventhubs)

## Azure Event Hubs

This sampler lets you send an AMQP request to an Azure Event Hubs.  

Currently, this sampler does not support all Azure Event Hubs features. For example, authentication with Azure AD, send properties, etc are not supported.  
I will prioritize the implementation of the ones that are most frequently requested by everyone.

### Parameters

|Attribute|Description|Required|
|-----|-----|-----|
|Name|Descriptive name for this sampler that is shown in the tree|No|
|Event Hubs Namespace|Azure Event Hubs namespace name to send messages to.<br />(e.g. YOUREVENTHUBS<span></span>.servicebus.windows.net\)|Yes|
|Event Hub|Event Hub name to send messages to.|Yes|
|Shared Access Policy|Shared access policy name of the Event Hubs namespace or Event Hub.|Yes|
|Shared Access Key|Shared access keys for the shared access policy|Yes|
|Partition|Partition to send message to. You can select either Not specified, Partition Key, or Partition ID.|No|
|Event data|List of messages to be sent in batches. Select "String" to send a UTF-8 string, "Base64 encoded binary" to send a Base64 encoded binary, or "File" to send a file as binary.|No|

### Sample files

- [AzEventHubsSampler.jmx](../samples/AzEventHubsSampler.jmx)
<!-- ### Tutorial -->
