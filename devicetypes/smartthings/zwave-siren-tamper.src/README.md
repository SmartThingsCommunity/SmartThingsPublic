# Yale External Siren

Cloud Execution

Works with: 

* [Yale External Siren] (https://products.z-wavealliance.org/products/2039)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Alarm** - allows for interacting with devices that serve as alarms
* **Battery** - It defines that the device has a battery
* **Refresh** - _refresh()_ command for status updates
* **Switch** - can detect state (possible values: on/off)
* **Health Check** - indicates ability to get device health notifications

## Device Health

Yale external siren is a beamable device. Under battery only operation (without 9V power supply), 
the siren will report the battery level (100%,75%, 50%, 25%, 10%, 0%) and low battery condition(when power drops below 10%).
Under 9V power supply siren will report battery level (100%,75%, 50%, 25%, 10%, 0%) only if the battery switch is ON, in other case - a siren will report: 0xFF.

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [Yale External Siren manufacturer product manual](https://products.z-wavealliance.org/ProductManual/File?folder=&filename=Manuals/2039/YSL_External_Siren_SR-BX-ZW_1C.pdf)