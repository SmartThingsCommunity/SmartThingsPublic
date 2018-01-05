# Zigbee Lock

Cloud Execution

Works with: 

* KwikSet SmartCode 910 Deadbolt Door Lock
* KwikSet SmartCode 910 Contemporary Deadbolt Door Lock
* KwikSet SmartCode 912 Lever Door Lock
* KwikSet SmartCode 914 Deadbolt Door Lock
* KwikSet SmartCode 916 Touchscreen Deadbolt Door Lock
* Yale Touch Screen Lever Lock
* Yale Push Button Deadbolt Lock
* Yale Touch Screen Deadbolt Lock
* Yale Push Button Lever Lock

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Lock** - allows for the control of a lock device
* **Refresh** - _refresh()_ command for status updates
* **Sensor** - detects sensor events
* **Battery** - defines device uses a battery
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Health Check** - indicates ability to get device health notifications

## Device Health

Yale Push Button Deadbolt (YRD210-HA) is a Zigbee device and checks in every 1 hour.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*60 + 2)mins = 122 mins.

 * __122min__ checkInterval

## Battery Specification

Four AA 1.5V batteries are required.

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the sensor from SmartThings can be found in the following link:
* [Yale Locks Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/205138400)