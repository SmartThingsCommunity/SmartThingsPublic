/**
 *  Copyright 2019 SmartThings
 *
 *  DTH showing example preference usage and to facilitate testing
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
metadata {
	definition (name: "Simulated Device Preferences", namespace: "smartthings/testing", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
	}

	preferences {
		section {
			input(title: "Section 1 Title",
					description: "Section 1 Description",
					displayDuringSetup: false,
					type: "paragraph",
					element: "paragraph")
			input("textInput", "text",
					title: "Text Title",
					description: "Text Description",
					defaultValue: "default value",
					required: false)
			input("enumInput", "enum",
					title: "Enum Title (key/value options)",
					description: "Enum Description (key/value options)",
					options: ["Option1Key":"Option 1 Value", "Option2Key":"Option 2 Value", "Option3Key":"Option 3 Value", "Option4Key":"Option 4 Value"],
					defaultValue: "Option1Key",
					required: false)
			input("enumInput2", "enum",
					title: "Enum Title 2 (value options)",
					description: "Enum Description 2 (value options)",
					options: ["Option 1 Value", "Option 2 Value", "Option 3 Value", "Option 4 Value"],
					defaultValue: "Option 1 Value",
					required: false)
			input("enumInput3", "enum",
					title: "Enum Title 3 (no options)", description: "Enum Description 3 (no options)",
					required: false)
			input("boolInput", "boolean",
					title: "Boolean Title",
					description: "Boolean Description",
					defaultValue: "true",
					required: false)
		}
		section {
			input(title: "Section 2 Title",
					description: "Section 2 Description",
					displayDuringSetup: false,
					type: "paragraph",
					element: "paragraph")
			input("intInput", "integer",
					title: "Integer Title",
					description: "Integer Description",
					defaultValue: 5,
					range: "1..10",
					required: false)
			input("decInput", "decimal",
					title: "Decimal Title",
					description: "Decimal Description",
					defaultValue: "5.0",
					required: false)
			input("passInput", "password",
					title: "Password Title",
					description: "Password Description",
					defaultValue: "default password",
					required: false)
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOn", defaultState: true
				attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOn"
				attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOff"
			}
		}

		standardTile("explicitOn", "device.switch", width: 2, height: 2, decoration: "flat") {
			state "default", label: "On", action: "switch.on", icon: "st.Home.home30", backgroundColor: "#ffffff"
		}
		standardTile("explicitOff", "device.switch", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Off", action: "switch.off", icon: "st.Home.home30", backgroundColor: "#ffffff"
		}

		main(["switch"])
		details(["switch", "explicitOn", "explicitOff"])

	}
}

def parse(description) {
}

def updated() {
	Map newPreferences = [
		boolInput: boolInput,
		decInput: decInput,
		enumInput: enumInput,
		enumInput2: enumInput2,
		enumInput3: enumInput3,
		intInput: intInput,
		passInput: passInput,
		textInput: textInput
	]
	log.debug "Current preferences: ${state.preferences}"
	log.debug "New preferences: ${newPreferences}"
	state.preferences = newPreferences
}

def on() {
	sendEvent(name: "switch", value: "on", isStateChange: true)
}

def off() {
	sendEvent(name: "switch", value: "off", isStateChange: true)
}

def installed() {
	on()
}
