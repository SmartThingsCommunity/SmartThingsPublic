# Smartsense Open/Closed Sensor

Local Execution on V2 Hubs

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

SmartSense Open Closed sensor with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* V1, TV, HubV2 AppEngine < 1.5.1 - __121min__ checkInterval
* HubV2 AppEngine 1.5.1 - __12min__ checkInterval

## Battery Specification

One CR2 3V battery required.

## Troubleshooting

If the sensor doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried again by placing the sensor closer to the hub.
Instructions related to pairing, resetting and removing the sensor from SmartThings can be found in the following link:
* [SmartSense Open/Closed Sensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/202836844-SmartSense-Open-Closed-Sensor)