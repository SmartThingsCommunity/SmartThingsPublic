# Tasmota SmartApp
SmartThings SmartApp for controlling devices running the Sonoff-Tasmota firmware (see https://github.com/arendst/Sonoff-Tasmota)

## Supported Modules
I do not physically have all the modules that the Sonoff-Tasmota firmware supports.  I was able to implement use for some modules listed below.
1.  Sonoff Basic
2.  Sonoff 4CH / Sonoff 4CH Pro
3.  Sonoff RF Bridge

(Please contribute to help create support for all modules supported by the Sonoff-Tasmota firmware)

## Installation
This SmartApp works by creating several child devices that the user would interact with.

### Install Necessary Device Handler(s) (Required)
#### Install the Tasmota Device Handler (Required)
This device handler is necessary for all module types.  This device handler will make an HTTP call to the Tasmota device to determine the module type.  Depending on the module type defined, it will create child devices.
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My Device Handlers`
3. Click `Create New Device Handler`
4. In the `From Code` tab paste in the code from https://github.com/BrettSheleski/SmartThingsPublic/blob/master/devicetypes/BrettSheleski/tasmota.src/tasmota.groovy
5. Click `Create`
6. Click `Publish` --> `For Me`

### Tasmota-Power Device Handler
For Sonoff-Basic, Sonoff-4CH, and Sonoff Dual devices this is required
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My Device Handlers`
3. Click `Create New Device Handler`
4. In the `From Code` tab paste in the code from https://github.com/BrettSheleski/SmartThingsPublic/blob/master/devicetypes/BrettSheleski/tasmota.src/tasmota-power.groovy
5. Click `Create`
6. Click `Publish` --> `For Me`

### Tasmota-RF-Bridge-Button Device Handler
For Sonoff-Basic, Sonoff-4CH, and Sonoff Dual devices this is required
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My Device Handlers`
3. Click `Create New Device Handler`
4. In the `From Code` tab paste in the code from https://github.com/BrettSheleski/SmartThingsPublic/blob/master/devicetypes/BrettSheleski/tasmota.src/tasmota-rf-bridge-button.groovy
5. Click `Create`
6. Click `Publish` --> `For Me`

## Install SmartApp (Required)
This is the brains of everything.
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My SmartApps`
3. Click `New SmartApp`
4. In the `From Code` tab paste in the code from https://github.com/BrettSheleski/SmartThingsPublic/blob/master/devicetypes/BrettSheleski/tasmota.src/tasmota-power.groovy
5. Click `Create`
6. Click `Publish` --> `For Me`

## Usage
Install the SmartApp, Tasmota Device Handler, and any othe necessary Device Handlers (see above).

Then in the SmartThings mobile App go to 
1.  Automation Tab (bottom tab)
2.  SmartApps (top tab)
3.  Add a SmartApp (bottom of list)
4.  My Apps (bottom of list)
5.  Tasmota
6.  Set the IP Address
7.  (optional) set the Username and Password

Once done you may notice that there is at least 1 new device:
* Tasmota-Master device.  
This is a device that may spawn other devices that the user would use.  It is not intended to be used directly by the user, but is what is used by its child devices to send commands and deal with the responses.
* Tasmota-Power device(s).  
These device(s) get spawned by Sonoff-Basic, Sonoff 4CH and Sonoff Dual devices.  These are the actual Switch/Momentary devices the user would normally interact with.

## Contribute
As I don't have all the module types supported by the Sonoff-Tasmota firmware I currently do not have implementation for all module types.  Please create any other child device(s) for the module.

See https://github.com/BrettSheleski/SmartThingsPublic/blob/master/devicetypes/BrettSheleski/tasmota.src/tasmota-base.groovy for a skeleton of a device handler to start with.

## Future
I also plan for the 'Master' device to also check the GPIO configuration and spawn additional child devices depending.