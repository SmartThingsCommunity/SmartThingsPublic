# Connected Cree LED Bulb



Works with: 

* [Connected Cree LED Bulb](https://support.smartthings.com/hc/en-us/articles/204258280-Cree-Connected-LED-Bulb)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Polling** - represents that poll() can be implemented for the device
* **Refresh** - _refresh()_ command for status updates
* **Switch** - can detect state (possible values: on/off)
* **Switch Level** - represents current light level, usually 0-100 in percent
* **Health Check** - indicates ability to get device health notifications

## Device Health

A Category C6 Connected Cree LED Bulb with maxReportTime of 10 min.
Check-in interval is double the value of maxReportTime for Zigbee device. 
This gives the device twice the amount of time to respond before it is marked as offline.
Check-in interval = 2*10 = 20 min

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [Cree Connected LED Bulb Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/204258280-Cree-Connected-LED-Bulb)