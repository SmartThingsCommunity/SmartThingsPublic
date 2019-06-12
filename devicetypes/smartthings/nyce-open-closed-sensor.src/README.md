# Nyce Door/Window Sensor (Open/Close Sensor)

Cloud Execution

Works with:

* [NYCE Door/Window Sensor NCZ-3011](https://support.smartthings.com/hc/en-us/articles/204576764-NYCE-Door-Window-Sensor)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Contact Sensor** - can detect contact (with possible values - open/closed)
* **Battery** - defines device uses a battery
* **Refresh** - _refresh()_ command for status updates
* **Health Check** - indicates ability to get device health notifications

## Device Health

Nyce Door/Window sensor with reporting interval of 5 min.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 
 
* __12min__ checkInterval


## Battery Specification

One 3V CR2032 battery required.

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried again by placing the sensor closer to the hub.
Instructions related to pairing, resetting and removing the sensor from SmartThings can be found in the following link:
* [Nyce Door/Window Sensor](https://support.smartthings.com/hc/en-us/articles/204576764-NYCE-Door-Window-Sensor)
