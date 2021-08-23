# Z-wave motion light sensor

Cloud Execution

Works with: 

* Dome Motion Detector DMMS1
* NEO Coolcam Motion Sensor NAS-PD02ZU-T

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)
* [Troubleshooting](#troubleshooting)

## Capabilities
      
* **Motion Sensor** - can detect motion
* **Sensor** - detects sensor events
* **Battery** - defines device uses a battery
* **Health Check** - indicates ability to get device health notifications
* **Illuminance Measurement - indicates ability mesure illuminance using LUX units 

## Device Health

Dome Motion Detector DMMS1 is a Z-wave sleepy device and checks in every 12 hour.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (12*60 + 2)mins = 1442 mins.


NEO Coolcam Motion Sensor NAS-PD02ZU-T is a Z-wave sleepy device and checks in every 12 hour.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (12*60 + 2)mins = 1442 mins.

* __1442min__ checkInterval for Dome Motion Detector
* __1442min__ checkInterval for NEO Coolcam Motion Sensor

## Battery Specification

Dome Motion Detector - 1xCR123A battery is required.
NEO Coolcam Motion Sensor - 1xCR123A battery is required.
## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [Dome Motion Detector and NEO Coolcam Motion Sensor Troubleshooting Tips]
### To connect the Dome Motion Detector or NEO Coolcam Motion Sensor with the SmartThings Hub
```
Insert the batterry into Z-Wave module (provided separately).

Put the Hub in Add Device mode
While the Hub searches, quickly Triple-press Z-Wave button on located inside the globe
The process may take as long as 30s
Upon successful inclusion, The Z-Wave module LED will quickly blink 5 times  (500ms)
When the device is discovered, it will be listed at the top of the screen
Tap the device to rename it and tap Done
When finished, tap Save
Tap Ok to confirm
```
### To exclude the Dome Motion Detector or NEO Coolcam Motion Sensor
If the the Dome Motion Detector or NEO Coolcam Motion Sensor was not discovered, you may need to reset, or ?exclude,? the device before it can successfully connect with the SmartThings Hub.
```
Put the Hub in General Device Exclusion Mode
quickly Triple-press Z-Wave button on located inside the globe
The process may take as long as 30s
Upon successful exclusion, The Z-Wave module LED will quickly blink 5 times  (500ms)
After the app indicates that the device was successfully removed from SmartThings, follow the first set of instructions above to connect the Dome Motion Detector or NEO Coolcam Motion device.
```

