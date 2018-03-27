# Aeon Multisensor

Cloud Execution

Works with: 

* [Aeotec MultiSensor (DSB05-ZWUS)](https://www.smartthings.com/products/aeotec-multisensor-5)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Motion Sensor** - can detect motion
* **Temperature Measurement** - defines device measures current temperature
* **Relative Humidity Measurement** - allow reading the relative humidity from devices that support it
* **Illuminance Measurement** - gives the illuminance reading from devices that support it
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Sensor** - detects sensor events
* **Battery** - defines device uses a battery
* **Health Check** - indicates ability to get device health notifications


## Device Health

Aeon Labs MultiSensor is polled by the hub.
Aeon MultiSensor reports in once every hour.

* __122min__ checkInterval

## Battery Specification

Four AAA batteries are required.

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [Aeon MultiSensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/206157226-How-to-connect-Aeon-Labs-MultiSensors)