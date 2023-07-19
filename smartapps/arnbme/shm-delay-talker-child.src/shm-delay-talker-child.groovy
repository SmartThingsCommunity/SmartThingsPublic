/**
 *  SHM Delay Talker Child
 *  Supplements Big Talker adding speech when SHMDelay enters the Exit or Entry delay time period
 *		For LanNouncer Device: Chime, TTS text, Chime
 *		For speakers (such as Sonos)  TTS text
 *	Supports TTS devices and speakers
 *	When devices use differant messages, install multiple copies of this code
 *	When speakers need different volumes, install multiple copies of this code
 *
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
 * 	Dec 17, 2018 v1.0.4 Change speaker capability audioNotification to musicPlayer. Did not select Sonos speakers
 * 	Nov 04, 2018 v1.0.3 Add support for generic quiet time per user request on messages
 *						Delayed messages are super delayed by unknown cloud processing error, allow for no chime and instant speak
 * 	Oct 21, 2018 v1.0.2	Support Arming Canceled messages from SHM Delay 
 * 	Jul 05, 2018 v1.0.1	correct non standard icon 
 * 	Jul 04, 2018 v1.0.1	Check for non Lannouner TTS devices and when true eliminate chime command 
 *	Jun 26, 2018 V1.0.0 Create from standalone module Keypad ExitDelay Talker
 */
definition(
    name: "SHM Delay Talker Child",
    namespace: "arnbme",
    author: "Arn Burkhoff",
    description: "(${version()}) Speak during SHM Delay Exit and Entry Delay",
    category: "My Apps",
    parent: "arnbme:SHM Delay",
    iconUrl: "https://www.arnb.org/IMAGES/hourglass.png",
    iconX2Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    iconX3Url: "https://www.arnb.org/IMAGES/hourglass@2x.png")

def version()
	{
	return "1.0.4";
	}

preferences {
	page(name: "pageZeroVerify")
	page(name: "pageZero", nextPage: "pageZeroVerify")
	page(name: "pageOne", nextPage: "pageOneVerify")
	page(name: "pageOneVerify")
	page(name: "pageTwo")		//recap page when everything is valid. No changes allowed.
	}

def pageZeroVerify()
//	Verify this is installed as a child
	{
	if (parent && parent.getInstallationState()=='COMPLETE')
		{
		pageOne()
		}
	else
		{
		pageZero()
		}
	}	

def pageZero()
	{
	dynamicPage(name: "pageZero", title: "This App cannot be installed", uninstall: true, install:false)
		{
		section
			{
			paragraph "This SmartApp, SHM Delay Talker, must be used as a child app of SHM Delay."
			}
		}
	}	

def pageOne()
	{
	dynamicPage(name: "pageOne", title: "Talker Messages and Devices", install: false, uninstall: true)
		{
		section("The SHM Delay Message Settings")
			{
			if (state.error_data)
				{
				paragraph "${state.error_data}"
				state.remove("error_data")
				}
			paragraph "%nn in any message is replaced with the respective delay seconds"
			input "theExitMsgKypd", "string", required: true, title: "Keypad initiated Exit message", 
				defaultValue: "Smart Home Monitor is arming in %nn seconds. Please exit the facility"
			input "theExitMsgNkypd", "string", required: true, title: "Non-Keypad initiated Exit message", 
				defaultValue: "You have %nn seconds to exit the facility"
			if (parent?.globalKeypadControl)
				{
				input "theEntryMsg", "string", required: false, title: "Entry message", 
					defaultValue: "Please enter your pin on the keypad"
				}
			else
				{
				input "theEntryMsg", "string", required: false, title: "Entry message", 
					defaultValue: "Please disarm Smart Home Monitor"
				}
			input(name: 'theStartTime', type: 'time', title: 'Do not talk: Start Time', required: false)
			input(name: 'theEndTime', type: 'time', title: 'Do not talk: End Time', required: false)
			input "theSoundChimes", "bool", defaultValue: true, required: false,
				title: "Sound TTS Chimes with messages when using LanNouncer. If Cloud is slow and message delayed set false. Default: On/True"
			input "theTTS", "capability.speechSynthesis", required: false, multiple: true, submitOnChange: true,
				title: "LanNouncer/DLNA TTS Devices"
			input "theSpeakers", "capability.musicPlayer", required: false, multiple: true, submitOnChange: true,
				title: "Speaker Devices?"
			input "theVolume", "number", required: true, range: "1..100", defaultValue: 40,
				title: "Speaker Volume Level from 1 to 100"
			}

//		Generate a unique Profile
		if (app?.getLabel())
			{
			section([mobileOnly:true]) 
				{
				label title: "Profile name", defaultValue: app.getLabel(), required: true
				}
			}	

		else
			{
			def namematch=true
			def cid=0
			def talkers
			def appLabel
			while (namematch)
				{
				namematch=false
				cid=cid+1
				appLabel="Profile: Talk: ${cid}"
//				log.debug "applabel: ${appLabel}"
				talkers = parent.findAllChildAppsByName("SHM Delay Talker Child")
				talkers.each
					{
//					log.debug "child label: ${it?.getLabel()} ${it?.getInstallationState()}"
					if (it.getInstallationState() == 'COMPLETE' && it.getLabel() == appLabel)
						namematch=true
//					else
//						log.debug "no match ${it.getLabel()} $appLabel"  
					}
				}	
			section([mobileOnly:true]) 
				{
				label title: "Profile name", defaultValue: appLabel, required: true
				}
			}	
		}
	}	

def pageOneVerify() 				//edit page one info, go to pageTwo when valid
	{
	def error_data = ""
	if (theStartTime>"" && theEndTime>"")
		{}
	else
	if (theStartTime>"")
		error_data="Please set do not talk end time or clear do not talk start time"
	else	
	if (theEndTime>"")
		error_data="Please set do not talk start time or clear do not talk end time"

	if (error_data!="")
		{
		state.error_data=error_data.trim()
		pageOne()
		}
	else
		{
		pageTwo()
		}
	}	

//	This page summarizes the data prior to save	
def pageTwo(error_data)
	{
	dynamicPage(name: "pageTwo", title: "Verify settings then tap Save, or tap < (back) to change settings", install: true, uninstall: true)
		{
		def chimes=true
		def chimetxt='(Chime) '
		try 
			{chimes=theSoundChimes}
		catch(Exception e)
			{}
		if (!chimes)
			chimetxt=''
		section
			{
			if (theExitMsgKypd)
				paragraph "The Keypad Exit Delay Message:\n${chimetxt}${theExitMsgKypd} ${chimetxt}"
			else	
				paragraph "The Keypad Exit Delay Message is not defined"
			if (theExitMsgNkypd)
				paragraph "The Non-Keypad Exit Delay Message:\n${chimetxt}${theExitMsgNkypd} ${chimetxt}"
			else	
				paragraph "The Non-Keypad Exit Delay Message is not defined"
			if (theEntryMsg)
				paragraph "The Entry Delay Message:\n${theEntryMsg}"
			else	
				paragraph "The Entry Delay Message is not defined"
			if (theStartTime>"" && theEndTime>"")
				paragraph "Quiet time active from ${theStartTime.substring(11,16)} to ${theEndTime.substring(11,16)}"	
			else
				paragraph "Quiet time is inactive"

			if (!chimes)
				paragraph "Chimes do not sound with messages"	
			if (theTTS)
				paragraph "The Text To Speech Devices are ${theTTS}"
			else	
				paragraph "No Text To Speech Devices are defined"
			if (theSpeakers)
				{
				paragraph "The Text To Speech Devices are ${theSpeakers}"
				paragraph "The Speaker Volume Level is ${theVolume}"
				}
			else	
				paragraph "No Speaker Devices are defined"
			paragraph "${app.getLabel()}\nModule SHM Delay User ${version()}"
			}	
		}
	}	

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(location, "shmdelaytalk", TalkerHandler)
	}

def TalkerHandler(evt)
	{
	log.debug("TalkerHandler entered, event: ${evt.value} ${evt?.data}")
	def delaydata=evt?.data			//get the delay time 
	def msgout
	def nonnouncer=false
	def chimes=true

//	1.0.3 Nov 4, 2018 check time values for quiet
	if (theStartTime>"" && theEndTime>"")
		{
		def between = timeOfDayIsBetween(theStartTime.substring(11,16), theEndTime.substring(11,16), new Date(), location.timeZone)
		if (between)
			{
//			log.debug ("it is quiet time")	
			return false
			}
		}
//	log.debug ("not quiet time")	

//	1.0.3 Nov 4, 2018 Set Chimes sound
	try 
		{chimes=theSoundChimes}
	catch(Exception e)
		{}
//	if (chimes)
//		log.debug "chime on"
//	else
//		log.debug "chime off"	

	if (theTTS)
		{
		theTTS.find
			{
			if (it.typeName != 'LANnouncer Alerter')
				{
				nonnouncer=true		
				return true		//stop searching
				}
			else
				return false
			}
		}	


	if (evt.value=="entryDelay" && theEntryMsg>"")
		{
		if (delaydata>"")
			msgout=theEntryMsg.replaceAll("%nn",delaydata)
		else
			msgout=theEntryMsg
		if (theTTS)
			{
			if (nonnouncer || chimes==false)
				{theTTS.speak(msgout)}
			else		
				{
				theTTS.speak("@|ALARM=CHIME")
				theTTS.speak(msgout,[delay: 1800])
				theTTS.speak("@|ALARM=CHIME", [delay: 5000])
				}
			}	
		if (theSpeakers)
			{
			theSpeakers.playTextAndResume(msgout,theVolume)
			}
		}
	else
	if (evt.value=="exitDelay" && theExitMsgKypd>"")
		{
		if (delaydata>"")
			msgout=theExitMsgKypd.replaceAll("%nn",delaydata)
		else
			msgout=theExitMsgKypd
		if (theTTS)
			{
			if (nonnouncer || chimes==false)
				{theTTS.speak(msgout)}
			else		
				{
				theTTS.speak("@|ALARM=CHIME")
				theTTS.speak(msgout,[delay: 1800])
				theTTS.speak("@|ALARM=CHIME", [delay: 8000])
				}
			}
		if (theSpeakers)
			{
			theSpeakers.playTextAndResume(msgout,theVolume)
			}
		}
	else
	if (evt.value=="exitDelayNkypd" && theExitMsgNkypd>"")
		{
		if (delaydata>"")
			msgout=theExitMsgNkypd.replaceAll("%nn",delaydata)
		else
			msgout=theExitMsgNkypd
		if (theTTS)
			{
			if (nonnouncer || chimes==false)
				theTTS.speak(msgout, [delay: 2000])					//allows Bigtalker to speak armed in away mode msg
			else		
				{
				theTTS.speak("@|ALARM=CHIME", [delay : 2000])		//allows BigTalker to speak armed in away mode msg
				theTTS.speak(msgout, [delay: 3800])
				theTTS.speak("@|ALARM=CHIME", [delay: 8000])
				}
			}
		if (theSpeakers)
			{
			theSpeakers.playTextAndResume(msgout,theVolume)
			}
		}
	if (evt.value=="ArmCancel" && delaydata>"")
		{
		if (theTTS)
			{theTTS.speak(delaydata)}					
		if (theSpeakers)
			{theSpeakers.playTextAndResume(delaydata,theVolume)}
		}
	}