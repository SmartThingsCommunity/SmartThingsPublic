/**
 *  !Front Door Button - Sensor version!
 *
 *  Copyright 2018 ELY M.
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
 */
definition(
    name: "!Front Door Button - Sensor version!",
    namespace: "ELY3M",
    author: "ELY M.",
    description: "if someone presses this button hooked to contact sensor. it will turn on the strobe and green lights on.  ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When this sensor is open or closed") {
		input "master", "capability.contactSensor", title: "Where?"
	}
	section("Turn on all of these switches as well") {
		input "switches", "capability.switch", multiple: true, required: false
	}
    
    section("Pick lights to change colors") {
    input "coloredlights", "capability.colorControl", title: "pick colored light", multiple: true, required: false
}

    section("Choose light effects...")
    {
        input "color", "enum", title: "Bulb Color?", required: false, multiple:false, options: [
            ["Soft White":"Soft White - Default"],
            ["White":"White - Concentrate"],
            ["Daylight":"Daylight - Energize"],
            ["Warm White":"Warm White - Relax"],
            "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
        input "lightLevel", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
    }
   
	section("Flash light?"){
		input "flashlight", "capability.switch", title: "These lights", multiple: true, required: false
		input "numFlashes", "number", title: "This number of times (default 30)", required: false
	}
	section("Time settings in milliseconds (optional)..."){
		input "onFor", "number", title: "On for (default 500)", required: false
		input "offFor", "number", title: "Off for (default 500)", required: false
	}
    
	section("Text me at...") {
        input "phonenum", "phone", title: "Phone number?", required: false
		input "phonetext", "text", title: "text?", required: false
	}
    
    
}
    

def installed()
{   
	subscribe(master, "contact.open", onHandler)
	subscribe(master, "contact.close", offHandler)
}

def updated()
{
	unsubscribe()
 	subscribe(master, "contact.open", onHandler)
	subscribe(master, "contact.close", offHandler)
}







def logHandler(evt) {
	log.debug evt.value
}

def onHandler(evt) {
	log.debug evt.value
	log.debug onSwitches()
	onSwitches()?.on()
    changecolor(evt) 
    flashLights()
    textme(evt) 
}

def offHandler(evt) {
	//log.debug evt.value
	//log.debug offSwitches()
	//offSwitches()?.off()
	log.debug evt.value
	log.debug onSwitches()
	onSwitches()?.on()
	changecolor(evt)
    flashLights()
    textme(evt) 

}


private onSwitches() {
	if(switches && onSwitches) { switches + onSwitches }
	else if(switches) { switches }
	else { onSwitches }
}

private offSwitches() {
	if(switches && offSwitches) { switches + offSwitches }
	else if(switches) { switches }
	else { offSwitches }
}

private changecolor(evt) {
	def Color = 0
	def saturation = 100

	switch(color) {
		case "White":
			Color = 52
			saturation = 19
			break;
		case "Daylight":
			Color = 53
			saturation = 91
			break;
		case "Soft White":
			Color = 23
			saturation = 56
			break;
		case "Warm White":
			Color = 20
			saturation = 80 //83
			break;
		case "Blue":
			Color = 70
			break;
		case "Green":
			Color = 39
			break;
		case "Yellow":
			Color = 25
			break;
		case "Orange":
			Color = 10
			break;
		case "Purple":
			Color = 75
			break;
		case "Pink":
			Color = 83
			break;
		case "Red":
			Color = 100
			break;
	}

	state.previous = [:]

	coloredlights.each {
		state.previous[it.id] = [
			"switch": it.currentValue("switch"),
			"level" : it.currentValue("level"),
			"hue": it.currentValue("hue"),
			"saturation": it.currentValue("saturation")
		]
	}

	log.debug "current values = $state.previous"

	def newValue = [hue: Color, saturation: saturation, level: lightLevel as Integer ?: 100]
	log.debug "new value = $newValue"

	coloredlights*.setColor(newValue)
}

private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 500
	def offFor = offFor ?: 500
	def numFlashes = numFlashes ?: 30

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING $numFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = flashlight.collect{it.currentSwitch != "on"}
		def delay = 0L
		numFlashes.times {
			log.trace "Switch on after  $delay msec"
			flashlight.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onFor
			log.trace "Switch off after $delay msec"
			flashlight.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
				}
			}
			delay += offFor
		}
	}
}


private textme(evt) {
  log.trace "$evt.value: $evt, $settings"
  log.debug "$master was open/close, sending text"
  sendSms(phonenum, phonetext)

}    
 
 
