# Smartsense Motion Sensor

Local Execution on V2 Hubs

Works with: 

* [Samsung SmartThings Motion Sensor](https://shop.smartthings.com/#!/products/samsung-smartthings-motion-sensor)
* [Bosch Motion Detector](https://us.boschsecurity.com/en/products/intrusionalarmsystems/detectorsandaccessories/motionpir/radionpirzbwirelessmotion/radionpirzbwirelessmotion_products_56178)

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

SmartSense Motion sensor with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* V1, TV, HubV2 AppEngine < 1.5.1 - __121min__ checkInterval
* HubV2 AppEngine 1.5.1 - __12min__ checkInterval


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
