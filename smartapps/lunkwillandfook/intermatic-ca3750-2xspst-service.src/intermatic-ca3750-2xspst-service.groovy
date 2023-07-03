/**
 *  Intermatic CA3750 Service
 *
 *  Copyright 2015 Jeremy Huckeba
 *
 */
definition(
    name: "Intermatic CA3750 2xSPST Service",
    namespace: "LunkwillAndFook",
    author: "Jeremy Huckeba",
    description: "Enables control of second relay for the Intermatic CA-3750 when in 2xSPST.",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light14-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light14-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light14-icn@3x.png")

preferences {
	page(name: "page1", title: "Welcome", uninstall: true, install: true) {
		section() {
            input(name: "selectedSwitch", type: "capability.switch", title: "The switch which is the paired Intermatic CA-3750", multiple: false, required: false)
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	def deviceId = selectedSwitch.id
    def deviceName = selectedSwitch.label
    def slaveDeviceId = deviceId + "Switch2"
    def slaveDeviceName = deviceName + " Switch 2"
    def slaveDevice = getChildDevice(slaveDeviceId)
    if(!slaveDevice) {
        slaveDevice = addChildDevice("LunkwillAndFook", "Intermatic CA3750 Switch 2", slaveDeviceId, null, [name: "Device.${slaveDeviceId}", label: slaveDeviceName, completedSetup: false])
    }
    
    subscribe(selectedSwitch, "switch2.on", switch2OnHandler)
    subscribe(selectedSwitch, "switch2.off", switch2OffHandler)
    log.debug "subscribed"
}

def on() {
	selectedSwitch.on2()
}

def off() {
	selectedSwitch.off2()
}

def refresh() {
	selectedSwitch.refresh()
}

def poll() {
	selectedSwitch.poll()
}

def switch2OnHandler(evt) {
	log.debug "switch2OnHandler ${evt.name}"
	if(evt.name == "switch2") {
        def deviceId = selectedSwitch.id
        def slaveDeviceId = deviceId + "Switch2"
        def slaveDevice = getChildDevice(slaveDeviceId)
        slaveDevice.turnedOn()
    }
}

def switch2OffHandler(evt) {
	log.debug "switch2OffHandler ${evt.name}"
	if(evt.name == "switch2") {
        def deviceId = selectedSwitch.id
        def slaveDeviceId = deviceId + "Switch2"
        def slaveDevice = getChildDevice(slaveDeviceId)
        slaveDevice.turnedOff()
    }
}
