# Z-Wave Door Window Sensor

Cloud Execution

Works with: 

* [Aeon Labs Door/Window Sensor (Gen 5)](https://www.smartthings.com/works-with-smartthings/aeon-labs/aeon-labs-doorwindow-sensor-gen-5)

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

## Device Health

Z-Wave Door Window Sensor is a Z-wave sleepy device and checks in every 4 hours.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*4*60 + 2)mins = 482 mins.

* __482min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following links
for the different models:
* [Aeon Labs Door/Window Sensor (Gen 5) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/211834163-How-to-connect-Aeon-Labs-door-window-sensors)