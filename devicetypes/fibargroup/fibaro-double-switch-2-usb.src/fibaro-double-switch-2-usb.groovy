/**
 *	Fibaro Double Switch 2 Child Device
 */
metadata {
	definition (name: "Fibaro Double Switch 2 - USB", namespace: "FibarGroup", author: "Fibar Group", mnmn: "SmartThings", vid:"generic-switch-power-energy") {
		capability "Switch"
		capability "Actuator"
		capability "Sensor"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"
		
		command "reset"
	  
	}

	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/switch/switch_2.png", backgroundColor: "#ffffff"
				attributeState "on", label: '${name}', action: "switch.off", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/switch/switch_1.png", backgroundColor: "#00a0dc"			
			}
			tileAttribute("device.combinedMeter", key:"SECONDARY_CONTROL") {
				attributeState("combinedMeter", label:'${currentValue}')
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "power", label:'${currentValue}\nW', action:"refresh"
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "energy", label:'${currentValue}\nkWh', action:"refresh"
		}
		valueTile("reset", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "reset", label:'reset\nkWh', action:"reset"
		}
		standardTile("main", "device.switch", decoration: "flat", canChangeIcon: true) {
			state "off", label: 'off', action: "switch.on", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/switch/switch_2.png", backgroundColor: "#ffffff"
			state "on", label: 'on', action: "switch.off", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/switch/switch_1.png", backgroundColor: "#00a0dc"
		}
		
		main(["switch"])
		details(["switch","power","energy","reset"])
	}

	preferences {
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
		input ( type: "paragraph", element: "paragraph", title: null, description: "This is a child device. If you're looking for parameters to set you'll find them in main component of this device." )
	}
}

def installed(){
	sendEvent(name: "checkInterval", value: 1920, displayed: false, data: [protocol: "zwave", hubHardwareId: parent.hubID])
	response(refresh())
}

def ping() {
	parent.childRefresh()
}

def on() {
	parent.childOn()
}

def off() {
	parent.childOff()
}

def reset() {
	resetEnergyMeter()
}

def resetEnergyMeter() {
	parent.childReset()
}

def refresh() {
	parent.childRefresh()
}
