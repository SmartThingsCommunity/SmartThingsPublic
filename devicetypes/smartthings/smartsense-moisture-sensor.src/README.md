# Samsung SmartThings Water Leak Sensor

Local Execution on V2 Hubs

Works with: 

* [Samsung SmartThings Water Leak Sensor](https://shop.smartthings.com/#!/products/samsung-smartthings-water-leak-sensor)
* SmartSense Moisture Sensor (2014)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Battery** - defines device uses a battery
* **Refresh** - _refresh()_ command for status updates
* **Temperature Measurement** - defines device measures current temperature
* **Water Sensor** - can detect presence of water (dry or wet)
* **Health Check** - indicates ability to get device health notifications

## Device Health

SmartSense Moisture sensor with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* V1, TV, HubV2 AppEngine < 1.5.1 - __121min__ checkInterval
* HubV2 AppEngine 1.5.1 - __12min__ checkInterval

## Battery Specification

One CR2 3V battery required.

## Troubleshooting

If the Moisture Sensor didn't connect to the Hub when you removed the pull-tab, it might be out of range. 
Bring the sensor closer to the Hub and attempt to connect it again.
Other troubleshooting tips are listed as follows:
* [Samsung SmartThings Water Leak Sensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205957630)
* [Troubleshooting: Samsung SmartThings Water Leak Sensor won’t pair after removing pull-tab](https://support.smartthings.com/hc/en-us/articles/204966616-Troubleshooting-Samsung-SmartThings-device-won-t-pair-after-removing-pull-tab)
* [SmartSense Moisture Sensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/202847044-SmartSense-Moisture-Sensor)
