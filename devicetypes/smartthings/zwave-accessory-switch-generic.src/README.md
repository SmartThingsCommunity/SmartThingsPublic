# Z-wave Switch Generic

Cloud Execution

Works with: 

* [Eaton Accessory Switch](http://www.cooperindustries.com/content/public/en/wiring_devices/products/lighting_controls/aspire_rf_wireless/switches/aspire_rf_accessory_switch_rf9517.html)
* [Eaton Accessory Dimmer](http://www.cooperindustries.com/content/public/en/wiring_devices/products/lighting_controls/aspire_rf_wireless/dimmers/aspire_rf_accessory_w_leds_rf9542_z_.html)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Health Check** - indicates ability to get device health notifications
* **Switch** - can detect state (possible values: on/off)
* **Polling** - represents that poll() can be implemented for the device
* **Refresh** - _refresh()_ command for status updates
* **Sensor** - detects sensor events

## Device Health

Eaton Accessory Dimmer and Eaton Accessory Switch are polled by the hub.
As of hubCore version 0.14.38 the hub sends up reports every 15 minutes regardless of whether the state changed.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*15 + 2)mins = 32 mins.
Not to mention after going OFFLINE when the device is plugged back in, it might take a considerable amount of time for
the device to appear as ONLINE again. This is because if this listening device does not respond to two poll requests in a row,
it is not polled for 5 minutes by the hub. This can delay up the process of being marked ONLINE by quite some time.

* __32min__ checkInterval
