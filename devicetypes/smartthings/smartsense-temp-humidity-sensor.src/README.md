# SmartSense Temp/Humidity Sensor



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

A Category C2 SmartSense Temp/Humidity Sensor with maxReportTime of 1 hr.
Check-in interval is double the value of maxReportTime for Zigbee device. 
This gives the device twice the amount of time to respond before it is marked as offline.
Check-in interval = 2*60 = 120 min

## Battery Specification

One CR2 battery is required.

## Troubleshooting

If the sensor doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried by placing the sensor closer to the hub.
Instructions related to pairing, resetting and removing the sensor from SmartThings can be found in the following link:
* [Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/203040294)