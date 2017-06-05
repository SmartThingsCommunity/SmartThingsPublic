# SmartSense Temp/Humidity Sensor

Local Execution on V2 Hubs

Works with: 

* [Samsung SmartSense Temp/Humidity Sensor](https://shop.smartthings.com/#!/products/smartsense-temp-humidity-sensor)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Battery** - defines device uses a battery
* **Relative Humidity Measurement** - defines device measures relative humidity
* **Refresh** - _refresh()_ command for status updates
* **Temperature Measurement** - defines device measures current temperature
* **Health Check** - indicates ability to get device health notifications
* **Sensor** - detects sensor events

## Device Health

SmartSense Temp/Humidity Sensor with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* V1, TV, HubV2 AppEngine < 1.5.1 - __121min__ checkInterval
* HubV2 AppEngine 1.5.1 - __12min__ checkIntervalr 5 min interval is confirmed

## Battery Specification

One CR2 battery is required.

## Troubleshooting

If the sensor doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried by placing the sensor closer to the hub.
Instructions related to pairing, resetting and removing the sensor from SmartThings can be found in the following link:
* [Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/203040294)