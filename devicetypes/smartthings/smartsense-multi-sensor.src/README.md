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

One CR2450 (for Samsung SmartThings Multipurpose Sensor) battery / Two AAAA (for SmartSense Multi Sensor) batteries required.

## Troubleshooting

If the sensor doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried again by placing the sensor closer to the hub.
Other troubleshooting tips are listed as follows:
* [Troubleshooting: Samsung SmartThings Multipurpose Sensor is stuck on "open" or "closed"](https://support.smartthings.com/hc/en-us/articles/200955940-Troubleshooting-Samsung-SmartThings-Multipurpose-Sensor-is-stuck-on-open-or-closed-)
* [Troubleshooting: Temperature reading for the Samsung SmartThings Multipurpose Sensor is off](https://support.smartthings.com/hc/en-us/articles/200756845-Troubleshooting-Temperature-reading-for-the-Samsung-SmartThings-Multipurpose-Sensor-is-off)
* [Troubleshooting: Samsung SmartThings Multipurpose Sensor won’t pair after removing pull-tab](https://support.smartthings.com/hc/en-us/articles/204966616-Troubleshooting-Samsung-SmartThings-device-won-t-pair-after-removing-pull-tab)