# Ecobee (Connect) and Ecobee Device Handlers Change Log

Notable changes to this project will be documented in this file.

This project is in a pre-1.0 state. This means that its APIs and behavior are subject to breaking changes without deprecation notices. However, all due efforts will be made to warn the user. Until 1.0, version numbers will follow a [Semver][]-ish `0.y.z` format, where `y` is incremented when new features or breaking changes are introduced, and `z` is incremented for lesser changes or bug fixes.


## [0.9.17][] (2016-6-28)
### General Changes
* Added new Child SmartApp `Open Contacts` to detect open doors/windows
* Better handling of Debug Level zero (0) to reduce logging
* Minor change to apiConnected events to reduce logging/event stream

### ecobee Open Contacts SmartApp
* Brand new SmartApp

Features include:
* Change one or multiple thermostats
* Trigger based on one or multiple contact sensors
* Configurable delay timers (for trigger and reset)
* Configurable actions: Notify Only, HVAC Only or Both
* Support for Contact Book or simply SMS for notifications
* Temporarily Disable app without having to delete and recreate!


## [0.9.13][] (2016-02-28) **(Beta Release)**
### General Changes
* 11 changed files with 1,368 additions and 1,095 deletions
* Overhauled authentication handling with much better stability
* Revamped UI to give it a more familiar look that ecobee users are used to
* Removal of certain 3PP code segments and update of Copyright Headers


### SmartApp Changes
#### Ecobee (Connect) SmartApp
* Completely overhauled Authentication Token Handling using recommended sequence outlined in Ecobee API documentation. **This has greatly improved the stability of the API connection!**
* [Experimental] Adds support for one (1) and three (3) minute polling intervals
* Added ability to use external devices (such as motion sensors, temperature readings, etc) to trigger additional polling events (and also trigger the watchdog)
* Fixed issue causing the added devices to show and not configured in the Marketplace in the Mobile App
* Added sunrise()/sunset() events to watchdog handling and to determine day/night for icons
* Implemented off() and circulate() modes for the fan along with the appropriate state information to allow a proper return using resumeProgram()
* Implemented support for getting the current HVAC Mode
* Added `update()` and `refreshAuthToken()` helper functions to the Debug Dashboard

#### ecobee Routines Child SmartApp
* Cleaned up UI to better reflect how users will use the features
* Added support for triggered Routines, not just Modes
* Added support for custom Programs (in addition to the standard Home, Away and Sleep)


### Device Handler Changes
* [All] Updated with new UI Scheme using themed icons
* [Sensor] States for when the motion sensor is not supported
* [Thermostat] Added full range of Thermostat Capabilities
* [Thermostat] Added a "Last Poll" Timestamp to the UI to make it easier to determine when the most recent successful poll took place
* [Thermostat] Added a Tile to show the current HVAC System Status (On, Off, Auto, Off)
* [Thermostat] Added support for both day **and** night weather icons 
* [Thermostat] Removal of many un-needed functions (commands) that weren't part of the standard capabilities set




## [0.9.0-RC6][] (2016-02-01) 
### General Changes
* 3 changed files with 876 additions and 467 deletions (To be updated)
* Introduction of support for Child SmartApps! First SmartApp is `ecobee Routines` for using SmartThings Modes (Routines) to control the modes (and other settings) on the Ecobee Thermostats
* More hooks to be defensive against the SmartThings platform stability issues (e.g. Scheduled Events)

### SmartApp Changes
#### **NEW** ecobee Routines Child SmartApp
* Control your Ecobee Thermostats based on SmartThings Modes/Routines
* Support for multiple instances for running different options at different modes
* Support for controlling the following options per instance:
  * Multiple SmartThings Modes
  * Multiple Thermostats
  * Select Ecobee Program to run (Away, Home, Sleep or Resume Program) (NOTE: Custom programs are on the wish list for a future release)
  * Select Fan Mode (Optional)
  * Can also be run at Sunrise and/or Sunset event (Optional)
  * Toggle to temporarily disable the Handler so you don't have to delete it when you want it off temporarily

#### Ecobee (Connect)
* Complete overhaul of the Ecobee (Connect) User Interface to make it easier to manage and to allow for the introduction of the Child SmartApps
* Switched from using `atomicState` to just `state` to reduce overhead
* Using helper functions to build JSON bodies for API requests. This should spead up adding new features over time
* Added checks to make sure the customer Device Handlers are installed before proceeding with setup
* Added a `Debug Dashboard` so that various state variables and functions can be performed right from within the SmartApp. No need to go the IDE just to check things!
* Added some watchdog capabilities to improve reliability of Scheduled Events
* Updated the fan mode handling to not disrupt the existing running event (hold) or program, only changes the fan settings now
* Set several helper function to `private` so as to not pollute the exposed interfaces
* Thermostats can now also be exposed as individual Remote Sensors so that the actual temperature at the Thermostat can be used for HA

### Device Handler Changes
* [Thermostat] Faster response in User Interface (send events immediately when a change is made in app)
* [Thermostat] Defaulting back to the thermostat `multiAttributeTile` with the release of the 2.0.8 phone apps (Needs Android testing!)

## [0.8.0][] (2016-01-28)
*  4 changed files with 469 additions and 932 deletions
*  Support for `setThermostatFanMode` handling to set the fan modes
*  Fan Modes Implemented:
 *  "on", "off", "auto": full support
 *  "circulate": Partially implemented, currently just calls `auto()`
*  Added a button for changing the fan between "auto" and "on" on the Thermostat Tile
*  Continued code refactoring
*  Changed holdType labels to "Until I Change" and "Until Next Program" to better align with the Ecobee terms

## [0.7.5][] (2016-01-25)

### General Changes
* First tagged release
* Added support for different debug levels. This should make it easier to thin out the logs to see real errors instead of all the trace information
* Total of 3 changed files with 889 additions and 454 deletions

### SmartApp Changes
* Added support for Comfort Settings
* Tweeks to the handling for API connection errors in an attempt to recover a connection
* Fixes for handling an offline Remote Sensor (due to range, battery, etc)
* Refactoring of code to simplify maintenance (this will be an ongoing process for the next several releases)
* Added support for Ecobee Events to read the current state. This means we can now show status for Manual and Automatic "Holds" that are running including `Smart Home` and `Smart Away`
* Clear the waittime between polls immediately after making changes to the settings. This allows the `refresh` button to be more responsive. 

### Device Handler Changes
* [Thermostat] Added support for Comfort Settings (programs). Adds the following new commands (can be invoked by other SmartApps):
  * setThermostatProgram(program, holdType)
  * home()
  * sleep()
  * away()
* [Thermostat] Added the corresponding Tiles to support manipulating and viewing the Comfort Settings. (NOTE: UI elements are likely to change in the future once the overall development stabalizes)
* [Remote Sensor] Added state for an offline sensor. Marks the `motion` tile to indicate the failure


## [0.6.0][] (2016-01-15)
* Checkpoint from previous commits to use as baseline
* Click the release number to see all of the changes made to this branch up until this date



[Semver]: http://semver.org
[0.9.0-17]: https://github.com/StrykerSKS/SmartThingsPublic/commit/90e648d7827fcfd81036f545f8b15a269bb2c3cd#diff-56eb4c4a092c115bba0049125518e88e
[0.9.0-13]: https://github.com/StrykerSKS/SmartThingsPublic/compare/v0.9.0-RC6...v0.9.13 
[0.9.0-RC6]: https://github.com/StrykerSKS/SmartThingsPublic/compare/StrykerSKS:v0.8.0...StrykerSKS:v0.9.0-RC6
[0.8.0]: https://github.com/StrykerSKS/SmartThingsPublic/compare/StrykerSKS:v0.7.5...StrykerSKS:v0.8.0
[0.7.5]: https://github.com/StrykerSKS/SmartThingsPublic/compare/302bb77d7237132caaa5281b64d4bfbf4420f7cf...StrykerSKS:v0.7.5
[0.6.0]: https://github.com/StrykerSKS/SmartThingsPublic/compare/master...StrykerSKS:302bb77d7237132caaa5281b64d4bfbf4420f7cf

[Development]: https://github.com/StrykerSKS/SmartThingsPublic/compare/StrykerSKS:v0.8.0...HEAD
[Beta]: https://github.com/StrykerSKS/SmartThingsPublic/compare/StrykerSKS:HEAD...StrykerSKS-enhanced-ecobeedevice
[Alpha]: https://github.com/StrykerSKS/SmartThingsPublic/compare/StrykerSKS:HEAD...StrykerSKS-development
