# Z-wave Basic Smoke Alarm

Cloud Execution

Works with: 

* FireAngel ZHT-630 Heat Detector/Alarm

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Temperature Alarm** - measure extreme heat 
* **Sensor** - detects sensor events
* **Battery** - defines device uses a battery
* **Health Check** - indicates ability to get device health notifications

## Device Health

FireAngel ZHT-630 Heat Detector/Alarm is a Z-wave sleepy device and checks in every 1 hour.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*60 + 2)mins = 122 mins.

* __122min__ checkInterval

## Battery Specification
    
FireAngel ZHT-630 Heat Detector/Alarm One CR2 battery required

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [FireAngel ZHT-630 Heat Detector/Alarm Troubleshooting Tips]
### To connect the FireAngel ZHT-630 Heat Detector/Alarm with the SmartThings Hub
```
Insert the batterry into Z-Wave module (provided separately).

Then, in the SmartThings mobile app:

Tap My Home 
Under Things, tap Add a Thing at the bottom of your Things list
The app will say Looking for devices…
While the Hub searches, Triple-press Z-Wave module button on back of Z-Wave using Pin, the LED will show a quick blink one per second
The process may take as long as 30s
Upon successful inclusion, The Z-Wave module LED will flash 3 times 
When the device is discovered, it will be listed at the top of the screen
Tap the device to rename it and tap Done
When finished, tap Save
Tap Ok to confirm
```
### To exclude the FireAngel ZHT-630 Heat Detector/Alarm
If the FireAngel ZHT-630 Heat Detector/Alarm was not discovered, you may need to reset, or “exclude,” the device before it can successfully connect with the SmartThings Hub. To do this in the SmartThings mobile app:
```
Tap the menu (Android) / More (iOS) 
Tap the Hub
Tap Z-Wave Utilities
Tap General Device Exclusion
Triple-press Z-Wave module button on back of Z-Wave module using Pin, the LED will show a quick double-blink one per second
The process may take as long as 30s
Upon successful exculsion, The Z-Wave module LED will flash 5 times
After the app indicates that the device was successfully removed from SmartThings, follow the first set of instructions above to connect the First Alert device.
```

### To remove the  FireAngel ZHT-630 Heat Detector/Alarm from SmartThings
If the FireAngel FireAngel ZHT-630 Heat Detector/Alarm is connected but not operating as normal, you may need to remove the device and reconnect it with the SmartThings Hub.
```
To remove the device via the SmartThings app:

Tap My Home 
Tap Things
Select the device
Tap the gear icon 
Tap Remove
Confirm removal
When prompted, perform the exclusion procedure to remove the device from the mobile app and from the SmartThings Hub
```