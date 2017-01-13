/**
 *  Enerwave 8-Button Scene Controller ZWN-SC8
 *  Copyright 2015 Eric Maycock
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
	definition (name: "Enerwave 8-Button Scene Controller ZWN-SC8", namespace: "erocm123", author: "Eric Maycock") {
		capability "Actuator"
		capability "Button"
		capability "Configuration"
		capability "Sensor"
        capability "Switch"
        capability "Refresh"
        
        attribute "numberOfButtons", "number"
        
        (1..8).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			command "on$n"
			command "off$n"
		}

		fingerprint deviceId: "0x1801", inClusters: "0x5E,0x86,0x72,0x5A,0x73,0x85,0x59,0x5B,0x87,0x20,0x7A"
	}

	simulator {
	}
	tiles (scale: 2) {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
		}
        standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        (1..8).each { n ->
			standardTile("switch$n", "switch$n", canChangeIcon: true, width: 2, height: 2) {
				state "off", label: "switch$n", action: "on$n", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
                state "on", label: "switch$n", action: "off$n", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			}
		}
        
		main "button"
		details(["button", "configure", "refresh",
                 "switch1", "switch2", "switch3", "switch4",
                 "switch5", "switch6", "switch7", "switch8"])
	}
    
    preferences {
       input name: "enableDebugging", type: "boolean", title: "Enable Debug?", defaultValue: false, displayDuringSetup: false, required: false
    }
}

def parse(String description) {
	def results = []
    if (settings.debug == true) log.debug "${description}"
	if (description.startsWith("Err")) {
	    results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [0x2B: 1, 0x80: 1, 0x84: 1])
		if(cmd) results += zwaveEvent(cmd)
		if(!results) results = [ descriptionText: cmd, displayed: false ]
	}
    
    if(state.isConfigured != "true") configure()
    
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
        
        switch (cmd.value) {
           case 1:
              buttonEvent(1, "pushed")
           break
           case 2:
              buttonEvent(2, "pushed")
           break
           case 4:
              buttonEvent(3, "pushed")
           break
           case 8:
              buttonEvent(4, "pushed")
           break
           case 16:
              buttonEvent(5, "pushed")
           break
           case 32:
              buttonEvent(6, "pushed")
           break
           case 64:
              buttonEvent(7, "pushed")
           break
           case 128:
              buttonEvent(8, "pushed")
           break
           default:
              log.debug "Unhandled CentralSceneNotification: ${cmd}"
           break
        }
}

def buttonEvent(button, value) {
	createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled zwaveEvent: ${cmd}"
}

def installed() {
    log.debug "installed()"
    configure()
}

def updated() {
    state.enableDebugging = settings.enableDebugging
    log.debug "updated()"
    configure()
}

def configure() {
	log.debug "configure()"
    sendEvent(name: "numberOfButtons", value: 8, displayed: true)
    state.isConfigured = "true"
}

def onCmd(endpoint = null) {
    logging("onCmd($endpoint)")
    if (endpoint != null) {
	   zwave.indicatorV1.indicatorSet(value:(2 ^ (endpoint - 1))).format()
    } else {
       zwave.indicatorV1.indicatorSet(value:255).format()
    }
}

def offCmd() {
    logging("offCmd($value, $endpoint)")
	zwave.indicatorV1.indicatorSet(value:0).format()
}

def on() { onCmd() }
def off() { offCmd() }

def on1() { onCmd(1) }
def on2() { onCmd(2) }
def on3() { onCmd(3) }
def on4() { onCmd(4) }
def on5() { onCmd(5) }
def on6() { onCmd(6) }
def on7() { onCmd(7) }
def on8() { onCmd(8) }

def off1() { offCmd() }
def off2() { offCmd() }
def off3() { offCmd() }
def off4() { offCmd() }
def off5() { offCmd() }
def off6() { offCmd() }
def off7() { offCmd() }
def off8() { offCmd() }

def refresh() {
    logging("refresh")
	def cmds = [
		zwave.basicV1.basicGet().format(),
		zwave.meterV3.meterGet(scale: 0).format(),
		zwave.meterV3.meterGet(scale: 2).format(),
		encap(zwave.basicV1.basicGet(), 1)  // further gets are sent from the basic report handler
	]
            cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), null)
    (1..4).each { endpoint ->
            cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
    }
    (1..6).each { endpoint ->
			cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
            cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
	}
    [90, 101, 102, 111, 112].each { p ->
           cmds << zwave.configurationV1.configurationGet(parameterNumber: p).format()
    }
    delayBetween(cmds, 1000)
}

private def logging(message) {
    if (state.enableDebugging == "true") log.debug message
}