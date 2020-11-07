/**
 *  Tuya Window Shade (v.0.1.0)
 *	Copyright 2020 iquix
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 This DTH is coded based on iquix's tuya-window-shade DTH.
 https://github.com/iquix/Smartthings/blob/master/devicetypes/iquix/tuya-window-shade.src/tuya-window-shade.groovy
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Ajax Online Zigbee Blind1", namespace: "Ajax Online", author: "Ajax Online", ocfDeviceType: "oic.d.blind", vid: "generic-shade") {
		capability "Actuator"
		capability "Configuration"
		capability "Window Shade"
		capability "Window Shade Preset"
		capability "Switch Level"
		capability "Sensor"

		command "pause"
		command "levelOpenClose"
        
        attribute "Direction", "enum", ["Reverse","Forward"]
        attribute "OCcommand", "enum", ["Replace","Original"]
        attribute "stapp", "enum", ["Reverse","Forward"]
        attribute "remote", "enum", ["Reverse","Forward"]

		fingerprint endpointId: "0x01", profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TYST11_wmcdj3aq", model: "mcdj3aq", deviceJoinName: "Ajax Online Zigbee Blind"
		fingerprint endpointId: "0x01", profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TYST11_cowvfr", model: "owvfni3", deviceJoinName: "Ajax Online Zigbee Blind"
		fingerprint endpointId: "0x01", profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019", deviceJoinName: "Ajax Online Zigbee Blind"
		fingerprint endpointId: "0x01", profileId: "0104", inClusters: "0000, 000A, 0004, 0005, EF00", outClusters: "0019", manufacturer: "_TZE200_wmcdj3aq", model: "TS0601", deviceJoinName: "Ajax Online Zigbee Blind"
        fingerprint endpointId: "0x01", profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019", manufacturer: " _TYST11_uzinxci0", model: "zinxci0", deviceJoinName: "Ajax Online Zigbee Blind"
	}

	preferences {
		input "preset", "number", title: "Preset position", description: "Set the window shade preset position", defaultValue: 50, range: "0..100", required: false, displayDuringSetup: false
        input name: "Direction", type: "enum", title: "Direction Set", options:["01": "Reverse", "00": "Forward"], required: true, displayDuringSetup: true
        input name: "OCcommand", type: "enum", title: "Replace Open and Close commands", options:["2": "Replace", "0": "Original"], required: true, displayDuringSetup: true
        input name: "stapp", type: "enum", title: "app opening,closing Change", options:["2": "Reverse", "0": "Forward"], required: true, displayDuringSetup: true
        input name: "remote", type: "enum", title: "RC opening,closing Change", options:["1": "Reverse", "0": "Forward"], required: true, displayDuringSetup: true
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"windowShade", type: "generic", width: 6, height: 4) {
			tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label: 'Open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "closing"
				attributeState "closed", label: 'Closed', action: "open", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "opening"
				attributeState "partially open", label: 'Partially open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#d45614", nextState: "closing"
				attributeState "opening", label: 'Opening', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "closing"
				attributeState "closing", label: 'Closing', action: "open", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "opening"
			}
		}
		standardTile("contPause", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "pause", label:"", icon:'st.sonos.pause-btn', action:'pause', backgroundColor:"#cccccc"
		}
		standardTile("presetPosition", "device.presetPosition", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Preset", action:"presetPosition", icon:"st.Home.home2"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("shadeLevel", "device.level", width: 4, height: 1) {
			state "level", label: 'Shade is ${currentValue}% up', defaultState: true
		}
		controlTile("levelSliderControl", "device.level", "slider", width:2, height: 1, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}

		main "windowShade"
		details(["windowShade", "contPause", "presetPosition", "shadeLevel", "levelSliderControl", "refresh"])
	}
}

private getCLUSTER_TUYA() { 0xEF00 }
private getSETDATA() { 0x00 }

// Parse incoming device messages to generate events
def parse(String description) {
	if (description?.startsWith('catchall:') || description?.startsWith('read attr -')) {
		Map descMap = zigbee.parseDescriptionAsMap(description)        
		if (descMap?.clusterInt==CLUSTER_TUYA) {
        	log.debug descMap
			if ( descMap?.command == "01" || descMap?.command == "02" ) {
				def dp = zigbee.convertHexToInt(descMap?.data[3]+descMap?.data[2])
                log.debug "dp = " + dp
				switch (dp) {
					case 1025: 
                        def parData = descMap.data[6] as int
                        if(parData != 1){
                        def stappVal = (stapp ?:"0") as int
                        def data = Math.abs(parData - stappVal)
						sendEvent([name:"windowShade", value: (data == 0 ? "opening":"closing"), displayed: true])
                        log.debug "App control=" + (data == 0 ? "opening":"closing")
                        }
                    	break
					case 1031: // 0x04 0x07: Confirm opening/closing/stopping (triggered from remote)
                        def parData = descMap.data[6] as int
                        def remoteVal = remote as int
                        def data = Math.abs(parData - remoteVal)
						sendEvent([name:"windowShade", value: (data == 0 ? "opening":"closing"), displayed: true])
                        log.debug "Remote control=" + (data == 0 ? "opening":"closing")
                    	break
					case 514: // 0x02 0x02: Started moving to position (triggered from Zigbee)
                    	def setLevel = zigbee.convertHexToInt(descMap.data[9])
                        def lastLevel = device.currentValue("level")
						sendEvent([name:"windowShade", value: (setLevel >= lastLevel ? "opening":"closing"), displayed: true])
                        log.debug "Remote control=" + (setLevel >= lastLevel ? "opening":"closing")
                        break
					case 515: // 0x02 0x03: Arrived at position
                    	def pos = zigbee.convertHexToInt(descMap.data[9])
                    	log.debug "arrived at position :"+pos
                    	if (pos > 0 && pos <100) {
                        	sendEvent(name: "windowShade", value: "partially open")
                        } else {
                        	sendEvent([name:"windowShade", value: (pos == 100 ? "open":"closed"), displayed: true])
                        }
                        sendEvent(name: "level", value: (pos))
                        break
				}
			}
		}
	}
}

def close() {
	log.info "close()"
    def cm = (OCcommand ?:"0") as int
    log.debug "cm=${cm}"
    def val = Math.abs(2 - cm)
    sendTuyaCommand("0104", "00", "0101")
	
}

def open() {
	log.info "open()"
    def cm = (OCcommand ?:"0") as int
    def val = Math.abs(0 - cm)
	sendTuyaCommand("0104", "00", "010" + val)
}

def pause() {
	log.info "pause()"
	sendTuyaCommand("0104", "00", "010" + val)
}

def setLevel(data, rate = null) {
	log.info "setLevel("+data+")"
    def currentLevel = device.currentValue("level")
    if (currentLevel == data) {
    	sendEvent(name: "level", value: currentLevel, displayed: true)
        return
    }
	sendTuyaCommand("0202", "00", "04000000"+zigbee.convertToHexString(data, 2))
}

def refresh() {
	zigbee.readAttribute(CLUSTER_TUYA, 0x00, )
}

def presetPosition() {
    setLevel(preset ?: 50)
}

def installed() {
	sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(["open", "close", "pause"]), displayed: false)
}

def updated() {
	log.debug "val(${Direction}),valC(${OCcommand}),valR(${remote})"
	DirectionSet(Direction ?:"00")
}	

def DirectionSet(Dval) {
	log.info "Dset(${Dval})"
   sendHubCommand(zigbee.command(CLUSTER_TUYA, SETDATA, "00" + zigbee.convertToHexString(rand(256), 2) + "05040001" + Dval))
}

def configure() {
	log.info "configure()"
}

private sendTuyaCommand(dp, fn, data) {
	log.info "${zigbee.convertToHexString(rand(256), 2)}=${dp},${fn},${data}"
	zigbee.command(CLUSTER_TUYA, SETDATA, "00" + zigbee.convertToHexString(rand(256), 2) + dp + fn + data)
}

private rand(n) {
	return (new Random().nextInt(n))
}