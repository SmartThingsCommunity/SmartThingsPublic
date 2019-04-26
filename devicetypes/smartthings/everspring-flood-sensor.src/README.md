# Everspring Flood Sensor

Cloud Execution

Works with: 

* [Everspring Water Detector](https://www.smartthings.com/works-with-smartthings/sensors/everspring-water-detector)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Water Sensor** - can detect presence of water (dry or wet)
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Sensor** - detects sensor events
* **Battery** - defines device uses a battery
* **Health Check** - indicates ability to get device health notifications

## Device Health

Everspring Water Detector is a Z-wave sleepy device and wakes up every 4 hours.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*4*60 + 2)mins = 482 mins.

* __482min__ checkInterval

## Battery Specification

Three AA 1.5V batteries are required.

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [Everspring Water Detector Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/202088304-Everspring-Water-Detector)