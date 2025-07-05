# Samplers

- [Azure Event Hubs](#azure-event-hubs)
- [Azure Service Bus](#azure-service-bus)
- [Azure Storage Queue](#azure-storage-queue)

## Azure Event Hubs

This sampler lets you send an AMQP request to an Azure Event Hubs.  

Currently, this sampler does not support all Azure Event Hubs features. For example, send properties, etc are not supported.  
I will prioritize the implementation of the ones that are most frequently requested by everyone.  

Works with Apache JMeter™ v5.4.1 or later.

### How to install

Download jmeter-plugins-azure-eventhubs.?.?.?.jar file from [latest release](https://github.com/pnopjp/jmeter-plugins/releases/latest) and put it into lib/ext directory of JMeter \(e.g. /usr/local/jmeter/lib/ext\), then restart JMeter.

### Parameters

|Attribute|Description|Required|
|-----|-----|-----|
|Name|Descriptive name for this sampler that is shown in the tree|No|
|Event Hubs Namespace|Azure Event Hubs namespace name to send messages to.<br />\(e.g. YOUREVENTHUBS<span></span>.servicebus.windows.net\)|Yes|
|Event Hub|Event Hub name to send messages to.|Yes|
|Partition|Partition to send message to. You can select either Not specified, Partition Key, or Partition ID.|No|
|Auth Type|Authorization type to use when sending messages to Azure Event Hubs.<br />If you select "Microsoft Entra ID credential", also define the Microsoft Entra ID Credential Config Element.|Yes|
|Shared Access Policy [\*1](#1-eventhubs)|Shared access policy name of the Event Hubs namespace or Event Hub.|No|
|Shared Access Key [\*1](#1-eventhubs)|Shared access keys for the shared access policy|No|
|Variable Name of credential declared in Microsoft Entra ID Crednetial|The variable name of the credential declared in Microsoft Entra ID Credential, specified if "Microsoft Entra ID credential" is selected for "Auth Type".|No|
|Event data|List of messages to be sent in batches. Select "String" to send a UTF-8 string, "Base64 encoded binary" to send a Base64 encoded binary, or "File" to send a file as binary.|No|

<span id="1-eventhubs">\*1</span>: Set these parameters only if "Shared access signature" is selected in Auth type.

### Sample files

- [AzEventHubsSampler.jmx](../samples/AzEventHubsSampler.jmx)

### Tutorial

- [How to request to Azure Event Hubs with Apache JMeter™](https://blog.pnop.co.jp/jmeter-azure-event-hubs_en/)

## Azure Service Bus

This sampler lets you send an AMQP request to an Azure Service Bus.  

Currently, this sampler does not support all Azure Service Bus features.   
I will prioritize the implementation of the ones that are most frequently requested by everyone.  

Works with Apache JMeter™ v5.4.1 or later.

### How to install

Download jmeter-plugins-azure-servicebus.?.?.?.jar file from [latest release](https://github.com/pnopjp/jmeter-plugins/releases/latest) and put it into lib/ext directory of JMeter \(e.g. /usr/local/jmeter/lib/ext\), then restart JMeter.

### Parameters

|Attribute|Description|Required|
|-----|-----|-----|
|Name|Descriptive name for this sampler that is shown in the tree|No|
|Connection/Transaction|Choose whether to create a new connection, use a previously created connection, or use a previously created transaction.|Yes|
|Variable Name of Connection/Transaction Defined in Azure Service Bus Sampler|Name of the previously defined Connection or Transaction, if "User Defined Connection" or "User Defined Transaction" was selected for Connection/Transaction.|No|
|Service Bus Namespace [\*1](#1-servicebus)|Azure Service Bus namespace name to send messages to.<br />(e.g. YOURSERVICEBUS<span></span>.servicebus.windows.net\)|Yes|
|Send messages to [\*1](#1-servicebus)|Choose whether to send the messages to the "Queue" or to the "Topic".|Yes|
|Queue name / Topic name [\*1](#1-servicebus)|Queue/Topic name to send messages to.|Yes|
|Protocol|Protocol for sending messages|Yes|
|Auth Type [\*1](#1-servicebus)|Authorization type to use when sending messages to Azure Event Hubs.<br />If you select "Microsoft Entra ID credential", also define the Microsoft Entra ID Credential Config Element.|Yes|
|Shared Access Policy [\*1](#1-servicebus) [\*2](#2-servicebus)|Shared access policy name of the Event Hubs namespace or Event Hub.|No|
|Shared Access Key [\*1](#1-servicebus) [\*2](#2-servicebus)|Shared access keys for the shared access policy|No|
|Variable Name of credential declared in Microsoft Entra ID Crednetial [\*1](#1-servicebus)|The variable name of the credential declared in Microsoft Entra ID Credential, specified, if "Microsoft Entra ID credential" is selected for "Auth Type".|No|
|Create transaction before sending messages|Create a transaction before sending a messages.|No|
|Variable name for created transaction|The name of the transaction to create, if "Create transaction before sending messages" is turned on.|No|
|Transaction state|Specify whether to commit or rollback, if "Use Defined Transaction" is selected for "Connection/Transaction".<ul><li>\[Continue transaction\]<br />No commit, and no rollback.</li><li>\[Commit transaction after sending messages\]<br />Commits the specified transaction after sending the messages.</li><li>\[Rollback transaction before sending messages\]<br />Sends the messages after the specified transaction is rolled back. (Sending messages are not included in the transaction)</li></ul>|No|
|Messages|List of messages to be sent in batches. Select "String" to send a UTF-8 string, "Base64 encoded binary" to send a Base64 encoded binary, or "File" to send a file as binary.<br /><br />To also send headers, standard properties and message atributes, fill in the "headers/properties/attributes" column in JSON format. There you can include the following as keys<ul><li>"correlation-id" or "CorrelationId"</li><li>"reply-to" or "ReplyTo"</li><li>"reply-to-group-id" or "ReplyToSessionId"</li><li>"to"</li><li>"ttl" or "TimeToLive"</li><li>"x-opt-scheduled-enqueue-time" or "ScheduledEnqueueTime"</li></ul>(e.g., {"reply-to": "foo<span></span>@example.com", "ttl": "3 HOURS", "ScheduledEnqueueTime": "2022-08-03T10:15:30+01:00"})<br /><br />To also send custom properties (user-defined properties), fill in the "custom properties" column in JSON format. (e.g., {"prop1": "value1", "prop2":2})<br /><br />Multiple messages exceeding a total of 256 KByte cannot be sent together.<br />When sending multiple messages, please make sure that they do not exceed 256 KByte or send only one message. (If you send only one message, it can exceed 256 KByte.|No|

<span id="1-servicebus">\*1</span>: If "Create New Connection" is selected for "Connection/Transaction", set these parameters.  
<span id="2-servicebus">\*2</span>: If "Shared access signature" is selected for Auth type, set these parameters.

### Sample files

- [AzServiceBusSampler.jmx](../samples/AzServiceBusSampler.jmx)

### Tutorial

- [How to request to Azure Service Bus by Apache JMeter™](https://blog.pnop.co.jp/jmeter-azure-service-bus_en/)

## Azure Storage Queue

This sampler lets you send a request to an Azure Storage Queue.  

Currently, this sampler does not support all Azure Storage Queue features. For example, update a message, etc are not supported.  
I will prioritize the implementation of the ones that are most frequently requested by everyone.  

Works with Apache JMeter™ v5.4.1 or later.

### How to install

Download jmeter-plugins-azure-storage-queue.?.?.?.jar file from [latest release](https://github.com/pnopjp/jmeter-plugins/releases/latest) and put it into lib/ext directory of JMeter \(e.g. /usr/local/jmeter/lib/ext\), then restart JMeter.

### Parameters

|Attribute|Description|Required|
|-----|-----|-----|
|Name|Descriptive name for this sampler that is shown in the tree|No|
|Auth type|Authorization type to use when sending messages to Azure Storage Queue.<br />If you select "Microsoft Entra ID credential", also define the Microsoft Entra ID Credential Config Element.|Yes|
|Connection string|Connection string of the target Azure Storage, specified if "Auth type" is selected for "Connection string".|No|
|Default Endpoints Protocol [\*1](#1-storagequeue)|Select the protocol from "http" or "https".|No|
|Account Name [\*1](#1-storagequeue)|Target Azure storage account name.|No|
|Storage Key [\*1](#1-storagequeue)|Storage key of the target Azure Storage.|No|
|Endpoint Suffix [\*1](#1-storagequeue)|Usually, "core<span></span>.windows.net" is specified. However, if your target is Azure Government, specify "core<span></span>.usgovcloudapi.net", and if your target is Azure China, specify "core<span></span>.chinacloudapi.cn". In this way, if a Storage Queue other than Azure Public is targeted, specify the appropriate suffix.|No|
|Variable Name of credential declared in Microsoft Entra ID Crednetial [\*2](#2-storagequeue)|The variable name of the credential declared in Microsoft Entra ID Credential.|No|
|Endpoint URL [\*2](#2-storagequeue) [\*3](#3-storagequeue)|Endpoint URL of the target Azure Storage Queue.|No|
|Queue name|Queue name to send message to.|Yes|
|SAS token [\*3](#3-storagequeue)|Shared access signature token string. It is sometimes called a "Query string".|No|
|Message type|To send a string or Base64 encoded binary string, select "String / Base64 encoded binary".<br />To send a file, select "File".|Yes|
|Message|Enter the string or Base64 encoded binary string to be sent as a message, if "String / Base64 encoded binary" is selected for "Message type".|No|
|Message filename|Enter the filename to be sent as a message, if "FIle" is selected for "Message type".|No|
|Visibility timeout \(sec\)|The timeout period for how long the message is invisible in the queue. If unset the value will default to 0 and the message will be instantly visible. The timeout must be between 0 seconds and 604,800 seconds.|No|
|Time to live \(sec\)|How long the message will stay alive in the queue. If unset the value will default to 604,800 seconds, if "-1" is passed the message will not expire. The time to live must be "-1" or any positive number of seconds.|No|
|Timeout \(sec\)|Timeout applied to the operation.|No|

<span id="1-storagequeue">\*1</span>: If "Storage key" is selected for "Auth type", set these parameters.  
<span id="2-storagequeue">\*2</span>: If "Azuer AD credential" is selected for "Auth type", set these parameters.  
<span id="3-storagequeue">\*3</span>: If "Shared access signature" is selected for "Auth type", set these parameters.  

### Sample files

- [AzStorageQueueSampler.jmx](../samples/AzStorageQueueSampler.jmx)

<!--
### Tutorial

- [How to request to Azure Storage Queue by Apache JMeter™](https://blog.pnop.co.jp/jmeter-azure-storage-queue_en/)
-->
