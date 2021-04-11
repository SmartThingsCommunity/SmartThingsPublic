# Sonoff-Tasmota 4 Channel Version
SmartThings device handler for Sonoff-Tasmota firmware (https://github.com/arendst/Sonoff-Tasmota).

## Instructions
### Add Device Handler
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My Device Handlers`
3. Click `Create New Device Handler`
4. In the `From Code` tab paste in the code from 
https://github.com/BrettSheleski/SmartThingsPublic/blob/master/devicetypes/brettsheleski/sonoff-tasmota-4ch.src/sonoff-tasmota-4ch.groovy
5. Click `Create`
6. Click `Publish` --> `For Me`

### Add Device
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My Devices`
3. Click `New Device`
4. Give it a Name, And Device Network Id
5. For Type scroll to the bottom of the dropdown and select 'Sonoff-Tasmota 4CH'
6. Click `Create`
7. In the table that displays the different properties of the newly created device, click the `edit` link next to Preferences.
8.  Enter the IP Address of your Sonoff and specify Port 80 (and username and password if applicable).
9.  Click `Save`

Now the device is added to your account and you should be able to control your device from the SmartThings app on your phone.
