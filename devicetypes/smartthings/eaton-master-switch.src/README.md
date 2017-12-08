# Eaton Master Switch

Cloud Execution

Works with:

* [Eaton Master Switch](http://www.cooperindustries.com/content/public/en/wiring_devices/products/lighting_controls/aspire_rf_wireless/switches/aspire_rf_15a_wireless_switch_rf9501.html)

## Table of contents

* [Capabilities](#capabilities)
* [Installation](#installation)
* [Supported Functionality](#supported-functionality)
* [Deinstallation](#deinstallation)

## Capabilities

* **Actuator** - represents device has commands
* **Refresh** - is capable of refreshing current cloud state with values retrieved from the device
* **Sensor** - detects sensor events
* **Switch** - represents a device with a switch
* **Relay Switch** -  represents a device with a relay switch

## Installation

When device is not connected to z-wave network, blue LED will be blinking.

* To include this device in SmartThings Hub network, start device discovery from SmartThings app, then press the device's On/Off button one time.
* Device LED should stop blinking
* If device's LED does not stop blinking after a while, press device's button again.

Repeat last step until device successfuly connects to SmartThing network.

## Supported Functionality

* SmartThings support Eaton Master Switch switch functionality.
* SmartThings support setting Eaton Master Switch delayed off delay. To trigger delayed off feature, user needs to hold device's button while device is turned on.
* SmartThings support setting Eaton Master Switch Protection. Protection can be enabled from SmartThing application. It offers 3 levels of protection:
  a) unprotected - protection is disabled
  b) sequence - turning switch on/off requires 3 button clicks within 1 second
  c) noControl - manual switch operations are disabled 
* 
## Deinstallation
* Start device exlusion using SmartThings app.
* Press on/off button on the device one time to exclude it from SmartThings.
