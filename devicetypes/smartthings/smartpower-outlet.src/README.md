# SmartPower Outlet

Local Execution on V2 Hubs

Works with: 

* [Samsung SmartPower Outlet](https://shop.smartthings.com/#!/products/smartpower-outlet)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)

## Capabilities

* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Actuator** - represents that a Device has commands
* **Switch** - can detect state (possible values: on/off)
* **Refresh** - _refresh()_ command for status updates
* **Power Meter** - detects power meter for device in either w or kw.
* **Health Check** - indicates ability to get device health notifications
* **Sensor** - detects sensor events

## Device Health

SmartPower outlet with reporting interval of 5 mins
SmartThings platform will ping the device after `checkInterval` seconds of inactivity in last attempt to reach the device before marking it `OFFLINE` 

* V1, TV, HubV2 AppEngine < 1.5.1 - __21min__ checkInterval
* HubV2 AppEngine 1.5.1 - __12min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following links
for the different models:
* [SmartPower Outlet Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/201084854-SmartPower-Outlet)
* [Samsung SmartThings Outlet Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205957620)