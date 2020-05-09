# Tyco Door Window Sensor

Cloud Execution

Works with: 

* [Tyco Door Window Sensor](https://support.smartthings.com/hc/en-us/articles/204834100-Tyco-Door-Window-Sensor)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Battery** - defines device uses a battery
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Contact Sensor** - can detect contact (open/close)
* **Refresh** - _refresh()_ command for status updates
* **Temperature Measurement** - can measure the device temperature
* **Health Check** - indicates ability to get device health notifications

## Device Health

Tyco Door Window Sensor with reporting interval of 5 mins.
Check-in interval is double the value of maxReportTime for Zigbee device. 
This gives the device twice the amount of time to respond before it is marked as offline.

* __12min__ checkInterval

## Battery Specification

3V CR2032 battery is required.

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that either the sensor needs to be reseted or the sensor is out of range.
Reset needs to be done by inserting the battery in the sensor and then quickly pressing the adjacent black button 10 times. Pairing should be tried again now.
It may happen that sensor is out of range, then pairing needs to be tried again by placing the sensor closer to the hub.
Instructions related to pairing, resetting and removing the different motion sensors from SmartThings can be found in the following links
for the different models:
* [Tyco Door Window Sensor (MCT-340)](https://support.smartthings.com/hc/en-us/articles/204834100-Tyco-Door-Window-Sensor)
