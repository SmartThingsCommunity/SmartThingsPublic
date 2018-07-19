/*
Power Is Out

https://github.com/flyjmz/jmzSmartThings
https://community.smartthings.com/t/this-is-awesome-power-is-out-based-on-state-of-v1-motion-sensor/3757/1
   
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at:
 
	http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
for the specific language governing permissions and limitations under the License.

Based on "Power is Out" by @twack:
https://community.smartthings.com/t/power-sensor/1148/4

Version 1.0		1 May 2014			- Forked @twack's code and added "Power is back on" notification, plus did some app maturing.
Version 1.1		2 August 2016 		- updated notes within app to clarify.
Version 1.2	 	4 August 2016 		- created ability to turn on/off lights/switches (like Cree bulbs, Hue, etc.) when power is restored.  Based on inputs from scottinpollock.
Version 1.3		3 September 2016	- Cleaned up code a little
Version 1.4		10 October 2016		- Added contact book capability, removed copyright, added ability to return bulbs back to previous state (thanks to @nsweet's SmartBulb Power Outage app)
Version 1.5		29 August 2017		- Added Periodic Notifications

Todo:
-add periodic notifications snooze
*/
 
definition(
    name: "Power Is Out",
    namespace: "flyjmz",
    author: "flyjmz230@gmail.com",
    description: "Alerts when power lost (uses SmartSense Motion v1's change from wired-power to battery-power)",
    category: "Safety & Security",
    iconUrl: "https://github.com/flyjmz/jmzSmartThings/raw/master/resources/home2-icn@2x.png",
    iconX2Url: "https://github.com/flyjmz/jmzSmartThings/raw/master/resources/home2-icn@2x.png",
    iconX3Url: "https://github.com/flyjmz/jmzSmartThings/raw/master/resources/home2-icn@2x.png"
)

preferences {
    page(name: "settings")
}

def settings() {
    dynamicPage(name: "settings", title: "", install: true, uninstall: true) {
        section("When there is wired-power loss on...") {
            input "motion1", "capability.motionSensor", title: "Where?"
            paragraph "Must be a SmartSense Motion v1.  Also, SmartThings Hub and internet connection (modem/router) must retain power for this to work (e.g. connect them to a UPS)."
        }
        section("Make changes to the following when powered is restored..."){
            input "offSwitches", "capability.switch", title: "Turn these off", required: false, multiple: true
            input "onSwitchesAlways", "capability.switch", title: "Turn these on", required: false, multiple: true
            input "onSwitchesDark", "capability.switch", title: "Turn these on if after sunset", required: false, multiple: true
            input "returnSwitches", "capability.switch", title: "Return these to their state before the power failure", required: false, multiple: true
            input "waitSeconds", "number", title: "How long to wait before conducting changes (in seconds)?  (To ensure devices have reconnected to hub)", required: true, defaultValue: 30
        }
        section("Notification Type"){
            input("recipients", "contact", title: "Send notifications to") {
                input "pushAndPhone", "enum", title: "Send SMS? (optional, it will always send push)", required: false, options: ["Yes", "No"]		//TO DO - Add periodic notifications!
                input "phone", "phone", title: "Phone Number (only for SMS)", required: false
                paragraph "If outside the US please make sure to enter the proper country code"
            }
        }
        section("Periodic Notificaitons") {
            paragraph "You can have periodic notifications inbetween the initial 'Power is out' and 'Power is back on' notifications." 
            input "periodicNotifications", "bool", title: "Receive periodic notifications?", required: false, submitOnChange: true
            if (periodicNotifications) {
                input "waitMinutes", "number", title: "Minutes inbetween timed periodic notifications?", required: false
                input "modeChange", "bool", title: "Notify on mode change?", required: false
                input "sunChange", "bool", title: "Notify at sunrise/sunset?", required: false
                //paragraph "Periodic Notifications can be snoozed easily with a virtual switch device type.  This is useful when you are unable to resolve an issue and the notifications become irritable.  A 'snooze' switch in your Things is easier to hit than changeing these settings." 
                //input "snoozeSwitch", "capability.switch", title: "Which switch controls snoozing periodic notifications?", required: false
            }      
        }
    }
}    

def installed() {
	 initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motion1, "powerSource.battery", onBatteryPowerHandler)
    subscribe(motion1, "powerSource.powered", PoweredPowerHandler)
    subscribe(returnSwitches, "switch", saveStates)
    saveStates()
    if (modeChange) subscribe(location, "mode", periodicNotifier) //checks status every time mode changes (in case it missed it)
    if (sunChange) {				//checks status every sun rises/sets (in case it missed it)
        subscribe(location, "sunrise", periodicNotifier)
    	subscribe(location, "sunset", periodicNotifier)
    }
}

def saveStates(evt) {
   def switchStates = [:]
   returnSwitches?.each {
       switchStates[it.id] = it.currentSwitch
       log.debug "${it.id} value ${it.currentSwitch}" 
       state.switches = switchStates
   }
}

def onBatteryPowerHandler(evt) {
	log.trace "$evt.value: $evt"
    atomicState.problemTime = now()
	def msg = "${motion1.label ?: motion1.name} sensed Power is Out!"
    log.debug "sending push for power is out"
	sendMessage(msg)
    if (periodicNotifications) {
        if (waitMinutes) {
            runIn((waitMinutes * 60), periodicNotifier)
            log.debug "periodic notifications is on, scheduling stillWrong() to run again in ${waitMinutes} minutes"
        }
    }
}

def PoweredPowerHandler(evt) {
	log.trace "$evt.value: $evt"
	def msg = "${motion1.label ?: motion1.name} sensed Power is Back On!"
	runIn(waitSeconds,switchChanger)  //wait a period of time to ensure the things you're trying to change are connected to hub again
    log.debug "sending push for power is back on"
	sendMessage(msg)
}

def switchChanger() {
	if (offSwitches) {
    	log.debug "turning off switches now that power is restored"
    	offSwitches.off()
	}
    if (onSwitchesDark) {
    	log.debug "turning on switches now that power is restored and it's dark"
        def ss = getSunriseAndSunset()
        def now = new Date()
		def dark = ss.sunset
        if (dark.before(now)) {
    		onSwitchesDark.on()
        }    
	}
    if (onSwitchesAlways) {
    	log.debug "turning on switches now that power is restored"
    	onSwitchesAlways.on()
	}
    if (returnSwitches) {
 		returnSwitches?.each {
            if (state.switches[it.id] == "off") {
            	log.debug "turning $it.label off"
				it.off()
            } else if (state.switches[it.id] == "on") {
            	log.debug "turning $it.label on"
				it.on()
        	}
        }
    }
}

def periodicNotifier(evt) {
    def myMotionPowerState = motion1.currentState("powerSource")
    if (myMotionPowerState.value == "battery") {
    	int timeSince = ((now() - atomicState.problemTime) / 60000) //time since issue occured in whole minutes
        if (timeSince > 180) {  //determines whether to report in hours or minutes (longer than 180 minutes is reported in hours).
            int timeMsg = timeSince / 60
            sendMessage("Periodic Alert: ${motion1.label ?: motion1.name} sensed power has been out for ${timeMsg} hours!")
        } else {
            sendMessage("Periodic Alert: ${motion1.label ?: motion1.name} sensed power has been out for ${timeSince} minutes!")
        }
        if (waitMinutes) {
            runIn((waitMinutes * 60), periodicNotifier)
            log.debug "periodic notifications is on, scheduling to run again in ${waitMinutes} minutes"
        }
	}
}

private sendMessage(msg) {
	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients)
	} else {
		Map options = [:]
        if (phone) {
			options.phone = phone
			log.debug 'sending SMS'
		} else if (pushAndPhone == 'Yes') {
        	options.method = 'both'
            options.phone = phone
        } else options.method = 'push'
		sendNotification(msg, options)
	}
}