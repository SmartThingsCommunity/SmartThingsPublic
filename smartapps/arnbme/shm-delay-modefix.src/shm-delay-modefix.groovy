/*
 *  SHM Delay ModeFix 
 *  Functions: Fix the mode when it is invalid, generally caused when using Dashboard to switch modes
 * 
 *  Copyright 2017 Arn Burkhoff
 * 
 *  Changes to Apache License
 *	4. Redistribution. Add paragraph 4e.
 *	4e. This software is free for Private Use. All derivatives and copies of this software must be free of any charges,
 *	 	and cannot be used for commercial purposes.
 *
 *  Licensed under the Apache License with changes noted above, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	May 14, 2019 	V0.1.8  add support for Xfinity branded UEI keypad
 *	Mar 05, 2019 	V0.1.7  Added: Boolean flag for debug message logging, default false
 *
 *	Jan 06, 2019 	V0.1.6  Added: Support for 3400_G Centralite V3
 *
 * 	Oct 17, 2018	v0.1.5	Allow user to set if entry and exit delays occur for a state/mode combination
 *								
 * 	Apr 24, 2018	v0.1.4	For Xfinity and Centralite model 3400 keypad on armed (Home) modes 
 *								add device icon button to light Stay (Entry Delay) or Night (Instant Intrusion)
 *								
 * 	Mar 11, 2018    v0.1.3  add logging to notifications when mode is changed. 
 *								App issued changes are not showing in PhoneApp notifications
 *								Assumed system would log this but it does not
 * 	Sep 23, 2017    v0.1.2  Ignore alarm changes caused by True Entry Delay in SHM Delay Child
 * 	Sep 05, 2017    v0.1.1  minor code change to allow this module to run stand alone
 * 	Sep 02, 2017    v0.1.0  add code to fix bad alarmstate set by unmodified Keypad module
 * 	Sep 02, 2017    v0.1.0  Repackage logic that was in parent into this module for better reliability
 *					and control
 * 	Aug 26/27, 2017 v0.0.0  Create 
 *
 */

definition(
    name: "SHM Delay ModeFix",
    namespace: "arnbme",
    author: "Arn Burkhoff",
    description: "(${version()}) Fix the ST Mode and or Alarm State when using ST Dashboard to change AlarmState or Mode",
    category: "My Apps",
	parent: "arnbme:SHM Delay",
    iconUrl: "https://www.arnb.org/IMAGES/hourglass.png",
    iconX2Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    iconX3Url: "https://www.arnb.org/IMAGES/hourglass@2x.png")

preferences {
	page(name: "pageOne", nextPage: "pageOneVerify")
	page(name: "pageOneVerify")
	page(name: "pageTwo")
	page(name: "aboutPage", nextPage: "pageOne")
}

def version()
	{
	return "0.1.8";
	}

def pageOne(error_msg)
	{
	dynamicPage(name: "pageOne", title: "For each alarm state, set valid modes and default modes.", install: false, uninstall: true)
		{
		section
			{
			if (error_msg instanceof String )
				{
				paragraph error_msg
				}
			else
				paragraph "Caution! Wrong settings may create havoc. If you don't fully understand Alarm States and Modes, read the Introduction and use the defaults!"
			href(name: "href",
			title: "Introduction",
			required: false,
			page: "aboutPage")
			}
		section ("Debugging messages")
			{
			input "logDebugs", "bool", required: false, defaultValue:false,
				title: "Log debugging messages? Normally off/false"
			}
		section ("Alarm State: Disarmed / Off")
			{
			input "offModes", "mode", required: true, multiple: true, defaultValue: "Home",
				title: "Valid Modes for: Disarmed"
			input "offDefault", "mode", required: true, defaultValue: "Home",
				title: "Default Mode for: Disarmed"
			}	
		section ("Alarm State: Armed (Away)")
			{
			if (away_error_data instanceof String )
				{
				paragraph away_error_data
				}
			input "awayModes", "mode", required: true, multiple: true, defaultValue: "Away", submitOnChange: true,
				title: "Valid modes for: Armed Away"
			input "awayDefault", "mode", required: true, defaultValue: "Away",
				title: "Default Mode: Armed Away"
			awayModes.each
				{
				input "awayExit${it.value}", "bool", required: true, defaultValue: true,
					title: "Create Exit Delay for Armed (Away) ${it.value} mode"
				input "awayEntry${it.value}", "bool", required: true, defaultValue: true,
					title: "Create Entry Delay for Armed (Away) ${it.value} mode"
				}	
			}	
		section ("Alarm State: Armed (Home) aka Stay or Night")
			{
			input "stayModes", "mode", required: true, multiple: true, defaultValue: "Night", submitOnChange: true,
				title: "Valid Modes for Armed Home"
			input "stayDefault", "mode", required: true, defaultValue: "Night",
				title: "Default Mode for Armed Home"
			stayModes.each
				{
				input "stayExit${it.value}", "bool", required: true, defaultValue: false,
					title: "Create Exit Delay for Armed (Home) ${it.value} mode"
				if (it.value =='Stay')
					input "stayEntry${it.value}", "bool", required: true, defaultValue: true,
						title: "Create Entry Delay for Armed (Home) ${it.value} mode"
				else
					input "stayEntry${it.value}", "bool", required: true, defaultValue: false,
						title: "Create Entry Delay for Armed (Home) ${it.value} mode"
				}	
			}	
		if (parent.globalKeypadControl)
			{
			def showLights=false;
			parent.globalKeypadDevices.each
				{ keypad ->
				logdebug "modefix ${keypad?.getModelName()} ${keypad?.getManufacturerName()}"
//				if (keypad?.getModelName()=="3400" && keypad?.getManufacturerName()=="CentraLite")	//Iris = 3405-L
				if (['3400','3400-G','URC4450BC0-X-R'].contains(keypad?.getModelName()))	
					{showLights=true}
				}					
			if (showLights)
				{
				section ("A model 3400 or UEI Keypad is defined\nSet the keypad Light Icon and smartapp action: Stay (Entry Delay) or Night (Instant Intrusion) when setting Armed (Night) from non-keypad source")
					{
					stayModes.each
						{
						input "stayLight${it.value}", "enum", options: ["Night", "Stay"], required: true, defaultValue: "Night",
							title: "${it.value} Mode"
						}
					}
				}	
			}
		section
			{
			paragraph "SHM Delay Modefix ${version()}"
			}

		}	
	}	

def pageOneVerify() 				//edit page One
	{

//	Verify disarm/off data
	def off_error="Disarmed / Off Default Mode not defined in Valid Modes"
	def children = offModes
	children.each
		{ child ->
		if (offDefault == child)
			{
			off_error=null
			}
		}
	
//	Verify Away data
	def away_error="Armed (Away) Default Mode not defined in Valid Modes"
	children = awayModes
	children.each
		{ child ->
		if (awayDefault == child)
			{
			away_error=null
			}
		}

//	Verify Stay data
	def stay_error="Armed (Home) Default Mode not defined in Valid Modes"
	children = stayModes
	children.each
		{ child ->
		if (stayDefault == child)
			{
			stay_error=null
			}
		}

	if (off_error == null && away_error == null && stay_error == null)
		{
		pageTwo()
		}
	else	
		{
		def error_msg=""
		def newline=""
		if (off_error>"")
			{
			error_msg=off_error
			newline="\n"
			}
		if (away_error >"")
			{
			error_msg+=newline + away_error
			newline="\n"
			}	
		if (stay_error >"")
			{
			error_msg+=newline + stay_error
			newline="\n"
			}
		pageOne(error_msg)
		}
	}

def pageTwo()
	{
	dynamicPage(name: "pageTwo", title: "Mode settings verified, press 'Done/Save' to install, press '<' to change, ", install: true, uninstall: true)
		{
/*		section
			{
			href(name: "href",
			title: "Introduction",
			required: false,
			page: "aboutPage")
			}
*/		section ("Alarm State: Disarmed / Off")
			{
			input "offModes", "mode", required: true, multiple: true, defaultValue: "Home",
				title: "Valid Modes for: Disarmed"
			input "offDefault", "mode", required: true, defaultValue: "Home",
				title: "Default Mode for: Disarmed"
			}	
		section ("Alarm State: Armed (Away)")
			{
			input "awayModes", "mode", required: true, multiple: true, defaultValue: "Away", submitOnChange: true,
				title: "Valid modes for: Armed Away"
			input "awayDefault", "mode", required: true, defaultValue: "Away",
				title: "Default Mode: Armed Away"
			awayModes.each
				{
				input "awayExit${it.value}", "bool", required: true, defaultValue: true,
					title: "Create Exit Delay for Armed (Away) ${it.value} mode"
				input "awayEntry${it.value}", "bool", required: true, defaultValue: true,
					title: "Create Entry Delay for Armed (Away) ${it.value} mode"
				}	
			}	
		section ("Alarm State: Armed (Home) aka Stay or Night")
			{
			input "stayModes", "mode", required: true, multiple: true, defaultValue: "Night",  submitOnChange: true,
				title: "Valid Modes for Armed Home"
			input "stayDefault", "mode", required: true, defaultValue: "Night",
				title: "Default Mode for Armed Home"
			stayModes.each
				{
				input "stayExit${it.value}", "bool", required: true, defaultValue: false,
					title: "Create Exit Delay for Armed (Home) ${it.value} mode"
				input "stayEntry${it.value}", "bool", required: true, defaultValue: false,
					title: "Create Entry Delay for Armed (Home) ${it.value} mode"
				}	
				
			}	
		if (parent.globalKeypadControl)
			{
			def showLights=false;
			parent.globalKeypadDevices.each
				{ keypad ->
				logdebug "modefix ${keypad?.getModelName()} ${keypad?.getManufacturerName()}"
//				if (keypad?.getModelName()=="3400" && keypad?.getManufacturerName()=="CentraLite")	//Iris = 3405-L
				if (['3400','3400-G','URC4450BC0-X-R'].contains(keypad?.getModelName()))	
					{showLights=true}
				}					
			if (showLights)
				{
				section ("A model 3400 or UEI Keypad is defined\nSet the Light Icon and smartapp action: Stay (Entry Delay) or Night (Instant Intrusion) when setting Armed (Night) from non-keypad source")
					{
					stayModes.each
						{
						input "stayLight${it.value}", "enum", options: ["Night", "Stay"], required: true, defaultValue: "Night",
							title: "${it.value} Mode"
						}
					}
				}	
			}
		section
			{
			paragraph "SHM Delay Modefix ${version()}"
			}
		}
	}	

	
def aboutPage()
	{
	dynamicPage(name: "aboutPage", title: "Introduction")
		{
		section 
			{
			paragraph "Have you ever wondered why Mode restricted Routines, SmartApps, and Pistons sometimes fail to execute, or execute when they should not?\n\n"+
			"Perhaps you conflated AlarmState and Mode, however they are separate and independent SmartThings settings, "+
			"and when Alarm State is changed using the SmartThings Dashboard Home Solutions---surprise, Mode does not change!\n\n" +
			"SmartHome routines generally, but not always, have a defined SystemAlarm and Mode settings. "+
			"Experienced SmartThings users seem to favor changing the AlarmState using SmartHome routines, avoiding use of the Dashboard's Home Solutions\n\n"+
			"If like me, you can't keep track of all this, or utilize the Dashboard to change the AlarmState, this app may be helpful.\n\n"+
			"For each AlarmState, set the Valid Mode states, and a Default Mode. This SmartApp attempts to correctly set the Mode by monitoring AlarmState for changes. When the current Mode is not defined as a Valid Mode for the AlarmState, the app sets Mode to the AlarmState's Default Mode\n\n"+
			"Please Note: This app does not, directly or (knowingly) indirectly, execute a SmartHome Routine"  
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
	subscribe(location, "alarmSystemStatus", alarmStatusHandler)
	}

def alarmStatusHandler(evt)
	{
/*	some entries to this function are direct from ST Events
	others are from SHM Delay Child repackaged evt object 
	which passes a childid property stoppings multiple notifications from sending
*/	
	def theAlarm = evt.value		//off, stay, or away Alarm Mode set by event value
	def fromST=true					//event is assumed from Smarthings subscribe till proven otherwise
	try
		{
		if (evt.childid)
			fromST=false
		}
	catch (e)
		{}
//	logdebug "alarm status entry ${fromST} ${theAlarm} ${evt.source}"
	if (theAlarm == "night")	//bad AlarmState set by unmodified Keypad module
		{
  		def event = [
  		      name:'alarmSystemStatus',
  		      value: "stay",
  		      displayed: true,
  		      description: "SHM Delay Fix System Status from night to stay"]
    		sendLocationEvent(event)	//change alarmstate to stay	
		setLocationMode("Night")	//set the mode
//		sendNotificationEvent("Change the Lock Manager Keypad module to version in github ARNBME lock-master SHMDelay")
		log.warn "Change the Lock Manager Keypad module to version in github ARNBME lock-master SHMDelay ModeFix"
		return "Night"
		}
	if (parent && !parent.globalFixMode)
		{return false}
	def theMode = location.currentMode
	def oldMode = theMode
	def delaydata=evt?.data
	if (delaydata==null)
		{}
	else	
	if (delaydata.startsWith("shmtruedelay"))	//ignore SHM Delay Child "true entry delay" alarm state changes
		{
		logdebug "Modefix ignoring True Entry Delay event, alarm state ${theAlarm}"
		return false}
	logdebug "ModeFix alarmStatusHandler entered alarm status change: ${theAlarm} Mode: ${theMode} "
//	Fix the mode to match the Alarm State. When user sets alarm from dashboard
//	the Mode is not set, resulting in Smarthings having Schizophrenia or cognitive dissonance. 
	def modeOK=false
	if (theAlarm=="off")
		{
		offModes.each
			{ child ->
			if (theMode == child)
				{modeOK=true}
			}
		if (!modeOK)
			{
			if (fromST)
				setLocationMode(offDefault)
			theMode=offDefault
			}
		}
	else
	if (theAlarm=="stay")
		{
		stayModes.each
			{ child ->
			if (theMode == child)
				{modeOK=true}
			}
		if (!modeOK)
			{
			if (fromST)
				setLocationMode(stayDefault)
			theMode=stayDefault
			}
		}
	else
	if (theAlarm=="away")
		{
		awayModes.each
			{ child ->
			if (theMode == child)
				{modeOK=true}
			}
		if (!modeOK)
			{
			if (fromST)			
				setLocationMode(awayDefault)
			theMode=awayDefault
			}
		}
	else{
		log.error "ModeFix alarmStatusHandler Unknown alarm mode: ${theAlarm} in "}
	if (theMode != oldMode)
		{
		if (fromST)	
			sendNotificationEvent("Modefix: Mode changed to ${theMode}. Cause: ${evt.source} set alarm to ${theAlarm}")
		logdebug("ModeFix alarmStatusHandler Mode was changed From:$oldMode To:$theMode")
		}
	return theMode
	}
def logdebug(txt)
	{
   	if (logDebugs)
   		log.debug ("${txt}")
    }