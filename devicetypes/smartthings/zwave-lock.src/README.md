# Z-Wave Lock

Cloud Execution

Works with: 

* KwikSet SmartCode 910 Deadbolt Door Lock
* KwikSet SmartCode 910 Contemporary Deadbolt Door Lock
* KwikSet SmartCode 912 Lever Door Lock
* KwikSet SmartCode 914 Deadbolt Door Lock
* KwikSet SmartCode 916 Touchscreen Deadbolt Door Lock
* Schlage Camelot Touchscreen Deadbolt Door Lock
* Schlage Century Touchscreen Deadbolt Door Lock
* Schlage Connected Keypad Lever Door Lock
* Yale Touchscreen Deadbolt Door Lock
* Yale Touchscreen Lever Door Lock
* Yale Push Button Deadbolt Door Lock
* Yale Push Button Lever Door Lock
* Yale Assure Lock with Bluetooth
* Yale Keyless Connected Smart Door Lock
* Yale Assure Lock Push Button Deadbolt
* Samsung Digital Lock: SHP-DH525, SHP-DS705, SHP-DP728

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#Troubleshooting)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Battery** - defines device uses a battery
* **Lock** - allows for the control of a lock device
* **Lock Codes** - allows for the lock code control of a lock device
* **Polling** - represents that poll() can be implemented for the device
* **Refresh** - _refresh()_ command for status updates
* **Sensor** - detects sensor events
* **Health Check** - indicates ability to get device health notifications

## Device Health

Z-Wave Locks are polled by the hub.
As of hubCore version 0.14.38 the hub sends up reports every 15 minutes regardless of whether the state changed.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*15 + 2)mins = 32 mins.
Not to mention after going OFFLINE when the device is plugged back in, it might take a considerable amount of time for
the device to appear as ONLINE again. This is because if this listening device does not respond to two poll requests in a row,
it is not polled for 5 minutes by the hub. This can delay up the process of being marked ONLINE by quite some time.

* __32min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [General Z-Wave/ZigBee Yale Lock Troubleshooting](https://support.smartthings.com/hc/en-us/articles/205138400-How-to-connect-Yale-locks)
