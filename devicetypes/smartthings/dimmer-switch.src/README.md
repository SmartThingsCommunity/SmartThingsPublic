# Z-wave Dimmer Switch

Local Execution on V2 Hubs

Works with:

* [GE Z-Wave In-Wall Smart Dimmer (GE 12724)](http://products.z-wavealliance.org/products/1197)
* [GE Z-Wave In-Wall Smart Dimmer (Toggle) (GE 12729)](http://products.z-wavealliance.org/products/1201)
* [GE Z-Wave Plug-in Smart Dimmer (GE 12718)](http://products.z-wavealliance.org/products/1191)
* [GE 1,000-Watt In-Wall Smart Dimmer Switch (GE 12725)](http://products.z-wavealliance.org/products/1198)
* [GE In-Wall Smart Fan Control (GE 12730)](http://products.z-wavealliance.org/products/1202)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#Troubleshooting)

## Capabilities

* **Switch Level** - it's defined to accept two parameters, the level and the rate of dimming
* **Actuator** - represents that a Device has commands
* **Indicator** - gives you the ability to set the indicator LED light on a Z-Wave switch
* **Switch** - can detect state (possible values: on/off)
* **Polling** - represents that poll() can be implemented for the device
* **Refresh** - _refresh()_ command for status updates
* **Sensor** - detects sensor events
* **Health Check** - indicates ability to get device health notifications

## Device Health

Z-Wave Smart Dimmers (In-Wall, In-Wall(Toggle), Plug-In), 1000-watt In-Wall Smart Dimmer Switch and In-Wall Smart Fan Control are polled by the hub.
As of hubCore version 0.14.38 the hub sends up reports every 15 minutes regardless of whether the state changed.
Check-in interval = 32 mins.
Not to mention after going OFFLINE when the device is plugged back in, it might take a considerable amount of time for
the device to appear as ONLINE again. This is because if this listening device does not respond to two poll requests in a row,
it is not polled for 5 minutes by the hub. This can delay up the process of being marked ONLINE by quite some time.

* __32min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [General Z-Wave Dimmer/Switch Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/200955890-Troubleshooting-GE-in-wall-switch-or-dimmer-won-t-respond-to-commands-or-automations-Z-Wave-)
* [GE Z-Wave In-Wall Smart Dimmer (GE 12724) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/200902600-GE-In-Wall-Paddle-Dimmer-Switch-GE-12724-Z-Wave-)
* [GE Z-Wave In-Wall Smart Dimmer (Toggle) (GE 12729) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/207568463-GE-In-Wall-Smart-Toggle-Dimmer-GE-12729-Z-Wave-)
* [GE Z-Wave Plug-in Smart Dimmer (GE 12718) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/202088474-GE-Plug-In-Smart-Dimmer-GE-12718-Z-Wave-)
* [GE 1,000-Watt In-Wall Smart Dimmer Switch (GE 12725) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/200879274)
* [GE In-Wall Smart Fan Control (GE 12730) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/200879274)