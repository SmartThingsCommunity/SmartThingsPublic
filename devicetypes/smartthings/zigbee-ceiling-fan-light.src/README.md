# ZigBee Ceiling Fan Light

Cloud Execution

Works with: 

* Samsung ITM

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)

## Capabilities

* **Actuator** - represents that a Device has commands* 
* **Configuration** - _configure()_ command called when device is installed or device preferences updated.
* **Refresh** - _refresh()_ command for status updates
* **Switch** - can detect state (possible values: on/off)
* **Switch Level** - represents current light level, usually 0-100 in percent
* **Health Check** - indicates ability to get device health notifications
* **Fan Speed** - represents current fan speed, 0 - 4(Off, Low, Mid, High, Max)

## Device Health

Zigbee Bulb with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

*__12min__ checkInterval
