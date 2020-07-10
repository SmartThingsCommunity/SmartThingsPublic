# Eaton Anyplace Switch

Cloud Execution

Works with:

* [Eaton Anyplace Switch](http://www.cooperindustries.com/content/public/en/wiring_devices/products/lighting_controls/aspire_rf_wireless/anyplace.html)

## Table of contents

* [Capabilities](#capabilities)
* [Installation](#installation)
* [Supported Functionality](#supported-functionality)
* [Deinstallation](#deinstallation)

## Capabilities

* **Actuator** - represents device has commands
* **Switch** - represents a device with a switch
* **Sensor** - detects sensor events

## Installation

* To include this device in SmartThings Hub network, start device discovery from SmartThings app, then press the device's On/Off button one time.
* Device LED will start blinking
* When device LED stops blinking, device is already added to the network and goes to sleep.
* User is required to push dimmer buttons a few times, to wake device up long enough for hub to properly retrieve device information.
* If new "Eaton Anyplace Switch" device appears in SmartThings app, confirm addition of new device.

## Supported Functionality

* SmartThings support Eaton Anyplace Switch switch functionality.
This device type handler assumes primary device's function is handled by SmartThings using automations and smartapps.

Eaton Anyplace Switch can be associated directly to other z-wave device as described in manufacturer's manual.
If device associated to Eaton Anyplace Switch was not part of SmartThings network, it should now appear on device list.
Eaton Anyplace Switch queries SmartThings Hub to check its state. In order to always maintain synch with other associated devices, automation that updates Eaton Anyplace Switch state based on associated devices' switch state is required.

## Deinstallation
* Start device exlusion using SmartThings app.
* Press the switch on/off button one time to exclude device from SmartThings.

