# Aeon Siren

Cloud Execution

Works with: 

* [Aeon Labs Siren (Gen 5)](https://www.smartthings.com/works-with-smartthings/aeon-labs/aeon-labs-siren-gen-5)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Alarm** - allows for interacting with devices that serve as alarms
* **Switch** - can detect state (possible values: on/off)
* **Health Check** - indicates ability to get device health notifications

## Device Health

Aeon Labs Siren (Gen 5) is polled by the hub.
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
* [Aeon Labs Siren (Gen 5) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/204555240-Aeon-Labs-Siren-Gen-5-)