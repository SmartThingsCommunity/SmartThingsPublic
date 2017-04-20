# Express Controls EZMultiPli

Cloud Execution

Works with: 

* [Express Controls EZMultiPli](https://www.smartthings.com/works-with-smartthings/)

## Table of contents

* [Release Notes](#releasenotes)
* [Capabilities](#capabilities)
* [Troubleshooting](#troubleshooting)

## Release Notes

* 2017-04-10 - DrZwave (with help from Donald Kirker) - changed fingerprint to the new format, lowered the OnTime and other parameters to be "more in line with ST user expectations", get the luminance in LUX so it reports in lux all the time.
* 2016-10-06 - erocm1231 - Added "updated" method to run when configuration options are changed. Depending on model of unit, luminance is being reported as a relative percentace or as a lux value. Added the option to configure this in the handler.
* 2016-01-28 - erocm1231 - Changed the configuration method to use scaledConfiguration so that it properly formatted negative numbers. Also, added configurationGet and a configurationReport method so that config values can be verified.
* 2015-12-04 - erocm1231 - added range value to preferences as suggested by @Dela-Rick.
* 2015-11-26 - erocm1231 - Fixed null condition error when adding as a new device. 
* 2015-11-24 - erocm1231 - Added refresh command. Made a few changes to how the handler maps colors to the LEDs. Fixed the device not having its on/off status updated when colors are changed.
* 2015-11-23 - erocm1231 - Changed the look to match SmartThings v2 devices. 
* 2015-11-21 - erocm1231 - Made code much more efficient. Also made it compatible when setColor is passed a hex value. Mapping of special colors: Soft White - Default - Yellow, White - Concentrate - White, Daylight - Energize - Teal, Warm White - Relax - Yellow
* 2015-11-19 - erocm1231 - Fixed a couple incorrect colors, changed setColor to be more compatible with other apps
* 2015-11-18 - erocm1231 - Added to setColor for compatibility with Smart Lighting
* v0.1.0 - DrZWave - chose better icons, Got color LED to work - first fully functional version
* v0.0.9 - jrs - got the temp and luminance to work. Motion works. Debugging the color wheel.
* v0.0.8 - DrZWave 2/25/2015 - change the color control to be tiles since there are only 8 colors.
* v0.0.7 - jrs - 02/23/2015 - Jim Sulin

## Capabilities

* **Actuator** - represents that a Device has commands
* **Sensor** - detects sensor events
* **Motion Sensor** - can detect motion
* **Temperature Measurement** - defines device measures current temperature
* **Illuminance Measurement** - gives the illuminance reading from devices that support it
* **Switch** - can detect state (possible values: on/off)
* **Color Control** - represents that the color attributes of a device can be controlled (hue, saturation, color value
* **Configuration** - configure() command called when device is installed or device preferences updated
* **Refresh** - refresh() command for status updates

## Troubleshooting

