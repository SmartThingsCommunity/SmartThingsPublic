# GE Plug-In/In-Wall Smart Dimmer (ZigBee)



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

## Device Health

A Zigbee dimmer with maxReportTime of 5 mins.
Check-in interval is double the value of maxReportTime.
This gives the device twice the amount of time to respond before it is marked as offline.
Enrolls with default periodic reporting until newer 5 min interval is confirmed
It then enrolls the device with updated checkInterval i.e. 12 mins

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [GE Z-Wave In-Wall Smart Dimmer (GE 45857) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/204988564-GE-In-Wall-Smart-Dimmer-45857GE-ZigBee-)
* [GE Zigbee Plug-in Smart Dimmer (GE 45852) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205239280-GE-Plug-In-Smart-Dimmer-45852GE-ZigBee-)
