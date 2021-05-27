/**
 *  Copyright 2015 NodOn
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
	definition (name: "NodOn Wall Switch", namespace: "NodOn", author: "Alexis Lutun") {
    	capability "Actuator"
		capability "Button"
		capability "Configuration"
        capability "Sleep Sensor"
		capability "Battery"

		command "pushButtonOne"
		command "pushButtonTwo"
		command "pushButtonThree"
		command "pushButtonFour"
		command	"buttonEvent"
		command	"buttonPushed"
		command	"buttonPushed", [int]
		command	"refresh"

	fingerprint mfr: "0165", prod: "0002", model: "0003", cc: "5E,85,59,80,5B,70,5A,72,73,86,84", ccOut: "5E,5B,2B,27,22,20,26,84" // Wall Switch
    //LEGACY FINGERPRINT GENERIC FOR THREE : fingerprint deviceId: "0x0101", inClusters: "0x5E,0x85,0x59,0x80,0x5B,0x70,0x5A,0x72,0x73,0x86,0x84,0xEF,0x5E,0x5B,0x2B,0x27,0x22,0x20,0x26,0x84"

    }

	tiles(scale: 2) {
    	standardTile("My Octan", "device.button", width: 1, height: 1)
        {
			state "default", label: "", icon:"http://nodon.fr/smarthings/wall-switch/wallswitchfullicon.png", backgroundColor: "#ffffff"
    	}
        multiAttributeTile(name:"BatteryTile", type: "generic", width: 6, height: 4)
        {
       		tileAttribute ("device.battery", key: "PRIMARY_CONTROL")
            {
        		attributeState "default", backgroundColor: "#f58220", decoration: "flat", icon:"http://nodon.fr/smarthings/wall-switch/wallswitchfullicon.png"
           	}
        	tileAttribute ("device.battery", key: "SECONDARY_CONTROL")
        	{
        		attributeState "default", label:'${currentValue}% battery', unit:"%"
            }
		}
        standardTile("button One", "device.button", width: 2, height: 2, decoration: "flat")
        {
        	state "default", label: "", action: "pushButtonOne", icon:"http://nodon.fr/smarthings/wall-switch/wslefttop.png",defaultState: true, backgroundColor: "#ffffff"
            state "pushed", label: "", action: "pushButtonOne", icon:"http://nodon.fr/smarthings/wall-switch/wslefttop.png", backgroundColor: "#ffffff"
        }
        standardTile("button Two", "device.button", width: 2, height: 2, decoration: "flat")
        {
            state "default", label: "",action: "pushButtonTwo", icon:"http://nodon.fr/smarthings/wall-switch/wsrighttop.png", defaultState: true, backgroundColor: "#ffffff"
            state "pushed", label: "",action: "pushButtonTwo", icon:"http://nodon.fr/smarthings/wall-switch/wsrighttop.png", backgroundColor: "#ffffff"
        }
        standardTile("button Three", "device.button", width: 2, height: 2, decoration: "flat")
        {
            state "default", label: "", action: "pushButtonThree", icon: "http://nodon.fr/smarthings/wall-switch/wsleftbottom.png", defaultState: true, backgroundColor: "#ffffff"
            state "pushed", label: "", action: "pushButtonThree", icon: "http://nodon.fr/smarthings/wall-switch/wsleftbottom.png", backgroundColor: "#ffffff"
        }
        standardTile("button Four", "device.button", width: 2, height: 2,decoration: "flat")
        {
            state "default", label: "",action: "pushButtonFour", icon:"http://nodon.fr/smarthings/wall-switch/wsrightbottom.png", defaultState: true, backgroundColor: "#ffffff"
            state "pushed", label: "",action: "pushButtonFour", icon:"http://nodon.fr/smarthings/wall-switch/wsrightbottom.png", backgroundColor: "#ffffff"
        }
        standardTile("refresh", "generic", inactiveLabel: false, decoration: "flat", width: 2, height: 2)
        {
			state "default", label:'', action: "refresh", icon:"st.secondary.refresh"
		}

        standardTile("configure", "device.Configuration", decoration: "flat", width: 2, height: 2)
        {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
        }
		main "My Octan"
		details(["BatteryTile", "button One", "button Two", "configure", "button Three", "button Four", "refresh"])
	}
}
def installed()
{
    initialize()
}

def initialize()
{
    state.myRefresh = 0
    state.batteryRefresh = 0
}

def updated() {
    initialize()
}

def parse(String description)
{
	def results = []
	if (description.startsWith("Err"))
    {
	    results = createEvent(descriptionText:description, displayed:true)
	}
    else
    {
		def cmd = zwave.parse(description, [0x5B: 1, 0x80: 1, 0x84: 1]) //Central Scene , battery, wake up
		if(cmd)
        {
      		results += zwaveEvent(cmd)
           // log.debug "Parsed ${cmd} to ${result.inspect()}"
        }
		if(!results) results = [ descriptionText: cmd, displayed: false ]
	}
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{

	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]
	def prevBattery = device.currentState("battery")
   	if (!prevBattery || (new Date().time - prevBattery.date.time)/60000 >= 60 * 53 || state.batteryRefresh == 1)  //
	{
		results << response(zwave.batteryV1.batteryGet().format())
        createEvent(name: "battery", value: "10", descriptionText: "battery is now ${currentValue}%", isStateChange: true, displayed: true)
        state.batteryRefresh == 0
	}
    if (state.myRefresh == 1)
    {
    	results << response(zwave.configurationV1.configurationSet(parameterNumber: 8, scaledConfigurationValue:2).format())
        results << response(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format())
        state.myRefresh = 0
    }
	results << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	return results
}

def buttonEvent(button, attribute)
{
	if (attribute)
    {
    	createEvent(name: "button", value: "pushed", data: [buttonNumber: button, action: "held"] ,descriptionText: "$device.displayName button $button was held", icon:"http://nodon.fr/smarthings/octan-remote/octandiskfill.png", isStateChange: true, displayed: true)
    }
    else
    {
	  //  log.debug "ST event button Pushed ${button}"
    	createEvent(name: "button", value: "pushed", data: [buttonNumber: button, action: "pushed"] ,descriptionText: "$device.displayName button $button was pressed", icon:"http://nodon.fr/smarthings/octan-remote/octandiskfill.png", isStateChange: true, displayed: true)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification  cmd)
{
    Integer sceneNumber = cmd.sceneNumber as Integer
	Integer keyAttributes = cmd.keyAttributes as Integer
    Integer sequenceNumber = cmd.sequenceNumber as Integer

	buttonEvent(sceneNumber, keyAttributes)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd)
{
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF)
    {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
	}
    else
    {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd)
{
	[ descriptionText: "$device.displayName: $cmd", linkText:device.displayName, displayed: false ]
}

def refresh()
{
	state.batteryRefresh = 1
}

def configure()
{
	state.myRefresh = 1
}

def pushButtonOne()
{
	buttonPushed(1)
}

def pushButtonTwo()
{
	buttonPushed(2)
}

def buttonPushed(button)
{
	//log.debug "UX button Pushed ${button}"
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: button, action: "pushed"], descriptionText: "$device.displayName button $button was pushed",  icon:"http://nodon.fr/smarthings/octan-remote/octanfullicon.png", isStateChange: true)
}

def pushButtonThree()
{
	buttonPushed(3)
}

def pushButtonFour()
{
	buttonPushed(4)
}
