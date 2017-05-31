# Leviton Switch (ZigBee)

Cloud Execution

Works with:

* [Leviton Switch (ZigBee)](https://www.smartthings.com/works-with-smartthings/leviton/leviton-switch)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#Troubleshooting)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Refresh** - _refresh()_ command for status updates
* **Switch** - can detect state (possible values: on/off)
* **Health Check** - indicates ability to get device health notifications

## Device Health

A Zigbee Switch with reporting interval of 10 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* __22min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [Leviton Switch Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/209686003-How-to-connect-Leviton-ZigBee-devices)
