# Smartsense Open/Closed Sensor



Works with: 

* [Samsung SmartThings Open/Closed Sensor](https://shop.smartthings.com/#!/packs/smartsense-open-closed-sensor/)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Battery** - defines device uses a battery
* **Contact Sensor** - can detect contact (possible values: open,closed)
* **Refresh** - _refresh()_ command for status updates
* **Temperature Measurement** - defines device measures current temperature
* **Health Check** - indicates ability to get device health notifications
* **Sensor** - detects sensor events

## Device Health

A Category C2 open/closed sensor with maxReportTime of 1 hr.
Check-in interval is double the value of maxReportTime for Zigbee device. 
This gives the device twice the amount of time to respond before it is marked as offline.
Check-in interval = 2*60 = 120 min

## Battery Specification

One CR2 3V battery required.

## Troubleshooting

If the sensor doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried again by placing the sensor closer to the hub.
Instructions related to pairing, resetting and removing the sensor from SmartThings can be found in the following link:
* [SmartSense Open/Closed Sensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/202836844-SmartSense-Open-Closed-Sensor)