/*
 *  Zooz Child Switch Button
 *
 *  Changelog:
 *
 *    2022-03-02
 *      - Publication Release
 *
 *  Copyright 2022 Zooz
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
*/

metadata {
    definition (
		name: "Zooz Child Switch Button",
		namespace: "Zooz",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "oic.d.light",
		mnmn: "SmartThingsCommunity",
		vid: "29d51c12-bb47-3d95-ad2e-831656ed20a8"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Button"
		capability "Refresh"
    }

	preferences() {}
}

def parse(String description) {
	return []
}

def on() {
	log.debug "on()..."
	parent.childOn(device.deviceNetworkId)
}

def off() {
	log.debug "off()..."
	parent.childOff(device.deviceNetworkId)
}

def refresh() {
	log.debug "refresh()..."
	parent.childRefresh(device.deviceNetworkId)
}