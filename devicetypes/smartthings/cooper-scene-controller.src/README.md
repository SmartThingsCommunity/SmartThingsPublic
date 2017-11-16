# Cooper 5-scene keypad

Cloud Execution

Works with: 

* [Cooper 5-scene keypad](http://www.cooperindustries.com/content/public/en/wiring_devices/products/lighting_controls/aspire_rf_wireless/aspire_rf_5_button_scene_control_keypad_rfwdc_rfwc5.html)

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

Child devices support:

* **Actuator** - represents device has commands
* **Switch** - represents a device with a switch
* **Sensor** - detects sensor events

## Installation

The Cooper 5-scene keypad has blue LEDs which will all blink when the device is not included in a Z-Wave network.

* To include this device in SmartThings Hub network, start device discovery from SmartThings app, then press the device's All Off button one time.
* DO NOT press any buttons while the device LEDs are blinking sequentially. After pairing is complete the LEDs will stop blinking.
* If all blue LEDs on the device start blinking again, press All Off button again.
* Confirm addition of new device from SmartThings app.
* Initial device configuration will start. It will take a while, so Hub will light LEDs from 1 to 5 to indicate which buttons are already configured.
* After initial configuration ends, Handler will check if configuration was successful, and retry if neccessary.
* Again, It will take a while, so Hub will light LEDs from 1 to 5 to indicate progress.
* This process may fail too. To check if set up was successful, wait for all leds to be turned off, and turn every switch on (Important note: do this without turning any switch off).
* Now check status of all switches. If all switches are turned on, set up was successful.
* If some switches are turned off, please enter device settings and press save to run configuration check/retry again. After this, check if malfunctioning switches started working.

Repeat last step until all switches start working correctly.

## Supported Functionality

SmartThings will treat Cooper 5-scene keypad as 5-switch remote and will support All Off button functionality.

## Unsupported Functionality

SmartThings does not support Dimmer functionality of Cooper 5-scene keypad.

## Deinstallation
* Start device exlusion using SmartThings app.
* Press the ALL OFF button one time to exclude device from SmartThings.
* All the device's LEDs will start blinking indicating that the device is no longer in the z-wave network.

