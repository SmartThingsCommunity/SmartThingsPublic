# Smartsense Multi Sensor



Works with: 

* [Samsung SmartThings Multi Sensor](https://shop.smartthings.com/#!/products/smartsense-multi)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Three Axis** - monitors the state of a single axis
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Battery** - defines device uses a battery
* **Sensor** - detects sensor events
* **Contact Sensor** - can detect contact (possible values: open,closed)
* **Acceleration Sensor** - allows for acceleration detection.
* **Refresh** - _refresh()_ command for status updates
* **Temperature Measurement** - defines device measures current temperature
* **Health Check** - indicates ability to get device health notifications

## Device Health

A Category C2 multi sensor with maxReportTime of 1 hr.
Check-in interval is double the value of maxReportTime for Zigbee device. 
This gives the device twice the amount of time to respond before it is marked as offline.
Check-in interval = 2*60 = 120 min

## Battery Specification

Two AAAA battery required.