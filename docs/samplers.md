# Samplers

- [Azure Event Hubs](#azure-event-hubs)
- [Azure Service Bus](#azure-service-bus)

## Azure Event Hubs

This sampler lets you send an AMQP request to an Azure Event Hubs.  

Currently, this sampler does not support all Azure Event Hubs features. For example, send properties, etc are not supported.  
I will prioritize the implementation of the ones that are most frequently requested by everyone.

### How to install

Download jmeter-plugins-azure-eventhubs.?.?.?.jar file from [latest release](https://github.com/pnopjp/jmeter-plugins/releases/latest) and put it into lib/ext directory of JMeter \(e.g. /usr/local/jmeter/lib/ext\), then restart JMeter.

### Parameters

|Attribute|Description|Required|
|-----|-----|-----|
|Name|Descriptive name for this sampler that is shown in the tree|No|
|Event Hubs Namespace|Azure Event Hubs namespace name to send messages to.<br />\(e.g. YOUREVENTHUBS<span></span>.servicebus.windows.net\)|Yes|
|Event Hub|Event Hub name to send messages to.|Yes|
|Partition|Partition to send message to. You can select either Not specified, Partition Key, or Partition ID.|No|
|Auth Type|Authorization type to use when sending messages to Azure Event Hubs.<br />If you select "Azure AD credential", also define the Azure AD Credential Config Element.|Yes|
|Shared Access Policy [\*1](#1-eventhubs)|Shared access policy name of the Event Hubs namespace or Event Hub.|No|
|Shared Access Key [\*1](#1-eventhubs)|Shared access keys for the shared access policy|No|
|Variable Name of credential declared in Azure AD Crednetial|The variable name of the credential declared in Azure AD Credential, specified if "Azure AD credential" is selected for "Auth Type".|No|
|Event data|List of messages to be sent in batches. Select "String" to send a UTF-8 string, "Base64 encoded binary" to send a Base64 encoded binary, or "File" to send a file as binary.|No|

<span id="1-eventhubs">\*1</span>: Set these parameters only if "Shared access signature" is selected in Auth type.

### Sample files

- [AzEventHubsSampler.jmx](../samples/AzEventHubsSampler.jmx)

### Tutorial

- [How to request to Azure Event Hubs with Apache JMeter™](https://blog.pnop.co.jp/jmeter-azure-event-hubs_en/)

## Azure Service Bus

This sampler lets you send an AMQP request to an Azure Service Bus.  

Currently, this sampler does not support all Azure Servic eBus features. For example, send properties, etc are not supported.  
I will prioritize the implementation of the ones that are most frequently requested by everyone.

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
|Auth Type [\*1](#1-servicebus)|Authorization type to use when sending messages to Azure Event Hubs.<br />If you select "Azure AD credential", also define the Azure AD Credential Config Element.|Yes|
|Shared Access Policy [\*1](#1-servicebus) [\*2](#2-servicebus)|Shared access policy name of the Event Hubs namespace or Event Hub.|No|
|Shared Access Key [\*1](#1-servicebus) [\*2](#2-servicebus)|Shared access keys for the shared access policy|No|
|Variable Name of credential declared in Azure AD Crednetial [\*1](#1-servicebus)|The variable name of the credential declared in Azure AD Credential, specified, if "Azure AD credential" is selected for "Auth Type".|No|
|Create transaction before sending messages|Create a transaction before sending a messages.|No|
|Variable name for created transaction|The name of the transaction to create, if "Create transaction before sending messages" is turned on.|No|
|Transaction state|Specify whether to commit or rollback, if "Use Defined Transaction" is selected for "Connection/Transaction".<ul><li>\[Continue transaction\]<br />No commit, and no rollback.</li><li>\[Commit transaction after sending messages\]<br />Commits the specified transaction after sending the messages.</li><li>\[Rollback transaction before sending messages\]<br />Sends the messages after the specified transaction is rolled back. (Sending messages are not included in the transaction)</li></ul>|No|
|Messages|List of messages to be sent in batches. Select "String" to send a UTF-8 string, "Base64 encoded binary" to send a Base64 encoded binary, or "File" to send a file as binary.|No|

<span id="1-servicebus">\*1</span>: If "Create New Connection" is selected for "Connection/Transaction", set these parameters.  
<span id="2-servicebus">\*2</span>: If "Shared access signature" is selected for Auth type, set these parameters.

### Sample files

- [AzServiceBusSampler.jmx](../samples/AzServiceBusSampler.jmx)

### Tutorial

- [How to request to Azure Service Bus by Apache JMeter™](https://blog.pnop.co.jp/jmeter-azure-event-hubs_en/)
