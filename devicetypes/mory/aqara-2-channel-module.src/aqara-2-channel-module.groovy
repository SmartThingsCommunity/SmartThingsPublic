/**
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
 *  03/2020 first release
 */

metadata {
    definition (name: "Aqara 2 Channel Module", namespace: "mory", author: "Motysoft") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Health Check"
		capability "Light"
		capability "Configuration"
		capability "Refresh"
        
        command "childOn"
		command "childOff"
        
        command "comInstalled"
        command "comRecreateChildren"
        
        fingerprint profileId: "0104", 
        	inClusters: "0000, 0001, 0003, 0004, 0006, 0008", 
            outClusters: "0001, 0006, 0008, 000A, 0019", 
            model: "lumi.relay.c2acn01", 
            deviceJoinName: "Aqara Module L1",
            manufacturer: "LUMI"
    }

    tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
		}
        
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
        
		main "switch"
		details(["switch", "refresh"])
    }
}

def installed() {
    log.debug "Installed"
    
    createChildDevices()
    updateDataValue("onOff", "catchall")
    refresh()
}

def updated() {
    log.debug "updated()"
    updateDataValue("onOff", "catchall")
    refresh()
}

private void createChildDevices() {
    log.debug "Adding children"
    
    log.debug addChildDevice("Aqara 2 Channel Module (Child, L2)", "${device.deviceNetworkId}:02", device.hubId,
			[completedSetup: true, label: "${device.displayName[0..-4]} L2", isComponent: false])
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "└──────────────────────────────────────────────────────"
   	log.debug "parse.description '${description}'"
    
	Map eventMap = zigbee.getEvent(description)
	Map eventDescMap = zigbee.parseDescriptionAsMap(description)

   	log.debug "parse.eventMap ${eventMap}"
    log.debug "parse.eventDescMap ${eventDescMap}"

	if (!eventMap && eventDescMap) {
		eventMap = [:]
		if (eventDescMap?.clusterId == zigbee.ONOFF_CLUSTER) {
			eventMap[name] = "switch"
			eventMap[value] = eventDescMap?.value
		}
	}

	if (eventMap) {
		def endpoint = eventDescMap?.sourceEndpoint != null ? eventDescMap?.sourceEndpoint : eventDescMap?.endpoint
    
		if (endpoint == "01") {
			sendEvent(eventMap)
		} else if (endpoint == "02") {
			def childDevice = childDevices.find {
				it.deviceNetworkId == "$device.deviceNetworkId:${endpoint}"
			}
            
			if (childDevice) {
				childDevice.sendEvent(eventMap)
			} else {
				log.debug "Child device: $device.deviceNetworkId:${endpoint} was not found"
			}
		}
	}
	log.debug "┌──────────────────────────────────────────────────────"
}

def on() {
	log.debug("L1 on")
	zigbee.command(zigbee.ONOFF_CLUSTER, 0x01, "", [destEndpoint: 1])
}

def off() {
	log.debug("L1 off")
	zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: 1])
}

def childOn() {
	log.debug("L2 on")
	zigbee.command(zigbee.ONOFF_CLUSTER, 0x01, "", [destEndpoint: 2])
}

def childOff() {
	log.debug("L2 off")
	zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: 2])
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
    def cmds = zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: 2])
    cmds += zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: 3])
    return cmds
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
		childDevices.each {
			it.sendEvent(healthEvent)
		}
		state.hasConfiguredHealthCheck = true
	}
}

def configure() {
	log.debug "configure()"
	configureHealthCheck()

    //other devices supported by this DTH in the future
    def cmds = zigbee.onOffConfig(0, 120)
    
    for (i in 1..2) {
        cmds += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, 0x10, 0, 120, null, [destEndpoint: i])
    }
    
    cmds += refresh()
    
    return cmds
}

def uninstalled() {
	childDevices.each {
		try {
        	log.debug "Deleting ${it.deviceNetworkId}"
			deleteChildDevice(it.deviceNetworkId)
            log.debug "Deleted ${it.deviceNetworkId}"
		}
		catch (e) {
			log.debug "Error deleting ${it.deviceNetworkId}: ${e}"
		}
	}
}

def comInstalled() {
    installed()
}

def comRecreateChildren() {
	uninstalled()

    installed()
}