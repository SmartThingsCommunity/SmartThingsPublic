# Smartsense Motion Sensor



Works with: 

* [Samsung SmartThings Motion Sensor](https://shop.smartthings.com/#!/products/samsung-smartthings-motion-sensor)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Motion Sensor** - can detect motion
* **Battery** - defines device uses a battery
* **Refresh** - _refresh()_ command for status updates
* **Health Check** - indicates ability to get device health notifications

## Device Health

A Category C2 motion sensor with maxReportTime of 1 hr.
Check-in interval is double the value of maxReportTime for Zigbee device. 
This gives the device twice the amount of time to respond before it is marked as offline.
Check-in interval = 2*60 = 120 min

## Battery Specification

One CR2477 (for Samsung SmartThings Motion Sensor) / CR123A (SmartSense Motion Sensor) 3V battery is required.

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried again by placing the sensor closer to the hub.
Instructions related to pairing, resetting and removing the different motion sensors from SmartThings can be found in the following links
for the different models:
* [SmartSense Motion Sensor (original model) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/200903280-SmartSense-Motion-Sensor-original-model-)
* [SmartSense Motion Sensor (2014 model) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/203077520-SmartSense-Motion-Sensor-2014-model-)
* [Samsung SmartThings Motion Sensor (2015 model) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205957580-Samsung-SmartThings-Motion-Sensor-2015-model-)
Other troubleshooting tips are listed as follows:
* [Troubleshooting: Samsung SmartThings Motion Sensor is stuck showing "Motion Detected" or "No Motion"](https://support.smartthings.com/hc/en-us/articles/200961130-Troubleshooting-Samsung-SmartThings-Motion-Sensor-is-stuck-showing-Motion-Detected-or-No-Motion-)
* [Troubleshooting: Samsung SmartThings Motion Sensor won’t pair after removing pull-tab](https://support.smartthings.com/hc/en-us/articles/204966616-Troubleshooting-Samsung-SmartThings-device-won-t-pair-after-removing-pull-tab)
