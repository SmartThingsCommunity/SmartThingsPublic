# Plant Link

Cloud Execution

Works with: 

* [OSO Technologies PlantLink Soil Moisture Sensor](https://www.smartthings.com/works-with-smartthings/oso-technologies/oso-technologies-plantlink-soil-moisture-sensor)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#troubleshooting)

## Capabilities

* **Relative Humidity Measurement** - allows reading the relative humidity from devices that support it
* **Sensor** - detects sensor events
* **Battery** - defines device uses a battery
* **Health Check** - indicates ability to get device health notifications

## Device Health

Plant Link sensor is a ZigBee sleepy device and checks in every 15 minutes.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*15 + 2)mins = 32 mins.

* __32min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the sensor is out of range.
Pairing needs to be tried again by placing the sensor closer to the hub.
Instructions related to pairing, resetting and removing the different motion sensors from SmartThings can be found in the following links
for the different models:
* [OSO Technologies PlantLink Soil Moisture Sensor Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/206868986-PlantLink-Soil-Moisture-Sensor)
