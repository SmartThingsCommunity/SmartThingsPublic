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
	definition (name: "Enerwave 8-Button Scene Controller ZWN-SC8", namespace: "erocm123", author: "Eric Maycock", vid: "generic-button") {
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
			standardTile("switch$n", "switch$n", canChangeIcon: true, width: 2, height: 2, decoration: "flat") {
				state "off", label: "switch$n", action: "on$n", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
                state "on", label: "switch$n", action: "off$n", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
			}
		}
        
		main "button"
		details(["switch1", "switch2", "button", 
                 "switch3", "switch4", "configure",
                 "switch5", "switch6", "refresh",
                 "switch7", "switch8"])
	}
    
    preferences {
       input name: "sendScene", type: "boolean", title:"Send button event when activating switch (1-8)", required:false, displayDuringSetup:true
       input name: "enableDebugging", type: "boolean", title: "Enable Debug?", defaultValue: false, displayDuringSetup: false, required: false
    }
}

def parse(String description) {
	def results = []
    //logging("${description}")
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

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
        logging("keyAttributes: $cmd.keyAttributes")
        logging("sceneNumber: $cmd.sceneNumber")
        logging("sequenceNumber: $cmd.sequenceNumber")

        sendEvent(name: "sequenceNumber", value: cmd.sequenceNumber, displayed:false)
        buttonEvent(cmd.sceneNumber, "pushed")
}

def zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
        logging("IndicatorReport: $cmd")
        switch (cmd.value) {
           case 1:
              toggleTiles("switch1")
           break
           case 2:
              toggleTiles("switch2")
           break
           case 4:
              toggleTiles("switch3")
           break
           case 8:
              toggleTiles("switch4")
           break
           case 16:
              toggleTiles("switch5")
           break
           case 32:
              toggleTiles("switch6")
           break
           case 64:
              toggleTiles("switch7")
           break
           case 128:
              toggleTiles("switch8")
           break
           default:
              logging("Unhandled IndicatorReport: ${cmd}")
           break
        }
}

def buttonEvent(button, value) {
	createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logging("Unhandled zwaveEvent: ${cmd}")
}

private toggleTiles(value) {
   def tiles = ["switch1", "switch2", "switch3", "switch4", "switch5", "switch6", "switch7", "switch8"]
   tiles.each {tile ->
      if (tile != value) { sendEvent(name: tile, value: "off") }
      else { sendEvent(name:tile, value:"on"); sendEvent(name:"switch", value:"on") }
   }
}

def installed() {
    logging("installed()")
    configure()
}

def updated() {
    state.enableDebugging = settings.enableDebugging
    logging("updated()")
    configure()
}

def configure() {
	logging("configure()")
    sendEvent(name: "numberOfButtons", value: 8, displayed: true)
    state.isConfigured = "true"
}

def onCmd(endpoint = null) {
    logging("onCmd($endpoint)")
    toggleTiles("switch$endpoint")
    if (endpoint != null) {
       if (sendScene == "true") sendEvent(name: "button", value: "pushed", data: [buttonNumber: endpoint], descriptionText: "$device.displayName button $endpoint was pushed", isStateChange: true)
       zwave.indicatorV1.indicatorSet(value:(2.power(endpoint - 1))).format()
    } else {
       zwave.indicatorV1.indicatorSet(value:255).format()
    }
}

def offCmd(endpoint = null) {
    logging("offCmd($value, $endpoint)")
    if (endpoint != null) {
       if (sendScene == "true") { 
          sendEvent(name: "button", value: "pushed", data: [buttonNumber: endpoint], descriptionText: "$device.displayName button $endpoint was pushed", isStateChange: true)
          sendEvent(name: "switch$endpoint", value: "on", isStateChange: true)
       }
       zwave.indicatorV1.indicatorSet(value:(2.power(endpoint - 1))).format()
    } else {
       zwave.indicatorV1.indicatorSet(value:0).format()
    }
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

def off1() { offCmd(1) }
def off2() { offCmd(2) }
def off3() { offCmd(3) }
def off4() { offCmd(4) }
def off5() { offCmd(5) }
def off6() { offCmd(6) }
def off7() { offCmd(7) }
def off8() { offCmd(8) }

def refresh() {
    logging("refresh()")
	zwave.indicatorV1.indicatorGet().format()
}

private def logging(message) {
    if (state.enableDebugging == "true") log.debug message
}
