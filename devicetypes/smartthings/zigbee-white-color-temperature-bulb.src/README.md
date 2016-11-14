# ZigBee White Color Temperature Bulb

Cloud Execution

Works with: 

* [OSRAM Lightify Tunable 60 White](http://www.osram.com/osram_com/tools-and-services/tools/lightify---smart-connected-light/lightify-for-home---what-is-light-to-you/lightify-products/lightify-classic-a60-tunable-white/index.jsp)
* [OSRAM LIGHTIFY RT5/6 Tunable White](https://www.smartthings.com/works-with-smartthings/light-bulbs/osram-lightify-rt56-tunable-white)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Color Temperature** - represents color temperature, measured in degrees Kelvin.
* **Configuration** - _configure()_ command called when device is installed or device preferences updated.
* **Refresh** - _refresh()_ command for status updates
* **Switch** - can detect state (possible values: on/off)
* **Switch Level** - represents current light level, usually 0-100 in percent
* **Health Check** - indicates ability to get device health notifications

## Device Health

Zigbee Bulb with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

*__12min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Other troubleshooting tips are listed as follows:
* [OSRAM Lightify Tunable 60 White Troubleshooting](https://support.smartthings.com/hc/en-us/articles/204576454-OSRAM-LIGHTIFY-Tunable-White-60-Bulb)
* [OSRAM LIGHTIFY RT5/6 Tunable White Troubleshooting](https://support.smartthings.com/hc/en-us/articles/214191863-How-to-connect-OSRAM-LIGHTIFY-Bulbs)