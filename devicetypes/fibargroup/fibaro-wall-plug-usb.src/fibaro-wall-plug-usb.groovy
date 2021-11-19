/**
 *	Fibaro Wall Plug US child
 */
metadata {
	definition (name: "Fibaro Wall Plug USB", namespace: "FibarGroup", author: "Fibar Group", ocfDeviceType: "oic.d.smartplug") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		command "reset"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"usb", type: "generic", width: 3, height: 4, canChangeIcon: true){
			tileAttribute ("usb", key: "PRIMARY_CONTROL") {
				attributeState "usb", label: 'USB', action: "", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/wallPlugUS/plugusb_on.png", backgroundColor: "#00a0dc"
			}
			tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
				attributeState("multiStatus", label:'${currentValue}')
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
	}

	preferences {
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
		input ( type: "paragraph", element: "paragraph", title: null, description: "This is a child device. If you're looking for parameters to set you'll find them in main component of this device." )
	}
}

def installed() {
	sendEvent([name: "energy", value: 0, unit: "kWh"])
	sendEvent([name: "power", value: 0, unit: "W"])
	sendEvent(name: "checkInterval", value: 1920, displayed: false, data: [protocol: "zwave", hubHardwareId: parent.hubID])
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

def ping() {
	parent.childRefresh()
}