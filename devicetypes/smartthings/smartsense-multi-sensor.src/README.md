# Smartsense Multi Sensor

Local Execution on V2 Hubs

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

SmartSense Multi sensor with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* V1, TV, HubV2 AppEngine < 1.5.1 - __121min__ checkInterval
* HubV2 AppEngine 1.5.1 - __12min__ checkInterval

## Battery Specification

One CR2450 (for Samsung SmartThings Multipurpose Sensor) battery / Two AAAA (for SmartSense Multi Sensor) batteries required.

## Troubleshooting

If the sensor doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried again by placing the sensor closer to the hub.
Other troubleshooting tips are listed as follows:
* [Troubleshooting: Samsung SmartThings Multipurpose Sensor is stuck on "open" or "closed"](https://support.smartthings.com/hc/en-us/articles/200955940-Troubleshooting-Samsung-SmartThings-Multipurpose-Sensor-is-stuck-on-open-or-closed-)
* [Troubleshooting: Temperature reading for the Samsung SmartThings Multipurpose Sensor is off](https://support.smartthings.com/hc/en-us/articles/200756845-Troubleshooting-Temperature-reading-for-the-Samsung-SmartThings-Multipurpose-Sensor-is-off)
* [Troubleshooting: Samsung SmartThings Multipurpose Sensor won’t pair after removing pull-tab](https://support.smartthings.com/hc/en-us/articles/204966616-Troubleshooting-Samsung-SmartThings-device-won-t-pair-after-removing-pull-tab)