# Smartsense Moisture

Cloud Execution

Works with: 

* [FortrezZ Moisture Sensor](https://www.smartthings.com/works-with-smartthings/fortrezz/fortrezz-moisture-sensor)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Water Sensor** - can detect presence of water (dry or wet)
* **Sensor** - detects sensor events
* **Battery** - defines device uses a battery
* **Temperature Measurement** - represents capability to measure temperature
* **Health Check** - indicates ability to get device health notifications

## Device Health

Smartsense Moisture is a Z-wave sleepy device type and checks in every 4 hours.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*4*60 + 2)mins = 482 mins.

* __482min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried again by placing the sensor closer to the hub.
Instructions related to pairing, resetting and removing the different motion sensors from SmartThings can be found in the following links
for the different models:
* [FortrezZ Moisture Sensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/200930740-FortrezZ-Moisture-Sensor)
