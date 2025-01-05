# Microsoft Azure plugin for Apache JMeter™

## What is it?

This library contains Microsoft Azure plugin for Apache JMeter™.

- Functions  
    Useful functions for testing to Azure.
- Samplers for Azure services
    - Azure Event Hubs
    - Azure Service Bus Queue
    - Azure Storage Queue
- Stub function for Azure Load Testing

It has been optimized for use with the [Load Tester Powered by Apache JMeter™](https://azuremarketplace.microsoft.com/en-us/marketplace/apps/pnop.jmeter-multiple-remote-servers).

## Requirements

- Apache JMeter™ 5.4.1 or later
    - Some plugins also work with Apache JMeter 5.3.
- JRE 17 or later
    - If you wish to use this release with Java 16 or earlier, please use this release.  
      <https://github.com/pnopjp/jmeter-plugins/releases/tag/ASB0.3.2>

## How to install

Download \*.jar file from [latest release](https://github.com/pnopjp/jmeter-plugins/releases/latest) and put it into lib/ext directory of JMeter \(ex. /usr/local/jmeter/lib/ext\), then restart JMeter.

## How to build

### Requirements

- JDK 17 or later
    - If building with JDK 16 or earlier, branch from tag ASB0.3.2.  
      <https://github.com/pnopjp/jmeter-plugins/tree/ASB0.3.2>
- [maven](https://maven.apache.org/)

### Steps

1. Execute the fllowing command in the root directory of the project.  

    ```bash
    mvn clean package
    ```

1. A jar file is created in the 'target' directory for each plugins.  
    i.e. plugins/protocol/eventhubs/target/jmeter-plugins-azure-eventhubs-?.?.?.jar

1. To use the built plugins, copy these jar files to the lib/ext directory of JMeter.  
    However, jar files whose file name begins with 'original-' must not be copied.

## Documents

- [Functions](docs/functions.md)
- [Samplers](docs/samplers.md)
- [Config Elements](docs/configurations.md)
- [Stub function for Azure Load Testing](docs/azure-load-testing-stub.md)
