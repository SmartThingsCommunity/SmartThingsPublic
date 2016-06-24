/**
 *  CCTest
 *
 *  Copyright 2016 Kuldip
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
definition(
    name: "CloudCar",
    namespace: "CloudCar",
    author: "Kuldip@CloudCar",
    description: "Connect Car and Home",
    category: "Convenience",
    iconUrl: "http://justdrive.net/wp-content/themes/justdrive/assets/img/jd-logo-sans.png",
    iconX2Url: "http://justdrive.net/wp-content/themes/justdrive/assets/img/jd-logo-sans.png",
    iconX3Url: "http://justdrive.net/wp-content/themes/justdrive/assets/img/jd-logo-sans.png",
    oauth: [displayName: "CloudCar", displayLink: "https://CloudCar.com"]
)

preferences {
    section ("Control your home while driving your car") {
                input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
                input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
                input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
                input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
                input "temperatureSensors", "capability.temperatureMeasurement", title: "Which Temperature Sensors?", multiple: true, required: false
                input "accelerationSensors", "capability.accelerationSensor", title: "Which Vibration Sensors?", multiple: true, required: false
                input "waterSensors", "capability.waterSensor", title: "Which Water Sensors?", multiple: true, required: false
                input "lightSensors", "capability.illuminanceMeasurement", title: "Which Light Sensors?", multiple: true, required: false
                input "humiditySensors", "capability.relativeHumidityMeasurement", title: "Which Relative Humidity Sensors?", multiple: true, required: false
                input "alarms", "capability.alarm", title: "Which Sirens?", multiple: true, required: false
                input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
    }
}

mappings {
    path("/scan") {
        action: [
            GET: "scanDevices"
        ]
    }
    path("/control/:command") {
        action: [
            GET: "updateSwitches"
        ]
    }
    path("/control/:deviceType/:deviceId/:command") {
        action: [
            GET: "updateDevice"
        ]
    }
}

def installed() {
    log.debug "[installed]: state ${state}"
    initialize ()
}

def uninstalled() {
        log.debug "[uninstalled]: ${settings}"
}

def updated () {
	log.debug "[updated]: ${settings}"
    unsubscribe()
    initialize ()
}

def initialize () {
    state.clear() // clear previous state - state is loaded from persistant store
    addSubscription()
}
 
def addSubscription() {
	def callbackUrl = 'http://ec2-54-67-21-37.us-west-1.compute.amazonaws.com/notify' //data.callbackUrl
    log.debug "[addSubscription]  state ${state}"
    log.debug "[addSubscription]  settings ${settings}"
	settings.each {
		log.debug "[addSubscription]  settings ${it}"
    }
    
    def currentDeviceIds = settings.collect { k, devices -> devices }.flatten().collect()// { it.id }.unique()
    log.debug "[addSubscription]  currentDeviceIds  ${currentDeviceIds}"
	currentDeviceIds.each {
        log.debug "[addSubscription]  device ids ${it} - ${it.id}"
        log.debug "Adding switch subscription " + callbackUrl
		state[it.id] = [callbackUrl: callbackUrl]
    }
	subscribe(switches, "switch.on", mydeviceHandler)
	subscribe(contactSensors, "contact.open", mydeviceHandler)
    subscribe(motionSensors, "motion.active", mydeviceHandler)
	log.debug "[addSubscription] COMPLETED ${state}"
}

def mydeviceHandler(evt) {
	def callbackUrl = 'http://ec2-54-67-21-37.us-west-1.compute.amazonaws.com/notify' //data.callbackUrl
	//def callbackUrl = 'https://testing.justdrive.cloudcar.com/notify/5c5dca5f-1b98-fff9-f111-8072b41c5fb3'
	log.debug "[deviceHandler] $evt.displayName"
	def deviceInfo = state[evt.deviceId]
    log.debug "[deviceHandler] deviceInfo ${deviceInfo}"
	if (deviceInfo) {
		try {
				httpPostJson(uri: callbackUrl, path: '',  body: [deviceId: evt.deviceId, deviceType: evt.name, deviceName: evt.displayName, value: evt.value]) {   
				log.debug "[deviceHandler] Event data successfully posted"
			}
		} catch (groovyx.net.http.ResponseParseException e) {
			log.debug("Error parsing payload ${e}")
		}
	} else {
		log.debug "[deviceHandler] No subscribed device found"
	}
}


def scanDevices() {
        log.debug "[scandevices] ${type}"
        [
        switches: switches.collect{KPdevice(it,"switches")},
        contactSensors: contacts.collect{KPdevice(it,"contactSensors")},
        temperatures: temperatures.collect{KPdevice(it,"temperature")},
        ]
}

private KPdevice(it, type) {
	def device_state = null
    log.debug "KPDevice it: ${it}"
	if (it.currentValue("switch") == "on" ) {
        device_state = [label:it.label, type:type, id:it.id]
			for (attribute in it.supportedAttributes) {
				device_state."${attribute}" = it.currentValue("${attribute}")
			}
		}
		log.debug "KPdevice: ${device_state}"
		device_state ? device_state : null
}

def void updateSwitches() {
	// use the built-in request object to get the command parameter
	def command = params.command
	log.debug "updateSwitches: ${params.command}"
    // all switches have the command
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element

    switch(command) {
        case "on":
            switches.on()
            break
        case "off":
            switches.off()
            break
        default:
            httpError(400, "$command is not a valid command for all switches specified")
    }
}

def void updateDevice() {
	// use the built-in request object to get the command parameter
	def command = params.command
    def deviceId = params.deviceId
    def devices = settings[params.deviceType]

    log.debug "[updateDevice] ${deviceId} - ${command} - ${devices}"
    
    // all switches have the command
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element
	def device = devices.find { it.id == deviceId }
	if (!device) {
		httpError(404, "Device not found")
	} else {
		device."$command"()
	}
}

def list() {
        log.debug "[PROD] list, params: ${params}"
        def type = params.deviceType
        settings[type]?.collect{deviceItem(it)} ?: []
}

def listStates() {
        log.debug "[PROD] liststates, params: ${params}"
        def type = params.deviceType
        def attributeName = attributeFor(type)
        settings[type]?.collect{deviceState(it, it.currentState(attributeName))} ?: []
}

def listSubscriptions() {
        log.debug "[PROD] listSubscription, params: ${params}"
        state
}

def update() {
        def type = params.deviceType
        def data = request.JSON
        def devices = settings[type]
        def device = settings[type]?.find { it.id == params.id }
        def command = data.command

        log.debug "[PROD] update, params: ${params}, request: ${data}, devices: ${devices*.id}"

        if (!device) {
                httpError(404, "Device not found")
        }

        if (validateCommand(device, type, command)) {
                device."$command"()
        } else {
                httpError(403, "Access denied. This command is not supported by current capability.")
        }
}

/**
 * Validating the command passed by the user based on capability.

 * @return boolean
 */
def validateCommand(device, deviceType, command) {
        log.debug "[validatecommand] ${device} ${deviceType} ${command}"
        def capabilityCommands = getDeviceCapabilityCommands(device.capabilities)
        def currentDeviceCapability = getCapabilityName(deviceType)
        if (capabilityCommands[currentDeviceCapability]) {
                return command in capabilityCommands[currentDeviceCapability] ? true : false
        } else {
                // Handling other device types here, which don't accept commands
                httpError(400, "Bad request.")
        }
}

/**
 * Need to get the attribute name to do the lookup. Only
 * doing it for the device types which accept commands
 * @return attribute name of the device type
 */
def getCapabilityName(type) {
        log.debug "[getCapabilityName] ${type}"
    switch(type) {
                case "switches":
                        return "Switch"
                case "alarms":
                        return "Alarm"
                case "locks":
                        return "Lock"
                default:
                        return type
        }
}

/**
 * Constructing the map over here of
 * supported commands by device capability
 * @return a map of device capability -> supported commands
 */
def getDeviceCapabilityCommands(deviceCapabilities) {
        log.debug "[getDeviceCapabilities] ${deviceCapabilities}"
        def map = [:]
        deviceCapabilities.collect {
                map[it.name] = it.commands.collect{ it.name.toString() }
        }
        return map
}

def show() {
        def type = params.deviceType
        def devices = settings[type]
        def device = devices.find { it.id == params.id }

        log.debug "[PROD] show, params: ${params}, devices: ${devices*.id}"
        if (!device) {
                httpError(404, "Device not found")
        }
        else {
                def attributeName = attributeFor(type)
                def s = device.currentState(attributeName)
                deviceState(device, s)
        }
}

def removeSubscription() {
        def type = params.deviceType
        def devices = settings[type]
        def deviceId = params.id
        def device = devices.find { it.id == deviceId }

        log.debug "[PROD] removeSubscription, params: ${params}, request: ${data}, device: ${device}"
        if (device) {
                log.debug "Removing $device.displayName subscription"
                state.remove(device.id)
                unsubscribe(device)
        }
        log.info state
}

private deviceItem(it) {
        log.debug "[deviceItem] ${it}"
        it ? [id: it.id, label: it.displayName] : null

}

private deviceState(device, s) {
        log.debug "[deviceState] ${device}: ${s}"
        device && s ? [id: device.id, label: device.displayName, name: s.name, value: s.value, unixTime: s.date.time] : null
}

private attributeFor(type) {
        log.debug "[attributeFor] ${type}"
        switch (type) {
                case "switches":
                        log.debug "[PROD] switch type"
                        return "switch"
                case "locks":
                        log.debug "[PROD] lock type"
                        return "lock"
                case "alarms":
                        log.debug "[PROD] alarm type"
                        return "alarm"
                case "lightSensors":
                        log.debug "[PROD] illuminance type"
                        return "illuminance"
                default:
                        log.debug "[PROD] other sensor type"
                        return "Sensors"
        }
}
