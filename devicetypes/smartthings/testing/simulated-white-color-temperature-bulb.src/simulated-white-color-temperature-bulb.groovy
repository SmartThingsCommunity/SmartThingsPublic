/**
 *  Copyright 2017 SmartThings
 *
 *  Device Handler for a simulated Tunable White light bulb
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
 *  Author: SmartThings
 *  Date: 2017-08-07
 *
 */
import groovy.transform.Field

// really? colorUtils is missing black?
@Field final Map BLACK = [name: "Black", rgb: "#000000", h: 0, s: 0, l: 0]

@Field final IntRange PERCENT_RANGE = (0..100)

@Field final IntRange COLOR_TEMP_RANGE = (2200..7000)
@Field final Integer  COLOR_TEMP_DEFAULT = COLOR_TEMP_RANGE.getFrom() + ((COLOR_TEMP_RANGE.getTo() - COLOR_TEMP_RANGE.getFrom())/2)
@Field final Integer  COLOR_TEMP_STEP = 50 // Kelvin
@Field final List     COLOR_TEMP_EXTRAS = []
@Field final List     COLOR_TEMP_LIST = buildColorTempList(COLOR_TEMP_RANGE, COLOR_TEMP_STEP, COLOR_TEMP_EXTRAS)

@Field final Map MODE = [
    WHITE:	"White",
    OFF: 	"Off"
]

metadata {
    definition (name: "Simulated White Color Temperature Bulb", namespace: "smartthings/testing", author: "SmartThings", ocfDeviceType: "oic.d.light") {
        capability "HealthCheck"
        capability "Actuator"
        capability "Sensor"
        capability "Light"

        capability "Switch"
        capability "Switch Level"
        capability "Color Temperature"
        capability "Refresh"
        capability "Configuration"

        attribute  "colorTemperatureRange", "VECTOR3"

        attribute  "bulbMode", "ENUM", ["White", "Off"]
        attribute  "bulbValue", "STRING"
        attribute  "colorIndicator", "NUMBER"
        command    "simulateBulbState"

        command    "markDeviceOnline"
        command    "markDeviceOffline"
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#FFFFFF", nextState:"turningOn"
                attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#FFFFFF", nextState:"on"
                attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#00A0DC", nextState:"off"
            }

            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"setLevel"
            }

            tileAttribute ("brightnessLabel", key: "SECONDARY_CONTROL") {
                attributeState "Brightness", label: '${name}', defaultState: true
            }
        }

        valueTile("colorIndicator", "colorIndicator", width: 4, height: 2) {
            state("colorIndicator", label: 'Virtual Bulb',
                    backgroundColors: [
                        [value: 0, 		color: "#000000"],  // Black under 1000K
                        [value: 1000,   color: "#FF4300"],  // 1000K
                        [value: 1500,   color: "#FF6C00"],  // 1500K
                        [value: 2000,   color: "#FF880D"],  // 2000K
                        [value: 2200,   color: "#FF9227"],  // 2200K
                        [value: 2500,   color: "#FF9F46"],  // 2500K
                        [value: 2700,   color: "#FFA657"],  // 2700K
                        [value: 3000,   color: "#FFB16D"],  // 3000K
                        [value: 3500,   color: "#FFC08C"],  // 3500K
                        [value: 4000,   color: "#FFCDA6"],  // 4000K
                        [value: 4500,   color: "#FFD9BB"],  // 4500K
                        [value: 5000,   color: "#FFE4CD"],  // 5000K
                        [value: 5500,   color: "#FFEDDE"],  // 5500K
                        [value: 6000,   color: "#FFF6EC"],  // 6000K
                        [value: 6500,   color: "#FFFEFA"],  // 6500K
                        [value: 7000,   color: "#F2F2FF"],  // 7000K
                        [value: 7500,   color: "#E5EAFF"],  // 7500K
                        [value: 8000,   color: "#DDE5FF"],  // 8000K
                        [value: 8500,   color: "#D6E1FF"],  // 8500K
                        [value: 9000,   color: "#D1DEFF"],  // 9000K
                        [value: 9500,   color: "#CDDCFF"],  // 9500K
                        [value: 10000,  color: "#C9DAFF"],  // 10000K
                        [value: 15000,  color: "#B5CDFF"],  // 15000K
                        [value: 20000,  color: "#AAC6FF"],  // 20000K
                        [value: 25000,  color: "#A3C1FF"],  // 25000K
                        [value: 30000,  color: "#9EBEFF"],  // 30000K
                        [value: 35000,  color: "#9ABBFF"],  // 35000K
                        [value: 40000,  color: "#97B9FF"],  // 40000K
                        [value: 40001,  color: "#000000"]   // 40001K and beyond
                    ]
            )
        }

        valueTile("colorTempControlLabel", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: "White Color Temperature"
        }

        controlTile("colorTempControlSlider", "device.colorTemperature", "slider", width: 4, height: 1, inactiveLabel: false, range: "(2200..7000)") {
            state "colorTemperature", action: "setColorTemperature"
        }

        valueTile("bulbValue", "bulbValue", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "bulbValue", label: '${currentValue}'
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label: "", action: "refresh", icon: "st.secondary.refresh"
        }

        valueTile("reset", "device.switch", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label: "Reset", action: "configure"
        }

        standardTile("deviceHealthControl", "device.healthStatus", decoration: "flat", width: 2, height: 2, inactiveLabel: false) {
            state "online",  label: "ONLINE", backgroundColor: "#00A0DC", action: "markDeviceOffline", icon: "st.Health & Wellness.health9", nextState: "goingOffline", defaultState: true
            state "offline", label: "OFFLINE", backgroundColor: "#E86D13", action: "markDeviceOnline", icon: "st.Health & Wellness.health9", nextState: "goingOnline"
            state "goingOnline", label: "Going ONLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
            state "goingOffline", label: "Going OFFLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
        }

        main(["switch"])
        details(["switch", "colorTempControlLabel", "colorTempControlSlider", "bulbValue", "colorIndicator", "deviceHealthControl", "refresh", "reset"])
    }
}


//
// interface methods
//

// parse events into attributes
def parse(String description) {
    log.trace "parse $description"
    def parsedEvents
    def pair = description?.split(":")
    if (!pair || pair.length < 2) {
        log.warn "parse() could not extract an event name and value from '$description'"
    } else {
        String name = pair[0]?.trim()
        if (name) {
            name = name.replaceAll(~/\W/, "_").replaceAll(~/_{2,}?/, "_")
        }
        parsedEvents = createEvent(name: name, value: pair[1]?.trim())
    }
    return parsedEvents
}

def installed() {
    log.trace "Executing 'installed'"
    configure()
    done()
}

def updated() {
    log.trace "Executing 'updated'"
    initialize()
    done()
}

//
// command methods
//

def ping() {
    refresh()
}

def refresh() {
    log.trace "Executing 'refresh'"
    String currentMode = device.currentValue("bulbMode")
    if (!MODE.containsValue(currentMode)) {
        initialize()
    } else {
        simulateBulbState(currentMode)
    }
}

def configure() {
    log.trace "Executing 'configure'"
    // this would be for a physical device when it gets a handler assigned to it

    // for HealthCheck
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
    markDeviceOnline()

    initialize()
    done()
}

def on() {
    log.trace "Executing 'on'"
    turnOn()
    simulateBulbState(MODE.WHITE)
    done()
}

def off() {
    log.trace "Executing 'off'"
    turnOff()
    simulateBulbState(MODE.OFF)
    done()
}

def setLevel(levelPercent, rate = null) {
    Integer boundedPercent = boundInt(levelPercent, PERCENT_RANGE)
    log.trace "executing 'setLevel' ${boundedPercent}%"
    def effectiveMode = device.currentValue("bulbMode")
    if (boundedPercent > 0) { // just not if the brightness is set to zero
        implicitOn()
        sendEvent(name: "level", value: boundedPercent)
    } else {
        // setting the level to 0% is turning it off, but we don't actually set the level to 0%
        turnOff()
        effectiveMode = MODE.OFF
    }
    simulateBulbState(effectiveMode)
    done()
}

def setColorTemperature(kelvin) {
    Integer kelvinNorm = snapToClosest(kelvin, COLOR_TEMP_LIST)
    log.trace "executing 'setColorTemperature' ${kelvinNorm}K (was ${kelvin}K)"
    implicitOn()
    sendEvent(name: "colorTemperature", value: kelvinNorm)
    simulateBulbState(MODE.WHITE)
    done()
}

def markDeviceOnline() {
    setDeviceHealth("online")
}

def markDeviceOffline() {
    setDeviceHealth("offline")
}

private setDeviceHealth(String healthState) {
    log.debug("healthStatus: ${device.currentValue('healthStatus')}; DeviceWatch-DeviceStatus: ${device.currentValue('DeviceWatch-DeviceStatus')}")
    // ensure healthState is valid
    List validHealthStates = ["online", "offline"]
    healthState = validHealthStates.contains(healthState) ? healthState : device.currentValue("healthStatus")
    // set the healthState
    sendEvent(name: "DeviceWatch-DeviceStatus", value: healthState)
    sendEvent(name: "healthStatus", value: healthState)
}

/**
 * initialize all the attributes and state variable
 */
private initialize() {
    log.trace "Executing 'initialize'"

    sendEvent(name: "colorTemperatureRange", value: COLOR_TEMP_RANGE)
    sendEvent(name: "colorTemperature", value: COLOR_TEMP_DEFAULT)

    sendEvent(name: "level", value: 100)

    sendEvent(name: "switch", value: "off")
    state.lastMode = MODE.WHITE
    simulateBulbState(MODE.OFF)
    done()
}

/**
 * Turns device on if it is not already on
 */
private implicitOn() {
    if (device.currentValue("switch") != "on") {
        turnOn()
    }
}

/**
 * no-frills turn-on, no log, no simulation
 */
private turnOn() {
    sendEvent(name: "switch", value: "on")
}

/**
 * no-frills turn-off, no log, no simulation
 */
private turnOff() {
    sendEvent(name: "switch", value: "off")
}

/**
 * Call this after all events setting attributes have been sent to simulate the bulb's state
 * @param mode  a member of the MODE constant map
 */
private void simulateBulbState(String mode) {
    log.trace "Executing 'simulateBulbState' $mode"
    String valueText = "---"
    String hexColor = BLACK.rgb
    Integer colorIndicator = 0
    switch (mode) {
        case MODE.WHITE:
            Integer kelvin = device?.currentValue("colorTemperature")?:0
            colorIndicator = kelvin  // for tunable white, just use the color temperature
            hexColor = kelvinToHex(kelvin)
            valueText = "$mode\n${kelvin}K"
            state.lastMode = mode
            break;
        case MODE.OFF:
        default:
            mode = MODE.OFF
            valueText = mode
            // don't set state lastMode for Off
            break;
    }
    log.debug "bulbMode: $mode; bulbValue: $valueText; colorIndicator: $colorIndicator"
    sendEvent(name: "colorIndicator", value: colorIndicator)
    sendEvent(name: "bulbMode", value: mode)
    sendEvent(name: "bulbValue", value: valueText.replaceAll("\n", "  "))
}

/**
 * Just mark the end of the execution in the log
 */
private void done() {
    log.trace "---- DONE ----"
}

/**
 * Given a color temperature (in Kelvin), estimate an RGB equivalent
 * @method kelvinToRgb
 * @param  Integer     kelvin        white color temperature in Kelvin
 * @return String      RGB color value in hex
 */
private String kelvinToHex(Integer kelvin) {
    if (!kelvin) kelvin = COLOR_TEMP_DEFAULT
    kelvin = boundInt(kelvin, COLOR_TEMP_RANGE)

    Integer kTemp = kelvin / 100
    def r = 0
    def g = 0
    def b = 0

    // calculate red
    if (kTemp <= 66) {
        r = 255
    } else {
        r = kTemp - 60
        r = 329.698727446 * (r ** -0.1332047592)
        r = boundInt(r, colorUtil.rgbRange)
    }

    //calculate green
    if (kTemp <= 66) {
        g = kTemp
        g = 99.4708025861 * Math.log(g) - 161.1195681661
        g = boundInt(g, colorUtil.rgbRange)
    } else {
        g = kTemp - 60
        g = 288.1221695283 * (g ** -0.0755148492)
        g = boundInt(g, colorUtil.rgbRange)
    }

    // calculate blue
    if (kTemp >= 66) {
        b = 255
    } else if (kTemp <= 19) {
        b = 0
    } else {
        b = kTemp - 10
        b = 138.5177312231 * Math.log(b) - 305.0447927307
        b = boundInt(b, colorUtil.rgbRange)
    }

    return colorUtil.rgbToHex(r, g, b)
}

/**
 * Ensure an integer value is within the provided range, or set it to either extent if it is outside the range.
 * @param Number value         The integer to evaluate
 * @param IntRange theRange     The range within which the value must fall
 * @return Integer
 */
private Integer boundInt(Number value, IntRange theRange) {
    value = Math.max(theRange.getFrom(), Math.min(theRange.getTo(), value))
    return value.toInteger()
}

/**
 * Find periodic values in a range, allowing for inclusion of special values that do not fit the periodicity
 * @param IntRange kRange   define the range for the periodic values.
 * @param Integer kStep     the number between values, based from zero, not the lower bound of the range
 * @param List kExtras      additional values to include. The upper and lower range bounds are already included
 * @return List
 */
private List buildColorTempList(IntRange kRange, Integer kStep, List kExtras) {
    List colorTempList = [kRange.getFrom()] // start with range lower bound
    Integer kFirstNorm = kRange.getFrom() + kStep - (kRange.getFrom() % kStep) // find the first value within thr range which is a factor of kStep
    colorTempList += (kFirstNorm..kRange.getTo()).step(kStep) // now build the periodic list
    colorTempList << kRange.getTo() // include range upper bound
    colorTempList += kExtras // add in extra values
    return colorTempList.sort().unique() // sort and de-dupe
}

/**
 * given a numeric value and a list of acceptable values, return the acceptable value closest to the input value.
 * @param value                 the input value to "snap"
 * @param List validValues      a list of valid values
 * @return Number
 */
private Number snapToClosest(Number value, List validValues) {
    return validValues.sort { (it - value).abs() }.first()
}
