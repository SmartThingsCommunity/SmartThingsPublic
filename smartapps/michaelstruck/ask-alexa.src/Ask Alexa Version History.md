 *  Version 1.0.0 - Initial release
 *  Version 1.0.0a - Same day release. Bugs fixed: nulls in the device label now trapped and ensure LIST_OF_PARAMS and LIST_OF_REPORTS is always created
 *  Version 1.0.0b - Remove punctuation from the device, mode and routine names. Fixed bug where numbers were removed in modes and routine names 
 *  Version 1.0.1c - Added presense sensors; added up/down/lower/increase/decrease as commands for various devices
 *  Version 1.0.2b - Added motion sensors and a new function, "events" to list to the last events for a device; code optimization, bugs removed
 *  Version 1.1.0a - Changed voice reports to macros, added toggle commands to switches, bug fixes and code optimization
 *  Version 1.1.1d - Added limits to temperature and speaker values; additional macros device types added
 *  Version 1.1.2 (6/5/16) Updated averages of temp/humidity with proper math function
 *  Version 2.0.0b (6/10/16) Code consolidated from Parent/Child to a single code base. Added CoRE Trigger and CoRE support. Many fixes
 *  Version 2.0.1 (6/12/16) Fixed issue with listing CoRE macros; fixed syntax issues and improved acknowledgment message in Group Macros, more CoRE output behind-the-scenes
 *  Version 2.0.2a (6/17/16) Added %delay% macro for custom acknowledgment for pre/post text areas, dimmer/group fixes and added lunar phases (thanks again to Barry Burke), 2nd level acknowledgments in Alexa
 *  Version 2.0.3a (6/21/16) Filter of power meters in reports. Added Weather Advisories.
 *  Version 2.0.4 (7/8/16) Code fixes/optimizations, added additional options for secondary responses
 *  Version 2.0.5 (7/9/16) Fix for null String issues
 *  Version 2.0.6 (7/14/16) Syntax fixes, additional filters on voice reports, expanded secondary responses, CoRE Macro fix
 *  Version 2.0.7b (7/23/16) Small code/syntax/interface fixes, code optimization. Allows you to place an entry into the Notification Event Log when a macro is run. Fixed CoRE Macro activation logic
 *  Version 2.0.8c (8/2/16) Restructured code to allow future personality features; fixed thermostat heating/cooling logic; added minium value command to theromstat, added tide information; added window shade control
