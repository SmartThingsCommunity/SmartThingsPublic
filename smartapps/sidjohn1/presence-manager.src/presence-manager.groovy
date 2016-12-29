/**
 *  Presence Manager
 *
 *  Copyright 2015 Sidney Johnson
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Layout inspired by Tim Slagle & Michael Strucks' Lighting Director
 *
 *	Version 1.0 - Original release
 *	Version 1.0.1 - Number input bug fix, code clean up
 *	Version 1.1 - Upgraded Logic
 */
definition(
	name: "Presence Manager",
	namespace: "sidjohn1",
	author: "Sidney Johnson",
	description: "Manages presence based off lock code, presence, motion, contact and acceleration sensors.",
	category: "Family",
	iconUrl: "http://cdn.device-icons.smartthings.com/Home/home4-icn.png",
	iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@2x.png",
	iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@2x.png")

preferences {
	page name:"pageMain"
	page name:"pagePersonA"
	page name:"pagePersonB"
	page name:"pagePersonC"
	page name:"pagePersonD"
	page name:"pagePersonE"
	page name:"pagePersonF"
}

def pageMain() {
	return dynamicPage(name: "pageMain", nextPage: null, install: true, uninstall: true) {
		section("About") {
			paragraph "Presence Manager smartapp for Smartthings. This app manages presence based off lock code, presence, motion, contact and acceleration sensors."
			paragraph "${textVersion()}\n${textCopyright()}"	
		}
		section("People (Max:6)") {
			if (settings.PersonVPresenceA) {
				href "pagePersonA", title: getHrefTitle(settings.PersonVPresenceA.name), description: getHrefDesc(settings.PersonVPresenceA.name), state: getHrefState(settings.PersonVPresenceA.name)
			}
			else {
				href "pagePersonA", title: getHrefTitle(), description: getHrefDesc(), state: getHrefState()
			}
			
			if (settings.PersonVPresenceA || settings.PersonVPresenceB) {
				if (settings.PersonVPresenceB) {
					href "pagePersonB", title: getHrefTitle(settings.PersonVPresenceB.name), description: getHrefDesc(settings.PersonVPresenceB.name), state: getHrefState(settings.PersonVPresenceB.name)
				}
				else {
					href "pagePersonB", title: getHrefTitle(), description: getHrefDesc(), state: getHrefState()
				}
			}
			if (settings.PersonVPresenceB || settings.PersonVPresenceC) {
				if (settings.PersonVPresenceC) {
					href "pagePersonC", title: getHrefTitle(settings.PersonVPresenceC.name), description: getHrefDesc(settings.PersonVPresenceC.name), state: getHrefState(settings.PersonVPresenceC.name)
				}
				else {
					href "pagePersonC", title: getHrefTitle(), description: getHrefDesc(), state: getHrefState()
				}
			}
			if (settings.PersonVPresenceC || settings.PersonVPresenceD) {
				if (settings.PersonVPresenceD) {
					href "pagePersonD", title: getHrefTitle(settings.PersonVPresenceD.name), description: getHrefDesc(settings.PersonVPresenceD.name), state: getHrefState(settings.PersonVPresenceD.name)
				}
				else {
					href "pagePersonD", title: getHrefTitle(), description: getHrefDesc(), state: getHrefState()
				}
			}
			if (settings.PersonVPresenceD || settings.PersonVPresenceE) {
				if (settings.PersonVPresenceE) {
					href "pagePersonE", title: getHrefTitle(settings.PersonVPresenceE.name), description: getHrefDesc(settings.PersonVPresenceE.name), state: getHrefState(settings.PersonVPresenceE.name)
				}
				else {
					href "pagePersonE", title: getHrefTitle(), description: getHrefDesc(), state: getHrefState()
				}
			}
			if (settings.PersonVPresenceE || settings.PersonVPresenceF) {
				if (settings.PersonVPresenceF) {
					href "pagePersonF", title: getHrefTitle(settings.PersonVPresenceF.name), description: getHrefDesc(settings.PersonVPresenceF.name), state: getHrefState(settings.PersonVPresenceF.name)
				}
				else {
					href "pagePersonF", title: getHrefTitle(), description: getHrefDesc(), state: getHrefState()
				}
			}
		}
		section([title:"Options", mobileOnly:true]) {
			label title:"Assign a name", required: false, defaultValue: "Presence Manager"
		}
	}
}

def pagePersonA() {
	
	def inputPersonAccelerationA = [
		name: "PersonAccelerationA",
		type: "capability.accelerationSensor",
		title: "Acceleration Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonAccelerationDetailsA = [
		name: "PersonAccelerationDetailsA",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]

	def inputPersonPresenceA = [
		name: "PersonPresenceA",
		type: "capability.presenceSensor",
		title: "Presence Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def inputPersonVPresenceA = [
		name: "PersonVPresenceA",
		type: "device.SimulatedPresenceSensor",
		title: "Select a virtual presence sensor to update",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionA = [
		name: "PersonMotionA",
		type: "capability.motionSensor",
		title: "Motion Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionDetailsA = [
		name: "PersonMotionDetailsA",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]
	
	def inputPersonContactA = [
		name: "PersonContactA",
		type: "capability.contactSensor",
		title: "Contact Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonContactDetailsA = [
		name: "PersonContactDetailsA",
		type: "enum",
		options: [["open":"Open = Present"],["closed":"Closed = Present"]],
		title: "Contact Sensor Details",
		multiple: false,
		required: false,
		defaultValue: "closed",
		submitOnChange: false
	]
	
	def inputPersonLockA = [
		name: "PersonLockA",
		type: "capability.lock",
		title: "Door Lock",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonLockDetailsA = [
		name: "PersonLockDetailsA",
		type: "number",
		title: "Lock Code (1-10)",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def pageProperties = [
		name: "pagePersonA",
	]
	
	return dynamicPage(pageProperties) {
		section("Master Presence") {
			input inputPersonVPresenceA
		}
		if (PersonVPresenceA) {
			section("Select sensors to monitor that will update $settings.PersonVPresenceA.name"+"s' presence") {
				input inputPersonPresenceA
				input inputPersonMotionA
				if (PersonMotionA) {
					input inputPersonMotionDetailsA
				}
				input inputPersonAccelerationA
				if (PersonAccelerationA) {
					input inputPersonAccelerationDetailsA
				}
				input inputPersonContactA
				if (PersonContactA) {
					input inputPersonContactDetailsA
				}
				input inputPersonLockA
				if (PersonLockA) {
					input inputPersonLockDetailsA
				}
			}
		}
		else {
			section("Help") {
				paragraph nameText()
			}
		}
	}
}

def pagePersonB() {
	
	def inputPersonAccelerationB = [
		name: "PersonAccelerationB",
		type: "capability.accelerationSensor",
		title: "Acceleration Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonAccelerationDetailsB = [
		name: "PersonAccelerationDetailsB",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]

	def inputPersonPresenceB = [
		name: "PersonPresenceB",
		type: "capability.presenceSensor",
		title: "Presence Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def inputPersonVPresenceB = [
		name: "PersonVPresenceB",
		type: "device.SimulatedPresenceSensor",
		title: "Select a virtual presence sensor to update",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionB = [
		name: "PersonMotionB",
		type: "capability.motionSensor",
		title: "Motion Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionDetailsB = [
		name: "PersonMotionDetailsB",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]
	
	def inputPersonContactB = [
		name: "PersonContactB",
		type: "capability.contactSensor",
		title: "Contact Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonContactDetailsB = [
		name: "PersonContactDetailsB",
		type: "enum",
		options: [["open":"Open = Present"],["closed":"Closed = Present"]],
		title: "Contact Sensor Details",
		multiple: false,
		required: false,
		defaultValue: "closed",
		submitOnChange: false
	]
	
	def inputPersonLockB = [
		name: "PersonLockB",
		type: "capability.lock",
		title: "Door Lock",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonLockDetailsB = [
		name: "PersonLockDetailsB",
		type: "number",
		title: "Lock Code (1-10)",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def pageProperties = [
		name: "pagePersonB",
	]
	
	return dynamicPage(pageProperties) {
		section("Master Presence") {
			input inputPersonVPresenceB
		}
		if (PersonVPresenceB) {
			section("Select sensors to monitor that will update $settings.PersonVPresenceB.name"+"s' presence") {
				input inputPersonPresenceB
				input inputPersonMotionB
				if (PersonMotionB) {
					input inputPersonMotionDetailsB
				}
				input inputPersonAccelerationB
				if (PersonAccelerationB) {
					input inputPersonAccelerationDetailsB
				}
				input inputPersonContactB
				if (PersonContactB) {
					input inputPersonContactDetailsB
				}
				input inputPersonLockB
				if (PersonLockB) {
					input inputPersonLockDetailsB
				}
			}
		}
		else {
			section("Help") {
				paragraph nameText()
			}
		}
	}
}

def pagePersonC() {
	
	def inputPersonAccelerationC = [
		name: "PersonAccelerationC",
		type: "capability.accelerationSensor",
		title: "Acceleration Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonAccelerationDetailsC = [
		name: "PersonAccelerationDetailsC",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]

	def inputPersonPresenceC = [
		name: "PersonPresenceC",
		type: "capability.presenceSensor",
		title: "Presence Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def inputPersonVPresenceC = [
		name: "PersonVPresenceC",
		type: "device.SimulatedPresenceSensor",
		title: "Select a virtual presence sensor to update",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionC = [
		name: "PersonMotionC",
		type: "capability.motionSensor",
		title: "Motion Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionDetailsC = [
		name: "PersonMotionDetailsC",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]
	
	def inputPersonContactC = [
		name: "PersonContactC",
		type: "capability.contactSensor",
		title: "Contact Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonContactDetailsC = [
		name: "PersonContactDetailsC",
		type: "enum",
		options: [["open":"Open = Present"],["closed":"Closed = Present"]],
		title: "Contact Sensor Details",
		multiple: false,
		required: false,
		defaultValue: "closed",
		submitOnChange: false
	]
	
	def inputPersonLockC = [
		name: "PersonLockC",
		type: "capability.lock",
		title: "Door Lock",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonLockDetailsC = [
		name: "PersonLockDetailsC",
		type: "number",
		title: "Lock Code (1-10)",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def pageProperties = [
		name: "pagePersonC",
	]
	
	return dynamicPage(pageProperties) {
		section("Master Presence") {
			input inputPersonVPresenceC
		}
		if (PersonVPresenceC) {
			section("Select sensors to monitor that will update $settings.PersonVPresenceC.name"+"s' presence") {
				input inputPersonPresenceC
				input inputPersonMotionC
				if (PersonMotionC) {
					input inputPersonMotionDetailsC
				}
				input inputPersonAccelerationC
				if (PersonAccelerationC) {
					input inputPersonAccelerationDetailsC
				}
				input inputPersonContactC
				if (PersonContactC) {
					input inputPersonContactDetailsC
				}
				input inputPersonLockC
				if (PersonLockC) {
					input inputPersonLockDetailsC
				}
			}
		}
		else {
			section("Help") {
				paragraph nameText()
			}
		}
	}
}

def pagePersonD() {
	
	def inputPersonAccelerationD = [
		name: "PersonAccelerationD",
		type: "capability.accelerationSensor",
		title: "Acceleration Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonAccelerationDetailsD = [
		name: "PersonAccelerationDetailsD",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]

	def inputPersonPresenceD = [
		name: "PersonPresenceD",
		type: "capability.presenceSensor",
		title: "Presence Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def inputPersonVPresenceD = [
		name: "PersonVPresenceD",
		type: "device.SimulatedPresenceSensor",
		title: "Select a virtual presence sensor to update",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionD = [
		name: "PersonMotionD",
		type: "capability.motionSensor",
		title: "Motion Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionDetailsD = [
		name: "PersonMotionDetailsD",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]
	
	def inputPersonContactD = [
		name: "PersonContactD",
		type: "capability.contactSensor",
		title: "Contact Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonContactDetailsD = [
		name: "PersonContactDetailsD",
		type: "enum",
		options: [["open":"Open = Present"],["closed":"Closed = Present"]],
		title: "Contact Sensor Details",
		multiple: false,
		required: false,
		defaultValue: "closed",
		submitOnChange: false
	]
	
	def inputPersonLockD = [
		name: "PersonLockD",
		type: "capability.lock",
		title: "Door Lock",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonLockDetailsD = [
		name: "PersonLockDetailsD",
		type: "number",
		title: "Lock Code (1-10)",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def pageProperties = [
		name: "pagePersonD",
	]
	
	return dynamicPage(pageProperties) {
		section("Master Presence") {
			input inputPersonVPresenceD
		}
		if (PersonVPresenceD) {
			section("Select sensors to monitor that will update $settings.PersonVPresenceD.name"+"s' presence") {
				input inputPersonPresenceD
				input inputPersonMotionD
				if (PersonMotionD) {
					input inputPersonMotionDetailsD
				}
				input inputPersonAccelerationD
				if (PersonAccelerationD) {
					input inputPersonAccelerationDetailsD
				}
				input inputPersonContactD
				if (PersonContactD) {
					input inputPersonContactDetailsD
				}
				input inputPersonLockD
				if (PersonLockD) {
					input inputPersonLockDetailsD
				}
			}
		}
		else {
			section("Help") {
				paragraph nameText()
			}
		}
	}
}

def pagePersonE() {
	
	def inputPersonAccelerationE = [
		name: "PersonAccelerationE",
		type: "capability.accelerationSensor",
		title: "Acceleration Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonAccelerationDetailsE = [
		name: "PersonAccelerationDetailsE",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]

	def inputPersonPresenceE = [
		name: "PersonPresenceE",
		type: "capability.presenceSensor",
		title: "Presence Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def inputPersonVPresenceE = [
		name: "PersonVPresenceE",
		type: "device.SimulatedPresenceSensor",
		title: "Select a virtual presence sensor to update",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionE = [
		name: "PersonMotionE",
		type: "capability.motionSensor",
		title: "Motion Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionDetailsE = [
		name: "PersonMotionDetailsE",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]
	
	def inputPersonContactE = [
		name: "PersonContactE",
		type: "capability.contactSensor",
		title: "Contact Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonContactDetailsE = [
		name: "PersonContactDetailsE",
		type: "enum",
		options: [["open":"Open = Present"],["closed":"Closed = Present"]],
		title: "Contact Sensor Details",
		multiple: false,
		required: false,
		defaultValue: "closed",
		submitOnChange: false
	]
	
	def inputPersonLockE = [
		name: "PersonLockE",
		type: "capability.lock",
		title: "Door Lock",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonLockDetailsE = [
		name: "PersonLockDetailsE",
		type: "number",
		title: "Lock Code (1-10)",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def pageProperties = [
		name: "pagePersonE",
	]
	
	return dynamicPage(pageProperties) {
		section("Master Presence") {
			input inputPersonVPresenceE
		}
		if (PersonVPresenceE) {
			section("Select sensors to monitor that will update $settings.PersonVPresenceE.name"+"s' presence") {
				input inputPersonPresenceE
				input inputPersonMotionE
				if (PersonMotionE) {
					input inputPersonMotionDetailsE
				}
				input inputPersonAccelerationE
				if (PersonAccelerationE) {
					input inputPersonAccelerationDetailsE
				}
				input inputPersonContactE
				if (PersonContactE) {
					input inputPersonContactDetailsE
				}
				input inputPersonLockE
				if (PersonLockE) {
					input inputPersonLockDetailsE
				}
			}
		}
		else {
			section("Help") {
				paragraph nameText()
			}
		}
	}
}

def pagePersonF() {
	
	def inputPersonAccelerationF = [
		name: "PersonAccelerationF",
		type: "capability.accelerationSensor",
		title: "Acceleration Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonAccelerationDetailsF = [
		name: "PersonAccelerationDetailsF",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]

	def inputPersonPresenceF = [
		name: "PersonPresenceF",
		type: "capability.presenceSensor",
		title: "Presence Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def inputPersonVPresenceF = [
		name: "PersonVPresenceF",
		type: "device.SimulatedPresenceSensor",
		title: "Select a virtual presence sensor to update",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionF = [
		name: "PersonMotionF",
		type: "capability.motionSensor",
		title: "Motion Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonMotionDetailsF = [
		name: "PersonMotionDetailsF",
		type: "number",
		title: "Wait time (minutes)",
		multiple: false,
		required: false,
		defaultValue: "120",
		submitOnChange: false
	]
	
	def inputPersonContactF = [
		name: "PersonContactF",
		type: "capability.contactSensor",
		title: "Contact Sensor",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonContactDetailsF = [
		name: "PersonContactDetailsF",
		type: "enum",
		options: [["open":"Open = Present"],["closed":"Closed = Present"]],
		title: "Contact Sensor Details",
		multiple: false,
		required: false,
		defaultValue: "closed",
		submitOnChange: false
	]
	
	def inputPersonLockF = [
		name: "PersonLockF",
		type: "capability.lock",
		title: "Door Lock",
		multiple: false,
		required: false,
		defaultValue: empty,
		submitOnChange: true
	]
	
	def inputPersonLockDetailsF = [
		name: "PersonLockDetailsF",
		type: "number",
		title: "Lock Code (1-10)",
		multiple: false,
		required: true,
		defaultValue: empty,
		submitOnChange: false
	]
	
	def pageProperties = [
		name:	   "pagePersonF",
	]
	
	return dynamicPage(pageProperties) {
		section("Master Presence") {
			input inputPersonVPresenceF
		}
		if (PersonVPresenceF) {
			section("Select sensors to monitor that will update $settings.PersonVPresenceF.name"+"s' presence") {
				input inputPersonPresenceF
				input inputPersonMotionF
				if (PersonMotionF) {
					input inputPersonMotionDetailsF
				}
				input inputPersonAccelerationF
				if (PersonAccelerationF) {
					input inputPersonAccelerationDetailsF
				}
				input inputPersonContactF
				if (PersonContactF) {
					input inputPersonContactDetailsF
				}
				input inputPersonLockF
				if (PersonLockF) {
					input inputPersonLockDetailsF
				}
			}
		}
		else {
			section("Help") {
				paragraph nameText()
			}
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	log.info "Presence Manager ${textVersion()} ${textCopyright()}"
	if (PersonAccelerationA) {
		subscribe(settings.PersonAccelerationA, "acceleration", eventHandlerA)
	}
	if (PersonContactA) {
		subscribe(settings.PersonContactA, "contact", eventHandlerA)
	}
	if (PersonMotionA) {
		subscribe(settings.PersonMotionA, "motion", eventHandlerA)
	}
	if (PersonLockA) {
		subscribe(settings.PersonLockA, "lock.unlocked", eventHandlerA)
	}
	if (PersonPresenceA) {
		subscribe(settings.PersonPresenceA, "presence", eventHandlerA)
	}
	
	if (PersonAccelerationB) {
		subscribe(settings.PersonAccelerationB, "acceleration", eventHandlerB)
	}
	if (PersonContactB) {
		subscribe(settings.PersonContactB, "contact", eventHandlerB)
	}
	if (PersonMotionB) {
		subscribe(settings.PersonMotionB, "motion", eventHandlerB)
	}
	if (PersonLockB) {
		subscribe(settings.PersonLockB, "lock.unlocked", eventHandlerB)
	}
	if (PersonPresenceB) {
		subscribe(settings.PersonPresenceB, "presence", eventHandlerB)
	}
	
	if (PersonAccelerationC) {
		subscribe(settings.PersonAccelerationC, "acceleration", eventHandlerC)
	}
	if (PersonContactC) {
		subscribe(settings.PersonContactC, "contact", eventHandlerC)
	}
	if (PersonMotionC) {
		subscribe(settings.PersonMotionC, "motion", eventHandlerC)
	}
	if (PersonLockC) {
		subscribe(settings.PersonLockC, "lock.unlocked", eventHandlerC)
	}
	if (PersonPresenceC) {
		subscribe(settings.PersonPresenceC, "presence", eventHandlerC)
	}

	if (PersonAccelerationD) {
		subscribe(settings.PersonAccelerationD, "acceleration", eventHandlerD)
	}
	if (PersonContactD) {
		subscribe(settings.PersonContactD, "contact", eventHandlerD)
	}
	if (PersonMotionD) {
		subscribe(settings.PersonMotionD, "motion", eventHandlerD)
	}
	if (PersonLockD) {
		subscribe(settings.PersonLockD, "lock.unlocked", eventHandlerD)
	}
	if (PersonPresenceD) {
		subscribe(settings.PersonPresenceD, "presence", eventHandlerD)
	}

	if (PersonAccelerationE) {
		subscribe(settings.PersonAccelerationE, "acceleration", eventHandlerE)
	}
	if (PersonContactE) {
		subscribe(settings.PersonContactE, "contact", eventHandlerE)
	}
	if (PersonMotionE) {
		subscribe(settings.PersonMotionE, "motion", eventHandlerE)
	}
	if (PersonLockE) {
		subscribe(settings.PersonLockE, "lock.unlocked", eventHandlerE)
	}
	if (PersonPresenceE) {
		subscribe(settings.PersonPresenceE, "presence", eventHandlerE)
	}

	if (PersonAccelerationF) {
		subscribe(settings.PersonAccelerationF, "acceleration", eventHandlerF)
	}
	if (PersonContactF) {
		subscribe(settings.PersonContactF, "contact", eventHandlerF)
	}
	if (PersonMotionF) {
		subscribe(settings.PersonMotionF, "motion", eventHandlerF)
	}
	if (PersonLockF) {
		subscribe(settings.PersonLockF, "lock.unlocked", eventHandlerF)
	}
	if (PersonPresenceF) {
		subscribe(settings.PersonPresenceF, "presence", eventHandlerF)
	}
}

def eventHandlerA(evt) {
	atomicState.statusA = 0 
	if (evt.name == "contact" && evt.value == settings.PersonContactDetailsA) {
		state.lastChangedA = now()
    }
	if (settings.PersonVPresenceA.currentPresence == "present") {
		if (evt.name == "acceleration" && evt.value == "inactive" && PersonAccelerationDetailsA.toInteger() > 0) {
			runIn(settings.PersonAccelerationDetailsA.toInteger()*60, "PersonSchedeuleA", [overwrite: true])
		}
		if (evt.name == "motion" && evt.value == "inactive" && PersonMotionDetailsA.toInteger() > 0) {
			runIn(settings.PersonMotionDetailsA.toInteger()*60, "PersonSchedeuleA", [overwrite: true])
		}
		if (evt.name == "presence" && evt.value == "not present") {
        	if (settings.PersonAccelerationA && settings.PersonAccelerationA.currentAcceleration == "active") {
            	atomicState.statusA = atomicState.statusA+1
            }
			if (settings.PersonMotionA && settings.PersonMotionA.currentMotion == "active") {
            	atomicState.statusA = atomicState.statusA+1
            }
			if (settings.PersonContactA && settings.PersonContactA.currentContact == settings.PersonContactDetailsA) {
            	atomicState.statusA = atomicState.statusA+1
            }
			log.debug "${atomicState.statusA}"
			if (atomicState.statusA == 0) {
				unschedule(PersonMotionSchedeuleA)
				PersonVPresenceA.departed()
				sendEvent(linkText:app.label, name:"${settings.PersonVPresenceA.name}", value:"absent",descriptionText:"${settings.PersonVPresenceA.name} is absent due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "${settings.PersonVPresenceA.name} is absent due to ${evt.name}"
			}
		}
	}
	else {
		if ((evt.name == "acceleration" && evt.value == "active") || (evt.name == "contact" && evt.value == settings.PersonContactDetailsA) || (evt.name == "lock" && evt.value == "unlocked" && evt.descriptionText.contains("${PersonLockDetailsA}")) || (evt.name == "motion" && evt.value == "active") || (evt.name == "presence" && evt.value == "present")) {
			PersonVPresenceA.arrived()
			sendEvent(linkText:app.label, name:"${settings.PersonVPresenceA.name}", value:"present",descriptionText:"${settings.PersonVPresenceA.name} is present due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${settings.PersonVPresenceA.name} is present due to ${evt.name}"
		}
	}
}

def eventHandlerB(evt) {
	if (evt.name == "contact" && evt.value == settings.PersonContactDetailsB) {
		state.lastChangedB = now()
    }
	if (settings.PersonVPresenceB.currentPresence == "present") {
		if (evt.name == "acceleration" && evt.value == "inactive" && PersonAccelerationDetailsB.toInteger() > 0) {
			runIn(settings.PersonAccelerationDetailsB.toInteger()*60, "PersonSchedeuleB", [overwrite: true])
		}
		if (evt.name == "motion" && evt.value == "inactive" && PersonMotionDetailsB.toInteger() > 0) {
			runIn(settings.PersonMotionDetailsB.toInteger()*60, "PersonSchedeuleB", [overwrite: true])
		}
		if (evt.name == "presence" && evt.value == "not present") {
        	if (settings.PersonAccelerationB && settings.PersonAccelerationB.currentAcceleration == "active") {
            	atomicState.statusB = atomicState.statusB+1
            }
			if (settings.PersonMotionB && settings.PersonMotionB.currentMotion == "active") {
            	atomicState.statusB = atomicState.statusB+1
            }
			if (settings.PersonContactB && settings.PersonContactB.currentContact == settings.PersonContactDetailsB) {
            	atomicState.statusB = atomicState.statusB+1
            }
			if (atomicState.statusB == 0) {
				unschedule(PersonMotionSchedeuleB)
				PersonVPresenceB.departed()
				sendEvent(linkText:app.label, name:"${settings.PersonVPresenceB.name}", value:"absent",descriptionText:"${settings.PersonVPresenceB.name} is absent due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "${settings.PersonVPresenceB.name} is absent due to ${evt.name}"
			}
		}
	}
	else {
		if ((evt.name == "acceleration" && evt.value == "active") || (evt.name == "contact" && evt.value == settings.PersonContactDetailsB) || (evt.name == "lock" && evt.value == "unlocked" && evt.descriptionText.contains("${PersonLockDetailsB}")) || (evt.name == "motion" && evt.value == "active") || (evt.name == "presence" && evt.value == "present")) {
			PersonVPresenceB.arrived()
			sendEvent(linkText:app.label, name:"${settings.PersonVPresenceB.name}", value:"present",descriptionText:"${settings.PersonVPresenceB.name} is present due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${settings.PersonVPresenceB.name} is present due to ${evt.name}"
		}
	}
}

def eventHandlerC(evt) {
	if (evt.name == "contact" && evt.value == settings.PersonContactDetailsC) {
		state.lastChangedC = now()
    }
	if (settings.PersonVPresenceC.currentPresence == "present") {
		if (evt.name == "acceleration" && evt.value == "inactive" && PersonAccelerationDetailsC.toInteger() > 0) {
			runIn(settings.PersonAccelerationDetailsC.toInteger()*60, "PersonSchedeuleC", [overwrite: true])
		}
		if (evt.name == "motion" && evt.value == "inactive" && PersonMotionDetailsC.toInteger() > 0) {
			runIn(settings.PersonMotionDetailsC.toInteger()*60, "PersonSchedeuleC", [overwrite: true])
		}
		if (evt.name == "presence" && evt.value == "not present") {
        	if (settings.PersonAccelerationC && settings.PersonAccelerationC.currentAcceleration == "active") {
            	atomicState.statusC = atomicState.statusC+1
            }
			if (settings.PersonMotionC && settings.PersonMotionC.currentMotion == "active") {
            	atomicState.statusC = atomicState.statusC+1
            }
			if (settings.PersonContactC && settings.PersonContactC.currentContact == settings.PersonContactDetailsC) {
            	atomicState.statusC = atomicState.statusC+1
            }
			if (atomicState.statusC == 0) {
				unschedule(PersonMotionSchedeuleC)
				PersonVPresenceC.departed()
				sendEvent(linkText:app.label, name:"${settings.PersonVPresenceC.name}", value:"absent",descriptionText:"${settings.PersonVPresenceC.name} is absent due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "${settings.PersonVPresenceC.name} is absent due to ${evt.name}"
			}
		}
	}
	else {
		if ((evt.name == "acceleration" && evt.value == "active") || (evt.name == "contact" && evt.value == settings.PersonContactDetailsC) || (evt.name == "lock" && evt.descriptionText.contains("${PersonLockDetailsC}")) || (evt.name == "motion" && evt.value == "active") || (evt.name == "presence" && evt.value == "present")) {
			PersonVPresenceC.arrived()
			sendEvent(linkText:app.label, name:"${settings.PersonVPresenceC.name}", value:"present",descriptionText:"${settings.PersonVPresenceC.name} is present due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${settings.PersonVPresenceC.name} is present due to ${evt.name}"
		}
	}
}

def eventHandlerD(evt) {
	if (evt.name == "contact" && evt.value == settings.PersonContactDetailsD) {
		state.lastChangedD = now()
    }
	if (settings.PersonVPresenceD.currentPresence == "present") {
		if (evt.name == "acceleration" && evt.value == "inactive" && PersonAccelerationDetailsD.toInteger() > 0) {
			runIn(settings.PersonAccelerationDetailsD.toInteger()*60, "PersonSchedeuleD", [overwrite: true])
		}
		if (evt.name == "motion" && evt.value == "inactive" && PersonMotionDetailsD.toInteger() > 0) {
			runIn(settings.PersonMotionDetailsD.toInteger()*60, "PersonSchedeuleD", [overwrite: true])
		}
		if (evt.name == "presence" && evt.value == "not present") {
        	if (settings.PersonAccelerationD && settings.PersonAccelerationD.currentAcceleration == "active") {
            	atomicState.statusD = atomicState.statusD+1
            }
			if (settings.PersonMotionD && settings.PersonMotionD.currentMotion == "active") {
            	atomicState.statusD = atomicState.statusD+1
            }
			if (settings.PersonContactD && settings.PersonContactD.currentContact == settings.PersonContactDetailsD) {
            	atomicState.statusD = atomicState.statusD+1
            }
			if (atomicState.statusD == 0) {
				unschedule(PersonMotionSchedeuleD)
				PersonVPresenceD.departed()
				sendEvent(linkText:app.label, name:"${settings.PersonVPresenceD.name}", value:"absent",descriptionText:"${settings.PersonVPresenceD.name} is absent due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "${settings.PersonVPresenceD.name} is absent due to ${evt.name}"
			}
		}
	}
	else {
		if ((evt.name == "acceleration" && evt.value == "active") || (evt.name == "contact" && evt.value == settings.PersonContactDetailsD) || (evt.name == "lock" && evt.value == "unlocked" && evt.descriptionText.contains("${PersonLockDetailsD}")) || (evt.name == "motion" && evt.value == "active") || (evt.name == "presence" && evt.value == "present")) {
			PersonVPresenceD.arrived()
			sendEvent(linkText:app.label, name:"${settings.PersonVPresenceD.name}", value:"present",descriptionText:"${settings.PersonVPresenceD.name} is present due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${settings.PersonVPresenceD.name} is present due to ${evt.name}"
		}
	}
}

def eventHandlerE(evt) {
	if (evt.name == "contact" && evt.value == settings.PersonContactDetailsE) {
		state.lastChangedE = now()
    }
	if (settings.PersonVPresenceE.currentPresence == "present") {
		if (evt.name == "acceleration" && evt.value == "inactive" && PersonAccelerationDetailsE.toInteger() > 0) {
			runIn(settings.PersonAccelerationDetailsE.toInteger()*60, "PersonSchedeuleE", [overwrite: true])
		}
		if (evt.name == "motion" && evt.value == "inactive" && PersonMotionDetailsE.toInteger() > 0) {
			runIn(settings.PersonMotionDetailsE.toInteger()*60, "PersonSchedeuleE", [overwrite: true])
		}
		if (evt.name == "presence" && evt.value == "not present") {
        	if (settings.PersonAccelerationE && settings.PersonAccelerationE.currentAcceleration == "active") {
            	atomicState.statusE = atomicState.statusE+1
            }
			if (settings.PersonMotionE && settings.PersonMotionE.currentMotion == "active") {
            	atomicState.statusE = atomicState.statusE+1
            }
			if (settings.PersonContactE && settings.PersonContactE.currentContact == settings.PersonContactDetailsE) {
            	atomicState.statusE = atomicState.statusE+1
            }
			if (atomicState.statusE == 0) {
				unschedule(PersonMotionSchedeuleE)
				PersonVPresenceE.departed()
				sendEvent(linkText:app.label, name:"${settings.PersonVPresenceE.name}", value:"absent",descriptionText:"${settings.PersonVPresenceE.name} is absent due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "${settings.PersonVPresenceE.name} is absent due to ${evt.name}"
			}
		}
	}
	else {
		if ((evt.name == "acceleration" && evt.value == "active") || (evt.name == "contact" && evt.value == settings.PersonContactDetailsE) || (evt.name == "lock" && evt.value == "unlocked" && evt.descriptionText.contains("${PersonLockDetailsE}")) || (evt.name == "motion" && evt.value == "active") || (evt.name == "presence" && evt.value == "present")) {
			PersonVPresenceE.arrived()
			sendEvent(linkText:app.label, name:"${settings.PersonVPresenceE.name}", value:"present",descriptionText:"${settings.PersonVPresenceE.name} is present due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${settings.PersonVPresenceE.name} is present due to ${evt.name}"
		}
	}
}

def eventHandlerF(evt) {
	if (evt.name == "contact" && evt.value == settings.PersonContactDetailsF) {
		state.lastChangedF = now()
    }
	if (settings.PersonVPresenceF.currentPresence == "present") {
		if (evt.name == "acceleration" && evt.value == "inactive" && PersonAccelerationDetailsF.toInteger() > 0) {
			runIn(settings.PersonAccelerationDetailsF.toInteger()*60, "PersonSchedeuleF", [overwrite: true])
		}
		if (evt.name == "motion" && evt.value == "inactive" && PersonMotionDetailsF.toInteger() > 0) {
			runIn(settings.PersonMotionDetailsF.toInteger()*60, "PersonSchedeuleF", [overwrite: true])
		}
		if (evt.name == "presence" && evt.value == "not present") {
        	if (settings.PersonAccelerationF && settings.PersonAccelerationF.currentAcceleration == "active") {
            	atomicState.statusF = atomicState.statusF+1
            }
			if (settings.PersonMotionF && settings.PersonMotionF.currentMotion == "active") {
            	atomicState.statusF = atomicState.statusF+1
            }
			if (settings.PersonContactF && settings.PersonContactF.currentContact == settings.PersonContactDetailsF) {
            	atomicState.statusF = atomicState.statusF+1
            }
			if (atomicState.statusF == 0) {
				unschedule(PersonMotionSchedeuleF)
				PersonVPresenceF.departed()
				sendEvent(linkText:app.label, name:"${settings.PersonVPresenceF.name}", value:"absent",descriptionText:"${settings.PersonVPresenceF.name} is absent due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "${settings.PersonVPresenceF.name} is absent due to ${evt.name}"
			}
		}
	}
	else {
		if ((evt.name == "acceleration" && evt.value == "active") || (evt.name == "contact" && evt.value == settings.PersonContactDetailsF) || (evt.name == "lock" && evt.value == "unlocked" && evt.descriptionText.contains("${PersonLockDetailsF}")) || (evt.name == "motion" && evt.value == "active") || (evt.name == "presence" && evt.value == "present")) {
			PersonVPresenceF.arrived()
			sendEvent(linkText:app.label, name:"${settings.PersonVPresenceF.name}", value:"present",descriptionText:"${settings.PersonVPresenceF.name} is present due to ${evt.name}", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${settings.PersonVPresenceF.name} is present due to ${evt.name}"
		}
	}
}

def PersonSchedeuleA() {
	PersonVPresenceA.departed()
	sendEvent(linkText:app.label, name:"${settings.PersonVPresenceA.name}", value:"absent",descriptionText:"${settings.PersonVPresenceA.name} is absent due to inactivity", eventType:"SOLUTION_EVENT", displayed: true)
	log.trace "${settings.PersonVPresenceA.name} is absent due to inactivity"
}

def PersonSchedeuleB() {
	PersonVPresenceB.departed()
	sendEvent(linkText:app.label, name:"${settings.PersonVPresenceB.name}", value:"absent",descriptionText:"${settings.PersonVPresenceB.name} is absent due to inactivity", eventType:"SOLUTION_EVENT", displayed: true)
	log.trace "${settings.PersonVPresenceB.name} is absent due to inactivity"
}

def PersonSchedeuleC() {
	PersonVPresenceC.departed()
	sendEvent(linkText:app.label, name:"${settings.PersonVPresenceC.name}", value:"absent",descriptionText:"${settings.PersonVPresenceC.name} is absent due to inactivity", eventType:"SOLUTION_EVENT", displayed: true)
	log.trace "${settings.PersonVPresenceC.name} is absent due to inactivity"
}

def PersonSchedeuleD() {
	PersonVPresenceD.departed()
	sendEvent(linkText:app.label, name:"${settings.PersonVPresenceD.name}", value:"absent",descriptionText:"${settings.PersonVPresenceD.name} is absent due to inactivity", eventType:"SOLUTION_EVENT", displayed: true)
	log.trace "${settings.PersonVPresenceD.name} is absent due to inactivity"
}

def PersonSchedeuleE() {
	PersonVPresenceE.departed()
	sendEvent(linkText:app.label, name:"${settings.PersonVPresenceE.name}", value:"absent",descriptionText:"${settings.PersonVPresenceE.name} is absent due to inactivity", eventType:"SOLUTION_EVENT", displayed: true)
	log.trace "${settings.PersonVPresenceE.name} is absent due to inactivity"
}

def PersonSchedeuleF() {
	PersonVPresenceF.departed()
	sendEvent(linkText:app.label, name:"${settings.PersonVPresenceF.name}", value:"absent",descriptionText:"${settings.PersonVPresenceF.name} is absent due to inactivity", eventType:"SOLUTION_EVENT", displayed: true)
	log.trace "${settings.PersonVPresenceF.name} is absent due to inactivity"
}

def getHrefTitle(person) {
	def title = "Empty"
	if (person) {
		title = person
	}
	title
}

private def getHrefDesc(person) {
	def desc = "Tap to add a person"
	if (person) {
		desc = "Tap to edit a person"
	}
	desc	
}

private def getHrefState(person){
	def result = ""
	if (person) {
		result = "complete"	
	}
	result
}

private def nameText() {
	def text =
		"Select the master presence sensor for a housemate. If there are none listed please install a Simulated Presence Sensor for each housemate first."
	text
}

private hasBeenRecentContact(deviceContact)
{
	def isActive = false
	def deltaMinutes = minutes as Long
	if (deltaMinutes) {
		def contactEvents = deviceContact.eventsSince(new Date(now() - (60000 * 60)))
		log.trace "Found ${contactEvents?.size() ?: 0} events in the last hour"
		if (contactEvents.find { it.value == "active" }) {
			isActive = true
		}
	}

	isActive
}

private def textVersion() {
	def text = "Version 1.1"
}

private def textCopyright() {
	def text = "Copyright © 2015 Sidjohn1"
}