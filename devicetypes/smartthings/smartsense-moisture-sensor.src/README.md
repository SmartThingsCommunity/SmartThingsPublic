# Smartsense Moisture Sensor



Works with: 

* [Samsung SmartThings Moisture Sensor](https://shop.smartthings.com/#!/products/samsung-smartthings-water-leak-sensor)

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