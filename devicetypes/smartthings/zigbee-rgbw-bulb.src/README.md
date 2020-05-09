# OSRAM LIGHTIFY LED RGBW Bulb

Cloud Execution

Works with: 

* [OSRAM LIGHTIFY LED Smart Connected Light A19 RGBW](https://support.smartthings.com/hc/en-us/articles/207728173-OSRAM-LIGHTIFY-LED-Smart-Connected-Light-A19-RGBW)
* [OSRAM LIGHTIFY Flex RGBW strips](https://www.smartthings.com/works-with-smartthings/lighting-and-switches/osram-lightify-flex-rgbw-strips)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Actuator** - It represents that a device has commands.
* **Color Control** - It represents that the color attributes of a device can be controlled (hue, saturation, color value).
* **Color Temperature** - It represents color temperature capability measured in degree Kelvin.
* **Polling** - It represents that a device can be polled.
* **Switch** - can detect state (possible values: on/off)
* **Switch Level** - can detect current light level (0-100 in percent)
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Refresh** - _refresh()_ command for status updates
* **Health Check** - indicates ability to get device health notifications


## Device Health

OSRAM LIGHTIFY LED RGBW Bulb with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* __12min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
It may also happen that you need to reset the device.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [OSRAM LIGHTIFY LED Smart Connected Light A19 RGBW Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/207728173-OSRAM-LIGHTIFY-LED-Smart-Connected-Light-A19-RGBW)
* [OSRAM LIGHTIFY Flex RGBW strips Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/214191863)