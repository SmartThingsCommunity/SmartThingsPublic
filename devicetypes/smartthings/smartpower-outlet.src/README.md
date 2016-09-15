# Samsung SmartThings Outlet



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

A Category C1 smart power outlet with maxReportTime of 10 min.
Check-in interval is double the value of maxReportTime for Zigbee device. 
This gives the device twice the amount of time to respond before it is marked as offline.
Check-in interval = 2*10 = 20 min

## Troubleshooting

If the outlet did not pair to the Hub when it was first connected, it migh be out of range. 
Bring the outlet closer to the Hub and attempt to connect it again.
Other troubleshooting tips are listed as follows:
* [Samsung SmartThings Outlet Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205957620)
* [SmartPower Outlet Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/201084854-SmartPower-Outlet)
