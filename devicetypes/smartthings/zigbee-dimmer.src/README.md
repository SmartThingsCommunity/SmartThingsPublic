# Zigbee Dimmer

Cloud Execution

Works with: 

* [OSRAM Lightify LED On/Off/Dim](https://shop.smartthings.com/#!/products/osram-led-smart-bulb-on-off-dim)
* [WeMo LED Bulb](https://support.smartthings.com/hc/en-us/articles/204259040-Belkin-WeMo-LED-Bulb-F7C033-)
* [Leviton Lumina Dimming Wall Switch](https://home.leviton.com/products/lumina-rf-decora-0-10v-wall-switch-dimmer/)
* [Aurora] (https://auroralighting.com/au/ProductDetail/AU-A1ZB120)
* [Aurora Dimmer AU-A1ZB320](https://auroralighting.com/gb/ProductDetail/AU-A1ZB320)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#troubleshooting)
* [Battery](#battery-specification)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Refresh** - _refresh()_ command for status updates
* **Switch** - can detect state (possible values: on/off)
* **Switch Level** - represents current light level, usually 0-100 in percent
* **Health Check** - indicates ability to get device health notifications
* **Light** - represents that a Device has commands on() and off()


## Device Health

ZigBee Dimmer with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* __12min__ checkInterval


## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Other troubleshooting tips are listed as follows:
* [OSRAM Lightify LED On/Off/Dim Troubleshooting:](https://support.smartthings.com/hc/en-us/articles/207191763-OSRAM-LIGHTIFY-LED-Smart-Connected-Light-A19-On-Off-Dim)
* [WeMo LED Bulb Troubleshooting:](https://support.smartthings.com/hc/en-us/articles/204259040-Belkin-WeMo-LED-Bulb-F7C033-)
