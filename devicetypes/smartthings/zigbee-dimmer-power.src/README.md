# GE Plug-In/In-Wall Smart Dimmer (ZigBee)

Cloud Execution

Works with:

* [GE In-Wall Smart Dimmer (ZigBee)](https://shop.smartthings.com/#!/products/ge-in-wall-smart-dimmer-switch)
* [GE Plug-In Smart Dimmer (ZigBee)](https://www.smartthings.com/works-with-smartthings/ge/ge-plug-in-smart-dimmer-zigbee)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#Troubleshooting)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Refresh** - _refresh()_ command for status updates
* **Power Meter** - ability to check the power meter(energy consumption) of device
* **Sensor** - represents the device sensor capability
* **Switch** - can detect state (possible values: on/off)
* **Switch Level** - represents current light level, usually 0-100 in percent
* **Health Check** - indicates ability to get device health notifications
* **Light** - indicates that the device belongs to Light category.

## Device Health

A Zigbee Power Dimmer with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* __12min__ checkInterval


## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [GE Z-Wave In-Wall Smart Dimmer (GE 45857) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/204988564-GE-In-Wall-Smart-Dimmer-45857GE-ZigBee-)
* [GE Zigbee Plug-in Smart Dimmer (GE 45852) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205239280-GE-Plug-In-Smart-Dimmer-45852GE-ZigBee-)
