/*****************************************************************************************************************
 *  Copyright: Nick Veenstra
 *
 *  Name: GreenWave PowerNode 6
 *
 *  Date: 2018-01-04
 *
 *  Version: 1.00
 *
 *  Source and info: https://github.com/CopyCat73/SmartThings/tree/master/devicetypes/copycat73/greenwave-powernode-6.src
 *
 *  Author: Nick Veenstra
 *  Thanks to David Lomas, Cooper Lee and Eric Maycock for code inspiration 
 *
 *  Description: Device handler for the GreenWave PowerNode (multi socket) Z-Wave power outlet
 *
 *  License:
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *   for the specific language governing permissions and limitations under the License.
 *
 *****************************************************************************************************************/
metadata {
	definition (name: "GreenWave PowerNode 6", namespace: "copycat73", author: "Nick Veenstra", ocfDeviceType: "oic.d.switch") {
		capability "Energy Meter"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Configuration"
	
		attribute "switch", "string"
		attribute "switch1", "string"
		attribute "switch2", "string"
		attribute "switch3", "string"
		attribute "switch4", "string"
		attribute "switch5", "string"
		attribute "switch6", "string"
		attribute "power", "string"
		attribute "power1", "string"
		attribute "power2", "string"
		attribute "power3", "string"
		attribute "power4", "string"
		attribute "power5", "string"
		attribute "power6", "string"
		attribute "energy", "string"
		attribute "energy1", "string"
		attribute "energy2", "string"
		attribute "energy3", "string"
		attribute "energy4", "string"
		attribute "energy5", "string"
		attribute "energy6", "string"
        attribute "lastupdate", "string"

		command "on"
		command "off"
		command "on1"
		command "off1"
        command "on2"
		command "off2"
		command "on3"
		command "off3"
        command "on4"
		command "off4"
		command "on5"
		command "off5"
        command "on6"
		command "off6"
		command "reset"

		//fingerprint inClusters: "0x25,0x32"
		fingerprint mfr:"0099", prod:"0003", model:"0004", deviceJoinName: "GreenWave PowerNode 6"
        
	}

	simulator {
    	//TBD
	}
    
    preferences {
		// input name:"updateLight", type:"number", title:"After how many minutes the GreenWave device should start flashing if the controller didn't communicate with this device", defaultValue:255
	}
    
    tiles(scale: 2) {
  	
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label:'${name}', action:'switch.on', icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "on", label:'${name}', action:'switch.off', icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:'switch.on', icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:'switch.off', icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
            }
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
                attributeState "statusText", label:'${currentValue}'
            }
        }
   
        valueTile("power", "device.power", width: 2, height: 1, decoration: "flat") {
						state "default", label:'${currentValue} W' 
				}
		valueTile("energy", "device.energy", width: 2, height: 1, decoration: "flat") {
			state "default", label:'${currentValue} kWh'
		}
      
		standardTile("refresh", "device.switch", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
                        state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
                }
                
        standardTile("reset", "device.switch", inactiveLabel: false, decoration: "flat") {
        				state "default", label:"reset kWh", action:"reset"
                }
        standardTile("configure", "device.switch", inactiveLabel: false, decoration: "flat") {
        				state "default", label:"", action:"configure", icon:"st.secondary.configure"
                }    
		valueTile("lastupdate", "lastupdate", width: 4, height: 1, inactiveLabel: false) { 			
          				state "default", label:"Last updated: " + '${currentValue}' 		
				}       
        main(["switch", "power", "energy"])
        details(["switch", "power", "energy", "refresh", "lastupdate", "configure", "reset", ,childDeviceTiles("all")])
    
	}
}
/*****************************************************************************************************************
 *  SmartThings System Commands:
 *****************************************************************************************************************/

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description)
    if (cmd) {
        result = zwaveEvent(cmd)
        //log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        //log.debug "Non-parsed event: ${description}"
    }
    return result
}

def installed() {
    log.debug "installed"

    createChildDevices()

    //command(zwave.manufacturerSpecificV1.manufacturerSpecificGet())
    //command(zwave.configurationV1.configurationSet(configurationValue: [255], parameterNumber: 1, size: 1))

}

def uninstalled() {
	log.debug "uninstalled()"
	if (childDevices) {
    	log.debug "removing child devices"
    	removeChildDevices(getChildDevices())
	}
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def refresh() {
	pollNodes()
}

def updated() {
	log.debug "updated()"
}


def initialize() {
	unschedule()
	runEvery5Minutes(pollNodes)
}

def createChildDevices() {
    log.debug "creating child devices"
        
    try {
    	for (i in 1..6) {
        	def node = i as String
        	def devLabel = "Greenwave switch "+node
            addChildDevice("GreenWave PowerNode 6 Child Device", "${device.deviceNetworkId}-e${i}", null,
                [completedSetup: true, label: devLabel,
                isComponent: false, componentName: "switch$i", componentLabel: devLabel])
        }
    } catch (e) {
        log.debug "${e}"
        showAlert("Child device creation failed. Please make sure that the \"GreenWave PowerNode 6 Child Device\" is installed and published.","childDeviceCreation","failed")
    }
}

private showAlert(text,name,value) {
    sendEvent(
        descriptionText: text,
        eventType: "ALERT",
        name: name,
        value: value,
        displayed: true,
    )
}

def configure() {
	log.debug "configure()"
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(configurationValue: [255], parameterNumber: 1, size: 1).format()
    delayBetween(cmds)
}


def on() {
	log.debug "on"
    [    	
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchBinaryV1.switchBinaryGet().format(),
        "delay 3000",
        zwave.meterV2.meterGet(scale: 2).format()
    ]
}
def off() {
	log.debug "off"
	[
        zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.switchBinaryV1.switchBinaryGet().format(),
        "delay 3000",
        zwave.meterV2.meterGet(scale: 2).format()
    ]
}
def on1() {
	switchOn(1)
}
def off1() {
	switchOff(1)
}
def on2() {
	switchOn(2)
}
def off2() {
	switchOff(2)
}
def on3() {
	switchOn(3)
}
def off3() {
	switchOff(3)
}
def on4() {
	switchOn(4)
}
def off4() {
	switchOff(4)
}
def on5() {
	switchOn(5)
}
def off5() {
	switchOff(5)
}
def on6() {
	switchOn(6)
}
def off6() {
	switchOff(6)
}

def switchOn(node) {

	def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0xFF), node)))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), node)))
    sendHubCommand(cmds)
}

def switchOff(node) {
    def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0x00), node)))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), node)))
    sendHubCommand(cmds)
}


def poll() {
	pollNodes()
}

def pollNodes() {

  	log.debug "<FONT COLOR=RED>Polling Powerstrip - ${device.label}</FONT>"
    def cmds = []
	for ( i in 1..6 ) { 
        cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), i)))
        cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale:0),i)))
        cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale:2), i)))
	}
    cmds << zwave.switchBinaryV1.switchBinaryGet().format() 
    cmds << zwave.meterV2.meterGet(scale:0).format()
    cmds << zwave.meterV2.meterGet(scale:2).format()
	delayBetween(cmds,1000) 
}

def pollNode(endpoint)  {

  	log.debug "<FONT COLOR=RED>Polling Powerstrip - ${device.label} node ${endpoint}</FONT>"
    def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint)))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale:0), endpoint)))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale:2), endpoint)))
    sendHubCommand(cmds)
}

def updateChildLabel(endpoint) {
	log.debug "update tile label for endpoint $endpoint"
    // tbd
}

def lastUpdated(time) {
	def timeNow = now()
	def lastUpdate = ""
	if(location.timeZone == null) {
    	log.debug "Cannot set update time : location not defined in app"
    }
    else {
   		lastUpdate = new Date(timeNow).format("MMM dd yyyy HH:mm", location.timeZone)
    }
    return lastUpdate
}

def ping() {
	log.debug "ping() called"
	refresh()
}

def reset() {
  	log.debug "<FONT COLOR=RED>Resetting kWh for all endpoints</FONT>"
    def cmds = []
	for ( i in 1..6 ) { 
        cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterReset(), i)))
        cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale:0),i)))
	}
    cmds << new physicalgraph.device.HubAction(command(zwave.meterV2.meterReset()))
    cmds << new physicalgraph.device.HubAction(command(zwave.meterV2.meterGet(scale:0)))
	sendHubCommand(cmds)
}

def resetNode(endpoint) {
	log.debug "<FONT COLOR=RED>Resetting kWh for endpoint $endpoint</FONT>"
    def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterReset(), endpoint)))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale:0),endpoint)))
	sendHubCommand(cmds)
}


private encap(cmd, endpoint) {
    if (endpoint) {
        zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd)
    } else {
        cmd
    }
}

private command(physicalgraph.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=1000) {
    delayBetween(commands.collect{ command(it) }, delay)
}

/*****************************************************************************************************************
 *  Z-wave Event Handlers.
 *****************************************************************************************************************/


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep=null)
{
	//log.debug "Greenwave v1 basic report received"
	if (ep) {
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-e$ep"}
        if (childDevice)
        childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
    } else {
        def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
        def cmds = []
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        
        return result
    }
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep=null) {
   
   //log.debug "Greenwave v1 switchbinary report received for endpoint $ep value $cmd.value"
   if (ep) {
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-e$ep"}
        if (childDevice)
            childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
            childDevice.sendEvent(name: 'lastupdate', value: lastUpdated(now()), unit: "")
    } else {
        def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
        sendEvent(name: 'lastupdate', value: lastUpdated(now()), unit: "")
        def cmds = []
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        
        return result
    }
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep=null)
{
	//log.debug "Greenwave v3 meter report received for endpoint $ep scale $cmd.scale value $cmd.scaledMeterValue"
	def result
    def cmds = []
    if (cmd.scale == 0) {
        result = [name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
    } else if (cmd.scale == 1) {
        result = [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
    } else {
        result = [name: "power", value: cmd.scaledMeterValue, unit: "W"]
    }
    if (ep) {
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-e$ep"}
        if (childDevice)
            childDevice.sendEvent(result)
    } else {
       sendEvent(name: 'lastupdate', value: lastUpdated(now()), unit: "")
       sendEvent(result)
       (1..6).each { endpoint ->
            cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
            cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
       }
       
       return result
    }
}



def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	
    //log.debug "Greenwave v3 cMultiChannelCmdEncap command received"
    def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1]) 
    if (encapsulatedCommand) {
        return zwaveEvent(encapsulatedCommand,cmd.sourceEndPoint)
    }   
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	//log.debug "Greenwave v1 configuration report received"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	//log.debug "Greenwave v2 configuration report received"
}


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
	//log.debug "Greenwave v3 multi channel capability report received"
}