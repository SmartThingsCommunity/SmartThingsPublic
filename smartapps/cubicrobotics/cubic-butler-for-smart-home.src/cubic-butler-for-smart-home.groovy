/**
 *  Cubic Home
 * sdfssdfsfsdfsdfdsf
 *  Copyright 2016 Nikolay Zenovkin
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
        name: "Cubic Butler for Smart Home",
        namespace: "cubicrobotics",
        author: "Cubic Robotics",
        description: "Hey, I’m Cubic, AI butler for smart home! \n" +
                "I am the one and only app you need to control an entire home. \n" +
                "Right now I can help you to control your SmartThings devices via natural speech and dashboard.\n" +
                "Speak naturally, don’t learn robot! My A.I. is designed for a smart home control.\n" +
                "- You don’t have to remember exact commands and phrases.\n" +
                "- I have memory, and every new request will be processed in the context of previous ones.\n" +
                "- I can ask clarifying questions, if I’m not sure what you mean.",
        category: "Convenience",
        iconUrl: "https://lh3.googleusercontent.com/GX5BRhaFq22HpAEU6tD4JXvizlxWFuB9zjyZE39-pLpZvQvvUmVpWXa0v4-oaxz4tg=w300-rw",
        iconX2Url: "https://lh3.googleusercontent.com/GX5BRhaFq22HpAEU6tD4JXvizlxWFuB9zjyZE39-pLpZvQvvUmVpWXa0v4-oaxz4tg=w300-rw",
        iconX3Url: "https://lh3.googleusercontent.com/GX5BRhaFq22HpAEU6tD4JXvizlxWFuB9zjyZE39-pLpZvQvvUmVpWXa0v4-oaxz4tg=w300-rw",
        oauth: [displayName: "Cubic Home", displayLink: "http://cubic.ai/"])


preferences {
    section("Welcome to Cubic") {
        // TODO: put inputs here
    }
    section("Allow Cubic to control these switches") {
        input "switches", "capability.switch", multiple: true, required: false
    }

    section("Allow Cubic to control these motion sensors") {
        input "motionSensors", "capability.motionSensor", multiple: true, required: false
    }

    section("Allow Cubic to control these bulbs") {
        input "lamps", "capability.colorControl", multiple: true, required: false
    }

    section("Allow Cubic to control these thermostats") {
        input "thermostats", "capability.thermostat", multiple: true, required: false
    }

    section("Allow Cubic to control these water sensors") {
        input "waterSensors", "capability.waterSensor", multiple: true, required: false
    }

    section("Allow Cubic to control these CO sensors") {
        input "smokeSensors", "capability.carbonMonoxideDetector", multiple: true, required: false
    }

    section("Allow Cubic to control these door controls") {
        input "doorControls", "capability.doorControl", multiple: true, required: false
    }

}


mappings {
    path("/devices") {
        action:
        [
                GET: "getDevices"
        ]
    }
    path("/devices/:id") {
        action:
        [
                PUT: "updateDevice",
                GET: "getDevice"
        ]
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
    subscribe(motionSensors, "motion", motionHandler)
    subscribe(switches, "switch", switchHandler)
    subscribe(thermostats, "thermostat.temperature", thermostatHandler)
    subscribe(waterSensors, "waterSensor", waterHandler)
    subscribe(smokeSensors, "carbonMonoxideDetector", smokeHandler)
    subscribe(doorControls, "doorControl", doorControlHandler)
}

def doorControlHandler(evt) {
    if (evt.isStateChanged()) {
        sendPushEvent(evt.deviceId,
                "DOOR_CONTROL_SMART_THINGS",
                evt.value
        )
    }
}

def smokeHandler(evt) {
    if (evt.isStateChanged()) {
        sendPushEvent(evt.deviceId,
                "SMOKE_CO_SENSOR_SMART_THINGS",
                evt.value
        )
    }
}

def thermostatHandler(evt) {
    if (evt.isStateChanged()) {
        sendPushEvent(evt.deviceId,
                "THERMOSTAT_SMART_THINGS",
                evt.value
        )
    }
}

def waterHandler(evt) {
    if (evt.isStateChanged()) {
        sendPushEvent(evt.deviceId,
                "WATER_SENSOR_SMART_THINGS",
                evt.value
        )
    }
}

def motionHandler(evt) {
    if ("active" == evt.value) {
        sendPushEvent(evt.deviceId,
                "MOTION_SENSOR_SMART_THINGS",
                evt.value)
    }
}

def switchHandler(evt) {
    log.debug "$evt.deviceId switch changed state to $evt.value"
}

def getDevices() {
    def resp = []
    switches.each {
        resp << extractSwitch(it)
    }
    motionSensors.each {
        resp << extractMotionSensor(it)
    }

    lamps.each {
        resp << extractLamp(it)
    }

    thermostats.each {
        resp << extractThermostat(it)
    }

    return resp
}

def getDevice() {
    def id = params.id
    log.info "Getting device by id " + id
    def device = findDeviceById(id);
    if (device == null) {
        httpError(400, "$id is not a valid id for switch specified")
    } else if (device.type == "SWITCH_SMART_THINGS") {
        return extractSwitch(device.device);
    } else if (device.type == "THERMOSTAT_SMART_THINGS") {
        return extractThermostat(device.device);
    } else if (device.type == "LAMP_SMART_THINGS") {
        return extractLamp(device.device);
    } else if (device.type == "MOTION_SENSOR_SMART_THINGS") {
        return extractMotionSensor(device.device);
    }
    return nil
}

void updateDevice() {
    def id = params.id
    def device = findDeviceById(id)

    if (device == null) {
        httpError(400, "$id is not a valid switch id")
    } else if (device.type == "SWITCH_SMART_THINGS") {
        changeSwitchState(device, request.JSON?.powered)
    } else if (device.type == "THERMOSTAT_SMART_THINGS") {
        def heating_setpoint = request.JSON?.heating_setpoint
        def cooling_setpoint = request.JSON?.cooling_setpoint
        def thermostat_setpoint = request.JSON?.thermostat_setpoint
        if (heating_setpoint != null) {
            device.device.setHeatingSetpoint(heating_setpoint)
        }
        if (cooling_setpoint != null) {
            device.device.setCoolingSetpoint(cooling_setpoint)
        }
        //if (thermostat_setpoint != null) {
        //	device.device.setThermostatSetpoint(thermostat_setpoint)
        //}
    } else if (device.type == "LAMP_SMART_THINGS") {
        def powered = request.JSON?.powered
        def color_h = request.JSON?.color_h
        def color_s = request.JSON?.color_s
        def color_b = request.JSON?.color_b
        def color_temperature = request.JSON?.color_temperature
        if (powered != null) {
            changeSwitchState(device, request.JSON?.powered)
        }
        if (color_h != null) {
            device.device.setHue(color_h)
        }
        if (color_b != null) {
            device.device.setLevel(color_b)
        }
        if (color_s != null) {
            device.device.setSaturation(color_s)
        }
        if (color_temperature != null) {
            device.device.setColorTemperature(color_temperature)
        }
    } else if (device.type == "MOTION_SENSOR_SMART_THINGS") {
        httpError(400, "Unable to control motion sensor")
    }
}

def findDeviceById(id) {
    def device = switches.find { it.id == id }
    def type = "SWITCH_SMART_THINGS"
    if (device == null) {
        device = thermostats.find { it.id == id }
        type = "THERMOSTAT_SMART_THINGS"
    }
    if (device == null) {
        device = lamps.find { it.id == id }
        type = "LAMP_SMART_THINGS"
    }
    if (device == null) {
        device = motionSensors.find { it.id == id }
        type = "MOTION_SENSOR_SMART_THINGS"
    }
    if (device == null) {
        return null;
    }
    return [device: device, type: type];
}


def extractSwitch(it) {
    return [id: it.id, type: "SWITCH_SMART_THINGS", state: [name: it.displayName, powered: it.currentValue("switch") == "on" ? true : false]]
}

def extractMotionSensor(it) {
    return [id: it.id, type: "MOTION_SENSOR_SMART_THINGS", state: [name: it.displayName, state: it.currentValue("motion")]]
}

def extractLamp(it) {
    return [id: it.id, type: "LAMP_SMART_THINGS", state: [name             : it.displayName,
                                                          color_h          : it.currentValue("hue"), color_s: it.currentValue("saturation"), color_b: it.currentValue("level"),
                                                          color_temperature: it.currentValue("colorTemperature"), powered: it.currentValue("switch") == "on" ? true : false]]
}

def extractThermostat(it) {
    return [id: it.id, type: "THERMOSTAT_SMART_THINGS", state: [name                      : it.displayName,
                                                                temperature               : it.currentValue("temperature"),
                                                                heating_setpoint          : it.currentValue("heatingSetpoint"),
                                                                cooling_setpoint          : it.currentValue("coolingSetpoint"),
                                                                thermostat_setpoint       : it.currentValue("thermostatSetpoint"),
                                                                thermostat_mode           : it.currentValue("thermostatMode"),
                                                                thermostat_fan_mode       : it.currentValue("thermostatFanMode"),
                                                                thermostat_operating_state: it.currentValue("thermostatOperatingState")]]
}

def changeSwitchState(device, powered) {
    log.info "Updating device " + device.device.displayName + " to state $powered"
    switch (powered) {
        case "true":
            device.device.on()
            break
        case "false":
            device.device.off()
            break
        default:
            httpError(400, "$powered is not a valid power state for switch specified")
    }
}

def sendPushEvent(pushEvent) {
    def params = [
            uri               : "https://intent-processor-stage.cubic.ai/api/v1/pushEvent",
            headers           : [
                    Authorization: "Bearer -gm-IOuQR3W2Gim8Tjwsuw",
                    Accept       : "/"
            ],
            body              : pushEvent,
            requestContentType: "application/json"
    ]
    try {
        httpPost(params) { resp ->
            log.debug "response data: ${resp.data}"
            log.debug "response contentType: ${resp.contentType}"
        }
    } catch (e) {
        log.debug "something went wrong: $e"
    }
}

def sendPushEvent(id, type, state) {
    sendPushEvent(
            [device_id  : id,
             device_type: type,
             state      : state]
    )
}