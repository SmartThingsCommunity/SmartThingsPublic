# Z-Wave Door Temp Sensor

Local Execution

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#Troubleshooting)

## Capabilities

* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Health Check** - indicates ability to get device health notifications
* **Sensor** - detects sensor events
* **Battery** - defines that the device has a battery
* **Contact Sensor** - allows reading the value of a contact sensor device
* **Temperature Measurement** -- allows temperature reporting

## Device Health

Z-Wave Door/Temp Sensor is a Z-Wave sleepy device and checks in every 4 hours.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*4*60 + 2)mins = 482 mins.

* __482min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.