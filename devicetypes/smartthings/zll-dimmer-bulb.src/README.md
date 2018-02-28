# Zigbee Dimmer Bulb


Works with: 

* [IKEA of Sweden: TRADFRI bulb GU10 W op/ch 400lm] 
* [IKEA of Sweden: TRADFRI bulb E12 W op/ch 400lm]
* [OSRAM]
* [Philips]

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Configuration** - command called when device is installed or device preferences updated
* **Polling** - command for status updates
* **Refresh** - command for status updates
* **Switch** - can detect state (possible values: on/off)
* **Switch Level** - represents current light level, usually 0-100 in percent
* **Health Check** - indicates ability to get device health notifications

## Device Health

ZigBee Dimmer with reporting interval of 12 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* __12min__ checkInterval


## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, pairing needs to be tried again (sometimes few times). 

