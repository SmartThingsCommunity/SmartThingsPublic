/**
 *  Copyright 2015 SmartThings
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
 *  Version 1.00 : Initial Release
 *  Version 1.01 : Better online/offline verification - Responds in 15 seconds
 *  Version 1.02 : Code optimizations, checks twice for offline, responds in 10 seconds
 *  Version 1.03 : Code Fixes, for better results changed back to 15 seconds
 *  Version 1.04 : Will only report in recently when status changes or manual refresh
 *  Version 1.05 : Updated color to match SmartThings changes, change updated() to configure(), create force reconfigure
 *  Version 1.06 : Increased runIn to 60.  Decrease false positives
 *  Version 2.00 : Algorithm change to fix Health Check false positives
 *  Version 2.00a: Bug fix
 */
metadata {
	definition (name: "Z-Wave Repeater", namespace: "smartthings", author: "jhamstead") {
		capability "Health Check"
		capability "Refresh"
        capability "Configuration"

		fingerprint inClusters: "0x", deviceJoinName: "Z-Wave Repeater"
	}

	// simulator metadata
	simulator {

	}

	// tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "status", type: "generic", width: 6, height: 4) {
			tileAttribute("device.status", key: "PRIMARY_CONTROL") {
                attributeState "unknown", label: 'unknown', icon: "st.motion.motion.inactive", backgroundColor: "#ffffff"
				attributeState "online", label: 'online', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
				attributeState "offline", label: 'offline', icon: "st.motion.motion.inactive", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        standardTile("reconfigure", "device.reconfigure", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Force Reconfigure', action:"configure", icon:"st.secondary.refresh"
		}

		main "status"
		details(["status","refresh","reconfigure"])
	}
}

def configure() {
    state.failedTries = 0
    unschedule()
    runEvery15Minutes(sendRequest)
    // Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    log.debug "configure() - checkinterval duration of 32min"
    refresh()
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x70: 1])
	if (cmd) {
		zwaveEvent(cmd)
	}
	log.debug "Parse returned ${cmd}"
	//return result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    Map myMap = [name: "status", isStateChange: false, displayed: false, value: 'online', descriptionText: "$device.displayName is online" ]
    if (device.currentValue('status') != 'online' || state.manualPress ) {
       myMap.displayed = true
       myMap.isStateChange = true
    }
    state.failedTries = 0
    state.manualPress = false
    log.info "Device is online"
    sendEvent(myMap)
    
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)

}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    sendRequest()
}

def refresh() {
    state.manualPress = true
    log.debug "refresh() - Manual refresh"
    sendRequest()
}

// Private methods
def sendRequest() {
    Map myMap = [name: "status", isStateChange: true, displayed: true, value: 'offline', descriptionText: "$device.displayName is offline" ]
    if (state.failedTries >= 2) {
        if ( device.currentValue('status') != 'offline' || state.manualPress ) {
           log.debug "${myMap}"
           sendEvent(myMap)
           state.manualPress = false
        }
        log.info "Device is offline"
    }
    state.failedTries = state.failedTries + 1 
	sendHubCommand([value].collect {new physicalgraph.device.HubAction(zwave.manufacturerSpecificV1.manufacturerSpecificGet().format())})
}