# Z-Wave Switch

Local Execution on V2 Hubs

Works with: 

* [GE Z-Wave Plug-In Smart Switch (12719)](http://products.z-wavealliance.org/products/1193)
* [GE Z-Wave In-Wall Smart Outlet (12721)](http://products.z-wavealliance.org/products/1195)
* [GE Z-Wave In-Wall Smart Switch (12722)](http://products.z-wavealliance.org/products/1196)
* [GE Z-Wave In-Wall Smart Toggle Switch (12727)](http://products.z-wavealliance.org/products/1200)


## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#Troubleshooting)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Indicator** - gives you the ability to set the indicator LED light on a Z-Wave switch
* **Switch** - can detect state (possible values: on/off)
* **Polling** - represents that poll() can be implemented for the device
* **Refresh** - _refresh()_ command for status updates
* **Sensor** - detects sensor events
* **Health Check** - indicates ability to get device health notifications

## Device Health

Z-Wave Switches (Plug-In, In-Wall(Toggle Switch, Switch, Outlet)) are polled by the hub.
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
* [General Z-Wave Dimmer/Switch Troubleshooting](https://support.smartthings.com/hc/en-us/articles/200955890-Troubleshooting-GE-in-wall-switch-or-dimmer-won-t-respond-to-commands-or-automations-Z-Wave-)
* [GE Z-Wave Plug-In Smart Switch (12719) Troubleshooting](https://support.smartthings.com/hc/en-us/articles/200903070-GE-Plug-In-Smart-Switch-GE-12719-Z-Wave)
* [GE Z-Wave In-Wall Smart Outlet (12721) Troubleshooting](https://support.smartthings.com/hc/en-us/articles/200903020-GE-In-Wall-Smart-Outlet-GE-12721-Z-Wave)
* [GE Z-Wave In-Wall Smart Switch (12722) Troubleshooting](https://support.smartthings.com/hc/en-us/articles/200902540-GE-In-Wall-Smart-Switch-GE-12722-Z-Wave)
* [GE Z-Wave In-Wall Smart Toggle Switch (12727) Troubleshooting](https://support.smartthings.com/hc/en-us/articles/207568933-GE-In-Wall-Smart-Toggle-Switch-GE-12727-Z-Wave)


