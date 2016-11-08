# Zigbee Dimmer



Works with: 

* [OSRAM Lightify LED On/Off/Dim](https://shop.smartthings.com/#!/products/osram-led-smart-bulb-on-off-dim)
* [WeMo LED Bulb](https://support.smartthings.com/hc/en-us/articles/204259040-Belkin-WeMo-LED-Bulb-F7C033-)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Refresh** - _refresh()_ command for status updates
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
Other troubleshooting tips are listed as follows:
* [OSRAM Lightify LED On/Off/Dim Troubleshooting:](https://support.smartthings.com/hc/en-us/articles/207191763-OSRAM-LIGHTIFY-LED-Smart-Connected-Light-A19-On-Off-Dim)
* [WeMo LED Bulb Troubleshooting:](https://support.smartthings.com/hc/en-us/articles/204259040-Belkin-WeMo-LED-Bulb-F7C033-)
