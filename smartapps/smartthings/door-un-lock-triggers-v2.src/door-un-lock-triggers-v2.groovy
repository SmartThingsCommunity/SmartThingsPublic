/**
 *  Door Lock Triggers
 *
 *  09-Feb-2015: initial
 *
 *  Copyright 2015 Gary D
 *
 *  Licensed under the Apache License, Version 2.0 WITH EXCEPTIONS; you may not use this file except
 *  in compliance with the License AND Exceptions. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *  Exceptions are:
 *      1.  This code may NOT be used without freely distributing the code freely and without limitation, 
 *          in source form.  The distribution may be met with a link to source code,
 *      2.  This code may NOT be used, directly or indirectly, for the purpose of any type of monetary
 *          gain.   This code may not be used in a larger entity which is being sold, leased, or 
 *          anything other than freely given.
 *      3.  To clarify 1 and 2 above, if you use this code, it must for your own personal use, or be a 
 *          free project, and available to anyone with "no strings attached."  (You may require a free
 *          registration on a free website or portal in order to distribute the modifications.)
 *      4.  The above listed exceptions to this code do not apply to "SmartThings, Inc."  SmartThings,
 *          is granted a license to use this code under the terms of the Apache License (version 2) with
 *          no further exception.
 *
 */
definition(
	name: "Door (Un)Lock Triggers (v2)",
	namespace: "smartthings",
	author: "Gary D",
	description: "Triggers switches, door controls, etc based on door control lock events",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences 
{
	// preferences have to be seperate function in order to get the list of hellohome phrases.
	page(name: "page1prefs", nextPage: "page2prefs")
    page(name: "page2prefs")
}

def page1prefs()
{
	dynamicPage(name: "page1prefs", uninstall: true, install: false) 
	{

		def phrases = location.helloHome?.getPhrases()*.label
		if (phrases) 
		{
			phrases.sort()
		}
		
		section("When this door lock locks or unlocks...") 
		{
			input "lock1", "capability.lock", title: "Which Door Lock?", required: true
            input "cmd_lock", "enum", title: "When it Locks or Unlocks?", multiple: false, required: true, metadata:[values:["Lock", "Unlock"]]
			input "lockCode", "enum", title: "Only via these User Codes or Methods (optional)", multiple: true, required: false, metadata:[values:["manual", "auto", "keypad", "other", "1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30"]]
		}

		section("...Then...")
		{
			input "doors", "capability.doorControl", title: "Open or close these doors", multiple: true, required: false
			input "switches", "capability.switch", title: "Turn on or off these switches", multiple: true, required: false
        }

		section("Within these limits:")
		{
			input name: "nightOnly", type: "bool", title: "Only between sunset and sunrise?"
			input "modes", "mode", title: "Only during specific mode(s)", multiple: true, required: false
		}
	}
}

def page2prefs()
{
/*
	Notes about how some of the dynamic data dealt with.  The command to be sent for a given device is stored
    in a settings variable called "cmd_x" where "x" is the device's ID.  This ensures that, no matter what
    a user might do to re-order or rename a device, the device setting will stick AND will be unique.  This
    leads to some very long variable names such as "cmd_12345678-9abcd-ef01-2345-6789abcdef01".
    
    There is one flaw with this system:  Variables get leftover.  So, assume that a user wanted to turn on
    a switch with an ID of "abc."  That would lead to a setting called "cmd_abc : on".  If the user some
    time later decides that they no longer want to manipulate that switch, they go into the app and remove
    it.  However, there's no way for a SmartApp to completely remove settings, so "cmd_abc" will be forever
    stuck in the smartapp for all time (unless/until the user uninstalls the instance of the smartapp OR
    until SmartThings has a bad day and all settings get erased... it's happened..)
*/
	dynamicPage(name: "page2prefs", uninstall: true, install: true)
    {
        if (doors != null)
        {
  			section("Open/Close doors")
            {
        		doors.eachWithIndex {s, i ->
                	def door_setting_name = "cmd_" + s.id
					input name: door_setting_name, type: "enum", title: "Open or Close $s?", required: true, metadata:[values:["open", "close"]], refreshAfterSelection: true
            	}
            }
        }
        if (switches != null)
        {
  			section("Turn on/off Switches")
            {
        		switches.eachWithIndex {s, i ->
                	def switch_setting_name = "cmd_" + s.id
                	input name: switch_setting_name, type: "enum", title: "Turn $s on or off?", options: [ "on", "off"], required: true
            	}
            }
        }
		section
        {
			def phrases = location.helloHome?.getPhrases()*.label
			if (phrases) 
			{
				phrases.sort()
                input "HHPhrase", "enum", title: "Activate this 'Hello Home' Phrase", required: false, options: phrases, refreshAfterSelection: true
            }
			input name: "sendPush", type: "bool", title: "Push notification to mobile devices?", defaultValue: false
        }
        
		section()
		{
			label title: "Assign a name", required: false
		}
        
    
    }
}


def installed() 
{
	initialize()
}

def updated() 
{
	unsubscribe()
	initialize()
}

def initialize() 
{
	if (cmd_lock == "Unlock")
    {
    	subscribe(lock1, "lock.unlocked", lockHandler)
    }
    else if (cmd_lock == "Lock")
    {
		subscribe(lock1, "lock.locked", lockHandler)
    }
    else
    {
    	log.debug "Invalid cmd_lock selection"
    }
    
	if (nightOnly)
	{
		// force updating the sunrise/sunset data
		retrieveSunData(true)
	}
    
    // dump out the configuration
    log.debug "Initialize Settings: $settings"
}


def lockHandler(evt) 
{
	if (IsAllOkay())
	{
    	def bRunIt = false
        
		def usedCode = null
        def usedMethod = null
        
		if (evt.data != null)
		{
			usedCode = parseJson(evt.data).usedCode
            usedMethod = parseJson(evt.data).method
//            log.debug "lockHandler 1 shows a usedCode of \"$usedCode\" and a method of \"$usedMethod\""
		}
        
        // special case of "no filter" - always perform the action
        if ((lockCode == null) || lockCode.isEmpty()) // no code/method filter
        {
        	bRunIt = true
        }
        // ideally, the "method" and/or userCode elements should be set. However, older
        // z-wave lock device types don't set method, and only set usercode for non-0 usercodes
		else
        {        
            // if usedMethod isn't populated, try to populate based on the usedCode
            if (usedMethod == null)
            {
                // try to populate the method
                if (usedCode == "manual")
                {
                    usedMethod = "manual"
                }
                else if (usedCode == "auto")
                {
                    usedMethod = "auto"
                }
                else if ((usedCode == "keypad") || (usedCode == 0)) 
                {	// usedCode 0 is a special case of keypad being used (with no PIN)
                    usedMethod = "keypad"
                }
                else if (usedCode == null)
                {   // if usedCode is null, then it's not a keypad PIN number or something else.. so
                    // fall back to parsing the descriptionText
                    if (evt.descriptionText.contains("manually"))
                    {
                        usedMethod = "manual"
                    }
                    else if (evt.descriptionText.contains("autolocked"))
                    {
                        usedMethod = "auto"
                    }
                    else if (evt.descriptionText.contains("keypad"))
                    {
                        usedMethod = "keypad"
                    }
                    else
                    {
                        // both method and code are null, and no hints in the description - so it's other
                        usedMethod = "other"
                    }
                }
                else if (!(usedCode.toString().isInteger()))
                {
                    // usedCode is set, but it's not an integer... 
                    // This is a special case of "other" - we don't understand the usedCode, so it's
                    // just set to 'other'
//                    log.debug "doorlock event passed an unrecognized usedCode:  $usedCode"
                    usedMethod = "other"
                }
            }
            // at this point, "usedMethod" should either be set, or usedCode should be a PIN index
//            log.debug "lockHandler 2 shows a usedCode of \"$usedCode\" and a method of \"$usedMethod\""

            // if it's a matching usedCode... or
            // if it's a matching usedMethod
            if ( ((usedCode != null) && lockCode.contains((usedCode).toString())) ||
            	 ((usedMethod != null) && (lockCode.contains(usedMethod))))
        	{
//            	log.debug "Performing Action!"
            	bRunIt = true
        	}
        }
        if (bRunIt)
        {
        	performActions(evt)
        }
	}
}

private IsAllOkay()
{
	// check time and mode restrictions
	// first modes: 
	def bIsOkay = (modes == null) || (modes.contains(location.mode))
	if (bIsOkay && nightOnly)
	{
		// possibly update the sunrise/sunset data. (don't force the update)
		retrieveSunData(false)
        
		TimeZone.setDefault(location.timeZone)
		// get the current time
		def curTime = new Date(now())

		// when is/was sunrise TODAY after midnight local. 
		def dtSunrise = timeTodayAfter("0:00", state.sunriseTime, location.timeZone)
        // when is/was sunset TODAY after high noon local.
        def dtSunset = timeTodayAfter("12:00", state.sunsetTime, location.timeZone)
		bIsOkay = ((curTime.getTime() < dtSunrise.time) ||  (curTime.getTime() > dtSunset.time))        
	}  
	
	return bIsOkay
}

private retrieveSunData(forceIt)
{
	if ((true == forceIt) || (now() > state.nextSunCheck))
	{
		state.nextSunCheck = now() + (1000 * (60 * 60 *12)) // every 12 hours
		log.debug "Updating sunrise/sunset data"

	/* instead of absolute timedate stamps for sunrise and sunset, use just hours/minutes.	The reason
	   is that if we miss updating the sunrise/sunset data for a day or two, at least the times will be
	   within a few minutes.  Using "timeToday" or "timeTodayAfter", the hours:minutes can be converted
       to the current day.. (this won't work when transitioning into or out of DST) */

		TimeZone.setDefault(location.timeZone)
		def sunData = getSunriseAndSunset(zipcode : location.zipCode)
        
        // tzOffset should actually end up being "0", assuming the proper TZ is configured.  However,
        // I've seen ST come back with dates in Pacific time and UTC.. so do the work to find 
        // the "local" tzOffet for adding to the returned sunrise/sunset data.
        def tzOffset = location.timeZone.getOffset(sunData.sunrise.getTime()) + (sunData.sunrise.getTimezoneOffset() * 60000)

        def newDate = new Date(sunData.sunrise.getTime() + tzOffset)
		state.sunriseTime = newDate.hours + ':' + newDate.minutes

        newDate = new Date(sunData.sunset.getTime() + tzOffset)
		state.sunsetTime = newDate.hours + ':' + newDate.minutes
        
		log.debug "Sunrise time: ${state.sunriseTime} (sunData.sunrise: ${sunData.sunrise.inspect()})"
		log.debug "Sunset time: ${state.sunsetTime} (sunData.sunset: ${sunData.sunset.inspect()}) "
	}
}

private performActions(evt)
{
	doors.eachWithIndex {s, i ->
		def state = s.latestValue("door")
        def newState = settings."cmd_${s.id}"
        // doors aren't as simple as switches.  There are additional states and the states don't match the commands.
        // instead of getting into the complexities, just always send the command and let the device type
        // work out what should happen.
        
		log.debug "$s state is $state.  Sending $newState command."
		s."$newState"()
	}

	switches.eachWithIndex {s, i ->
		def state = s.latestValue("switch")
        def newState = settings."cmd_${s.id}"
		if (state != newState)
		{
			log.debug "$s is currently $state.  Turning it $newState."
			s."$newState"()
		}
		else
		{
			log.debug "$s is already $state."
		}
	}
	if (sendPush)
	{
		sendPushMessage(evt.descriptionText)
	}
	if (HHPhrase)
	{
		location.helloHome.execute(settings.HHPhrase)
	}
}