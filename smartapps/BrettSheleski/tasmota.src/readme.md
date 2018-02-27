# Tasmota SmartApp
SmartThings SmartApp for use with the SmartThings [Tasmota Device Handler](https://github.com/BrettSheleski/SmartThingsPublic/tree/master/devicetypes/BrettSheleski/tasmota.src)

The Device Handler is used for updating devices running the [Sonoff-Tasmota](https://github.com/arendst/Sonoff-Tasmota) firmware.

See Tasmota Device Handler (https://github.com/BrettSheleski/SmartThingsPublic/tree/master/devicetypes/BrettSheleski/tasmota.src) for more details.

## Installation
### Adding SmartApp to SmartThings
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My SmartApps`
3. Click `New SmartApp`
4. In the `From Code` tab paste in the code from https://github.com/BrettSheleski/SmartThingsPublic/blob/master/smartapps/BrettSheleski/tasmota.src/tasmota.groovy
5. Click `Create`
6. Click `Publish` --> `For Me`

### Installing SmartApp
Open the SmartThings app:
1.  Select `Automation` tab (on bottom)
2.  Select `SmartApps` tab (on top)
3.  Scroll to the bottom and select `Add a SmartApp`
4.  Scroll to the bottom and select `My Apps`
5.  Find the `Tasmota` SmartApp
6.  In the Devices input, select all devices using the `Tasmota` device handler.

### SmartApp Usage
The SmartApp exposes an HTTP endpoint which can be used to update the Tasmota devices.

TODO: Finish documenting this when finished.

## Example Usages
### Node-Red
A Node-Red flow can be created which will subscribe to the appropriate MQTT topic(s) for the device(s) and call make an HTTP request to the SmartThings SmartApp endpoint.

TODO: Document here.
