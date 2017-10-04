# Arrival Sensor HA (2016+ Model)

Cloud Execution

Works with:

* [Samsung SmartThings Arrival Sensor](https://support.smartthings.com/hc/en-us/articles/212417083-Samsung-SmartThings-Arrival-Sensor)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery)
* [Troubleshooting](#troubleshooting)


## Capabilities

* **Tone** - beep command to allow an audible tone
* **Actuator** - device has commands
* **Presence Sensor** - device tells presence with enum - {present, not present}
* **Sensor** - device has attributes
* **Battery** - defines device uses a battery
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Health Check** - indicates ability to get device health notifications


## Device Health

Arrival Sensor ZigBee is an untracked device. Sends broadcast of battery level every 20 seconds.
Disconnects when Hub goes OFFLINE.


## Battery

Uses 1 CR2032 Battery

* [Changing the Battery](https://support.smartthings.com/hc/en-us/articles/200907400-How-to-change-the-battery-in-the-SmartSense-Presence-Sensor-and-Samsung-SmartThings-Arrival-Sensor)


## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the arrival sensor is out of range.
Pairing needs to be tried again by placing the sensor closer to the hub.

* [Samsung SmartThings Arrival Sensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205382134-Samsung-SmartThings-Arrival-Sensor-2015-model-)

If the arrival sensor doesn't update its status, here are a few things you can try to debug.

* [Troubleshooting: Samsung SmartThings Arrival Sensor won't update its status](https://support.smartthings.com/hc/en-us/articles/200846514-Troubleshooting-Samsung-SmartThings-Arrival-Sensor-won-t-update-its-status)