# Z-wave Dimmer Switch Generic

Cloud Execution

Works with: 

* [Leviton Plug-in Lamp Dimmer Module (DZPD3-1LW)](https://www.smartthings.com/works-with-smartthings/outlets/leviton-plug-in-lamp-dimmer-module)
* [Leviton Universal Dimmer (DZMX1-LZ)](https://www.smartthings.com/works-with-smartthings/switches-and-dimmers/leviton-universal-dimmer)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Switch Level** - it's defined to accept two parameters, the level and the rate of dimming
* **Actuator** - represents that a Device has commands
* **Health Check** - indicates ability to get device health notifications
* **Switch** - can detect state (possible values: on/off)
* **Polling** - represents that poll() can be implemented for the device
* **Refresh** - _refresh()_ command for status updates
* **Sensor** - detects sensor events

## Device Health

Leviton Plug-in Lamp Dimmer Module (DZPA1-1LW) (Z-wave) and Leviton Universal Dimmer (DZMX1-LZ) (Z-Wave) are polled by the hub.
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
* [Leviton Plug-in Lamp Dimmer Module (DZPD3-1LW) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/206171053-How-to-connect-Leviton-Z-Wave-devices)
* [Leviton Universal Dimmer (DZMX1-LZ) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/206171053-How-to-connect-Leviton-Z-Wave-devices)