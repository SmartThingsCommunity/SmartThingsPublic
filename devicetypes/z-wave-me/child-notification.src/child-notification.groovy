/**
 *  Notification
 *
 *  Copyright 2018 Alexander Belov, Z-Wave.Me
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
	definition (name: "Child Notification", namespace: "z-wave-me", author: "Alexander Belov") {
		capability "Notification"
		capability "Refresh"
		
		attribute "lastUpdated", "String"
	}
	
	tiles(scale: 2) {
		standardTile("switchLogo", "device.switchLogo", inactiveLabel: true, decoration: "flat", width: 1, height: 1) {
			state "default", label:'', icon: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png"
		}
		valueTile("lastUpdated", "device.lastUpdated", decoration: "flat", width: 5, height: 1) {
			state "default", label:'Last updated ${currentValue}'
		}
		multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "idle", label: '${name}', icon: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png", backgroundColor: "#00A0DC"
				attributeState "triggered", label: '${name}', icon: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png", backgroundColor: "#ff0000"
			}
			tileAttribute("device.refresh", inactiveLabel: false, key: "SECONDARY_CONTROL") {
				attributeState "refresh", label: '', action:"refresh.refresh", icon:"st.secondary.refresh"
			}
		}
	}
}

def parse(def description) {
	def cmd = zwave.parse(description)
	
	if (description.startsWith("Err")) {
		createEvent(descriptionText: description, isStateChange:true)
	} else if (description != "updated") {
		zwaveEvent(cmd)
		
		def nowDay = new Date().format("MMM dd", location.timeZone)
		def nowTime = new Date().format("h:mm a", location.timeZone)
		sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def switchState = cmd.event > 0 ? "triggered" : "idle" 
	log.debug "$cmd.event,$cmd.eventParameter, $cmd.notificationStatus, $cmd.sequence"
	
	sendEvent(name: "switch", value: switchState)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationSupportedReport cmd) {
	if (cmd.smoke)           state.notificationType = 1
	if (cmd.co)              state.notificationType = 2
	if (cmd.co2)             state.notificationType = 3
	if (cmd.heat)            state.notificationType = 4
	if (cmd.water)           state.notificationType = 5
	if (cmd.accessControl)   state.notificationType = 6
	if (cmd.burglar)         state.notificationType = 7
	if (cmd.powerManagement) state.notificationType = 8
	if (cmd.system)          state.notificationType = 9
	if (cmd.emergency)       state.notificationType = 10
}

def refresh() {
	if (state.notificationType) {
	   	parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.notificationV3.notificationGet(notificationType: state.notificationType, event: 1).format()))
	} else {
		parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.notificationV3.notificationSupportedGet().format()))
		log.debug "Can't execute refresh. Waiting for Notification Supported Report Command"
	}
}

// to prevent errors on Sensor Binary Reports from the same channel
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {}