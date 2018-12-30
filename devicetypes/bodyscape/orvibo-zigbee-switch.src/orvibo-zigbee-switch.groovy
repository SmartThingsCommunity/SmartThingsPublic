/**
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
 
metadata {
	definition (name: "Orvibo ZigBee Switch", namespace: "bodyscape", author: "bodyscape", ocfDeviceType: "oic.d.switch",  minHubCoreVersion: '000.019.00012', executeCommandsLocally: true) {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Health Check"

		fingerprint profileId: "0104", inClusters: "0000, 0005, 0004, 0006", outClusters: "0000", manufacturer: "ORVIBO", model: "074b3ffba5a045b7afd94c47079dd553", deviceJoinName: "Orvibo Switch"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "switch"
		details(["switch", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	Map map = zigbee.getEvent(description)
	if (map) {
		if (description?.startsWith('on/off')) {
			log.debug "receive on/off message without endpoint id"
			sendHubCommand(refresh().collect { new physicalgraph.device.HubAction(it) }, 0)
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterId == "0006" && descMap.sourceEndpoint == "01") {
				log.debug "$descMap"
				sendEvent(map)
			} else  {
				log.warn "DID NOT PARSE MESSAGE for description : $description"
				log.debug "$descMap"
			}
		}
	}else {
		Map descMap = zigbee.parseDescriptionAsMap(description)
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug "$descMap"
	}
}

def installed() {
	updateDataValue("onOff", "catchall")
}

def updated() {
	log.debug "updated()"
	updateDataValue("onOff", "catchall")
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
	return zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0xFF])
}

def poll() {
	refresh()
}

def healthPoll() {
	log.debug "healthPoll()"
	def cmds = refresh()
	cmds.each { sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def configureHealthCheck() {
	Integer hcIntervalMinutes = 12
	if (!state.hasConfiguredHealthCheck) {
		log.debug "Configuring Health Check, Reporting"
		unschedule("healthPoll")
		runEvery5Minutes("healthPoll")
		def healthEvent = [name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
		// Device-Watch allows 2 check-in misses from device
		sendEvent(healthEvent)
		state.hasConfiguredHealthCheck = true
	}
}

def configure() {
	log.debug "configure()"
	configureHealthCheck()
	//the orvibo switch will send out device anounce message at ervery 2 mins as heart beat,setting 0x0099 to 1 will disable it.
	return zigbee.writeAttribute(0x0000, 0x0099, 0x20, 0x01, [mfgCode: 0x0000])
}