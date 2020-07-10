/**
 *  Copyright 2016 SmartThings, Inc.
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
	definition (
		name: "standardDeviceTile",
		namespace: "smartthings/tile-ux",
		author: "SmartThings") {

		capability "Switch"
	}

	tiles(scale: 2) {
		// standard tile with actions
		standardTile("actionRings", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}

		// standard flat tile with actions
		standardTile("actionFlat", "device.switch", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}

		// standard flat tile without actions
		standardTile("noActionFlat", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${currentValue}',icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}

		// standard flat tile with only a label
		standardTile("flatLabel", "device.switch", width: 2, height: 2, decoration: "flat") {
			state "label", label: 'On Action', action: "switch.on", backgroundColor: "#ffffff", defaultState: true
		}

		// standard flat tile with icon and label
		standardTile("flatIconLabel", "device.switch", width: 2, height: 2, decoration: "flat") {
			state "iconLabel", label: 'Off Action', action: "switch.off", icon:"st.switches.switch.off", backgroundColor: "#ffffff", defaultState: true
		}

		// standard flat tile with only icon (Refreh text is IN the icon file)
		standardTile("flatIcon", "device.switch", width: 2, height: 2, decoration: "flat") {
			state "icon", action:"refresh.refresh", icon:"st.secondary.refresh", defaultState: true
		}

		// standard with defaultState = true
		standardTile("flatDefaultState", "null", width: 2, height: 2, decoration: "flat") {
			state "off", label: 'Fail!', icon: "st.switches.switch.off"
			state "on", label: 'Pass!', icon: "st.switches.switch.on", defaultState: true
		}

		// standard with implicit defaultState based on order (0 index is selected)
		standardTile("flatImplicitDefaultState1", "null", width: 2, height: 2, decoration: "flat") {
			state "on", label: 'Pass!', icon: "st.switches.switch.on"
			state "off", label: 'Fail!', icon: "st.switches.switch.off"
		}

		// standard with implicit defaultState based on state.name == default
		standardTile("flatImplicitDefaultState2", "null", width: 2, height: 2, decoration: "flat") {
			state "off", label: 'Fail!', icon: "st.switches.switch.off"
			state "default", label: 'Pass!', icon: "st.switches.switch.on"
		}

		// utility tiles to fill the spaces
		standardTile("empty2x2", "null", width: 2, height: 2, decoration: "flat") {
			state "emptySmall", label:'', defaultState: true
		}
		standardTile("empty4x2", "null", width: 4, height: 2, decoration: "flat") {
			state "emptyBigger", label:'', defaultState: true
		}

		// multi-line text (explicit newlines)
		standardTile("multiLine", "device.multiLine", width: 2, height: 2) {
			state "multiLine", label: '${currentValue}', defaultState: true
		}

		standardTile("multiLineWithIcon", "device.multiLine", width: 2, height: 2) {
			state "multiLineIcon", label: '${currentValue}', icon: "st.switches.switch.off", defaultState: true
		}

		main("actionRings")
		details([
			"actionRings", "actionFlat", "noActionFlat",

			"flatLabel", "flatIconLabel", "flatIcon",

			"flatDefaultState", "flatImplicitDefaultState1", "flatImplicitDefaultState2",

			"multiLine", "multiLineWithIcon"
		])
	}
}

def installed() {
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "multiLine", value: "Line 1\nLine 2\nLine 3")
}

def parse(String description) {
}

def on() {
	log.debug "on()"
	sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "off")
}
