# ZigBee CPX Smart Panel Light

Cloud Execution

Works with: 

* ABL Lithonia
* Samsung LED

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)

## Capabilities

* **Actuator** - represents that a Device has commands* 
* **Color Temperaturer** - It represents color temperature capability measured in degree Kelvin.
* **Configuration** - _configure()_ command called when device is installed or device preferences updated.
* **Health Check** - indicates ability to get device health notifications
* **Refresh** - _refresh()_ command for status updates
* **Switch** - can detect state (possible values: on/off)
* **Switch Level** - represents current light level, usually 0-100 in percent
* **Motion Sensor** - can detect motion

## Device Health

Zigbee Bulb with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

*__12min__ checkInterval
