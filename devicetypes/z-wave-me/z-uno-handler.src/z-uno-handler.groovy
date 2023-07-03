/**
 *  Z-Uno Handler
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
	definition (name: "Z-Uno Handler", namespace: "z-wave-me", author: "Alexander Belov") {
		capability "Refresh"
		capability "Configuration"
	  
		command "associationSet"
		command "parentCommand"
				
		fingerprint mfr: "0115", prod: "0110", model: "0001", inClusters: "0x60"
		fingerprint mfr: "0115", prod: "0111", inClusters: "0x60"
	}

	tiles (scale: 2) {
		childDeviceTiles('all')
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "configure", label:'Update devices', action:"configure", icon:"st.secondary.tools"
		}
		main ([configure])
	}	
}

def parse(String description) {
	def msg = zwave.parse(description)?.format()
	def parts = []
	def name = ""
	parts = msg

	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {			
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
		else {
			log.warn "Unparsed description $description"
		}		
	}
	
	result
}

def installed() {
	command(zwave.multiChannelV3.multiChannelEndPointGet())
}

// EVENTS
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd) {
	def epc = cmd.endPoints
	def cmds = []
	state.epc = epc
	
	for (i in 1..epc) { 
		cmds << command(zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: i))
  	}

	[response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
	def cc = cmd.commandClass
	def ep = cmd.endPoint
	def needCreate = null
	
	if (!childDevices.find{ it.deviceNetworkId.endsWith("-ep${ep}") || !childDevices}) {
		createChildDevices(cc, ep)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def ep = cmd.sourceEndPoint
	def childDevice = null
	
	childDevices.each {
		if (it.deviceNetworkId =="${device.deviceNetworkId}-ep${ep}") {
			childDevice = it
		}
	}
	
	if (childDevice) {
		log.debug "Parse ${childDevice.deviceNetworkId}, cmd: ${cmd}"
		childDevice.parse(cmd.encapsulatedCommand().format())
	} else {
		log.debug "Child device not found.cmd: ${cmd}"
	}
}

// To ignore reports from channel 0
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) { createEvent(descriptionText: "Unallocated Sensor Multilevel Report: $cmd.scaledValue") }
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)         { createEvent(descriptionText: "Unallocated Switch Binary Report: $cmd.value") }
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) { createEvent(descriptionText: "Unallocated Switch Multilevel Report: $cmd.value") }
def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd)                       { createEvent(descriptionText: "Unallocated Meter Report: $cmd.scaledMeterValue") }
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd)         { createEvent(descriptionText: "Unallocated Sensor Binary Report: cmd.event") }
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)         { createEvent(descriptionText: "Unallocated Notification Report: cmd.event") }

// handle commands
void createChildDevices(def cc, def ep) {
	try {
		def deviceCCHandler = ""
		def deviceCCType = ""
		
		for (def i = 0; i < cc.size(); i++) {
			switch (cc[i]) {
				case 0x26: 
					deviceCCType = "Multilevel Switch"
					deviceCCHandler = "Child Multilevel Switch"
					break
					
				case 0x25: 
					deviceCCType =  "Binary Switch"
					deviceCCHandler = "Child Binary Switch"
					break
					
				case 0x31: 
					deviceCCType = "Multilevel Sensor"
					deviceCCHandler = "Child Multilevel Sensor"
					break
					
				case 0x32:
					deviceCCType = "Meter"
					deviceCCHandler = "Child Meter"
					break
					
				case 0x71: 
			   		deviceCCType = "Notification"
					deviceCCHandler = "Child Notification"
					break
					
				case 0x40:					
				case 0x43: 
					deviceCCType = "Thermostat"
					deviceCCHandler = "Child Thermostat"
					break

				default:
					log.debug "No Child Device Handler case for command class: '$cc'"
			}
			
			// stop on the first matched CC
			if (deviceCCHandler != "") break
		}
		
		if (deviceCCHandler != "") {
			try {
				addChildDevice(deviceCCHandler, "${device.deviceNetworkId}-ep${ep}", null,
								[completedSetup: true, label: "${deviceCCType}-${ep}", 
								isComponent: false, componentName: "${deviceCCType}-${ep}", componentLabel: "${deviceCCType}-${ep}"])
			} catch (e) {
				log.error "Creation child devices failed with error = ${e}"
			}
		}
		
		associationSet()
	} catch (e) {
		log.error "Child device creation failed with error = ${e}"
	}
}

def associationSet() {
	def cmds = []
	def multiChannelAssociationCC = "8E"
	def setCmd =                    "01"
	def groupingIdentifier =        "01"
	def marker =                    "00"
	def nodeId =                    prependZero(zwaveHubNodeId)
	def ep =                        "00"
	
	cmd << "${multiChannelAssociationCC}${setCmd}${groupingIdentifier}${marker}${nodeId}${ep}"

	return cmd	
}

def command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

def parentCommand(def cmd) {
	"${cmd}"
}

def encap(def source, def destination, def cmd) {
	def multiChannel =  "60"
	def cmdEncap =      "0D"
	def sourceEP =      prependZero(source)
	def destinationEP = prependZero(destination)

	return "${multiChannel}${cmdEncap}${sourceEP}${destinationEP}${cmd}"
}

def extractEP(def s) {
	def result = null
	
	if (contains(s, "-")) {
		result = s.substring(s.length() - (s.indexOf("-") - 1)) 
	}
	return result as Integer
}

boolean contains(def s, def ss) {
	boolean result = false
	
	if (s != null && !s.isEmpty()) {
		result = s.matches(".*${ss}.*")
	}
	return result
}

def prependZero(def s) {
	if (s > 9)
		return s
	else
		return "0$s"
}