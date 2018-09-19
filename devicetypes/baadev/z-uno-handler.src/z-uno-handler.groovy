/**
 *  Z-Uno Handler
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
	definition (name: "Z-Uno Handler", namespace: "baadev", author: "Alexander Belov") {
        capability "Refresh"
        capability "Configuration"
      
        command "debug"
        command "associationSet"
        command "parentCommand"
                
        fingerprint mfr: "0115", prod: "0110", model: "0001", inClusters: "0x60"    
	}

	tiles (scale: 2) {
        childDeviceTiles('all')
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "configure", label:'Update devices', action:"configure", icon:"st.secondary.tools"
        }
        standardTile("associationSet","device.associationSet", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
        	state "associationSet", label:'Association set', action:"associationSet", icon:"st.secondary.tools"
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

def installed(){
	command(zwave.multiChannelV3.multiChannelEndPointGet())
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

    log.debug "${device.displayName} have $epc EndPoints"
    [event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd)
{
	def cc = cmd.commandClass
    def ep = cmd.endPoint
    def needCreate = null
    if (!childDevices.find{ it.deviceNetworkId.endsWith("-ep${ep}") }) {
        createChildDevices(cc, ep)
    } else if (!childDevices) {
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
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {}
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {}
def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {}
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {}
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {}

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
                case 0x32:
                	deviceCCType = "Meter"
                    deviceCCHandler = "Child Meter"
                    log.debug "case '0x32'"
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
            				[completedSetup: true, label: "${deviceCCType}-${ep}", 
                             isComponent: false, componentName: "${deviceCCType}-${ep}", componentLabel: "${deviceCCType}-${ep}"])
            }
        	catch (e) {
            log.error "Creation child devices failed with error = ${e}"
            }
        }
        
        associationSet()
    }
    catch (e) {
        log.error "Child device creation failed with error = ${e}"
    }
}

def associationSet() {
	def cmds = []
	def multiChannelAssociationCC = 		"8E"
    def setCmd = 							"01"
	def groupingIdentifier = 				"01"
    def marker = 							"00"
    def nodeId = 							prependZero(zwaveHubNodeId)
    def ep =                                "00"
	
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
	def result = null
    if (source || destination) {
        def multiChannel = 						"60"
        def cmdEncap =			 				"0D"
        def sourceEP = 							prependZero(source)
        def destinationEP = 					prependZero(destination)

		result = "${multiChannel}${cmdEncap}${sourceEP}${destinationEP}${cmd}"
	}
    return result
}

def extractEP(def s) {
	def result = null
	if (contains(s, "-")) {
    	result = s.substring(s.length() - (s.indexOf("-") - 1)) 
    }
	return result as Integer
}

private boolean contains(def s, def ss) {
    boolean contains = false
    if (s != null && !s.isEmpty()) {
		contains = s.matches(".*${ss}.*")
    }
    return contains
}

def prependZero(def s) {
	if (s > 9) 
    	return s
    else
    	return "0$s"
}