#Description
Big Talker is a SmartApp for SmartThings that can make your house talk depending on various triggered events. <br />

Pair it with a SmartThings compatible audio device such as Sonos, Ubi, LANnouncer, VLC Thing, or a DLNA device using the "Generic MediaRenderer" SmartApp/Device! <br />  
See <b>More Details</b> section below for more features.<br />

Version: 1.1.10 <br />

#Support the project
 This SmartApp is free. Donations to support development efforts are accepted via (non-refundable, no gurantee of service/support):
 * PayPal to: rayzur [at] rayzurbock.com
 * Link: https://www.paypal.me/brianlowrance
 * Link: <a href=https://cash.me/$Lowrance>Cash.me/$Lowrance</a> (Debit cards = free, Credit cards charge 3% on top of your donation)
 * Link: <a href='https://squareup.com/market/brian-lowrance/item/donation-bigtalker'>Square Marketplace</a> (Square receives 2.9% + .30)

#License
**BIG TALKER -- A SmartApp for SmartThings Home Automation System** <br />
Copyright 2014-2017 - rayzur [at] rayzurbock.com - Brian S. Lowrance <br />
For the latest version, development and test releases visit http://www.github.com/rayzurbock <br />
<br />
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the  License. You may obtain a copy of the License at: <br/>
<br />
http://www.apache.org/licenses/LICENSE-2.0 <br />
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. <br/>

#SmartThings GitHub Integration Settings:
**Owner:** rayzurbock <br />
**Name:** SmartThings-BigTalker <br />
**Branch:** master   (development, for alpha/beta testing) <br />

#Installation (Manual)
1. Login at <a href=http://graph.api.smartthings.com>http://graph.api.smartthings.com</a>
2. Go to "**My SmartApps**" section and click on the "**+ New SmartApp**" button on the right.
3. On the "*New SmartApp*" page, Select the Tab "**From Code**" , Copy the BigTalker source code from GitHub (*https://github.com/rayzurbock/SmartThings-BigTalker/blob/master/smartapps/rayzurbock/big-talker.src/big-talker.groovy*) and paste it into the IDE editor window.
4. Click the "**Create**" button at the bottom.
6. Click the blue "**Save**" button above the editor window.
7. Click the "**Publish**" button next to it and select "**For Me**". You have now self-published your SmartApp.
8. Open SmartThings mobile app and go to the Market Place (star burst icon).
9. Press "**SmartApps**"
9. Press "**My Apps**"
10. "**BigTalker**" app should be available in the list of SmartApps that appears. Tap it.
  * Android users have reported having to logout of the SmartThings app, closing it, and logging back in before new apps will show up.
11. Tap **Configure** to start setting up your talking events.  Setup as many or as few as you like, but try not to setup events that would fire rapidly back to back or they will conflict with each other.

#Installation (SmartThings IDE GitHub Integration)
1. Login at <a href=http://graph.api.smartthings.com>http://graph.api.smartthings.com</a>
2. Click "**My SmartApps**"
3. Click "**Settings**"
4. Click "**Add new repository**"
  * Owner: **rayzurbock**
  * Name: **SmartThings-BigTalker**
  * Branch: **master**
8. Click "**Save**"
9. Click "**Update from Repo**"
10. Click "**SmartThings-BigTalker (master)**"
11. If there is an update you should see it in the Obsolete box. Check BigTalker.src in the Obsolete box.
12. Check "**Public**"
13. Click "**Execute Update**"
14. (If you are just updating your existing BigTalker with a new version, you are done at this step)
15. Open SmartThings mobile app and go to the Market Place (star burst icon).
16. Press "**SmartApps**"
17. Press "**My Apps**"
18. "**BigTalker**" app should be available in the list of SmartApps that appears. Tap it.
  * Android users have reported having to logout of the SmartThings app, closing it, and logging back in before new apps will show up.
19. Tap **Configure** to start setting up your talking events.  Setup as many or as few as you like, but try not to setup events that would fire rapidly back to back or they will conflict with each other.
20. For future configuration changes, Launch the SmartThings App, go to Automation > SmartApps > BigTalker

#ISSUES?
**Report Issues:** [https://github.com/rayzurbock/SmartThings-BigTalker/issues](https://github.com/rayzurbock/SmartThings-BigTalker/issues)
When reporting issues, please include the version of BigTalker that you are using. This can be found in the IDE logs with each BigTalker posted event or in the SmartThings App > Automation > BigTalker > Scroll down to the last line on the main menu under About.
If you do not have a GitHub account and do not wish to create one, you can PM me here in the community (@rayzurbock). I'd rather have one of these two methods for troubleshooting bugs and such (preferably GitHub) rather than have to scan through and try to mentally organize this thread into various issues/subjects.

If you have trouble and need assistance, please do enable debug logging within BigTalker. SmartThings > Automation > SmartApps > BigTalker > Configure Defaults > Enable debug logging. Then when reporting issues, please submit any relevant logs which can be seen in realtime at [https://graph.api.smartthings.com/ide/logs](https://graph.api.smartthings.com/ide/logs)


# More Details
Have you ever wanted a talking house? Now you can! With my Big Talker SmartApp ( http://github.com/rayzurbock/SmartThings-BigTalker )

When SmartThings is paired with a compatible audio device (such as a Sonos, Ubi, LANnouncer(http://www.keybounce.com/LANdroidHowTo/LANdroid.html), VLC Thing( http://community.smartthings.com/t/vlc-thing-a-poor-mans-sonos/5433 ), AskAlexa ( http://thingsthataresmart.wiki/index.php?title=Ask_Alexa )) and Big Talker SmartApp, your house can say what you want it to say when events occur.

Currently supported events:

* **Time**, Time of Day + Day(s) of Week
* **Motion**, Active/Inactive
* **Switch**, On/Off
* **Presence**, Arrive/Depart
* **Lock**, Lock/Unlock
* **Contact**, Open/Close
* **Mode**, Change (Home, Away, etc)
* **Thermostat**, Cooling/Heating/Fan/Idle(not running)
* **Acceleration**, Active/Inactive
* **Water**, Wet/Dry
* **Smoke**, Detect/Clear/Test
* **Button**,  Press/Held
* **Smart Home Monitor Status**, Armed-Away, Armed-Home, Disarmed

Each supported event supports 3 different groups so that you can alter your spoken phrases based on which group your selection of devices is configured in.

Voice phrases support the following variables (to be filled in at runtime)

* **%askalexa%** = Send phrase to AskAlexa message queue. (Alexa, Tell SmartThings to play messages)
* **%description%** = The description of the event that is to be displayed to the user in the mobile application.
* **%devicename%** = Triggering devices display name
* **%devicetype%** = Triggering device type; motion, switch, etc...
* **%devicechange%** = State change that occurred; on/off, active/inactive, etc...
* **%locationname%** = Hub Location name; home, work, etc...
* **%lastmode%** = Last home mode; home, away, etc...
* **%mode%** = Current home mode; home, away, etc...
* **%time%** = Current time; HH:mm am/pm
* **%shmstatus%** = SmartHome Monitor Status (Disarmed, Armed Stay, Armed Away)
* **%weathercurrent%** = Current weather (from Weather Underground; based on SmartThings Hub Zipcode)
* **%weathertoday%** = Today's weather forecast (from Weather Underground; based on SmartThings Hub Zipcode)
* **%weathertonight%** = Tonight's weather forecast (from Weather Underground; based on SmartThings Hub Zipcode)
* **%weathertomorrow%** = Tomorrow's weather forecast (from Weather Underground; based on SmartThings Hub Zipcode)
* **%weathercurrent(00000)%** = Current weather (from Weather Underground; using custom Zipcode; replace 00000)
* **%weathertoday(00000)%** = Today's weather forecast (from Weather Underground; using custom Zipcode; replace 00000)
* **%weathertonight(00000)%** = Tonight's weather forecast (from Weather Underground; using custom Zipcode; replace 00000)
* **%weathertomorrow(00000)%** = Tomorrow's weather forecast (from Weather Underground; using custom Zipcode; replace 00000)
  * (00000) can also be replaced by any supported location of the WeatherUnderground API such as (France/Paris), (latitude,longitude), (AirportCode), (State/City)

For example when turning off a switch named "Office Light" with a spoken phrase of "*%devicename% %devicetype% has been turned %devicechange%*" would speak "*Office light switch has been turned off*"

Keep in mind, if you configure highly active or too many devices, it may get annoying!! We had a family gathering and the kids kept going in/out the back door to play in the yard. "Back Door has been opened, Back Door has been closed, Back Door has been opened, you get the idea." I wouldn't configure chatty motion sensors either, but that's up to you.

***Have Fun!***

<hr>
# Revision History
*  12/13/2014 - 1.0.0 - Initial Release
*  12/17/2014 - 1.0.1 - Sonos Support corrected. Resume playback after speaking (not supported for VLC-Thing until the Device Type supports it), Corrected custom speech device save for Switch events, general cleanup.  More event support to come in new releases soon. Special thanks to SmartThings community member Greg for help with testing Sonos functionality and making this release possible.
*  12/26/2014 - 1.0.2 - Corrected bug where Sonos would show up to be selected as a default speech device, but not as a custom speech device for each device/event group.
*  3/4/2015 - 1.1.0
  * Feature: New sensor event handlers
    * Acceleration (active/inactive) event
    * Water (wet/dry) event added
    * Smoke (detected/clear/tested) event 
    * Button (press) event added, to be tested..., Need someone to test.
  * Feature: Mode change exclusion: Remain silent when changed to a configured mode, when coming from an excluded mode.  Thanks for the request SmartThingsCommunity:Greg.
  * Feature: Added default "talk while in mode(s)" with custom mode overrides for each event group.
  * Feature: Added Volume Change (supported for Sonos, VLC-Thing, not supported for Ubi due to lack of support in it's device type)
  * Feature: Toggle support for either capability.musicPlayer(Sonos/VLCThing) or capability.speechSynthesis(Ubi/VLCThing).  Note: Only one type or the other is currently configurable in the app at a time.
    * Install the app twice to support both modes.
  * Feature: Optional: Default Allowed Talk Time, with per event group override. (Thanks ST Community: Greg for the idea)
  * Feature: Added Talk Now feature (once the app is properly setup/configured, it will show up on the main page under Status and Configure).
  * Feature: Added scheduled event based on time of day and day(s) of the week.  Only allowed 3 as ST apps are only allowed 4 schedules at a time, so I'm reserving 1 for future/internal use (Thanks ST Community: Greg for the feature request)
  * Feature Modification: Adjusted some debug/trace log info
  * Feature Modification: Status page: add defaults, cleanup look
  * Feature Modification: Configuration flow change (to better support the choice of musicPlayer / speechSynthesis)
  * Feature Modification: Default text is shown in Group 1 of each device type as an example; if the user deletes the text and saves, it reappears the next time they edit the event type. This modification only fills the default text if the speech text is blank AND the device list is empty.  (Thanks for the feedback ST Community: Greg)
  * Feature Modification: Added time scheduled events to the status page
  * Feature Modification: Added phrase variable %time% which will return the current time.
  * BugFix: VLCThing reporting "stopped" instead of "disconnected" therefore it was calling "playTextAndResume" and cutting off phrases.  Adjusted to playText if no trackdata found.
  * BugFix: Switch Group 3 was not working.  onSwitch3Event() function missing; Added.  Thanks GitHub @roblandry (Issue #5).
  * BugFix: Ensure Uninstall button is always on the "Configure" page, even for partially installed instances
  * BugFix: Mode change announcement may announce previous mode incorrectly.  Resolved.
  * BugFix: Hopefully fixed a bug where upgrading from versions before 1.0.3-Beta1 speechDevice selections may show up as a text field; toggling Sonos/Ubi support resolved, so added code to try to prevent the issue to start with (Thanks ST Community: Greg for the report)
  * BugFix: When attempting to configure a "motion" event user receives the message "Error:You are not authorized to perform the requested operation" (Thanks: ST:chaaad614)
  * BugFix(attempt; needs testing): Under capability.musicPlayer Talk() calls playTextAndResume() even when it detects that nothing was playing before speaking. Changed to playTextAndRestore() when nothing is previously playing. Thanks for the report ST Community:Kristopher
  * BugFix: Fixed an issue where custom talk modes were not checked when using a scheduled Time event.
  * BugFix: Fixed an issue where current day of the week was not calculated properly for a scheduled Time event causing these events to speak on days of the week that were not desired.
  * BugFix: Fixed an issue where "Talk Now" would sometimes say the last spoken phrase upon entering the "Talk Now" page.
*  10/25/2015 - 1.1.1
  * BugFix: Corrected issue with Motion 1 announcement custom time restrictions
*  10/25/2015 - 1.1.2
  * BugFix: Corrected issue with setting speech volume. Ensure that Volume is not 0; Set to 75 if it is.
*  10/26/2015 - 1.1.3
  * BugFix: Added additional check in Talk() if using MusicPlayer device, currentTrack = null and currentStatus was "playing" then BT would not "resume", but instead would stop the track, PlayText() and not resume the track.
  * BugFix: Replaced deprecated "refreshAfterSelection" in dynamic pages with replacement "submitOnChange". This fixes things like TalkNow's expected operation.  
  * Feature Enhancement: Additional logging added in Talk()
* 1.1.4 was a development/beta version branch.  These features and bug fixes were rolled into 1.1.5
* 1/31/2016 - 1.1.5
  * Metadata Update: Update copyright from 2014 to 2014-2016.
  * New Feature: poll() or refresh() on Talk() to try to update current status of player. Protect crash using try/catch routines. Delays poll for 2 minutes after any speech event. (mediaPlayer mode only)
  * New Feature: Added detection of failure to convert text to speech (SmartThings intermittent issue with textToSpeech function), send notification instead so message is not missed.
  * New Feature: Added new Talk variables %weathercurrent%, %weathertoday%, %weathertonight%, %weathertomorrow%. Can be added to any event.
  * New Feature: Upon Install or settings update, a poll process will execute every minute to check all configured speech devices for their latest status. This is in an effort to better detect if a device is playing before interrupting to speak. (Note: VLCThing doesn't report 'playing' until after the second poll or 2 minutes into a playlist; others may as well). (mediaPlayer mode only)
  * New Feature: Added Status page item to show the ZipCode that SmartThings has derived from the hub via Latitude/Longitude.
  * New Feature: Added Phrase Variables
    * %weathercurrent% - Current weather
    * %weathertoday% - Today's weather forecast
    * %weathertonight% - Tonight's weather forecast
    * %weathertomorrow% - Tomorrow's weather forecast
    * %weathercurrent(00000)% = Current weather based on custom zipcode (replace 00000)
    * %weathertoday(00000)% = Today's weather forecast based on custom zipcode (replace 00000)
    * %weathertonight(00000)% = Tonight's weather forecast based on custom zipcode (replace 00000)
    *%weathertomorrow(00000)% = Tomorrow's weather forecast based on custom zipcode (replace 00000)
    *(00000) can also be replaced by any supported location of the WeatherUnderground API such as (France/Paris), (latitude,longitude), (AirportCode), (State/City)
  * New Feature: Add adjustWeatherPhrase() function to convert mph to "Miles Per Hour", NNE to "North Northeast", etc..
  * New Feature: Added "Help" section to each event configuration page with a "Phrase Tokens" option to give the user an In-App reference of the known phrase tokens that can be used.
  * Feature Modification: Re-write of Talk() to better detect latest playing track and play status (latestValue() instead of currentValue()). (mediaPlayer mode only)
  * Feature Modification: Modified Start page, About section
  * Feature Modification: Add LANnouncer into the supported device descriptions for speechSynthesis.
  * Feature Modification: Remove unsupported Volume setting / function when operating in speechSynthesis mode.
  * BugFix: Corrected default phrases for Water sensor (Thanks ST Community: Greg) (was using acceleration phrases)
  * BugFix: Corrected home mode change issue if exclusion was not set (Thanks for the report ST Community: Greg)
  * BugFix: Resolve an error that is present if both currentTrack and currentStatus return null. "java.lang.NullPointerException: Cannot get property 'status' on null object"
  * BugFix: Setting changes were not activated when editing within the "Configure Defaults" menu and pressing Done. Settings only activated after editing within the "Configure Events" menu and pressing Done. Resolved.
  * BugFix: Replace special characters found in phrase (ie: URL tokens) such as those that end up in weather reports which cause LANnouncer to not speak when expected.
  * BugFix: Fix Talk() routine for speechSynthesis only devices which typically do TTS on the phrase passed to them on their own or within their deviceType.
  * BugFix: Talk() function has been further optimized and more bugs have been resolved.
* 5/5/2016 - 5/25/2016
  * Added phrase token %shmstatus% to speak your current SmartHome Monitor Status (Disarmed, Armed Away, Armed Stay)  ** Note this is not documented at this time by SmartThings and may stop working at any time and/or be removed in a future version.  Thanks STCommunity: @bsanker for the request and STCommunity: @MichaelS for the tip on implementing.
* 8/22/2016 - 1.1.7
  * Added missing %devicechange% reference in in-app "Phrase Tokens" help page. (Thanks ST Community: adamoutler)
  * Removed Paypal donation link that no longer allows donation without login; just send via regular PayPal or the other methods. (Thanks ST Community: Gorilla800lbs)
  * Called setAppVersion() sooner in SmartApp execution to show version # more accurately.
* 10/11/2016 - 1.1.8
  * I believe that I have squashed a couple of bugs that may have missed playback or caused playback that wasn't desired for musicPlayer devices (Sonos, etc).
  * There is a new %description% token that you can use for a description of the event which occurred via device-specific text. Ex: %description% becomes 'Front door is closed' (Thanks for the idea STCommunity: adamoutler).
  * %devicename% does error control to find the next best device name rather than crashing the app if the expected call fails.  The calls try in the following order: evt.displayName, evt.device.displayName, and finally evt.device.name - One of these will return something; the order is to try to make sure it's the user defined name.
  *  Changes default volume applied if in musicPlayer mode and musicPlayer device volume is less than 50% and a desired volume is not configured then device will bump up to 50% to speak and should return to the user configured volume on resume (was originally set to 75% if device was at 50 or below).
* 1.1.9 = In-development and Alpha/Beta testing branch
* 3/12/2017 - 1.1.10 = Release (Major thanks to all of the alpha/beta testers of the 1.1.9 branch!!)
  * Additional Talk() debug logging
  * Test variables for null with safe navigation operator (?) in more places within Talk().
  * Attempt to resolve musicPlayer playing text and resuming/restoring.
  * Clear state.polledDevices on initialize
  * In musicPlayer mode, def Talk() now gives currentTrack.status precedence over currentStatus if it exists as it seems more accurate in testing based on feedback.
  * If default speech volume was not set, speech may not have occurred due to a null variable issue. Resolved with groovy safe navigation operator
  * Partnered with @MichaelS; Added AskAlexa support (send phrase to AskAlexa queue) with %askalexa% in the phrase.
  * Removed requirement to set a speechDevice for users that only wish to use another supported SmartApp to offload/handle the speech - (currently only AskAlexa)
  * Added Smart Home Monitor (Dashboard) status events (speak on Arm-Away, Arm-Stay, Disarm)
  * Device/SmartApp output detection logging (did we send to a device? did we only send to a SmartApp (AskAlexa), did we send a phrase but didn't send to a device or another SmartApp?)
  * Option to disable resume globally and/or per event.
  * A lot of work on Talk() and others
  * Pulled the code into Notepad++ where I could work with it better, collapse conditions, etc.
  * Found several bugs in the Talk() function that I didn't see otherwise. Squashed.
  * Added a "Poll Now" toggle to the BT main screen, when in Debug and musicPlayer mode.
  * Added option to disable periodic polling for musicPlayer mode (some users were concerned with BigTalker showing up in the "Activity" feed so often. If music starts playing after speech when it wasn't before, please make sure that polling is enabled)
  * When resume is enabled and in musicPlayer mode and in Debug mode, main menu now has an option to cause an instant poll "DEBUG: Poll Now (simply toggle)".
  * Corrected a missed null check in Talk() regarding an empty phrase.
  * Added a null check in Talk() for resume var.
  * Added logging and in-app notification if Talk() is called on an empty phrase.
  * Added Button Held event. (needs testing. I do not have a zwave button)
  * Renamed SHM Stay to SHM Home to match the language in the SmartApp Dashboard.
  * Additional null checks in Talk() (specifically addressed if the desired phrase only had %askalexa%; throw an error in the logs and notify via push that there is a configuration error due to a missing phrase)
