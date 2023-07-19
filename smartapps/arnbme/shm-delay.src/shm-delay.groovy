/*
 *  Smart Home Entry and Exit Delay and Open Contact Monitor, Parent 
 *  Functions: 
 *		Acts as a container/controller for Child module
 *		Process all Keypad activity
 * 
 *  Copyright 2017 Arn Burkhoff
 * 
 * 	Changes to Apache License
 *	4. Redistribution. Add paragraph 4e.
 *	4e. This software is free for Private Use. All derivatives and copies of this software must be free of any charges,
 *	 	and cannot be used for commercial purposes.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * 
 *	May 08, 2019 v2.2.9  Undocumented ST Platform changes killed create and send events with data
 *							requires updated keypad driver and changed code in keypadCodeHandler
 *	Mar 26, 2019 v2.2.8  Corrected keypad lights not properly see around statement 1034/5  fakeEvt = [value: theMode]
 *	Mar 14, 2019 v2.2.8  Change: Period not saved in Apple IOS, remove it as a phone number delimter
 *	Mar 12, 2019 v2.2.8  add phone number delimiters pound sign(#) and period(.) the semi colon no longer shows in android, nor is saved in IOS?
 *	Mar 03, 2019 v2.2.8  add flag to turn debug messages on, default is off
 *	Mar 03, 2019 v2.2.8  log debugging for exit delay issue
 *	Feb 19, 2019 v2.2.7  globalPinPush was miscoded should have been globalBadPinPush around line 331 Send Bad Pin Push Notification
 *	Jan 06, 2019 V2.2.6  Added: Support for 3400_G Centralite V3
 *	Jan 05, 2019 V2.2.5  Fixed: iPhone classic phone app crashes when attempting to set 3 character emergency number
 *								remove ,"" selection option						
 *	Nov 30, 2018 V2.2.4  add additional panic subscribe when using RBoy DTH
 *	Nov 30, 2018 V2.2.4  Minor logic change for Iris V3 when testing for 3405-L
 *	Nov 19, 2018 V2.2.3  Test Modefix user settings for exit delay in verify version
 *	Nov 19, 2018 V2.2.2  User exit event not running in SHM Delay BuzzerSwitch, modify routine verify_version()
 *	Nov 03, 2018 v2.2.1	 Adjust logic per Rboy suggestions
 *							Change Name of Rboy DTH
 *							When RBoy DTH do not issue: acknowledgeArmRequest and sendInvalidKeycodeResponse
 *							On install and Rboy DTH execute disableInvalidPinLogging(true) stops Rboy dth from issuing acknowledgement 
 *							On uninstall execute disableInvalidPinLogging(false) when it exists in DTH 
 *	Oct 27, 2018 v2.2.1	 Fix bug selecting keypad devices with Rboy Dth, move Rboy input selector to place that makes sense
 *	Oct 26, 2018 v2.2.1	 Fix bug testing globalKeypadDevices size when it doew not exist
 *	Oct 22, 2018 v2.2.1	 Repackage some settings and adujst some text, no logic changes
 *	Oct 21, 2018 v2.2.1	 Check for open user defined contacts prior to arming (will not arm or set exit delay)
 *						 separate setting for away and stay alarm states
 *	Oct 17, 2018 v2.2.0	 Use user exit delay settings in ModeFix to control exit delay on keypad
 *						 When two or more keypads: each keypad gets unique exit delay time setting
 *	Oct 15, 2018 v2.1.9	 Move non keypad exit delay from SHM Delay Child to routine verify_version
 *							symptom multiple non keypad exit delay messages being issued
 *							issue: when non keypad exit times vary in delay profiles the minimum number is announced 
 *	Oct 10, 2018 v2.1.8	 Add support for RBOY DTH, add global dthrboy
 *	Sep 20, 2018 v2.1.7	 Change pin verification lookup reducing overhead in routine keypadcodehandler around line 376
 *	Jul 24	2018 v2.1.7	 Pin 0000 not User or UserRoutinePiston and ingore off was previously set, it was honored
 *							(released on Sep 20, 2018)
 *	Jul 21	2018 v2.1.6	 add support for Iris Keypad quick arm with no pin and Off or Partial key
 *							sends a 0000 pin code
 *	Jul 19	2018 v2.1.5	 add notification options on Bad Pin entry on global basis
 *	Jul 18	2018 v2.1.5	 add notification options on Pin entry on global and each user pin
 *	Jul 17	2018 v2.1.4  Add support for multifunction UserRoutinePiston pins and
 *							Keypad mode selection on Routine, Piston, and UserRoutinePiston pins
 *							based upon code added to SHm Delay Users V1.0.0 to setup the fields
 *	Jul 11	2018 v2.1.3  Make all keypads sound Exit Delay tones when any keypad set to Exit Delay
 *						 Change default for Multiple Motion Sensors to True
 *	Jul 02	2018 v2.1.2  Add code to verify simkypd and talker modules
 *	Jun 27	2018 v2.1.1  Add logic to trigger all SHM Delay Talker Child profiles
 *							with exitDelay when keypad enters exitdelay
 *  Jun 16, 2018 v2.1.0  Fix Error saving page caused by lack of event on call to veerify_version
 *							add dummy_evt to call
 *  Jun 13, 2018 v2.0.9  Add logic to process pin restrictions by mode and device
 *  Jun 03, 2018 v2.0.8  Show exit delay on internet keypad, and Panic when triggered
 *							When exit delay triggered by internet keypad sound exit delay on all real keypads
 *  Jun 01, 2018 v2.0.8  Add logic to queue pinstatus, ST status and ST mode for sse display in keypad.html			
 *  May 29, 2018 v2.0.7  Add logic to KeypadLightHandler to process simulated keypads so DTH armMode is properly set
 *							Split original adding function KeypadLighton
 *  May 28, 2018 v2.0.7  Allow GlobalKeypadControl to be set when no real Keypads are defined
 *							fixes problem when using simulated keypads without a real keypad
 *  May 25, 2018 v2.0.7  Add Simulated Keypad Child App
 *  May 23, 2018 v2.0.6  Add Simulated Panic support. (deprecated, moved the simulated keypad childapp)
 *  Apr 30, 2018 v2.0.6  Add Simulated keypad support.(deprecated, moved the simulated keypad childapp)
 *  Apr 30, 2018 v2.0.5  Add Version verification when updating.
 *						 Add subscribe for alarm state change that executes Version verification
 *  Apr 25, 2018 v2.0.4  Add Dynamic Version number;
 *						 Use user defined armed (home) light mode for 3400 keypads defined in SHm Delay Modefix
 *						 Add globalDuplicateMotionSensors used by SHM Delay Child to implement logic to handle
 *							false alarm issues when a motion sensors is defined in multiple delay profiles	
 *  Apr 24, 2018 v2.0.3  When Modefix: on; then change mode with Action Tiles from night to stay;
 *							3400 keypad night light did not change to stay
 *  Apr 23, 2018 v2.0.2b cleanup keypadModeHandler debug messages, see change in keypadModeHandler
 *  Apr 23, 2018 v2.0.2a reduce overhead by asking for keypad status as needed, may be creating keypad traffic collisions
 *  Apr 23, 2018 v2.0.2  when arming on Xfinity 3400 with Stay icon, light flipped to Night icon caused instant alarm
 *							See logic for Stay and Night in routine keypadLightHandler
 *  Apr 04, 2018 v2.0.1  Fix issue with burned pin, move all documentation to community forum release thread,
 *							change global keypad selection from capability to device
 *  Mar 20, 2018 v2.0.0  add reverse mode fix, user defined modes, set Alarm State when mode changes 
 *  Mar 18, 2018 v2.0.0  add Panic option
 *	Mar 14, 2018 v2.0.0  add logic that executes a Routine for a pin 
 *	Mar 13, 2018 v2.0.0  add logic for weekday, time and dates just added to SHM Delay User 
 *  Mar 02, 2018 v2.0.0  add support for users and total keypad control
 *							Use mode vs alarmstatus to set Keypad mode lights, requires modefix be live
 *	Dec 31, 2017 v1.6.0  Add bool to allow Multiple Motion sensors in delay profile,
 *							without forcing existing users to update their profile data.
 *	Sep 23, 2017 v1.4.0  Document True Entry Delay and optional followed motion sensor in Delay Profile 
 *	Sep 23, 2017 v1.3.0  Add Global setting for option True Entry Delay, default off/false 
 * 	Sep 06, 2017 v1.2.0b add custom app remove button and text
 * 	Sep 02, 2017 v1.2.0a fix sorry there was an unexpected error due to having app name as modefixx from testing on
 *					one of the app connections
 * 	Sep 02, 2017 v1.2.0  repackage Modefix logic back into child ModeFix module where it belongs
 * 	Aug 30, 2017 v1.1.1  add global for using the upgraded Keypad module.
 * 	Aug 27, 2017 v1.1.0  Add child module SHM Delay ModeFix for Mode fixup profiles and adjust menus to reflect status
 * 	Aug 25, 2017 v1.1.0  SmartHome send stay mode when going into night mode. Force keypad to show
 *					night mode and have no entry delay. Add globalTrueNight for this option and globalFixMode 
 *	Aug 23, 2017 v1.0.7  Add police 911 and telephone numbers as links in notification messages
 *	Aug 20, 2017 v1.0.6a Change default global options: non-unique to false, create intrusion messages to true
 *					update documentation
 *	Aug 19, 2017 v1.0.6  Add global options allowing non unique simulated sensors, and alarm trigger messages
 *	Aug 17, 2017 v1.0.5  Revise documentation prior to release
 *	Aug 14, 2017 v1.0.4  Revise documentation for exit delay, split about page into about and installation pages
 *	Aug 14, 2017 v1.0.3  Revise initial setup testing app.getInstallationState() for COMPLETE vs childApps.size
 *					done in v1.0.1
 *	Aug 13, 2017 v1.0.2  Add documentation pages (Thanks to Stephan Hackett Button Controller)
 *	Aug 12, 2017 v1.0.1  Add warning on initial setup to install first (Thanks to E Thayer Lock Manager code) 
 *	Aug 11, 2017 v1.0.0  Create from example in developer documentation 
 *
 */

include 'asynchttp_v1'

definition(
    name: "SHM Delay",
    namespace: "arnbme",
    author: "Arn Burkhoff",
    description: "(${version()}) Smart Home Monitor Exit/Entry Delays with optional Keypad support",
    category: "My Apps",
    iconUrl: "https://www.arnb.org/IMAGES/hourglass.png",
    iconX2Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    iconX3Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    singleInstance: true)

preferences {
    page(name: "main")
    page(name: "globalsPage", nextPage: "main")	
}

def version()
	{
	return "2.2.9";
	}
def main()
	{
	dynamicPage(name: "main", install: true, uninstall: true)
		{
		if (app.getInstallationState() == 'COMPLETE')	//note documentation shows as lower case, but returns upper
			{  
			def modeFixChild="Create"
			def children = getChildApps()
			children.each
				{ child ->
				def childLabel = child.getLabel()
				def appid=app.getId()
//				logdebug "child label ${childLabel} ${appid}"
				if (childLabel.matches("(.*)(?i)ModeFix(.*)"))	
					{
					modeFixChild="Update"
					}
				}
			def modeActive=" Inactive"
			if (globalFixMode || globalKeypadControl)
				{modeActive=" Active"}
			def fixtitle = modeFixChild + modeActive + " Mode Fix Settings"
			section 
				{
				input "logDebugs", "bool", required: false, defaultValue:false,
					title: "Log debugging messages? SHM Delay module only. Normally off/false"
				}
			section 
				{
				app(name: "EntryDelayProfile", appName: "SHM Delay Child", namespace: "arnbme", title: "Create A New Delay Profile", multiple: true)
				}
			if (globalKeypadControl)
				{
				section 
					{
					app(name: "UserProfile", appName: "SHM Delay User", namespace: "arnbme", title: "Create A New User Profile", multiple: true)
					}
				section 
					{
					app(name: "SimKypdProfile", appName: "SHM Delay Simkypd Child", namespace: "arnbme", title: "Create A New Sim Keypad Profile", multiple: true)
					}
				}	
			section 
				{
				app(name: "TalkerProfile", appName: "SHM Delay Talker Child", namespace: "arnbme", title: "Create A New Talker Profile", multiple: true)
				}
			section
    			{
  				href(name: 'toglobalsPage', page: 'globalsPage', title: 'Globals Settings')
				}	
			section
				{
			if (globalFixMode && modeFixChild == "Create")
				{
				app(name: "ModeFixProfile", appName: "SHM Delay ModeFix", namespace: "arnbme", title: "${fixtitle}", multiple: false)
				}	
			else
				{
				app(name: "ModeFixProfile", appName: "SHM Delay ModeFix", namespace: "arnbme", title: "${fixtitle}", multiple: false)
				}	
				}
			}
		else
			{
			section 
				{
				paragraph "Please review and set global settings, then complete the install by clicking 'Save' above. After the install completes, you may set Delay, User and Modefix profiles"
				}
			section
    			{
  				href(name: 'toglobalsPage', page: 'globalsPage', title: 'Globals Settings')
				}	
			}	
/*		section
			{
			href (url: "https://community.smartthings.com/t/release-shm-delay-version-2-0/121800",
			title: "Smartapp Documentation",
			style: "external")
			}
*/		section
			{
			paragraph "SHM Delay Version ${version()}"
			}
		remove("Uninstall SHM Delay","Warning!!","This will remove the ENTIRE SmartApp, including all profiles and settings.")
		}
	}

def globalsPage()
	{	
	dynamicPage(name: "globalsPage", title: "Global Settings")
		{
		section 
			{
			input "globalDisable", "bool", required: true, defaultValue: false,
				title: "Disable All Functions. Default: Off/False"
			input "globalKeypadControl", "bool", required: true, defaultValue: false, submitOnChange: true,
				title: "A real or simulated Keypad is used to arm and disarm Smart Home Monitor (SHM). Default: Off/False"
			input "globalIntrusionMsg", "bool", required: false, defaultValue: true,
				title: "This app issues an intrusion message with name of triggering real sensor? Default: On/True."
			input (name: "global911", type:"enum", required: false, options: ["911","999","112"],
				title: "Add 3 digit emergency call number on this app's intrusion message?")
			input "globalPolice", "phone", required: false, 
				title: "Include this phone number as a link on this app's intrusion message? Separate multiple phone numbers with a pound sign(#), or semicolon(;)"
			input "globalDuplicateMotionSensors", "bool", required: true, defaultValue: false, 
				title: "I have the same motion sensor defined in multiple delay profiles. Stop false motion sensor triggered alarms by cross checking for sensor in other delay profiles.\nDefault Off/False"
			if (globalKeypadControl)
				{
				input "globalFixMode", "bool", required: true, defaultValue: true,
					title: "Mode Fix when system armed from non keypad source: \nAlarm State change - verify and set a valid SHM mode\nSHM Mode change - verify and set Alarm state\nthen set keypad status and lights to match system.\nDefault: On/True"
				}
			else	
				{
				input "globalFixMode", "bool", required: true, defaultValue: false,
					title: "Mode Fix when system armed from non keypad source: \nAlarm State change - verify and set a valid SHM mode\nSHM Mode change - verify and set Alarm status.\nDefault: Off/False"
				}
//			input "globalKeypad", "bool", required: true, defaultValue: false,		//deprecated Was used with Version1
//				title: "The upgraded Keypad module is installed Default: Off/False"
			input "globalTrueNight", "bool", required: true, defaultValue: false, 
				title: "True Night Flag. When arming in Stay from a non keypad device, or Partial from an Iris keypad, and monitored sensor triggers:\nOn: Instant intrusion\nOff: Entry Delay"
			if (globalKeypadControl)
				{
				input "globalRboyDth", "bool", required: false, defaultValue:false, submitOnChange: true,
					title: "I am using the RBoy Apps Keypad DTH"
				def actions = location.helloHome?.getPhrases()*.label
				actions?.sort()
				if (globalRboyDth)
					{
					input "globalKeypadDevices", "device.EnhancedZigbeeKeypadLock", required: false, multiple: true, submitOnChange: true,
						title: "Real Keypads used to arm and disarm SHM"
					}
				else	
					{
					input "globalKeypadDevices", "device.CentraliteKeypad", required: false, multiple: true, submitOnChange: true,
						title: "Real Keypads used to arm and disarm SHM"
					}
				if (globalKeypadDevices && globalKeypadDevices.size() > 1)
					{
					def kpnm
					globalKeypadDevices.each
						{
						kpnm=it.displayName.replaceAll(" ","_")	
						input "globalKeypadExitDelay${kpnm}", "number", required: true, range: "0..90", defaultValue: 30,
							title: "Device: ${kpnm}, Exit delay seconds when arming with a delay from this keypad. range 0-90, default:30"
						}	
					}
				else
					{
					input "globalKeypadExitDelay", "number", required: true, range: "0..90", defaultValue: 30,
						title: "Default True exit delay in seconds when arming with a delay. range 0-90, default:30"
					}
				input "globalOff", "enum", options: actions, required: true, defaultValue: "I'm Back!",
					title: "Keypad Disarmed/OFF executes Routine. Default: I'm Back!"
				input "globalStay", "enum", options: actions, required: true, defaultValue: "Good Night!",
					title: "Keypad Stay/Partial executes Routine. Default: Good Night!"
				input "globalNight", "enum", options: actions, required: true, defaultValue: "Good Night!",
					title: "Keypad Night executes Routine. Default: Good Night!"
				input "globalAway", "enum", options: actions, required: true, defaultValue: "Goodbye!",
					title: "Keypad Away/On executes Routine. Default: Goodbye!"
				input "globalPanic", "bool", required: true, defaultValue: true,
					title: "Iris Panic Key is Monitored. No Panic key? Set this flag on, add a User Profile, Pin Usage: Panic. Default: On/True"
//				input "globalBadpins", "number", required: true, range: "0..5", defaultValue: 1,
//					title: "Sound invalid pin code tone on keypad after how many invalid pin code entries. 0 = disabled, range: 1-5, default: 1"
//				input "globalBadpinsIntrusion", "number", required: true, range: "0..10", defaultValue: 4,
//					title: "(Future enhancement) Create intrusion alert after how many invalid pin code entries. 0 = disabled, range: 1-10, default: 4"
				input "globalPinMsgs", "bool", required: false, defaultValue: true, submitOnChange: true,
					title: "Log pin entries. Default: On/True"
				if (globalPinMsgs)
					{
					input "globalPinLog", "bool", required: false, defaultValue:true,
						title: "Log Pin to Notifications?"
					if (location.contactBookEnabled)
						{
						input("globalPinRecipients", "contact", title: "Pin Notify Contacts (When used ST system forces send to notification log, so set prior setting to false)",required:false,multiple:true) 
						input "globalPinPush", "bool", required: false, defaultValue:false,
							title: "Send Pin Push Notification?"
						}
					else	
						{
						input "globalPinPush", "bool", required: false, defaultValue:true,
							title: "Send Pin Push Notification?"
						}
					input "globalPinPhone", "phone", required: false, 
						title: "Send Pin text message to this number. For multiple SMS recipients, separate phone numbers with a pound sign(#), or semicolon(;)"
					}
				input "globalBadPinMsgs", "bool", required: false, defaultValue: true, submitOnChange: true,
					title: "Log invalid keypad entries, pins not found in a User Profile Default: On/True"
				if (globalBadPinMsgs)
					{
					input "globalBadPinLog", "bool", required: false, defaultValue:true,
						title: "Log Bad Pins to Notifications?"
					if (location.contactBookEnabled)
						{
						input("globalBadPinRecipients", "contact", title: "Bad Pin Notify Contacts (When used ST system forces send to notification log, so set prior setting to false)",required:false,multiple:true) 
						input "globalBadPinPush", "bool", required: false, defaultValue:false,
							title: "Send Bad Pin Push Notification?"
						}
					else	
						{
						input "globalBadPinPush", "bool", required: false, defaultValue:true,
							title: "Send Bad Pin Push Notification?"
						}
					input "globalBadPinPhone", "phone", required: false, 
						title: "Send Invalid Bad Pin text message to this number. For multiple SMS recipients, separate phone numbers with a pound sign(#), or semicolon(;)"
					}

				input "globalAwayContacts", "capability.contactSensor", required: false, submitOnChange: true, multiple: true,
					title: "(Optional!) Contacts must be closed prior to arming Away from a Keypad"
				if (globalAwayContacts)
					{
					input (name: "globalAwayNotify", type:"enum", required: false, options: ["Notification log", "Push Msg", "SMS","Talk"],multiple:true,
						title: "How to notify contact is open, arming Away")
					}
				input "globalStayContacts", "capability.contactSensor", required: false, submitOnChange: true, multiple:true,
					title: "(Optional!) Contacts must be closed prior to arming Stay from a Keypad."
				if (globalStayContacts)
					{
					input (name: "globalStayNotify", type:"enum", required: false, options: ["Notification log", "Push Msg", "SMS","Talk"],multiple:true,
						title: "How to notify contact is open arming Stay")
					}
				}
			input "globalSimUnique", "bool", required: false, defaultValue:false,
				title: "Simulated sensors must be unique? Default: Off/False allows using a single simulated sensor."
			input "globalTrueEntryDelay", "bool", required: true, defaultValue: false,
				title: "True Entry Delay: This is a last resort when adding motion sensors to delay profile does not stop Intrusion Alert. AlarmState Away and Stay with an entry delay time, ignore triggers from all other sensors when Monitored Contact Sensor opens. Default: Off/False"
			input "globalMultipleMotion", "bool", required: true, defaultValue: true,
				title: "Allow Multiple Motion Sensors in Delay Profile. Default: On/True" 
			}
		}
	}	

def installed() {
    log.info "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.info "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize()
	{
	if (globalKeypadControl && !globalDisable)
		{
		subscribe(globalKeypadDevices, 'codeEntered',  keypadCodeHandler)
		subscribe (location, "mode", keypadModeHandler)
		if (globalPanic)
			{
			if (globalRboyDth)
				subscribe (globalKeypadDevices, "button.pushed", keypadPanicHandler)
			else	
				subscribe (globalKeypadDevices, "contact.open", keypadPanicHandler)
			}
		globalKeypadDevices?.each
			{
			if (it.hasCommand("disableInvalidPinLogging"))
				it.disableInvalidPinLogging(true)
			}
		}
	subscribe(location, "alarmSystemStatus", verify_version)
	verify_version("dummy_evt")
	}	

def uninstalled()
	{
	globalKeypadDevices?.each
		{
		if (it.hasCommand("disableInvalidPinLogging"))
			it.disableInvalidPinLogging(false)
		}
	}

//  --------------------------Keypad support added Mar 02, 2018 V2-------------------------------
/*				Basic location modes are Home, Night, Away. This can be very confusing
Xfinity			Default mode
Centralite	Iris		Location	Default			Triggers	Xfinity		Iris
Icon		Button		Mode 		AlarmStatus		Routine		Icon lit	key lit
(off)		Off			Home		off				I'm Home	(none)		Off??
Stay		Partial		Night		stay			GoodNight	Stay		Partial
Night					Night		stay			GoodNight	Stay		Partial, but night key should not occur	
Away		On			Away		away			GoodBye!	Away		Away


Xfinity			When Location Stay mode is defined and SHM Stay routine defined for Xfinity only
Centralite				Location	Default			Triggers	Xfinity		
Keypad					Mode 		AlarmStatus		Routine		Icon lit	
(off)					Home		off				I'm Home	(none)		
Stay					Stay		stay			Stay		Stay		
Night					Night		stay			GoodNight	Night			
Away					Away		away			GoodBye!	Away		
*/
def keypadCodeHandler(evt)
	{
//	User entered a code on a keypad	
	if (!globalKeypadControl || globalDisable)
		{return false}			//just in case
	def keypad = evt.getDevice();
//	logdebug "keypadCodeHandler called: $evt by device : ${keypad.displayName}"
	def str = evt.value.split("[/]");
	def codeEntered = str[0] as String				//the entered pin
	def modeEntered = str[1] as Integer				//the selected mode off(0), stay(1), night(2), away(3)
	def itext = [dummy: "dummy"]						//external find it data or dummy map to fake it when pin not found										
	def fireBadPin=true									//flag to stop double band pin fire. Caused by timing issue 
//															with Routine, Piston and UserRoutinePiston processing	
	if (modeEntered < 0 || modeEntered> 3)				//catch an unthinkable bad mode, this is catastrophic 
		{
		log.error "${app.label}: Unexpected arm mode ${modeEntered} sent by keypad!"
		if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
			keypad.sendInvalidKeycodeResponse()
		return false
		}
//	def currentarmMode = keypad.currentValue('armMode')
//	logdebug("Delayv2 codeentryhandler searching user apps for keypad ${keypad.displayName} ${evt.data} ${evt.value}")
	def userName=false;
	def badPin=true;
	def badPin_message = keypad.displayName + "\nInvalid pin: " + codeEntered
	def error_message=""
	def info_message=""
	def pinKeypadsOK=false;
	def damap=[dummy: "dummy"]				//dummy return map for Routine and Piston processing
	
//	Try to find a matching pin in the pin child apps	
//	def userApps = getChildApps()		//gets all completed child apps Sep 20, 2018
	def userApps = findAllChildAppsByName('SHM Delay User')
	userApps.find 	
		{
//		if (it.getName()=="SHM Delay User" && it.theuserpin == codeEntered)	Sep 20, 2018
		if (it.getInstallationState()=='COMPLETE' && it.theuserpin == codeEntered)	
			{
//			logdebug ("found the pin ${it.getName()} ${it.theuserpin} ${it.theusername} ")
//			verify burn cycles
			itext=it										//save for use outside of find loop
			if (it.themaxcycles > 0)						//check if pin is burned
				{
				def atomicUseId=it.getId()+'uses'			//build unique atomic id for uses
				if (atomicState."${atomicUseId}" < 0)		//initialize if never set
					{atomicState."${atomicUseId}" = 1}
				else	
		    		{atomicState."${atomicUseId}" = atomicState."${atomicUseId}" + 1}
		    	if (atomicState."${atomicUseId}" > it.themaxcycles)
		    		{
					logdebug "pin $codeEntered burned"
					error_message = keypad.displayName + " Burned pin entered for " + it.theusername
	    			}
	    		}	
			if (error_message == "" && codeEntered == '0000' && modeEntered == 0 && 
				(it.thepinusage=='User' || it.thepinusage=='UserRoutinePiston') && it?.thepinIgnoreOff)
				{
				badPin=true
				error_message=badPin_message
				}
			else	
				{
				badPin=false
				badPin_message=""
				}
//			logdebug "matched pin ${it.theuserpin} $it.pinScheduled"
//			When pin is scheduled verify Dates, Weekday and Time Range	
			if (error_message=="" && it.pinScheduled)
				{
//				keep this code in sync with similar code in SHM Delay Users				
    			def df = new java.text.SimpleDateFormat("EEEE")	//formatter for current time    			
    			df.setTimeZone(location.timeZone)
    			def day = df.format(new Date())
    			def df2 = new java.text.SimpleDateFormat("yyyyMMdd")    			
    			df2.setTimeZone(location.timeZone)
				def nowymd = df2.format(new Date());		//	the yyyymmdd format for comparing and processing
				def dtbetween=true
				def num_dtstart
				def num_dtend
				if (it.pinStartDt > "")
					num_dtstart=it.dtEdit(it.pinStartDt)
				if (it.pinEndDt > "")
					num_dtend=it.dtEdit(it.pinEndDt)
//				logdebug "pin found with schedule $nowymd $num_dtstart $num_dtend"
//				verify the dates
				if (it.pinStartDt>"" && it.pinEndDt>"")
					{
					if (num_dtstart > nowymd || num_dtend < nowymd)
						error_message = keypad.displayName + " dates out of range with pin for " + it.theusername
					}
				else
				if (it.pinStartDt>"")
					{
					if (num_dtstart > nowymd)
						error_message = keypad.displayName + " start date error with pin for " + it.theusername
					}
				else
				if (it.pinEndDt>"")
					{
					if (num_dtend < nowymd)
						error_message = keypad.displayName + " end date expired with pin for " + it.theusername
					}

//				verify the weekdays
				if (error_message=="" && it.pinDays)
					{
					if (!it.pinDays.contains(day))
						error_message = keypad.displayName + " not valid on $day with pin for " + it.theusername
					}
					
//				verify the hours stored by system as 2018-03-13T11:30:00.000-0400
				if (error_message=="" && it.pinStartTime>"" && it.pinEndTime>"")
					{
   					def between = timeOfDayIsBetween(it.pinStartTime.substring(11,16), it.pinEndTime.substring(11,16), new Date(), location.timeZone)
					if (!between)
						error_message = keypad.displayName + " time out of range with pin for " + it.theusername
					}
				}
				
//	Process pin mode and device restrictions
			if (error_message=="" && it.pinRestricted)
				{
				if (it.pinModes)
					{
					if (!it.pinModes.contains(location.mode))
						error_message = keypad.displayName + " mode: "+ location.mode + " invalid with pin for " + it.theusername
					}
				if (error_message=="" && (it.pinRealKeypads || it.pinSimKeypads))
					{
//					this wont work sigh if (it.pinSimKeypads.contains(keypad.displayName))
					it.pinRealKeypads.each
						{kp ->
						if (kp.displayName == keypad.displayName)
							pinKeypadsOK=true
						}
					it.pinSimKeypads.each
						{kp ->
						if (kp.displayName == keypad.displayName)
							pinKeypadsOK=true
						}
					if (!pinKeypadsOK) 	
						error_message = keypad.displayName + " is unauthorized keypad with pin for " + it.theusername
					}
				}	

//			Verify pin usage
			if (error_message=="")
				{
//				logdebug "processing the pin for ${it.thepinusage}"
				switch (it.thepinusage)
					{
					case 'User':
					case 'UserRoutinePiston':		//process arming now or get a possible keypad timeout
						userName=it.theusername
						break
					case 'Disabled':
						error_message = keypad.displayName + " disabled pin entered for " + it.theusername
						break
					case 'Ignore':
						error_message = keypad.displayName + " ignored pin entered for " + it.theusername
						break
					case 'Routine':
//						forced to do acknowledgeArmRequest here due to a hardware timeout on keypad
						if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
							keypad.acknowledgeArmRequest(4)				
						acknowledgeArmRequest(4,keypad);
						fireBadPin=false
						damap=process_routine(it, modeEntered, keypad)
						logdebug "Routine created ${damap}"
						if (damap?.err)
							error_message=damap.err
						else	
						if (damap?.info)
							info_message=damap.info
						break
					case 'Panic':
						if (globalPanic)
							{
							error_message = keypad.displayName + " Panic entered with pin for " + it.theusername
							keypadPanicHandler(evt)					
//							panicContactOpen()	//unable to get this working use klunky method above
							}
						else	
							{
							error_message = keypad.displayName + " Panic entered but globalPanic flag disabled with pin for " + it.theusername
							}
						break
					case 'Piston':
//						forced to do acknowledgeArmRequest here due to a possible hardware timeout on keypad
						if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
							keypad.acknowledgeArmRequest(4)				
						acknowledgeArmRequest(4,keypad);
						fireBadPin=false
						damap=process_piston(it, modeEntered, keypad)
						if (damap?.err)
							error_message=damap.err 
						else	
						if (damap?.info)
							info_message=damap.info 	
						else
							error_message = "Process Piston returned bad data: ${dmap} "
						break
					default:
						userName=it.theusername	
						break
					}		
				}
			return true				//this ends the ***find*** loop, not the function
			}
		else
			{return false}			//this continues the ***find*** loop, does not end function
		}

//	Now done with find loop and editing the pin entered on the keypad  		

	if (error_message!="")									// put out any messages to notification log
		{
		badPin=true
//		logdebug "${error_message} info ${info_message}"
		doPinNotifications(error_message, itext)
		}
	else	
	if (info_message!="")									
		{
		doPinNotifications(info_message, itext)
		}
		
//	Was pin not found
/*  in theory acknowledgeArmRequest(4) and sendInvalidKeycodeResponse() send same command
	but not working that way. Look at this when I get some time
*/	

	if (badPin)
		{
		if (fireBadPin)
			{
			if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
				keypad.acknowledgeArmRequest(4)				//always issue badpin very long beep
			acknowledgeArmRequest(4,keypad);
			}
		if (globalBadPinMsgs && badPin_message !="")
			doBadPinNotifications (badPin_message, itext)
/*	
**		Deprecated this logic on Mar 18, 2018 for better overall operation
		if (globalBadPins==1)
			{
			if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
				keypad.acknowledgeArmRequest(4)			//sounds a very long beep
			acknowledgeArmRequest(4,keypad);
			}
		else
			{
			if (atomicState.badpins < 0)			//initialize if never set
				{atomicState.badpins=0}
	    	atomicState.badpins = atomicState.badpins + 1
	    	if (atomicState.badpins >= globalBadpins)
	    		{
				if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
					keypad.acknowledgeArmRequest(4)		//sounds a very long beep
				acknowledgeArmRequest(4,keypad);
				atomicState.badpins = 0
    			}
			else
				{
				if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
					keypad.sendInvalidKeycodeResponse()	//sounds a medium duration beep
				acknowledgeArmRequest(4,keypad);
				}
    		}	
*/		return;
 		}

		
//	was this pin associated with a person
	if (!userName)									//if not a user pin, no further processing
		{
		if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
			keypad.sendInvalidKeycodeResponse()			//sounds a medium duration beep
		return
		}

//	Oct 21, 2018 verify contacts are closed prior to arming or exit delay
//	Message sensor, sensor open, Arming cancelled
	if (modeEntered > 0)
		{
		logdebug "checking for open contacts"
		if (modeEntered == 3)
			{
			if (globalAwayContacts)
				{
				if (!checkOpenContacts(globalAwayContacts, globalAwayNotify, keypad))
					return
				}
			}
		else
		if (globalStayContacts)
			{
			if(!checkOpenContacts(globalStayContacts, globalStayNotify, keypad))
				return
			}	
		}	

	if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
		keypad.acknowledgeArmRequest(modeEntered) 		//keypad demands a followup light setting or all lights blink
	acknowledgeArmRequest(modeEntered,keypad);
	unschedule(execRoutine)		//Attempt to handle rearming/disarming during exit delay by unscheduling any pending away tasks 
//	atomicState.badpins=0		//reset badpin count
	def armModes=['Home','Stay','Night','Away']
	def alarmModes=['home','stay','stay','away']
	def message = keypad.displayName + "\nset mode to " + armModes[modeEntered] + "\nwith pin for " + userName
	def aMap = [data: [codeEntered: codeEntered, armMode: armModes[modeEntered]]]
	def mf
	def am
	def daexitdelay=false
	def internalExitDelay=30		//set a default just in case
	if (globalKeypadDevices && globalKeypadDevices.size() > 1)
		{
		def kpnm=keypad.displayName.replaceAll(" ","_")
//		logdebug "keypad is $kpnm"
		if ("globalKeypadExitDelay${kpnm}")
			{
			internalExitDelay=settings."globalKeypadExitDelay${kpnm}"
//			logdebug "keypad ${kpnm} used, setting exit delay to ${internalExitDelay}"
			}
		else
		if (globalKeypadExitDelay)
			{
//			logdebug "Did not find setting for ${kpnm} using global default setting"
			internalExitDelay=globalKeypadExitDelay
			}
		}
	else
	if (globalKeypadExitDelay)
		{
//		logdebug "less than two keypads defined using global default"
		internalExitDelay=globalKeypadExitDelay
		}
		
	if (modeEntered > 0 && internalExitDelay > 0)
		{
		mf=findChildAppByName('SHM Delay ModeFix')
//		logdebug "${mf.getInstallationState()} ${mf.version()}"
		if (mf && mf.getInstallationState() == 'COMPLETE' && mf.version() > '0.1.4')
			{
			am="${alarmModes[modeEntered]}Exit${armModes[modeEntered]}"
			daexitdelay = mf."${am}"
//			logdebug "Version ${mf.version()} the daexitdelay is ${daexitdelay}"
			}
		else
		if (modeEntered==3)
			{daexitdelay=true}
		}	
	if (daexitdelay)
		{
		logdebug "entered exit delay for $am delay: ${internalExitDelay}"
		globalKeypadDevices.each
			{
			it.setExitDelay(internalExitDelay)
			}
		runIn(internalExitDelay, execRoutine, aMap)
		def locevent = [name:"shmdelaytalk", value: "exitDelay", isStateChange: true,
			displayed: true, descriptionText: "Issue exit delay talk event", linkText: "Issue exit delay talk event",
			data: internalExitDelay]	
		sendLocationEvent(locevent)
		qsse_status_mode(false,"Exit%20Delay")
		}
	else
		{execRoutine(aMap.data)}
	doPinNotifications(message,itext)

//	Process remainder of UserRoutinePiston settings
	if (itext.thepinusage == 'UserRoutinePiston')
		{
		damap=process_routine(itext, modeEntered, keypad)
		if (damap?.err)
			{
			if (damap.err != "nodata")
				doPinNotifications(damap.err,itext)			//no message when no routines where coded
			}
		else	
		if (damap?.info)
			doPinNotifications(damap.info,itext)
		else
			doPinNotifications("Process Routine returned bad data: ${damap}",itext)

		damap=process_piston(itext, modeEntered, keypad)
		if (damap?.err)
			{
			if (damap.err != "nodata")
				doPinNotifications(damap.err,itext)			//no message when no routines where coded
			}
		else	
		if (damap?.info)
			doPinNotifications(damap.info,itext)
		else
			doPinNotifications("Process Piston returned bad data: ${damap}",itext)
		}
	}

def acknowledgeArmRequest(armMode,keypad)
//	Post the status of the pin to the shmdelay_oauth db table
	{
//	logdebug "acknowledgeArmRequest entererd ${keypad?.getTypeName()} ${keypad.name}"
	if (keypad?.getTypeName()!="Internet Keypad")
		{return false}
//	keypad.properties.each { k,v ->	logdebug "${k}: ${v}"}
	def pinstatus
	if (armMode <  0 || armMode > 3)
		pinstatus="Rejected"
	else
		pinstatus ="Accepted"
	def uri='https://www.arnb.org/shmdelay/qsse.php'
	def simKeypadDevices=findAllChildAppsByName('SHM Delay Simkypd Child')
	simKeypadDevices.each
		{
		
		if (it.simkeypad.name == keypad.name)
			{
			uri+='?i='+it.getAtomic('accessToken').substring(0,8)	
			uri+='&p='+ pinstatus
//			logdebug "firing php ${uri} ${it.simkeypad.name} ${it.getAtomic('accessToken')}"
			try {
				asynchttp_v1.get('ackResponseHandler', [uri: uri])
				}
			catch (e)
				{
				logdebug "qsse.php Execution failed ${e}"
				}
			}
		}	
	}

def qsse_status_mode(status,mode)
//	store the status of the ST status and mode to the shmdelay_oauth db table for all simulated keypads
	{
	def st_status=status
	if (!st_status)
		st_status = location.currentState("alarmSystemStatus").value
	if (st_status=="off")
		st_status="Disarmed"
	else
		st_status="Armed%20("+st_status+")"	//need to base64 to get this to send
	def st_mode=mode
	if (!st_mode)
		st_mode = location.currentMode
	def uri
	findAllChildAppsByName('SHM Delay Simkypd Child').each
		{
		if (it.getInstallationState()=='COMPLETE')
			{
			uri='https://www.arnb.org/shmdelay/qsse.php'
			uri+='?i='+it.getAtomic('accessToken').substring(0,8)	
			uri+='&s='+ st_status
			uri+='&m='+ st_mode
//			logdebug "firing php ${uri} ${it.simkeypad.name} ${it.getAtomic('accessToken')}"
			try {
				asynchttp_v1.get('ackResponseHandler', [uri: uri])
				}
			catch (e)
				{
				logdebug "qsse.php Execution failed ${e}"
				}
			}
		}	
	}

def ackResponseHandler(response, data)
	{
    if(response.getStatus() != 200)
    	sendNotificationEvent("SHM Delay qsse.php HTTP Error = ${response.getStatus()}")
	}

def execRoutine(aMap) 
//	Execute default SmartHome Monitor routine, setting ST AlarmStatus and SHM Mode
	{
	def armMode = aMap.armMode
	def kMap = [mode: armMode, dtim: now()]	//save mode dtim any keypad armed/disarmed the system for use with
//											  not ideal prefer alarmtime but its before new alarm time is set
	def kMode=false							//new keypad light setting, waiting for mode to change is a bit slow
	def kbMap = [value: armMode, source: "keypad"]		
	logdebug "execRoutine aMap: ${aMap} kbMap: ${kbMap}"
	if (armMode == 'Home')					
		{
		keypadLightHandler(kbMap)
		location.helloHome?.execute(globalOff)
		}
	else
	if (armMode == 'Stay')
		{
		keypadLightHandler(kbMap)
		location.helloHome?.execute(globalStay)
		}
	else
	if (armMode == 'Night')
		{
		keypadLightHandler(kbMap)
		location.helloHome?.execute(globalNight)
		}
	else
	if (armMode == 'Away')
		{
		keypadLightHandler(kbMap)
		location.helloHome?.execute(globalAway)
		} 
	atomicState.kMap=kMap					//SHM Delay Child DoorOpens and MotionSensor active functions
	}

def keypadModeHandler(evt)		//react to all SHM Mode changes
	{
	if (globalDisable)
		{return false}			//just in case
	def	theMode=evt.value		//Used to set the keypad button/icon lights		
	def theStatus=false			//used to set SHM Alarm State
	if (globalFixMode)			//if global fix mode use data allowing for user defined modes
		{
		def it=findChildAppByName('SHM Delay ModeFix')
		if (it?.getInstallationState()!='COMPLETE')
			logdebug "keypadModeHandler: Modefix is not fully installed, please adjust data then save"
		else
		if (it.offDefault == theMode)
			{
			theStatus='off'
			theMode='Home'
			}
		else
		if (it.awayDefault == theMode)
			{
			theStatus='away'
			theMode='Away'
			}
		else
//		if (it.stayDefault == theMode)		2.0.4 Apr 24, 2018 commented out, handled to stayModes.contains below
//			{
//			theStatus='stay'
//			theMode='Night'
//			}
//		else 
		if (it.offModes.contains(theMode))
			{
			theStatus='off'
			theMode='Home'
			}
		else 
		if (it.awayModes.contains(theMode))
			{
			theStatus='away'
			theMode='Away'
			}
		else 
		if (it.stayModes.contains(theMode))
			{
			theStatus='stay'
//			if (theMode!="Stay")		//2.0.3 Apr 24, 2018 fix keypad light not changing from Night to Stay on 3400 keypad 
			if (it."stayLight${theMode}")	//2.0.4 Apr 24, 2018 select Icon light from User set Modefix Icon light data 
				{
				theMode=it."stayLight${theMode}"
//				logdebug "Stay mode ${theMode} picked from settings"
				}
			else	
				{
//				logdebug "Stay mode default night mode used"
				theMode='Night'
				}
			}
		}
	logdebug "keypadModeHandler GlobalFix:${globalFixMode} theMode: $theMode theStatus: $theStatus"

	if (globalKeypadControl)			//when we are controlling keypads, set lights
		{
		if (theMode=='Home' || theMode=='Away' || theMode=='Night' || theMode=='Stay')
			{
			def kMap=atomicState.kMap
			def kDtim=now()
			def kMode
			logdebug "keypadModeHandler KeypadControl entered theMode: ${theMode} AtomicState.kMap: ${kMap}"
			def setKeypadLights=true
			if (kMap)
				{
				kDtim=kMap.dtim
				kMode=kMap.mode
//				logdebug "keypadModeHandler ${evt} ${theMode} ${kMode}"
				if (theMode==kMode)
					{
					logdebug "Keypad lights are OK, no messages sent"
					setKeypadLights=false
					}
				}

//			Reset the keypad lights and mode, keep time when atomicState previously set, time is last time real keypad set mode		
			if (setKeypadLights)
				{
				kMap = [mode: theMode, dtim: kDtim]			//save mode dtim any keypad armed/disarmed the system for use with
				atomicState.kMap=kMap						//SHM Delay Child DoorOpens and MotionSensor active functions
				logdebug "keypadModeHandler issuing keypadlightHandler ${evt} ${evt.value}"
//				keypadLightHandler(evt)
				def fakeEvt = [value: theMode]					
				keypadLightHandler(fakeEvt)
				}
			}
		else
			{
			logdebug "keypadModeHandler mode $theMode cannot be used to set the keypad lights"
			}
		}	
		
//	When SHM alarm state does not match the requested SHM alarm state, change it
	if (theStatus)
		{
		def alarm = location.currentState("alarmSystemStatus")	//get ST alarm status
		def alarmstatus = alarm.value
		if (alarmstatus != theStatus)
			setSHM(theStatus)
		}	
	qsse_status_mode(theStatus,theMode)
	}

def keypadLightHandler(evt)						//set the Keypad lights
	{
	def	theMode=evt.value						//This should be a valid SHM Mode
	def simkeypad
	logdebug "keypadLightHandler entered ${evt} ${theMode} source: ${evt.source}"
	def simKeypadDevices=findAllChildAppsByName('SHM Delay Simkypd Child')
	simKeypadDevices.each
		{
		if (it?.getInstallationState()!='COMPLETE')
			{
			logdebug "${it.keypad} warning device not complete, please save the profile"
			}
		else
			{
			simkeypad=it.simkeypad		//get device 
			keypadLighton(evt,theMode,simkeypad)
			}
		}	
	globalKeypadDevices.each
		{ keypad ->
			keypadLighton(evt,theMode,keypad)
		}
	}

def	keypadLighton(evt,theMode,keypad)
	{
//	logdebug "keypadLighton entered $evt $theMode $keypad ${keypad?.getTypeName()}"
	def currkeypadmode=""
	if (theMode == 'Home')					//Alarm is off
		{keypad.setDisarmed()}
	else
	if (theMode == 'Stay')
		{
		keypad.setArmedStay()				//lights Partial light on Iris, Stay Icon on Xfinity/Centralite
//			deprecated on Apr 23, 2018		
//			if (evt.source !="keypad" && globalTrueNight && keypad?.getModelName()=="3400" && keypad?.getManufacturerName()=="CentraLite")
//				{keypad.setArmedNight()}
//			else	
//				{keypad.setArmedStay()}				//lights Partial light on Iris
//			deprecated on Apr 23, 2018 2.0.2a
//			if (keypad?.getModelName()=="3400" && keypad?.getManufacturerName()=="CentraLite" && currkeypadmode =="armedStay")
//				{}
//			else
//				{keypad.setArmedStay()}				//lights Partial light on Iris
		}
	else
	if (theMode == 'Night')					//Iris has no Night light set Partial on	
		{
//		if (keypad?.getModelName()=="3400" && keypad?.getManufacturerName()=="CentraLite" || 	Oct 10, 2018 v2.1.8
//		if (keypad?.getModelName()!="3405-L" || 	V2.2.4 Nov 30, 2018
//		if (keypad?.getModelName()=="3400" || 		v2.2.6 Jan 06, 2019
		if (['3400','3400-G'].contains(keypad?.getModelName()) ||
			keypad?.getTypeName()=="Internet Keypad")
			{
			if (evt.source=="keypad")
				{keypad.setArmedNight()}
			else
				{
				currkeypadmode = keypad?.currentValue("armMode")
				logdebug "keypadLightHandler LightRequest: ${theMode} model: ${keypad?.getModelName()} keypadmode: ${currkeypadmode}"
				if (currkeypadmode =="armedStay")
					{
//						logdebug "keypadLightHandler model: ${keypad?.getModelName()} keypadmode: ${currkeypadmode} no lights unchanged"
					}
				else
					{keypad.setArmedNight()}
				}
			}	
		else	
			{keypad.setArmedStay()}
		}	
	else
	if (theMode == 'Away')					//lights ON light on Iris
		{keypad.setArmedAway()}
	}

def keypadPanicHandler(evt)
	{
	if (!globalKeypadControl || globalDisable || !globalPanic)
		{return false}			//just in case
	def alarm = location.currentState("alarmSystemStatus")	//get ST alarm status
	def alarmstatus = alarm.value
	def keypad=evt.getDevice()		//set the keypad name
	def panic_map=[data:[cycles:5, keypad: keypad.name]]
	logdebug "the initial panic map ${panic_map} ${keypad.name}" 
	if (alarmstatus == "off")
		{
//		location.helloHome?.execute(globalAway)	//set alarm on deprecated Mar 20, 2018
		setSHM('away')							//set alarm on in the fastest possible way I know
		runIn(1, keypadPanicExecute,panic_map)
		}
	else
		{
		keypadPanicExecute(panic_map.data)		//Panic routine only uses the device name, should be ok
		}
	}	

def keypadPanicExecute(panic_map)						//Panic mode requested
/*	When system is armed: Open simulated sensor
**	When system is not armed: Wait for it to arm, open simulated sensor
**	Limit time to 5 cycles around 9 seconds of waiting maximum
*/		
	{
	def alarm = location.currentState("alarmSystemStatus")	//get ST alarm status
	def alarmstatus = alarm.value
	if (alarmstatus == "off")
		{
		logdebug "keypadPanicExecute entered $panic_map"
		if (panic_map.cycles > 1)
			{
			def cycles=panic_map.cycles-1
			def keypad=panic_map.keypad
			def newpanic_map=[data:[cycles: cycles, keypad: keypad]]
			runIn(2, keypadPanicExecute,newpanic_map)
			return false
			}
		}

//	prepare panic message, issued later		
	def message = "PANIC issued by $panic_map.keypad "
	if (global911 > ""  || globalPolice)
		{
		def msg_emergency
		if (global911 > "")
			{
			msg_emergency= ", call Police at ${global911}"
			}
		if (globalPolice)
			{
			if (msg_emergency==null)
				{
				msg_emergency= ", call Police at ${globalPolice}"
				}
			else
				{
				msg_emergency+= " or ${globalPolice}"
				}
			}
		message+=msg_emergency
		}
	else
		{
		message+=" by (SHM Delay App)"
		}
		
//	find a delay profile for use with panic
	logdebug "keypadPanicExecute searching for Delay profile"

	def childApps = getChildApps()		//gets all completed child apps
	def delayApp  = false	
	childApps.find 						//change from each to find to speed up the search
		{
		if (!delayApp && it.getName()=="SHM Delay Child")	
			{
			logdebug "keypadPanicExecute found Delay profile"
			delayApp=true		
			if (alarmstatus == "off")
				{
				message+=" System did not arm in 10 seconds, unable to issue Panic intrusion"
				it.doNotifications(message)		//issue messages as per child profile
				}
			else	
				{
				it.doNotifications(message)		//issue messages as per child profile
				it.thesimcontact.close()		//trigger an intrusion		
				it.thesimcontact.open()
				it.thesimcontact.close([delay: 4000])
				qsse_status_mode(false,"**Panic**")

				}
			return true							//this ends the **find** loop does not return to system
			}
		else	
			{return false}						//this continues the **find** loop does not return
		}
	if (!delayApp)
		{
		message +=' Unable to create instrusion, no delay profile found'
		sendNotificationEvent(message)				//log to notification we are toast
		}
	}

//	Directly set the SHM alarm status input must be off, away or stay	
def setSHM(state)
	{
	if (state=='off'|state=='away'||state=='stay')
		{
		def event = [name:"alarmSystemStatus", value: state, 
    		displayed: true, description: "System Status is ${state}"]
    	sendLocationEvent(event)
    	}
    }


/*
atempted to use this to trigger panic but it does not fire the subscribed event
and may create chaos when multiple keypad devices are defined
def panicContactOpen() {
	logdebug "Enter panicContactOpen $globalKeypadDevices"
    sendEvent(name: "contact", value: "open", displayed: true, isStateChange: true, Device: globalKeypadDevices)
    runIn(3, "panicContactClose")
}

def panicContactClose()
	{
	logdebug "Enter panicContactClose"
	sendEvent(name: "contact", value: "closed", displayed: true, isStateChange: true, Device: globalKeypadDevices)
	}
*/

//	Process response from async execution of WebCore Piston
def getResponseHandler(response, data)
	{
    if(response.getStatus() != 200)
    	sendNotificationEvent("SHM Delay Piston HTTP Error = ${response.getStatus()}")
	}

	
def verify_version(evt)		//evt needed to stop error whne coming from subscribe to alarm change
	{
	logdebug "Entered Verify Version. evt data ${evt.getProperties().toString()}"	
	def uri='https://www.arnb.org/shmdelay/'
//	uri+='?lat='+location.latitude					//Removed May 01, 2018 deemed obtrusive	
//	uri+='&lon='+location.longitude
	uri+='?hub='+location.hubs[0].encodeAsBase64()   //May have quotes and other stuff 
	uri+='&zip='+location.zipCode
	uri+='&cnty='+location.country
    uri+='&eui='+location.hubs[0].zigbeeEui
	def childApps = getChildApps()		//gets all completed child apps
	def vdelay=version()
	def vchild=''
	def vmodefix=''
	def vuser=''
	def vkpad=''
	def vtalk=''
	def vchildmindelay=9999
	def mf								//modefix module
	childApps.find 						//change from each to find to speed up the search
		{
//		logdebug "child ${it.getName()}"
//		if (vchild>'' && vmodefix>'' && vuser>''&& vkpad>''&& vtalk>'')		removed V2.1.9 Oct 16, 2018
//			return true														not getting minimum nonkeypad delay time
//		else
		if (it.getName()=="SHM Delay Child")	
			{
			if (vchild=='')
				vchild=it?.version()
			if (it?.theexitdelay < vchildmindelay)
				vchildmindelay=it.theexitdelay					//2.1.0 Oct 15, 2018 get delay profile exit delay time
			return false
			}	
		else
		if (it.getName()=="SHM Delay ModeFix")				//should only have 1 profile
			{
			mf=it											//save app for later 
			vmodefix=it?.version()
			return false
			}	
		else
		if (it.getName()=="SHM Delay Simkypd Child")			
			{
			if (vkpad=='')
				vkpad=it?.version()
			return false
			}	
		else
		if (it.getName()=="SHM Delay Talker Child")		
			{
			if (vtalk=='')
				vtalk=it?.version()
			return false
			}	
		else
		if (it.getName()=="SHM Delay User")	
			{
			if (vuser=='')
				vuser=it?.version()
			return false
			}	
		}
	uri+="&p=${vdelay}"
    uri+="&c=${vchild}"
    uri+="&m=${vmodefix}"
    uri+="&u=${vuser}"
    uri+="&k=${vkpad}"
    uri+="&t=${vtalk}"
    logdebug "${uri}"
    
	try {
		asynchttp_v1.get('versiongetResponseHandler', [uri: uri])
		}
	catch (e)
		{
		logdebug "Execution failed ${e}"
		}
	qsse_status_mode(evt.value,false)

//	Moved exitdelay non-keypad talk message to here from SHM Delay Child, V2.1.9 Oct 15, 2018
	def vaway=evt?.value
//	logdebug "Talker setup1 $vchildmindelay $vtalk $vaway" 
	
//	Nov 19, 2018 V2.2.2 User exit event not running in SHM Delay BuzzerSwitch
//	if (vtalk=='')			//talker profile not defined, return
//		return false

	logdebug "vchildmindelay: ${vchildmindelay}"
	if (vchildmindelay < 1)		//a nonkeypad time was set to 0
		return false;

	if (vchildmindelay == 9999)	//no non-keypad exit delay time?
		return false;
		
//	Nov 19, 2018 V2.2.3 Check Modefix data if State/Mode has an exit delay
	def daexitdelay=false

	def theMode = location.currentMode
	
	if (evt?.value == "stay" || evt?.value == "away")
		{
		if (vmodefix > '0.1.4')
			{
			def am="${evt?.value}Exit${theMode}"
			daexitdelay = mf."${am}"
			logdebug "Modefix Version ${vmodefix} the daeexitdelay is ${daexitdelay} amtext: ${am}"
			}
		else
		if (evt?.value == "away")
			daexitdelay=true
		}

	if (!daexitdelay)
		return false

	def locevent = [name:"shmdelaytalk", value: "exitDelayNkypd", isStateChange: true,
		displayed: true, descriptionText: "Issue exit delay talk event", linkText: "Issue exit delay talk event",
		data: vchildmindelay]

	logdebug "Talker setup2 $vchildmindelay $vtalk" 
	def alarm = location.currentState("alarmSystemStatus")
	def lastupdt = alarm?.date.time
	def alarmSecs = Math.round( lastupdt / 1000)

//	get current time in seconds
	def currT = now()
	def currSecs = Math.round(currT / 1000)	//round back to seconds
	def kSecs=0					//if defined in if statment it is lost after the if
	def kMap
	def kduration

	logdebug "Talker fields $kSecs $alarmSecs $vchildmindelay" 
	if (globalKeypadControl)
		{
		kMap=atomicState['kMap']	//no data returns null
		if (kMap>null)
			{
			kSecs = Math.round(kMap.dtim / 1000)
			kduration=alarmSecs - kSecs
//			logdebug "Talker fields $kSecs $alarmSecs $kduration $vchildmindelay" 
			if (kduration > 8)
				{
				sendLocationEvent(locevent)
//				logdebug "Away Talker from non keypad triggered"
				}
			}
		else	// no atomic map issue message		
			{
			sendLocationEvent(locevent)
			}
		}	
	else	
		{
		logdebug "sending location event nonkeypad arming"
		sendLocationEvent(locevent)
		}

	}	
	
//	Process response from async execution of version test to arnb.org
def versiongetResponseHandler(response, data)
	{
    if(response.getStatus() == 200)
    	{
		def results = response.getJson()
		logdebug "SHM Delay good response ${results.msg}"
		if (results.msg != 'OK')
    		sendNotificationEvent("${results.msg}")
        }
    else
    	sendNotificationEvent("SHM Delay Version Check, HTTP Error = ${response.getStatus()}")
    }


def	process_routine(it, modeEntered, keypad)
	{
//	the initial msg in rmap is the default error message
	def rmap = [err: "Process Routine " + keypad.displayName + " unknown keypad mode:" + modeEntered + " with pin for " + it.theusername]
//	modeEntered: off(0), stay(1), night(2), away(3)
	if (it?.thepinroutine)
		{
		rmap=fire_routine(it, modeEntered, keypad, it.thepinroutine[0], "All")
		}
	else 
	if (modeEntered == 0 && it?.thepinroutineOff)	
		{
		rmap=fire_routine(it, modeEntered, keypad, it.thepinroutineOff[0], "Off")
		}
	else 
	if (modeEntered == 3 && it?.thepinroutineAway)	
		{
		rmap=fire_routine(it, modeEntered, keypad, it.thepinroutineAway[0], "Away")
		}
	else 
	if ((modeEntered == 1 || modeEntered == 2) && it?.thepinroutineStay)	
		{
		rmap=fire_routine(it, modeEntered, keypad, it.thepinroutineStay[0], "Stay")
		}
	else
	if (it.pinuseage == "UserRoutinePiston" && modeEntered > -1 && modeEntered < 4 )	//nothing to process
		{
		rmap = [err: "nodata"]
		}
	return rmap		//return with an err or info map message
	}

def fire_routine(it, modeEntered, keypad, theroutine, textmode)
	{
	def rmsg = keypad.displayName + " Mode:" + textmode + " executed routine " + theroutine + " with pin for " + it.theusername
	def result
	location.helloHome?.execute(theroutine)
	if (it.thepinusage == "Routine")
		result = [err: rmsg]
	else
		result = [info: rmsg]
	return result
	}	

def process_piston(it, modeEntered, keypad)
	{	
	def rmap = [err: "Process Piston " + keypad.displayName + " unknown keypad mode:" + modeEntered + " with pin for " + it.theusername]
//	modeEntered: off(0), stay(1), night(2), away(3)
	if (it.thepinpiston)
		{
		rmap=fire_piston(it, modeEntered, keypad, it.thepinpiston, "All")
		}
	else 
	if (modeEntered == 0 && it.thepinpistonOff)	
		{
		rmap=fire_piston(it, modeEntered, keypad, it.thepinpistonOff, "Off")
		}
	else 
	if (modeEntered == 3 && it.thepinpistonAway)	
		{
		rmap=fire_piston(it, modeEntered, keypad, it.thepinpistonAway, "Away")
		}
	else 
	if ((modeEntered == 1 || modeEntered == 2) && it.thepinpistonStay)	
		{
		rmap=fire_piston(it, modeEntered, keypad, it.thepinpistonStay, "Stay")
		}
	else
	if (it.pinuseage == "UserRoutinePiston" && modeEntered > -1 && modeEntered < 4 )	//nothing to process
		{
		rmap = [err: "nodata"]
		}
	return rmap		//return with an err or info map message
	}
	
def fire_piston(it, modeEntered, keypad, thepiston, textmode)
	{
	def rmsg = keypad.displayName + " Mode:" + textmode + " executed piston with pin for " + it.theusername
	def result
	try {
		def params = [uri: thepiston]
//		def params = [uri: "https://www.google.com"]		//use to test
		asynchttp_v1.get('getResponseHandler', params)
		}
	catch (e)
		{
		rmsg = rmsg + " Piston Failed: " + e
		}    					
	if (it.thepinusage == "Piston")
		result = [err: rmsg]
	else
		result = [info: rmsg]
	return result

	}
	
// log, send notification, SMS message for pin entry, base code from SHM Delay Child	
def doPinNotifications(localmsg, it)
	{
//	logdebug "doPinNotifications entered ${localmsg} ${it}"
	if (it?.pinMsgOverride)
		{
//		logdebug "Pin msg override being used"

		if (it.UserPinLog)
			{
//			logdebug "sent to system log"
			sendNotificationEvent(localmsg)
			}
		if (location.contactBookEnabled && it.UserPinRecipients)
			{
//			logdebug "sent to contact folks"
			sendNotificationToContacts(localmsg, it.UserPinRecipients, [event: false])
			}
		if (it.UserPinPush)
			{
			sendPushMessage(localmsg)
			}
		if (it.UserPinPhone)
			{
			def phones = it.UserPinPhone.split("[;#]")
//			logdebug "$phones"
			for (def i = 0; i < phones.size(); i++)
				{
				sendSmsMessage(phones[i], localmsg)
				}
			}
		}
	else
	if (globalPinMsgs)	
		{
//		logdebug "global Pin msg settings being used"

		if (globalPinLog)
			{
//			logdebug "log to notification log"
			sendNotificationEvent(localmsg)
			}
		if (location.contactBookEnabled && globalPinRecipients)
			{
//			logdebug "global contacts being used"
			sendNotificationToContacts(localmsg, globalPinRecipients, [event: false])
			}
		if (globalPinPush)
			{
			sendPushMessage(localmsg)
			}
		if (globalPinPhone)
			{
			def phones = globalPinPhone.split("[;#]")
	//		logdebug "$phones"
			for (def i = 0; i < phones.size(); i++)
				{
				sendSmsMessage(phones[i], localmsg)
				}
			}
		}
	else
	if (globalPinMsgs && globalPinMsgs==false)	
		{}
	else
		{
//		logdebug "default pin msg logic used, log to notifications"
		sendNotificationEvent(localmsg)		//log to notification when no settings available
		}
	}
	
def doBadPinNotifications(localmsg, it)
	{
//	logdebug "doBadPinNotifications entered ${localmsg} ${it}"
	if (globalBadPinLog)
		{
		sendNotificationEvent(localmsg)
		}
	if (location.contactBookEnabled && globalBadPinRecipients)
		{
		sendNotificationToContacts(localmsg, globalBadPinRecipients, [event: false])
		}
	if (globalBadPinPush)
		{
		sendPushMessage(localmsg)
		}
	if (globalBadPinPhone)
		{
		def phones = globalBadPinPhone.split("[;#]")
		for (def i = 0; i < phones.size(); i++)
			{
			sendSmsMessage(phones[i], localmsg)
			}
		}
	}

def checkOpenContacts (contactList, notifyOptions, keypad)
	{
	def contactmsg=''
//	logdebug "contact list entered $contactList $notifyOptions $keypad"
	contactList.each
		{
//		logdebug "${it} ${it.currentContact}"
		if (it.currentContact=="open")
			{
			if (contactmsg == '')
				{
				if (!globalRboyDth)		//Nov 3, 2018 rBoy DTH already issues the acknowledgement
					keypad.sendInvalidKeycodeResponse()
				contactmsg = 'Arming cancelled. Close '+it.displayName
				}
			else
				contactmsg += ', '+it.displayName
			}
		}
	if (contactmsg>'')
		{
		notifyOptions.each
			{
//			logdebug "$it"
			if (it=='Notification log')
				{
				sendNotificationEvent(contactmsg)
				}
			else
			if (it=='Push Msg')
				{sendPushMessage(contactmsg)}
			else
			if (it=='SMS' && globalPinPhone)
				{
				def phones = globalPinPhone.split("[;#]")
				for (def i = 0; i < phones.size(); i++)
					{
					sendSmsMessage(phones[i], contactmsg)
					}
				}
			else
			if (it=='Talk')
				{
				def loceventcan = [name:"shmdelaytalk", value: "ArmCancel", isStateChange: true,
					displayed: true, descriptionText: "Issue exit delay talk event", linkText: "Issue exit delay talk event",
					data: contactmsg]	
				sendLocationEvent(loceventcan)
				}
			}
		return false
		}
	return true	
	}

def logdebug(txt)
	{
   	if (logDebugs)
   		log.debug ("${txt}")
    }