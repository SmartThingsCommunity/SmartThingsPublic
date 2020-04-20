/**
 *  Copyright 2016 AdamV
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
 *
 *  Version 0.9.0
 *  Author: AdamV
 *  Date: 2016-09-10
 *
 * 
 */
 
metadata {
	definition (name: "Fibaro Button", namespace: "Fibaro", author: "AdamV") {
		capability "Actuator"
		capability "Button"
        capability "Battery"
		capability "Configuration" 
       	capability "Refresh"
        
        command "describeAttributes"
        
		attribute "numberOfButtons", "number"
        attribute "buttonClicks", "enum", ["one click", "two clicks", "three clicks", "four clicks", "five clicks", "hold start", "hold release"]
        attribute "holdLevel", "number"

		fingerprint deviceId: "0x1801", inClusters: "0x5E, 0x86, 0x72, 0x5B, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x8E, 0x7A, 0x98", outClusters: "0x26, 0x9C"
							
   }

	simulator {
		status "button 1 pushed":  "command: 9881, payload: 00 5B 03 DE 00 01"
		
        // need to redo simulator commands

	}
	tiles (scale: 2){
		
        multiAttributeTile(name:"button", type:"generic", width:6, height:4) {
  			tileAttribute("device.button", key: "PRIMARY_CONTROL"){
    		attributeState "default", label:'Fibaro Button', backgroundColor:"#44b621", icon:"st.Home.home30"
            attributeState "held", label: "holding", backgroundColor: "#C390D4"
  			}
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
			attributeState "battery", label:'${currentValue} % battery'
            }
            
        }
		//standardTile("button", "device.button", width: 6, height: 4) {
		//	state "default", label: "", icon: "st.Home.home30", backgroundColor: "#ffffff"
        //    state "held", label: "holding", icon: "st.Home.home30", backgroundColor: "#C390D4"
       // } 
    	//valueTile("battery", "device.battery", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
         //tileAttribute ("device.battery", key: "PRIMARY_CONTROL"){
          // state "battery", label:'${currentValue}% battery', unit:""
        //}
        //}
        valueTile("configure", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "configure", backgroundColor: "#ffffff", action: "configure", icon:"st.secondary.configure"
        }
        
        main "button"
		details(["button", "configure"])
	}
}



def parse(String description) {
	def results = []
  //  log.debug("RAW command: $description")
	if (description.startsWith("Err")) {
		log.debug("An error has occurred")
		} 
    else {
       
       	def cmd = zwave.parse(description.replace("98C1", "9881")/*, [0x98: 1, 0x20: 1, 0x84: 1, 0x80: 1, 0x60: 3, 0x2B: 1, 0x26: 1]*/)
     //    log.debug "Parsed Command: $cmd"
        if (cmd) {
       	results = zwaveEvent(cmd)
		}
     	if ( !state.numberOfButtons ) {
    	state.numberOfButtons = "5"
        createEvent(name: "numberOfButtons", value: "5", displayed: false)

  		}
    }
}
  
def describeAttributes(payload) {
    	payload.attributes = [
        [ name: "holdLevel",    type: "number",    range:"1..100", capability: "button" ],
       	[ name: "buttonClicks",    type: "enum",    options: ["one click", "two clicks", "three clicks", "four clicks", "five clicks", "hold start", "hold release"], momentary: true, capability: "button" ],
    	]
    	return null
		}		  

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand()
				log.debug("UnsecuredCommand: $encapsulatedCommand")
        // can specify command class versions here like in zwave.parse
        if (encapsulatedCommand) {
        	log.debug("UnsecuredCommand: $encapsulatedCommand")
                return zwaveEvent(encapsulatedCommand)
        }
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	[ createEvent(descriptionText: "${device.displayName} woke up"),
	  response(zwave.wakeUpV2.wakeUpNoMoreInformation()) ]
      
      log.debug("Button Woke Up!")
}

/*def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	[ createEvent(descriptionText: "${device.displayName} woke up"),
	  response(zwave.wakeUpV1.wakeUpNoMoreInformation()) ]
      
      log.debug("Button Woke Up!")
      log.debug("wakeup v1 payload: $cmd.payload")
}
*/

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
      	// log.debug("Crc16Encap: $cmd")
        // log.debug( "Data: $cmd.data")
        // log.debug( "Payload: $cmd.payload")
        // log.debug( "command: $cmd.command")
        // log.debug( "commandclass: $cmd.commandClass")
	def versions = [0x31: 3, 0x30: 2, 0x84: 2, 0x9C: 1, 0x70: 2]
	// def encapsulatedCommand = cmd.encapsulatedCommand(versions)
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug "Could not extract command from $cmd"
	} else {
		zwaveEvent(encapsulatedCommand)
     //   log.debug("UnsecuredCommand: $encapsulatedCommand")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	//	log.debug( "keyAttributes: $cmd.keyAttributes")
    //    log.debug( "sceneNumber: $cmd.sceneNumber")
    //    log.debug( "sequenceNumber: $cmd.sequenceNumber")
      // 	log.debug( "payload: $cmd.payload")
      	
        if ( cmd.keyAttributes == 0 ) {
        	Integer button = 1
            sendEvent(name: "buttonClicks", value: "one click", descriptionText: "$device.displayName button was clicked once", isStateChange: true)
        	sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
            log.debug( "Button was pushed once" )
            }
        if ( cmd.keyAttributes == 3 ) {
        	Integer button = 2
            sendEvent(name: "buttonClicks", value: "two clicks", descriptionText: "$device.displayName button was pushed twice", isStateChange: true)
        	sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
            log.debug( "Button was pushed twice" )
            }
        if ( cmd.keyAttributes == 4 ) {
        	Integer button = 3
            sendEvent(name: "buttonClicks", value: "three clicks", descriptionText: "$device.displayName button was pushed three times", isStateChange: true)
        	sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
            log.debug( "Button was pushed three times" )
            }
        if ( cmd.keyAttributes == 5 ) {
        	Integer button = 4
            sendEvent(name: "buttonClicks", value: "four clicks", descriptionText: "$device.displayName button was pushed four times", isStateChange: true)
        	sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
            log.debug( "Button was pushed four times" )
            }
	    if ( cmd.keyAttributes == 6 ) {
        	Integer button = 5
            sendEvent(name: "buttonClicks", value: "five clicks", descriptionText: "$device.displayName button was pushed FIVE times", isStateChange: true)
        	sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
            log.debug( "Button was pushed five times" )
            }
	    if ( cmd.keyAttributes == 2 ) {
        	Integer button = 1
            sendEvent(name: "button", value: "held", descriptionText: "$device.displayName button $button was held", isStateChange: true)
            sendEvent(name: "buttonClicks", value: "hold start", data: [buttonClicks: "hold start"], descriptionText: "$device.displayName button is holdStart", isStateChange: true)
            sendEvent(name: "button", value: "level", data: [cmd.sequenceNumber], descriptionText: "$device.displayName level is $cmd.sequenceNumber", isStateChange: true)
        	log.debug( "Button held" )
            log.debug( "Button level: $cmd.sequenceNumber" )
            }
	    if ( cmd.keyAttributes == 1 ) {
            sendEvent(name: "buttonClicks", value: "hold release", descriptionText: "$device.displayName button is released", isStateChange: true)
        	log.debug( "Button released" )
            }
      
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelGet cmd) {
//	log.debug "Multilevel get: $cmd"
}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
//	log.debug "Multilevel report: $cmd.sensorValue"
}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet  cmd) {
//	log.debug "Multilevel dimmingDuration: $cmd.dimmingDuration"
//    log.debug "Multilevel value: $cmd.value"
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
        def map = [ name: "battery", unit: "%" ]
        if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
                map.value = 1
                map.descriptionText = "${device.displayName} has a low battery"
                map.isStateChange = true
        } else {
                map.value = cmd.batteryLevel
                log.debug ("Battery: $cmd.batteryLevel")
        }
        // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
        state.lastbatt = new Date().time
        createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd){
      //  log.debug "basic event: $cmd.value"
}
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd){
      //  log.debug "basic set value: $cmd.value"
      //  log.debug "basic set payload: $cmd.payload"
}
def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd){
        log.debug "Sensor Alarm seconds: $cmd.seconds"
        log.debug "Sensor Alarm sensorState: $cmd.sensorState"
        log.debug "Sensor Alarm sensorType: $cmd.sensorType"
        log.debug "Sensor Alarm sourceNodeId: $cmd.sourceNodeId"
        log.debug "Sensor Alarm payload: $cmd.payload"
}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd){
       // log.debug "Switch Start Leve Change dimmingDuration: $cmd.dimmingDuration"
       // log.debug "Switch Start Leve Change ignoreStartLevel: $cmd.ignoreStartLevel"
       // log.debug "Switch Start Leve Change incDec: $cmd.incDec"
       // log.debug "Switch Start Leve Change startLevel: $cmd.startLevel"
       // log.debug "Switch Start Leve Change stepSize: $cmd.stepSize"
       // log.debug "Switch Start Leve Change upDown: $cmd.upDown"
        
      //  Integer button = 1
      //  sendEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "Button $button is held", isStateChange: true)
      //  log.debug( "Button Hold start" )
}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd){
        // log.debug "Switch Stop Leve Change payload: $cmd.payload"
    //    Integer button = 1
    //    sendEvent(name: "button", value: "holdRelease", data: [buttonNumber: button], descriptionText: "Button $button is released")
    //    log.debug( "Button Hold stop" )
}


def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	log.debug( "Dimming Duration: $cmd.dimmingDuration")
    log.debug( "Button code: $cmd.sceneId")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug( "parameter: $cmd.parameterNumber, values: $cmd.configurationValue, size: $cmd.size")

}


  def configure() {
    
 
    def commands = [ ]
			log.debug "Resetting Sensor Parameters to SmartThings Compatible Defaults"
	def cmds = []
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 4, nodeId: zwaveHubNodeId).format()
	cmds << zwave.configurationV2.configurationSet(configurationValue: [127], parameterNumber: 1, size: 1).format()
    cmds << zwave.configurationV2.configurationSet(configurationValue: [7], parameterNumber: 3, size: 1).format()
    cmds << zwave.configurationV2.configurationSet(configurationValue: [16], parameterNumber: 30, size: 1).format()
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 1).format()  

    
    delayBetween(cmds, 500)
}