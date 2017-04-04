# EcoNet Vent

Cloud Execution

Works with: 

* [EcoNet Controls Z-Wave Vent](https://www.smartthings.com/works-with-smartthings/econet-controls/econet-controls-z-wave-vent)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Switch Level** - allows for the control of the level attribute of a light
* **Actuator** - represents that a Device has commands
* **Switch** - allows for the control of a switch device
* **Battery** - defines that the device has a battery
* **Refresh** - _refresh()_ command for status updates
* **Sensor** - detects sensor events
* **Polling** - allows for the polling of devices that support it
* **Configuration** - allow configuration of devices that support it
* **Health Check** - indicates ability to get device health notifications

## Device Health

EcoNet Controls Z-Wave Vent is polled by the hub.
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
* [EcoNet Controls Z-Wave Vent Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/204556420-EcoNet-EV100-Vent)