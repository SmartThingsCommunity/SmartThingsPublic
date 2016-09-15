# Samsung SmartThings Water Leak Sensor



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

A Category C2 moisture sensor with maxReportTime of 1 hr.
Check-in interval is double the value of maxReportTime for Zigbee device. 
This gives the device twice the amount of time to respond before it is marked as offline.
Check-in interval = 2*60 = 120 min

## Battery Specification

One CR2 3V battery required.

## Troubleshooting

If the Moisture Sensor didn't connect to the Hub when you removed the pull-tab, it might be out of range. 
Bring the sensor closer to the Hub and attempt to connect it again.
Other troubleshooting tips are listed as follows:
* [Samsung SmartThings Water Leak Sensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205957630)
* [Troubleshooting: Samsung SmartThings Water Leak Sensor won’t pair after removing pull-tab](https://support.smartthings.com/hc/en-us/articles/204966616-Troubleshooting-Samsung-SmartThings-device-won-t-pair-after-removing-pull-tab)
* [SmartSense Moisture Sensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/202847044-SmartSense-Moisture-Sensor)
