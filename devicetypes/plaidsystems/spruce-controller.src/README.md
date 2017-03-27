# Spruce Controller

Cloud Execution

Works with: 

* [Spruce Irrigation Controller](https://www.smartthings.com/works-with-smartthings/spruce/spruce-irrigation-controller)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Switch** - can detect state (possible values: on/off)
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Refresh** - _refresh()_ command for status updates
* **Actuator** - represents that a Device has commands
* **Valve** - allows for the control of a valve device
* **Health Check** - indicates ability to get device health notifications

## Device Health

Spruce Controller with reporting interval of 10 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* __22min__ checkInterval


## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Other troubleshooting tips are listed as follows:
* [Spruce Irrigation Controller Troubleshooting:](https://support.smartthings.com/hc/en-us/articles/208053773-Spruce-Irrigation-Controller-Sensor)
