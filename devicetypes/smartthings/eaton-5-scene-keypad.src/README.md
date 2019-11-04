# Eaton 5-scene keypad

Cloud Execution

Works with: 

* [Eaton 5-scene keypad](http://www.cooperindustries.com/content/public/en/wiring_devices/products/lighting_controls/aspire_rf_wireless/aspire_rf_5_button_scene_control_keypad_rfwdc_rfwc5.html)

## Table of contents

* [Capabilities](#capabilities)
* [Installation](#installation)
* [Supported Functionality](#supported-functionality)
* [Unsupported Functionality](#unsupported-functionality)
* [Deinstallation](#deinstallation)

## Capabilities

5 Controller supports:

* **Actuator** - represents device has commands
* **Refresh** - is capable of refreshing current cloud state with values retrieved from the device
* **Sensor** - detects sensor events
* **Health Check** - check if device is available or unavailable


Child devices support:

* **Actuator** - represents device has commands
* **Switch** - represents a device with a switch
* **Sensor** - detects sensor events

## Installation

The Eaton 5-scene keypad has blue LEDs which will all blink when the device is not included in a Z-Wave network.

* To include this device in SmartThings Hub network, start device discovery from SmartThings app, then press the device's All Off button one time.
* DO NOT press any buttons while the device LEDs are blinking sequentially. After pairing is complete the LEDs will stop blinking.
* If all blue LEDs on the device start blinking again, press All Off button again.
* Confirm addition of new device from SmartThings app.
* Initial device configuration will start. It will take about a minute, so Hub will light LEDs from 5 to 1 to indicate progress.
* After initial configuration ends, Handler will check twice if configuration was successful, and retry if neccessary. One check will take about a minute. Hub will light LEDs from 5 to 1 to indicate progress of each of those configuration checks.
* This process may fail too. To check if set up was successful, wait for all leds to be turned off, and turn every switch on (Important note: do this without turning any switch off).
* Now check status of all switches in mobile application. If all switches are turned on, set up was successful.
* If any switches is still turned off, please exclude Eaton 5-scene keypad from hub's z-wave network and try again.

## Supported Functionality

SmartThings will treat Eaton 5-scene keypad as 5-switch remote.

## Unsupported Functionality

SmartThings does not support Dimmer and All Off functionality of Eaton 5-scene keypad. Using All Off feature will most likely cause device to be out of synch with it's cloud state.

## Deinstallation
* Start device exlusion using SmartThings app.
* Press the ALL OFF button one time to exclude device from SmartThings.
* All the device's LEDs will start blinking indicating that the device is no longer in the z-wave network.

