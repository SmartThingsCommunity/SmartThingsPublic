# SmartSense Temp/Humidity Sensor



Works with: 

* [SmartSense Temp/Humidity Sensor (2014)](https://shop.smartthings.com/#!/products/smartsense-temp-humidity-sensor)

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

If the sensor didn't connect to the Hub when you removed the pull-tab, it might be out of range. 
Bring the sensor closer to the Hub and attempt to connect it again.
Additional troubleshooting tips are listed as follows:
* [Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/203040294)
