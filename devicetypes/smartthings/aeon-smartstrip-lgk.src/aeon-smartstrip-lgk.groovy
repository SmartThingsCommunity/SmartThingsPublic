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
 * lgk add polling and refresh ,
 * add update fx, fix overall power usage to not show in exponential
 * fix last outlet to change color correctly for on / off
 * add labels so you can name each switch
 * fix last switch which will not change color.
 * currently need to use ver 2.07 off app at least on android as 2.08 has a bug with labels being all
 * screwed up below the fold. I know the labels are too big but there is no way unfort. to 
 * only used 1/2 height tiles.. It would be a nice addition especially for cases like this when yo
 * want a whole width label.
 * change scale to 2 for smaller tiles, but for some strange reason had to put blank tile
 * on the end of the label otherwise it screws up and puts weird stuff there.
 
 */
metadata {
	definition (name: "Aeon SmartStrip LGK", namespace: "smartthings", author: "lgkapps") {
		capability "Switch"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Refresh"
		capability "Configuration"
		capability "Actuator"
		capability "Sensor"
        capability "Polling"
        attribute "Label1", "string"
 		attribute "Label2", "string"
	    attribute "Label3", "string"
 		attribute "Lable4", "string"
	    attribute "Label5", "string"
		command "reset"

		(1..4).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			attribute "power$n", "number"
			attribute "energy$n", "number"
			command "on$n"
			command "off$n"
			command "reset$n"
		}

		fingerprint deviceId: "0x1001", inClusters: "0x25,0x32,0x27,0x70,0x85,0x72,0x86,0x60", outClusters: "0x82"
	}


preferences {
 input("SwitchLabel1", "text", title: "Label for Switch 1?", required: false, defaultValue: "Switch 1", description: "Label for the first switch.")  
 input("SwitchLabel2", "text", title: "Label for Switch 2?", required: false, defaultValue: "Switch 2", description: "Label for the second switch.")  
 input("SwitchLabel3", "text", title: "Label for Switch 3?", required: false, defaultValue: "Switch 3", description: "Label for the third switch.")  
 input("SwitchLabel4", "text", title: "Label for Switch 4?", required: false, defaultValue: "Switch 4", description: "Label for the fourth switch.")  
 }

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off":  "command: 2003, payload: 00"
		status "switch1 on": "command: 600D, payload: 01 00 25 03 FF"
		status "switch1 off": "command: 600D, payload: 01 00 25 03 00"
		status "switch4 on": "command: 600D, payload: 04 00 25 03 FF"
		status "switch4 off": "command: 600D, payload: 04 00 25 03 00"
		status "power": new physicalgraph.zwave.Zwave().meterV1.meterReport(
		        scaledMeterValue: 30, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		status "energy": new physicalgraph.zwave.Zwave().meterV1.meterReport(
		        scaledMeterValue: 200, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		status "power1": "command: 600D, payload: 0100" + new physicalgraph.zwave.Zwave().meterV1.meterReport(
		        scaledMeterValue: 30, precision: 3, meterType: 4, scale: 2, size: 4).format()
		status "energy2": "command: 600D, payload: 0200" + new physicalgraph.zwave.Zwave().meterV1.meterReport(
		        scaledMeterValue: 200, precision: 3, meterType: 0, scale: 0, size: 4).format()

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"
	}

	// tile definitions
	tiles (scale: 2){
		standardTile("switch", "device.switch", width: 4, height: 4, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		valueTile("power", "device.power", width: 2, height: 2,decoration: "flat") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", width: 2, height: 2, decoration: "flat") {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", width: 2, height: 2,inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", width:2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		
			standardTile("switch1", "switch1", canChangeIcon: true, width: 2, height: 2) {
				state "on", label: '${name}', action: "off1", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				state "off", label: '${name}', action: "on1", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
			valueTile("power1", "power1",  width: 2, height: 2, decoration: "flat") {
				state "default", label:'${currentValue} W'
			}
			valueTile("energy1", "energy1",  width: 2, height: 2, decoration: "flat") {
				state "default", label:'${currentValue} kWh'
			}
		standardTile("switch2", "switch2", width: 2, height: 2, canChangeIcon: true) {
				state "on", label: '${name}', action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				state "off", label: '${name}', action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
			valueTile("power2", "power2", width: 2, height: 2, decoration: "flat") {
				state "default", label:'${currentValue} W'
			}
			valueTile("energy2", "energy2", width: 2, height: 2, decoration: "flat") {
				state "default", label:'${currentValue} kWh'
			}
		standardTile("switch3", "switch3", width: 2, height: 2, canChangeIcon: true) {
				state "on", label: '${name}', action: "off3", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				state "off", label: '${name}', action: "on3", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
			valueTile("power3", "power3", width: 2, height: 2, decoration: "flat") {
				state "default", label:'${currentValue} W'
			}
			valueTile("energy3", "energy3", width: 2, height: 2, decoration: "flat") {
				state "default", label:'${currentValue} kWh'
			}
		standardTile("switch4", "switch4", width: 2, height: 2, canChangeIcon: true) {
				state "on", label: '${name}', action: "off4", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				state "off", label: '${name}', action: "on4", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
			valueTile("power4", "power4", width: 2, height: 2, decoration: "flat") {
				state "default", label:'${currentValue} W', icon: ""
			}
			valueTile("energy4", "energy4", width: 2, height: 2, decoration: "flat") {
				state "default", label:'${currentValue} kWh', icon: ""
			}
		
  		 valueTile("label1", "device.Label1", width: 4, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}'
            }
        valueTile("label2", "device.Label2", width: 4, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}'
            }
  		valueTile("label3", "device.Label3", width: 4, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}'
            }
  		valueTile("label4", "device.Label4", width: 4, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}'
            }
		standardTile("blank1", "blank1", width: 2, height: 1, decoration: "flat") {
			state "default", label: " "
            }
        standardTile("blank1", "blank1", width: 2, height: 1, decoration: "flat") {
			state "default", label: " "
            }
        standardTile("blank2", "blank2", width: 2, height: 1, decoration: "flat") {
			state "default", label: " "
            }
        standardTile("blank3", "blank3", width: 2, height: 1, decoration: "flat") {
			state "default", label: " "
            }    
        standardTile("blank4", "blank4", width: 2, height: 1, decoration: "flat") {
			state "default", label: " "
            }
		main(["switch", "power", "energy"])
		details(["switch","power","energy",
				 "label1","blank1",
                 "switch1","power1","energy1",
				 "label2","blank2",
                 "switch2","power2","energy2",
				 "label3","blank3",
                 "switch3","power3","energy3",
				 "label4","blank4",
                 "switch4","power4","energy4",
				 "refresh","reset"])
	}
}


def updated()
{
log.debug "in updated"
  response(configure())
}

def parse(String description) {
//log.debug "in parse desc = $description"
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x60: 3, 0x32: 3, 0x25: 1, 0x20: 1])
		if (cmd) {
			result = zwaveEvent(cmd, null)
		}
	}
	log.debug "parsed '${description}' to ${result.inspect()}"
	result
}

def endpointEvent(endpoint, map) {
	if (endpoint) {
		map.name = map.name + endpoint.toString()
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
	if (encapsulatedCommand) {
		if (encapsulatedCommand.commandClassId == 0x32) {
			// Metered outlets are numbered differently than switches
			Integer endpoint = cmd.sourceEndPoint
			if (endpoint > 2) {
            log.debug ""
            def epnew  = endpoint - 2
            log.debug "setting endpoint to $epnew old ep = $ep old endpoint = $endpoint"
				zwaveEvent(encapsulatedCommand, endpoint - 2)
			} else if (endpoint == 0) {
				zwaveEvent(encapsulatedCommand, 0)
			} else {
				log.debug("Ignoring metered outlet $endpoint msg: $encapsulatedCommand")
				[]
			}
		} else {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint) {
//log.debug "in basic report cmd = $cmd ep = $endpoint"

	def map = [name: "switch", type: "physical", value: (cmd.value ? "on" : "off")]
	def events = [endpointEvent(endpoint, map)]
	def cmds = []
	if (endpoint) {
		cmds += delayBetween([2,0].collect { s -> encap(zwave.meterV3.meterGet(scale: s), endpoint) }, 1000)
		if(endpoint < 4) cmds += ["delay 1500", encap(zwave.basicV1.basicGet(), endpoint + 1)]
	} else if (events[0].isStateChange) {
		events += (1..4).collect { ep -> endpointEvent(ep, map.clone()) }
		cmds << "delay 3000"
		cmds += delayBetween((0..4).collect { ep -> encap(zwave.meterV3.meterGet(scale: 2), ep) }, 800)
	}
	if(cmds) events << response(cmds)
	events
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint) {
//log.debug "in binary report cmd = $cmd ep = $endpoint"

	def map = [name: "switch", value: (cmd.value ? "on" : "off")]
	def events = [endpointEvent(endpoint, map)]
	def cmds = []
   // log.debug "events = $events"
   // log.debug "events 0 = $events[0]"
   // log.debug "endpoint = $endpoint "
    def isstatechange = events[0].isStateChange
   // log.debug "is state change = $isstatechange"
    
	if (!endpoint && (isstatechange == true) )
    {
    //sendEvent(events[0])
       log.debug "setting events "
		events += (1..4).collect { ep -> endpointEvent(ep, map.clone()) }
		cmds << "delay 2000"
		cmds += delayBetween((1..4).collect { ep -> encap(zwave.meterV3.meterGet(scale: 2), ep) })
	}
	if(cmds) events << response(cmds)
	events
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep) {
	def event = [:]
	def cmds = []
    log.debug "in meter report cmd = $cmd ep = $ep"
		if (cmd.scale == 0) {
        log.debug " got kwh $cmd.scaledMeterValue"
			event = endpointEvent(ep, [name: "energy", value: cmd.scaledMeterValue, unit: "kWh"])
		} else if (cmd.scale == 1) {
          log.debug "got kvah $cmd.scaledMeterValue"
			event = endpointEvent(ep, [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"])
            }
	//if (cmd.scale < 2) {
          //log.debug " got kwh $cmd.scaledMeterValue"
			//return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	
		//def val = Math.round(cmd.scaledMeterValue*100)/100.0
       // log.debug "val = $val"
		//event = endpointEvent(ep, [name: "energy", value: cmd.scaledMeterValue, unit: ["kWh", "kVAh"][cmd.scale]])
	//} 
    else {
		event = endpointEvent(ep, [name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"])
	}
	if (!ep && event.isStateChange && event.name == "energy") {
		// Total strip energy consumption changed, check individual outlets
		(1..4).each { endpoint ->
			cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
			cmds << "delay 400"
		}
	}
	cmds ? [event, response(cmds)] : event
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, ep) {
	updateDataValue("MSR", String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId))
	null
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep) {
	log.debug "${device.displayName}: Unhandled ${cmd}" + (ep ? " from endpoint $ep" : "")
}

def onOffCmd(value, endpoint = null) {
log.debug "in onoff value = $value swith = $endpoint"
	[
		encap(zwave.basicV1.basicSet(value: value), endpoint),
		"delay 1000",
		encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint),
		"delay 2000",
		encap(zwave.meterV3.meterGet(scale: 2), endpoint)
	]
}

def on() { onOffCmd(0xFF)
  }
  
def off() { onOffCmd(0x0) 
 }

def on1() { onOffCmd(0xFF, 1) }
def on2() { onOffCmd(0xFF, 2) }
def on3() { onOffCmd(0xFF, 3) }
def on4() { onOffCmd(0xFF, 4) }

def off1() { onOffCmd(0, 1) }
def off2() { onOffCmd(0, 2) }
def off3() { onOffCmd(0, 3) }
def off4() { onOffCmd(0, 4) }

def refresh() {
log.debug "in refresh"
	delayBetween([
		zwave.basicV1.basicGet().format(),
		zwave.meterV3.meterGet(scale: 0).format(),
		zwave.meterV3.meterGet(scale: 2).format(),
		encap(zwave.basicV1.basicGet(), 1000)  // further gets are sent from the basic report handler
	])
}

def resetCmd(endpoint = null) {
log.debug "in reset cmd"
	delayBetween([
		encap(zwave.meterV2.meterReset(), endpoint),
		encap(zwave.meterV2.meterGet(scale: 0), endpoint)
	])
}

def reset() {
log.debug "in reset"
	delayBetween([resetCmd(null), reset1(), reset2(), reset3(), reset4()])
}

def reset1() { resetCmd(1) }
def reset2() { resetCmd(2) }
def reset3() { resetCmd(3) }
def reset4() { resetCmd(4) }

def configure() {
log.debug "in configure"

if (settings.SwitchLabel1 == null)
   settings.SwitchLabel1 = ""
if (settings.SwitchLabel2 == null)
   settings.SwitchLabel2 = ""
if (settings.SwitchLabel3 == null)
   settings.SwitchLabel3 = ""
if (settings.SwitchLabel4 == null)
   settings.SwitchLabel4 = ""
 
   sendEvent(name: "Label1", value: settings.SwitchLabel1, descriptionText: "Set switch 1 label: $settings.SwitchLabel1")
   sendEvent(name: "Label2", value: settings.SwitchLabel2, descriptionText: "Set switch 2 label: $settings.SwitchLabel2")
   sendEvent(name: "Label3", value: settings.SwitchLabel3, descriptionText: "Set switch 3 label: $settings.SwitchLabel3")
   sendEvent(name: "Label4", value: settings.SwitchLabel4, descriptionText: "Set switch 4 label: $settings.SwitchLabel4")
      
	def cmds = [
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, configurationValue: [0, 0, 0, 1]).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, configurationValue: [0, 0, 0x79, 0]).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 90).format(),
	]
	[5, 8, 9, 10, 11].each { p ->
		cmds << zwave.configurationV1.configurationSet(parameterNumber: p, size: 2, scaledConfigurationValue: 5).format()
	}
	[12, 15, 16, 17, 18].each { p ->
		cmds << zwave.configurationV1.configurationSet(parameterNumber: p, size: 1, scaledConfigurationValue: 50).format()
	}
	cmds += [
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 15*60).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [1]).format(),
	]
	delayBetween(cmds) + "delay 5000" + refresh()
}

private encap(cmd, endpoint) {
	if (endpoint) {
		if (cmd.commandClassId == 0x32) {
			// Metered outlets are numbered differently than switches
			if (endpoint < 0x80) {
				endpoint += 2
			} else {
				endpoint = ((endpoint & 0x7F) << 2) | 0x80
			}
		}
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}