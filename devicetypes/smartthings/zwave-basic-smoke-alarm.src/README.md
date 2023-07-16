# Z-wave Basic Smoke Alarm

Cloud Execution

Works with: 

* [First Alert Smoke Detector (ZSMOKE)](https://www.smartthings.com/products/first-alert-smoke-detector)
* FireAngel Thermoptek ZST-630 Smoke Alarm/Detector 

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Smoke Detector** - measure smoke and optionally carbon monoxide levels
* **Sensor** - detects sensor events
* **Battery** - defines device uses a battery
* **Health Check** - indicates ability to get device health notifications

## Device Health

First Alert Smoke Detector (ZSMOKE) is a Z-wave sleepy device and checks in every 1 hour.   
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*60 + 2)mins = 122 mins.

FireAngel Thermoptek ZST-630 Smoke Alarm/Detector is a Z-wave sleepy device and checks in every 4 hour.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (8*60 + 2)mins = 482 mins.

* __122min__ checkInterval for First Alert Smoke Detector 
* __482min__ checkInterval for FireAngel Thermoptek ZST-630 Smoke Alarm/Detector

## Battery Specification

First Alert Smoke Detector (ZSMOKE) Two AA 1.5V batteries are required.
FireAngel Thermoptek ZST-630 Smoke Alarm/Detector One CR2 battery required
## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [First Alert Smoke Detector (ZSMOKE) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/207150556-First-Alert-Smoke-Detector-ZSMOKE-)
* [FireAngel Thermoptek ZST-630 Smoke Alarm/Detector Troubleshooting Tips]
### To connect the FireAngel Thermoptek ZST-630 Smoke Alarm/Detector with the SmartThings Hub
```
Insert the batterry into Z-Wave module (provided separately).

Then, in the SmartThings mobile app:

Put the Hub in Add Device mode
While the Hub searches, Triple-press Z-Wave button on back of Z-Wave module using Pin, the LED will show a quick blink once per second
The process may take as long as 30s
Upon successful inclusion, The Z-Wave module LED will flash 3 times 
When the device is discovered, it will be listed at the top of the screen
Tap the device to rename it and tap Done
When finished, tap Save
Tap Ok to confirm
```
### To exclude the FireAngel Thermoptek ZST-630 Smoke Alarm/Detector
If the FireAngel Thermoptek ZST-630 Smoke Alarm/Detector was not discovered, you may need to reset, or “exclude,” the device before it can successfully connect with the SmartThings Hub. To do this in the SmartThings mobile app:
```
Put the Hub in General Device Exclusion Mode
Triple-press Z-Wave button on back of Z-Wave module using Pin, the LED will show a quick double-blink once per second
The process may take as long as 30s
Upon successful exculsion, The Z-Wave module LED will flash 5 times
After the app indicates that the device was successfully removed from SmartThings, follow the first set of instructions above to connect the First Alert device.
```
 