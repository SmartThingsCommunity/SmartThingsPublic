# Zen Thermostat

Cloud Execution

Works with: 

* [Zen Thermostat](https://www.smartthings.com/works-with-smartthings/zen/zen-thermostat)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Thermostat** - allows for the control of a thermostat device
* **Temperature Measurement** - get the temperature from a Device that reports current temperature
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Refresh** - _refresh()_ command for status updates
* **Sensor** - it represents that a Device has attributes
* **Health Check** - indicates ability to get device health notifications

## Device Health

Zen Thermostat with reporting interval of 5 mins.
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* __12min__ checkInterval


## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Other troubleshooting tips are listed as follows:
* [Zen Thermostat Troubleshooting:](https://support.smartthings.com/hc/en-us/articles/204356564-Zen-Thermostat)
