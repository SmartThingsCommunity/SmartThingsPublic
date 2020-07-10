# Express Controls EZMultiPli

Works with: 

* [Express Controls EZMultiPli](https://www.smartthings.com/works-with-smartthings/)

## Table of contents

* [Release Notes](#release-notes)
* [Capabilities](#capabilities)
* [Troubleshooting](#troubleshooting)

## Release Notes

* **2017-04-19** - _dkirker_ - Update default config values in config value range check functions, use lux if lum option is null, fix NullPointerException on initial pairing when color data has not been set (and set the default color data!)
* **2017-04-10** - _DrZwave_ (with help from Donald Kirker) - changed fingerprint to the new format, lowered the OnTime and other parameters to be "more in line with ST user expectations", get the luminance in LUX so it reports in lux all the time.
* **2016-10-06** - _erocm1231_ - Added "updated" method to run when configuration options are changed. Depending on model of unit, luminance is being reported as a relative percentace or as a lux value. Added the option to configure this in the handler.
* **2016-01-28** - _erocm1231_ - Changed the configuration method to use scaledConfiguration so that it properly formatted negative numbers. Also, added configurationGet and a configurationReport method so that config values can be verified.
* **2015-12-04** - _erocm1231_ - added range value to preferences as suggested by @Dela-Rick.
* **2015-11-26** - _erocm1231_ - Fixed null condition error when adding as a new device. 
* **2015-11-24** - _erocm1231_ - Added refresh command. Made a few changes to how the handler maps colors to the LEDs. Fixed the device not having its on/off status updated when colors are changed.
* **2015-11-23** - _erocm1231_ - Changed the look to match SmartThings v2 devices. 
* **2015-11-21** - _erocm1231_ - Made code much more efficient. Also made it compatible when setColor is passed a hex value. Mapping of special colors: Soft White - Default - Yellow, White - Concentrate - White, Daylight - Energize - Teal, Warm White - Relax - Yellow
* **2015-11-19** - _erocm1231_ - Fixed a couple incorrect colors, changed setColor to be more compatible with other apps
* **2015-11-18** - _erocm1231_ - Added to setColor for compatibility with Smart Lighting
* **v0.1.0** - _DrZWave_ - chose better icons, Got color LED to work - first fully functional version
* **v0.0.9** - _jrs_ - got the temp and luminance to work. Motion works. Debugging the color wheel.
* **v0.0.8** - _DrZWave_ 2/25/2015 - change the color control to be tiles since there are only 8 colors.
* **v0.0.7** - _jrs_ - 02/23/2015 - Jim Sulin

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

