/**
 *  ZHC5010 Z-Wave switch module test
 *
 *  Copyright 2016 DarwinsDen.com
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
 *	Author: Darwin@DarwinsDen.com
 *	Date: 2016-06-08
 *
 *	Changelog:
 *
 *	0.01 (06/08/2016) -	Initial 0.01 Test Code/Beta
 *	0.02 (08/04/2016) -	Added double and triple tap (increments button by +4, and +8 respectively)
 *	0.03 (12/23/2016) -	Added test/workaround preference option to cancel single press after double press. Added
 *                      preference option to disable switch relay
 *
 */
 
metadata {
	definition (name: "ZHC5010", namespace: "darwinsden", author: "darwin@darwinsden.com") {
		capability "Actuator"
		capability "Switch"
        capability "Button"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        
 
        //fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x25, 0x27, 0x70, 0x2C, 0x2B, 0x5B, 0x7A", outClusters: "0x5B"
}

	// simulator metadata
	simulator {
	}
    
    preferences {      
       input "doublePressCancelsSingle", "bool", title: "Cancel Single-Press when followed by Double-Press",  defaultValue: false,  displayDuringSetup: true, required: false	       
       input "disableSwitchRelay", "bool", title: "Disable the switch physical relay",  defaultValue: false,  displayDuringSetup: true, required: false	       
    }

	tiles(scale: 2) {
    
       	valueTile("buttonTile", "device.buttonNum", width: 2, height: 2) {
			state("", label:'${currentValue}')
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
     
		main "buttonTile"
		details(["buttonTile","refresh"])
	}
}



def parse(String description) {
 	def result = null
 	def cmd = zwave.parse(description, [0x20: 1, 0x70: 1])
	
    if (cmd) {
  		result = zwaveEvent(cmd)
  	}
    if (!result){
        log.debug "Parse returned ${result} for command ${cmd}"
    }
    else {
  		log.debug "Parse returned ${result}"
    }   
 	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
  	createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "physical"])
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
  	createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "physical"])
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
 	createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "digital"])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
 	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
  	if (state.manufacturer != cmd.manufacturerName) {
 		createEvent(updateDataValue("manufacturer", cmd.manufacturerName))
 	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    log.debug (cmd)
    createEvent([:])
}

def pressedButton (def btnRes) {
  
  def canceling = false
  
  if (state.doublePressed1 && btnRes ==1) {
     canceling = true
     state.doublePressed1 = false
  }
  else if (state.doublePressed2 && btnRes == 2) {
     canceling = true
     state.doublePressed2 = false
  }
  else if (state.doublePressed3 && btnRes == 3) {
     canceling = true
     state.doublePressed3 = false
  }
  else if (state.doublePressed4 && btnRes == 4) {
     canceling = true
     state.doublePressed4 = false
  }
  
  if (canceling) {
         log.debug ("Canceling single press for button $btnRes")
         state.doublePressed=false
  }
  else
     {
         log.debug ("button $btnRes pushed")
         sendEvent(name: "buttonNum" , value: "Btn: $btnRes pushed")
         sendEvent([name: "button", value: "pushed", data: [buttonNumber: "$btnRes"], descriptionText: "$device.displayName $btnRes pressed", isStateChange: true, type: "physical"])
       }
}

def pressedButton1() {
   pressedButton (1)
}

def pressedButton2() {
   pressedButton (2)
}

def pressedButton3() {
   pressedButton (3)
}

def pressedButton4() {
   pressedButton (4)
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    log.debug("sceneNumber: ${cmd.sceneNumber} keyAttributes: ${cmd.keyAttributes}")
    def result = []

    switch (cmd.keyAttributes) {
       case 0:
           //pressed
           def buttonResult = cmd.sceneNumber
           if (doublePressCancelsSingle)
           {
             switch (buttonResult) {
               case 1:
                  state.doublePressed1=false
                  runIn (1, pressedButton1) 
                  break
               case 2:
                  state.doublePressed2=false
                  runIn (1, pressedButton2)  
                  break
               case 3:
                  state.doublePressed3=false
                  runIn (1, pressedButton3) 
                  break
               case 4:
                  state.doublePressed4=false
                  runIn (1, pressedButton4)  
                  break
               default:
                 log.debug ("unexpected button $buttonNum")
             }
           }
           else
           {
             sendEvent(name: "buttonNum" , value: "Btn: $buttonResult pushed")
             result=createEvent([name: "button", value: "pushed", data: [buttonNumber: "$buttonResult"], 
                descriptionText: "$device.displayName $buttonResult pressed", isStateChange: true, type: "physical"])
           }
           break
 
       case 1:
           //released
           def buttonResult = cmd.sceneNumber
           sendEvent(name: "buttonNum" , value: "Btn: $buttonResult released")
           result=createEvent([name: "button", value: "released", data: [buttonNumber: "$buttonResult"], 
                         descriptionText: "$device.displayName $buttonResult released", isStateChange: true, type: "physical"])
           break
       
       case 2:
           //held
           def buttonResult = cmd.sceneNumber
           result=createEvent([name: "button", value: "held", data: [buttonNumber: "$buttonResult"], 
                         descriptionText: "$device.displayName $buttonResult held", isStateChange: true, type: "physical"])
           break
    
       case 3:
           //double press
           def buttonResult = cmd.sceneNumber + 4
           
           switch (buttonResult) {
           case 5:
              state.doublePressed1=true
              break
           case 6:
              state.doublePressed2=true
              break
           case 7:
              state.doublePressed3=true
              break
           case 8:
              state.doublePressed4=true
              break
           default:
              log.debug ("unexpected double press button: $buttonResult")
           }
              
           sendEvent(name: "buttonNum" , value: "Btn: $buttonResult double press")
           result=createEvent([name: "button", value: "pushed", data: [buttonNumber: "$buttonResult"], 
                         descriptionText: "$device.displayName $buttonResult double-pressed", isStateChange: true, type: "physical"])
           break                  

       case 4:
           //triple press -- not currently supported
           def buttonResult = cmd.sceneNumber + 8
           sendEvent(name: "buttonNum" , value: "Btn: $buttonResult double press")
           result=createEvent([name: "button", value: "pushed", data: [buttonNumber: "$buttonResult"], 
                         descriptionText: "$device.displayName $buttonResult double-pressed", isStateChange: true, type: "physical"])
           break                  


      default:
           // unexpected case


           log.debug ("unexpected attribute: $cmd.keyAttributes")
   }  
   return result
}

def configure() {
     sendEvent(name: "numberOfButtons", value: 12, displayed: false)
     if (disableSwitchRelay) {
        zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 15, size: 1).format()
     }
}
def refresh() {
  configure()
}