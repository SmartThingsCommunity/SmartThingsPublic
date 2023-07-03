# Samsung SmartThings Outlet

Local Execution on V2 Hubs

Works with: 

* [Samsung SmartThings  Outlet](https://shop.smartthings.com/#!/products/samsung-smartthings-outlet)
* [SmartPower Outlet (2014)](https://shop.smartthings.com/#!/products/smartpower-outlet)

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

If the outlet did not pair to the Hub when it was first connected, it migh be out of range. 
Bring the outlet closer to the Hub and attempt to connect it again.
Other troubleshooting tips are listed as follows:
* [Samsung SmartThings Outlet Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205957620)
* [SmartPower Outlet Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/201084854-SmartPower-Outlet)
