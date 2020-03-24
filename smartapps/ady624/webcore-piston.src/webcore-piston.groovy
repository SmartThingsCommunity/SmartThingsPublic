/*
 *  webCoRE - Community's own Rule Engine - Web Edition
 *
 *  Copyright 2016 Adrian Caramaliu <ady624("at" sign goes here)gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Version history
*/
public static String version() { return "v0.3.110.20191009" }
/*
 *	10/09/2019 >>> v0.3.110.20191009 - BETA M3 - Load devices into dashboard in multiple batches when necessary, switch to FontAwesome Kit to always use latest version
 *	08/22/2019 >>> v0.3.10f.20190822 - BETA M3 - Custom headers on web requests by @Bloodtick_Jones (write as JSON in Authorization header field), capabilities split into three pages to fix device selection errors
 *	06/28/2019 >>> v0.3.10e.20190628 - BETA M3 - Reinstated dirty fix for dashboard timeouts after reports of increased error rates, NaN device status is back
 *	06/27/2019 >>> v0.3.10d.20190627 - BETA M3 - Reverted attempted fix for dashboard timeouts, fixes NaN device status on piston editing, dashboard tweaks for Hubitat by E_Sch
 *	05/22/2019 >>> v0.3.10c.20190522 - BETA M3 - Changed the device selection page in main app to fix timeout issues in Asia-Pacific
 *	05/14/2019 >>> v0.3.10b.20190514 - BETA M3 - Changed the device selection page to fix timeout issues in Asia-Pacific
 *	02/23/2019 >>> v0.3.10a.20190223 - BETA M3 - Added $twcweather to replace discontinued $weather, added new :twc-[iconCode]: weather icon set, fixed content type for local HTTP requests
 *	12/07/2018 >>> v0.3.109.20181207 - BETA M3 - Dirty fix for dashboard timeouts: seems like ST has a lot of trouble reading the list of devices/commands/attributes/values these days, so giving up on reading values makes this much faster - temporarily?!
 *	09/06/2018 >>> v0.3.108.20180906 - BETA M3 - Restore pistons from backup file, hide "(unknown)" SHM status, fixed string to date across DST thanks @bangali, null routines, integer trailing zero cast, saving large pistons and disappearing variables on mobile
 *	08/06/2018 >>> v0.3.107.20180806 - BETA M3 - Font Awesome 5 icons, expanding textareas to fix expression scrolling, boolean date and datetime global variable editor fixes
 *	07/31/2018 >>> v0.3.106.20180731 - BETA M3 - Contact Book removal support
 *	06/28/2018 >>> v0.3.105.20180628 - BETA M3 - Reorder variables, collapse fuel streams, custom web request body, json and urlEncode functions
 *	03/23/2018 >>> v0.3.104.20180323 - BETA M3 - Fixed unexpected dashboard logouts, updating image urls in tiles, 12 am/pm in time(), unary negation following another operator
 *	02/24/2018 >>> v0.3.000.20180224 - BETA M3 - Dashboard redesign by @acd37, collapsible sidebar, fix "was" conditions on decimal attributes and log failures due to duration threshold
 *	01/16/2018 >>> v0.2.102.20180116 - BETA M2 - Fixed IE 11 script error, display of offset expression evaluation, blank device lists on piston restore, avoid error and log a warning when ST sunrise/sunset is blank
 *	12/27/2017 >>> v0.2.101.20171227 - BETA M2 - Fixed 172.x.x.x web requests thanks to @tbam, fixed array subscripting with 0.0 decimal value as in a for loop using $index
 *	12/11/2017 >>> v0.2.100.20171211 - BETA M2 - Replaced the scheduler-based timeout recovery handling to ease up on resource usage
 *	11/29/2017 >>> v0.2.0ff.20171129 - BETA M2 - Fixed missing conditions and triggers for several device attributes, new comparison group for binary files
 *	11/09/2017 >>> v0.2.0fe.20171109 - BETA M2 - Fixed on events subscription for global and superglobal variables
 *	11/05/2017 >>> v0.2.0fd.20171105 - BETA M2 - Further DST fixes
 *	11/05/2017 >>> v0.2.0fc.20171105 - BETA M2 - DST fixes
 *	10/26/2017 >>> v0.2.0fb.20171026 - BETA M2 - Partial support for super global variables - works within same location - no inter-location comms yet
 *	10/11/2017 >>> v0.2.0fa.20171010 - BETA M2 - Various bug fixes and improvements - fixed the mid() and random() functions
 *	10/07/2017 >>> v0.2.0f9.20171007 - BETA M2 - Added previous location attribute support and methods to calculate distance between places, people, fixed locations...
 *	10/06/2017 >>> v0.2.0f8.20171006 - BETA M2 - Added support for Android geofence filtering depending on horizontal accuracy
 *	10/04/2017 >>> v0.2.0f7.20171004 - BETA M2 - Added speed and bearing support
 *	10/04/2017 >>> v0.2.0f6.20171004 - BETA M2 - Bug fixes for geofencing
 *	10/04/2017 >>> v0.2.0f5.20171003 - BETA M2 - Bug fixes for geofencing
 *	10/04/2017 >>> v0.2.0f4.20171003 - BETA M2 - Bug fixes for geofencing
 *	10/03/2017 >>> v0.2.0f3.20171003 - BETA M2 - Bug fixes for geofencing
 *	10/03/2017 >>> v0.2.0f2.20171003 - BETA M2 - Updated iOS app to add timestamps
 *	10/01/2017 >>> v0.2.0f1.20171001 - BETA M2 - Added debugging options
 *	09/30/2017 >>> v0.2.0f0.20170930 - BETA M2 - Added last update info for both geofences and location updates
 *	09/30/2017 >>> v0.2.0ef.20170930 - BETA M2 - Minor fixes for Android
 *	09/29/2017 >>> v0.2.0ed.20170929 - BETA M2 - Added support for Android presence
 *	09/27/2017 >>> v0.2.0ec.20170927 - BETA M2 - Fixed a problem where the 'was' comparison would fail when the event had no device
 *	09/25/2017 >>> v0.2.0eb.20170925 - BETA M2 - Added Sleep Sensor capability to the webCoRE Presence Sensor, thanks to @Cozdabuch and @bangali
 *	09/24/2017 >>> v0.2.0ea.20170924 - BETA M2 - Fixed a problem where $nfl.schedule.thisWeek would only return one game, it now returns all games for the week. Same for lastWeek and nextWeek.
 *	09/21/2017 >>> v0.2.0e9.20170921 - BETA M2 - Added support for the webCoRE Presence Sensor
 *	09/18/2017 >>> v0.2.0e8.20170918 - BETA M2 - Alpha testing for presence
 *	09/06/2017 >>> v0.2.0e7.20170906 - BETA M2 - Added support for the $nfl composite variable, fixed some bugs with boolean comparisons of null
 *	08/30/2017 >>> v0.2.0e6.20170830 - BETA M2 - Minor fixes regarding some isNumber() errors and errors with static variables using non-defined variables
 *	08/12/2017 >>> v0.2.0e5.20170812 - BETA M2 - Allowing global variables create device subscriptions (due to demand)
 *	08/11/2017 >>> v0.2.0e4.20170811 - BETA M2 - Support for quick set of local variables
 *	08/10/2017 >>> v0.2.0e3.20170810 - BETA M2 - Improved support for threeAxis and added support for axisX, axisY, and axisZ as decimal values
 *	08/08/2017 >>> v0.2.0e2.20170808 - BETA M2 - Fixed a bug with time restrictions for conditions/triggers (not timers) where day of week, hour, etc. would be compared against UTC making edge comparisons fail (Sun 11pm would look like a Mon 3am for EST, therefore not on a Sunday anymore)
 *	07/28/2017 >>> v0.2.0e1.20170728 - BETA M2 - Added the rainbowValue function to provide dynamic colors in a range
 *	07/26/2017 >>> v0.2.0e0.20170726 - BETA M2 - Added support for rangeValue() which allows quick inline conversion of decimal ranges to values coresponding to them (i.e. translate level or temperature into a color)
 *	07/25/2017 >>> v0.2.0df.20170725 - BETA M2 - Minor bug fixes and improvements - decimal display is now using a dynamic decimal place count
 *	07/24/2017 >>> v0.2.0de.20170724 - BETA M2 - Minor fixes regarding lists and is_equal_to can now compare strings as well as numbers
 *	07/22/2017 >>> v0.2.0dd.20170722 - BETA M2 - Added support for the Authentication header in HTTP(S) requests, support for image in local network requests (does not work yet)
 *	07/22/2017 >>> v0.2.0dc.20170722 - BETA M2 - Progress towards bi-directional emails and support for storing media (paid feature)
 *	07/17/2017 >>> v0.2.0db.20170717 - BETA M2 - Added two more functions abs(number) and hslToHex(hue(0-360°), saturation(0-100%), level(0-100%)), fixed a bug with LIFX when not passing a period
 *	07/16/2017 >>> v0.2.0da.20170716 - BETA M2 - Fixed a bug where clearing tiles higher than 8 would not work
 *	07/14/2017 >>> v0.2.0d9.20170714 - BETA M2 - Adds support for waiting on piston executions as long as the caller and callee are in the same webCoRE instance
 *	07/13/2017 >>> v0.2.0d8.20170713 - BETA M2 - Fixes for orientation triggers, variable lists referenced with $index, a weird condition where negative numbers would be inverted to absolute values, extended tiles to 16
 *	07/13/2017 >>> v0.2.0d7.20170713 - BETA M2 - Unknown feature added to tiles
 *	07/13/2017 >>> v0.2.0d6.20170713 - BETA M2 - Updated tiles to allow for multiple tiles and footers - this update breaks all previous tiles, sorry
 *	07/12/2017 >>> v0.2.0d5.20170712 - BETA M2 - Bug fixes and fixed a bug that where piston tile state would not be preserved during a piston save
 *	07/12/2017 >>> v0.2.0d4.20170712 - BETA M2 - Added categories support and piston tile support
 *	07/11/2017 >>> v0.2.0d3.20170711 - BETA M2 - Lots of bug fixes and improvements
 *	07/10/2017 >>> v0.2.0d2.20170710 - BETA M2 - Added long integer support to variables and fixed a bug where time comparisons would apply a previously set offset to custom times
 *	07/08/2017 >>> v0.2.0d1.20170708 - BETA M2 - Added Piston recovery procedures to the main app
 *	07/08/2017 >>> v0.2.0d0.20170708 - BETA M2 - Fixed a bug allowing the script to continue outside of timers, added Followed By support - basic tests performed
 *	07/06/2017 >>> v0.2.0cf.20170706 - BETA M2 - Fix for parsing string date and times, implemented local http request response support - local web requests will wait for a response for up to 20 seconds - JSON response, if any, is available via $response
 *	06/29/2017 >>> v0.2.0ce.20170629 - BETA M2 - Fix for broken time scheduling and device variables
 *	06/29/2017 >>> v0.2.0cd.20170629 - BETA M2 - [DO NOT UPDATE UNLESS REQUESTED TO] - Adds typed list support
 *	06/29/2017 >>> v0.2.0cc.20170629 - BETA M2 - Fixes to date, datetime, and time - datetime(string) was returning a 0, fixed it
 *	06/26/2017 >>> v0.2.0cb.20170626 - BETA M2 - Minor bug fixes (including a fix with json data arrays), and added string functions trim, trimLeft/ltrim, and trimRight/rtrim
 *	06/23/2017 >>> v0.2.0ca.20170623 - BETA M2 - Minor bug and fixes, UI support for followed by - SmartApp does not yet implement it
 *	06/22/2017 >>> v0.2.0c9.20170622 - BETA M2 - Added orientation support (not fully tested)
 *	06/22/2017 >>> v0.2.0c8.20170622 - BETA M2 - Improved support for JSON parsing, including support for named properties $json[element] - element can be an integer index, a variable name, or a string (no quotes), fixed a bug with Wait for time
 *	06/21/2017 >>> v0.2.0c7.20170621 - BETA M2 - A bug fix for boolean and dynamic types - thoroughly inspect their values rather than rely on the data type
 *	06/20/2017 >>> v0.2.0c6.20170620 - BETA M2 - Bug fix for timers - last time refactoring affected timers (timezone offset miscalculations)
 *	06/20/2017 >>> v0.2.0c5.20170620 - BETA M2 - Refactored date and time to be more user friendly and consistent to their data type. Added formatDateTime - see https://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html for more details
 *	06/19/2017 >>> v0.2.0c4.20170619 - BETA M2 - Fixed a bug with LIFX scenes, added more functions: weekDayName, monthName, arrayItem
 *	06/18/2017 >>> v0.2.0c3.20170618 - BETA M2 - Added more LIFX methods like set, toggle, breath, pulse
 *	06/16/2017 >>> v0.2.0c2.20170616 - BETA M2 - Added support for lock codes, physical interaction
 *	06/16/2017 >>> v0.2.0c1.20170616 - BETA M2 - Added support for the emulated $status device attribute, cancel all pending tasks, allow pre-scheduled tasks to execute during restrictions
 *	06/14/2017 >>> v0.2.0c0.20170614 - BETA M2 - Added support for $weather and external execution of pistons
 *	06/14/2017 >>> v0.2.0bf.20170614 - BETA M2 - Some fixes (typo found by @DThompson10), added support for JSON arrays, as well as Parse JSON data task
 *	06/13/2017 >>> v0.2.0be.20170613 - BETA M2 - 0be happy - capture/restore is here
 *	06/12/2017 >>> v0.2.0bd.20170612 - BETA M2 - More bug fixes, work started on capture/restore, DO NOT USE them yet
 *	06/11/2017 >>> v0.2.0bc.20170611 - BETA M2 - More bug fixes
 *	06/09/2017 >>> v0.2.0bb.20170609 - BETA M2 - Added support for the webCoRE Connector - an easy way for developers to integrate with webCoRE
 *	06/09/2017 >>> v0.2.0ba.20170609 - BETA M2 - More bug fixes
 *	06/08/2017 >>> v0.2.0b9.20170608 - BETA M2 - Added location mode, SHM mode and hub info to the dashboard
 *	06/07/2017 >>> v0.2.0b8.20170607 - BETA M2 - Movin' on up
 *	06/03/2017 >>> v0.1.0b7.20170603 - BETA M1 - Even more bug fixes - fixed issues with cancel on piston state change, rescheduling timers when ST decides to run early
 *	06/02/2017 >>> v0.1.0b6.20170602 - BETA M1 - More bug fixes
 *	05/31/2017 >>> v0.1.0b5.20170531 - BETA M1 - Bug fixes
 *	05/31/2017 >>> v0.1.0b4.20170531 - BETA M1 - Implemented $response and the special $response.<dynamic> variables to read response data from HTTP requests
 *	05/30/2017 >>> v0.1.0b3.20170530 - BETA M1 - Various speed improvements - MAY BREAK THINGS
 *	05/30/2017 >>> v0.1.0b2.20170530 - BETA M1 - Various fixes, added IFTTT query string params support in $args
 *	05/24/2017 >>> v0.1.0b1.20170524 - BETA M1 - Fixes regarding trigger initialization and a situation where time triggers may cancel tasks that should not be cancelled
 *	05/23/2017 >>> v0.1.0b0.20170523 - BETA M1 - Minor fixes and improvements to command optimizations
 *	05/22/2017 >>> v0.1.0af.20170522 - BETA M1 - Minor fixes (stays away from trigger, contacts not found, etc.), implemented Command Optimizations (turned on by default) and Flash
 *	05/22/2017 >>> v0.1.0ae.20170522 - BETA M1 - Minor fix for very small decimal numbers
 *	05/19/2017 >>> v0.1.0ad.20170519 - BETA M1 - Various bug fixes, including broken while loops with a preceeding exit statement (exit and break statements conflicted with async runs)
 *	05/18/2017 >>> v0.1.0ac.20170518 - BETA M1 - Preparing the grounds for advanced engine blocks
 *	05/17/2017 >>> v0.1.0ab.20170517 - BETA M1 - Fixed a bug affecting some users, regarding the new LIFX integration
 *	05/17/2017 >>> v0.1.0aa.20170517 - BETA M1 - Added egress LIFX integration
 *	05/17/2017 >>> v0.1.0a9.20170517 - BETA M1 - Added egress IFTTT integration
 *	05/16/2017 >>> v0.1.0a8.20170516 - BETA M1 - Improved emoji support
 *	05/15/2017 >>> v0.1.0a7.20170515 - BETA M1 - Added a way to test pistons from the UI - Fixed a bug in UI values where decimal values were converted to integers - those values need to be re-edited to be fixed
 *	05/12/2017 >>> v0.1.0a6.20170512 - BETA M1 - Pistons can now (again) access devices stored in global variables
 *	05/11/2017 >>> v0.1.0a5.20170511 - BETA M1 - Fixed a bug with time scheduling offsets
 *	05/09/2017 >>> v0.1.0a4.20170509 - BETA M1 - Many structural changes to fix issues like startup-spin-up-time for instances having a lot of devices, as well as wrong name displayed in the device's Recent activity tab. New helper app added, needs to be installed/published. Pause/Resume of all active pistons is required.
 *	05/09/2017 >>> v0.1.0a3.20170509 - BETA M1 - DO NOT INSTALL THIS UNLESS ASKED TO - IT WILL BREAK YOUR ENVIRONMENT - IF YOU DID INSTALL IT, DO NOT GO BACK TO A PREVIOUS VERSION
 *	05/07/2017 >>> v0.1.0a2.20170507 - BETA M1 - Added the random() expression function.
 *	05/06/2017 >>> v0.1.0a1.20170506 - BETA M1 - Kill switch was a killer. Killed it.
 *	05/05/2017 >>> v0.1.0a0.20170505 - BETA M1 - Happy Cinco de Mayo
 *	05/04/2017 >>> v0.1.09f.20170504 - BETA M1 - Various improvements, added more expression operators, replaced localStorage with localforage, improvements on parent app memory usage
 *	05/03/2017 >>> v0.1.09e.20170503 - BETA M1 - Added the formatDuration function, added volume to playText, playTextAndResume, and playTextAndRestore
 *	05/03/2017 >>> v0.1.09d.20170503 - BETA M1 - Fixed a problem where async blocks inside async blocks were not working correctly.
 *	05/03/2017 >>> v0.1.09c.20170503 - BETA M1 - Fixes for race conditions where a second almost simultaneous event would miss cache updates from the first event, also improvements on timeout recovery
 *	05/02/2017 >>> v0.1.09b.20170502 - BETA M1 - Fixes for async elements as well as setColor hue inconsistencies
 *	05/01/2017 >>> v0.1.09a.20170501 - BETA M1 - Some visual UI fixes, added ternary operator support in expressions ( condition ? trueValue : falseValue ) - even with Groovy-style support for ( object ?: falseValue)
 *	05/01/2017 >>> v0.1.099.20170501 - BETA M1 - Lots of fixes and improvements - expressions now accept more logical operators like !, !!, ==, !=, <, >, <=, >= and some new math operators like \ (integer division) and % (modulo)
 *	04/30/2017 >>> v0.1.098.20170430 - BETA M1 - Minor bug fixes
 *	04/29/2017 >>> v0.1.097.20170429 - BETA M1 - First Beta Milestone 1!
 *	04/29/2017 >>> v0.0.096.20170429 - ALPHA - Various bug fixes, added options to disable certain statements, as per @eibyer's original idea and @RobinWinbourne's annoying persistance :)
 *	04/29/2017 >>> v0.0.095.20170429 - ALPHA - Fully implemented the on event statements
 *	04/28/2017 >>> v0.0.094.20170428 - ALPHA - Fixed a bug preventing timers from scheduling properly. Added the on statement and the do statement
 *	04/28/2017 >>> v0.0.093.20170428 - ALPHA - Fixed bugs (piston state issues, time condition schedules ignored offsets). Implemented more virtual commands (the fade suite)
 *	04/27/2017 >>> v0.0.092.20170427 - ALPHA - Added time trigger happens daily at...
 *	04/27/2017 >>> v0.0.091.20170427 - ALPHA - Various improvements and fixes
 *	04/26/2017 >>> v0.0.090.20170426 - ALPHA - Minor fixes for variables and the eq() function
 *	04/26/2017 >>> v0.0.08f.20170426 - ALPHA - Implemented $args and the special $args.<dynamic> variables to read arguments from events. Bonus: ability to parse JSON data to read subitem by using $args.item.subitem (no array support yet)
 *	04/26/2017 >>> v0.0.08e.20170426 - ALPHA - Implemented Send notification to contacts
 *	04/26/2017 >>> v0.0.08d.20170426 - ALPHA - Timed triggers should now play nice with multiple devices (any/all)
 *	04/25/2017 >>> v0.0.08c.20170425 - ALPHA - Various fixes and improvements and implemented custom commands with parameters
 *	04/24/2017 >>> v0.0.08b.20170424 - ALPHA - Fixed a bug preventing subscription to IFTTT events
 *	04/24/2017 >>> v0.0.08a.20170424 - ALPHA - Implemented Routine/AskAlexa/EchoSistant/IFTTT integrations - arguments (where available) are not processed yet - not tested
 *	04/24/2017 >>> v0.0.089.20170424 - ALPHA - Added variables in conditions and matching/non-matching device variable output
 *	04/23/2017 >>> v0.0.088.20170423 - ALPHA - Time condition offsets
 *	04/23/2017 >>> v0.0.087.20170423 - ALPHA - Timed triggers (stay/stays) implemented - need additional work to get them to play nicely with "Any of devices stays..." - this never worked in CoRE, but proved to might-have-been-helpful
 *	04/23/2017 >>> v0.0.086.20170423 - ALPHA - Subscriptions to @global variables
 *	04/22/2017 >>> v0.0.085.20170422 - ALPHA - Fixed a bug with virtual device options
 *	04/22/2017 >>> v0.0.084.20170422 - ALPHA - NFL integration complete LOL (not really, implemented global variables though)
 *	04/21/2017 >>> v0.0.083.20170421 - ALPHA - Fixed a bug introduced during device-typed variable refactoring, $currentEventDevice was not properly stored as a List of device Ids
 *	04/21/2017 >>> v0.0.082.20170421 - ALPHA - Fixed a pseudo-bug where older pistons (created before some parameters were added) are missing some operands and that causes errors during evaluations
 *	04/21/2017 >>> v0.0.081.20170421 - ALPHA - Fixed a bug preventing a for-each to work with device-typed variables
 *	04/21/2017 >>> v0.0.080.20170421 - ALPHA - Fixed a newly introduced bug where function parameters were parsed as strings, also fixed functions time, date, and datetime's timezone
 *	04/21/2017 >>> v0.0.07f.20170421 - ALPHA - Fixed an inconsistency in setting device variable (array) - this was in the UI and may require resetting the variables
 *	04/21/2017 >>> v0.0.07e.20170421 - ALPHA - Fixed a bug with local variables introduced in 07d
 *	04/21/2017 >>> v0.0.07d.20170421 - ALPHA - Lots of improvements for device variables
 *	04/20/2017 >>> v0.0.07c.20170420 - ALPHA - Timed conditions are finally working (was* and changed/not changed), basic tests performed
 *	04/19/2017 >>> v0.0.07b.20170419 - ALPHA - First attempt to get 'was' conditions up and running
 *	04/19/2017 >>> v0.0.07a.20170419 - ALPHA - Minor bug fixes, triggers inside timers no longer subscribe to events (the timer is a trigger itself) - triggers should not normally be used inside timers
 *	04/19/2017 >>> v0.0.079.20170419 - ALPHA - Time condition restrictions are now working, added date and date&time conditions, offsets still missing
 *	04/18/2017 >>> v0.0.078.20170418 - ALPHA - Time conditions now subscribe for time events - added restrictions to UI dialog, but not yet implemented
 *	04/18/2017 >>> v0.0.077.20170418 - ALPHA - Implemented time conditions - no date or datetime yet, also, no subscriptions for time events yet
 *	04/18/2017 >>> v0.0.076.20170418 - ALPHA - Implemented task mode restrictions and added setColor using HSL
 *	04/17/2017 >>> v0.0.075.20170417 - ALPHA - Fixed a problem with $sunrise and $sunset pointing to the wrong date
 *	04/17/2017 >>> v0.0.074.20170417 - ALPHA - Implemented HTTP requests, importing response data not working yet, need to figure out a way to specify what data goes into which variables
 *	04/17/2017 >>> v0.0.073.20170417 - ALPHA - isBetween fix - use three params, not two, thanks to @c1arkbar
 *	04/16/2017 >>> v0.0.072.20170416 - ALPHA - Quick fix for isBetween
 *	04/16/2017 >>> v0.0.071.20170416 - ALPHA - Added the ability to execute routines
 *	04/16/2017 >>> v0.0.070.20170416 - ALPHA - Added support for multiple-choice comparisons (any of), added more improvements like the ability to disable event subscriptions (follow up pistons)
 *	04/15/2017 >>> v0.0.06f.20170415 - ALPHA - Fix for wait for date&time
 *	04/15/2017 >>> v0.0.06e.20170415 - ALPHA - Attempt to fix a race condition where device value would change before we even executed - using event's value instead
 *	04/15/2017 >>> v0.0.06d.20170415 - ALPHA - Various fixes and improvements, added the ability to execute pistons in the same location (arguments not working yet)
 *	04/15/2017 >>> v0.0.06c.20170415 - ALPHA - Fixed a bug with daily timers and day of week restrictions
 *	04/14/2017 >>> v0.0.06b.20170414 - ALPHA - Added more functions: date(value), time(value), if(condition, valueIfTrue, valueIfFalse), not(value), isEmpty(value), addSeconds(dateTime, seconds), addMinutes(dateTime, minutes), addHours(dateTime, hours), addDays(dateTime, days), addWeeks(dateTime, weeks)
 *	04/14/2017 >>> v0.0.06a.20170414 - ALPHA - Fixed a bug where multiple timers would cancel each other's actions out, implemented (not extensively tested yet) the TCP and TEP
 *	04/13/2017 >>> v0.0.069.20170413 - ALPHA - Various bug fixes and improvements
 *	04/12/2017 >>> v0.0.068.20170412 - ALPHA - Fixed a bug with colors from presets
 *	04/12/2017 >>> v0.0.067.20170412 - ALPHA - Fixed a bug introduced in 066 and implemented setColor
 *	04/12/2017 >>> v0.0.066.20170412 - ALPHA - Fixed hourly timers and implemented setInfraredLevel, setHue, setSaturation, setColorTemperature
 *	04/11/2017 >>> v0.0.065.20170411 - ALPHA - Fix for long waits being converted to scientific notation, causing the scheduler to misunderstand them and wait 1ms instead
 *	04/11/2017 >>> v0.0.064.20170411 - ALPHA - Fix for timer restrictions error
 *	04/11/2017 >>> v0.0.063.20170411 - ALPHA - Some fixes for timers, implemented all timers, implemented all timer restrictions.
 *	04/10/2017 >>> v0.0.062.20170410 - ALPHA - Some fixes for timers, implemented all timers, their restrictions still not active.
 *	04/07/2017 >>> v0.0.061.20170407 - ALPHA - Some fixes for timers (waits inside timers) and implemented weekly timers. Months/years not working yet. Should be more stable.
 *	04/06/2017 >>> v0.0.060.20170406 - ALPHA - Timers for second/minute/hour/day are in. week/month/year not working yet. May be VERY quirky, still.
 *	03/30/2017 >>> v0.0.05f.20170329 - ALPHA - Attempt to fix setLocation, added Twilio integration (dialog support coming soon)
 *	03/29/2017 >>> v0.0.05e.20170329 - ALPHA - Added sendEmail
 *	03/29/2017 >>> v0.0.05d.20170329 - ALPHA - Minor typo fixes, thanks to @rayzurbock
 *	03/28/2017 >>> v0.0.05c.20170328 - ALPHA - Minor fixes regarding location subscriptions
 *	03/28/2017 >>> v0.0.05b.20170328 - ALPHA - Minor fixes for setting location mode
 *	03/27/2017 >>> v0.0.05a.20170327 - ALPHA - Minor fixes - location events do not have a device by default, overriding with location
 *	03/27/2017 >>> v0.0.059.20170327 - ALPHA - Completed SHM status and location mode. Can get/set, can subscribe to changes, any existing condition in pistons needs to be revisited and fixed
 *	03/25/2017 >>> v0.0.058.20170325 - ALPHA - Fixes for major issues introduced due to the new comparison editor (you need to re-edit all comparisons to fix them), added log multiline support, use \r or \n or \r\n in a string
 *	03/24/2017 >>> v0.0.057.20170324 - ALPHA - Improved installation experience, preventing direct installation of child app, location mode and shm status finally working
 *	03/23/2017 >>> v0.0.056.20170323 - ALPHA - Various fixes for restrictions
 *	03/22/2017 >>> v0.0.055.20170322 - ALPHA - Various improvements, including a revamp of the comparison dialog, also moved the dashboard website to https://dashboard.webcore.co
 *	03/21/2017 >>> v0.0.054.20170321 - ALPHA - Moved the dashboard website to https://webcore.homecloudhub.com/dashboard/
 *	03/21/2017 >>> v0.0.053.20170321 - ALPHA - Fixed a bug where variables containing expressions would be cast to the variable type outside of evaluateExpression (the right way)
 *	03/20/2017 >>> v0.0.052.20170320 - ALPHA - Fixed $shmStatus
 *	03/20/2017 >>> v0.0.051.20170320 - ALPHA - Fixed a problem where start values for variables would not be correctly picked up from atomicState (used state by mistake)
 *	03/20/2017 >>> v0.0.050.20170320 - ALPHA - Introducing parallelism, a semaphore mechanism to allow synchronization of multiple simultaneous executions, disabled by default (pistons wait at a semaphore)
 *	03/20/2017 >>> v0.0.04f.20170320 - ALPHA - Minor fixes for device typed variables (lost attribute) and counter variable in for each
 *	03/20/2017 >>> v0.0.04e.20170320 - ALPHA - Major operand/expression/cast refactoring to allow for arrays of devices - may break things. Also introduced for each loops and actions on device typed variables
 *	03/19/2017 >>> v0.0.04d.20170319 - ALPHA - Fixes for functions and device typed variables
 *	03/19/2017 >>> v0.0.04c.20170319 - ALPHA - Device typed variables now enabled - not yet possible to use them in conditions or in actions, but getting there
 *	03/18/2017 >>> v0.0.04b.20170318 - ALPHA - Various fixes
 *	03/18/2017 >>> v0.0.04a.20170318 - ALPHA - Enabled manual piston status and added the set piston status task as well as the exit statement
 *	03/18/2017 >>> v0.0.049.20170318 - ALPHA - Third attempt to fix switch
 *	03/18/2017 >>> v0.0.048.20170318 - ALPHA - Second attempt to fix switch fallbacks with wait breaks, wait in secondary cases were not working
 *	03/18/2017 >>> v0.0.047.20170318 - ALPHA - Attempt to fix switch fallbacks with wait breaks
 *	03/18/2017 >>> v0.0.046.20170318 - ALPHA - Various critical fixes - including issues with setLevel without a required state
 *	03/18/2017 >>> v0.0.045.20170318 - ALPHA - Fixed a newly introduced bug for Toggle (missing parameters)
 *	03/17/2017 >>> v0.0.044.20170317 - ALPHA - Cleanup ghost else-ifs on piston save
 *	03/17/2017 >>> v0.0.043.20170317 - ALPHA - Added "View piston in dashboard" to child app UI
 *	03/17/2017 >>> v0.0.042.20170317 - ALPHA - Various fixes and enabled restrictions - UI for conditions and restrictions needs refactoring to use the new operand editor
 *	03/16/2017 >>> v0.0.041.20170316 - ALPHA - Various fixes
 *	03/16/2017 >>> v0.0.040.20170316 - ALPHA - Fixed a bug where optional parameters were not correctly interpreted, leading to setLevel not working, added functions startsWith, endsWith, contains, eq, le, lt, ge, gt
 *	03/16/2017 >>> v0.0.03f.20170316 - ALPHA - Completely refactored task parameters and enabled variables. Dynamically assigned variables act as functions - it can be defined as an expression and reuse it in lieu of that expression
 *	03/15/2017 >>> v0.0.03e.20170315 - ALPHA - Various improvements
 *	03/14/2017 >>> v0.0.03d.20170314 - ALPHA - Fixed a bug with caching operands for triggers
 *	03/14/2017 >>> v0.0.03c.20170314 - ALPHA - Fixed a bug with switches
 *	03/14/2017 >>> v0.0.03b.20170314 - ALPHA - For statement finally getting some love
 *	03/14/2017 >>> v0.0.03a.20170314 - ALPHA - Added more functions (age, previousAge, newer, older, previousValue) and fixed a bug where operand caching stopped working after earlier code refactorings
 *	03/13/2017 >>> v0.0.039.20170313 - ALPHA - The Switch statement should now be functional - UI validation not fully done
 *	03/12/2017 >>> v0.0.038.20170312 - ALPHA - Traversing else ifs and else statements in search for devices to subscribe to
 *	03/12/2017 >>> v0.0.037.20170312 - ALPHA - Added support for break and exit (partial, piston state is not set on exit) - fixed some comparison data type incompatibilities
 *	03/12/2017 >>> v0.0.036.20170312 - ALPHA - Added TCP = cancel on condition change and TOS = Action - no other values implemented yet, also, WHILE loops are now working, please remember to add a WAIT in it...
 *	03/11/2017 >>> v0.0.035.20170311 - ALPHA - A little error creeped into the conditions, fixed it
 *	03/11/2017 >>> v0.0.034.20170311 - ALPHA - Multiple device selection aggregation now working properly. COUNT(device list's contact) rises above 1 will be true when at least two doors in the list are open :D
 *	03/11/2017 >>> v0.0.033.20170311 - ALPHA - Implemented all conditions except "was..." and all triggers except "stays..."
 *	03/11/2017 >>> v0.0.032.20170311 - ALPHA - Fixed setLevel null params and added version checking
 *	03/11/2017 >>> v0.0.031.20170310 - ALPHA - Various fixes including null optional parameters, conditional groups, first attempt at piston restrictions (statement restrictions not enabled yet), fixed a problem with subscribing device bolt indicators only showing for one instance of each device/attribute pair, fixed sendPushNotification
 *	03/10/2017 >>> v0.0.030.20170310 - ALPHA - Fixed a bug in scheduler introduced in 02e/02f
 *	03/10/2017 >>> v0.0.02f.20170310 - ALPHA - Various improvements, added toggle and toggleLevel
 *	03/10/2017 >>> v0.0.02e.20170310 - ALPHA - Fixed a problem where long expiration settings prevented logins (integer overflow)
 *	03/10/2017 >>> v0.0.02d.20170310 - ALPHA - Reporting version to JS
 *	03/10/2017 >>> v0.0.02c.20170310 - ALPHA - Various improvements and a new virtual command: Log to console. Powerful.
 *	03/10/2017 >>> v0.0.02b.20170310 - ALPHA - Implemented device versioning to correctly handle multiple browsers accessing the same dashboard after a device selection was performed, enabled security token expiry
 *	03/09/2017 >>> v0.0.02a.20170309 - ALPHA - Fixed parameter issues, added support for expressions in all parameters, added notification virtual tasks
 *	03/09/2017 >>> v0.0.029.20170309 - ALPHA - More execution flow fixes, sticky trace lines fixed
 *	03/08/2017 >>> v0.0.028.20170308 - ALPHA - Scheduler fixes
 *	03/08/2017 >>> v0.0.027.20170308 - ALPHA - Very early implementation of wait/delay scheduling, needs extensive testing
 *	03/08/2017 >>> v0.0.026.20170308 - ALPHA - More bug fixes, trace enhancements
 *	03/07/2017 >>> v0.0.025.20170307 - ALPHA - Improved logs and traces, added basic time event handler
 *	03/07/2017 >>> v0.0.024.20170307 - ALPHA - Improved logs (reverse order and live updates) and added trace support
 *	03/06/2017 >>> v0.0.023.20170306 - ALPHA - Added logs to the dashboard
 *	03/05/2017 >>> v0.0.022.20170305 - ALPHA - Some tasks are now executed. UI has an issue with initializing params on editing a task, will get fixed soon.
 *	03/01/2017 >>> v0.0.021.20170301 - ALPHA - Most conditions (and no triggers yet) are now parsed and evaluated during events - action tasks not yet executed, but getting close, very close
 *	02/28/2017 >>> v0.0.020.20170228 - ALPHA - Added runtime data - pistons are now aware of devices and global variables - expressions can query devices and variables (though not all system variables are ready yet)
 *	02/27/2017 >>> v0.0.01f.20170227 - ALPHA - Added support for a bunch more functions
 *	02/27/2017 >>> v0.0.01e.20170227 - ALPHA - Fixed a bug in expression parser where integer + integer would result in a string
 *	02/27/2017 >>> v0.0.01d.20170227 - ALPHA - Made progress evaluating expressions
 *	02/24/2017 >>> v0.0.01c.20170224 - ALPHA - Added functions support to main app
 *	02/06/2017 >>> v0.0.01b.20170206 - ALPHA - Fixed a problem with selecting thermostats
 *	02/01/2017 >>> v0.0.01a.20170201 - ALPHA - Updated comparisons
 *	01/30/2017 >>> v0.0.019.20170130 - ALPHA - Improved comparisons - ouch
 *	01/29/2017 >>> v0.0.018.20170129 - ALPHA - Fixed a conditions where devices would not be sent over to the UI
 *	01/28/2017 >>> v0.0.017.20170128 - ALPHA - Incremental update
 *	01/27/2017 >>> v0.0.016.20170127 - ALPHA - Minor compatibility fixes
 *	01/27/2017 >>> v0.0.015.20170127 - ALPHA - Updated capabilities, attributes, commands and refactored them into maps
 *	01/26/2017 >>> v0.0.014.20170126 - ALPHA - Progress getting comparisons to work
 *	01/25/2017 >>> v0.0.013.20170125 - ALPHA - Implemented the author field and more improvements to the piston editor
 *	01/23/2017 >>> v0.0.012.20170123 - ALPHA - Implemented the "delete" piston
 *	01/23/2017 >>> v0.0.011.20170123 - ALPHA - Fixed a bug where account id was not hashed
 *	01/23/2017 >>> v0.0.010.20170123 - ALPHA - Duplicate piston and restore from automatic backup :)
 *	01/23/2017 >>> v0.0.00f.20170123 - ALPHA - Automatic backup to myjson.com is now enabled. Restore is not implemented yet.
 *	01/22/2017 >>> v0.0.00e.20170122 - ALPHA - Enabled device cache on main app to speed up dashboard when using large number of devices
 *	01/22/2017 >>> v0.0.00d.20170122 - ALPHA - Optimized data usage for piston JSON class (might have broken some things), save now works
 *	01/21/2017 >>> v0.0.00c.20170121 - ALPHA - Made more progress towards creating new pistons
 *	01/21/2017 >>> v0.0.00b.20170121 - ALPHA - Made progress towards creating new pistons
 *	01/20/2017 >>> v0.0.00a.20170120 - ALPHA - Fixed a problem with dashboard URL and shards other than na01
 *	01/20/2017 >>> v0.0.009.20170120 - ALPHA - Reenabled the new piston UI at new URL
 *	01/20/2017 >>> v0.0.008.20170120 - ALPHA - Enabled html5 routing and rewrite to remove the /#/ contraption
 *	01/20/2017 >>> v0.0.007.20170120 - ALPHA - Cleaned up CoRE ST UI and removed "default" theme from URL.
 *	01/19/2017 >>> v0.0.006.20170119 - ALPHA - UI is now fully moved and security enabled - security password is now required
 *	01/18/2017 >>> v0.0.005.20170118 - ALPHA - Moved UI to homecloudhub.com and added support for pretty url (core.homecloudhub.com) and web+core:// handle
 *	01/17/2017 >>> v0.0.004.20170117 - ALPHA - Updated to allow multiple instances
 *	01/17/2017 >>> v0.0.003.20170117 - ALPHA - Improved security, object ids are hashed, added multiple-location-multiple-instance support (CoRE will be able to work across multiple location and installed instances)
 *	12/02/2016 >>> v0.0.002.20161202 - ALPHA - Small progress, Add new piston now points to the piston editor UI
 *	10/28/2016 >>> v0.0.001.20161028 - ALPHA - Initial release
 */

/******************************************************************************/
/*** webCoRE DEFINITION														***/
/******************************************************************************/
private static String handle() { return "webCoRE" }
include 'asynchttp_v1'
definition(
    name: "${handle()} Piston",
    namespace: "ady624",
    author: "Adrian Caramaliu",
    description: "Do not install this directly, use webCoRE instead",
    category: "Convenience",
	parent: "ady624:${handle()}",
    /* icons courtesy of @chauger - thank you */
	iconUrl: "https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE.png",
	iconX2Url: "https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE@2x.png",
	iconX3Url: "https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE@3x.png"
)


preferences {
	//common pages
	page(name: "pageMain")
	page(name: "pageRun")
	page(name: "pageClear")
	page(name: "pageClearAll")
}

/******************************************************************************/
/*** 																		***/
/*** CONFIGURATION PAGES													***/
/*** 																		***/
/******************************************************************************/
def pageMain() {
	//webCoRE Piston main page
	return dynamicPage(name: "pageMain", title: "", uninstall: !!state.build) {
    	if (!parent || !parent.isInstalled()) {
        	section() {
				paragraph "Sorry, you cannot install a piston directly from the Marketplace, please use the webCoRE SmartApp instead."
            }
        	section("Installing webCoRE") {
            	paragraph "If you are trying to install webCoRE, please go back one step and choose webCoRE, not webCoRE Piston. You can also visit wiki.webcore.co for more information on how to install and use webCoRE"
				if (parent) href "", title: "More information", description: parent.getWikiUrl(), style: "external", url: parent.getWikiUrl(), image: "https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE.png", required: false
            }
        } else {
            def currentState = state.currentState

            section ("General") {
                label name: "name", title: "Name", required: true, state: (name ? "complete" : null), defaultValue: parent.generatePistonName(), submitOnChange: true
            }

            section("Dashboard") {
                def dashboardUrl = parent.getDashboardUrl()
                if (dashboardUrl) {
                    dashboardUrl = "${dashboardUrl}piston/${hashId(app.id)}"
                    href "", title: "View piston in dashboard", style: "external", url: dashboardUrl, image: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/dashboard.png", required: false
                } else {
                    paragraph "Sorry, your dashboard does not seem to be enabled, please go to the parent app and enable the dashboard."
                }
            }

            section(title:"Application Info") {
                if (state.bin) {
                	paragraph state.bin, title: "Automatic backup bin code"
                }
                paragraph version(), title: "Version"
                paragraph mem(), title: "Memory Usage"
            }

            section(title:"Recovery") {
            	href "pageRun", title: "Force-run this piston"
                href "pageClear", title: "Clear all data except variables", description: "You will lose all logs, trace points, statistics, but no variables"
                href "pageClearAll", title: "Clear all data", description: "You will lose all data stored in any variables"
            }
        }
	}
}

def pageRun() {
	test()
	return dynamicPage(name: "pageRun", title: "", uninstall: false) {
    	section("Run") {
        	paragraph "OK - under construction"
        }
    }
}

def pageClear() {
	state.cache = [:]
    state.hash = [:]
    state.logs = []
    state.stats = [:]
    state.trace = [:]
	return dynamicPage(name: "pageClear", title: "", uninstall: false) {
    	section("Clear") {
        	paragraph "All non-essential data has been cleared."
        }
    }
}

def pageClearAll() {
	state.cache = [:]
    state.hash = [:]
    state.logs = []
    state.stats = [:]
    state.trace = [:]
    state.vars = [:]
	return dynamicPage(name: "pageClearAll", title: "", uninstall: false) {
    	section("Clear All") {
        	paragraph "All data has been cleared."
        }
    }
}

/******************************************************************************/
/*** 																		***/
/*** PUBLIC METHODS															***/
/*** 																		***/
/******************************************************************************/

def installed() {
   	state.created = now()
    state.modified = now()
    state.build = 0
    state.piston = [:]
    state.vars = state.vars?: [:]
    state.subscriptions = state.subscriptions ?: [:]
    state.logging = 0
	initialize()
	return true
}

def updated() {
	unsubscribe()
	initialize()
	return true
}

def initialize() {
	if (!!state.active) {
    	resume()
    }
}

def get(boolean minimal = false) {
	return [
    	meta: [
			id: hashId(app.id),
    		author: state.author,
    		name: app.label,
    		created: state.created,
	    	modified: state.modified,
	    	build: state.build,
	    	bin: state.bin,
	    	active: state.active,
            category: state.category ?: 0
			],
        piston: state.piston
	] + (minimal ? [:] : [
        systemVars: getSystemVariablesAndValues(),
        subscriptions: state.subscriptions,
	    stats: state.stats,
        state: state.state,
        logging: state.logging ?: 0,
        logs: state.logs,
        trace: state.trace,
        localVars: state.vars,
        memory: mem(),
        lastExecuted: state.lastExecuted,
        nextSchedule: state.nextSchedule,
        schedules: state.schedules
    ])
}

def activity(lastLogTimestamp) {
	def logs = state.logs
    def llt = lastLogTimestamp && lastLogTimestamp instanceof String && lastLogTimestamp.isLong() ? lastLogTimestamp.toLong() : 0
    def index = llt ? logs.findIndexOf{ it.t == llt } : 0
    index = index > 0 ? index : (llt ? 0 : logs.size())
	return [
    	name: app.label,
        state: state.state,
    	logs: index ? logs[0..index-1] : [],
    	trace: state.trace,
        localVars: state.vars,
        memory: mem(),
        lastExecuted: state.lastExecuted,
        nextSchedule: state.nextSchedule,
        schedules: state.schedules
    ]
}

def clearLogs() {
	atomicState.logs = []
    return [:]
}


def setup(data, chunks) {
	if (!data) return false
    state.trace = [:]
    state.logs = []
    state.hash = [:]
	state.modified = now()
    state.build = (int)(state.build ? (int)state.build + 1 : 1)
    def piston = [
    	o: data.o ?: {},
    	r: data.r ?: [],
    	rn: !!data.rn,
		rop: data.rop ?: 'and',
		s: data.s ?: [],
        v: data.v ?: [],
        z: data.z ?: ''
    ]
    if (data.n) app.updateLabel(data.n)
    setIds(piston)
    for(chunk in settings.findAll{ it.key.startsWith('chunk:') && !chunks[it.key] }) {
    	app.updateSetting(chunk.key, [type: 'text', value: ''])
    }
    for(chunk in chunks) {
    	app.updateSetting(chunk.key, [type: 'text', value: chunk.value])
    }
    app.updateSetting('bin', [type: 'text', value: state.bin ?: ''])
    app.updateSetting('author', [type: 'text', value: state.author ?: ''])
    state.piston = piston
    state.trace = [:]
    state.schedules = []
    state.vars = state.vars ?: [:];
    state.modifiedVersion = version()
    //todo replace this
    Map rtData = [:]
    if ((state.build == 1) || (!!state.active)) {
    	rtData = resume()
    }
    return [active: state.active, build: state.build, modified: state.modified, state: state.state, rtData: rtData]
}


private int setIds(node, maxId = 0, existingIds = [:], requiringIds = [], level = 0) {
    if (node?.t in ['if', 'while', 'repeat', 'for', 'each', 'switch', 'action', 'every', 'condition', 'restriction', 'group', 'do', 'on', 'event']) {
        def id = node['$']
        if (!id || existingIds[id]) {
            requiringIds.push(node)
        } else {
            maxId = maxId < id ? id : maxId
            existingIds[id] = id
        }
        if ((node.t == 'if') && (node.ei)) {
        	node.ei.removeAll{ !it.c && !it.s }
			for (elseIf in node.ei) {
		        id = elseIf['$']
                if (!id || existingIds[id]) {
                    requiringIds.push(elseIf)
                } else {
                    maxId = (maxId < id) ? id : maxId
                    existingIds[id] = id
                }
            }
        }
        if ((node.t == 'switch') && (node.cs)) {
			for (_case in node.cs) {
		        id = _case['$']
                if (!id || existingIds[id]) {
                    requiringIds.push(_case)
                } else {
                    maxId = (maxId < id) ? id : maxId
                    existingIds[id] = id
                }
            }
        }
        if ((node.t == 'action') && (node.k)) {
			for (task in node.k) {
		        id = task['$']
                if (!id || existingIds[id]) {
                    requiringIds.push(task)
                } else {
                    maxId = (maxId < id) ? id : maxId
                    existingIds[id] = id
                }
            }
        }
    }
	for (list in node.findAll{ it.value instanceof List }) {
        for (item in list.value.findAll{ it instanceof Map }) {
            maxId = setIds(item, maxId, existingIds, requiringIds, level + 1)
        }
    }
    if (level == 0) {
    	for (item in requiringIds) {
        	maxId += 1
        	item['$'] = maxId
        }
    }
    return maxId
}



def config(data) {
	if (!data) {
    	return false;
    }
	if (data.bin) {
		state.bin = data.bin;
	    app.updateSetting('bin', [type: 'text', value: state.bin ?: ''])
    }
	if (data.author) {
		state.author = data.author;
	    app.updateSetting('author', [type: 'text', value: state.author ?: ''])
    }
	if (data.initialVersion) {
		state.initialVersion = data.initialVersion;
    }
}

def setBin(bin) {
	if (!bin || !!state.bin) {
    	return false;
    }
	atomicState.bin = bin;
    app.updateSetting('bin', [type: 'text', value: bin ?: ''])
    return [:]
}

Map pause() {
	state.active = false
    def rtData = getRunTimeData()
	def msg = timer "Piston successfully stopped", null, -1
    if (rtData.logging) info "Stopping piston...", rtData, 0
    checkVersion(rtData)
    state.schedules = []
    rtData.stats.nextSchedule = 0
    unsubscribe()
    unschedule()
    app.updateSetting('dev', null)
    app.updateSetting('contacts', null)
    state.hash = null
    state.trace = [:]
    state.subscriptions = [:]
    state.schedules = []
    if (rtData.logging) info msg, rtData
    updateLogs(rtData)
    atomicState.schedules = []
	atomicState.active = false
    return rtData
}

Map resume() {
	state.active = true;
	def tempRtData = getTemporaryRunTimeData()
    def msg = timer "Piston successfully started", null,  -1
	if (tempRtData.logging) info "Starting piston... (${version()})", tempRtData, 0
    def rtData = getRunTimeData(tempRtData, null, true)
    checkVersion(rtData)
    state.hash = null
    state.subscriptions = [:]
    atomicState.schedules = []
    state.schedules = []
    subscribeAll(rtData)
    if (rtData.logging) info msg, rtData
    updateLogs(rtData)
    rtData.result = [active: true, subscriptions: state.subscriptions]
	atomicState.active = true
    return rtData
}


def setLoggingLevel(level) {
    def logging = "$level".toString()
    logging = logging.isInteger() ? logging.toInteger() : 0
    if (logging < 0) logging = 0
    if (logging > 3) logging = 3
	atomicState.logging = logging
    return [logging: logging]
}

def setCategory(category) {
	atomicState.category = category
    return [category: category]
}

def test() {
	handleEvents([date: new Date(), device: location, name: 'test', value: now()])
    return [:]
}

def execute(data, source) {
	handleEvents([date: new Date(), device: location, name: 'execute', value: source ?: now(), jsonData: data])
    return [:]
}

def clickTile(index) {
	handleEvents([date: new Date(), device: location, name: 'tile', value: index])
    return state.state ?: [:]
}

private getTemporaryRunTimeData() {
	return [
    	temporary: true,
       	timestamp: now(),
        logging: state.logging ?: 0,
        logs:[]
    ]
}

private getRunTimeData(rtData = null, semaphore = null, fetchWrappers = false) {
	def n = now()
	try {
		def timestamp = rtData?.timestamp ?: now()
	    def piston = state.piston
        def appId = hashId(app.id)
	    def logs = (rtData && rtData.temporary) ? rtData.logs : null
		rtData = (rtData && !rtData.temporary) ? rtData : parent.getRunTimeData((semaphore && !(piston.o?.pep)) ? appId : null, !!fetchWrappers)
	    rtData.timestamp = timestamp
	    rtData.logs = [[t: timestamp]]
	    if (logs && logs.size()) {
	        rtData.logs = rtData.logs + logs
	    }
	    rtData.trace = [t: timestamp, points: [:]]
	    rtData.id = appId
		rtData.active = state.active;
        rtData.category = state.category;
	    rtData.stats = [nextScheduled: 0]
	    //we're reading the cache from atomicState because we might have waited at a semaphore
	    rtData.cache = atomicState.cache ?: [:]
	    rtData.newCache = [:]
	    rtData.schedules = []
	    rtData.cancelations = [statements:[], conditions:[], all: false]
	    rtData.piston = piston
        def logging = "$state.logging".toString()
        logging = logging.isInteger() ? logging.toInteger() : 0
        rtData.logging = (int) logging
	    rtData.locationId = hashId(location.id)
        rtData.locationModeId = hashId(location.getCurrentMode().id)
	    //flow control
	    //we're reading the old state from atomicState because we might have waited at a semaphore
        def st = atomicState.state
        rtData.state = (st instanceof Map) ? st : [old: '', new: '']
	    rtData.state.old = rtData.state.new;
        rtData.store = atomicState.store ?: [:]
	    rtData.statementLevel = 0;
	    rtData.fastForwardTo = null
	    rtData.break = false
        rtData.updateDevices = false
        state.schedules = atomicState.schedules
        if (!fetchWrappers) {
        	rtData.devices = (settings.dev && (settings.dev instanceof List) ? settings.dev.collectEntries{[(hashId(it.id)): it]} : [:])
        	rtData.contacts = (settings.contacts && (settings.contacts instanceof List) ? settings.contacts.collectEntries{[(hashId(it.id)): it]} : [:])
        }
	    rtData.systemVars = getSystemVariables()
	    rtData.localVars = getLocalVariables(rtData, piston.v)
	} catch(all) {
    	error "Error while getting runtime data:", rtData, null, all
    }
	return rtData
}

private checkVersion(rtData) {
	def ver = version()
    if (ver != rtData.coreVersion) {
    	//parent and child apps have different versions
        warn "WARNING: Results may be unreliable because the ${ver > rtData.coreVersion ? "child app's version ($ver) is newer than the parent app's version (${rtData.coreVersion})" : "parent app's version (${rtData.coreVersion}) is newer than the child app's version ($ver)" }. Please consider updating both apps to the same version.", rtData
    }
    if (!location.timeZone) {
    	error "Your SmartThings location is not setup correctly - timezone information is missing. Please visit the SmartThings mobile app, go to More, tap the cog settings button, tap the map, select your location by placing the pin and radius on the map, then tap Save, and then tap Done. You may encounter error or incorrect timing until this is fixed."
    }
}

/******************************************************************************/
/*** 																		***/
/*** EVENT HANDLING															***/
/*** 																		***/
/******************************************************************************/
def fakeHandler(event) {
	def rtData = getRunTimeData()
	warn "Received unexpected event [${event.device?:location}].${event.name} = ${event.value} (device has no active subscriptions)... ", rtData
    updateLogs(rtData)
}

def deviceHandler(event) {
	handleEvents(event)
}

def timeHandler(event, recovery = false) {
try {
	handleEvents([date: new Date(event.t), device: location, name: 'time', value: event.t, schedule: event, recovery: recovery])
    } catch (Exception e) {
    error "Unexpected error:", null, null, e
    }
}

//new and improved timeout recovery management
def timeoutRecoveryHandler_webCoRE(event) {
	timeHandler([t:now()], true)
}

/*
def timeRecoveryHandler(event) {
	timeHandler(event, true)
}
*/

def executeHandler(event) {
	handleEvents([date: event.date, device: location, name: 'execute', value: event.value, jsonData: event.jsonData])
}

//entry point for all events
def handleEvents(event) {
	//cancel all pending jobs, we'll handle them later
	//unschedule(timeHandler)
    if (!state.active) return
	def startTime = now()
    state.lastExecuted = startTime
    def eventDelay = startTime - event.date.getTime()
	def msg = timer "Event processed successfully", null, -1
    def tempRtData = getTemporaryRunTimeData()
	if (tempRtData.logging) info "Received event [${event.device?:location}].${(event.name == 'time') ? event.name + (event.recovery ? '/recovery' : '') : event.name} = ${event.value} with a delay of ${eventDelay}ms", tempRtData, 0
    state.temp = [:]
    //todo start execution
    def ver = version()
	def msg2 = timer "Runtime successfully initialized in 0ms ($ver)"
    Map rtData = getRunTimeData(tempRtData, true)
    if (rtData.logging > 2) debug "RunTime Analysis CS > ${rtData.started - startTime}ms > PS > ${rtData.generatedIn}ms > PE > ${now() - rtData.ended}ms > CE", rtData
    if (!rtData.enabled) {
		warn "Kill switch is active, aborting piston execution."
    	return;
    }
    checkVersion(rtData)
    setTimeoutRecoveryHandler('timeoutRecoveryHandler_webCoRE')
	//runIn(30, timeRecoveryHandler)
    if (rtData.semaphoreDelay) {
    	warn "Piston waited at a semaphore for ${rtData.semaphoreDelay}ms", rtData
    }
    msg2.m = "Runtime (${"$rtData".size()} bytes) successfully initialized in ${rtData.generatedIn}ms ($ver)"
    if (rtData.logging > 1) trace msg2, rtData
    rtData.stats.timing = [
    	t: startTime,
    	d: eventDelay > 0 ? eventDelay : 0,
        l: now() - startTime
    ]
    startTime = now()
	msg2 = timer "Execution stage complete.", null, -1
    if (rtData.logging > 1) trace "Execution stage started", rtData, 1
    def success = true
    def syncTime = false
    if ((event.name != 'time') && (event.name != 'wc_async_reply')) {
    	success = executeEvent(rtData, event)
        syncTime = true
    }
    //process all time schedules in order
    def t = now()
    while (success && (20000 + rtData.timestamp - now() > 15000)) {
        //we only keep doing stuff if we haven't passed the 10s execution time mark
        def schedules = rtData.piston.o?.pep ? atomicState.schedules : state.schedules
        //anything less than 2 seconds in the future is considered due, we'll do some pause to sync with it
        //we're doing this because many times, the scheduler will run a job early, usually 0-1.5 seconds early...
        if (!schedules || !schedules.size()) break
        if (event.name == 'wc_async_reply') {
        	event.schedule = schedules.sort{ it.t }.find{ it.d == event.value }
        } else {
        	event = [date: event.date, device: location, name: 'time', value: now(), schedule: schedules.sort{ it.t }.find{ it.t < now() + 2000 }]
       }
        if (!event.schedule) break
        long threshold = now() > event.schedule.t ? now() : event.schedule.t
        //schedules.removeAll{ (it.t <= threshold) && (it.s == event.schedule.s) && (it.i == event.schedule.i) }
        schedules.remove(event.schedule)
        if (event.name == 'wc_async_reply') {
        	if (event.schedule.stack) {
              event.schedule.stack.response = event.responseData
              event.schedule.stack.json = event.jsonData
            }
            event.name = 'time'
            event.value = now()
            int responseCode = cast(rtData, event.responseCode, 'integer')
            def contentType = cast(rtData, event.contentType, 'string')
            setSystemVariableValue(rtData, '$httpContentType', contentType)
            setSystemVariableValue(rtData, '$httpStatusCode', responseCode)
            setSystemVariableValue(rtData, '$httpStatusOk', (responseCode >= 200) && (responseCode <= 299))
            if (event.setRtData) {
            	for(item in event.setRtData) {
                	rtData[item.key] = item.value
                }
            }
        } else {
        	if (event.schedule.d == 'httpRequest') {
            	setSystemVariableValue(rtData, '$httpContentType', '')
            	setSystemVariableValue(rtData, '$httpStatusCode', 408)
            	setSystemVariableValue(rtData, '$httpStatusOk', false)
            }
        }
        //if we have any other pending -3 events (device schedules), we cancel them all
        //if (event.schedule.i > 0) schedules.removeAll{ (it.s == event.schedule.s) && ( it.i == -3 ) }
        if (rtData.piston.o?.pep) {
        	atomicState.schedules = schedules
        } else {
        	state.schedules = schedules
        }
        def delay = event.schedule.t - now()
        if (syncTime && (delay > 0)) {
        	if (rtData.logging > 2) debug "Fast executing schedules, waiting for ${delay}ms to sync up", rtData
        	pause delay
        }
       	success = executeEvent(rtData, event)
        syncTime = true
        //if we waited at a semaphore, we don't want to process too many events
        if (rtData.semaphoreDelay) break
    }
	rtData.stats.timing.e = now() - startTime
    if (rtData.logging > 1) trace msg2, rtData
    if (!success) msg.m = "Event processing failed"
    finalizeEvent(rtData, msg, success)
    if (rtData.currentEvent) {
    	try {
		    def desc = 'webCore piston \'' + app.label + '\' was executed'
    		sendLocationEvent(name: 'webCoRE', value: 'pistonExecuted', isStateChange: true, displayed: false, linkText: desc, descriptionText: desc, data: [
    			id: hashId(app.id),
	        	name: app.label,
	    	    event: [date: rtData.currentEvent.date, delay: rtData.currentEvent.delay, duration: now() - rtData.currentEvent.date, device: "$rtData.event.device", name: rtData.currentEvent.name, value: rtData.currentEvent.value, physical: rtData.currentEvent.physical, index: rtData.currentEvent.index],
    	    	state: [old: rtData.state.old, new: rtData.state.new]
			])
		} catch (all) {
        }
	}
}

private Boolean executeEvent(rtData, event) {
	try {
    	rtData = rtData ?: getRunTimeData()
        //event processing

		rtData.event = event
        rtData.previousEvent = state.lastEvent
        def index = 0
        if (event.jsonData) {
            def attribute = rtData.attributes[event.name]
            if (attribute && attribute.i && event.jsonData[attribute.i]) {
                index = event.jsonData[attribute.i]
            }
            if (!index) index = 1
        }
        def srcEvent = event && (event.name == 'time') && event.schedule && event.schedule.evt ? event.schedule.evt : null
       	rtData.args = event ? ((event.name == 'time') && event.schedule && event.schedule.args && (event.schedule.args instanceof Map) ? event.schedule.args : (event.jsonData ?: [:])) : [:]
        if (event && (event.name == 'time') && event.schedule && event.schedule.stack) {
            setSystemVariableValue(rtData, '$index', event.schedule.stack.index)
            setSystemVariableValue(rtData, '$device', event.schedule.stack.device)
            setSystemVariableValue(rtData, '$devices', event.schedule.stack.devices)
            rtData.json = event.schedule.stack.json ?: [:]
            rtData.response = event.schedule.stack.response ?: [:]
		}
        rtData.currentEvent = [
            date: event.date.getTime(),
            delay: rtData.stats?.timing?.d ?: 0,
            device: srcEvent ? srcEvent.device : hashId((event.device?:location).id),
            name: srcEvent ? srcEvent.name : event.name,
            value: srcEvent ? srcEvent.value : event.value,
            unit: srcEvent ? srcEvent.unit : event.unit,
            physical: srcEvent ? srcEvent.physical : !!event.physical,
            index: index
        ]
        state.lastEvent = rtData.currentEvent
        //previous variables
        rtData.conditionStateChanged = false
        rtData.pistonStateChanged = false
        rtData.fastForwardTo = null
        rtData.resumed = false
        rtData.terminated = false
        if (event.name == 'time') {
        	rtData.fastForwardTo = event.schedule.i
        }
		setSystemVariableValue(rtData, '$state', rtData.state.new)
        setSystemVariableValue(rtData, '$previousEventDate', rtData.previousEvent?.date ?: now())
        setSystemVariableValue(rtData, '$previousEventDelay', rtData.previousEvent?.delay ?: 0)
        setSystemVariableValue(rtData, '$previousEventDevice', [rtData.previousEvent?.device])
        setSystemVariableValue(rtData, '$previousEventDeviceIndex', rtData.previousEvent?.index ?: 0)
        setSystemVariableValue(rtData, '$previousEventAttribute', rtData.previousEvent?.name ?: '')
        setSystemVariableValue(rtData, '$previousEventValue', rtData.previousEvent?.value ?: '')
        setSystemVariableValue(rtData, '$previousEventUnit', rtData.previousEvent?.unit ?: '')
        setSystemVariableValue(rtData, '$previousEventDevicePhysical', !!rtData.previousEvent?.physical)
        //current variables
        setSystemVariableValue(rtData, '$currentEventDate', rtData.currentEvent.date ?: now())
        setSystemVariableValue(rtData, '$currentEventDelay', rtData.currentEvent.delay ?: 0)
        setSystemVariableValue(rtData, '$currentEventDevice', [rtData.currentEvent?.device])
        setSystemVariableValue(rtData, '$currentEventDeviceIndex', (rtData.currentEvent.index != '') && (rtData.currentEvent.index != null) ? rtData.currentEvent.index : 0)
        setSystemVariableValue(rtData, '$currentEventAttribute', rtData.currentEvent.name ?: '')
        setSystemVariableValue(rtData, '$currentEventValue', rtData.currentEvent.value ?: '')
        setSystemVariableValue(rtData, '$currentEventUnit', rtData.currentEvent.unit ?: '')
        setSystemVariableValue(rtData, '$currentEventDevicePhysical', !!rtData.currentEvent.physical)
		//todo - check restrictions
        rtData.stack = [c: 0, s: 0, cs:[], ss:[]]
        def ended = false
        try {
		    def allowed = !rtData.piston.r || !(rtData.piston.r.length) || evaluateConditions(rtData, rtData.piston, 'r', true)
           	rtData.restricted = !rtData.piston.o?.aps && !allowed
    		if (allowed || !!rtData.fastForwardTo) {
				if (rtData.fastForwardTo == -3) {
                	//device related time schedules
					if (!rtData.restricted) {
                        def data = event.schedule.d
                        if (data && data.d && data.c) {
                            //we have a device schedule, execute it
                            def device = getDevice(rtData, data.d)
                            if (device) {
                                //executing scheduled physical command
                                //used by fades, flashes, etc.
                                executePhysicalCommand(rtData, device, data.c, data.p, null, null, true)
                            }
                        }
                    }
				} else {
                    if (executeStatements(rtData, rtData.piston.s)) {
                        ended = true
                        tracePoint(rtData, 'end', 0, 0)
                    }
                    processSchedules rtData
				}
            } else {
            	warn "Piston execution aborted due to restrictions in effect", rtData
                //we need to run through all to update stuff
                rtData.fastForwardTo = -9
                executeStatements(rtData, rtData.piston.s)
                ended = true
				tracePoint(rtData, 'end', 0, 0)
				processSchedules rtData

            }
            if (!ended) tracePoint(rtData, 'break', 0, 0)
        } catch (all) {
        	error "An error occurred while executing the event: ", rtData, null, all
        }
		return true
    } catch(all) {
    	error "An error occurred within executeEvent: ", rtData, null, all
    }
    processSchedules rtData
    return false
}

private finalizeEvent(rtData, initialMsg, success = true) {


	def startTime = now()
    processSchedules(rtData, true)

	if (rtData.updateDevices) {
    	updateDeviceList(rtData.devices*.value.id)
    }
    if (initialMsg) {
    	if (success) {
        	if (rtData.logging) info initialMsg, rtData
        } else {
        	error initialMsg
        }
    }

	updateLogs(rtData)
	//update graph data
    rtData.stats.timing.u = now() - startTime
    def stats = (rtData.piston.o?.pep ? atomicState.stats : state.stats) ?: [:]
    stats.timing = stats.timing ?: []
    stats.timing.push(rtData.stats.timing)
    if (stats.timing.size() > 500) stats.timing = stats.timing[stats.timing.size() - 500..stats.timing.size() - 1]
    rtData.trace.d = now() - rtData.trace.t
    //temporary fix for migration from single to multiple tiles
	if (rtData.state.i || rtData.state.t) {
    	rtData.state.i1 = rtData.state.i
    	rtData.state.t1 = rtData.state.t
    	rtData.state.c1 = rtData.state.c
    	rtData.state.b1 = rtData.state.b
    	rtData.state.f1 = rtData.state.f
    	rtData.state.remove('i')
    	rtData.state.remove('t')
    	rtData.state.remove('c')
    	rtData.state.remove('b')
    	rtData.state.remove('f')
    }
	state.state = rtData.state
    state.stats = stats
    state.trace = rtData.trace
    //flush the new cache value
    for(item in rtData.newCache) rtData.cache[item.key] = item.value
    //remove the media as it may be large
    rtData.media = null
	parent.updateRunTimeData(rtData)
    //clear the global vars - we already set them
    rtData.gvCache = null
    rtData.gvStoreCache = null
    //beat race conditions
    //overwrite state, might have changed meanwhile
    if (rtData.piston.o?.pep) {
    	state.schedules = atomicState.schedules
    	atomicState.cache = rtData.cache
        atomicState.store = rtData.store
    }
	state.cache = rtData.cache
    state.store = rtData.store
}

private processSchedules(rtData, scheduleJob = false) {
	//def msg = timer "Processing schedules..."
	//reschedule stuff
    //todo, override tasks, if any

    def tt = now()
    def schedules = (rtData.piston.o?.pep ? atomicState.schedules : state.schedules) ?: []
    /*
    for (timer in rtData.piston.s.findAll{ it.t == 'every' }) {
    	if (!schedules.find{ it.s == timer.$ } && !rtData.schedules.find{ it.s == timer.$ }) {
        	if (rtData.logging > 2) debug "Rescheduling missing timer ${timer.$}", rtData
    		scheduleTimer(rtData, timer, 0)
        }
    }
    */
	//reset states
    //if automatic states, we set it based on the autoNew - if any
    if (!rtData.piston.o?.mps) {
    	rtData.state.new = rtData.state.autoNew ?: 'true'
    }

    //debug msg, rtData

    rtData.state.old = rtData.state.new
    //schedules = (atomicState.schedules ?: [])
    if (rtData.cancelations.all) {
    	schedules.removeAll{ it.i > 0 }
    }
    //cancel statements
	schedules.removeAll{ schedule -> !!rtData.cancelations.statements.find{ cancelation -> (cancelation.id == schedule.s) && (!cancelation.data || (cancelation.data == schedule.d)) }}
    //cancel on conditions
	for(cid in rtData.cancelations.conditions) {
    	schedules.removeAll{ cid in it.cs }
    }
    //cancel on piston state change
    if (rtData.pistonStateChanged) {
    	schedules.removeAll{ !!it.ps }
    }
    //rtData.pistonStateChanged = false
    rtData.cancelations = []
    rtData.hasNewSchedules = rtData.hasNewSchedules || (rtData.schedules && rtData.schedules.size())
    schedules = (schedules + (rtData.schedules ?: []))//.sort{ it.t }
    //add traces for all remaining schedules
    /*for (schedule in schedules) {
    	def t = now() - schedule.t
        if ((t < 0) && (schedule.i > 0) && !rtData.trace.points["t:${schedule.i}"]) {
            //we enter a fake trace point to show it on the trace view
    		tracePoint(rtData, "t:${schedule.i}", 0, t)
        }
    }*/
    if (scheduleJob) {
        if (schedules.size()) {
    		def next = schedules.sort{ it.t }[0]
        	def t = (next.t - now()) / 1000
        	t = (t < 1 ? 1 : t)
        	rtData.stats.nextSchedule = next.t
        	if (rtData.logging) info "Setting up scheduled job for ${formatLocalTime(next.t)} (in ${t}s)" + (schedules.size() > 1 ? ', with ' + (schedules.size() - 1).toString() + ' more job' + (schedules.size() > 2 ? 's' : '') + ' pending' : ''), rtData
        	runIn(t, timeHandler, [data: next])
        	//runIn(t + 30, timeRecoveryHandler, [data: next])
    	} else {
	    	rtData.stats.nextSchedule = 0
            //remove the recovery
    		//unschedule(timeRecoveryHandler)
	    }
    }
    if (rtData.piston.o?.pep) atomicState.schedules = schedules
    state.schedules = schedules
    state.schedules = schedules
    state.nextSchedule = rtData.stats.nextSchedule
    rtData.schedules = []
}

private updateLogs(rtData) {
	//we only save the logs if we got some
	if (!rtData || !rtData.logs || (rtData.logs.size() < 2)) return
    def logs = (rtData.logs?:[]) + (atomicState.logs?:[])
    def maxLogSize = 500
    //we attempt to store 500 logs, but if that's too much, we go down in 50 increments
    while (maxLogSize >= 0) {
	    if (logs.size() > maxLogSize) {
        	def maxSz = maxLogSize < logs.size() ? maxLogSize : logs.size()
            if (maxSz) {
    			logs = logs[0..maxSz]
            } else {
            	logs = []
            }
    	}
        state.logs = logs
        if ("$state".size() > 75000) {
        	maxLogSize -= 50
        } else {
        	break
        }
    }
    atomicState.logs = logs
    state.logs = logs
    //rtData.remove('logs')
}



private Boolean executeStatements(rtData, statements, async = false) {
	rtData.statementLevel = rtData.statementLevel + 1
	for(statement in statements) {
    	//only execute statements that are enabled
    	if (!statement.di && !executeStatement(rtData, statement, !!async)) {
        	//stop processing
			rtData.statementLevel = rtData.statementLevel - 1
        	return false
        }
    }
    //continue processing
	rtData.statementLevel = rtData.statementLevel - 1
    return true
}

private Boolean executeStatement(rtData, statement, async = false) {
	//if rtData.fastForwardTo is a positive, non-zero number, we need to fast forward through all
    //branches until we find the task with an id equal to that number, then we play nicely after that
	if (!statement) return false
    if (!rtData.fastForwardTo) {
    	switch (statement.tep) {
        	case 'c':
            	if (!rtData.conditionStateChanged) {
                	if (rtData.logging > 2) debug "Skipping execution for statement #${statement.$} because condition state did not change", rtData
            		return;
                }
                break;
        	case 'p':
            	if (!rtData.pistonStateChanged) {
                	if (rtData.logging > 2) debug "Skipping execution for statement #${statement.$} because piston state did not change", rtData
                	return;
                }
                break;
        	case 'b':
            	if ((!rtData.conditionStateChanged) && (!rtData.pistonStateChanged)) {
                	if (rtData.logging > 2) debug "Skipping execution for statement #${statement.$} because neither condition state nor piston state changed", rtData
                	return;
                }
                break;
        }
    }
    rtData.stack.ss.push(rtData.stack.s)
    rtData.stack.s = statement.$
    def t = now()
    def value = true
    def c = rtData.stack.c
    def stacked = (true /* cancelable on condition change */)
    if (stacked) {
    	rtData.stack.cs.push(c)
    }
    def parentConditionStateChanged = rtData.conditionStateChanged
    def parentAsync = async
    def parentIndex = getVariable(rtData, '$index').v
    def parentDevice = getVariable(rtData, '$device').v
    def selfAsync = (statement.a == "1") || (statement.t == 'every') || (statement.t == 'on')
    async = !!async || selfAsync
    def perform = false
    def repeat = true
    def index = null
    def allowed = !statement.r || !(statement.r.length) || evaluateConditions(rtData, statement, 'r', async)
    if (allowed || !!rtData.fastForwardTo) {
    	while (repeat) {
            switch (statement.t) {
                case 'every':
                	//we override current condition so that child statements can cancel on it
                    def ownEvent = (rtData.event && (rtData.event.name == 'time') && rtData.event.schedule && (rtData.event.schedule.s == statement.$) && (rtData.event.schedule.i < 0))
                    if (ownEvent || !state.schedules.find{ it.s == statement.$ }) {
                    	//if the time has come for our timer, schedule the next timer
                        //if no next time is found quick enough, a new schedule with i = -2 will be setup so that a new attempt can be made at a later time
                    	if (ownEvent) rtData.fastForwardTo = null
                        scheduleTimer(rtData, statement, ownEvent ? rtData.event.schedule.t : 0)
                    }
	                rtData.stack.c = statement.$
					if (ownEvent) rtData.fastForwardTo = null
                    if (!!rtData.fastForwardTo || (ownEvent && allowed && !rtData.restricted)) {
                    	//we don't want to run this if there are piston restrictions in effect
                    	//we only execute the every if i = -1 (for rapid timers with large restrictions i.e. every second, but only on Mondays) we need to make sure we don't block execution while trying
                        //to find the next execution scheduled time, so we give up after too many attempts and schedule a rerun with i = -2 to give us the chance to try again at that later time
                    	if (!!rtData.fastForwardTo || (rtData.event.schedule.i == -1)) executeStatements(rtData, statement.s, true);
                        //we always exit a timer, this only runs on its own schedule, nothing else is executed
                        if (ownEvent) rtData.terminated = true
                        value = false
                        break
                    }
                    value = true
                    break
                case 'repeat':
                	//we override current condition so that child statements can cancel on it
	                rtData.stack.c = statement.$
                    if (!executeStatements(rtData, statement.s, async)) {
                        //stop processing
                        value = false
                        if (!rtData.fastForwardTo) break
                    }
                    value = true
                    perform = (evaluateConditions(rtData, statement, 'c', async) == false)
                    break
                case 'on':
                    perform = false
                    if (!rtData.fastForwardTo) {
                    	//look to see if any of the event matches
                        def deviceId = (rtData.event.device) ? hashId(rtData.event.device.id) : null
                        for (event in statement.c) {
                        	def operand = event.lo
                            if (operand && operand.t) {
                            	switch (operand.t) {
                                	case 'p':
                                    	if (!!deviceId && (rtData.event.name == operand.a) && !!operand.d && (deviceId in expandDeviceList(rtData, operand.d, true))) perform = true
                                    	break;
                                	case 'v':
                                    	if (rtData.event.name == operand.v) perform = true
                                    	break;
                                   	case 'x':
                                    	if ((rtData.event.value == operand.x) && (rtData.event.name == (operand.x.startsWith('@@') ? '@@' + handle() : rtData.instanceId))) perform = true
                                    	break;
                                }
                            }
                            if (perform) break
                        }
                    }
                    value = (!!rtData.fastForwardTo || perform) ? executeStatements(rtData, statement.s, async) : true
                    break
				case 'if':
                case 'while':
                    //check conditions for if and while
                    perform = evaluateConditions(rtData, statement, 'c', async)
                	//we override current condition so that child statements can cancel on it
	                rtData.stack.c = statement.$
                    if (!rtData.fastForwardTo && (!rtData.piston.o?.mps) && (statement.t == 'if') && (rtData.statementLevel == 1) && perform) {
                        //automatic piston state
                        rtData.state.autoNew = 'true';
                    }
                    if (perform || !!rtData.fastForwardTo) {
                        if (statement.t in ['if', 'while']) {
                            if (!executeStatements(rtData, statement.s, async)) {
                                //stop processing
                                value = false
                                if (!rtData.fastForwardTo) break
                            }
                            value = true
                            if (!rtData.fastForwardTo) break
                        }
                    }
                    if ((perform == false) || !!rtData.fastForwardTo) {
                        if (statement.t == 'if') {
                            //look for else-ifs
                            for (elseIf in statement.ei) {
                                perform = evaluateConditions(rtData, elseIf, 'c', async)
                                if (perform || !!rtData.fastForwardTo) {
                                    if (!executeStatements(rtData, elseIf.s, async)) {
                                        //stop processing
                                        value = false
                                        if (!rtData.fastForwardTo) break
                                    }
                                    value = true
                                    if (!rtData.fastForwardTo) break
                                }
                            }
                            if (!rtData.fastForwardTo && (!rtData.piston.o?.mps) && (rtData.statementLevel == 1)) {
                            	//automatic piston state
                                rtData.state.autoNew = 'false';
                            }
                            if ((!perform || !!rtData.fastForwardTo) && !executeStatements(rtData, statement.e, async)) {
                                //stop processing
                                value = false
                                if (!rtData.fastForwardTo) break
                            }
                        }
                    }
                    break
                case 'for':
                case 'each':
                    def devices = []
                    double startValue = 0
                    double endValue = 0
                    double stepValue = 1
                    if (statement.t == 'each') {
                        devices = evaluateOperand(rtData, null, statement.lo).v ?: []
                        endValue = devices.size() - 1
                    } else {
                    	startValue = evaluateScalarOperand(rtData, statement, statement.lo, null, 'decimal').v
                    	endValue = evaluateScalarOperand(rtData, statement, statement.lo2, null, 'decimal').v
                    	stepValue = evaluateScalarOperand(rtData, statement, statement.lo3, null, 'decimal').v ?: 1.0
                    }
                    String counterVariable = getVariable(rtData, statement.x).t != 'error' ? statement.x : null
                    if (((startValue <= endValue) && (stepValue > 0)) || ((startValue >= endValue) && (stepValue < 0)) || !!rtData.fastForwardTo) {
                    	//initialize the for loop
                        if (rtData.fastForwardTo) index = cast(rtData, rtData.cache["f:${statement.$}"], 'decimal')
                    	if (index == null) {
                        	index = cast(rtData, startValue, 'decimal')
	                        rtData.cache["f:${statement.$}"] = index
                        }
                        setSystemVariableValue(rtData, '$index', index)
						if ((statement.t == 'each') && !rtData.fastForward) setSystemVariableValue(rtData, '$device', (index < devices.size() ? [devices[(int) index]] : []))
                        if (counterVariable && !rtData.fastForward) setVariable(rtData, counterVariable, (statement.t == 'each') ? (index < devices.size() ? [devices[(int) index]] : []) : index)
                        //do the loop
                        perform = executeStatements(rtData, statement.s, async)
                        if (!perform) {
                            //stop processing
                            value = false
                            if (!!rtData.break) {
                                //we reached a break, so we really want to continue execution outside of the switch
                                value = true
                                rtData.break = null
                                perform = false
                            }
                            break
                        }
                        //don't do the rest if we're fast forwarding
                        if (!!rtData.fastForwardTo) break
                        index = index + stepValue
                        setSystemVariableValue(rtData, '$index', index)
						if ((statement.t == 'each') && !rtData.fastForward) setSystemVariableValue(rtData, '$device', (index < devices.size() ? [devices[(int) index]] : []))
                        if (counterVariable && !rtData.fastForward) setVariable(rtData, counterVariable, (statement.t == 'each') ? (index < devices.size() ? [devices[(int) index]] : []) : index)
                        rtData.cache["f:${statement.$}"] = index
                        if (((stepValue > 0 ) && (index > endValue)) || ((stepValue < 0 ) && (index < endValue))) {
                        	perform = false
                            break
                        }
                    }
                	break
				case 'switch':
                	def lo = [operand: statement.lo, values: evaluateOperand(rtData, statement, statement.lo)]
                    //go through all cases
                    def found = false
                    def implicitBreaks = (statement.ctp == 'i')
                    def fallThrough = !implicitBreaks
                    perform = false
                    if (rtData.logging > 2) debug "Evaluating switch with values $lo.values", rtData
                    for (_case in statement.cs) {
                    	def ro = [operand: _case.ro, values: evaluateOperand(rtData, _case, _case.ro)]
                        def ro2 = (_case.t == 'r') ? [operand: _case.ro2, values: evaluateOperand(rtData, _case, _case.ro2, null, false, true)] : null
                        perform = perform || evaluateComparison(rtData, (_case.t == 'r' ? 'is_inside_of_range' : 'is'), lo, ro, ro2)
                        found = found || perform
                        if (perform || (found && fallThrough) || !!rtData.fastForwardTo) {
	                        def fastForwardTo = rtData.fastForwardTo
                        	if (!executeStatements(rtData, _case.s, async)) {
 								//stop processing
                                value = false
                                if (!!rtData.break) {
                                	//we reached a break, so we really want to continue execution outside of the switch
                                	value = true
                                    found = true
                                    fallThrough = false
                                    rtData.break = null
                                }
                                if (!rtData.fastForwardTo) {
                                	break
                                }
							}
                            //if we determine that the fast forwarding ended during this execution, we assume found is true
                            found = found || (fastForwardTo != rtData.fastForwardTo)
                            value = true
                            //if implicit breaks
                            if (implicitBreaks && !rtData.fastForwardTo) {
                                fallThrough = false
                            	break
                            }
                        }
                    }
                    if (statement.e && statement.e.length && (value || !!rtData.fastForwardTo) && (!found || fallThrough || !!rtData.fastForwardTo)) {
                    	//no case found, let's do the default
						if (!executeStatements(rtData, statement.e, async)) {
                            //stop processing
                            value = false
                            if (!!rtData.break) {
                                //we reached a break, so we really want to continue execution outside of the switch
                                value = true
                                rtData.break = null
                            }
                            if (!rtData.fastForwardTo) break
						}
                    }
                	break
                case 'action':
                    value = executeAction(rtData, statement, async)
                    break
                case 'do':
                	value = executeStatements(rtData, statement.s, async)
                    break
                case 'break':
                	if (!rtData.fastForwardTo) {
                		rtData.break = true
					}
                    value = false
                    break
                case 'exit':
                	if (!rtData.fastForwardTo) {
                		vcmd_setState(rtData, null, [cast(rtData, evaluateOperand(rtData, null, statement.lo).v, 'string')])
                     	rtData.terminated = true
                    }
                    value = false
                    break
            }
            //break the loop
            if (rtData.fastForwardTo || (statement.t == 'if')) perform = false

            //is this statement a loop
            def loop = (statement.t in ['while', 'repeat', 'for', 'each'])
            if (loop && !value && !!rtData.break) {
            	//someone requested a break from the loop, we're doing it
            	rtData.break = false
                //but we're allowing the rest to continue
                value = true
                perform = false
            }
            //do we repeat the loop?
            repeat = perform && value && loop && !rtData.fastForwardTo
        }
    }
	if (!rtData.fastForwardTo) {
    	def schedule = (statement.t == 'every') ? (rtData.schedules.find{ it.s == statement.$} ?: state.schedules.find{ it.s == statement.$ }) : null
        if (schedule) {
        	//timers need to show the remaining time
    		tracePoint(rtData, "s:${statement.$}", now() - t, now() - schedule.t)
        } else {
    		tracePoint(rtData, "s:${statement.$}", now() - t, value)
        }
    }
	//if (statement.a == '1') {
		//when an async action requests the thread termination, we continue to execute the parent
        //when an async action terminates as a result of a time event, we exit completely
//		value = (rtData.event.name != 'time')
	//}
    if (selfAsync) {
    	//if running in async mode, we return true (to continue execution)
    	value = !rtData.resumed
        rtData.resumed = false
    }
    if (rtData.terminated) {
    	value = false
    }
    //restore current condition
    rtData.stack.c = c
    if (stacked) {
        rtData.stack.cs.pop()
    }
    rtData.stack.s = rtData.stack.ss.pop()
    setSystemVariableValue(rtData, '$index', parentIndex)
    setSystemVariableValue(rtData, '$device', parentDevice)
    rtData.conditionStateChanged = parentConditionStateChanged
	return value || !!rtData.fastForwardTo
}


private Boolean executeAction(rtData, statement, async) {
	def parentDevicesVar = rtData.systemVars['$devices'].v
	//def devices = []
    //def deviceIds = []
    //if override
    if (!rtData.fastForwardTo && (statement.tsp != 'a')) {
    	cancelStatementSchedules(rtData, statement.$)
    }
    def result = true
    List deviceIds = expandDeviceList(rtData, statement.d)
    List devices = deviceIds.collect{ getDevice(rtData, it) }
    /*
    for (d in statement.d) {
    	if (d.startsWith(':')) {
    		def device = getDevice(rtData, d)
        	if (device) {
	        	devices.push(device)
                deviceIds.push(d)
	        }
        } else {
        	//we're dealing with a variable, let's get the list of devices from it
        	def var = getVariable(rtData, d)
            if (var.t == 'device') {
            	for (vd in var.v) {
                    def device = getDevice(rtData, vd)
                    if (device) {
                        devices.push(device)
                		deviceIds.push(vd)
                    }
                }
            }
        }
    }
    */
    rtData.currentAction = statement
    for (task in statement.k) {
	    if (task.$ == rtData.fastForwardTo) {
        	//resuming a waiting task, we need to bring back the devices
            if (rtData.event && rtData.event.schedule && rtData.event.schedule.stack) {
	            setSystemVariableValue(rtData, '$index', rtData.event.schedule.stack.index)
    	        setSystemVariableValue(rtData, '$device', rtData.event.schedule.stack.device)
                if (rtData.event.schedule.stack.devices instanceof List) {
	        	    setSystemVariableValue(rtData, '$devices', rtData.event.schedule.stack.devices)
    	            deviceIds = rtData.event.schedule.stack.devices
        	        devices = deviceIds.collect{ getDevice(rtData, it) }
                }
            }
		}
		rtData.systemVars['$devices'].v = deviceIds
        result = executeTask(rtData, devices, statement, task, async)
        if (!result && !rtData.fastForwardTo) {
        	break
        }
    }
    rtData.systemVars['$devices'].v = parentDevicesVar
    return result
}

private Boolean executeTask(rtData, devices, statement, task, async) {
    //parse parameters
   	def virtualDevice = devices.size() ? null : location
    def t = now()
    if (rtData.fastForwardTo) {
	    if (task.$ == rtData.fastForwardTo) {
    		//finally found the resuming point, play nicely from hereon
            tracePoint(rtData, "t:${task.$}", now() - t, null)
    		rtData.fastForwardTo = null
            //restore $device and $devices
            rtData.resumed = true
        }
       	//we're not doing anything, we're fast forwarding...
       	return true
    }
    if (task.m && (task.m instanceof List) && (task.m.size())) {
    	if (!(rtData.locationModeId in task.m)) {
        	if (rtData.logging > 2) debug "Skipping task ${task.$} because of mode restrictions", rtData
        	return true;
        }
    }
    def params = []
    for (param in task.p) {
    	def p
    	switch (param.vt) {
        	case 'variable':
            	p = param.x instanceof List ? param.x : (param.x + (param.xi != null ? '[' + param.xi + ']' : ''));
                break;
            default:
            	def v = evaluateOperand(rtData, null, param)
                //if not selected, we want to return null
                p = (v.v != null) ? evaluateExpression(rtData, v, param.vt).v : null
        }
        //ensure value type is successfuly passed through
		params.push p
    }

 	def vcmd = rtData.commands.virtual[task.c]
    long delay = 0
    for (device in (virtualDevice ? [virtualDevice] : devices)) {
        if (!virtualDevice && device.hasCommand(task.c)) {
            def msg = timer "Executed [$device].${task.c}"
        	try {
            	delay = "cmd_${task.c}"(rtData, device, params)
            } catch(all) {
	            executePhysicalCommand(rtData, device, task.c, params)
			}
            if (rtData.logging > 1) trace msg, rtData
        } else {
            if (vcmd) {
	        	delay = executeVirtualCommand(rtData, vcmd.a ? devices : device, task, params)
                //aggregate commands only run once, for all devices at the same time
                if (vcmd.a) break
            }
        }
    }
    //if we don't have to wait, we're home free
    if (delay) {
    	//get remaining piston time
    	def timeLeft = 20000 + rtData.timestamp - now()
        //negative delays force us to reschedule, no sleeping on this one
        boolean reschedule = (delay < 0)
        delay = reschedule ? -delay : delay
    	//we're aiming at waking up with at least 10s left
    	if (reschedule || (timeLeft - delay < 10000) || (delay >= 5000) || async) {
	        //schedule a wake up
	        if (rtData.logging > 1) trace "Requesting a wake up for ${formatLocalTime(now() + delay)} (in ${cast(rtData, delay / 1000, 'decimal')}s)", rtData
            tracePoint(rtData, "t:${task.$}", now() - t, -delay)
            requestWakeUp(rtData, statement, task, delay, task.c)
	        return false
	    } else {
	        if (rtData.logging > 1) trace "Waiting for ${delay}ms", rtData
	        pause(delay)
	    }
	}
	tracePoint(rtData, "t:${task.$}", now() - t, delay)
    return true
}

private long executeVirtualCommand(rtData, devices, task, params)
{
	def msg = timer "Executed virtual command ${devices ? (devices instanceof List ? "$devices." : "[$devices].") : ""}${task.c}"
    long delay = 0
    try {
		delay = "vcmd_${task.c}"(rtData, devices, params)
	    if (rtData.logging > 1) trace msg, rtData
    } catch(all) {
    	msg.m = "Error executing virtual command ${devices instanceof List ? "$devices" : "[$devices]"}.${task.c}:"
        msg.e = all
        error msg, rtData
    }
    return delay
}

private executePhysicalCommand(rtData, device, command, params = [], delay = null, scheduleDevice = null, disableCommandOptimization = false) {
	if (!!delay && !!scheduleDevice) {
    	//we're using schedules instead
        def statement = rtData.currentAction
    	def cs = [] + ((statement.tcp == 'b') || (statement.tcp == 'c') ? (rtData.stack?.cs ?: []) : [])
        def ps = (statement.tcp == 'b') || (statement.tcp == 'p') ? 1 : 0
        cs.removeAll{ it == 0 }
        def schedule = [
            t: now() + delay,
            s: statement.$,
            i: -3,
            cs: cs,
            ps: ps,
            d: [
            	d: scheduleDevice,
                c: command,
                p: params
            ]
        ]
        rtData.schedules.push(schedule)
    } else {
        try {
            params = (params instanceof List) ? params : (params != null ? [params] : [])
            //cleanup the params so that SONOS works
            while (params.size() && (params[params.size()-1] == null)) params.pop()
            def msg = timer ""
            def skip = false
            if (!rtData.piston.o?.dco && !disableCommandOptimization && !(command in ['setColorTemperature', 'setColor', 'setHue', 'setSaturation'])) {
                def cmd = rtData.commands.physical[command]
                if (cmd && cmd.a) {
                    if (cmd.v && !params.size()) {
                        //commands with no parameter that set an attribute to a preset value
                        if (getDeviceAttributeValue(rtData, device, cmd.a) == cmd.v) {
                            skip = true
                        }
                    } else if (params.size() == 1) {
                        if (getDeviceAttributeValue(rtData, device, cmd.a) == params[0]) {
                            skip = (command in ['setLevel', 'setInfraredLevel'] ? getDeviceAttributeValue(rtData, device, 'switch') == 'on' : true)
                        }
                    }
                }
            }
            //if we're skipping, we already have a message
            if (skip) {
            	msg.m = "Skipped execution of physical command [${device.label}].$command($params) because it would make no change to the device."
            } else {
                if (params.size()) {
                    if (delay) {
                        device."$command"((params as Object[]) + [delay: delay])
                        msg.m = "Executed physical command [${device.label}].$command($params, [delay: $delay])"
                    } else {
                        device."$command"(params as Object[])
                        msg.m = "Executed physical command [${device.label}].$command($params)"
                    }
                } else {
                    if (delay) {
                        device."$command"([delay: delay])
                        msg.m = "Executed physical command [${device.label}].$command([delay: $delay])"
                    } else {
                        device."$command"()
                        msg.m = "Executed physical command [${device.label}].$command()"
                    }
                }
            }
            if (rtData.logging > 2) debug msg, rtData
        } catch(all) {
            error "Error while executing physical command $device.$command($params):", rtData, null, all
        }
        if (rtData.piston.o?.ced) {
            pause(rtData.piston.o.ced)
            if (rtData.logging > 2) debug "Injected a ${rtData.piston.o.ced}ms delay after [$device].$command(${params ? "$params" : ''})", rtData
        }
    }
}


private scheduleTimer(rtData, timer, long lastRun = 0) {
	//if already scheduled once during this run, don't do it again
    if (rtData.schedules.find{ it.s == timer.$ }) return
	//complicated stuff follows...
    def t = now()
    def interval = "${evaluateOperand(rtData, null, timer.lo).v}"
    if (!interval.isInteger()) return
    interval = interval.toInteger()
    if (interval <= 0) return
    def intervalUnit = timer.lo.vt

    int level = 0
    switch(intervalUnit) {
        case 'ms': level = 1; break;
        case 's':  level = 2; break;
        case 'm':  level = 3; break;
        case 'h':  level = 4; break;
        case 'd':  level = 5; break;
        case 'w':  level = 6; break;
        case 'n':  level = 7; break;
        case 'y':  level = 8; break;
    }

    long delta = 0
    long time = 0
    switch (intervalUnit) {
    	case 'ms': delta = 1; break;
    	case 's': delta = 1000; break;
    	case 'm': delta = 60000; break;
    	case 'h': delta = 3600000; break;
    }

    if (!delta) {
    	//let's get the offset
        time = evaluateExpression(rtData, evaluateOperand(rtData, null, timer.lo2), 'datetime').v
        if (timer.lo2.t != 'c') {
        	def offset = evaluateOperand(rtData, null, timer.lo3)
        	time += (long) evaluateExpression(rtData, [t: 'duration', v: offset.v, vt: offset.vt], 'long').v
        }
        //resulting time is in UTC
        if (!lastRun) {
        	//first run, just adjust the time so we're in the future
            while (time <= now()) {
            	//add days to bring it to next occurrence
                //we need to go through local time to support DST
                time = localToUtcTime(utcToLocalTime(time) + 86400000)
            }
        }
    }
    delta = delta * interval
    def priorActivity = !!lastRun


    //switch to local date/times
    time = utcToLocalTime(time)
    long rightNow = utcToLocalTime(now())
    lastRun = lastRun ? utcToLocalTime(lastRun) : rightNow
    long nextSchedule = lastRun

    if (lastRun > rightNow) {
    	//sometimes ST runs timers early, so we need to make sure we're at least in the near future
    	rightNow = lastRun + 1
    }

    if (intervalUnit == 'h') {
    	long min = cast(rtData, timer.lo.om, 'long')
    	nextSchedule = (long) 3600000 * Math.floor(nextSchedule / 3600000) + (min * 60000)
    }

    //next date
	int cycles = 100
    while (cycles) {
    	if (delta) {
        	if (nextSchedule < (rightNow - delta)) {
            	//we're behind, let's fast forward to where the next occurrence happens in the future
                def count = Math.floor((rightNow - nextSchedule) / delta)
                //if (rtData.logging > 2) debug "Timer fell behind by $count interval${count > 1 ? 's' : ''}, catching up...", rtData
               	nextSchedule = nextSchedule + delta * count
            }
	    	nextSchedule = nextSchedule + delta
	    } else {

            //advance one day if we're in the past
            while (time < rightNow) time += 86400000
            long lastDay = Math.floor(nextSchedule / 86400000)
            long thisDay = Math.floor(time / 86400000)

	    	//the repeating interval is not necessarily constant
            switch (intervalUnit) {
            	case 'd':
                	if (priorActivity) {
                    	//add the required number of days
                    	nextSchedule = time + 86400000 * (interval - (thisDay - lastDay))
                    } else {
                    	nextSchedule = time
                    }
                    break
            	case 'w':
                	//figure out the first day of the week matching the requirement
					long currentDay = new Date(time).day
	    			long requiredDay = cast(rtData, timer.lo.odw, 'long')
                    if (currentDay > requiredDay) requiredDay += 7
                    //move to first matching day
                    nextSchedule = time + 86400000 * (requiredDay - currentDay)
                    if (nextSchedule < rightNow) {
                    	nextSchedule += 604800000 * interval
                    }
                    break
            	case 'n':
            	case 'y':
                	//figure out the first day of the week matching the requirement
                    int odm = timer.lo.odm.toInteger()
                    def odw = timer.lo.odw
                    def omy = intervalUnit == 'y' ? timer.lo.omy.toInteger() : 0
                    int day = 0
                    def date = new Date(time)
                    int year = date.year
                    int month = (intervalUnit == 'n' ? date.month : omy) + (priorActivity ? interval : ((nextSchedule < rightNow) ? 1 : 0)) * (intervalUnit == 'n' ? 1 : 12)
                    if (month >= 12) {
                        year += Math.floor(month / 12)
                        month = month.mod(12)
                    }
                    date.setDate(1)
                    date.setMonth(month)
                    date.setYear(year)
                    def lastDayOfMonth = (new Date(date.year, date.month + 1, 0)).date
                    if (odw == 'd') {
						if (odm > 0) {
                        	day = (odm <= lastDayOfMonth) ? odm : 0
                        } else {
                        	day = lastDayOfMonth + 1 + odm
                        	day = (day >= 1) ? day : 0
                        }
                    } else {
                    	odw = odw.toInteger()
                    	//find the nth week day of the month
                        if (odm > 0) {
                            //going forward
                            def firstDayOfMonthDOW = (new Date(date.year, date.month, 1)).day
                            //find the first matching day
                            def firstMatch = 1 + odw - firstDayOfMonthDOW + (odw < firstDayOfMonthDOW ? 7 : 0)
                            day = firstMatch + 7 * (odm - 1)
                            day = (result <= lastDayOfMonth) ? day : 0
                        } else {
                            //going backwards
                            def lastDayOfMonthDOW = (new Date(date.year, date.month + 1, 0)).day
                            //find the first matching day
                            def firstMatch = lastDayOfMonth + odw - lastDayOfMonthDOW - (odw > lastDayOfMonthDOW ? 7 : 0)
                            day = firstMatch + 7 * (odm + 1)
                            day = (day >= 1) ? day : 0
                        }
                    }
                    if (day) {
                    	date.setDate(day)
                        nextSchedule = date.time
                    }
					break
            }
    	}
        //check to see if it fits the restrictions
        if (nextSchedule >= rightNow) {
        	long offset = checkTimeRestrictions(rtData, timer.lo, nextSchedule, level, interval)
            if (!offset) break
            if (offset > 0) nextSchedule += offset
        }
        time = nextSchedule
        priorActivity = true
        cycles -= 1
    }

    if (nextSchedule > lastRun) {
    	//convert back to UTC
    	nextSchedule = localToUtcTime(nextSchedule)
    	rtData.schedules.removeAll{ it.s == timer.$ }
        requestWakeUp(rtData, timer, [$: -1], nextSchedule)
    }

}


private scheduleTimeCondition(rtData, condition) {
	//if already scheduled once during this run, don't do it again
    if (rtData.schedules.find{ (it.s == condition.$) && (it.i == 0) }) return
	def comparison = rtData.comparisons.conditions[condition.co]
    def trigger = false
    if (!comparison) {
		comparison = rtData.comparisons.triggers[condition.co]
	    if (!comparison) return
    	trigger = true
    }
    cancelStatementSchedules(rtData, condition.$)
    if (!comparison.p) return
    def tv1 = condition.ro && (condition.ro.t != 'c') ? evaluateOperand(rtData, null, condition.to) : null
    def v1 = evaluateExpression(rtData, evaluateOperand(rtData, null, condition.ro), 'datetime').v + (tv1 ? evaluateExpression(rtData, [t: 'duration', v: tv1.v, vt: tv1.vt], 'long').v : 0)
    def tv2 = condition.ro2 && (condition.ro2.t != 'c') && (comparison.p > 1) ? evaluateOperand(rtData, null, condition.to2) : null
    def v2 = trigger ? v1 : ((comparison.p > 1) ? (evaluateExpression(rtData, evaluateOperand(rtData, null, condition.ro2, null, false, true), 'datetime').v + (tv2 ? evaluateExpression(rtData, [t: 'duration', v: tv2.v, vt: tv2.vt]).v : 0)) : (condition.lo.v == 'time' ? getMidnightTime(rtData) : v1))
    def n = now() + 2000
    if (condition.lo.v == 'time') {
    	//v1 = (v1 % 86400000) + getMidnightTime()
    	//v2 = (v2 % 86400000) + getMidnightTime()
	    while (v1 < n) v1 += 86400000
    	while (v2 < n) v2 += 86400000
/*        int cnt = 100
        error "checking restrictions for $condition.lo", rtData
        while (cnt) {
        	//repeat until we find a day that's matching the restrictions
	    	n = v1 < v2 ? v1 : v2
			if (checkTimeRestrictions(rtData, condition.lo, n, 5, 1) == 0) break
            long n2 = localToUtcTime(utcToLocalTime(n) + 86400000)
            error "adding a day, $n >>> $n2", rtData
            v1 = (v1 == n) ? n2 : v1
            v2 = (v2 == n) ? n2 : v2
            cnt = cnt - 1
        }*/
    }
    //figure out the next time
    v1 = (v1 < n) ? v2 : v1
    v2 = (v2 < n) ? v1 : v2
   	n = v1 < v2 ? v1 : v2
    if (n > now()) {
    	if (rtData.logging > 2) debug "Requesting time schedule wake up at ${formatLocalTime(n)}", rtData
	    requestWakeUp(rtData, condition, [$:0], n)
    }
}

private Long checkTimeRestrictions(Map rtData, Map operand, long time, int level, int interval) {
	//returns 0 if restrictions are passed
    //returns a positive number as millisecond offset to apply to nextSchedule for fast forwarding
    //returns a negative number as a failed restriction with no fast forwarding offset suggestion

	List om = (level <= 2) && (operand.om instanceof List) && operand.om.size() ? operand.om : null;
    List oh = (level <= 3) && (operand.oh instanceof List) && operand.oh.size() ? operand.oh : null;
    List odw = (level <= 5) && (operand.odw instanceof List) && operand.odw.size() ? operand.odw : null;
    List odm = (level <= 6) && (operand.odm instanceof List) && operand.odm.size() ? operand.odm : null;
    List owm = (level <= 6) && !odm && (operand.owm instanceof List) && operand.owm.size() ? operand.owm : null;
    List omy = (level <= 7) && (operand.omy instanceof List) && operand.omy.size() ? operand.omy : null;


	if (!om && !oh && !odw && !odm && !owm && !omy) return 0
	def date = new Date(time)

    long result = -1
    //month restrictions
    if (omy && (omy.indexOf(date.month + 1) < 0)) {
    	int month = (omy.sort{ it }.find{ it > date.month + 1 } ?: 12 + omy.sort{ it }[0]) - 1
        int year = date.year + (month >= 12 ? 1 : 0)
        month = (month >= 12 ? month - 12 : month)
        def ms = (new Date(year, month, 1)).time - time
    	switch (level) {
        	case 2: //by second
          	    result = interval * (Math.floor(ms / 1000 / interval) - 2) * 1000
                break
        	case 3: //by minute
          	    result = interval * (Math.floor(ms / 60000 / interval) - 2) * 60000
                break
        }
		return (result > 0) ? result : -1
    }

   	//week of month restrictions
    if (owm) {
		if (!((owm.indexOf(getWeekOfMonth(date)) >= 0) || (owm.indexOf(getWeekOfMonth(date, true)) >= 0))) {
            switch (level) {
                case 2: //by second
                result = interval * (Math.floor(((7 - date.day) * 86400 - date.hours * 3600 - date.minutes * 60) / interval) - 2) * 1000
                break
                case 3: //by minute
                result = interval * (Math.floor(((7 - date.day) * 1440 - date.hours * 60 - date.minutes) / interval) - 2) * 60000
                break
            }
            return (result > 0) ? result : -1
        }
    }

   	//day of month restrictions
    if (odm) {
		if (odm.indexOf(date.date) < 0) {
            def lastDayOfMonth = (new Date(date.year, date.month + 1, 0)).date
            if (odm.find{ it < 1 }) {
                //we need to add the last days
                odm = [] + odm //copy the array
                if (odm.indexOf(-1) >= 0) odm.push(lastDayOfMonth)
                if (odm.indexOf(-2) >= 0) odm.push(lastDayOfMonth - 1)
                if (odm.indexOf(-3) >= 0) odm.push(lastDayOfMonth - 2)
                odm.removeAll{ it < 1 }
            }
            switch (level) {
                case 2: //by second
                result = interval * (Math.floor((((odm.sort{ it }.find{ it > date.date } ?: lastDayOfMonth + odm.sort{ it }[0]) - date.date) * 86400 - date.hours * 3600 - date.minutes * 60) / interval) - 2) * 1000
                break
                case 3: //by minute
                result = interval * (Math.floor((((odm.sort{ it }.find{ it > date.date } ?: lastDayOfMonth + odm.sort{ it }[0]) - date.date) * 1440 - date.hours * 60 - date.minutes) / interval) - 2) * 60000
                break
            }
            return (result > 0) ? result : -1
        }
    }

	//day of week restrictions
    if (odw && (odw.indexOf(date.day) < 0)) {
    	switch (level) {
        	case 2: //by second
          	    result = interval * (Math.floor((((odw.sort{ it }.find{ it > date.day } ?: 7 + odw.sort{ it }[0]) - date.day) * 86400 - date.hours * 3600 - date.minutes * 60) / interval) - 2) * 1000
                break
        	case 3: //by minute
          	    result = interval * (Math.floor((((odw.sort{ it }.find{ it > date.day } ?: 7 + odw.sort{ it }[0]) - date.day) * 1440 - date.hours * 60 - date.minutes) / interval) - 2) * 60000
                break
        }
		return (result > 0) ? result : -1
    }

    //hour restrictions
    if (oh && (oh.indexOf(date.hours) < 0)) {
    	switch (level) {
        	case 2: //by second
          	    result = interval * (Math.floor((((oh.sort{ it }.find{ it > date.hours } ?: 24 + oh.sort{ it }[0]) - date.hours) * 3600 - date.minutes * 60) / interval) - 2) * 1000
                break
        	case 3: //by minute
          	    result = interval * (Math.floor((((oh.sort{ it }.find{ it > date.hours } ?: 24 + oh.sort{ it }[0]) - date.hours) * 60 - date.minutes) / interval) - 2) * 60000
                break
        }
		return (result > 0) ? result : -1
    }

    //minute restrictions
    if (om && (om.indexOf(date.minutes) < 0)) {
    	//get the next highest minute
        //suggest an offset to reach the next minute
    	result = interval * (Math.floor(((om.sort{ it }.find{ it > date.minutes } ?: 60 + om.sort{ it }[0]) - date.minutes - 1) * 60 / interval) - 2) * 1000
		return (result > 0) ? result : -1
    }
    return 0
}


//return the number of occurrences of same day of week up until the date or from the end of the month if backwards, i.e. last Sunday is -1, second-last Sunday is -2
private int getWeekOfMonth(date = null, backwards = false) {
	def day = date.date
	if (backwards) {
		def month = date.month
		def year = date.year
		def lastDayOfMonth = (new Date(year, month + 1, 0)).date
		return -(1 + Math.floor((lastDayOfMonth - day) / 7))
	} else {
		return 1 + Math.floor((day - 1) / 7) //1 based
	}
}


private requestWakeUp(rtData, statement, task, timeOrDelay, data = null) {
	def time = timeOrDelay > 9999999999 ? timeOrDelay : now() + timeOrDelay
    def cs = [] + ((statement.tcp == 'b') || (statement.tcp == 'c') ? (rtData.stack?.cs ?: []) : [])
    def ps = (statement.tcp == 'b') || (statement.tcp == 'p') ? 1 : 0
    cs.removeAll{ it == 0 }
    def schedule = [
    	t: time,
        s: statement.$,
        i: task?.$,
        cs: cs,
        ps: ps,
        d: data,
        evt: rtData.currentEvent,
        args: rtData.args,
        stack: [
        	index: getVariable(rtData, '$index').v,
        	device: getVariable(rtData, '$device').v,
        	devices: getVariable(rtData, '$devices').v,
        	json: rtData.json ?: [:],
            response: rtData.response ?: [:]
        ]
    ]
    rtData.schedules.push(schedule)
}


private long cmd_setLevel(rtData, device, params) {
	def level = params[0]
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    executePhysicalCommand(rtData, device, 'setLevel', level, delay)
    return 0
}

private long cmd_setInfraredLevel(rtData, device, params) {
	def level = params[0]
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    executePhysicalCommand(rtData, device, 'setInfraredLevel', level, delay)
    return 0
}

private long cmd_setHue(rtData, device, params) {
	int hue = cast(rtData, params[0] / 3.6, 'integer')
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    executePhysicalCommand(rtData, device, 'setHue', hue, delay)
    return 0
}

private long cmd_setSaturation(rtData, device, params) {
	def saturation = params[0]
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    executePhysicalCommand(rtData, device, 'setSaturation', saturation, delay)
    return 0
}

private long cmd_setColorTemperature(rtData, device, params) {
	def colorTemperature = params[0]
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    executePhysicalCommand(rtData, device, 'setColorTemperature', colorTemperature, delay)
    return 0
}

private getColor(colorValue) {
    def color = (colorValue == 'Random') ? colorUtil?.RANDOM : colorUtil?.findByName(colorValue)
    if (color) {
		color = [
        	hex: color.rgb,
        	hue: Math.round(color.h / 3.6),
        	saturation: color.s,
        	level: color.l
    	]
    } else {
    	color = hexToColor(colorValue)
        if (color) {
            color = [
                hex: color.hex,
                hue: color.hue,
                saturation: color.saturation,
                level: color.level
            ]
        }
    }
    return color
}

private long cmd_setColor(rtData, device, params) {
	def color = getColor(params[0])
    if (!color) {
    	error "ERROR: Invalid color $params", rtData
        return 0
    }
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    executePhysicalCommand(rtData, device, 'setColor', color, delay)
    return 0
}

private long cmd_setAdjustedColor(rtData, device, params) {
    def color = getColor(params[0])
    if (!color) {
    	error "ERROR: Invalid color $params", rtData
        return 0
    }
    def duration = cast(rtData, params[1], 'long')
    def state = params.size() > 2 ? params[2] : ""
    def delay = params.size() > 3 ? params[3] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    executePhysicalCommand(rtData, device, 'setAdjustedColor', [color, duration], delay)
    return 0
}

private long cmd_setAdjustedHSLColor(rtData, device, params) {
    def hue = cast(rtData, params[0] / 3.6, 'integer')
    def saturation = params[1]
    def level = params[2]
    def color = [
        hue: hue,
        saturation: saturation,
        level: level
    ]
    def duration = cast(rtData, params[3], 'long')
    def state = params.size() > 4 ? params[4] : ""
    def delay = params.size() > 5 ? params[5] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    executePhysicalCommand(rtData, device, 'setAdjustedColor', [color, duration], delay)
    return 0
}

private long cmd_setLoopDuration(rtData, device, params) {
    int duration = (int) Math.round(cast(rtData, params[0], 'long') / 1000)
    executePhysicalCommand(rtData, device, 'setLoopDuration', duration, delay)
    return 0
}

private long cmd_setVideoLength(rtData, device, params) {
    int duration = (int) Math.round(cast(rtData, params[0], 'long') / 1000)
    executePhysicalCommand(rtData, device, 'setVideoLength', duration, delay)
    return 0
}


private long vcmd_log(rtData, device, params) {
	def command = params[0]
	def message = params[1]
	log message, rtData, null, null, "${command}".toLowerCase().trim(), true
    def save = params.size() > 2 ? !!params[2] : false
	if (save) {
		sendNotificationEvent(message)
	}
    return 0
}

private long vcmd_setState(rtData, device, params) {
	def value = params[0]
    if (rtData.piston.o?.mps) {
    	rtData.state.new = value
    	rtData.pistonStateChanged = rtData.pistonStateChanged || (rtData.state.old != rtData.state.new)
        setSystemVariableValue(rtData, '$state', rtData.state.new)
    } else {
	    error "Cannot set the piston state while in automatic mode. Please edit the piston settings to disable the automatic piston state if you want to manually control the state.", rtData
    }
    return 0
}

private long vcmd_setTileColor(rtData, device, params) {
	int index = cast(rtData, params[0], 'integer')
    if ((index < 1) || (index > 16)) return 0
    rtData.state["c$index"] = getColor(params[1])?.hex
    rtData.state["b$index"] = getColor(params[2])?.hex
    rtData.state["f$index"] = !!params[3]
    return 0
}

private long vcmd_setTileTitle(rtData, device, params) {
	int index = cast(rtData, params[0], 'integer')
    if ((index < 1) || (index > 16)) return 0
   	rtData.state["i$index"] = params[1]
    return 0
}

private long vcmd_setTileText(rtData, device, params) {
	int index = cast(rtData, params[0], 'integer')
    if ((index < 1) || (index > 16)) return 0
	rtData.state["t$index"] = params[1]
    return 0
}

private long vcmd_setTileFooter(rtData, device, params) {
	int index = cast(rtData, params[0], 'integer')
    if ((index < 1) || (index > 16)) return 0
   	rtData.state["o$index"] = params[1]
    return 0
}

private long vcmd_setTile(rtData, device, params) {
	int index = cast(rtData, params[0], 'integer')
    if ((index < 1) || (index > 16)) return 0;
   	rtData.state["i$index"] = params[1]
   	rtData.state["t$index"] = params[2]
   	rtData.state["o$index"] = params[3]
    rtData.state["c$index"] = getColor(params[4])?.hex
    rtData.state["b$index"] = getColor(params[5])?.hex
    rtData.state["f$index"] = !!params[6]
    return 0
}

private long vcmd_clearTile(rtData, device, params) {
	int index = cast(rtData, params[0], 'integer')
    if ((index < 1) || (index > 16)) return 0;
   	rtData.state["i$index"] = ''
   	rtData.state["t$index"] = ''
    rtData.state["c$index"] = ''
    rtData.state["o$index"] = ''
    rtData.state["b$index"] = ''
    rtData.state["f$index"] = ''
    return 0
}


private long vcmd_setLocationMode(rtData, device, params) {
	def modeIdOrName = params[0]
    def mode = location.getModes()?.find{ (hashId(it.id) == modeIdOrName) || (it.name == modeIdOrName)}
    if (mode) {
    	location.setMode(mode.name)
    } else {
	    error "Error setting location mode. Mode '$modeIdOrName' does not exist.", rtData
    }
    return 0
}

private long vcmd_setAlarmSystemStatus(rtData, device, params) {
	def statusIdOrName = params[0]
    def status = rtData.virtualDevices['alarmSystemStatus']?.o?.find{ (it.key == statusIdOrName) || (it.value == statusIdOrName)}.collect{ [id: it.key, name: it.value] }
    if (status && status.size()) {
	    sendLocationEvent(name: 'alarmSystemStatus', value: status[0].id)
    } else {
	    error "Error setting SmartThings Home Monitor status. Status '$statusIdOrName' does not exist.", rtData
    }
    return 0
}

private long vcmd_sendEmail(rtData, device, params) {
	def data = [
    	i: hashId(app.id),
        n: app.label,
        t: params[0],
        s: params[1],
        m: params[2]
    ]

	def requestParams = [
		uri:  "https://api.webcore.co/email/send/${rtData.locationId}",
		query: null,
		requestContentType: "application/json",
		body: data
	]
    def success = false
    def msg = 'Unknown error'
	httpPost(requestParams) { response ->
    	if (response.status == 200) {
			def jsonData = response.data instanceof Map ? response.data : (LinkedHashMap) new groovy.json.JsonSlurper().parseText(response.data)
            if (jsonData) {
            	if (jsonData.result == 'OK') {
            		success = true
                } else {
                	msg = jsonData.result.replace('ERROR ', '')
                }
            }
        }
	}
    if (!success) {
	    error "Error sending email to ${data.t}: $msg", rtData
    }
    return 0
}

private long vcmd_noop(rtData, device, params) {
	return 0
}

private long vcmd_wait(rtData, device, params) {
	return cast(rtData, params[0], 'long')
}

private long vcmd_waitRandom(rtData, device, params) {
	def min = cast(rtData, params[0], 'long')
    def max = cast(rtData, params[1], 'long')
    if (max < min) {
    	def v = max
        max = min
        min = v
    }
	return min + (int)Math.round((max - min) * Math.random())
}

private long vcmd_waitForTime(rtData, device, params) {
	long time = now()
    time = cast(rtData, cast(rtData, params[0], 'time'), 'datetime', 'time')
    long rightNow = now()
    while (time < rightNow) time += 86400000
    return time - rightNow
}

private long vcmd_waitForDateTime(rtData, device, params) {
	long time = cast(rtData, params[0], 'datetime')
    long rightNow = now()
    return (time > rightNow) ? time - rightNow : 0
}

private long vcmd_setSwitch(rtData, device, params) {
	if (cast(rtData, params[0], 'boolean')) {
	    executePhysicalCommand(rtData, device, 'on')
    } else {
	    executePhysicalCommand(rtData, device, 'off')
    }
    return 0
}

private long vcmd_toggle(rtData, device, params) {
	if (getDeviceAttributeValue(rtData, device, 'switch') == 'off') {
	    executePhysicalCommand(rtData, device, 'on')
    } else {
	    executePhysicalCommand(rtData, device, 'off')
    }
    return 0
}

private long vcmd_toggleRandom(rtData, device, params) {
	int probability = cast(rtData, params.size() == 1 ? params[0] : 50, 'integer')
    if (probability <= 0) probability = 50
	if (Math.round(100 * Math.random()) <= probability) {
	    executePhysicalCommand(rtData, device, 'on')
    } else {
	    executePhysicalCommand(rtData, device, 'off')
    }
    return 0
}

private long vcmd_toggleLevel(rtData, device, params) {
	def level = params[0]
	if (getDeviceAttributeValue(rtData, device, 'level') == level) {
	    executePhysicalCommand(rtData, device, 'setLevel', 0)
    } else {
	    executePhysicalCommand(rtData, device, 'setLevel', level)
    }
    return 0
}

private long vcmd_adjustLevel(rtData, device, params) {
	int level = cast(rtData, params[0], 'integer')
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    level = level + cast(rtData, getDeviceAttributeValue(rtData, device, 'level'), 'integer')
    level = (level < 0) ? 0 : ((level > 100) ? 100 : level)
    executePhysicalCommand(rtData, device, 'setLevel', level, delay)
    return 0
}

private long vcmd_adjustInfraredLevel(rtData, device, params) {
	int infraredLevel = cast(rtData, params[0], 'integer')
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    infraredLevel = infraredLevel + cast(rtData, getDeviceAttributeValue(rtData, device, 'infraredLevel'), 'integer')
    infraredLevel = (infraredLevel < 0) ? 0 : ((infraredLevel > 100) ? 100 : infraredLevel)
    executePhysicalCommand(rtData, device, 'setInfraredLevel', infraredLevel, delay)
    return 0
}

private long vcmd_adjustSaturation(rtData, device, params) {
	int saturation = cast(rtData, params[0], 'integer')
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    saturation = saturation + cast(rtData, getDeviceAttributeValue(rtData, device, 'saturation'), 'integer')
    saturation = (saturation < 0) ? 0 : ((saturation > 100) ? 100 : saturation)
    executePhysicalCommand(rtData, device, 'setSaturation', saturation, delay)
    return 0
}

private long vcmd_adjustHue(rtData, device, params) {
	int hue = cast(rtData, params[0] / 3.6, 'integer')
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    hue = hue + cast(rtData, getDeviceAttributeValue(rtData, device, 'hue'), 'integer')
    hue = (hue < 0) ? 0 : ((hue > 100) ? 100 : hue)
    executePhysicalCommand(rtData, device, 'setHue', hue, delay)
    return 0
}

private long vcmd_adjustColorTemperature(rtData, device, params) {
	int colorTemperature = cast(rtData, params[0], 'integer')
    def state = params.size() > 1 ? params[1] : ""
    def delay = params.size() > 2 ? params[2] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    colorTemperature = colorTemperature + cast(rtData, getDeviceAttributeValue(rtData, device, 'colorTemperature'), 'integer')
    colorTemperature = (colorTemperature < 1000) ? 1000 : ((colorTemperature > 30000) ? 30000 : colorTemperature)
    executePhysicalCommand(rtData, device, 'setColorTemperature', colorTemperature, delay)
    return 0
}

private long vcmd_fadeLevel(rtData, device, params) {
	int startLevel = (params[0] != null) ? cast(rtData, params[0], 'integer') : cast(rtData, getDeviceAttributeValue(rtData, device, 'level'), 'integer')
    int endLevel = cast(rtData, params[1], 'integer')
    long duration = cast(rtData, params[2], 'long')
    def state = params.size() > 3 ? params[3] : ""
    def delay = params.size() > 4 ? params[4] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    startLevel = (startLevel < 0) ? 0 : ((startLevel > 100) ? 100 : startLevel)
    endLevel = (endLevel < 0) ? 0 : ((endLevel > 100) ? 100 : endLevel)
    return vcmd_internal_fade(rtData, device, 'setLevel', startLevel, endLevel, duration)
}

private long vcmd_fadeInfraredLevel(rtData, device, params) {
	int startLevel = (params[0] != null) ? cast(rtData, params[0], 'integer') : cast(rtData, getDeviceAttributeValue(rtData, device, 'infraredLevel'), 'integer')
    int endLevel = cast(rtData, params[1], 'integer')
    long duration = cast(rtData, params[2], 'long')
    def state = params.size() > 3 ? params[3] : ""
    def delay = params.size() > 4 ? params[4] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    startLevel = (startLevel < 0) ? 0 : ((startLevel > 100) ? 100 : startLevel)
    endLevel = (endLevel < 0) ? 0 : ((endLevel > 100) ? 100 : endLevel)
    return vcmd_internal_fade(rtData, device, 'setInfraredLevel', startLevel, endLevel, duration)
}

private long vcmd_fadeSaturation(rtData, device, params) {
	int startLevel = (params[0] != null) ? cast(rtData, params[0], 'integer') : cast(rtData, getDeviceAttributeValue(rtData, device, 'saturation'), 'integer')
    int endLevel = cast(rtData, params[1], 'integer')
    long duration = cast(rtData, params[2], 'long')
    def state = params.size() > 3 ? params[3] : ""
    def delay = params.size() > 4 ? params[4] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    startLevel = (startLevel < 0) ? 0 : ((startLevel > 100) ? 100 : startLevel)
    endLevel = (endLevel < 0) ? 0 : ((endLevel > 100) ? 100 : endLevel)
    return vcmd_internal_fade(rtData, device, 'setSaturation', startLevel, endLevel, duration)
}

private long vcmd_fadeHue(rtData, device, params) {
	int startLevel = (params[0] != null) ? cast(rtData, params[0] / 3.6, 'integer') : cast(rtData, getDeviceAttributeValue(rtData, device, 'hue'), 'integer')
    int endLevel = cast(rtData, params[1], 'integer')
    long duration = cast(rtData, params[2], 'long')
    def state = params.size() > 3 ? params[3] : ""
    def delay = params.size() > 4 ? params[4] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    startLevel = (startLevel < 0) ? 0 : ((startLevel > 100) ? 100 : startLevel)
    endLevel = (endLevel < 0) ? 0 : ((endLevel > 100) ? 100 : endLevel)
    return vcmd_internal_fade(rtData, device, 'setHue', startLevel, endLevel, duration)
}

private long vcmd_fadeColorTemperature(rtData, device, params) {
	int startLevel = (params[0] != null) ? cast(rtData, params[0], 'integer') : cast(rtData, getDeviceAttributeValue(rtData, device, 'colorTemperature'), 'integer')
    int endLevel = cast(rtData, params[1], 'integer')
    long duration = cast(rtData, params[2], 'long')
    def state = params.size() > 3 ? params[3] : ""
    def delay = params.size() > 4 ? params[4] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    startLevel = (startLevel < 1000) ? 1000 : ((startLevel > 30000) ? 30000 : startLevel)
    endLevel = (endLevel < 1000) ? 1000 : ((endLevel > 30000) ? 30000 : endLevel)
    return vcmd_internal_fade(rtData, device, 'setColorTemperature', startLevel, endLevel, duration)
}

private long vcmd_internal_fade(Map rtData, device, String command, int startLevel, int endLevel, long duration) {
    long minInterval = 5000
    if (duration <= 5000) {
    	minInterval = 500
    } else if (duration <= 10000) {
    	minInterval = 1000
	} else if (duration <= 30000) {
		minInterval = 3000
	} else {
		minInterval = 5000
    }
    if ((startLevel == endLevel) || (duration <= 500)) {
    	//if the fade is too fast, or not changing anything, give it up and go to the end level directly
    	executePhysicalCommand(rtData, device, command, endLevel)
        return 0
    }
	int delta = endLevel - startLevel
    //the max number of steps we can do
    int steps = delta > 0 ? delta : -delta
    //figure out the interval
    long interval = Math.round(duration / steps)
    if (interval < minInterval) {
    	//intervals too small, adjust to do one change per 500ms
    	steps = Math.floor(1.0 * duration / minInterval)
        interval = Math.round(1.0 * duration / steps)
    }
    def scheduleDevice = (duration > 10000) ? hashId(device.id) : null
    int oldLevel = startLevel
	executePhysicalCommand(rtData, device, command, startLevel)
    for(def i = 1; i <= steps; i++) {
        int newLevel = Math.round(startLevel + delta * i / steps)
        if (oldLevel != newLevel) {
           	executePhysicalCommand(rtData, device, command, newLevel, i * interval, scheduleDevice, true)
        }
        oldLevel = newLevel
    }
    //for good measure, send a last command 100ms after the end of the interval
    executePhysicalCommand(rtData, device, command, endLevel, duration + 99, scheduleDevice, true)
    return duration + 100
}

private long vcmd_flash(rtData, device, params) {
	long onDuration = cast(rtData, params[0], 'long')
	long offDuration = cast(rtData, params[1], 'long')
    int cycles = cast(rtData, params[2], 'integer')
    def state = params.size() > 3 ? params[3] : ""
    def delay = params.size() > 4 ? params[4] : 0
    def currentState = getDeviceAttributeValue(rtData, device, 'switch')
    if (state && (currentState != "$state")) {
        return 0
    }
    long duration = (onDuration + offDuration) * cycles
    if (duration <= 500) {
    	//if the flash is too fast, ignore it
        return 0
    }
    //initialize parameters
    def firstCommand = currentState == 'on' ? 'off' : 'on'
    long firstDuration = firstCommand == 'on' ? onDuration : offDuration
    def secondCommand = firstCommand == 'on' ? 'off' : 'on'
    long secondDuration = firstCommand == 'on' ? offDuration : onDuration
    def scheduleDevice = (duration > 10000) ? hashId(device.id) : null
    long dur = 0
    for(def i = 1; i <= cycles; i++) {
    	executePhysicalCommand(rtData, device, firstCommand, [], dur, scheduleDevice, true)
        dur += firstDuration
    	executePhysicalCommand(rtData, device, secondCommand, [], dur, scheduleDevice, true)
        dur += secondDuration
    }
    //for good measure, send a last command 100ms after the end of the interval
    executePhysicalCommand(rtData, device, currentState, [], duration + 99, scheduleDevice, true)
	return duration + 100
}

private long vcmd_flashLevel(rtData, device, params) {
	int level1 = cast(rtData, params[0], 'int')
	long duration1 = cast(rtData, params[1], 'long')
	int level2 = cast(rtData, params[2], 'int')
	long duration2 = cast(rtData, params[3], 'long')
    int cycles = cast(rtData, params[4], 'integer')
    def state = params.size() > 5 ? params[5] : ""
    def delay = params.size() > 6 ? params[6] : 0
    def currentState = getDeviceAttributeValue(rtData, device, 'switch')
    def currentLevel = getDeviceAttributeValue(rtData, device, 'level')
    if (state && (currentState != "$state")) {
        return 0
    }
    long duration = (duration1 + duration2) * cycles
    if (duration <= 500) {
    	//if the flash is too fast, ignore it
        return 0
    }
    //initialize parameters
    def scheduleDevice = (duration > 10000) ? hashId(device.id) : null
    long dur = 0
    for(def i = 1; i <= cycles; i++) {
    	executePhysicalCommand(rtData, device, 'setLevel', [level1], dur, scheduleDevice, true)
        dur += duration1
    	executePhysicalCommand(rtData, device, 'setLevel', [level2], dur, scheduleDevice, true)
        dur += duration2
    }
    //for good measure, send a last command 100ms after the end of the interval
    executePhysicalCommand(rtData, device, 'setLevel', [currentLevel], duration + 98, scheduleDevice, true)
    executePhysicalCommand(rtData, device, currentState, [], duration + 99, scheduleDevice, true)
	return duration + 100
}

private long vcmd_flashColor(rtData, device, params) {
	def color1 = getColor(params[0])
	long duration1 = cast(rtData, params[1], 'long')
	def color2 = getColor(params[2])
	long duration2 = cast(rtData, params[3], 'long')
    int cycles = cast(rtData, params[4], 'integer')
    def state = params.size() > 5 ? params[5] : ""
    def delay = params.size() > 6 ? params[6] : 0
    def currentState = getDeviceAttributeValue(rtData, device, 'switch')
    if (state && (currentState != "$state")) {
        return 0
    }
    long duration = (duration1 + duration2) * cycles
    if (duration <= 500) {
    	//if the flash is too fast, ignore it
        return 0
    }
    //initialize parameters
    def scheduleDevice = (duration > 10000) ? hashId(device.id) : null
    long dur = 0
    for(def i = 1; i <= cycles; i++) {
    	executePhysicalCommand(rtData, device, 'setColor', [color1], dur, scheduleDevice, true)
        dur += duration1
    	executePhysicalCommand(rtData, device, 'setColor', [color2], dur, scheduleDevice, true)
        dur += duration2
    }
    //for good measure, send a last command 100ms after the end of the interval
    executePhysicalCommand(rtData, device, currentState, [], duration + 99, scheduleDevice, true)
	return duration + 100
}

private long vcmd_sendNotification(rtData, device, params) {
	def message = params[0]
    sendNotificationEvent(message)
    return 0
}

private long vcmd_sendPushNotification(rtData, device, params) {
	def message = params[0]
    def save = !!params[1]
	if (save) {
		sendPush(message)
	} else {
		sendPushMessage(message)
	}
    return 0
}

private long vcmd_sendSMSNotification(rtData, device, params) {
	def message = params[0]
	def phones = "${params[1]}".replace(" ", "").replace("-", "").replace("(", "").replace(")", "").tokenize(",;*|").unique()
	def save = !!params[2]
	for(def phone in phones) {
		if (save) {
			sendSms(phone, message)
		} else {
			sendSmsMessage(phone, message)
		}
		//we only need one notification
		save = false
	}
    return 0
}

private long vcmd_sendNotificationToContacts(rtData, device, params) {
	// Contact Book has been disabled and we're falling back onto PUSH notifications, if the option is enabled in the SmartApp's settings
    if (!rtData.redirectContactBook) return 0
	def message = params[0]
    def save = !!params[2]
    return vcmd_sendPushNotification(rtData, devices, [message, save])
}


private Map parseVariableName(name) {
	Map result = [
    	name: name,
        index: null
    ]
	if (name && !name.startsWith('$') && name.endsWith(']')) {
    	def parts = name.replace(']', '').tokenize('[')
        if (parts.size() == 2) {
        	result = [
            	name: parts[0],
                index: parts[1]
            ]
        }
    }
    return result
}

private long vcmd_setVariable(rtData, device, params) {
	def name = params[0]
    def value = params[1]
	setVariable(rtData, name, value)
    return 0
}

private long vcmd_executePiston(rtData, device, params) {
	def selfId = hashId(app.id)
	def pistonId = params[0]
    def arguments = (params[1] instanceof List ? params[1] : params[1].toString().tokenize(',')).unique();
    def wait = (params.size() > 2) ? cast(rtData, params[2], 'boolean') : false
    def description = "webCoRE: Piston $app.label requested execution of piston $pistonId"
    Map data = [:]
    for (argument in arguments) {
    	if (argument) data[argument] = getVariable(rtData, argument).v
    }
    if (wait) {
    	wait = !!parent.executePiston(pistonId, data, selfId)
    }
    if (!wait) {
    	sendLocationEvent(name: pistonId, value: selfId, isStateChange: true, displayed: false, linkText: description, descriptionText: description, data: data)
    }
    return 0
}

private long vcmd_pausePiston(rtData, device, params) {
	def selfId = hashId(app.id)
	def pistonId = params[0]
    parent.pausePiston(pistonId)
    return 0
}

private long vcmd_resumePiston(rtData, device, params) {
	def selfId = hashId(app.id)
	def pistonId = params[0]
    parent.resumePiston(pistonId)
    return 0
}

private long vcmd_executeRoutine(rtData, device, params) {
    def routineId = params[0]
    def routines = location.helloHome?.getPhrases()
    if (routines) {
        def routine = routines.find{ hashId(it.id) == routineId }
        if (routine) {
            location.helloHome?.execute(routine.label)
        }
    }
    return 0
}

private long vcmd_setHSLColor(rtData, device, params) {
    def hue = cast(rtData, params[0] / 3.6, 'integer')
    def saturation = params[1]
    def level = params[2]
    def color = [
        hue: hue,
        saturation: saturation,
        level: level
    ]
    def state = params.size() > 3 ? params[3] : ""
    def delay = params.size() > 4 ? params[4] : 0
    if (state && (getDeviceAttributeValue(rtData, device, 'switch') != "$state")) {
        return 0
    }
    executePhysicalCommand(rtData, device, 'setColor', color, delay)
    return 0
}



private long vcmd_wolRequest(rtData, device, params) {
	def mac = params[0]
	def secureCode = params[1]
	mac = mac.replace(":", "").replace("-", "").replace(".", "").replace(" ", "").toLowerCase()
	sendHubCommand(new physicalgraph.device.HubAction(
		"wake on lan $mac",
		physicalgraph.device.Protocol.LAN,
		null,
		secureCode ? [secureCode: secureCode] : [:]
	))
    return 0
}

private long vcmd_iftttMaker(rtData, device, params) {
	def key = (rtData.settings.ifttt_url ?: "").trim().replace('https://', '').replace('http://', '').replace('maker.ifttt.com/use/', '')
    if (!key) {
    	error "Failed to send IFTTT event, because the IFTTT integration is not properly set up. Please visit Settings in your dashboard and configure the IFTTT integration.", rtData
        return 0
    }
	def event = params[0]
    def value1 = params.size() > 1 ? params[1] : ""
    def value2 = params.size() > 2 ? params[2] : ""
    def value3 = params.size() > 3 ? params[3] : ""
    def body = [:]
    if (value1) body.value1 = value1
    if (value2) body.value2 = value2
    if (value3) body.value3 = value3
    def requestParams = [
        uri:  "https://maker.ifttt.com/trigger/${java.net.URLEncoder.encode(event, "UTF-8")}/with/key/" + key,
        requestContentType: "application/json",
        body: body
    ]
    httpPost(requestParams){ response ->
    	setSystemVariableValue(rtData, '$iftttStatusCode', response.status)
        setSystemVariableValue(rtData, '$iftttStatusOk', response.status == 200)
        return 0
    }
	return 0
}



private long vcmd_lifxScene(rtData, device, params) {
	def token = rtData.settings?.lifx_token
    if (!token) {
    	error "Sorry, you need to enable the LIFX integration in your dashboard's Settings section before trying to activate a LIFX scene.", rtData
        return 0
    }
	def sceneId = params[0]
    double duration = params.size() > 1 ? cast(rtData, params[1], 'long') / 1000 : 0
    if (!rtData.lifx?.scenes) {
    	error "Sorry, there seems to be no available LIFX scenes, please ensure the LIFX integration is working.", rtData
        return 0
    }
    sceneId = rtData.lifx.scenes.find{ (it.key == sceneId) || (it.value == sceneId) }?.key
	if (!sceneId) {
    	error "Sorry, could not find the specified LIFX scene.", rtData
        return 0
    }
    def requestParams = [
        uri:  "https://api.lifx.com",
        path: "/v1/scenes/scene_id:${sceneId}/activate",
        headers: [
            "Authorization": "Bearer $token"
        ],
        body: duration ? [duration: duration] : null
    ]
    try {
        httpPut(requestParams) { response ->
            if ((response.status >= 200) && (response.status < 300)) {
                return 0;
            }
            error "Error while activating LIFX scene. Result status is ${response.status}.", rtData
            return 0;
        }
    }
    catch(all) {
    	error "Error while activating LIFX scene:", rtData, null, all
        return 0
    }
	return duration * 1000
}


private getLifxSelector(rtData, selector) {
	def selectorId = ''
    if (selector == 'all') return 'all'
    def obj = rtData.lifx.scenes?.find{ (it.key == selector) || (it.value == selector) }?.key
    if (obj) {
    	selectorId = "scene_id:$obj"
    } else {
        obj = rtData.lifx.lights?.find{ (it.key == selector) || (it.value == selector) }?.key
        if (obj) {
            selectorId = "id:$obj"
        } else {
            obj = rtData.lifx.groups?.find{ (it.key == selector) || (it.value == selector) }?.key
            if (obj) {
                selectorId = "group_id:$obj"
            } else {
                obj = rtData.lifx.locations?.find{ (it.key == selector) || (it.value == selector) }?.key
                if (obj) {
                    selectorId = "location_id:$obj"
                }
            }
        }
    }
    return selectorId
}

private long vcmd_lifxState(rtData, device, params) {
	def token = rtData.settings?.lifx_token
    if (!token) {
    	error "Sorry, you need to enable the LIFX integration in your dashboard's Settings section before trying to activate a LIFX scene.", rtData
        return 0
    }
    def selector = getLifxSelector(rtData, params[0])
	if (!selector) {
    	error "Sorry, could not find the specified LIFX selector.", rtData
        return 0
    }
	def power = params[1]
    def color = getColor(params[2])
    def level = params[3]
    def infraredLevel = params[4]
    double duration = cast(rtData, params[5], 'long') / 1000
    def requestParams = [
        uri:  "https://api.lifx.com",
        path: "/v1/lights/${selector}/state",
        headers: [
            "Authorization": "Bearer $token"
        ],
        body: [:] + (power ? ([power: power]) : [:]) + (color ? ([color: color.hex]) : [:]) + (level != null ? ([brightness: level / 100.0]) : [:]) + (infrared != null ? [infrared: infraredLevel] : [:]) + (duration != null ? [duration: duration] : [:])
    ]
    try {
        httpPut(requestParams) { response ->
            if ((response.status >= 200) && (response.status < 300)) {
                return 0;
            }
            error "Error while setting LIFX lights state. Result status is ${response.status}.", rtData
            return 0;
        }
    }
    catch(all) {
    	error "Error while setting LIFX lights state:", rtData, null, all
        return 0
    }
	return duration * 1000
}

private long vcmd_lifxToggle(rtData, device, params) {
	def token = rtData.settings?.lifx_token
    if (!token) {
    	error "Sorry, you need to enable the LIFX integration in your dashboard's Settings section before trying to activate a LIFX scene.", rtData
        return 0
    }
    def selector = getLifxSelector(rtData, params[0])
	if (!selector) {
    	error "Sorry, could not find the specified LIFX selector.", rtData
        return 0
    }
    double duration = cast(rtData, params[1], 'long') / 1000
    def requestParams = [
        uri:  "https://api.lifx.com",
        path: "/v1/lights/${selector}/toggle",
        headers: [
            "Authorization": "Bearer $token"
        ],
        body: [:] + (duration != null ? [duration: duration] : [:])
    ]
    try {
        httpPost(requestParams) { response ->
            if ((response.status >= 200) && (response.status < 300)) {
                return 0;
            }
            error "Error while toggling LIFX lights. Result status is ${response.status}.", rtData
            return 0;
        }
    }
    catch(all) {
    	error "Error while toggling LIFX lights:", rtData, null, all
        return 0
    }
	return duration * 1000
}

private long vcmd_lifxBreathe(rtData, device, params) {
	def token = rtData.settings?.lifx_token
    if (!token) {
    	error "Sorry, you need to enable the LIFX integration in your dashboard's Settings section before trying to activate a LIFX scene.", rtData
        return 0
    }
    def selector = getLifxSelector(rtData, params[0])
	if (!selector) {
    	error "Sorry, could not find the specified LIFX selector.", rtData
        return 0
    }
    def color = getColor(params[1])
    def fromColor = (params[2] == null) ? null : getColor(params[2])
    def period = (params[3] == null) ? null : cast(rtData, params[3], 'long') / 1000
    def cycles = params[4]
	def peak = params[5]
    def powerOn = (params[6] == null) ? null : cast(rtData, params[6], 'boolean')
    def persist = (params[7] == null) ? null : cast(rtData, params[7], 'boolean')
    def requestParams = [
        uri:  "https://api.lifx.com",
        path: "/v1/lights/${selector}/effects/breathe",
        headers: [
            "Authorization": "Bearer $token"
        ],
        body: [color: color.hex] + (fromColor ? ([from_color: fromColor.hex]) : [:]) + (period != null ? ([period: period]) : [:]) + (cycles ? ([cycles: cycles]) : [:]) + (powerOn != null ? ([power_on: powerOn]) : [:]) + (persist != null ? ([persist: persist]) : [:]) + (peak != null ? ([peak: peak / 100]) : [:])
    ]
    try {
        httpPost(requestParams) { response ->
            if ((response.status >= 200) && (response.status < 300)) {
                return 0;
            }
            error "Error while breathing LIFX lights. Result status is ${response.status}.", rtData
            return 0;
        }
    }
    catch(all) {
    	error "Error while breathing LIFX lights:", rtData, null, all
        return 0
    }
	return (period ? period : 1) * 1000 * (cycles ? cycles : 1)
}

private long vcmd_lifxPulse(rtData, device, params) {
	def token = rtData.settings?.lifx_token
    if (!token) {
    	error "Sorry, you need to enable the LIFX integration in your dashboard's Settings section before trying to activate a LIFX scene.", rtData
        return 0
    }
    def selector = getLifxSelector(rtData, params[0])
	if (!selector) {
    	error "Sorry, could not find the specified LIFX selector.", rtData
        return 0
    }
    def color = getColor(params[1])
    def fromColor = (params[2] == null) ? null : getColor(params[2])
    def period = (params[3] == null) ? null : cast(rtData, params[3], 'long') / 1000
    def cycles = params[4]
    def powerOn =(params[5] == null)? null : cast(rtData, params[5], 'boolean')
    def persist = (params[6] == null) ? null : cast(rtData, params[6], 'boolean')
    def requestParams = [
        uri:  "https://api.lifx.com",
        path: "/v1/lights/${selector}/effects/pulse",
        headers: [
            "Authorization": "Bearer $token"
        ],
        body: [color: color.hex] + (fromColor ? ([from_color: fromColor.hex]) : [:]) + (period != null ? ([period: period]) : [:]) + (cycles ? ([cycles: cycles]) : [:]) + (powerOn != null ? ([power_on: powerOn]) : [:]) + (persist != null ? ([persist: persist]) : [:])
    ]
    try {
        httpPost(requestParams) { response ->
            if ((response.status >= 200) && (response.status < 300)) {
                return 0;
            }
            error "Error while pulsing LIFX lights. Result status is ${response.status}.", rtData
            return 0;
        }
    }
    catch(all) {
    	error "Error while pulsing LIFX lights:", rtData, null, all
        return 0
    }
	return (period ? period : 1) * 1000 * (cycles ? cycles : 1)
}


public localHttpRequestHandler(physicalgraph.device.HubResponse hubResponse) {
	def responseCode = ''
	for (header in hubResponse.headers) {
    	if (header.key.startsWith('http')) {
        	def parts = header.key.tokenize(' ')
            if (parts.size() > 2) {
            	responseCode = parts[1]
            }
        }
    }

    def binary = false
    def mediaType = hubResponse.getHeaders()['content-type']?.toLowerCase().tokenize(';')[0]
    switch (mediaType) {
        case 'image/jpeg':
        case 'image/png':
        case 'image/gif':
        binary = true
    }
	def data = hubResponse.body
   	def json = [:]
    def setRtData = [:]
    if (binary) {
		setRtData.mediaType = mediaType
		setRtData.mediaData = data?.getBytes()
    } else if (data) {
        try {
            def trimmed = data.trim()
            if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
                    json = (LinkedHashMap) new groovy.json.JsonSlurper().parseText(trimmed)
                } else if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
                    json = (List) new groovy.json.JsonSlurper().parseText(trimmed)
                } else {
                    json = [:]
                }
        } catch (all) {
            json = [:]
        }
    }
	handleEvents([date: new Date(), device: location, name: 'wc_async_reply', value: 'httpRequest', contentType: mediaType, responseData: data, jsonData: json, responseCode: responseCode, setRtData: setRtData])
}

private long vcmd_httpRequest(rtData, device, params) {
	def uri = params[0].replace(" ", "%20")
	def method = params[1]
	def useQueryString = method == 'GET' || method == 'DELETE' || method == 'HEAD'
	def requestBodyType = params[2]
	def variables = params[3]
    def auth = null
    def requestBody = null
    def contentType = null
    if (params.size() == 5) {
	    auth = params[4];
    } else if (params.size() == 7) {
        requestBody = params[4]
        contentType = params[5] ?: 'text/plain'
        auth = params[6];
    }
    if (!uri) return false
	def protocol = "https"
	def requestContentType = (method == "GET" || requestBodyType == "FORM") ? "application/x-www-form-urlencoded" : (requestBodyType == "JSON") ? "application/json" : contentType
    def userPart = ""
	def uriParts = uri.split("://").toList()
	if (uriParts.size() > 2) {
		warn "Invalid URI for web request: $uri", rtData
		return false
	}
	if (uriParts.size() == 2) {
		//remove the httpX:// from the uri
		protocol = uriParts[0].toLowerCase()
		uri = uriParts[1]
	}
    //support for user:pass@IP
    if (uri.contains('@')) {
    	def uriSubParts = uri.split('@').toList()
    	userPart = uriSubParts[0] + '@'
    	uri = uriSubParts[1]
    }
	def internal = uri.startsWith("10.") || uri.startsWith("192.168.")
	if ((!internal) && uri.startsWith("172.")) {
		//check for the 172.16.x.x/12 class
		def b = uri.substring(4,6)
		if (b.isInteger()) {
			b = b.toInteger()
			internal = (b >= 16) && (b <= 31)
		}
	}
	def data = null
    if (requestBodyType == 'CUSTOM' && !useQueryString) {
	    data = requestBody
    } else if (variables instanceof List) {
    	for(variable in variables.findAll{ !!it }) {
        	data  = data ?: [:]
			data[variable] = getVariable(rtData, variable).v
		}
    }
	if (internal) {
		try {
			if (rtData.logging > 2) debug "Sending internal web request to: $userPart$uri", rtData
            def ip = ((uri.indexOf("/") > 0) ? uri.substring(0, uri.indexOf("/")) : uri)
            if (!ip.contains(':')) ip += ':80'
            Map requestParams = [
				method: method,
				path: (uri.indexOf("/") > 0) ? uri.substring(uri.indexOf("/")) : "",
				headers: [
					HOST: userPart + ip,
					'Content-Type': requestContentType
				] + (auth ? ((auth.startsWith('{') && auth.endsWith('}')) ? ( new groovy.json.JsonSlurper().parseText( auth ) ) : [Authorization : auth]) : [:]),
				query: useQueryString ? data : null, //thank you @destructure00
				body: !useQueryString ? data : null //thank you @destructure00
			]
			sendHubCommand(new physicalgraph.device.HubAction(requestParams, null, [callback: localHttpRequestHandler]))
            return 20000
		} catch (all) {
			error "Error executing internal web request: ", rtData, null, all
		}
	} else {
		try {
			if (rtData.logging > 2) debug "Sending external web request to: $uri", rtData
			def requestParams = [
				uri:  "${protocol}://${userPart}${uri}",
				query: useQueryString ? data : null,
				headers: (auth ? ((auth.startsWith('{') && auth.endsWith('}')) ? ( new groovy.json.JsonSlurper().parseText( auth ) ) : [Authorization : auth]) : [:]),
				requestContentType: requestContentType,
				body: !useQueryString ? data : null
			]
			def func = ""
			switch(method) {
				case "GET":
					func = "httpGet"
					break
				case "POST":
					func = "httpPost"
					break
				case "PUT":
					func = "httpPut"
					break
				case "DELETE":
					func = "httpDelete"
					break
				case "HEAD":
					func = "httpHead"
					break
			}
			if (func) {
				"$func"(requestParams) { response ->
					setSystemVariableValue(rtData, "\$httpContentType", response.contentType)
					setSystemVariableValue(rtData, "\$httpStatusCode", response.status)
					setSystemVariableValue(rtData, "\$httpStatusOk", (response.status >= 200) && (response.status <= 299))
                    def binary = false
                    def mediaType = response.contentType.toLowerCase()
                    switch (mediaType) {
                    	case 'image/jpeg':
                    	case 'image/png':
                    	case 'image/gif':
                        	binary = true
                    }
					if ((response.status == 200) && response.data && !binary) {
						try {
							rtData.response = response.data instanceof Map ? response.data : (LinkedHashMap) new groovy.json.JsonSlurper().parseText(response.data)
						} catch (all) {
                        	rtData.response = response.data
						}
					} else {
                    	rtData.response = null
                        if (response.data && (response.data instanceof java.io.ByteArrayInputStream)) {
	                        rtData.mediaType = mediaType
    	                    rtData.mediaData = response.data.getBytes()
                        } else {
	                        rtData.mediaType = null
    	                    rtData.mediaData = null
                        }
                        rtData.mediaUrl = null;
                    }
				}
			}
		} catch (all) {
			error "Error executing external web request: ", rtData, null, all
		}
	}
	return 0
}


private long vcmd_writeToFuelStream(rtData, device, params) {
	def canister = params[0]
    def name = params[1]
    def data = params[2]
    def source = params[3]
	def requestParams = [
        uri:  "https://api-${rtData.region}-${rtData.instanceId[32]}.webcore.co:9247",
        path: "/fuelStream/write",
        headers: [
            'ST' : rtData.instanceId
        ],
         body: [
        	c: canister,
        	n: name,
            s: source,
        	d: data,
            i: rtData.instanceId
    	],
        requestContentType: "application/json"
    ]
    if (asynchttp_v1) asynchttp_v1.put(null, requestParams)
    return 0
}

private long vcmd_storeMedia(rtData, device, params) {
    if (!rtData.mediaData || !rtData.mediaType || !(rtData.mediaData) || (rtData.mediaData.size() <= 0)) {
    	error "No media is available to store, operation aborted.", rtData
        return 0
    }
    String data = new String(rtData.mediaData, 'ISO_8859_1')
	def requestParams = [
        uri:  "https://api-${rtData.region}-${rtData.instanceId[32]}.webcore.co:9247",
        path: "/media/store",
        headers: [
            'ST' : rtData.instanceId,
            'media-type' : rtData.mediaType
        ],
        body: data,
        requestContentType: rtData.mediaType
    ]
    if (asynchttp_v1) asynchttp_v1.put(asyncHttpRequestHandler, requestParams, [command: 'storeMedia'])
    return 20000
}

public asyncHttpRequestHandler(response, callbackData) {
	def mediaId
    def mediaUrl
    if (response.status == 200) {
	    def data = response.getJson()
        if ((data.result == 'OK') && (data.url)) {
            mediaId = data.id
            mediaUrl = data.url
        } else {
            if (data.message) {
                error "Error storing media item: $response.data.message"
            }
        }
    }
	handleEvents([date: new Date(), device: location, name: 'wc_async_reply', value: callbackData?.command, responseCode: response.status, setRtData: [mediaId: mediaId, mediaUrl: mediaUrl]])
}


private long vcmd_saveStateLocally(rtData, device, params, global = false) {
	def attributes = cast(rtData, params[0], 'string').tokenize(',')
    def canister = (params.size() > 1 ? cast(rtData, params[1], 'string') + ':' : '') + hashId(device.id) + ':'
    boolean overwrite = !(params.size() > 2 ? cast(rtData, params[2], 'boolean') : false)
    for (attr in attributes) {
    	def n = canister + attr
    	if (overwrite || (global ? (rtData.globalStore[n] == null) : (rtData.store[n] == null))) {
        	def value = getDeviceAttributeValue(rtData, device, attr)
            if (attr == 'hue') value = value * 3.6
            if (global) {
                rtData.globalStore[n] = value
                Map cache = rtData.gvStoreCache ?: [:]
                cache[n] = value
                rtData.gvStoreCache = cache
            } else {
        		rtData.store[n] = value
            }
        }
    }
	return 0
}

private long vcmd_saveStateGlobally(rtData, device, params) {
	return vcmd_saveStateLocally(rtData, device, params, true)
}


private long vcmd_loadStateLocally(rtData, device, params, global = false) {
	def attributes = cast(rtData, params[0], 'string').tokenize(',')
    def canister = (params.size() > 1 ? cast(rtData, params[1], 'string') + ':' : '') + hashId(device.id) + ':'
    boolean empty = params.size() > 2 ? cast(rtData, params[2], 'boolean') : false
    for (attr in attributes) {
    	def n = canister + attr
        def value = global ? rtData.globalStore[n] : rtData.store[n]
        if (attr == 'hue') value = cast(rtData, value, 'decimal') / 3.6
        if (empty) {
        	if (global) {
        		rtData.globalStore.remove(n)
                Map cache = rtData.gvStoreCache ?: [:]
                cache[n] = null
                rtData.gvStoreCache = cache
            } else {
        		rtData.store.remove(n)
            }
        }
        //find the right command for this attribute
        if (value == null) continue
        def exactCommand = null
        def fuzzyCommand = null
        for (command in rtData.commands.physical) {
        	if (command.value.a == attr) {
            	if (command.value.v == null) {
                	fuzzyCommand = command.key
                } else {
                	if (command.value.v == value) {
                    	exactCommand = command.key
                        break
                    }
                }
            }
        }
        if (exactCommand) {
        	if (rtData.logging > 2) debug "Restoring attribute '$attr' to value '$value' using command $exactCommand()", rtData
			executePhysicalCommand(rtData, device, exactCommand)
        	continue
        }
        if (fuzzyCommand) {
        	if (rtData.logging > 2) debug "Restoring attribute '$attr' to value '$value' using command $fuzzyCommand($value)", rtData
			executePhysicalCommand(rtData, device, fuzzyCommand, value)
        	continue
        }
        warn "Could not find a command to set attribute '$attr' to value '$value'", rtData
    }
	return 0
}

private long vcmd_loadStateGlobally(rtData, device, params) {
	return vcmd_loadStateLocally(rtData, device, params, true)
}

private long vcmd_parseJson(rtData, device, params) {
	def data = params[0]
	try {
		if (data.startsWith('{') && data.endsWith('}')) {
			rtData.json = (LinkedHashMap) new groovy.json.JsonSlurper().parseText(data)
		} else if (data.startsWith('[') && data.endsWith(']')) {
			rtData.json = (List) new groovy.json.JsonSlurper().parseText(data)
        } else {
        	rtData.json = [:]
        }
	} catch (all) {
    	error "Error parsing JSON data $data", rtData
    }
    return 0;
}

private long vcmd_cancelTasks(rtData, device, params) {
	rtData.cancelations.all = true
    return 0
}

private evaluateFollowedByCondition(rtData, condition, collection, async, ladderUpdated) {
	def result = evaluateCondition(rtData, condition, collection, async)
}

private evaluateConditions(rtData, conditions, collection, async) {
	def t = now()
    def msg = timer ''
    //override condition id
    def c = rtData.stack.c
    rtData.stack.c = conditions.$
    def not = (collection == 'c') ? !!conditions.n : !!conditions.rn
    def grouping = (collection == 'c') ? conditions.o : conditions.rop
    def value = (grouping == 'or' ? false : true)


    if ((grouping == 'followed by') && (collection == 'c')) {
    	if (!rtData.fastForwardTo || (rtData.fastForwardTo == conditions.$)) {
            //we're dealing with a followed by condition
            int ladderIndex = cast(rtData, rtData.cache["c:fbi:${conditions.$}"], 'integer')
            long ladderUpdated = cast(rtData, rtData.cache["c:fbt:${conditions.$}"], 'datetime')
            int steps = conditions[collection] ? conditions[collection].size() : 0
            if (ladderIndex >= steps) {
                value = false
            } else {
                def condition = conditions[collection][ladderIndex]
                long duration = 0
                if (ladderIndex) {
                    def tv = evaluateOperand(rtData, null, condition.wd)
                    duration = evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: tv.vt], 'long').v
                }
                if (ladderUpdated && duration && (ladderUpdated + duration < now())) {
                    //time has expired
                    value = (condition.wt == 'n')
                    if (!value) {
	                    if (rtData.logging > 2) debug "Conditional ladder step failed due to a timeout", rtData
                    }
                } else {
                    value = evaluateCondition(rtData, condition, collection, async)
                    if (condition.wt == 'n') {
                    	if (value) {
                        	value = false
                       	} else {
                        	value = null
                        }
                    }
                    //we allow loose matches to work even if other events happen
                    if ((condition.wt == 'l') && (!value)) value = null
                }
                if (value) {
                    //successful step, move on
                    ladderIndex += 1
                    ladderUpdated = now()
                    cancelStatementSchedules(rtData, conditions.$)
                    if (rtData.logging > 2) debug "Condition group #${conditions.$} made progress up the ladder, currently at step $ladderIndex of $steps", rtData
                    if (ladderIndex < steps) {
                        //delay decision, there are more steps to go through
                        value = null
                        condition = conditions[collection][ladderIndex]
                        def tv = evaluateOperand(rtData, null, condition.wd)
                        duration = evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: tv.vt], 'long').v
                        requestWakeUp(rtData, conditions, conditions, duration)
                    }
                }
            }

            switch (value) {
                case null:
                    //we need to exit time events set to work out the timeouts...
                    if (rtData.fastForwardTo == conditions.$) rtData.terminated = true
                	break
                case true:
                case false:
                    //ladder either collapsed or finished, reset data
                    ladderIndex = 0
                	ladderUpdated = 0
                    cancelStatementSchedules(rtData, conditions.$)
                	break
            }
            if (rtData.fastForwardTo == conditions.$) rtData.fastForwardTo = null
            rtData.cache["c:fbi:${conditions.$}"] = ladderIndex
            rtData.cache["c:fbt:${conditions.$}"] = ladderUpdated
        }
    } else {
        for(condition in conditions[collection]) {
            def res = evaluateCondition(rtData, condition, collection, async)
            value = (grouping == 'or') ? value || res : value && res
            //conditions optimizations go here
            if (!rtData.fastForwardTo && (!rtData.piston.o?.cto) && ((value && (grouping == 'or')) || (!value && (grouping == 'and')))) break
        }
	}
	def result
    if (value != null) {
        result = not ? !value : !!value
        if (!rtData.fastForwardTo) tracePoint(rtData, "c:${conditions.$}", now() - t, result)
        def oldResult = !!rtData.cache["c:${conditions.$}"];
        rtData.conditionStateChanged = (oldResult != result)
        if (rtData.conditionStateChanged) {
            //condition change, perform TCP
            cancelConditionSchedules(rtData, conditions.$)
        }
        rtData.cache["c:${conditions.$}"] = result
        //true/false actions
        if (collection == 'c') {
            if ((result || rtData.fastForwardTo) && conditions.ts && conditions.ts.length) executeStatements(rtData, conditions.ts, async)
            if ((!result || rtData.fastForwardTo) && conditions.fs && conditions.fs.length) executeStatements(rtData, conditions.fs, async)
        }
        if (!rtData.fastForwardTo) {
            msg.m = "Condition group #${conditions.$} evaluated $result (state ${rtData.conditionStateChanged ? 'changed' : 'did not change'})"
            if (rtData.logging > 2) debug msg, rtData
        }
    }
    //restore condition id
    rtData.stack.c = c
	return result
}

private evaluateOperand(rtData, node, operand, index = null, trigger = false, nextMidnight = false) {
	def values = []
    //older pistons don't have the 'to' operand (time offset), we're simulating an empty one
    if (!operand) operand = [t: 'c']
    switch (operand.t) {
    	case '': //optional, nothing selected
        	values = [[i: "${node?.$}:$index:0", v: [t: operand.vt, v: null]]]
            break
        case "p": //physical device
        	def j = 0;
            def attribute = rtData.attributes[operand.a]
        	for(deviceId in expandDeviceList(rtData, operand.d)) {
            	def value = [i: "${deviceId}:${operand.a}", v:getDeviceAttribute(rtData, deviceId, operand.a, operand.i, trigger) + (operand.vt ? [vt: operand.vt] : [:]) + (attribute && attribute.p ? [p: operand.p] : [:])]
                updateCache(rtData, value)
	            values.push(value)
	            j++
			}
	        if ((values.size() > 1) && !(operand.g in ['any', 'all'])) {
	            //if we have multiple values and a grouping other than any or all we need to apply that function
	            try {
	                values = [[i: "${node?.$}:$index:0", v:"func_${operand.g}"(rtData, values*.v) + (operand.vt ? [vt: operand.vt] : [:])]]
	            } catch(all) {
	                error "Error applying grouping method ${operand.g}", rtData
	            }
	        }
	        break;
		case 'd': //devices
        	def deviceIds = []
            for (d in expandDeviceList(rtData, operand.d)) {
				if (getDevice(rtData, d)) deviceIds.push(d)
            }
            /*
            for (d in rtData, operand.d) {
                if (d.startsWith(':')) {
                    if (getDevice(rtData, d)) deviceIds.push(d)
                } else {
                    //we're dealing with a variable, let's get the list of devices from it
                    def var = getVariable(rtData, d)
                    if (var.t == 'device') {
                        for (vd in var.v) {
							if (getDevice(rtData, vd)) deviceIds.push(vd)
                    	}
                	}
            	}
            }
            */
			values = [[i: "${node?.$}:d", v:[t: 'device', v: deviceIds.unique()]]]
            break
		case 'v': //virtual devices
        	switch (operand.v) {
            	case 'mode':
            	case 'alarmSystemStatus':
                	values = [[i: "${node?.$}:v", v:getDeviceAttribute(rtData, rtData.locationId, operand.v)]];
                    break;
            	case 'powerSource':
                	values = [[i: "${node?.$}:v", v:[t: 'enum', v:rtData.powerSource]]];
                    break;
				case 'time':
				case 'date':
				case 'datetime':
                	values = [[i: "${node?.$}:v", v:[t: operand.v, v: cast(rtData, now(), operand.v, 'long')]]];
                    break;
				case 'routine':
                	values = [[i: "${node?.$}:v", v:[t: 'string', v: (rtData.event.name == 'routineExecuted' ? hashId(rtData.event.value) : null)]]];
                    break;
                case 'tile':
				case 'ifttt':
                	values = [[i: "${node?.$}:v", v:[t: 'string', v: (rtData.event.name == operand.v ? rtData.event.value : null)]]];
                    break;
				case 'email':
                	values = [[i: "${node?.$}:v", v:[t: 'email', v: (rtData.event.name == operand.v ? rtData.event.value : null)]]];
                    break;
				case 'askAlexa':
                	values = [[i: "${node?.$}:v", v:[t: 'string', v: (rtData.event.name == 'askAlexaMacro' ? hashId(rtData.event.value) : null)]]];
                    break;
				case 'echoSistant':
                	values = [[i: "${node?.$}:v", v:[t: 'string', v: (rtData.event.name == 'echoSistantProfile' ? hashId(rtData.event.value) : null)]]];
                    break;
            }
            break
        case "s": //preset
        	switch (operand.vt) {
        		case 'time':
                case 'datetime':
                    def v = 0;
                    switch (operand.s) {
                        case 'midnight': v = nextMidnight ? getNextMidnightTime(rtData) : getMidnightTime(rtData); break;
                        case 'sunrise': v = getSunriseTime(rtData); break;
                        case 'noon': v = getNoonTime(rtData); break;
                        case 'sunset': v = getSunsetTime(rtData); break;
                    }
                    values = [[i: "${node?.$}:$index:0", v:[t:operand.vt, v:v]]]
					break
				default:
					values = [[i: "${node?.$}:$index:0", v:[t:operand.vt, v:operand.s]]]
					break
			}
            break
        case "x": //variable
        	if ((operand.vt == 'device') && (operand.x instanceof List)) {
            	//we could have multiple devices selected
                def sum = []
                for (x in operand.x) {
                	def var = getVariable(rtData, x)
                    if (var.v instanceof List) {
                    	sum += var.v
                    } else {
                    	sum.push(var.v)
                    }
                    values = [[i: "${node?.$}:$index:0", v:[t: 'device', v: sum] + (operand.vt ? [vt: operand.vt] : [:])]]
                }
            } else {
	        	values = [[i: "${node?.$}:$index:0", v:getVariable(rtData, operand.x + (operand.xi != null ? '[' + operand.xi + ']' : '')) + (operand.vt ? [vt: operand.vt] : [:])]]
            }
            break
        case "c": //constant
        	switch (operand.vt) {
            	case 'time':
                    def offset = cast(rtData, operand.c, 'integer')
                    values = [[i: "${node?.$}:$index:0", v: [t: 'time', v:(offset % 1440) * 60000]]]
                    break
            	case 'date':
            	case 'datetime':
                    values = [[i: "${node?.$}:$index:0", v: [t: operand.vt, v:operand.c]]]
                    break
            }
            if (values.size()) break
        case "e": //expression
	        values = [[i: "${node?.$}:$index:0", v: [:] + evaluateExpression(rtData, operand.exp) + (operand.vt ? [vt: operand.vt] : [:])]]
            break
        case "u": //expression
	        values = [[i: "${node?.$}:$index:0", v: getArgument(rtData, operand.u)]]
            break
    }
    if (!node)
    {
    	if (values.length) return values[0].v
        return [t: 'dynamic', v: null]
    }
    return values
}

private evaluateScalarOperand(rtData, node, operand, index = null, dataType = 'string') {
	def value = evaluateOperand(rtData, null, operand, index)
    return [t: dataType, v: cast(rtData, (value ? value.v: ''), dataType)]
}

private Boolean evaluateCondition(rtData, condition, collection, async) {
	def t = now()
    def msg = timer ''
    //override condition id
    def c = rtData.stack.c
    rtData.stack.c = condition.$
    def not = false
    def oldResult = !!rtData.cache["c:${condition.$}"];
    def result = false
    if (condition.t == 'group') {
    	return evaluateConditions(rtData, condition, collection, async)
    } else {
        not = !!condition.n
        def comparison = rtData.comparisons.triggers[condition.co]
        def trigger = !!comparison
        if (!comparison) comparison = rtData.comparisons.conditions[condition.co]
        rtData.wakingUp = (rtData.event.name == 'time') && (!!rtData.event.schedule) && (rtData.event.schedule.s == condition.$)
        if (rtData.fastForwardTo || comparison) {
            if (!rtData.fastForwardTo || (rtData.fastForwardTo == -9 /*initial run*/)) {
                def paramCount = comparison.p ?: 0
                def lo = null
                def ro = null
                def ro2 = null
                for(int i = 0; i <= paramCount; i++) {
                    def operand = (i == 0 ? condition.lo : (i == 1 ? condition.ro : condition.ro2))
                    //parse the operand
                    def values = evaluateOperand(rtData, condition, operand, i, trigger)
                    switch (i) {
                        case 0:
                        lo = [operand: operand, values: values]
                        break
                        case 1:
                        ro = [operand: operand, values: values]
                        break
                        case 2:
                        ro2 = [operand: operand, values: values]
                        break
                    }
                }

                //we now have all the operands, their values, and the comparison, let's get to work
                Map options = [
                	//we ask for matching/non-matching devices if the user requested it or if the trigger is timed
                    //setting matches to true will force the condition group to evaluate all members (disables evaluation optimizations)
					matches: lo.operand.dm || lo.operand.dn || (trigger && comparison.t),
                    forceAll: (trigger && comparison.t)
				]
                def to = (comparison.t || (ro && (lo.operand.t == 'v') && (lo.operand.v == 'time') && (ro.operand.t != 'c'))) && condition.to ? [operand: condition.to, values: evaluateOperand(rtData, null, condition.to)] : null
                def to2 = ro2 && (lo.operand.t == 'v') && (lo.operand.v == 'time') && (ro2.operand.t != 'c') && condition.to2 ? [operand: condition.to2, values: evaluateOperand(rtData, null, condition.to2)] : null
                result = evaluateComparison(rtData, condition.co, lo, ro, ro2, to, to2, options)
                //save new values to cache
                if (lo) for (value in lo.values) updateCache(rtData, value)
                if (ro) for (value in ro.values) updateCache(rtData, value)
                if (ro2) for (value in ro2.values) updateCache(rtData, value)
                if (rtData.fastForwardTo == null) tracePoint(rtData, "c:${condition.$}", now() - t, result)
                if (lo.operand.dm && options.devices) setVariable(rtData, lo.operand.dm, options.devices?.matched ?: [])
                if (lo.operand.dn && options.devices) setVariable(rtData, lo.operand.dn, options.devices?.unmatched ?: [])
                //do the stay logic here
                if (trigger && comparison.t && (rtData.fastForwardTo == null)) {
                	//timed trigger
                    if (to) {
                        def tvalue = to && to.operand && to.values ? to.values + [f: to.operand.f] : null
                        if (tvalue) {
	                        long delay = evaluateExpression(rtData, [t: 'duration', v: tvalue.v, vt: tvalue.vt], 'long').v
                            if ((lo.operand.t == 'p') && (lo.operand.g == 'any') && lo.values.size() > 1) {
                            	def schedules = rtData.piston.o?.pep ? atomicState.schedules : state.schedules
                            	for (value in lo.values) {
                                	def dev = value.v?.d
                                    if (dev in options.devices.matched) {
                                    	//schedule one device schedule
                                        if (!schedules.find{ (it.s == condition.$) && (it.d == dev)  }) {
                                        	//schedule a wake up if there's none, otherwise just move on
                                            if (rtData.logging > 2) debug "Adding a timed trigger schedule for device $dev for condition ${condition.$}", rtData
			                            	requestWakeUp(rtData, condition, condition, delay, dev)
                                        }
                                    } else {
                                    	//cancel that one device schedule
                                        if (rtData.logging > 2) debug "Cancelling any timed trigger schedules for device $dev for condition ${condition.$}", rtData
                                		cancelStatementSchedules(rtData, condition.$, dev)
                                    }
                                }
                            } else {
                            	if (result) {
                                	//if we find the comparison true, set a timer if we haven't already
	                            	def schedules = rtData.piston.o?.pep ? atomicState.schedules : state.schedules
									if (!schedules.find{ (it.s == condition.$) }) {
                                		if (rtData.logging > 2) debug "Adding a timed trigger schedule for condition ${condition.$}", rtData
		                            	requestWakeUp(rtData, condition, condition, delay)
                                    }
                                } else {
                                	if (rtData.logging > 2) debug "Cancelling any timed trigger schedules for condition ${condition.$}", rtData
                                	cancelStatementSchedules(rtData, condition.$)
                                }
                            }
                        }
                    }
                    result = false
                }
                result = not ? !result : !!result
            } else if ((rtData.event.name == 'time') && (rtData.fastForwardTo == condition.$)) {
            	rtData.fastForwardTo = null
                rtData.resumed = true
				result = not ? false : true
        	} else {
                result = oldResult
            }
        }
    }
    rtData.wakingUp = false
    rtData.conditionStateChanged = oldResult != result
    if (rtData.conditionStateChanged) {
    	//condition change, perform TCP
        cancelConditionSchedules(rtData, condition.$)
    }
    rtData.cache["c:${condition.$}"] = result
    //true/false actions
    if ((result || rtData.fastForwardTo) && condition.ts && condition.ts.length) executeStatements(rtData, condition.ts, async)
	if ((!result || rtData.fastForwardTo) && condition.fs && condition.fs.length) executeStatements(rtData, condition.fs, async)
    //restore condition id
    rtData.stack.c = c
    if (!rtData.fastForwardTo) {
	    msg.m = "Condition #${condition.$} evaluated $result"
	    if (rtData.logging > 2) debug msg, rtData
    }
    if ((rtData.fastForwardTo <= 0) && condition.s && (condition.t == 'condition') && condition.lo && condition.lo.t == 'v') {
    	switch (condition.lo.v) {
        	case 'time':
            case 'date':
            case 'datetime':
	            scheduleTimeCondition(rtData, condition);
            	break;
        }
    }
    return result
}

private updateCache(rtData, value) {
    def oldValue = rtData.cache[value.i]
    if (!oldValue || (oldValue.t != value.v.t) || (oldValue.v != value.v.v)) {
        //if (rtData.logging > 2) debug "Updating value", rtData
        rtData.newCache[value.i] = value.v + [s: now()]
    } else {
        //if (rtData.logging > 2) debug "Not updating value", rtData
    }
}

private Boolean evaluateComparison(rtData, comparison, lo, ro = null, ro2 = null, to = null, to2 = null, options = [:]) {
		def fn = "comp_${comparison}"
        def result = (lo.operand.g == 'any' ? false : true)
        if (options?.matches) {
        	options.devices = [matched: [], unmatched: []]
        }
        //if multiple left values, go through each
        def tvalue = to && to.operand && to.values ? to.values + [f: to.operand.f] : null
        def tvalue2 = to2 && to2.operand && to2.values ? to2.values : null
        for(value in lo.values) {
            def res = false
            if (value && value.v && (!value.v.x || options.forceAll)) {
                try {
                	//physical support
                	//value.p = lo.operand.p
                    if (value && (value.v.t == 'device')) value.v = evaluateExpression(rtData, value.v, 'dynamic')
                    if (!ro) {
                    	def msg = timer ""
                        res = "$fn"(rtData, value, null, null, tvalue, tvalue2)
                    	msg.m = "Comparison  (${value?.v?.t}) ${value?.v?.v} $comparison = $res"
                        if (rtData.logging > 2) debug msg, rtData
                    } else {
                        def rres
                        res = (ro.operand.g == 'any' ? false : true)
                        //if multiple right values, go through each
                        for (rvalue in ro.values) {
		                    if (rvalue && (rvalue.v.t == 'device')) rvalue.v = evaluateExpression(rtData, rvalue.v, 'dynamic')
                            if (!ro2) {
                                def msg = timer ""
                                rres = "$fn"(rtData, value, rvalue, null, tvalue, tvalue2)
                                msg.m = "Comparison  (${value?.v?.t}) ${value?.v?.v} $comparison  (${rvalue?.v?.t}) ${rvalue?.v?.v} = $rres"
                                if (rtData.logging > 2) debug msg, rtData
                            } else {
                                rres = (ro2.operand.g == 'any' ? false : true)
                                //if multiple right2 values, go through each
                                for (r2value in ro2.values) {
				                    if (r2value && (r2value.v.t == 'device')) r2value.v = evaluateExpression(rtData, r2value.v, 'dynamic')
                                	def msg = timer ""
                                    def r2res = "$fn"(rtData, value, rvalue, r2value, tvalue, tvalue2)
                                    msg.m = "Comparison (${value?.v?.t}) ${value?.v?.v} $comparison  (${rvalue?.v?.t}) ${rvalue?.v?.v} .. (${r2value?.v?.t}) ${r2value?.v?.v} = $r2res"
                                    if (rtData.logging > 2) debug msg, rtData
                                    rres = (ro2.operand.g == 'any' ? rres || r2res : rres && r2res)
                                    if (((ro2.operand.g == 'any') && rres) || ((ro2.operand.g != 'any') && !rres)) break
                                }
                            }
                            res = (ro.operand.g == 'any' ? res || rres : res && rres)
                            if (((ro.operand.g == 'any') && res) || ((ro.operand.g != 'any') && !res)) break
                        }
                    }
                } catch(all) {
                    error "Error calling comparison $fn:", rtData, null, all
                    res = false
                }
                if (res && (lo.operand.t == 'v')) {
                	switch (lo.operand.v) {
                    	case 'time':
                        case 'date':
                        case 'datetime':
							boolean pass = checkTimeRestrictions(rtData, lo.operand, utcToLocalTime(), 5, 1) == 0
                            if (rtData.logging > 2) debug "Time restriction check ${pass ? 'passed' : 'failed'}", rtData
                        	if (!pass) res = false;
                    }
                }
            }
            result = (lo.operand.g == 'any' ? result || res : result && res)
            if (options?.matches && value.v.d) {
            	if (res) {
                	options.devices.matched.push(value.v.d)
                } else {
                	options.devices.unmatched.push(value.v.d)
                }
            }
            if ((lo.operand.g == 'any') && res && !(options?.matches)) {
            	//logical OR if we're using the ANY keyword
            	break;
            }
            if ((lo.operand.g == 'all') && !result && !(options?.matches)) {
            	//logical AND if we're using the ALL keyword
            	break;
            }
        }
        return result
}

private cancelStatementSchedules(rtData, statementId, data = null) {
	//cancel all schedules that are pending for statement statementId
    if (rtData.logging > 2) debug "Cancelling statement #${statementId}'s schedules...", rtData
    if (!(statementId in rtData.cancelations.statements)) {
    	rtData.cancelations.statements.push([id: statementId, data: data])
    }
}

private cancelConditionSchedules(rtData, conditionId) {
	//cancel all schedules that are pending for condition conditionId
    if (rtData.logging > 2) debug "Cancelling condition #${conditionId}'s schedules...", rtData
    if (!(conditionId in rtData.cancelations.conditions)) {
    	rtData.cancelations.conditions.push(conditionId)
    }
}

private Boolean matchDeviceSubIndex(list, deviceSubIndex) {
	if (!list || !(list instanceof List) || (list.size() == 0)) return true
    return list.collect{ "$it".toString() }.indexOf("$deviceSubIndex".toString()) >= 0
}

private Boolean matchDeviceInteraction(option, isPhysical) {
	return !(((option == 'p') && !isPhysical) || ((option == 's') && !!isPhysical))
}

private List listPreviousStates(device, attribute, threshold, excludeLast) {
	def result = []
	//if (!(device instanceof DeviceWrapper)) return result
	def events = device.events([all: true, max: 100]).findAll{it.name == attribute}
	//if we got any events, let's go through them
	//if we need to exclude last event, we start at the second event, as the first one is the event that triggered this function. The attribute's value has to be different from the current one to qualify for quiet
    if (events.size()) {
		def thresholdTime = now() - threshold
		def endTime = now()
		for(def i = 0; i < events.size(); i++) {
			def startTime = events[i].date.getTime()
			def duration = endTime - startTime
			if ((duration >= 1000) && ((i > 0) || !excludeLast)) {
				result.push([value: events[i].value, startTime: startTime, duration: duration])
			}
			if (startTime < thresholdTime)
				break
			endTime = startTime
		}
	} else {
    	def currentState = device.currentState(attribute)
        if (currentState) {
	        def startTime = currentState.getDate().getTime()
    	    result.push([value: currentState.value, startTime: startTime, duration: now() - startTime])
        }
    }
	return result
}

private Map valueCacheChanged(rtData, comparisonValue) {
	def oldValue = rtData.cache[comparisonValue.i]
    def newValue = comparisonValue.v
    if (!(oldValue instanceof Map)) oldValue = false
    return (!!oldValue && ((oldValue.t != newValue.t) || ("${oldValue.v}" != "${newValue.v}"))) ? [i: comparisonValue.i, v: oldValue] : null
}

private boolean valueWas(rtData, comparisonValue, rightValue, rightValue2, timeValue, func) {
	if (!comparisonValue || !comparisonValue.v || !comparisonValue.v.d || !comparisonValue.v.a || !timeValue || !timeValue.v || !timeValue.vt) {
    	return false
    }
    def device = getDevice(rtData, comparisonValue.v.d)
    if (!device) return false
    def attribute = comparisonValue.v.a
    long threshold = evaluateExpression(rtData, [t: 'duration', v: timeValue.v, vt: timeValue.vt], 'long').v

	def states = listPreviousStates(device, attribute, threshold, (rtData.event.device?.id == device.id) && (rtData.event.name == attribute))
    def result = true
    long duration = 0
    for (state in states) {
    	if (!("comp_$func"(rtData, [i: comparisonValue.i, v: [t: comparisonValue.v.t, v: cast(rtData, state.value, comparisonValue.v.t)]], rightValue, rightValue2, timeValue))) break
        duration += state.duration
    }
    if (!duration) return false
    result = (timeValue.f == 'l') ? duration < threshold : duration >= threshold
    if (rtData.logging > 2) debug "Duration ${duration}ms for ${func.replace('is_', 'was_')} ${timeValue.f == 'l' ? '<' : '>='} ${threshold}ms threshold = ${result}", rtData
    return result
}

private boolean valueChanged(rtData, comparisonValue, timeValue) {
	if (!comparisonValue || !comparisonValue.v || !comparisonValue.v.d || !comparisonValue.v.a || !timeValue || !timeValue.v || !timeValue.vt) {
    	return false
    }
    def device = getDevice(rtData, comparisonValue.v.d)
    if (!device) return false
    def attribute = comparisonValue.v.a
    long threshold = evaluateExpression(rtData, [t: 'duration', v: timeValue.v, vt: timeValue.vt], 'long').v

	def states = listPreviousStates(device, attribute, threshold, false)
    if (!states.size()) return false
    def value = states[0].value
    for (state in states) {
    	if (state.value != value) return true
    }
    return false
}

private boolean match(string, pattern) {
    if ((pattern.size() > 2) && pattern.startsWith('/') && pattern.endsWith('/')) {
        pattern = ~pattern.substring(1, patern.size() - 1)
        return !!(string =~ pattern)
    }
    return string.contains(pattern)
}

//comparison low level functions
private boolean comp_is								(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return (evaluateExpression(rtData, lv.v, 'string').v == evaluateExpression(rtData, rv.v, 'string').v) || (lv.v.n && (cast(rtData, lv.v.n, 'string') == cast(rtData, rv.v.v, 'string'))) }
private boolean comp_is_not							(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !comp_is(rtData, lv, rv, rv2, tv, tv2) }
private boolean comp_is_equal_to					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def dt = ((lv?.v?.t == 'decimal') || (rv?.v?.t == 'decimal') ? 'decimal' : ((lv?.v?.t == 'integer') || (rv?.v?.t == 'integer') ? 'integer' : 'dynamic')); return evaluateExpression(rtData, lv.v, dt).v == evaluateExpression(rtData, rv.v, dt).v }
private boolean comp_is_not_equal_to				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def dt = ((lv?.v?.t == 'decimal') || (rv?.v?.t == 'decimal') ? 'decimal' : ((lv?.v?.t == 'integer') || (rv?.v?.t == 'integer') ? 'integer' : 'dynamic')); return evaluateExpression(rtData, lv.v, dt).v != evaluateExpression(rtData, rv.v, dt).v }
private boolean comp_is_different_than				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_not_equal_to(rtData, lv, rv, rv2, tv, tv2) }
private boolean comp_is_less_than					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return evaluateExpression(rtData, lv.v, 'decimal').v < evaluateExpression(rtData, rv.v, 'decimal').v }
private boolean comp_is_less_than_or_equal_to		(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return evaluateExpression(rtData, lv.v, 'decimal').v <= evaluateExpression(rtData, rv.v, 'decimal').v }
private boolean comp_is_greater_than				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return evaluateExpression(rtData, lv.v, 'decimal').v > evaluateExpression(rtData, rv.v, 'decimal').v }
private boolean comp_is_greater_than_or_equal_to	(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return evaluateExpression(rtData, lv.v, 'decimal').v >= evaluateExpression(rtData, rv.v, 'decimal').v }
private boolean comp_is_even						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return evaluateExpression(rtData, lv.v, 'integer').v.mod(2) == 0 }
private boolean comp_is_odd							(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return evaluateExpression(rtData, lv.v, 'integer').v.mod(2) != 0 }
private boolean comp_is_true						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !!evaluateExpression(rtData, lv.v, 'boolean').v }
private boolean comp_is_false						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !evaluateExpression(rtData, lv.v, 'boolean').v }
private boolean comp_is_inside_of_range				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def v = evaluateExpression(rtData, lv.v, 'decimal').v; def v1 = evaluateExpression(rtData, rv.v, 'decimal').v; def v2 = evaluateExpression(rtData, rv2.v, 'decimal').v; return (v1 < v2) ? ((v >= v1) && (v <= v2)) : ((v >= v2) && (v <= v1)); }
private boolean comp_is_outside_of_range			(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !comp_is_inside_of_range(rtData, lv, rv, rv2, tv, tv2) }
private boolean comp_is_any_of						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def v = evaluateExpression(rtData, lv.v, 'string').v; for (vi in rv.v.v.tokenize(',')) { if (v == evaluateExpression(rtData, [t: rv.v.t, v: "$vi".toString().trim(), i: rv.v.i, a: rv.v.a, vt: rv.v.vt], 'string').v) return true; }; return false;}
private boolean comp_is_not_any_of					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !comp_is_any_of(rtData, lv, rv, rv2, tv, tv2); }

private boolean comp_was							(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is'); }
private boolean comp_was_not						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_not'); }
private boolean comp_was_equal_to					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_equal_to'); }
private boolean comp_was_not_equal_to				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_not_equal_to'); }
private boolean comp_was_different_than				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_different_than'); }
private boolean comp_was_less_than					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_less_than'); }
private boolean comp_was_less_than_or_equal_to		(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_less_than_or_equal_to'); }
private boolean comp_was_greater_than				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_greater_than'); }
private boolean comp_was_greater_than_or_equal_to	(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_greater_than_or_equal_to'); }
private boolean comp_was_even						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_even'); }
private boolean comp_was_odd						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_odd'); }
private boolean comp_was_true						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_true'); }
private boolean comp_was_false						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_false'); }
private boolean comp_was_inside_of_range			(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_inside_of_range'); }
private boolean comp_was_outside_of_range			(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_outside_of_range'); }
private boolean comp_was_any_of						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_any_of'); }
private boolean comp_was_not_any_of					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_not_any_of'); }

private boolean comp_changed						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueChanged(rtData, lv, tv); }
private boolean comp_did_not_change					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !valueChanged(rtData, lv, tv); }

private boolean comp_is_any							(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return true; }
private boolean comp_is_before						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { long offset1 = tv ? evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: tv.vt], 'long').v : 0; return cast(rtData, evaluateExpression(rtData, lv.v, 'datetime').v + 2000, lv.v.t) < cast(rtData, evaluateExpression(rtData, rv.v, 'datetime').v + offset1, lv.v.t); }
private boolean comp_is_after						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { long offset1 = tv ? evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: tv.vt], 'long').v : 0; return cast(rtData, evaluateExpression(rtData, lv.v, 'datetime').v + 2000, lv.v.t) >= cast(rtData, evaluateExpression(rtData, rv.v, 'datetime').v + offset1, lv.v.t); }
private boolean comp_is_between						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { long offset1 = tv ? evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: tv.vt], 'long').v : 0; long offset2 = tv2 ? evaluateExpression(rtData, [t: 'duration', v: tv2.v, vt: tv2.vt], 'long').v : 0; long v = cast(rtData, evaluateExpression(rtData, lv.v, 'datetime').v + 2000, lv.v.t); long v1 = cast(rtData, evaluateExpression(rtData, rv.v, 'datetime').v + offset1, lv.v.t); long v2 = cast(rtData, evaluateExpression(rtData, rv2.v, 'datetime').v + offset2, lv.v.t); return (v1 < v2) ? (v >= v1) && (v < v2) : (v < v2) || (v >= v1); }
private boolean comp_is_not_between					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !comp_is_between(rtData, lv, rv, rv2, tv, tv2); }

/*triggers*/
private boolean comp_gets							(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return (cast(rtData, lv.v.v, 'string') == cast(rtData, rv.v.v, 'string')) && matchDeviceSubIndex(lv.v.i, rtData.currentEvent.index)}
private boolean comp_executes						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is(rtData, lv, rv, rv2, tv, tv2) }
private boolean comp_arrives						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return (rtData.event.name == 'email') && match(rtData.event?.jsonData?.from ?: '', evaluateExpression(rtData, rv.v, 'string').v) && match(rtData.event?.jsonData?.message ?: '', evaluateExpression(rtData, rv2.v, 'string').v) }
private boolean comp_happens_daily_at				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return rtData.wakingUp }

private boolean comp_changes						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueCacheChanged(rtData, lv) && matchDeviceInteraction(lv.p, rtData.currentEvent.physical); }
private boolean comp_changes_to						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return valueCacheChanged(rtData, lv) && ("${lv.v.v}" == "${rv.v.v}") && matchDeviceInteraction(lv.p, rtData.currentEvent.physical); }
private boolean comp_changes_away_from				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && ("${oldValue.v.v}" == "${rv.v.v}") && matchDeviceInteraction(lv.p, rtData.currentEvent.physical); }
private boolean comp_drops							(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') > cast(rtData, lv.v.v, 'decimal')); }
private boolean comp_does_not_drop					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !comp_drops(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_drops_below					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') >= cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') < cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_drops_to_or_below				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') > cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') <= cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_rises							(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') < cast(rtData, lv.v.v, 'decimal')); }
private boolean comp_does_not_rise					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !comp_rises(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_rises_above					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') <= cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') > cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_rises_to_or_above				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') < cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') >= cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_remains_below					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') < cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') < cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_remains_below_or_equal_to		(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') <= cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') <= cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_remains_above					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') > cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') > cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_remains_above_or_equal_to		(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') >= cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') >= cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_enters_range					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); if (!oldValue) return false; def ov = cast(rtData, oldValue.v.v, 'decimal'); def v = cast(rtData, lv.v.v, 'decimal'); def v1 = cast(rtData, rv.v.v, 'decimal'); def v2 = cast(rtData, rv2.v.v, 'decimal'); if (v1 > v2) { def vv = v1; v1 = v2; v2 = vv; }; return ((ov < v1) || (ov > v2)) && ((v >= v1) && (v <= v2)); }
private boolean comp_exits_range					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); if (!oldValue) return false; def ov = cast(rtData, oldValue.v.v, 'decimal'); def v = cast(rtData, lv.v.v, 'decimal'); def v1 = cast(rtData, rv.v.v, 'decimal'); def v2 = cast(rtData, rv2.v.v, 'decimal'); if (v1 > v2) { def vv = v1; v1 = v2; v2 = vv; }; return ((ov >= v1) && (ov <= v2)) && ((v < v1) || (v > v2)); }
private boolean comp_remains_inside_of_range		(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); if (!oldValue) return false; def ov = cast(rtData, oldValue.v.v, 'decimal'); def v = cast(rtData, lv.v.v, 'decimal'); def v1 = cast(rtData, rv.v.v, 'decimal'); def v2 = cast(rtData, rv2.v.v, 'decimal'); if (v1 > v2) { def vv = v1; v1 = v2; v2 = vv; }; return (ov >= v1) && (ov <= v2) && (v >= v1) && (v <= v2); }
private boolean comp_remains_outside_of_range		(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); if (!oldValue) return false; def ov = cast(rtData, oldValue.v.v, 'decimal'); def v = cast(rtData, lv.v.v, 'decimal'); def v1 = cast(rtData, rv.v.v, 'decimal'); def v2 = cast(rtData, rv2.v.v, 'decimal'); if (v1 > v2) { def vv = v1; v1 = v2; v2 = vv; }; return ((ov < v1) || (ov > v2)) && ((v < v1) || (v > v2)); }
private boolean comp_becomes_even					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'integer').mod(2) != 0) && (cast(rtData, lv.v.v, 'integer').mod(2) == 0); }
private boolean comp_becomes_odd					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'integer').mod(2) == 0) && (cast(rtData, lv.v.v, 'integer').mod(2) != 0); }
private boolean comp_remains_even					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'integer').mod(2) == 0) && (cast(rtData, lv.v.v, 'integer').mod(2) == 0); }
private boolean comp_remains_odd					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'integer').mod(2) != 0) && (cast(rtData, lv.v.v, 'integer').mod(2) != 0); }

private boolean comp_changes_to_any_of				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return !!valueCacheChanged(rtData, lv) && comp_is_any_of(rtData, lv, rv, rv2, tv, tv2) && matchDeviceInteraction(lv.p, rtData.currentEvent.physical); }
private boolean comp_changes_away_from_any_of		(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return !!oldValue && comp_is_any_of(rtData, oldValue, rv, rv2) && matchDeviceInteraction(lv.p, rtData.currentEvent.physical); }

private boolean comp_stays							(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_unchanged				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return true; }
private boolean comp_stays_not						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_not(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_equal_to					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_equal_to(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_different_than			(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_different_than(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_less_than				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_less_than(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_less_than_or_equal_to	(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_less_than_or_equal_to(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_greater_than				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_greater_than(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_greater_than_or_equal_to	(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_greater_than_or_equal_to(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_even						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_even(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_odd						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_odd(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_true						(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_true(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_false					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_false(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_inside_of_range			(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_inside_of_range(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_outside_of_range			(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_outside_of_range(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_any_of					(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_any_of(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_away_from				(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_not_equal_to(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_away_from_any_of			(rtData, lv, rv = null, rv2 = null, tv = null, tv2 = null) { return comp_is_not_any_of(rtData, lv, rv, rv2, tv, tv2); }


private traverseStatements(node, closure, parentNode = null, data = null) {
    if (!node) return
	//if a statements element, go through each item
	if (node instanceof List) {
    	for(def item in node) {
        	if (!item.di) {
	           	boolean lastTimer = (data && data.timer)
    	        if (data && (item.t == 'every')) {
        	        data.timer = true
            	}
	    		traverseStatements(item, closure, parentNode, data)
            	if (data) {
                	data.timer = lastTimer
            	}
            }
	    }
        return
	}

    //got a statement, pass it on to the closure
    if (closure instanceof Closure) {
    	closure(node, parentNode, data)
    }

    //if the statements has substatements, go through them
    if (node.s instanceof List) {
    	traverseStatements(node.s, closure, node, data)
    }
    if (node.e instanceof List) {
    	traverseStatements(node.e, closure, node, data)
    }
}

private traverseEvents(node, closure, parentNode = null) {
    if (!node) return
	//if a statements element, go through each item
	if (node instanceof List) {
    	for(item in node) {
	    	traverseEvents(item, closure, parentNode)
	    }
        return
	}
    //got a condition, pass it on to the closure
    if ((closure instanceof Closure)) {
    	closure(node, parentNode)
    }
}

private traverseConditions(node, closure, parentNode = null) {
    if (!node) return
	//if a statements element, go through each item
	if (node instanceof List) {
    	for(item in node) {
	    	traverseConditions(item, closure, parentNode)
	    }
        return
	}
    //got a condition, pass it on to the closure
    if ((node.t == 'condition') && (closure instanceof Closure)) {
    	closure(node, parentNode)
    }
    //if the statements has substatements, go through them
    if (node.c instanceof List) {
    	if (closure instanceof Closure) closure(node, parentNode)
    	traverseConditions(node.c, closure, node)
    }
}

private traverseRestrictions(node, closure, parentNode = null) {
    if (!node) return
	//if a statements element, go through each item
	if (node instanceof List) {
    	for(item in node) {
	    	traverseRestrictions(item, closure, parentNode)
	    }
        return
	}
    //got a restriction, pass it on to the closure
    if ((node.t == 'restriction') && (closure instanceof Closure)) {
    	closure(node, parentNode)
    }
    //if the statements has substatements, go through them
    if (node.r instanceof List) {
    	if (closure instanceof Closure) closure(node, parentNode)
    	traverseRestrictions(node.r, closure, node)
    }
}
private traverseExpressions(node, closure, param, parentNode = null) {
    if (!node) return
	//if a statements element, go through each item
	if (node instanceof List) {
    	for(item in node) {
	    	traverseExpressions(item, closure, param, parentNode)
	    }
        return
	}
    //got a statement, pass it on to the closure
    if (closure instanceof Closure) {
    	closure(node, parentNode, param)
    }
    //if the statements has substatements, go through them
    if (node.i instanceof List) {
    	traverseExpressions(node.i, closure, param, node)
    }
}

private getRoutineById(routineId) {
	def routines = location.helloHome?.getPhrases()
    for(routine in routines) {
    	if (routine && routine?.label && (hashId(routine.id) == routineId)) {
    		return routine
        }
    }
    return null
}

private void updateDeviceList(deviceIdList) {
	app.updateSetting('dev', [type: 'capability.device', value: deviceIdList.unique()])
}

private void updateContactList(contactIdList) {
	app.updateSetting('contacts', [type: 'contact', value: contactIdList.unique()])
}


private void subscribeAll(rtData) {
	try {
        rtData = rtData ?: getRunTimeData()
        def ss = [
            events: 0,
            controls: 0,
            devices: 0,
        ]
        def statementData = [timer: false]
        def msg = timer "Finished subscribing", null, -1
        unsubscribe()
        if (rtData.logging > 1) trace "Subscribing to devices...", rtData, 1
        Map devices = [:]
        Map rawDevices = [:]
        Map rawContacts = [:]
        Map subscriptions = [:]
        def count = 0
        def hasTriggers = false
        def downgradeTriggers = false
        //traverse all statements
        def expressionTraverser
        def operandTraverser
        def eventTraverser
        def conditionTraverser
		def restrictionTraverser
		def statementTraverser
        expressionTraverser = { expression, parentExpression, comparisonType ->
        	def subscriptionId = null
            def deviceId = null
            def attribute = null
            if ((expression.t == 'device') && (expression.id)) {
                devices[expression.id] = [c: (comparisonType ? 1 : 0) + (devices[expression.id]?.c ?: 0)]
                subscriptionId = "${expression.id}${expression.a}"
                deviceId = expression.id
                attribute = expression.a
            }
            if ((expression.t == 'variable') && expression.x && expression.x.startsWith('@')) {
                subscriptionId = "${expression.x}"
                deviceId = rtData.locationId
                attribute = "${expression.x.startsWith('@@') ? '@@' + handle() : rtData.instanceId}.${expression.x}"
            }
            if (subscriptionId && deviceId) {
                def ct = subscriptions[subscriptionId]?.t ?: null
                if ((ct == 'trigger') || (comparisonType == 'trigger')) {
                    ct = 'trigger'
                } else {
                    ct = ct ?: comparisonType
                }
                subscriptions[subscriptionId] = [d: deviceId, a: attribute, t: ct, c: (subscriptions[subscriptionId] ? subscriptions[subscriptionId].c : []) + [condition]]
                if ((deviceId != rtData.locationId) && (deviceId.startsWith(':'))) {
                	rawDevices[deviceId] = rtData.devices[deviceId]
                    devices[deviceId] =  [c: (comparisonType ? 1 : 0) + (devices[deviceId]?.c ?: 0)]
                }
            }
        }
        operandTraverser = { node, operand, value, comparisonType ->
        	if (!operand) return
            switch (operand.t) {
                case "p": //physical device
                    for(deviceId in expandDeviceList(rtData, operand.d, true)) {
                        devices[deviceId] = [c: (comparisonType ? 1 : 0) + (devices[deviceId]?.c ?: 0)]
                        //if we have any trigger, it takes precedence over anything else
                        def ct = subscriptions["$deviceId${operand.a}"]?.t ?: null
                        if ((ct == 'trigger') || (comparisonType == 'trigger')) {
                            ct = 'trigger'
                        } else {
                            ct = ct ?: comparisonType
                        }
                        subscriptions["$deviceId${operand.a}"] = [d: deviceId, a: operand.a, t: ct , c: (subscriptions["$deviceId${operand.a}"] ? subscriptions["$deviceId${operand.a}"].c : []) + (comparisonType?[node]:[])]
                        if ((deviceId != rtData.locationId) && (deviceId.startsWith(':'))) {
                            rawDevices[deviceId] = rtData.devices[deviceId]
                        }
                    }
                    break;
                case "v": //physical device
                    def deviceId = rtData.locationId
                    //if we have any trigger, it takes precedence over anything else
                    devices[deviceId] = [c: (comparisonType ? 1 : 0) + (devices[deviceId]?.c ?: 0)]
                    def subscriptionId = null
                    def attribute = null
                    switch (operand.v) {
						case 'time':
                        case 'date':
                        case 'datetime':
                        case 'mode':
                        case 'powerSource':
                        case 'alarmSystemStatus':
                       		subscriptionId = "$deviceId${operand.v}"
                           	attribute = operand.v
                            break
                        case 'routine':
                        	if (value && (value.t == 'c') && (value.c)) {
                            	def routine = getRoutineById(value.c)
                                if (routine) {
	                        		subscriptionId = "$deviceId${operand.v}${routine.id}"
    	                        	attribute = "routineExecuted.${routine.id}"
                                }
                            }
                            break
                        case 'email':
                            subscriptionId = "$deviceId${operand.v}${hashId(app.id)}"
                            attribute = "email.${hashId(app.id)}"
                            break
                        case 'ifttt':
                        case 'askAlexa':
                        case 'echoSistant':
                        	if (value && (value.t == 'c') && (value.c)) {
                            	def options = rtData.virtualDevices[operand.v]?.o
                            	def item = options ? options[value.c] : value.c
                                if (item) {
	                        		subscriptionId = "$deviceId${operand.v}${item}"
                                	attribute = "${operand.v}.${item}"
                                    switch (operand.v) {
                                    	case 'askAlexa':
                                        	attribute = "askAlexaMacro.${item}"
                                            break;
                                    	case 'echoSistant':
                                        	attribute = "echoSistantProfile.${item}"
                                            break;
                                    }
                                }
                            }
                            break
                    }
                    if (subscriptionId) {
                    	def ct = subscriptions[subscriptionId]?.t ?: null
                        if ((ct == 'trigger') || (comparisonType == 'trigger')) {
                            ct = 'trigger'
                        } else {
                            ct = ct ?: comparisonType
                        }
                        subscriptions[subscriptionId] = [d: deviceId, a: attribute, t: ct , c: (subscriptions[subscriptionId] ? subscriptions[subscriptionId].c : []) + (comparisonType?[node]:[])]
                        break;
                    }
					break;
                case 'x':
                	if (operand.x && operand.x.startsWith('@')) {
                    	def subscriptionId = operand.x
                        def attribute = "${operand.x.startsWith('@@') ? '@@' + handle() : rtData.instanceId}.${operand.x}"
                        def ct = subscriptions[subscriptionId]?.t ?: null
                        if ((ct == 'trigger') || (comparisonType == 'trigger')) {
                            ct = 'trigger'
                        } else {
                            ct = ct ?: comparisonType
                        }
                        subscriptions[subscriptionId] = [d: rtData.locationId, a: attribute, t: ct , c: (subscriptions[subscriptionId] ? subscriptions[subscriptionId].c : []) + (comparisonType?[node]:[])]
                    }
                	break;
                case "c": //constant
                case "e": //expression
                    traverseExpressions(operand.exp?.i, expressionTraverser, comparisonType)
                    break
            }
        }
        eventTraverser = { event, parentEvent ->
            if (event.lo) {
				def comparisonType = 'trigger'
                operandTraverser(event, event.lo, null, comparisonType)
            }
        }
        conditionTraverser = { condition, parentCondition ->
            if (condition.co) {
                def comparison = rtData.comparisons.conditions[condition.co]
                def comparisonType = 'condition'
                if (!comparison) {
                    hasTriggers = true
                    comparisonType = downgradeTriggers || (condition.sm == 'never') ? 'condition' : 'trigger'
                    comparison = rtData.comparisons.triggers[condition.co]
                }
                if (comparison) {
                    condition.ct = comparisonType.take(1)
                    def paramCount = comparison.p ?: 0
                    for(int i = 0; i <= paramCount; i++) {
                        //get the operand to parse
                        def operand = (i == 0 ? condition.lo : (i == 1 ? condition.ro : condition.ro2))
                        operandTraverser(condition, operand, condition.ro, comparisonType)
                    }
                }
            }
            if (condition.ts instanceof List) traverseStatements(condition.ts, statementTraverser, condition, statementData)
            if (condition.fs instanceof List) traverseStatements(condition.fs, statementTraverser, condition, statementData)
        }
        restrictionTraverser = { restriction, parentRestriction ->
            if (restriction.co) {
                def comparison = rtData.comparisons.conditions[restriction.co]
                def comparisonType = 'condition'
                if (!comparison) {
                    //hasTriggers = true
                    //comparisonType = downgradeTriggers ? 'condition' : 'trigger'
                    comparison = rtData.comparisons.triggers[restriction.co]
                }
                if (comparison) {
                    def paramCount = comparison.p ?: 0
                    for(int i = 0; i <= paramCount; i++) {
                        //get the operand to parse
                        def operand = (i == 0 ? restriction.lo : (i == 1 ? restriction.ro : restriction.ro2))
                        operandTraverser(restriction, operand, null, null)
                    }
                }
            }
        }
        statementTraverser = { node, parentNode, data ->
        	downgradeTriggers = data && data.timer
            if (node.r) traverseRestrictions(node.r, restrictionTraverser)
            for(deviceId in node.d) {
                devices[deviceId] = devices[deviceId] ?: [c: 0]
                if ((deviceId != rtData.locationId) && (deviceId.startsWith(':'))) {
                	rawDevices[deviceId] = rtData.devices[deviceId]
                }
            }
            switch( node.t ) {
            	case 'action':
                	//we need to get a list of used contacts in tasks...
                	if (node.k) {
                    	for (k in node.k) {
                        	for (operand in k.p) {
								if ((operand.vt == 'contact') && (operand.c instanceof List)) {
                        			for (c in operand.c) {
                            			rawContacts[c] = rtData.contacts[c]
                        			}
                    			}
                            }
                        }
                    }
                    break
            	case 'if':
                	if (node.ei) {
                    	for (ei in node.ei) {
                        	traverseConditions(ei.c?:[], conditionTraverser)
                    		traverseStatements(ei.s?:[], statementTraverser, ei, data)
                        }
                    }
                case 'while':
                case 'repeat':
                	traverseConditions(node.c, conditionTraverser)
                    break;
                case 'on':
                	traverseEvents(node.c?:[], eventTraverser)
                    break
            	case 'switch':
                	operandTraverser(node, node.lo, null, 'condition')
                	for (c in node.cs) {
	                    operandTraverser(c, c.ro, null, null)
	                    //if case is a range, traverse the second operand too
	                    if (c.t == 'r') operandTraverser(c, c.ro2, null, null)
	                    if (c.s instanceof List) {
	                        traverseStatements(c.s, statementTraverser, node, data)
	                    }
	                }
					break;
				case 'every':
                	hasTriggers = true;
                    break;
            }
        }
        if (rtData.piston.r) traverseRestrictions(rtData.piston.r, restrictionTraverser)
        if (rtData.piston.s) traverseStatements(rtData.piston.s, statementTraverser, null, statementData)
        //device variables
        for(variable in rtData.piston.v.findAll{ (it.t == 'device') && it.v && it.v.d && (it.v.d instanceof List)}) {
            for (deviceId in variable.v.d) {
                devices[deviceId] = [c: 0 + (devices[deviceId]?.c ?: 0)]
                if (deviceId != rtData.locationId) {
                	rawDevices[deviceId] = rtData.devices[deviceId]
                }
            }
        }
        def dds = [:]
        for (subscription in subscriptions) {
        	def altSub = 'never';
            for (condition in subscription.value.c) if (condition) {
            	condition.s = false
                altSub = (condition.sm == 'always') ? condition.sm : ((altSub != 'always') && (condition.sm != 'never') ? condition.sm : altSub)
			}
            if (!rtData.piston.o.des && !!subscription.value.t && !!subscription.value.c && (altSub != "never") && ((subscription.value.t == "trigger") || (altSub == "always") || !hasTriggers)) {
                def device = subscription.value.d.startsWith(':') ? getDevice(rtData, subscription.value.d) : null
				def a = (subscription.value.a == 'orientation') || (subscription.value.a == 'axisX') || (subscription.value.a == 'axisY') || (subscription.value.a == 'axisZ') ? 'threeAxis' : subscription.value.a
                if (device) {
					for (condition in subscription.value.c) if (condition) { condition.s = (condition.sm != 'never') && ((condition.ct == 't') || (condition.sm == 'always') || (!hasTriggers)) }
                	switch (subscription.value.a) {
                    	case 'time':
                        case 'date':
                        case 'datetime':
                        	break;
						default:
                            if (rtData.logging) info "Subscribing to $device.${a}...", rtData
                            subscribe(device, a, deviceHandler)
                            ss.events = ss.events + 1
                            if (!dds[device.id]) {
                                ss.devices = ss.devices + 1
                                dds[device.id] = 1
                            }
                    }
                } else {
                    error "Failed subscribing to $device.${a}, device not found", rtData
                }
            } else {
				for (condition in subscription.value.c) if (condition) { condition.s = false }
                if (devices[subscription.value.d]) {
	                devices[subscription.value.d].c = devices[subscription.value.d].c - 1
                }
            }
        }
        //save devices
        List deviceIdList = rawDevices.collect{ it && it.value ? it.value.id : null }
        deviceIdList.removeAll{ it == null }
        updateDeviceList(deviceIdList)
        //save contacts
        List contactIdList = rawContacts.collect{ it && it.value ? it.value.id : null }
        contactIdList.removeAll{ it == null }
        updateContactList(contactIdList)
        //fake subscriptions for controlled devices to force the piston being displayed in those devices' Smart Apps tabs
        for (d in devices.findAll{ ((it.value.c <= 0) || (rtData.piston.o.des)) && (it.key != rtData.locationId) }) {
            def device = d.key.startsWith(':') ? getDevice(rtData, d.key) : null
            if (device && (device != location)) {
                if (rtData.logging > 1) trace "Subscribing to $device...", rtData
                ss.controls = ss.controls + 1
                if (!dds[device.id]) {
                    ss.devices = ss.devices + 1
                    dds[device.id] = 1
                }
            }
        }
        state.subscriptions = ss
        if (rtData.logging > 1) trace msg, rtData


        def event = [date: new Date(), device: location, name: 'time', value: now(), schedule: [t: 0, s: 0, i: -9]]
        //subscribe(app, appHandler)
        subscribe(location, hashId(app.id), executeHandler)
        executeEvent(rtData, event)
		processSchedules rtData, true
        //save cache collected through dummy run
        for(item in rtData.newCache) rtData.cache[item.key] = item.value
        atomicState.cache = rtData.cache
    } catch (all) {
    	error "An error has occurred while subscribing: ", rtData, null, all
    }
}


private List expandDeviceList(rtData, List devices, localVarsOnly = false) {
	//temporary allowing global vars
    localVarsOnly = false
	List result = []
	for(deviceId in devices) {
    	if (deviceId && (deviceId.size() == 34) && deviceId.startsWith(':') && deviceId.endsWith(':')) {
        	result.push(deviceId)
        } else {
        	if (localVarsOnly) {
            	//during subscriptions we use local vars only to make sure we don't subscribe to "variable" lists of devices
	        	def var = rtData.localVars[deviceId]
    	        if (var && (var.t == 'device') && (var.v instanceof Map) && (var.v.t == 'd') && (var.v.d instanceof List) && var.v.d.size()) result += var.v.d
            } else {
	        	def var = getVariable(rtData, deviceId)
    	        if (var && (var.t == 'device') && (var.v instanceof List) && var.v.size()) result += var.v
    	        if (var && (var.t != 'device')) {
                	var device = getDevice(rtData, cast(rtData, var.v, 'string'))
                    if (device) result += [hashId(device.id)]
                }
            }
        }
    }
    return result.unique()
}

def appHandler(evt) {
}


private sanitizeVariableName(name) {
	name = name ? "$name".trim().replace(" ", "_") : null
}

private getDevice(rtData, idOrName) {
	if (rtData.locationId == idOrName) return location
	def device = rtData.devices[idOrName] ?: rtData.devices.find{ it.value.getDisplayName() == idOrName }?.value
    if (!device) {
    	if (!rtData.allDevices) rtData.allDevices = parent.listAvailableDevices(true)
        if (rtData.allDevices) {
			def deviceMap = rtData.allDevices.find{ (idOrName == it.key) || (idOrName == it.value.getDisplayName()) }
	        if (deviceMap) {
            	rtData.updateDevices = true
            	rtData.devices[deviceMap.key] = deviceMap.value
            	device = deviceMap.value
			}
        } else {
            error "Device ${idOrName} was not found. Please review your piston.", rtData
        }
    }
    return device
}

private getDeviceAttributeValue(rtData, device, attributeName) {
	if (rtData.event && (rtData.event.name == attributeName) && (rtData.event.device?.id == device.id)) {
    	return rtData.event.value;
    } else {
    	switch (attributeName) {
        	case '$status':
            	return device.getStatus()
        	case 'orientation':
        		return getThreeAxisOrientation(rtData.event && (rtData.event.name == 'threeAxis') && (rtData.event.device?.id == device.id) ? rtData.event.xyzValue : device.currentValue('threeAxis'))
        	case 'axisX':
        		return rtData.event && (rtData.event.name == 'threeAxis') && (rtData.event.device?.id == device.id) ? rtData.event.xyzValue.x : device.currentValue('threeAxis').x
			case 'axisY':
        		return rtData.event && (rtData.event.name == 'threeAxis') && (rtData.event.device?.id == device.id) ? rtData.event.xyzValue.y : device.currentValue('threeAxis').y
            case 'axisZ':
        		return rtData.event && (rtData.event.name == 'threeAxis') && (rtData.event.device?.id == device.id) ? rtData.event.xyzValue.z : device.currentValue('threeAxis').z
        }
        def result
        try {
        	result = device.currentValue(attributeName)
        } catch (all) {
        	error "Error reading current value for $device.$attributeName:", rtData, all
        }
		return result ?: ''
    }
}

private Map getDeviceAttribute(rtData, deviceId, attributeName, subDeviceIndex = null, trigger = false) {
	if (deviceId == rtData.locationId) {
    	//we have the location here
        switch (attributeName) {
        	case 'mode':
            	def mode = location.getCurrentMode();
            	return [t: 'string', v: hashId(mode.getId()), n: mode.getName()]
        	case 'alarmSystemStatus':
				def v = hubUID ? 'off' : location.currentState("alarmSystemStatus")?.value
                def n = hubUID ? 'Disarmed' : rtData.virtualDevices['alarmSystemStatus']?.o[v]
				return [t: 'string', v: v, n: n]
        }
        return [t: 'string', v: location.getName().toString()]
    }
	def device = getDevice(rtData, deviceId)
    if (device) {
        def attribute = rtData.attributes[attributeName ?: '']
        if (!attribute) {
            attribute = [t: 'string', m: false]
        }
        //x = eXclude - if a momentary attribute is looked for and the device does not match the current device, then we must ignore this during comparisons
        def value = (attributeName ? cast(rtData, getDeviceAttributeValue(rtData, device, attributeName), attribute.t) : "$device")
        if (attributeName == 'hue') {
        	value = cast(rtData, cast(rtData, value, 'decimal') * 3.6, attribute.t)
        }
		return [t: attribute.t, v: value, d: deviceId, a: attributeName, i: subDeviceIndex, x: (!!attribute.m || !!trigger) && ((device?.id != (rtData.event.device?:location).id) || (((attributeName == 'orientation') || (attributeName == 'axisX') || (attributeName == 'axisY') || (attributeName == 'axisZ') ? 'threeAxis' : attributeName) != rtData.event.name))]
    }
    return [t: "error", v: "Device '${deviceId}' not found"]
}

private Map getJsonData(rtData, data, name, feature = null) {
	if (data != null) {
        try {
            List parts = name.replace('][', '].[').tokenize('.');
            def args = (data instanceof Map ? [:] + data : (data instanceof List ? [] + data : new groovy.json.JsonSlurper().parseText(data)))
            def partIndex = -1
            for(part in parts) {
            	partIndex = partIndex + 1
                if ((args instanceof String) || (args instanceof GString)) {
                    if (args.startsWith('{') && args.endsWith('}')) {
                        args = (LinkedHashMap) new groovy.json.JsonSlurper().parseText(args)
                    } else if (args.startsWith('[') && args.endsWith(']')) {
                        args = (List) new groovy.json.JsonSlurper().parseText(args)
                    }
                }
                if (args instanceof List) {
                    switch (part) {
                        case 'length':
                            return [t: 'integer', v: args.size()]
                        case 'first':
                            args = args.size() > 0 ? args[0] : ''
                            continue
                            break
                        case 'second':
                            args = args.size() > 1 ? args[1] : ''
                            continue
                            break
                        case 'third':
                            args = args.size() > 2 ? args[2] : ''
                            continue
                            break
                        case 'fourth':
                            args = args.size() > 3 ? args[3] : ''
                            continue
                            break
                        case 'fifth':
                            args = args.size() > 4 ? args[4] : ''
                            continue
                            break
                        case 'sixth':
                            args = args.size() > 5 ? args[5] : ''
                            continue
                            break
                        case 'seventh':
                            args = args.size() > 6 ? args[6] : ''
                            continue
                            break
                        case 'eighth':
                            args = args.size() > 7 ? args[7] : ''
                            continue
                            break
                        case 'ninth':
                            args = args.size() > 8 ? args[8] : ''
                            continue
                            break
                        case 'tenth':
                            args = args.size() > 9 ? args[9] : ''
                            continue
                            break
                        case 'last':
                            args = args.size() > 0 ? args[args.size() - 1] : ''
                            continue
                            break
                    }
                }
                if (!(args instanceof Map) && !(args instanceof List)) return [t: 'dynamic', v: '']
                //nfl overrides
                def overrideArgs = false
                if ((feature == 'NFL') && (partIndex == 1) && !!args && !!args.games) {
                	def offset = null
                    def start = null
                    def end = null
                    def date = localDate()
                    def dow = date.day
                	switch ("$part".tokenize('[')[0].toLowerCase()) {
                    	case 'yesterday':
                        	offset = -1
                        	break
                    	case 'today':
                        	offset = 0
                        	break
                        case 'tomorrow':
                        	offset = 1
                        	break
                        case 'mon':
                        case 'monday':
                        	offset = dow <= 2 ? 1 - dow : 8 - dow
                        	break
                        case 'tue':
                        case 'tuesday':
                        	offset = dow <= 2 ? 2 - dow : 9 - dow
                        	break
                        case 'wed':
                        case 'wednesday':
                        	offset = dow <= 2 ? - 4 - dow : 3 - dow
                        	break
                        case 'thu':
                        case 'thursday':
                        	offset = dow <= 2 ? - 3 - dow : 4 - dow
                        	break
                        case 'fri':
                        case 'friday':
                        	offset = dow <= 2 ? - 2 - dow : 5 - dow
                        	break
                        case 'sat':
                        case 'saturday':
                        	offset = dow <= 2 ? - 1 - dow : 6 - dow
                        	break
                        case 'sun':
                        case 'sunday':
                        	offset = dow <= 2 ? 0 - dow : 7 - dow
                        	break
                        case 'lastweek':
                        	start = (dow <= 2 ? - 4 - dow : 3 - dow) - 7
                            end = (dow <= 2 ? 2 - dow : 9 - dow) - 7
                        	break
                        case 'thisweek':
                        	start = dow <= 2 ? - 4 - dow : 3 - dow
                            end = dow <= 2 ? 2 - dow : 9 - dow
                        	break
                        case 'nextweek':
                        	start = (dow <= 2 ? - 4 - dow : 3 - dow) + 7
                            end = (dow <= 2 ? 2 - dow : 9 - dow) + 7
                        	break
                    }
                    if (offset != null) {
                    	date.setTime(date.getTime() + offset * 86400000)
                        def game = args.games.find{ (it.year == date.year + 1900) && (it.month == date.month + 1) && (it.day == date.date) }
                        args = game
                        continue
                    }
                    if (start != null) {
                    	def startDate = localDate()
                    	startDate.setTime(date.getTime() + start * 86400000)
                    	def endDate = localDate()
                    	endDate.setTime(date.getTime() + end * 86400000)
                        start = (startDate.year + 1900) * 372 + (startDate.month * 31) + (startDate.date - 1)
                        end = (endDate.year + 1900) * 372 + (endDate.month * 31) + (endDate.date - 1)
                        if (parts[0].size() > 3) {
	                        def games = args.games.findAll{ (it.year * 372 + (it.month - 1) * 31 + (it.day - 1) >= start) && (it.year * 372 + (it.month - 1) * 31 + (it.day - 1) <= end) }
    	                    args = games
                            overrideArgs = true
                        } else {
	                        def game = args.games.find{ (it.year * 372 + (it.month - 1) * 31 + (it.day - 1) >= start) && (it.year * 372 + (it.month - 1) * 31 + (it.day - 1) <= end) }
    	                    args = game
	                        continue
                        }
                    }
                }
                def idx = 0
                if (part.endsWith(']')) {
                    //array index
                    def start = part.indexOf('[')
                    if (start >= 0) {
                        idx = part.substring(start + 1, part.size() - 1)
                        part = part.substring(0, start)
                        if (idx.isInteger()) {
                            idx = idx.toInteger()
                        } else {
                            def var = getVariable(rtData, idx)
                            idx = var && (var.t != 'error') ? var.v : idx
                        }
                    }
                    if (!overrideArgs && !!part) args = args[part]
                    if (args instanceof List) idx = cast(rtData, idx, 'integer')
                    args = args[idx]
                    continue
                }
                if (!overrideArgs) args = args[part]
            }
            return [t: 'dynamic', v: "$args".toString()]
        } catch (all) {
            error "Error retrieving JSON data part $part", rtData
            return [t: 'dynamic', v: '']
        }
    }
    return [t: 'dynamic', v: '']
}

private Map getArgument(rtData, name) {
	return getJsonData(rtData, rtData.args, name)
}

private Map getJson(rtData, name) {
	return getJsonData(rtData, rtData.json, name)
}

private Map getPlaces(rtData, name) {
	return getJsonData(rtData, rtData.settings?.places, name)
}

private Map getResponse(rtData, name) {
	return getJsonData(rtData, rtData.response, name)
}

private Map getWeather(rtData, name) {
    warn 'The Weather Underground data source used by $weather will soon be shut down; please see https://wiki.webcore.co/TWC_Weather', rtData
	List parts = name.tokenize('.');
    rtData.weather = rtData.weather ?: [:]
    if (parts.size() > 0) {
    	def dataFeature = parts[0]
        if (rtData.weather[dataFeature] == null) {
        	rtData.weather[dataFeature] = app.getWeatherFeature(dataFeature)
        }
    }
	return getJsonData(rtData, rtData.weather, name)
}

private Map getTwcWeather(rtData, name) {
	List parts = name.tokenize('.[');
    rtData.twcWeather = rtData.twcWeather ?: [:]
    if (parts.size() > 0) {
    	def dataFeature = parts[0]
        if (rtData.twcWeather[dataFeature] == null) {
            switch (dataFeature) {
                case 'alerts':
                    rtData.twcWeather[dataFeature] = app.getTwcAlerts()
                    break
                case 'conditions':
                    rtData.twcWeather[dataFeature] = app.getTwcConditions()
                    break
                case 'forecast':
                    rtData.twcWeather[dataFeature] = app.getTwcForecast()
                    break
                case 'location':
                    rtData.twcWeather[dataFeature] = app.getTwcLocation()?.location
                    break
            }
        }
    }
	return getJsonData(rtData, rtData.twcWeather, name)
}

private Map getNFLDataFeature(dataFeature) {
	def requestParams = [
        uri:  "https://api.webcore.co/nfl/$dataFeature",
        query: method == "GET" ? data : null
    ]
	httpGet(requestParams) { response ->
        if ((response.status == 200) && response.data && !binary) {
            try {
                return response.data instanceof Map ? response.data : (LinkedHashMap) new groovy.json.JsonSlurper().parseText(response.data)
            } catch (all) {
                return null
            }
        }
        return null
    }
}

private Map getNFL(rtData, name) {
	List parts = name.tokenize('.');
    rtData.nfl = rtData.nfl ?: [:]
    if (parts.size() > 0) {
    	def dataFeature = parts[0].tokenize('[')[0]
        if (rtData.nfl[dataFeature] == null) {
        	rtData.nfl[dataFeature] = getNFLDataFeature(dataFeature)
        }
    }
	return getJsonData(rtData, rtData.nfl, name, 'NFL')
}

private Map getIncidents(rtData, name) {
	initIncidents(rtData)
	return getJsonData(rtData, rtData.incidents, name)
}


private initIncidents(rtData) {
	if (rtData.incidents instanceof List) return;
	def incidentThreshold = now() - 604800000
	rtData.incidents = hubUID ? [] : location.activeIncidents.collect{[date: it.date.time, title: it.getTitle(), message: it.getMessage(), args: it.getMessageArgs(), sourceType: it.getSourceType()]}.findAll{ it.date >= incidentThreshold }
}

private Map getVariable(rtData, name) {
	def var = parseVariableName(name)
	name = sanitizeVariableName(var.name)
	if (!name) return [t: "error", v: "Invalid empty variable name"]
    def result
	if (name.startsWith("@")) {
    	result = rtData.globalVars[name]
        result.v = cast(rtData, result.v, result.t)
        if (!(result instanceof Map)) result = [t: "error", v: "Variable '$name' not found"]
	} else {
		if (name.startsWith('$')) {
        	if (name.startsWith('$args.') && (name.size() > 6)) {
            	result = getArgument(rtData, name.substring(6))
        	} else if (name.startsWith('$args[') && (name.size() > 6)) {
            	result = getArgument(rtData, name.substring(5))
        	} else if (name.startsWith('$json.') && (name.size() > 6)) {
            	result = getJson(rtData, name.substring(6))
        	} else if (name.startsWith('$json[') && (name.size() > 6)) {
            	result = getJson(rtData, name.substring(5))
        	} else if (name.startsWith('$places.') && (name.size() > 8)) {
            	result = getPlaces(rtData, name.substring(7))
        	} else if (name.startsWith('$places[') && (name.size() > 8)) {
            	result = getPlaces(rtData, name.substring(7))
            } else if (name.startsWith('$response.') && (name.size() > 10)) {
            	result = getResponse(rtData, name.substring(10))
            } else if (name.startsWith('$response[') && (name.size() > 10)) {
            	result = getResponse(rtData, name.substring(9))
            } else if (name.startsWith('$nfl.') && (name.size() > 5)) {
            	result = getNFL(rtData, name.substring(5))
            } else if (name.startsWith('$weather.') && (name.size() > 9)) {
            	result = getWeather(rtData, name.substring(9))
            } else if (name.startsWith('$twcweather.') && (name.size() > 12)) {
            	result = getTwcWeather(rtData, name.substring(12))
        	} else if (name.startsWith('$incidents.') && (name.size() > 11)) {
            	result = getIncidents(rtData, name.substring(11))
        	} else if (name.startsWith('$incidents[') && (name.size() > 11)) {
            	result = getIncidents(rtData, name.substring(10))
            } else {
				result = rtData.systemVars[name]
            	if (!(result instanceof Map)) result = [t: "error", v: "Variable '$name' not found"]
            	if (result && result.d) {
	            	result = [t: result.t, v: getSystemVariableValue(rtData, name)]
	            }
            }
		} else {
			def localVar = rtData.localVars[name]
            if (!(localVar instanceof Map)) {
            	result = [t: "error", v: "Variable '$name' not found"]
            } else {
            	result = [t: localVar.t, v: localVar.v]
                //make a local copy of the list
                if (result.v instanceof List) result.v = [] + result.v
                //make a local copy of the map
                if (result.v instanceof Map) result.v = [:] + result.v
            }
		}
	}
    if (result && (result.t.endsWith(']'))) {
    	result.t = result.t.replace('[]', '')
    	if ((result.v instanceof Map) && (var.index != null) && (var.index != '')) {
        	Map indirectVar = getVariable(rtData, var.index)
            //indirect variable addressing
            if (indirectVar && (indirectVar.t != 'error')) {
                def value = indirectVar.t == 'decimal' ? cast(rtData, indirectVar.v, 'integer', indirectVar.t) : indirectVar.v
                def dataType = indirectVar.t == 'decimal' ? 'integer' : indirectVar.t
                var.index = cast(rtData, value, 'string', dataType)
            }
        	result.v = result.v[var.index]
//        } else {
        	//result.v = "$result.v"
        }
    } else {
        /*if (result && (result.t == 'device')) {
            def deviceIds = []
            def devices = []
            for(deviceId in ((result.v instanceof List) ? result.v : [result.v])) {
                deviceIds.push(deviceId)
            }
            result = [t: result.t, v: deviceIds]
        } else*/ if (result.v instanceof Map) {
            //we're dealing with an operand, let's parse it
            result = evaluateExpression(rtData, evaluateOperand(rtData, null, result.v), result.t)
        }
    }
    return [t: result.t, v: result.v]
}

private Map setVariable(rtData, name, value) {
	def var = parseVariableName(name)
	name = sanitizeVariableName(var.name)
	if (!name) return [t: "error", v: "Invalid empty variable name"]
	if (name.startsWith("@")) {
    	def variable = rtData.globalVars[name]
    	if (variable instanceof Map) {
        	//set global var
            variable.v = cast(rtData, value, variable.t)
            Map cache = rtData.gvCache ?: [:]
            cache[name] = variable
            rtData.gvCache = cache
            return variable
        }
	} else {
		def variable = rtData.localVars[var.name]
        if (variable instanceof Map) {
            //set value
            if (variable.t.endsWith(']')) {
            	//we're dealing with a list
                variable.v = (variable.v instanceof Map) ? variable.v : [:]
                Map indirectVar = getVariable(rtData, var.index)
                //indirect variable addressing
                if (indirectVar && (indirectVar.t != 'error')) {
                    var.index = cast(rtData, indirectVar.v, 'string', indirectVar.t)
                }
                variable.v[var.index] = cast(rtData, value, variable.t.replace('[]', ''))
            } else {
            	variable.v = cast(rtData, value, variable.t)
            }
            if (!variable.f) {
            	def vars = state.vars
                vars[name] = variable.v
                state.vars = vars
                atomicState.vars = vars
            }
            return variable

		}
	}
   	result = [t: 'error', v: 'Invalid variable']
}

def setLocalVariable(name, value) {
	name = sanitizeVariableName(name)
    if (!name || name.startsWith('@')) return
	def vars = atomicState.vars ?: [:]
    vars[name] = value
    atomicState.vars = vars
    return vars
}

/******************************************************************************/
/*** 																		***/
/*** EXPRESSION FUNCTIONS													***/
/*** 																		***/
/******************************************************************************/

def Map proxyEvaluateExpression(rtData, expression, dataType = null) {
	resetRandomValues()
    rtData = getRunTimeData(rtData)
	def result = evaluateExpression(rtData, expression, dataType)
    if ((result.t == 'device') && (result.a)) {
    	def attr  = rtData.attributes[result.a]
    	result = evaluateExpression(rtData, result, attr && attr.t ? attr.t : 'string')
    }
    return result
}
private Map simplifyExpression(expression) {
	while ((expression.t == 'expression') && expression.i && (expression.i.size() == 1)) expression = expression.i[0]
    return expression
}

private Map evaluateExpression(rtData, expression, dataType = null) {
    //if dealing with an expression that has multiple items, let's evaluate each item one by one
    //let's evaluate this expression
    if (!expression) return [t: 'error', v: 'Null expression']
    //not sure what it was needed for - need to comment more
    //if (expression && expression.v instanceof Map) return evaluateExpression(rtData, expression.v, expression.t)
    expression = simplifyExpression(expression)
    def time = now()
    def result = expression
    switch (expression.t) {
        case "string":
        case "integer":
        case "int32":
        case "int64":
        case "long":
        case "decimal":
        case "boolean":
        case "time":
        case "date":
        case "datetime":
        	result = [t: expression.t, v: cast(rtData, expression.v, expression.t, expression.t)]
        	break
        case "enum":
        case "error":
        case "phone":
        case "uri":
        case "text":
        	result = [t: 'string', v: cast(rtData, expression.v, 'string', expression.t)]
        	break
		case "bool":
        	result = [t: "boolean", v: cast(rtData, expression.v, "boolean", expression.t)]
        	break
        case "number":
        case "float":
        case "double":
        	result = [t: "decimal", v: cast(rtData, expression.v, "decimal", expression.t)]
			result = expression
        	break
        case "duration":
        	long multiplier = 1
            switch (expression.vt) {
            	case 'ms': multiplier = 1; break;
            	case 's': multiplier = 1000; break;
            	case 'm': multiplier = 60000; break;
            	case 'h': multiplier = 3600000; break;
            	case 'd': multiplier = 86400000; break;
            	case 'w': multiplier = 604800000; break;
            	case 'n': multiplier = 2592000000; break;
            	case 'y': multiplier = 31536000000; break;
            }
        	result = [t: "long", v: (long) cast(rtData, cast(rtData, expression.v, 'long') * multiplier, "long")]
        	break
        case "variable":
        	//get variable as {n: name, t: type, v: value}
           	result = [t: 'error', v: 'Invalid variable']
        	result = getVariable(rtData, expression.x + (expression.xi != null ? '[' + expression.xi + ']' : ''))
        	break
        case "device":
        	//get variable as {n: name, t: type, v: value}
            if (expression.v instanceof List) {
            	//already parsed
                result = expression
            } else {
                def deviceIds = (expression.id instanceof List) ? expression.id : (expression.id ? [expression.id] : [])
                if (!deviceIds.size()) {
                    def var = getVariable(rtData, expression.x)
                    if (var) {
                    	if (var.t == 'device') {
                        	deviceIds = var.v
                        } else {
                        	def device = getDevice(rtData, var.v)
                            if (device) deviceIds = [hashId(device.id)]
                        }
                    }
                }
				result = [t: 'device', v: deviceIds, a: expression.a]
            }
        	break
        case "operand":
        	result = [t: "string", v: cast(rtData, expression.v, "string")]
        	break
        case "function":
            def fn = "func_${expression.n}"
            //in a function, we look for device parameters, they may be lists - we need to reformat all parameters to send them to the function properly
            try {
				def params = []
                if (expression.i && expression.i.size()) {
                    for (i in expression.i) {
                    	def param = simplifyExpression(i)
                        if ((param.t == 'device') || (param.t == 'variable')) {
                        	//if multiple devices involved, we need to spread the param into multiple params
                            param = evaluateExpression(rtData, param)
                            def sz = param.v instanceof List ? param.v.size() : 1
                            switch (sz) {
                            	case 0: break;
                            	case 1: params.push(param); break;
                                default:
		                            for (v in param.v) {
                                    	params.push([t: param.t, a: param.a, v: [v]])
                                    }
                            }
                        } else {
                        	params.push(param);
                        }
                    }
                }
				result = "$fn"(rtData, params)
			} catch (all) {
				//log error
                result = [t: "error", v: all]
			}
        	break
        case "expression":
        	//if we have a single item, we simply traverse the expression
        	List items = []
            def operand = -1
            def lastOperand = -1
        	for(item in expression.i) {
	            if (item.t == "operator") {
                	if (operand < 0) {
                    	switch (item.o) {
                        	case '+':
                            case '-':
                            case '**':
                        	case '&':
                        	case '|':
                            case '^':
                        	case '~':
                        	case '~&':
                        	case '~|':
                        	case '~^':
                        	case '<':
                        	case '>':
                        	case '<=':
                        	case '>=':
                        	case '==':
                        	case '!=':
                        	case '<>':
                        	case '<<':
                        	case '>>':
                        	case '!':
                        	case '!!':
                        	case '?':
                            	items.push([t: integer, v: 0, o: item.o])
                                break;
                        	case ':':
                            	if (lastOperand >= 0) {
                                	//groovy-style support for (object ?: value)
                                	items.push(items[lastOperand] + [o: item.o])
                                } else {
                            		items.push([t: integer, v: 0, o: item.o])
                                }
                                break;
                        	case '*':
                            case '/':
                            	items.push([t: integer, v: 1, o: item.o])
                                break;
                        	case '&&':
                        	case '!&':
                            	items.push([t: boolean, v: true, o: item.o])
                                break;
                        	case '||':
                        	case '!|':
                        	case '^^':
                        	case '!^':
                            	items.push([t: boolean, v: false, o: item.o])
                                break;
                        }
                    } else {
                    	items[operand].o = item.o;
                        operand = -1;
                    }
	            } else {
	                items.push(evaluateExpression(rtData, item) + [:])
                    operand = items.size() - 1
                    lastOperand = operand
	            }
	        }
            //clean up operators, ensure there's one for each
            def idx = 0
            for(item in items) {
            	if (!item.o) {
                	switch (item.t) {
                    	case "integer":
                    	case "float":
                    	case "double":
                    	case "decimal":
                    	case "number":
                        	def nextType = 'string'
                        	if (idx < items.size() - 1) nextType = items[idx+1].t
                        	item.o = (nextType == 'string' || nextType == 'text') ? '+' : '*';
                            break;
                        default:
                        	item.o = '+';
                            break;
                    }
                }
                idx++
            }
            //do the job
            idx = 0
            def secondary = false
            while (items.size() > 1) {
            	//ternary
                if ((items.size() == 3) && (items[0].o == '?') && (items[1].o == ':')) {
                	//we have a ternary operator
                    if (evaluateExpression(rtData, items[0], 'boolean').v) {
                    	items = [items[1]]
                    } else {
                    	items = [items[2]]
                    }
                    items[0].o = null;
                    break
                }
	           	//order of operations :D
                idx = 0
                //#2 	 !   !!   ~   - 	Logical negation, logical double-negation, bitwise NOT, and numeric negation unary operators
                for (item in items) {
                	if (((item.o) == '!') || ((item.o) == '!!') || ((item.o) == '~') || (item.t == null && item.o == '-')) break;
                    secondary = true
                    idx++
                }
                //#3 	** 	Exponent operator
                if (idx >= items.size()) {
	                //we then look for power **
                    idx = 0
	                for (item in items) {
    	            	if ((item.o) == '**') break;
        	            idx++
            	    }
                }
                //#4 	*   /   \   % MOD 	Multiplication, division, modulo
                if (idx >= items.size()) {
                    //we then look for * or /
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '*') || ((item.o) == '/') || ((item.o) == '\\') || ((item.o) == '%')) break;
                        idx++
                    }
                }
                //#5 	+   - 	Addition and subtraction
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '+') || ((item.o) == '-')) break;
                        idx++
                    }
                }
                //#6 	<<   >> 	Shift left and shift right operators
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '<<') || ((item.o) == '>>')) break;
                        idx++
                    }
                }
                //#7 	<   <=   >   >= 	Comparisons: less than, less than or equal to, greater than, greater than or equal to
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '>') || ((item.o) == '<') || ((item.o) == '>=') || ((item.o) == '<=')) break;
                        idx++
                    }
                }
                //#8 	==   != 	Comparisons: equal and not equal
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '==') || ((item.o) == '!=') || ((item.o) == '<>')) break;
                        idx++
                    }
                }
                //#9 	& 	Bitwise AND
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '&') || ((item.o) == '~&')) break;
                        idx++
                    }
                }
                //#10 	^ 	Bitwise exclusive OR (XOR)
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '^') || ((item.o) == '~^')) break;
                        idx++
                    }
                }
                //#11 	| 	Bitwise inclusive (normal) OR
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '|') || ((item.o) == '~|')) break;
                        idx++
                    }
                }
                //#12 	&& 	Logical AND
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '&&') || ((item.o) == '!&')) break;
                        idx++
                    }
                }
                //#13 	^^ 	Logical XOR
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '^^') || ((item.o) == '~^')) break;
                        idx++
                    }
                }
                //#14 	|| 	Logical OR
                if (idx >= items.size()) {
                    idx = 0
                    for (item in items) {
                        if (((item.o) == '||') || ((item.o) == '!|')) break;
                        idx++
                    }
                }
                if (idx >= items.size()) {
                	//just get the first one
                	idx = 0;
                }
                if (idx >= items.size() - 1) idx = 0
                //we're onto something
                def v = null
                def o = items[idx].o
                def a1 = items[idx].a
                def t1 = items[idx].t
                def v1 = items[idx].v
                def a2 = items[idx + 1].a
                def t2 = items[idx + 1].t
                def v2 = items[idx + 1].v
                def t = t1
                //fix-ups
                //integer with decimal gives decimal, also *, / require decimals
                if ((t1 == 'device') && a1) {
                	def attr = rtData.attributes[a1]
                    t1 = attr ? attr.t : 'string'
                }
                if ((t2 == 'device') && a2) {
                	def attr = rtData.attributes[a2]
                    t2 = attr ? attr.t : 'string'
                }
                if ((t1 == 'device') && (t2 == 'device') && ((o == '+') || (o == '-'))) {
					v1 = (v1 instanceof List) ? v1 : [v1]
					v2 = (v2 instanceof List) ? v2 : [v2]
                    v = (o == '+') ? v1 + v2 : v1 - v2
        	        //set the results
    	            items[idx + 1].t = 'device'
                    items[idx + 1].v = v
                } else {
                	def t1d = (t1 == 'datetime') || (t1 == 'date') || (t1 == 'time')
                    def t2d = (t2 == 'datetime') || (t2 == 'date') || (t2 == 'time')
                	def t1i = (t1 == 'number') || (t1 == 'integer') || (t1 == 'long')
                	def t2i = (t2 == 'number') || (t2 == 'integer') || (t2 == 'long')
                	def t1f = (t1 == 'decimal') || (t1 == 'float')
                	def t2f = (t2 == 'decimal') || (t2 == 'float')
                	def t1n = t1i || t1f
                	def t2n = t2i || t2f
                    //warn "Precalc ($t1) $v1 $o ($t2) $v2 >>> t1d = $t1d, t2d = $t2d, t1n = $t1n, t2n = $t2n", rtData
                	if (((o == '+') || (o == '-')) && (t1d || t2d) && (t1d || t1n) && (t2n || t2d)) {
                    	//if dealing with date +/- date/numeric then
                        if (t1n) {
                        	t = t2
                        } else if (t2n) {
                        	t = t1
                        } else {
                        	t = (t1 == 'date') && (t2 == 'date') ? 'date' : ((t1 == 'time') && (t2 == 'time') ? 'time' : 'datetime')
                        }
                    } else {
                    	if ((o == '+') || (o == '-')) {
                        	//devices and others play nice
                        	if (t1 == 'device') {
                            	t = t2
                                t1 = t2
                            } else if (t2 == 'device') {
                            	t = t1
                                t2 = t1
							}
                        }
                        if ((o == '*') || (o == '/') || (o == '-') || (o == '**')) {
                        	t = (t1i && t2i) ? ((t1 == 'long') || (t2 == 'long') ? 'long' : 'integer') : 'decimal'
                            t1 = t
                            t2 = t
                            //if ((t1 != 'number') && (t1 != 'integer') && (t1 != 'decimal') && (t1 != 'float') && (t1 != 'datetime') && (t1 != 'date') && (t1 != 'time')) t1 = 'decimal'
                            //if ((t2 != 'number') && (t2 != 'integer') && (t2 != 'decimal') && (t2 != 'float') && (t2 != 'datetime') && (t2 != 'date') && (t2 != 'time')) t2 = 'decimal'
                            //t = (t1 == 'datetime') || (t2 == 'datetime') ? 'datetime' : ((t1 == 'date') && (t2 == 'date') ? 'date' : ((t1 == 'time') && (t2 == 'time') ? 'time' : (((t1 == 'date') && (t2 == 'time')) || ((t1 == 'time') && (t2 == 'date')) ? 'datetime' : 'decimal')))
                        }
                        if ((o == '\\') || (o == '%') || (o == '&') || (o == '|') || (o == '^') || (o == '~&') || (o == '~|') || (o == '~^') || (o == '<<') || (o == '>>')) {
                            t = (t1 == 'long') || (t2 == 'long') ? 'long' : 'integer'
                            t1 = t
                            t2 = t
                        }
                        if ((o == '&&') || (o == '||') || (o == '^^') || (o == '!&') || (o == '!|') || (o == '!^') || (o == '!') || (o == '!!')) {
                            t1 = 'boolean'
                            t2 = 'boolean'
                            t = 'boolean'
                        }
                        if ((o == '+') && ((t1 == 'string') || (t1 == 'text') || (t2 == 'string') || (t2 == 'text'))) {
                            t1 = 'string';
                            t2 = 'string';
                            t = 'string'
                        }
                        if (t1n && t2n) {
                            t = (t1i && t2i) ? ((t1 == 'long') || (t2 == 'long') ? 'long' : 'integer') : 'decimal'
                            t1 = t
                            t2 = t
                        }
                        if ((o == '==') || (o == '!=') || (o == '<') || (o == '>') || (o == '<=') || (o == '>=') || (o == '<>')) {
                            if (t1 == 'device') t1 = 'string'
                            if (t2 == 'device') t2 = 'string'
                            t1 = t1 == 'string' ? t2 : t1
                            t2 = t2 == 'string' ? t1 : t2
                            t = 'boolean'
                        }
                    }
                    v1 = evaluateExpression(rtData, items[idx], t1).v
	                v2 = evaluateExpression(rtData, items[idx + 1], t2).v
                    v1 = v1 == "null" ? null : v1
                    v2 = v2 == "null" ? null : v2
                    switch (o) {
                    	case '?':
                    	case ':':
                        	error "Invalid ternary operator. Ternary operator's syntax is ( condition ? trueValue : falseValue ). Please check your syntax and try again.", rtData
                            v = '';
                            break
        	            case '-':
            	        	v = v1 - v2
                	    	break
	                    case '*':
    	                	v = v1 * v2
        	            	break
            	        case '/':
                	    	v = (v2 != 0 ? v1 / v2 : 0)
	                    	break
            	        case '\\':
                	    	v = (int) Math.floor(v2 != 0 ? v1 / v2 : 0)
	                    	break
            	        case '%':
                	    	v = (int) (v2 != 0 ? v1 % v2 : 0)
	                    	break
    	                case '**':
        	            	v = v1 ** v2
            	        	break
                	    case '&':
	                    	v = v1 & v2
    	                	break
        	            case '|':
            	        	v = v1 | v2
                	    	break
    	                case '^':
        	            	v = v1 ^ v2
            	        	break
                	    case '~&':
	                    	v = ~(v1 & v2)
    	                	break
        	            case '~|':
            	        	v = ~(v1 | v2)
                	    	break
    	                case '~^':
        	            	v = ~(v1 ^ v2)
            	        	break
    	                case '~':
        	            	v = ~v2
            	        	break
    	                case '<<':
        	            	v = v1 << v2
            	        	break
    	                case '>>':
        	            	v = v1 >> v2
            	        	break
                	    case '&&':
	                    	v = !!v1 && !!v2
    	                	break
        	            case '||':
            	        	v = !!v1 || !!v2
                	    	break
        	            case '^^':
            	        	v = !v1 != !v2
                	    	break
                	    case '!&':
	                    	v = !(!!v1 && !!v2)
    	                	break
        	            case '!|':
            	        	v = !(!!v1 || !!v2)
                	    	break
        	            case '!^':
            	        	v = !(!v1 != !v2)
                	    	break
                	    case '==':
	                    	v = v1 == v2
    	                	break
        	            case '!=':
        	            case '<>':
            	        	v = v1 != v2
                	    	break
        	            case '<':
            	        	v = v1 < v2
                	    	break
        	            case '>':
            	        	v = v1 > v2
                	    	break
        	            case '<=':
            	        	v = v1 <= v2
                	    	break
        	            case '>=':
            	        	v = v1 >= v2
                	    	break
        	            case '!':
            	        	v = !v2
                	    	break
        	            case '!!':
            	        	v = !!v2
                	    	break
	                    case '+':
    	                default:
        	                v = t == 'string' ? "$v1$v2" : v1 + v2
            	        	break
                	}

					if (rtData.logging > 2) debug "Calculating ($t1) $v1 $o ($t2) $v2 >> ($t) $v", rtData

                    //set the results
                    items[idx + 1].t = t
                    items[idx + 1].v = cast(rtData, v, t)
                }
                def sz = items.size()
                items.remove(idx)
            }
    	    result = items[0] ? ((items[0].t == 'device') ? items[0] : evaluateExpression(rtData, items[0])) : [t: 'dynamic', v: null]
	        break
    }
    //return the value, either directly or via cast, if certain data type is requested
  	//when dealing with devices, they need to be "converted" unless the request is to return devices
    if (dataType && (dataType != 'device') && (result.t == 'device')) {
    	//if not a list, make it a list
    	if (!(result.v instanceof List)) result.v = [result.v]
        switch (result.v.size()) {
            case 0: result = [t: 'error', v: 'Empty device list']; break;
            case 1: result = getDeviceAttribute(rtData, result.v[0], result.a, result.i); break;
            default: result = [t: 'string', v: buildDeviceAttributeList(rtData, result.v, result.a)]; break;
        }
    }
    if (dataType) {
    	result = [t: dataType, v: cast(rtData, result.v, dataType, result.t)] + (result.a ? [a: result.a] : [:]) + (result.i ? [a: result.i] : [:])
    }
    result.d = now() - time;
	return result
}

private buildList(list, suffix = 'and') {
    if (!list) return ''
    if (!(list instanceof List)) list = [list]
	def cnt = 1
	def result = ""
	for (item in list) {
		result += "$item" + (cnt < list.size() ? (cnt == list.size() - 1 ? " $suffix " : ", ") : "")
		cnt++
	}
	return result;
}

private buildDeviceList(rtData, devices, suffix = 'and') {
    if (!devices) return ''
    if (!(devices instanceof List)) devices = [devices]
    def list = []
	for (device in devices) {
    	def dev = getDevice(rtData, device)
        if (dev) list.push(dev)
	}
	return buildList(list, suffix);
}

private buildDeviceAttributeList(rtData, devices, attribute, suffix = 'and') {
    if (!devices) return ''
    if (!(devices instanceof List)) devices = [devices]
    def list = []
	for (device in devices) {
    	def value = getDeviceAttribute(rtData, device, attribute).v
        list.push(value)
	}
	return buildList(list, suffix);
}


/******************************************************************************/
/*** dewPoint returns the calculated dew point temperature					***/
/*** Usage: dewPoint(temperature, relativeHumidity[, scale])				***/
/******************************************************************************/
private func_dewpoint(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting dewPoint(temperature, relativeHumidity[, scale])"];
    }
    double t = evaluateExpression(rtData, params[0], 'decimal').v
    double rh = evaluateExpression(rtData, params[1], 'decimal').v
    //if no temperature scale is provided, we assume the location's temperature scale
    boolean fahrenheit = cast(rtData, params.size() > 2 ? evaluateExpression(rtData, params[2]).v : location.temperatureScale, "string").toUpperCase() == "F"
    if (fahrenheit) {
    	//convert temperature to Celsius
        t = (t - 32.0) * 5.0 / 9.0
    }
    //convert rh to percentage
    if ((rh > 0) && (rh < 1)) {
    	rh = rh * 100.0
    }
    double b = (Math.log(rh / 100) + ((17.27 * t) / (237.3 + t))) / 17.27
	double result = (237.3 * b) / (1 - b)
    if (fahrenheit) {
    	//convert temperature back to Fahrenheit
        result = result * 9.0 / 5.0 + 32.0
    }
    return [t: "decimal", v: result]
}

/******************************************************************************/
/*** celsius converts temperature from Fahrenheit to Celsius				***/
/*** Usage: celsius(temperature)											***/
/******************************************************************************/
private func_celsius(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting celsius(temperature)"];
    }
    double t = evaluateExpression(rtData, params[0], 'decimal').v
    //convert temperature to Celsius
    return [t: "decimal", v: (double) (t - 32.0) * 5.0 / 9.0]
}


/******************************************************************************/
/*** fahrenheit converts temperature from Celsius to Fahrenheit				***/
/*** Usage: fahrenheit(temperature)											***/
/******************************************************************************/
private func_fahrenheit(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting fahrenheit(temperature)"];
    }
    double t = evaluateExpression(rtData, params[0], 'decimal').v
    //convert temperature to Fahrenheit
    return [t: "decimal", v: (double) t * 9.0 / 5.0 + 32.0]
}


/******************************************************************************/
/*** fahrenheit converts temperature between Celsius and Fahrenheit if the  ***/
/*** units differ from location.temperatureScale                            ***/
/*** Usage: convertTemperatureIfNeeded(celsiusTemperature, 'C')             ***/
/******************************************************************************/
private func_converttemperatureifneeded(rtData, params) {
    if (!params || !(params instanceof List) || (params.size() < 2)) {
        return [t: "error", v: "Invalid parameters. Expecting convertTemperatureIfNeeded(temperature, unit)"];
    }
    double t = evaluateExpression(rtData, params[0], 'decimal').v
    def u = evaluateExpression(rtData, params[1], 'string').v.toUpperCase()
    //convert temperature to Fahrenheit
    switch (location.temperatureScale) {
       case u: return [t: "decimal", v: t]
       case 'F': return func_celsius(rtData, [params[0]])
       case 'C': return func_fahrenheit(rtData, [params[0]])
   }
}

/******************************************************************************/
/*** integer converts a decimal value to it's integer value					***/
/*** Usage: integer(decimal or string)										***/
/******************************************************************************/
private func_integer(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting integer(decimal or string)"];
    }
    return [t: "integer", v: evaluateExpression(rtData, params[0], 'integer').v]
}
private func_int(rtData, params) { return func_integer(rtData, params) }

/******************************************************************************/
/*** decimal/float converts an integer value to it's decimal value			***/
/*** Usage: decimal(integer or string)										***/
/******************************************************************************/
private func_decimal(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting decimal(integer or string)"];
    }
    return [t: "decimal", v: evaluateExpression(rtData, params[0], 'decimal').v]
}
private func_float(rtData, params) { return func_decimal(rtData, params) }
private func_number(rtData, params) { return func_decimal(rtData, params) }

/******************************************************************************/
/*** string converts an value to it's string value							***/
/*** Usage: string(anything)												***/
/******************************************************************************/
private func_string(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting string(anything)"];
    }
	def result = ''
    for(param in params) {
    	result += evaluateExpression(rtData, param, 'string').v
    }
    return [t: "string", v: result]
}
private func_concat(rtData, params) { return func_string(rtData, params) }
private func_text(rtData, params) { return func_string(rtData, params) }

/******************************************************************************/
/*** boolean converts a value to it's boolean value							***/
/*** Usage: boolean(anything)												***/
/******************************************************************************/
private func_boolean(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting boolean(anything)"];
    }
    return [t: "boolean", v: evaluateExpression(rtData, params[0], 'boolean').v]
}
private func_bool(rtData, params) { return func_boolean(rtData, params) }

/******************************************************************************/
/*** sqr converts a decimal value to it's square decimal value				***/
/*** Usage: sqr(integer or decimal or string)								***/
/******************************************************************************/
private func_sqr(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting sqr(integer or decimal or string)"];
    }
    return [t: "decimal", v: evaluateExpression(rtData, params[0], 'decimal').v ** 2]
}

/******************************************************************************/
/*** sqrt converts a decimal value to it's square root decimal value		***/
/*** Usage: sqrt(integer or decimal or string)								***/
/******************************************************************************/
private func_sqrt(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting sqrt(integer or decimal or string)"];
    }
    return [t: "decimal", v: Math.sqrt(evaluateExpression(rtData, params[0], 'decimal').v)]
}

/******************************************************************************/
/*** power converts a decimal value to it's power decimal value				***/
/*** Usage: power(integer or decimal or string, power)						***/
/******************************************************************************/
private func_power(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting sqrt(integer or decimal or string, power)"];
    }
    return [t: "decimal", v: evaluateExpression(rtData, params[0], 'decimal').v ** evaluateExpression(rtData, params[1], 'decimal').v]
}

/******************************************************************************/
/*** round converts a decimal value to it's rounded value					***/
/*** Usage: round(decimal or string[, precision])							***/
/******************************************************************************/
private func_round(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting round(decimal or string[, precision])"];
    }
    int precision = (params.size() > 1) ? evaluateExpression(rtData, params[1], 'integer').v : 0
    return [t: "decimal", v: Math.round(evaluateExpression(rtData, params[0], 'decimal').v * (10 ** precision)) / (10 ** precision)]
}

/******************************************************************************/
/*** floor converts a decimal value to it's closest lower integer value		***/
/*** Usage: floor(decimal or string)										***/
/******************************************************************************/
private func_floor(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting floor(decimal or string)"];
    }
    return [t: "integer", v: cast(rtData, Math.floor(evaluateExpression(rtData, params[0], 'decimal').v), 'integer')]
}

/******************************************************************************/
/*** ceiling converts a decimal value to it's closest higher integer value	***/
/*** Usage: ceiling(decimal or string)										***/
/******************************************************************************/
private func_ceiling(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting ceiling(decimal or string)"];
    }
    return [t: "integer", v: cast(rtData, Math.ceil(evaluateExpression(rtData, params[0], 'decimal').v), 'integer')]
}
private func_ceil(rtData, params) { return func_ceiling(rtData, params) }


/******************************************************************************/
/*** sprintf converts formats a series of values into a string				***/
/*** Usage: sprintf(format, arguments)										***/
/******************************************************************************/
private func_sprintf(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting sprintf(format, arguments)"];
    }
    def format = evaluateExpression(rtData, params[0], 'string').v
    List args = []
    for (int x = 1; x < params.size(); x++) {
    	args.push(evaluateExpression(rtData, params[x]).v)
    }
    try {
        return [t: "string", v: sprintf(format, args)]
    } catch(all) {
    	return [t: "error", v: "$all"]
    }
}
private func_format(rtData, params) { return func_sprintf(rtData, params) }

/******************************************************************************/
/*** left returns a substring of a value									***/
/*** Usage: left(string, count)												***/
/******************************************************************************/
private func_left(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting left(string, count)"];
    }
    def value = evaluateExpression(rtData, params[0], 'string').v
    def count = evaluateExpression(rtData, params[1], 'integer').v
    if (count > value.size()) count = value.size()
    return [t: "string", v: value.substring(0, count)]
}

/******************************************************************************/
/*** right returns a substring of a value									***/
/*** Usage: right(string, count)												***/
/******************************************************************************/
private func_right(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting right(string, count)"];
    }
    def value = evaluateExpression(rtData, params[0], 'string').v
    def count = evaluateExpression(rtData, params[1], 'integer').v
    if (count > value.size()) count = value.size()
    return [t: "string", v: value.substring(value.size() - count, value.size())]
}

/******************************************************************************/
/*** strlen returns the length of a string value							***/
/*** Usage: strlen(string)													***/
/******************************************************************************/
private func_strlen(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting strlen(string)"];
    }
    def value = evaluateExpression(rtData, params[0], 'string').v
    return [t: "integer", v: value.size()]
}
private func_length(rtData, params) { return func_strlen(rtData, params) }


/******************************************************************************/
/*** coalesce returns the first non-empty parameter							***/
/*** Usage: coalesce(value1[, value2[, ..., valueN]])						***/
/******************************************************************************/
private func_coalesce(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting coalesce(value1[, value2[, ..., valueN]])"];
    }
    for (i = 0; i < params.size(); i++) {
	    def value = evaluateExpression(rtData, params[0])
        if (!((value.v instanceof List ? (value.v == [null]) || (value.v == []) || (value.v == ['null']) : false) || (value.v == null) || (value.t == 'error') || (value.v == 'null') || (cast(rtData, value.v, 'string') == ''))) {
        	return value
        }
    }
    return [t: "dynamic", v: null]
}


/******************************************************************************/
/*** trim removes leading and trailing spaces from a string					***/
/*** Usage: trim(value)														***/
/******************************************************************************/
private func_trim(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting trim(value)"];
    }
    def value = evaluateExpression(rtData, params[0], 'string').v.trim()
    return [t: "string", v: value]
}

/******************************************************************************/
/*** trimleft removes leading spaces from a string							***/
/*** Usage: trimLeft(value)													***/
/******************************************************************************/
private func_trimleft(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting trimLeft(value)"];
    }
    def value = evaluateExpression(rtData, params[0], 'string').v.replaceAll('^\\s+', '')
    return [t: "string", v: value]
}
private func_ltrim(rtData, params) { return func_trimleft(rtData, params) }

/******************************************************************************/
/*** trimright removes trailing spaces from a string						***/
/*** Usage: trimRight(value)												***/
/******************************************************************************/
private func_trimright(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting trimRight(value)"];
    }
    def value = evaluateExpression(rtData, params[0], 'string').v.replaceAll('\\s+$', '')
    return [t: "string", v: value]
}
private func_rtrim(rtData, params) { return func_trimright(rtData, params) }

/******************************************************************************/
/*** substring returns a substring of a value								***/
/*** Usage: substring(string, start, count)									***/
/******************************************************************************/
private func_substring(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting substring(string, start, count)"];
    }
    def value = evaluateExpression(rtData, params[0], 'string').v
    def start = evaluateExpression(rtData, params[1], 'integer').v
   	def count = params.size() > 2 ? evaluateExpression(rtData, params[2], 'integer').v : null
    def end = null
    def result = ''
    if ((start < value.size()) && (start > -value.size())) {
        if (count != null) {
        	if (count < 0) {
           		//reverse
                start = start < 0 ? -start : value.size() - start
                count = - count
                value = value.reverse()
            }
        	if (start >= 0) {
            	if (count > value.size() - start) count = value.size() - start
            } else {
            	if (count > -start) count = -start
            }
        }
        start = start >= 0 ? start : value.size() + start
        if (count > value.size() - start) count = value.size() - start
        result = (count == null) ? value.substring(start) : value.substring(start, start + count)
    }
    return [t: "string", v: result]
}
private func_substr(rtData, params) { return func_substring(rtData, params) }
private func_mid(rtData, params) { return func_substring(rtData, params) }

/******************************************************************************/
/*** replace replaces a search text inside of a value						***/
/*** Usage: replace(string, search, replace[, [..], search, replace])		***/
/******************************************************************************/
private func_replace(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 3) || (params.size() %2 != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting replace(string, search, replace[, [..], search, replace])"];
    }
    def value = evaluateExpression(rtData, params[0], 'string').v
    int cnt = Math.floor((params.size() - 1) / 2)
    for (int i = 0; i < cnt; i++) {
    	def search = evaluateExpression(rtData, params[i * 2 + 1], 'string').v
        def replace = evaluateExpression(rtData, params[i * 2 + 2], 'string').v
        if ((search.size() > 2) && search.startsWith('/') && search.endsWith('/')) {
        	search = ~search.substring(1, search.size() - 1)
	        value = value.replaceAll(search, replace)
        } else {
	        value = value.replace(search, replace)
        }
    }
    return [t: "string", v: value]
}

/******************************************************************************/
/*** rangeValue returns the matching value in a range						***/
/*** Usage: rangeValue(input, defaultValue, point1, value1[, [..], pointN, valueN])***/
/******************************************************************************/
private func_rangevalue(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2) || (params.size() %2 != 0)) {
    	return [t: "error", v: "Invalid parameters. Expecting rangeValue(input, defaultValue, point1, value1[, [..], pointN, valueN])"];
    }
    def input = evaluateExpression(rtData, params[0], 'decimal').v
    def value = params[1]
    int cnt = Math.floor((params.size() - 2) / 2)
    for (int i = 0; i < cnt; i++) {
    	def point = evaluateExpression(rtData, params[i * 2 + 2], 'decimal').v
        if (input >= point) value = params[i * 2 + 3]
    }
    return value
}

/******************************************************************************/
/*** rainbowValue returns the matching value in a range						***/
/*** Usage: rainbowValue(input, minInput, minColor, maxInput, maxColor)		***/
/******************************************************************************/
private func_rainbowvalue(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 5)) {
    	return [t: "error", v: "Invalid parameters. Expecting rainbowValue(input, minColor, minValue, maxInput, maxColor)"];
    }
    def input = evaluateExpression(rtData, params[0], 'integer').v
    def minInput = evaluateExpression(rtData, params[1], 'integer').v
    def minColor = getColor(evaluateExpression(rtData, params[2], 'string').v)
    def maxInput = evaluateExpression(rtData, params[3], 'integer').v
    def maxColor = getColor(evaluateExpression(rtData, params[4], 'string').v)
    if (minInput > maxInput) {
    	def x = minInput
        minInput = maxInput
        maxInput = x
        x = minColor
        minColor = maxColor
        maxColor = x
    }
    input = (input < minInput ? minInput : (input > maxInput ? maxInput : input))
    if ((input == minInput) || (minInput == maxInput)) return [t: "string", v: minColor.hex]
    if (input == maxInput) return [t: "string", v: maxColor.hex]
    def start = hexToHsl(minColor.hex)
    def end = hexToHsl(maxColor.hex)
    float alpha = 1.0000000 * (input - minInput) / (maxInput - minInput + 1)
	def h = Math.round(start[0] - ((input - minInput) * (start[0] - end[0]) / (maxInput - minInput)))
    h = h < 0 ? h % 360 + 360 : h % 360
    int s = Math.round(start[1] + (end[1] - start[1]) * alpha)
    int l = Math.round(start[2] + (end[2] - start[2]) * alpha)
	return [t: "string", v: hslToHex(h, s, 2 * l)]
}

/******************************************************************************/
/*** indexOf finds the first occurrence of a substring in a string			***/
/*** Usage: indexOf(stringOrDeviceOrList, substringOrItem)							***/
/******************************************************************************/
private func_indexof(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2) || ((params[0].t != 'device') && (params.size() != 2))) {
    	return [t: "error", v: "Invalid parameters. Expecting indexOf(stringOrDeviceOrList, substringOrItem)"];
    }
    if ((params[0].t == 'device') && (params.size() > 2)) {
        def item = evaluateExpression(rtData, params[params.size() - 1], 'string').v
        for (int idx = 0; idx < params.size() - 1; idx++) {
        	def it = evaluateExpression(rtData, params[idx], 'string')
        	if (it.v == item) {
            	return [t: "integer", v: idx]
            }
        }
        return [t: "integer", v: -1]
    } else if (params[0].v instanceof Map) {
        def item = evaluateExpression(rtData, params[1], params[0].t).v
        def key = params[0].v.find{ it.value == item }?.key
        return [t: "string", v: key]
    } else {
    	def value = evaluateExpression(rtData, params[0], 'string').v
    	def substring = evaluateExpression(rtData, params[1], 'string').v
    	return [t: "integer", v: value.indexOf(substring)]
    }
}

/******************************************************************************/
/*** lastIndexOf finds the first occurrence of a substring in a string		***/
/*** Usage: lastIndexOf(string, substring)									***/
/******************************************************************************/
private func_lastindexof(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2) || ((params[0].t != 'device') && (params.size() != 2))) {
    	return [t: "error", v: "Invalid parameters. Expecting lastIndexOf(string, substring)"];
    }
    if ((params[0].t == 'device') && (params.size() > 2)) {
        def item = evaluateExpression(rtData, params[params.size() - 1], 'string').v
        for (int idx = params.size() - 2; idx >= 0; idx--) {
        	def it = evaluateExpression(rtData, params[idx], 'string')
        	if (it.v == item) {
            	return [t: "integer", v: idx]
            }
        }
        return [t: "integer", v: -1]
    } else if (params[0].v instanceof Map) {
        def item = evaluateExpression(rtData, params[1], params[0].t).v
        def key = params[0].v.find{ it.value == item }?.key
        return [t: "string", v: key]
    } else {
    	def value = evaluateExpression(rtData, params[0], 'string').v
    	def substring = evaluateExpression(rtData, params[1], 'string').v
    	return [t: "integer", v: value.lastIndexOf(substring)]
    }
}


/******************************************************************************/
/*** lower returns a lower case value of a string							***/
/*** Usage: lower(string)													***/
/******************************************************************************/
private func_lower(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting lower(string)"];
    }
    def result = ''
    for(param in params) {
    	result += evaluateExpression(rtData, param, 'string').v
    }
    return [t: "string", v: result.toLowerCase()]
}

/******************************************************************************/
/*** upper returns a upper case value of a string							***/
/*** Usage: upper(string)													***/
/******************************************************************************/
private func_upper(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting upper(string)"];
    }
    def result = ''
    for(param in params) {
    	result += evaluateExpression(rtData, param, 'string').v
    }
    return [t: "string", v: result.toUpperCase()]
}

/******************************************************************************/
/*** title returns a title case value of a string							***/
/*** Usage: title(string)													***/
/******************************************************************************/
private func_title(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting title(string)"];
    }
    def result = ''
    for(param in params) {
    	result += evaluateExpression(rtData, param, 'string').v
    }
    return [t: "string", v: result.tokenize(" ")*.toLowerCase()*.capitalize().join(" ")]
}

/******************************************************************************/
/*** avg calculates the average of a series of numeric values				***/
/*** Usage: avg(values)														***/
/******************************************************************************/
private func_avg(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting avg(value1, value2, ..., valueN)"];
    }
    double sum = 0
    for (param in params) {
    	sum += evaluateExpression(rtData, param, 'decimal').v
    }
    return [t: "decimal", v: sum / params.size()]
}

/******************************************************************************/
/*** median returns the value in the middle of a sorted array				***/
/*** Usage: median(values)													***/
/******************************************************************************/
private func_median(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting median(value1, value2, ..., valueN)"];
    }
    List data = params.collect{ evaluateExpression(rtData, it, 'dynamic') }.sort{ it.v }
    if (data.size()) {
    	return data[(int) Math.floor(data.size() / 2)]
    }
    return [t: 'dynamic', v: '']
}


/******************************************************************************/
/*** least returns the value that is least found a series of numeric values	***/
/*** Usage: least(values)													***/
/******************************************************************************/
private func_least(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting least(value1, value2, ..., valueN)"];
    }
    Map data = [:]
    for (param in params) {
    	def value = evaluateExpression(rtData, param, 'dynamic')
    	data[value.v] = [t: value.t, v: value.v, c: (data[value.v]?.c ?: 0) + 1]
    }
    def value = data.sort{ it.value.c }.collect{ it.value }[0]
    return [t: value.t, v: value.v]
}

/******************************************************************************/
/*** most returns the value that is most found a series of numeric values	***/
/*** Usage: most(values)													***/
/******************************************************************************/
private func_most(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting most(value1, value2, ..., valueN)"];
    }
    Map data = [:]
    for (param in params) {
    	def value = evaluateExpression(rtData, param, 'dynamic')
    	data[value.v] = [t: value.t, v: value.v, c: (data[value.v]?.c ?: 0) + 1]
    }
    def value = data.sort{ - it.value.c }.collect{ it.value }[0]
    return [t: value.t, v: value.v]
}

/******************************************************************************/
/*** sum calculates the sum of a series of numeric values					***/
/*** Usage: sum(values)														***/
/******************************************************************************/
private func_sum(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting sum(value1, value2, ..., valueN)"];
    }
    double sum = 0
    for (param in params) {
    	sum += evaluateExpression(rtData, param, 'decimal').v
    }
    return [t: "decimal", v: sum]
}

/******************************************************************************/
/*** variance calculates the standard deviation of a series of numeric values */
/*** Usage: stdev(values)													***/
/******************************************************************************/
private func_variance(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting variance(value1, value2, ..., valueN)"];
    }
    double sum = 0
    List values = []
    for (param in params) {
    	double value = evaluateExpression(rtData, param, 'decimal').v
        values.push(value)
        sum += value
    }
    double avg = sum / values.size()
    sum = 0
    for(int i  = 0; i < values.size(); i++) {
    	sum += (values[i] - avg) ** 2
    }
    return [t: "decimal", v: sum / values.size()]
}

/******************************************************************************/
/*** stdev calculates the standard deviation of a series of numeric values	***/
/*** Usage: stdev(values)													***/
/******************************************************************************/
private func_stdev(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting stdev(value1, value2, ..., valueN)"];
    }
    def result = func_variance(rtData, params)
    return [t: "decimal", v: Math.sqrt(result.v)]
}

/******************************************************************************/
/*** min calculates the minimum of a series of numeric values				***/
/*** Usage: min(values)														***/
/******************************************************************************/
private func_min(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting min(value1, value2, ..., valueN)"];
    }
    List data = params.collect{ evaluateExpression(rtData, it, 'dynamic') }.sort{ it.v }
    if (data.size()) {
    	return data[0]
    }
    return [t: 'dynamic', v: '']
}

/******************************************************************************/
/*** max calculates the maximum of a series of numeric values				***/
/*** Usage: max(values)														***/
/******************************************************************************/
private func_max(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting max(value1, value2, ..., valueN)"];
    }
    List data = params.collect{ evaluateExpression(rtData, it, 'dynamic') }.sort{ it.v }
    if (data.size()) {
    	return data[data.size() - 1]
    }
    return [t: 'dynamic', v: '']
}

/******************************************************************************/
/*** abs calculates the absolute value of a number							***/
/*** Usage: abs(number)														***/
/******************************************************************************/
private func_abs(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting abs(value)"];
    }
    def value = evaluateExpression(rtData, params[0], 'decimal').v
    def dataType = (value == Math.round(value) ? 'integer' : 'decimal')
    return [t: dataType, v: cast(rtData, Math.abs(value), dataType, 'decimal')]
}

/******************************************************************************/
/*** hslToHex converts a hue/saturation/level trio to it hex #rrggbb representation ***/
/*** Usage: hslToHex(hue, saturation, level)								***/
/******************************************************************************/
private func_hsltohex(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 3)) {
    	return [t: "error", v: "Invalid parameters. Expecting hsl(hue, saturation, level)"];
    }
    float hue = evaluateExpression(rtData, params[0], 'decimal').v
    float saturation = evaluateExpression(rtData, params[1], 'decimal').v
    float level = evaluateExpression(rtData, params[2], 'decimal').v
    return [t: 'string', v: hslToHex(hue, saturation, level)]
}

/******************************************************************************/
/*** count calculates the number of true/non-zero/non-empty items in a series of numeric values		***/
/*** Usage: count(values)														***/
/******************************************************************************/
private func_count(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "integer", v: 0];
    }
    def count = 0
    if ((params.size() == 1) && ((params[0].t == 'string') || (params[0].t == 'dynamic'))) {
    	def list = evaluateExpression(rtData, params[0], 'string').v.split(',').toList()
    	for (int i=0; i< list.size(); i++) {
	    	count += cast(rtData, list[i], 'boolean') ? 1 : 0
	    }
    } else {
    	for (param in params) {
	    	count += evaluateExpression(rtData, param, 'boolean').v ? 1 : 0
	    }
    }
    return [t: "integer", v: count]
}

/******************************************************************************/
/*** size returns the number of values provided								***/
/*** Usage: size(values)													***/
/******************************************************************************/
private func_size(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1)) {
    	return [t: "integer", v: 0];
    }
    def count = 0
    if ((params.size() == 1) && ((params[0].t == 'string') || (params[0].t == 'dynamic'))) {
    	def list = evaluateExpression(rtData, params[0], 'string').v.split(',').toList()
    	count = list.size()
    } else {
    	count = params.size()
    }
    return [t: "integer", v: count]
}

/******************************************************************************/
/*** age returns the number of milliseconds an attribute had the current value*/
/*** Usage: age([device:attribute])											***/
/******************************************************************************/
private func_age(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting age([device:attribute])"];
    }
    def param = evaluateExpression(rtData, params[0], 'device')
    if ((param.t == 'device') && (param.a) && param.v.size()) {
		def device = getDevice(rtData, param.v[0])
        if (device) {
        	def state = device.currentState(param.a)
            if (state) {
            	long result = now() - state.getDate().getTime()
                return [t: "long", v: result]
            }
        }
    }
    return [t: "error", v: "Invalid device"]
}

/******************************************************************************/
/*** previousAge returns the number of milliseconds an attribute had the 	***/
/*** previous value															***/
/*** Usage: previousAge([device:attribute])									***/
/******************************************************************************/
private func_previousage(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting previousAge([device:attribute])"];
    }
    def param = evaluateExpression(rtData, params[0], 'device')
    if ((param.t == 'device') && (param.a) && param.v.size()) {
		def device = getDevice(rtData, param.v[0])
        if (device && (device.id != location.id)) {
        	def states = device.statesSince(param.a, new Date(now() - 604500000), [max: 5])
            if (states.size() > 1) {
            	def newValue = states[0].getValue()
                //some events get duplicated, so we really want to look for the last "different valued" state
                for(int i = 1; i < states.size(); i++) {
                	if (states[i].getValue() != newValue) {
            			def result = now() - states[i].getDate().getTime()
                		return [t: "long", v: result]
                    }
                }
            }
            //we're saying 7 days, though it may be wrong - but we have no data
             return [t: "long", v: 604800000]
        }
    }
    return [t: "error", v: "Invalid device"]
}

/******************************************************************************/
/*** previousValue returns the previous value of the attribute				***/
/*** Usage: previousValue([device:attribute])								***/
/******************************************************************************/
private func_previousvalue(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting previousValue([device:attribute])"];
    }
    def param = evaluateExpression(rtData, params[0], 'device')
    if ((param.t == 'device') && (param.a) && param.v.size()) {
    	def attribute = rtData.attributes[param.a]
        if (attribute) {
			def device = getDevice(rtData, param.v[0])
	        if (device && (device.id != location.id)) {
                def states = device.statesSince(param.a, new Date(now() - 604500000), [max: 5])
                if (states.size() > 1) {
                    def newValue = states[0].getValue()
                    //some events get duplicated, so we really want to look for the last "different valued" state
                    for(int i = 1; i < states.size(); i++) {
                        def result = states[i].getValue()
                        if (result != newValue) {
                            return [t: attribute.t, v: cast(rtData, result, attribute.t)]
                        }
                    }
                }
                //we're saying 7 days, though it may be wrong - but we have no data
                return [t: 'string', v: '']
            }
        }
    }
    return [t: "error", v: "Invalid device"]
}

/******************************************************************************/
/*** newer returns the number of devices whose attribute had the current    ***/
/*** value for less than the specified number of milliseconds			    ***/
/*** Usage: newer([device:attribute] [,.., [device:attribute]], threshold)	***/
/******************************************************************************/
private func_newer(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting newer([device:attribute] [,.., [device:attribute]], threshold)"];
    }
    def threshold = evaluateExpression(rtData, params[params.size() - 1], 'integer').v
    int result = 0
    for (def i = 0; i < params.size() - 1; i++) {
    	def age = func_age(rtData, [params[i]])
        if ((age.t != 'error') && (age.v < threshold)) result++
    }
    return [t: "integer", v: result]
}

/******************************************************************************/
/*** older returns the number of devices whose attribute had the current    ***/
/*** value for more than the specified number of milliseconds			    ***/
/*** Usage: older([device:attribute] [,.., [device:attribute]], threshold)	***/
/******************************************************************************/
private func_older(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting older([device:attribute] [,.., [device:attribute]], threshold)"];
    }
    def threshold = evaluateExpression(rtData, params[params.size() - 1], 'integer').v
    int result = 0
    for (def i = 0; i < params.size() - 1; i++) {
    	def age = func_age(rtData, [params[i]])
        if ((age.t != 'error') && (age.v >= threshold)) result++
    }
    return [t: "integer", v: result]
}

/******************************************************************************/
/*** startsWith returns true if a string starts with a substring			***/
/*** Usage: startsWith(string, substring)									***/
/******************************************************************************/
private func_startswith(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting startsWith(string, substring)"];
    }
    def string = evaluateExpression(rtData, params[0], 'string').v
    def substring = evaluateExpression(rtData, params[1], 'string').v
    return [t: "boolean", v: string.startsWith(substring)]
}

/******************************************************************************/
/*** endsWith returns true if a string ends with a substring				***/
/*** Usage: endsWith(string, substring)										***/
/******************************************************************************/
private func_endswith(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting endsWith(string, substring)"];
    }
    def string = evaluateExpression(rtData, params[0], 'string').v
    def substring = evaluateExpression(rtData, params[1], 'string').v
    return [t: "boolean", v: string.endsWith(substring)]
}

/******************************************************************************/
/*** contains returns true if a string contains a substring					***/
/*** Usage: contains(string, substring)										***/
/******************************************************************************/
private func_contains(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2) || ((params[0].t != 'device') && (params.size() != 2))) {
    	return [t: "error", v: "Invalid parameters. Expecting contains(string, substring)"];
    }
	if ((params[0].t == 'device') && (params.size() > 2)) {
        def item = evaluateExpression(rtData, params[params.size() - 1], 'string').v
        for (int idx = 0; idx < params.size() - 1; idx++) {
        	def it = evaluateExpression(rtData, params[idx], 'string')
        	if (it.v == item) {
            	return [t: "boolean", v: true]
            }
        }
        return [t: "boolean", v: false]
    } else {
    	def string = evaluateExpression(rtData, params[0], 'string').v
    	def substring = evaluateExpression(rtData, params[1], 'string').v
    	return [t: "boolean", v: string.contains(substring)]
	}
}


/******************************************************************************/
/*** matches returns true if a string matches a pattern						***/
/*** Usage: matches(string, pattern)										***/
/******************************************************************************/
private func_matches(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting matches(string, pattern)"];
    }
    def string = evaluateExpression(rtData, params[0], 'string').v
    def pattern = evaluateExpression(rtData, params[1], 'string').v
    if ((pattern.size() > 2) && pattern.startsWith('/') && pattern.endsWith('/')) {
        pattern = ~pattern.substring(1, pattern.size() - 1)
        return [t: "boolean", v: !!(string =~ pattern)]
    }
    return [t: "boolean", v: string.contains(pattern)]
}

/******************************************************************************/
/*** eq returns true if two values are equal								***/
/*** Usage: eq(value1, value2)												***/
/******************************************************************************/
private func_eq(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting eq(value1, value2)"];
    }
    def t = params[0].t == 'device' ? params[1].t : params[0].t
    def value1 = evaluateExpression(rtData, params[0], t)
    def value2 = evaluateExpression(rtData, params[1], t)
    return [t: "boolean", v: value1.v == value2.v]
}

/******************************************************************************/
/*** lt returns true if value1 < value2										***/
/*** Usage: lt(value1, value2)												***/
/******************************************************************************/
private func_lt(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting lt(value1, value2)"];
    }
    def value1 = evaluateExpression(rtData, params[0])
    def value2 = evaluateExpression(rtData, params[1], value1.t)
    return [t: "boolean", v: value1.v < value2.v]
}

/******************************************************************************/
/*** le returns true if value1 <= value2									***/
/*** Usage: le(value1, value2)												***/
/******************************************************************************/
private func_le(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting le(value1, value2)"];
    }
    def value1 = evaluateExpression(rtData, params[0])
    def value2 = evaluateExpression(rtData, params[1], value1.t)
    return [t: "boolean", v: value1.v <= value2.v]
}

/******************************************************************************/
/*** gt returns true if value1 > value2									***/
/*** Usage: gt(value1, value2)												***/
/******************************************************************************/
private func_gt(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting gt(value1, value2)"];
    }
    def value1 = evaluateExpression(rtData, params[0])
    def value2 = evaluateExpression(rtData, params[1], value1.t)
    return [t: "boolean", v: value1.v > value2.v]
}

/******************************************************************************/
/*** ge returns true if value1 >= value2									***/
/*** Usage: ge(value1, value2)												***/
/******************************************************************************/
private func_ge(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting ge(value1, value2)"];
    }
    Map value1 = evaluateExpression(rtData, params[0])
    Map value2 = evaluateExpression(rtData, params[1], value1.t)
    return [t: "boolean", v: value1.v >= value2.v]
}

/******************************************************************************/
/*** not returns the negative boolean value 								***/
/*** Usage: not(value)														***/
/******************************************************************************/
private func_not(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting not(value)"];
    }
    boolean value = evaluateExpression(rtData, params[0], 'boolean').v
    return [t: "boolean", v: !value]
}

/******************************************************************************/
/*** if evaluates a boolean and returns value1 if true, or value2 otherwise ***/
/*** Usage: if(condition, valueIfTrue, valueIfFalse)						***/
/******************************************************************************/
private func_if(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 3)) {
    	return [t: "error", v: "Invalid parameters. Expecting if(condition, valueIfTrue, valueIfFalse)"];
    }
    boolean value = evaluateExpression(rtData, params[0], 'boolean').v
    return value ? evaluateExpression(rtData, params[1]) : evaluateExpression(rtData, params[2])
}

/******************************************************************************/
/*** isEmpty returns true if the value is empty								***/
/*** Usage: isEmpty(value)													***/
/******************************************************************************/
private func_isempty(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting isEmpty(value)"];
    }
    def value = evaluateExpression(rtData, params[0])
    boolean result = (value.v instanceof List ? (value.v == [null]) || (value.v == []) || (value.v == ['null']) : false) || (value.v == null) || (value.t == 'error') || (value.v == 'null') || (cast(rtData, value.v, 'string') == '') || ("$value.v" == '')
    return [t: "boolean", v: result]
}

/******************************************************************************/
/*** datetime returns the value as a datetime type							***/
/*** Usage: datetime([value])												***/
/******************************************************************************/
private func_datetime(rtData, params) {
	if (!(params instanceof List) || (params.size() > 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting datetime([value])"];
    }
    long value = params.size() > 0 ? evaluateExpression(rtData, params[0], 'datetime').v : now()
    return [t: "datetime", v: value]
}

/******************************************************************************/
/*** date returns the value as a date type									***/
/*** Usage: date([value])													***/
/******************************************************************************/
private func_date(rtData, params) {
	if (!(params instanceof List) || (params.size() > 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting date([value])"];
    }
    long value = params.size() > 0 ? evaluateExpression(rtData, params[0], 'date').v : cast(rtData, now(), 'date', 'datetime')
    return [t: "date", v: value]
}

/******************************************************************************/
/*** time returns the value as a time type									***/
/*** Usage: time([value])													***/
/******************************************************************************/
private func_time(rtData, params) {
	if (!(params instanceof List) || (params.size() > 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting time([value])"];
    }
    long value = params.size() > 0 ? evaluateExpression(rtData, params[0], 'time').v : cast(rtData, now(), 'time', 'datetime')
    return [t: "time", v: value]
}

/******************************************************************************/
/*** addSeconds returns the value as a time type							***/
/*** Usage: addSeconds([dateTime, ]seconds)									***/
/******************************************************************************/
private func_addseconds(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting addSeconds([dateTime, ]seconds)"];
    }
    long value = params.size() == 2 ? evaluateExpression(rtData, params[0], 'datetime').v : now()
    long delta = evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 1000
    return [t: "datetime", v: value + delta]
}

/******************************************************************************/
/*** addMinutes returns the value as a time type							***/
/*** Usage: addMinutes([dateTime, ]minutes)									***/
/******************************************************************************/
private func_addminutes(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting addMinutes([dateTime, ]minutes)"];
    }
    long value = params.size() == 2 ? evaluateExpression(rtData, params[0], 'datetime').v : now()
    long delta = evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 60000
    return [t: "datetime", v: value + delta]
}

/******************************************************************************/
/*** addHours returns the value as a time type								***/
/*** Usage: addHours([dateTime, ]hours)										***/
/******************************************************************************/
private func_addhours(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting addHours([dateTime, ]hours)"];
    }
    long value = params.size() == 2 ? evaluateExpression(rtData, params[0], 'datetime').v : now()
    long delta = evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 3600000
    return [t: "datetime", v: value + delta]
}

/******************************************************************************/
/*** addDays returns the value as a time type								***/
/*** Usage: addDays([dateTime, ]days)										***/
/******************************************************************************/
private func_adddays(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting addDays([dateTime, ]days)"];
    }
    long value = params.size() == 2 ? evaluateExpression(rtData, params[0], 'datetime').v : now()
    long delta = evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 86400000
    return [t: "datetime", v: value + delta]
}

/******************************************************************************/
/*** addWeeks returns the value as a time type								***/
/*** Usage: addWeeks([dateTime, ]weeks)										***/
/******************************************************************************/
private func_addweeks(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting addWeeks([dateTime, ]weeks)"];
    }
    long value = params.size() == 2 ? evaluateExpression(rtData, params[0], 'datetime').v : now()
    long delta = evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 604800000
    return [t: "datetime", v: value + delta]
}


/******************************************************************************/
/*** weekDayName returns the name of the week day							***/
/*** Usage: weekDayName(dateTimeOrWeekDayIndex)								***/
/******************************************************************************/
private func_weekdayname(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting weekDayName(dateTimeOrWeekDayIndex)"];
    }
    long value = evaluateExpression(rtData, params[0], 'long').v
    int index = ((value >= 86400000) ? utcToLocalDate(value).day : value) % 7
    return [t: "string", v: weekDays()[index]]
}

/******************************************************************************/
/*** monthName returns the name of the month								***/
/*** Usage: monthName(dateTimeOrMonthNumber)								***/
/******************************************************************************/
private func_monthname(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting monthName(dateTimeOrMonthNumber)"];
    }
    long value = evaluateExpression(rtData, params[0], 'long').v
   	int index = ((value >= 86400000) ? utcToLocalDate(value).month : value - 1) % 12 + 1
    return [t: "string", v: yearMonths()[index]]
}

/******************************************************************************/
/*** arrayItem returns the nth item in the parameter list					***/
/*** Usage: arrayItem(index, item0[, item1[, .., itemN]])					***/
/******************************************************************************/
private func_arrayitem(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting arrayItem(index, item0[, item1[, .., itemN]])"];
    }
    int index = evaluateExpression(rtData, params[0], 'integer').v
    if ((params.size() == 2) && ((params[1].t == 'string') || (params[1].t == 'dynamic'))) {
    	def list = evaluateExpression(rtData, params[1], 'string').v.split(',').toList()
        if ((index < 0) || (index >= list.size())) {
            return [t: "error", v: "Array item index is outside of bounds."]
        }
        return [t: 'string', v: list[index]]
    }
    int sz = params.size() - 1
    if ((index < 0) || (index >= sz)) {
        return [t: "error", v: "Array item index is outside of bounds."]
    }
    return params[index + 1]
}


/******************************************************************************/
/*** isBetween returns true if value >= startValue and value <= endValue	***/
/*** Usage: isBetween(value, startValue, endValue)							***/
/******************************************************************************/
private func_isbetween(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 3)) {
    	return [t: "error", v: "Invalid parameters. Expecting isBetween(value, startValue, endValue)"];
    }
    def value = evaluateExpression(rtData, params[0])
    def startValue = evaluateExpression(rtData, params[1], value.t)
    def endValue = evaluateExpression(rtData, params[2], value.t)
    return [t: "boolean", v: (value.v >= startValue.v) && (value.v <= endValue.v)]
}

/******************************************************************************/
/*** formatDuration returns a duration in a readable format					***/
/*** Usage: formatDuration(value[, friendly = false[, granularity = 's'[, showAdverbs = false]]])	***/
/******************************************************************************/
private func_formatduration(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 4)) {
    	return [t: "error", v: "Invalid parameters. Expecting formatDuration(value[, friendly = false[, granularity = 's'[, showAdverbs = false]]])"];
    }
    long value = evaluateExpression(rtData, params[0], 'long').v
	boolean friendly = params.size() > 1 ? !!evaluateExpression(rtData, params[1], 'boolean').v : false
	def granularity = params.size() > 2 ? evaluateExpression(rtData, params[2], 'string').v : 's'
	boolean showAdverbs = params.size() > 3 ? !!evaluateExpression(rtData, params[3], 'boolean').v : false

	int sign = (value >= 0) ? 1 : -1
    if (sign < 0) value = -value
	int ms = value % 1000
    value = Math.floor((value - ms) / 1000)
	int s = value % 60
    value = Math.floor((value - s) / 60)
	int m = value % 60
    value = Math.floor((value - m) / 60)
	int h = value % 24
    value = Math.floor((value - h) / 24)
	int d = value

    def parts = 0
    def partName = ''
    switch (granularity) {
    	case 'd': parts = 1; partName = 'day'; break;
    	case 'h': parts = 2; partName = 'hour'; break;
    	case 'm': parts = 3; partName = 'minute'; break;
    	case 'ms': parts = 5; partName = 'millisecond'; break;
    	default: parts = 4; partName = 'second'; break;
    }
    parts = friendly ? parts : (parts < 3 ? 3 : parts)
    def result = ''
    if (friendly) {
    	List p = []
        if (d) p.push("$d day" + (d > 1 ? 's' : ''))
        if ((parts > 1) && h) p.push("$h hour" + (h > 1 ? 's' : ''))
        if ((parts > 2) && m) p.push("$m minute" + (m > 1 ? 's' : ''))
        if ((parts > 3) && s) p.push("$s second" + (s > 1 ? 's' : ''))
        if ((parts > 4) && ms) p.push("$ms millisecond" + (ms > 1 ? 's' : ''))
        switch (p.size()) {
        	case 0:
            	result = showAdverbs ? 'now' : '0 ' + partName + 's'
                break
            case 1:
            	result = p[0]
                break
			default:
            	result = '';
                int sz = p.size()
                for (int i=0; i < sz; i++) {
                	result += (i ? (sz > 2 ? ', ' : ' ') : '') + (i == sz - 1 ? 'and ' : '') + p[i]
                }
                result = (showAdverbs && (sign > 0) ? 'in ' : '') + result + (showAdverbs && (sign < 0) ? ' ago' : '')
            	break
		}
    } else {
    	result = (sign < 0 ? '-' : '') + (d > 0 ? sprintf("%dd ", d) : '') + sprintf("%02d:%02d", h, m) + (parts > 3 ? sprintf(":%02d", s) : '') + (parts > 4 ? sprintf(".%03d", ms) : '')
    }
    return [t: "string", v: result]
}

/******************************************************************************/
/*** formatDateTime returns a datetime in a readable format					***/
/*** Usage: formatDateTime(value[, format])									***/
/******************************************************************************/
private func_formatdatetime(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting formatDateTime(value[, format])"];
    }
    long value = evaluateExpression(rtData, params[0], 'datetime').v
	def format = params.size() > 1 ? evaluateExpression(rtData, params[1], 'string').v : null
    return [t: 'string', v: (format ? formatLocalTime(value, format) : formatLocalTime(value))]
}

/******************************************************************************/
/*** random returns a random value											***/
/*** Usage: random([range | value1, value2[, ..,valueN]])	***/
/******************************************************************************/
private func_random(rtData, params) {
    int sz = params && (params instanceof List) ? params.size() : 0
    switch (sz) {
		case 0:
        	return [t: 'decimal', v: Math.random()]
		case 1:
        	def range = evaluateExpression(rtData, params[0], 'decimal').v
        	return [t: 'integer', v: (int)Math.round(range * Math.random())]
		case 2:
        	if (((params[0].t == 'integer') || (params[0].t == 'decimal')) && ((params[1].t == 'integer') || (params[1].t == 'decimal'))) {
        		def min = evaluateExpression(rtData, params[0], 'decimal').v
    	    	def max = evaluateExpression(rtData, params[1], 'decimal').v
                if (min > max) {
                	def swap = min
                    min = max
                    max = swap
                }
	        	return [t: 'integer', v: (int)Math.round(min + (max - min) * Math.random())]
            }
    }
    int choice = (int)Math.round((sz - 1) * Math.random())
    if (choice >= sz) choice = sz - 1
    return params[choice]
}


/******************************************************************************/
/*** random returns a random value											***/
/*** Usage: distance((device | latitude, longitude), (device | latitude, longitude)[, unit])	***/
/******************************************************************************/
private func_distance(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 2) || (params.size() > 4)) {
    	return [t: "error", v: "Invalid parameters. Expecting distance((device | latitude, longitude), (device | latitude, longitude)[, unit])"];
    }
    float lat1, lng1, lat2, lng2
    def unit
    def idx = 0
    def pidx = 0
    def errMsg = ''
    while (pidx < params.size()) {
  	 	if ((params[pidx].t != 'device') || ((params[pidx].t == 'device') && !!params[pidx].a)) {
    		//a decimal or device attribute is provided
    		switch (idx) {
            	case 0:
                	lat1 = evaluateExpression(rtData, params[pidx], 'decimal').v
                    break
            	case 1:
                	lng1 = evaluateExpression(rtData, params[pidx], 'decimal').v
                    break
            	case 2:
                	lat2 = evaluateExpression(rtData, params[pidx], 'decimal').v
                    break
            	case 3:
                	lng2 = evaluateExpression(rtData, params[pidx], 'decimal').v
                    break
                case 4:
                	unit = evaluateExpression(rtData, params[pidx], 'string').v
            }
            idx += 1
            pidx += 1
            continue
        } else {
            switch (idx) {
                case 0:
                case 2:
                	params[pidx].a = 'latitude'
                	float lat = evaluateExpression(rtData, params[pidx], 'decimal').v
                	params[pidx].a = 'longitude'
                	float lng = evaluateExpression(rtData, params[pidx], 'decimal').v
                	if (idx == 0) {
	                    lat1 = lat
	                    lng1 = lng
    	            } else {
        	            lat2 = lat
            	        lng2 = lng
          			}
                	idx += 2
                    pidx += 1
	                continue
                default:
                    errMsg = 'Invalid parameter order. Expecting parameter #${idx+1} to be a decimal, not a device.';
                    pidx = -1
    	            break;
            }
        }
        if (pidx == -1) break
    }
    if (errMsg) return [t: 'error', v: errMsg]
    if ((idx < 4) || (idx > 5)) return [t: 'error', v: 'Invalid parameter combination. Expecting either two devices, a device and two decimals, or four decimals, followed by an optional unit.']
    double earthRadius = 6371000; //meters
    double dLat = Math.toRadians(lat2-lat1);
    double dLng = Math.toRadians(lng2-lng1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
               Math.sin(dLng/2) * Math.sin(dLng/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    float dist = (float) (earthRadius * c);
    switch (unit ?: 'm') {
    	case 'km':
        case 'kilometer':
        case 'kilometers':
        	return [t: 'decimal', v: dist / 1000.0]
    	case 'mi':
        case 'mile':
        case 'miles':
        	return [t: 'decimal', v: dist / 1609.3440]
    	case 'ft':
        case 'foot':
        case 'feet':
        	return [t: 'decimal', v: dist / 0.3048]
    	case 'yd':
        case 'yard':
        case 'yards':
        	return [t: 'decimal', v: dist / 0.9144]
    }
	return [t: 'decimal', v: dist]
}

/******************************************************************************/
/*** json encodes data as a JSON string					***/
/*** Usage: json(value[, pretty])									***/
/******************************************************************************/
private func_json(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
    	return [t: "error", v: "Invalid parameters. Expecting json(value[, format])"];
    }
    def builder = new groovy.json.JsonBuilder([params[0].v])
    def op = params[1] ? 'toPrettyString' : 'toString'
    def json = builder."${op}"()
    return [t: 'string', v: json[1..-2].trim()]
}

/******************************************************************************/
/*** percent encodes data for use in a URL					***/
/*** Usage: urlencode(value)									***/
/******************************************************************************/
private func_urlencode(rtData, params) {
	if (!params || !(params instanceof List) || (params.size() != 1)) {
    	return [t: "error", v: "Invalid parameters. Expecting urlencode(value])"];
    }
    // URLEncoder converts spaces to + which is then indistinguishable from any 
    // actual + characters in the value. Match encodeURIComponent in ECMAScript
    // which encodes "a+b c" as "a+b%20c" rather than URLEncoder's "a+b+c"
    def value = (evaluateExpression(rtData, params[0], 'string').v ?: '').replaceAll('\\+', '__wc_plus__')
    return [t: 'string', v: URLEncoder.encode(value, 'UTF-8').replaceAll('\\+', '%20').replaceAll('__wc_plus__', '+')]
}
private func_encodeuricomponent(rtData, params) { return func_urlencode(rtData, params); }

/******************************************************************************/
/*** 																		***/
/*** COMMON PUBLISHED METHODS												***/
/*** 																		***/
/******************************************************************************/

def mem(showBytes = true) {
	def bytes = state.toString().length()
	return Math.round(100.00 * (bytes/ 100000.00)) + "%${showBytes ? " ($bytes bytes)" : ""}"
}
/******************************************************************************/
/***																		***/
/*** UTILITIES																***/
/***																		***/
/******************************************************************************/

def String md5(String md5) {
   try {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5")
        byte[] array = md.digest(md5.getBytes())
        def result = ""
        for (int i = 0; i < array.length; ++i) {
          result += Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3)
       }
        return result
    } catch (java.security.NoSuchAlgorithmException e) {
    }
    return null;
}

def String hashId(id) {
	//enabled hash caching for faster processing
	def result = state.hash ? state.hash[id] : null
    if (!result) {
		result = ":${md5("core." + id)}:"
        def hash = state.hash ?: [:]
        hash[id] = result
        state.hash = hash
    }
    return result
}

private getThreeAxisOrientation(value, getIndex = false) {
	if (value instanceof Map) {
		if ((value.x != null) && (value.y != null) && (value.z != null)) {
			def x = Math.abs(value.x)
			def y = Math.abs(value.y)
			def z = Math.abs(value.z)
			def side = (x > y ? (x > z ? 0 : 2) : (y > z ? 1 : 2))
			side = side + (((side == 0) && (value.x < 0)) || ((side == 1) && (value.y < 0)) || ((side == 2) && (value.z < 0)) ? 3 : 0)
			def orientations = ['rear', 'down', 'left', 'front', 'up', 'right']
			def result = getIndex ? side : orientations[side] + ' side up'
			return result
		}
	}
	return value
}

private long getTimeToday(long time) {
	long result = localToUtcTime(time + utcToLocalTime(getMidnightTime()))
    //we need to adjust for time overlapping during DST changes
    return result + time - (utcToLocalTime(result) % 86400000)
}

private cast(rtData, value, dataType, srcDataType = null) {
    //error "CASTING ($srcDataType) $value as $dataType", rtData
    //if (srcDataType == 'vector3') error "got x = $value.x", rtData
	if (dataType == 'dynamic') return value
	def trueStrings = ["1", "true", "on", "open", "locked", "active", "wet", "detected", "present", "occupied", "muted", "sleeping"]
	def falseStrings = ["0", "false", "off", "closed", "unlocked", "inactive", "dry", "clear", "not detected", "not present", "not occupied", "unmuted", "not sleeping", "null"]
	//get rid of GStrings
    if (value == null) {
    	value = '';
        srcDataType = 'string';
    }
	value = (value instanceof GString) ? "$value".toString() : value
    if (!srcDataType || (srcDataType == 'boolean') || (srcDataType == 'dynamic')) {
    	if (value instanceof List) { srcDataType = 'device' } else
		if (value instanceof Boolean) { srcDataType = 'boolean' } else
		if (value instanceof String) { srcDataType = 'string' } else
		if (value instanceof String) { srcDataType = 'string' } else
		if (value instanceof Integer) { srcDataType = 'integer' } else
		if (value instanceof BigInteger) { srcDataType = 'long' } else
		if (value instanceof Long) { srcDataType = 'long' } else
		if (value instanceof Double) { srcDataType = 'decimal' } else
		if (value instanceof Float) { srcDataType = 'decimal' } else
		if (value instanceof BigDecimal) { srcDataType = 'decimal' } else
        if ((value instanceof Map) && (value.x != null) && (value.y != null) && (value.z != null)) { srcDataType = 'vector3' } else {
            value = "$value".toString()
            srcDataType = 'string'
        }
	}
    //overrides
    switch (srcDataType) {
    	case 'bool': srcDataType = 'boolean'; break;
    	case 'number': srcDataType = 'decimal'; break;
    	case 'enum': srcDataType = 'string'; break;
    }
    switch (dataType) {
    	case 'bool': dataType = 'boolean'; break;
    	case 'number': dataType = 'decimal'; break;
    	case 'enum': dataType = 'string'; break;
    }
    //perform the conversion
	switch (dataType) {
		case "string":
		case "text":
        	switch (srcDataType) {
            	case 'boolean': return value ? "true" : "false";
            	case 'decimal':
                	//if (value instanceof Double) return sprintf('%f', value)
                    // strip trailing zeroes (e.g. 5.00 to 5 and 5.030 to 5.03)
                    return value.toString().replaceFirst(/(?:\.|(\.\d*?))0+$/, '$1')
            	case 'integer':
            	case 'long': break; if (value > 9999999999) { return formatLocalTime(value) }; break;
                case 'time': return formatLocalTime(value, 'h:mm:ss a z');
                case 'date': return formatLocalTime(value, 'EEE, MMM d yyyy');
                case 'datetime': return formatLocalTime(value);
                case 'device': return buildDeviceList(rtData, value);
            }
			return "$value".toString()
		case "integer":
			switch (srcDataType) {
            	case 'string':
                    value = value.replaceAll(/[^-\d.-E]/, '')
                    if (value.isInteger())
                        return (int) value.toInteger()
                    if (value.isFloat())
                        return (int) Math.floor(value.toDouble())
                    if (value in trueStrings)
                        return (int) 1
                    break
				case 'boolean': return (int) (value ? 1 : 0);
            }
			def result = (int) 0
			try {
				result = (int) value
			} catch(all) {
				result = (int) 0
			}
			return result ? result : (int) 0
		case "long":
			switch (srcDataType) {
            	case 'string':
                    value = value.replaceAll(/[^-\d.-E]/, '')
                    if (value.isLong())
                        return (long) value.toLong()
                    if (value.isInteger())
                        return (long) value.toInteger()
                    if (value.isFloat())
                        return (long) Math.floor(value.toDouble())
                    if (value in trueStrings)
                        return (long) 1
                    break
				case 'boolean': return (long) (value ? 1 : 0);
            }
			def result = (long) 0
			try {
				result = (long) value
			} catch(all) {
				result = (long) 0
			}
			return result ? result : (long) 0
		case "decimal":
			switch (srcDataType) {
            	case 'string':
                    value = value.replaceAll(/[^-\d.-E]/, '')
                    if (value.isDouble())
                        return (double) value.toDouble()
                    if (value.isFloat())
                        return (double) value.toDouble()
                    if (value.isLong())
                        return (double) value.toLong()
                    if (value.isInteger())
                        return (double) value.toInteger()
                    if (value in trueStrings)
                        return (double) 1
					break
				case 'boolean': return (double) (value ? 1 : 0);
            }
			def result = (double) 0
			try {
				result = (double) value
			} catch(all) {
			}
			return result ? result : (double) 0
		case "boolean":
			switch (srcDataType) {
            	case 'integer':
            	case 'decimal':
            	case 'boolean':
					return !!value;
            	case 'device':
					return (value instanceof List) && value.size();
			}
            if (value) {
            	if ("$value".toLowerCase().trim() in trueStrings) return true
	            if ("$value".toLowerCase().trim() in falseStrings) return false
            }
			return !!value
		case "time":
        	if ("$value".isNumber() && (value < 86400000)) return value
        	def n = localTime()
			return utcToLocalTime((srcDataType == 'string') ? localToUtcTime(value) : cast(rtData, value, "long")) % 86400000
		case "date":
        	if ((srcDataType == 'time') && (value < 86400000)) value = getTimeToday(value)
			def d = utcToLocalTime((srcDataType == 'string') ? localToUtcTime(value) : cast(rtData, value, "long"))
            return localToUtcTime(d - (d % 86400000))
		case "datetime":
        	if ((srcDataType == 'time') && (value < 86400000)) value = getTimeToday(value) //localToUtcTime(value + utcToLocalTime(getMidnightTime()))
			return ((srcDataType == 'string') ? localToUtcTime(value) : cast(rtData, value, "long"))
		case "vector3":
			return (value instanceof Map) && (value.x != null) && (value.y != null) && (value.z != null) ? value : [x:0, y:0, z:0]
		case "orientation":
			return getThreeAxisOrientation(value)
        case 'ms': return (long) cast(rtData, value, 'long')
        case 's': return (long) cast(rtData, value, 'long') * 1000
        case 'm': return (long) cast(rtData, value, 'long') * 60000
        case 'h': return (long) cast(rtData, value, 'long') * 3600000
        case 'd': return (long) cast(rtData, value, 'long') * 86400000
        case 'w': return (long) cast(rtData, value, 'long') * 604800000
        case 'n': return (long) cast(rtData, value, 'long') * 2592000000
        case 'y': return (long) cast(rtData, value, 'long') * 31536000000
        case 'device':
        	//device type is an array of device Ids
        	if (value instanceof List) {
            	def x = value.size()
            	value.removeAll{ !it }
                return value
            }
            def v = cast(rtData, value, 'string')
            if (v) return [v]
            return []
	}
	//anything else...
	return value
}

private utcToLocalDate(dateOrTimeOrString = null) {
	if (dateOrTimeOrString instanceof String) {
		//get UTC time
        try {
			//dateOrTimeOrString = timeToday(dateOrTimeOrString, location.timeZone).getTime()
			dateOrTimeOrString = localToUtcTime(dateOrTimeOrString)
        } catch (all) {
        	error "Error converting $dateOrTimeOrString to Date: ", null, null, all
        }
	}
	if (dateOrTimeOrString instanceof Date) {
		//get unix time
		dateOrTimeOrString = dateOrTimeOrString.getTime()
	}
	if (!dateOrTimeOrString) {
		dateOrTimeOrString = now()
	}
	if (dateOrTimeOrString instanceof Long) {
		return new Date(dateOrTimeOrString + (location.timeZone ? location.timeZone.getOffset(dateOrTimeOrString) : 0))
	}
	return null
}
private localDate() { return utcToLocalDate() }

private utcToLocalTime(dateOrTimeOrString = null) {
	if (dateOrTimeOrString instanceof String) {
		//get UTC time
//		dateOrTimeOrString = timeToday(dateOrTimeOrString, location.timeZone).getTime()
		dateOrTimeOrString = localToUtcTime(dateOrTimeOrString)
	}
	if (dateOrTimeOrString instanceof Date) {
		//get unix time
		dateOrTimeOrString = dateOrTimeOrString.getTime()
	}
	if (!dateOrTimeOrString) {
		dateOrTimeOrString = now()
	}
	if (dateOrTimeOrString instanceof Long) {
		return dateOrTimeOrString + (location.timeZone ? location.timeZone.getOffset(dateOrTimeOrString) : 0)
	}
	return null
}
private localTime() { return utcToLocalTime() }

private localToUtcDate(dateOrTime) {
	if (dateOrTime instanceof Date) {
		//get unix time
		dateOrTime = dateOrTime.getTime()
	}
	if (dateOrTime instanceof Long) {
		return new Date(dateOrTime - (location.timeZone ? location.timeZone.getOffset(dateOrTime) : 0))
	}
	return null
}

private localToUtcTime(dateOrTimeOrString) {
	if (dateOrTimeOrString instanceof Date) {
		//get unix time
		dateOrTimeOrString = dateOrTimeOrString.getTime()
	}
	if ("$dateOrTimeOrString".isNumber()) {
    	if (dateOrTimeOrString < 86400000) dateOrTimeOrString += getMidnightTime()
		return dateOrTimeOrString - (location.timeZone ? location.timeZone.getOffset(dateOrTimeOrString) : 0)
	}
	if (dateOrTimeOrString instanceof String) {
		//get unix time
        try {
            if (!(dateOrTimeOrString =~ /(\s[A-Z]{3}((\+|\-)[0-9]{2}\:[0-9]{2}|\s[0-9]{4})?$)/)) {
                def newDate = (new Date()).parse(dateOrTimeOrString + ' ' + formatLocalTime(now(), 'Z'))
                return newDate + (location.timeZone.getOffset(now()) - location.timeZone.getOffset(newDate))
            }
            return (new Date()).parse(dateOrTimeOrString)
		} catch (all) {
        	try {
	        	return (new Date(dateOrTimeOrString)).getTime()
			} catch(all2) {
                try {
                    def tz = location.timeZone
                    if (dateOrTimeOrString =~ /\s[A-Z]{3}$/) {
                        try {
                            tz = TimeZone.getTimeZone(dateOrTimeOrString[-3..-1])
                            dateOrTimeOrString = dateOrTimeOrString.take(dateOrTimeOrString.size() - 3).trim()
                        } catch (all4) {
                        }
                    }
                    long time = timeToday(dateOrTimeOrString, tz).getTime()
                    //adjust for PM - timeToday has no clue....
                    dateOrTimeOrString = dateOrTimeOrString.trim().toLowerCase()
                    def twelve = dateOrTimeOrString.startsWith('12')
                    if (twelve && dateOrTimeOrString.endsWith('am')) time -= 43200000
                    if (!twelve && dateOrTimeOrString.endsWith('pm')) time += 43200000
                    return time
                } catch (all3) {
                    return (new Date()).getTime()
                }
            }
        }
	}
	return null
}

private formatLocalTime(time, format = "EEE, MMM d yyyy @ h:mm:ss a z") {
	if ("$time".isNumber()) {
    	if (time < 86400000) time += getMidnightTime()
		time = new Date(time)
	}
	if (time instanceof String) {
		//get UTC time
		//time = timeToday(time, location.timeZone)
		time = new Date(localToUtcTime(time))
	}
    if (!(time instanceof Date)) {
		return null
	}
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}


private Map hexToColor(hex){
    hex = hex ? "$hex".toString() : '000000'
    if (hex.startsWith('#')) hex = hex.substring(1)
    if (hex.size() != 6) hex = '000000'
    double r = Integer.parseInt(hex.substring(0, 2), 16) / 255
    double g = Integer.parseInt(hex.substring(2, 4), 16) / 255
    double b = Integer.parseInt(hex.substring(4, 6), 16) / 255
    double min = Math.min(Math.min(r, g), b);
    double max = Math.max(Math.max(r, g), b)
    double h = (max + min) / 2.0;
    double s = h
    double l = s
    if(max == min){
        h = s = 0; // achromatic
    }else{
        double d = max - min;
        s = (l > 0.5) ? d / (2 - max - min) : d / (max + min);
        switch(max){
            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
            case g: h = (b - r) / d + 2; break;
            case b: h = (r - g) / d + 4; break;
        }
        h = h / 6;
    }
    return [
        hue: (int) Math.round(100 * h),
        saturation: (int) Math.round(100 * s),
        level: (int) Math.round(100 * l),
        hex: '#' + hex
    ];
};

private float _hue2rgb(p, q, t){
    if(t < 0) t += 1
    if(t > 1) t -= 1
    if(t < 1/6) return p + (q - p) * 6 * t
    if(t < 1/2) return q
    if(t < 2/3) return p + (q - p) * (2/3 - t) * 6
    return p
}

private String hslToHex(hue, saturation, level) {
	float h = hue / 360.0
	float s = saturation / 100.0
	float l = level / 200.0
    if (h < 0) h = 0
    if (h > 1) h = 1
    if (s < 0) s = 0
    if (s > 1) s = 1
    if (l < 0) l = 0
    if (l > 0.5) l = 0.5
	float r, g, b
    if(s == 0){
        r = g = b = l; // achromatic
    } else {

        float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        float p = 2 * l - q;
        r = _hue2rgb(p, q, h + 1/3);
        g = _hue2rgb(p, q, h);
        b = _hue2rgb(p, q, h - 1/3);
    }
    return sprintf('#%02X%02X%02X', Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
}

private List hexToHsl(hex){
	def rgb = hexToRgbArray(hex)
    float r = rgb[0] / 255
    float g = rgb[1] / 255
    float b = rgb[2] / 255
    float max = Math.max(Math.max(r, g), b)
    float min = Math.min(Math.min(r, g), b)
    float h, s, l = (max + min) / 2

    if(max == min){
        h = s = 0 // achromatic
    }else{
        float d = max - min
        s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
        switch(max){
            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
            case g: h = (b - r) / d + 2; break;
            case b: h = (r - g) / d + 4; break;
        }
        h /= 6
    }
    return [Math.round(h * 360), Math.round(s * 100), Math.round(l * 100)]
}

private List hexToRgbArray(hex) {
	hex = hex?.replace('#', '');
    if (hex && (hex.size() == 6)) {
    	try {
			List data = [0, 0, 0];
    		for(int i=0;i<3;i++) {
        		data[i] = Integer.decode('0x' + hex.substring(i*2, i*2 + 2));
    		}
    		return data;
    	} catch (e) {}
	}
    return [0, 0, 0];
}

/******************************************************************************/
/*** DEBUG FUNCTIONS														***/
/******************************************************************************/
private log(message, rtData = null, shift = null, err = null, cmd = null, force = false) {
    if (cmd == "timer") {
    	return [m: message, t: now(), s: shift, e: err]
    }
    if (message instanceof Map) {
    	shift = message.s
        err = message.e
        message = message.m + " (${now() - message.t}ms)"
    }
	//if (!force && rtData && rtData.logging && !rtData.logging[cmd] && (cmd != "error")) {
	//	return
	//}
	cmd = cmd ? cmd : "debug"
	//mode is
	// 0 - initialize level, level set to 1
	// 1 - start of routine, level up
	// -1 - end of routine, level down
	// anything else - nothing happens
	def maxLevel = 4
	def level = state.debugLevel ? state.debugLevel : 0
	def levelDelta = 0
	def prefix = "║"
    def prefix2 = "║"
	def pad = "" //"░"
	switch (shift) {
		case 0:
			level = 0
		case 1:
			level += 1
			prefix = "╚"
			prefix2 = "╔"
			pad = "═"
			break
		case -1:
        	level -= 1
			//levelDelta = -(level > 0 ? 1 : 0)
			pad = "═"
			prefix = "╔"
			prefix2 = "╚"
		break
	}

	if (level > 0) {
		prefix = prefix.padLeft(level + (shift == -1 ? 1 : 0), "║")
		prefix2 = prefix2.padLeft(level + (shift == -1 ? 1 : 0), "║")
    }

	//level += levelDelta
	state.debugLevel = level

	if (rtData && (rtData instanceof Map) && (rtData.logs instanceof List)) {
    	message = "$message".toString().replaceAll(/(\r\n|\r|\n|\\r\\n|\\r|\\n)+/, "\r");
        if (message.size() > 1024) {
        	message = message[0..1023] + '...[TRUNCATED]'
        }
    	List msgs = !err ? message.tokenize("\r") : [message]
        for(msg in msgs) {
    		rtData.logs.push([o: now() - rtData.timestamp, p: prefix2, m: msg + (!!err ? " $err" : ""), c: cmd])
        }
    }
  	if (hubUID) {
    	log."$cmd" "$prefix $message"
    } else {
		log."$cmd" "$prefix $message", err
    }
}
private info(message, rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'info' }
private trace(message, rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'trace' }
private debug(message, rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'debug' }
private warn(message, rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'warn' }
private error(message, rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'error' }
private timer(message, rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'timer' }

private tracePoint(rtData, objectId, duration, value) {
	if (objectId && rtData && rtData.trace) {
    	rtData.trace.points[objectId] = [o: now() - rtData.trace.t - duration, d: duration, v: value]
    } else {
    	error "Invalid object ID $objectID for trace point...", rtData
    }
}


private static Map weekDays() {
	return [
    	0: "Sunday",
        1: "Monday",
        2: "Tuesday",
        3: "Wednesday",
        4: "Thursday",
        5: "Friday",
        6: "Saturday"
    ]
}

private static Map yearMonths() {
	return [
    	1: "January",
        2: "February",
        3: "March",
        4: "April",
        5: "May",
        6: "June",
        7: "July",
        8: "August",
        9: "September",
        10: "October",
        11: "November",
        12: "December"
    ]
}

private initSunriseAndSunset(rtData) {
	def t = now()
    def rightNow = localTime()
    if (!rtData.sunTimes) {
    	def sunTimes = app.getSunriseAndSunset()
        if (!sunTimes.sunrise) {
            warn "Actual sunrise and sunset times are unavailable; please reset the location for your hub", rtData
            sunTimes.sunrise = new Date(getMidnightTime() + 7 * 3600000)
            sunTimes.sunset = new Date(getMidnightTime() + 19 * 3600000)
        }
        rtData.sunTimes = [
    		sunrise: sunTimes.sunrise.time,
    		sunset: sunTimes.sunset.time,
        	updated: now()
    	]
    }
    rtData.sunrise = localToUtcTime(rightNow - rightNow.mod(86400000) + utcToLocalTime(rtData.sunTimes.sunrise).mod(86400000))
	rtData.sunset = localToUtcTime(rightNow - rightNow.mod(86400000) + utcToLocalTime(rtData.sunTimes.sunset).mod(86400000))
}

private getSunriseTime(rtData) {
	if (!rtData.sunrise) initSunriseAndSunset(rtData)
	return rtData.sunrise
}

private getSunsetTime(rtData) {
	if (!rtData.sunset) initSunriseAndSunset(rtData)
	return rtData.sunset
}

private getNextSunriseTime(rtData) {
	if (!(rtData.sunrise instanceof Date)) initSunriseAndSunset(rtData)
	return rtData.sunrise + (rtData.sunrise < now() ? 86400000 : 0)
}

private getNextSunsetTime(rtData) {
	if (!(rtData.sunset instanceof Date)) initSunriseAndSunset(rtData)
	return rtData.sunset + (rtData.sunset < now() ? 86400000 : 0)
}

private getMidnightTime(rtData) {
	def rightNow = localTime()
    return localToUtcTime(rightNow - rightNow.mod(86400000))
}

private getNextMidnightTime(rtData) {
	def rightNow = utcToLocalTime(localToUtcTime(localTime() + 86400000))
    return localToUtcTime(rightNow - rightNow.mod(86400000))
}

private getNoonTime(rtData) {
	def rightNow = localTime()
    return localToUtcTime(rightNow - rightNow.mod(86400000) + 43200000)
}

private getNextNoonTime(rtData) {
	def rightNow = localTime()
	rightNow = utcToLocalTime(localToUtcTime(rightNow + (rightNow.mod(86400000) >= 43200000 ? 86400000 : 0)))
    return localToUtcTime(rightNow - rightNow.mod(86400000) + 43200000)
}

private Map getLocalVariables(rtData, vars) {
    rtData.localVars = [:]
    def values = atomicState.vars
	for (var in vars) {
    	def variable = [t: var.t, v: var.v ?: (var.t.endsWith(']') ? (values[var.n] instanceof Map ? values[var.n] : {}) : cast(rtData, values[var.n], var.t)), f: !!var.v] //f means fixed value - we won't save this to the state
        if (rtData && var.v && (var.a == 's') && !var.t.endsWith(']')) {
        	variable.v = evaluateExpression(rtData, evaluateOperand(rtData, null, var.v), var.t).v
        }
        rtData.localVars[var.n] = variable
    }
    return rtData.localVars
}

def Map getSystemVariablesAndValues(rtData) {
	rtData = rtData ?: [:]
	Map result = getSystemVariables()
    for(variable in result) {
    	if (variable.value.d) variable.value.v = getSystemVariableValue(rtData, variable.key)
    }
    return result
}

private static Map getSystemVariables() {
	return [
        '$args': [t: "dynamic", d: true],
        '$json': [t: "dynamic", d: true],
        '$places': [t: "dynamic", d: true],
        '$response': [t: "dynamic", d: true],
        '$weather': [t: "dynamic", d: true],
        '$twcweather': [t: "dynamic", d: true],
        '$nfl': [t: "dynamic", d: true],
        '$incidents': [t: "dynamic", d: true],
        '$shmTripped': [t: "boolean", d: true],
		"\$currentEventAttribute": [t: "string", v: null],
		"\$currentEventDate": [t: "datetime", v: null],
		"\$currentEventDelay": [t: "integer", v: null],
		"\$currentEventDevice": [t: "device", v: null],
		"\$currentEventDeviceIndex": [t: "integer", v: null],
		"\$currentEventDevicePhysical": [t: "boolean", v: null],
		"\$currentEventReceived": [t: "datetime", v: null],
		"\$currentEventValue": [t: "dynamic", v: null],
		"\$currentEventUnit": [t: "string", v: null],
		"\$currentState": [t: "string", v: null],
		"\$currentStateDuration": [t: "string", v: null],
		"\$currentStateSince": [t: "datetime", v: null],
		"\$nextScheduledTime": [t: "datetime", v: null],
		"\$name": [t: "string", d: true],
		"\$state": [t: "string", v: ''],
		"\$now": [t: "datetime", d: true],
        '$device': [t: 'device', v: null],
        '$devices': [t: 'device', v: null],
        '$location': [t: 'device', v: null],
		"\$utc": [t: "datetime", d: true],
		"\$localNow": [t: "datetime", d: true],
		"\$hour": [t: "integer", d: true],
		"\$hour24": [t: "integer", d: true],
		"\$minute": [t: "integer", d: true],
		"\$second": [t: "integer", d: true],
		"\$meridian": [t: "string", d: true],
		"\$meridianWithDots":  [t: "string", d: true],
		"\$day": [t: "integer", d: true],
		"\$dayOfWeek": [t: "integer", d: true],
		"\$dayOfWeekName": [t: "string", d: true],
		"\$month": [t: "integer", d: true],
		"\$monthName": [t: "string", d: true],
		"\$year": [t: "integer", d: true],
		"\$midnight": [t: "datetime", d: true],
		"\$noon": [t: "datetime", d: true],
		"\$sunrise": [t: "datetime", d: true],
		"\$sunset": [t: "datetime", d: true],
		"\$nextMidnight": [t: "datetime", d: true],
		"\$nextNoon": [t: "datetime", d: true],
		"\$nextSunrise": [t: "datetime", d: true],
		"\$nextSunset": [t: "datetime", d: true],
		"\$time": [t: "string", d: true],
		"\$time24": [t: "string", d: true],
		"\$index": [t: "decimal", v: null],
		"\$previousEventAttribute": [t: "string", v: null],
		"\$previousEventDate": [t: "datetime", v: null],
		"\$previousEventDelay": [t: "integer", v: null],
		"\$previousEventDevice": [t: "device", v: null],
		"\$previousEventDeviceIndex": [t: "integer", v: null],
		"\$previousEventDevicePhysical": [t: "boolean", v: null],
		"\$previousEventExecutionTime": [t: "integer", v: null],
		"\$previousEventReceived": [t: "datetime", v: null],
		"\$previousEventValue": [t: "dynamic", v: null],
		"\$previousEventUnit": [t: "string", v: null],
		"\$previousState": [t: "string", v: null],
		"\$previousStateDuration": [t: "string", v: null],
		"\$previousStateSince": [t: "datetime", v: null],
		"\$random": [t: "decimal", d: true],
		"\$randomColor": [t: "string", d: true],
		"\$randomColorName": [t: "string", d: true],
		"\$randomLevel": [t: "integer", d: true],
		"\$randomSaturation": [t: "integer", d: true],
		"\$randomHue": [t: "integer", d: true],
		"\$httpContentType": [t: "string", v: null],
		"\$httpStatusCode": [t: "integer", v: null],
		"\$httpStatusOk": [t: "boolean", v: null],
		"\$mediaId": [t: "string", d: true],
		"\$mediaUrl": [t: "string", d: true],
		"\$mediaType": [t: "string", d: true],
		"\$mediaSize": [t: "integer", d: true],
		"\$iftttStatusCode": [t: "integer", v: null],
		"\$iftttStatusOk": [t: "boolean", v: null],
		"\$locationMode": [t: "string", d: true],
		"\$shmStatus": [t: "string", d: true],
		"\$version": [t: "string", d: true],
		"\$temperatureScale": [t: "string", d: true]
	].sort{it.key}
}

private getSystemVariableValue(rtData, name) {
	switch (name) {
    	case '$args': return "${rtData.args}".toString()
    	case '$json': return "${rtData.json}".toString()
    	case '$places': return "${rtData.settings?.places}".toString()
    	case '$response': return "${rtData.response}".toString()
        case '$weather': return "${rtData.weather}".toString()
        case '$twcweather': return "${rtData.twcWeather}".toString()
        case '$nfl': return "${rtData.nfl}".toString()
        case '$incidents': return "${rtData.incidents}".toString()
        case '$shmTripped': initIncidents(rtData); return !!((rtData.incidents instanceof List) && (rtData.incidents.size()))
        case '$mediaId': return rtData.mediaId
        case '$mediaUrl': return rtData.mediaUrl
        case '$mediaType': return rtData.mediaType
        case '$mediaSize': return (rtData.mediaData ? rtData.mediaData.size() : 0)
		case "\$name": return app.label
		case "\$version": return version()
		case "\$now": return (long) now()
		case "\$utc": return (long) now()
		case "\$localNow": return (long) localTime()
		case "\$hour": def h = localDate().hours; return (h == 0 ? 12 : (h > 12 ? h - 12 : h))
		case "\$hour24": return localDate().hours
		case "\$minute": return localDate().minutes
		case "\$second": return localDate().seconds
		case "\$meridian": def h = localDate().hours; return ( h < 12 ? "AM" : "PM")
		case "\$meridianWithDots": def h = localDate().hours; return ( h < 12 ? "A.M." : "P.M.")
		case "\$day": return localDate().date
		case "\$dayOfWeek": return localDate().day
		case "\$dayOfWeekName": return weekDays()[localDate().day]
		case "\$month": return localDate().month + 1
		case "\$monthName": return yearMonths()[localDate().month + 1]
		case "\$year": return localDate().year + 1900
		case "\$midnight": return getMidnightTime(rtData)
		case "\$noon": return getNoonTime(rtData)
		case "\$sunrise": return getSunriseTime(rtData);
		case "\$sunset": return getSunsetTime(rtData);
		case "\$nextMidnight":  return getNextMidnightTime(rtData);
		case "\$nextNoon": return getNextNoonTime(rtData);
		case "\$nextSunrise": return getNextSunriseTime(rtData);
		case "\$nextSunset": return getNextSunsetTime(rtData);
		case "\$time": def t = localDate(); def h = t.hours; def m = t.minutes; return (h == 0 ? 12 : (h > 12 ? h - 12 : h)) + ":" + (m < 10 ? "0$m" : "$m") + " " + (h <12 ? "A.M." : "P.M.")
		case "\$time24": def t = localDate(); def h = t.hours; def m = t.minutes; return h + ":" + (m < 10 ? "0$m" : "$m")
		case "\$random": def result = getRandomValue("\$random") ?: (double)Math.random(); setRandomValue("\$random", result); return result
		case "\$randomColor": def result = getRandomValue("\$randomColor") ?: colorUtil?.RANDOM?.rgb; setRandomValue("\$randomColor", result); return result
		case "\$randomColorName": def result = getRandomValue("\$randomColorName") ?: colorUtil?.RANDOM?.name; setRandomValue("\$randomColorName", result); return result
		case "\$randomLevel": def result = getRandomValue("\$randomLevel") ?: (int)Math.round(100 * Math.random()); setRandomValue("\$randomLevel", result); return result
		case "\$randomSaturation": def result = getRandomValue("\$randomSaturation") ?: (int)Math.round(50 + 50 * Math.random()); setRandomValue("\$randomSaturation", result); return result
		case "\$randomHue": def result = getRandomValue("\$randomHue") ?: (int)Math.round(360 * Math.random()); setRandomValue("\$randomHue", result); return result
  		case "\$locationMode": return location.getMode()
		case "\$shmStatus": switch (hubUID ? 'off' : location.currentState("alarmSystemStatus")?.value) { case 'off': return 'Disarmed'; case 'stay': return 'Armed/Stay'; case 'away': return 'Armed/Away'; }; return null;
		case "\$temperatureScale": return location.getTemperatureScale()
    }
}

private setSystemVariableValue(rtData, name, value) {
	if (!name || !(name.startsWith('$'))) return
    def var = rtData.systemVars[name]
    if (!var || var.d) return
   	rtData.systemVars[name].v = value
}

private getRandomValue(name) {
	state.temp = state.temp ?: [:]
	state.temp.randoms = state.temp.randoms ?: [:]
	return state.temp?.randoms[name]
}

private void setRandomValue(name, value) {
	state.temp = state.temp ?: [:]
	state.temp.randoms = state.temp.randoms ?: [:]
	state.temp.randoms[name] = value
}

private void resetRandomValues() {
	state.temp = state.temp ?: [:]
	state.temp.randoms = [:]
}