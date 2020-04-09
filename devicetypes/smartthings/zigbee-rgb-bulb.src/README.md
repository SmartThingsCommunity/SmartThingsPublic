# ZigBee RGB Bulb

Cloud Execution

Works with: 

* [OSRAM LIGHTIFY Gardenspot mini RGB](https://www.smartthings.com/works-with-smartthings/lighting-and-switches/osram-lightify-gardenspot-rgb)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Color Control** - It represents that the color attributes of a device can be controlled (hue, saturation, color value).
* **Configuration** - _configure()_ command called when device is installed or device preferences updated.
* **Polling** - It represents that a device can be polled.
* **Refresh** - _refresh()_ command for status updates
* **Switch** - can detect state (possible values: on/off)
* **Switch Level** - represents current light level, usually 0-100 in percent
* **Health Check** - indicates ability to get device health notifications
* **Light** - Indicates that the device belongs to light category.

## Device Health

Zigbee RGB Bulb with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

*__12min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Other troubleshooting tips are listed as follows:
* [OSRAM LIGHTIFY Gardenspot mini RGB Troubleshooting](https://support.smartthings.com/hc/en-us/articles/214191863)