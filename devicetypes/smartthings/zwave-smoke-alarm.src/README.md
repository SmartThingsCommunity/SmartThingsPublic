# Z-wave Smoke Alarm

Cloud Execution

Works with: 

* [First Alert Smoke Detector and Carbon Monoxide Alarm (ZCOMBO)](https://www.smartthings.com/works-with-smartthings/sensors/first-alert-smoke-detector-and-carbon-monoxide-alarm-zcombo)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Battery](#battery-specification)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Smoke Detector** - measure smoke and optionally carbon monoxide levels
* **Carbon Monoxide Detector** - measure carbon monoxide levels
* **Sensor** - detects sensor events
* **Battery** - defines device uses a battery
* **Health Check** - indicates ability to get device health notifications

## Device Health

First Alert Smoke Detector and Carbon Monoxide Alarm (ZCOMBO) is a Z-wave sleepy device and checks in every 1 hour.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*60 + 2)mins = 122 mins.

* __122min__ checkInterval

## Battery Specification

Two AA 1.5V batteries are required.

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [First Alert Smoke Detector and Carbon Monoxide Alarm (ZCOMBO) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/201581984-First-Alert-Smoke-Detector-and-Carbon-Monoxide-Alarm-ZCOMBO-)