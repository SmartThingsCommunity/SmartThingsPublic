/**
 *  zunoChildDevice
 *
 *  Copyright 2018 Alexander Belov
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
	definition (name: "zunoParentHandler", namespace: "baadev", author: "Alexander Belov") {
        capability "Refresh"
        capability "Configuration"
      
      	command "initMC"
        command "debug"
        command "associationSet"
                
        fingerprint mfr: "0115", prod: "0110", model: "0001", inClusters: "0x60"    
	}

	tiles (scale: 2) {
        childDeviceTiles('all')
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "configure", label:'Configure', action:"configure", icon:"st.secondary.tools"
        }
        standardTile("associationSet","device.associationSet", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "associationSet", label:'Association set', action:"associationSet", icon:"st.secondary.tools"
        }
        
        main ([configure])
    }    
}

// parse events into attributes
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

// EVENTS
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd)
{
	def epc = cmd.endPoints
    def cmds = []
    state.epc = epc
    
    for (i in 1..epc) { 
        cmds << command(zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: i))
  	}

	def event = createEvent(descriptionText: "${device.displayName} have $epc EndPoints")

    log.debug "${device.displayName} have $epc EndPoints. Cmds: ${cmds}"
    [event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd)
{
	log.debug cmd
	def cc = cmd.commandClass
    def ep = cmd.endPoint
    createChildDevices(cc, ep)
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
        log.debug ""
        childDevice.parse(cmd.encapsulatedCommand().format())
    } else {
        log.debug "Child device not found.cmd: ${cmd}"
    }
}
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
}
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
}

// handle commands
private void createChildDevices(def cc, def ep) {
	try
    {
    	def deviceCCHandler = ""
        def deviceCCType = ""
        for (def i = 1; i <= cc.size(); i++) {
            switch (cc[i - 1]) {
                case 0x26: 
                	deviceCCType = "Multilevel Switch"
                	deviceCCHandler = "Child Multilevel Switch"
                	log.debug "case '0x26'"
                	break;
                case 0x25: 
                	deviceCCType =  "Binary Switch"
                	deviceCCHandler = "Child Binary Switch"
                    log.debug "case '0x25'"
                    break;
                case 0x31: 
                	deviceCCType = "Multilevel Sensor"
                	deviceCCHandler = "Child Multilevel Sensor"
                    log.debug "case '0x31'"
                    break;
                case 0x71: 
               		deviceCCType = "Notification"
                	deviceCCHandler = "Child Notification"
                    log.debug "case '0x71'"
                    break;
                case 0x40:
                	deviceCCType = "Thermostat"
                	deviceCCHandler = "Child Thermostat"
                    log.debug "case '0x40'"
                    break;
                case 0x43: 
                	deviceCCType = "Thermostat"
                	deviceCCHandler = "Child Thermostat"
                    log.debug "case '0x43'"
                    break;

                default:
                    log.debug "No Child Device Handler case for command class: '$cc'"
        	}
			if(deviceCCHandler != "") 
        		break;
        }
        
        if(deviceCCHandler != "") {
        	try {
            	addChildDevice(deviceCCHandler, "${device.deviceNetworkId}-ep${ep}", null,
            				[completedSetup: true, label: "${device.displayName} (${deviceCCType}-${ep})", 
                             isComponent: false, componentName: "${deviceCCType}-${ep}", componentLabel: "${deviceCCType} ${ep}"])
            }
        	catch (e) {
            log.error "Creation child devices failed with error = ${e}"
            }
        }
        
        associationSet()
    }
    catch (e) {
        log.error "Child device creation failed with error = ${e}"
        state.alertMessage = "Child device creation failed. Please make sure that the '${deviceCCHandler}' is installed and published."
        runIn(2, "sendAlert")
    }
}

private sendAlert() {
   sendEvent(
      descriptionText: state.alertMessage,
	  eventType: "ALERT",
	  name: "childDeviceCreation",
	  value: "failed",
	  displayed: true,
   )
}
def configure() {
	log.debug "Executing 'configure()'"
	initMC()
}

def initMC() {
	command(zwave.multiChannelV3.multiChannelEndPointGet())
}

def associationSet() {
	def cmds = []
	def multiChannelAssociationCC = 		"8E"
    def setCmd = 							"01"
	def groupingIdentifier = 				"01"
    def marker = 							"00"
    def ep = 								"00"
    def nodeId = zwaveHubNodeId > 9 ? zwaveHubNodeId : "0${zwaveHubNodeId}"
	
    if (state.epc > 1) {
        for (byte i = 1; i <= state.epc; i++) {
        	def multiChannelNodeId = i > 9 ? i : "0${i}"
            cmds << "${multiChannelAssociationCC}${setCmd}${groupingIdentifier}${nodeId}${marker}${multiChannelNodeId}${ep}"
        }
    }
    //cmds << "${encap(extractEP("ad-ep1"), zwaveHubNodeId, zwave.sensorMultilevelV5.sensorMultilevelGet(scale: 0, sensorType: 1))}"
	
    log.warn cmds
    delayBetween(cmds, 100)
    return cmds    
}

def command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

def encap(def source, def destination, def cmd) {
	def result = null
    if (source || destination) {
        def multiChannel = 						"60"
        def cmdEncap =			 				"0D"
        def sourceEP = 							source > 9 ? source : "0${source}"
        def destinationEP = 					destination > 9 ? destination : "0${destination}"

    	result = "${multiChannel}${cmdEncap}${sourceEP}${destinationEP}${cmd.format()}"
	}
    log.debug result
    return result
}

def extractEP(def s) {
	def result = null
	if (contains(s, "-")) {
    	result = s.substring(s.length() - (s.indexOf("-") - 1)) 
    }
	return result as Integer
}

private boolean contains(String s, def ss) {
    boolean contains = false
    log.debug "Search ${ss} in ${s} string"
    if (s != null && !s.isEmpty()) {
		contains = s.matches(".*${ss}.*")
    }
    return contains
}

