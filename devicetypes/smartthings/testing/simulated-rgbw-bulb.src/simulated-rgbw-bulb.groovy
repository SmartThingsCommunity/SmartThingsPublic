/**
 *  Copyright 2017-2018 SmartThings
 *
 *  Device Handler for a simulated mixed-mode RGBW and Tunable White light bulb
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
 *  Date: 2017-10-09
 *
 */
import groovy.transform.Field

// really? colorUtils is missing black?
@Field final Map      BLACK = [name: "Black", rgb: "#000000", h: 0, s: 0, l: 0]

@Field final IntRange PERCENT_RANGE = (0..100)

@Field final IntRange HUE_RANGE = PERCENT_RANGE
@Field final Integer  HUE_STEP = 5
@Field final IntRange SAT_RANGE = PERCENT_RANGE
@Field final Integer  SAT_STEP = 20
@Field final Integer  HUE_SCALE = 1000
@Field final Integer  COLOR_OFFSET = HUE_RANGE.getTo() * HUE_SCALE

@Field final IntRange COLOR_TEMP_RANGE = (2200..7000)
@Field final Integer  COLOR_TEMP_DEFAULT = COLOR_TEMP_RANGE.getFrom() + ((COLOR_TEMP_RANGE.getTo() - COLOR_TEMP_RANGE.getFrom())/2)
@Field final Integer  COLOR_TEMP_STEP = 50 // Kelvin
@Field final List     COLOR_TEMP_EXTRAS = []
@Field final List     COLOR_TEMP_LIST = buildColorTempList(COLOR_TEMP_RANGE, COLOR_TEMP_STEP, COLOR_TEMP_EXTRAS)

@Field final Map MODE = [
    COLOR:	"Color",
    WHITE:	"White",
    OFF: 	"Off"
]

metadata {
    definition (name: "Simulated RGBW Bulb", namespace: "smartthings/testing", author: "SmartThings", ocfDeviceType: "oic.d.light") {
        capability "Health Check"
        capability "Actuator"
        capability "Sensor"
        capability "Light"

        capability "Switch"
        capability "Switch Level"
        capability "Color Control"
        capability "Color Temperature"
        capability "Refresh"
        capability "Configuration"

        attribute  "colorTemperatureRange", "VECTOR3"

        attribute  "bulbMode", "ENUM", ["Color", "White", "Off"]
        attribute  "bulbValue", "STRING"
        attribute  "colorIndicator", "NUMBER"
        command    "simulateBulbState"

        command    "markDeviceOnline"
        command    "markDeviceOffline"
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#FFFFFF", nextState:"turningOn"
                attributeState "turningOn", label:'Turning On', icon:"st.switches.light.off", backgroundColor:"#FFFFFF", nextState:"on"
                attributeState "turningOff", label:'Turning Off', icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"off"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "setLevel"
            }
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action: "setColor"
            }
            tileAttribute ("device.bulbMode", key: "SECONDARY_CONTROL") {
                attributeState "Off", label: '${name}', defaultState: true
                attributeState "White", label: '${name}\nmode'
                attributeState "Color", label: '${name}\nmode'
            }
        }

        valueTile("colorIndicator", "device.colorIndicator", width: 4, height: 2) {
            state("colorIndicator", label: 'Virtual Bulb',
                    // value is simply the color temp in kelvin for color temperature
                    // for color, value is an offset plus the saturation pct plus the  hue pct * 1000
                    // Hues are represented evey 5% from 0-100
                    // Saturations are represented every 20% from 0-100
                    backgroundColors: [
                        [value: 0,      color: "#000000"],  // Black under 1000K
                        [value: 1000,   color: "#FF4300"],  // 1000K				// begin white color temperature
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
                        [value: 100000, color: "#FFFFFF"],  // hue: 0, 	 sat: 0		// Begin color
                        [value: 100020, color: "#FFCCCC"],  // hue: 0, 	 sat: 20
                        [value: 100040, color: "#FF9999"],  // hue: 0, 	 sat: 40
                        [value: 100060, color: "#FF6666"],  // hue: 0, 	 sat: 60
                        [value: 100080, color: "#FF3333"],  // hue: 0, 	 sat: 80
                        [value: 100100, color: "#FF0000"],  // hue: 0, 	 sat: 100
                        [value: 105000, color: "#FFFFFF"],  // hue: 5, 	 sat: 0
                        [value: 105020, color: "#FFDBCC"],  // hue: 5, 	 sat: 20
                        [value: 105040, color: "#FFB899"],  // hue: 5, 	 sat: 40
                        [value: 105060, color: "#FF9466"],  // hue: 5, 	 sat: 60
                        [value: 105080, color: "#FF7033"],  // hue: 5, 	 sat: 80
                        [value: 105100, color: "#FF4D00"],  // hue: 5, 	 sat: 100
                        [value: 110000, color: "#FFFFFF"],  // hue: 10,  sat: 0
                        [value: 110020, color: "#FFEBCC"],  // hue: 10,  sat: 20
                        [value: 110040, color: "#FFD699"],  // hue: 10,  sat: 40
                        [value: 110060, color: "#FFC266"],  // hue: 10,  sat: 60
                        [value: 110080, color: "#FFAD33"],  // hue: 10,  sat: 80
                        [value: 110100, color: "#FF9900"],  // hue: 10,  sat: 100
                        [value: 115000, color: "#FFFFFF"],  // hue: 15,  sat: 0
                        [value: 115020, color: "#FFFACC"],  // hue: 15,  sat: 20
                        [value: 115040, color: "#FFF599"],  // hue: 15,  sat: 40
                        [value: 115060, color: "#FFF066"],  // hue: 15,  sat: 60
                        [value: 115080, color: "#FFEB33"],  // hue: 15,  sat: 80
                        [value: 115100, color: "#FFE600"],  // hue: 15,  sat: 100
                        [value: 120000, color: "#FFFFFF"],  // hue: 20,  sat: 0
                        [value: 120020, color: "#F5FFCC"],  // hue: 20,  sat: 20
                        [value: 120040, color: "#EBFF99"],  // hue: 20,  sat: 40
                        [value: 120060, color: "#E0FF66"],  // hue: 20,  sat: 60
                        [value: 120080, color: "#D6FF33"],  // hue: 20,  sat: 80
                        [value: 120100, color: "#CCFF00"],  // hue: 20,  sat: 100
                        [value: 125000, color: "#FFFFFF"],  // hue: 25,  sat: 0
                        [value: 125020, color: "#E6FFCC"],  // hue: 25,  sat: 20
                        [value: 125040, color: "#CCFF99"],  // hue: 25,  sat: 40
                        [value: 125060, color: "#B3FF66"],  // hue: 25,  sat: 60
                        [value: 125080, color: "#99FF33"],  // hue: 25,  sat: 80
                        [value: 125100, color: "#80FF00"],  // hue: 25,  sat: 100
                        [value: 130000, color: "#FFFFFF"],  // hue: 30,  sat: 0
                        [value: 130020, color: "#D6FFCC"],  // hue: 30,  sat: 20
                        [value: 130040, color: "#ADFF99"],  // hue: 30,  sat: 40
                        [value: 130060, color: "#85FF66"],  // hue: 30,  sat: 60
                        [value: 130080, color: "#5CFF33"],  // hue: 30,  sat: 80
                        [value: 130100, color: "#33FF00"],  // hue: 30,  sat: 100
                        [value: 135000, color: "#FFFFFF"],  // hue: 35,  sat: 0
                        [value: 135020, color: "#CCFFD1"],  // hue: 35,  sat: 20
                        [value: 135040, color: "#99FFA3"],  // hue: 35,  sat: 40
                        [value: 135060, color: "#66FF75"],  // hue: 35,  sat: 60
                        [value: 135080, color: "#33FF47"],  // hue: 35,  sat: 80
                        [value: 135100, color: "#00FF19"],  // hue: 35,  sat: 100
                        [value: 140000, color: "#FFFFFF"],  // hue: 40,  sat: 0
                        [value: 140020, color: "#CCFFE0"],  // hue: 40,  sat: 20
                        [value: 140040, color: "#99FFC2"],  // hue: 40,  sat: 40
                        [value: 140060, color: "#66FFA3"],  // hue: 40,  sat: 60
                        [value: 140080, color: "#33FF85"],  // hue: 40,  sat: 80
                        [value: 140100, color: "#00FF66"],  // hue: 40,  sat: 100
                        [value: 145000, color: "#FFFFFF"],  // hue: 45,  sat: 0
                        [value: 145020, color: "#CCFFF0"],  // hue: 45,  sat: 20
                        [value: 145040, color: "#99FFE0"],  // hue: 45,  sat: 40
                        [value: 145060, color: "#66FFD1"],  // hue: 45,  sat: 60
                        [value: 145080, color: "#33FFC2"],  // hue: 45,  sat: 80
                        [value: 145100, color: "#00FFB2"],  // hue: 45,  sat: 100
                        [value: 150000, color: "#FFFFFF"],  // hue: 50,  sat: 0
                        [value: 150020, color: "#CCFFFF"],  // hue: 50,  sat: 20
                        [value: 150040, color: "#99FFFF"],  // hue: 50,  sat: 40
                        [value: 150060, color: "#66FFFF"],  // hue: 50,  sat: 60
                        [value: 150080, color: "#33FFFF"],  // hue: 50,  sat: 80
                        [value: 150100, color: "#00FFFF"],  // hue: 50,  sat: 100
                        [value: 155000, color: "#FFFFFF"],  // hue: 55,  sat: 0
                        [value: 155020, color: "#CCF0FF"],  // hue: 55,  sat: 20
                        [value: 155040, color: "#99E0FF"],  // hue: 55,  sat: 40
                        [value: 155060, color: "#66D1FF"],  // hue: 55,  sat: 60
                        [value: 155080, color: "#33C2FF"],  // hue: 55,  sat: 80
                        [value: 155100, color: "#00B2FF"],  // hue: 55,  sat: 100
                        [value: 160000, color: "#FFFFFF"],  // hue: 60,  sat: 0
                        [value: 160020, color: "#CCE0FF"],  // hue: 60,  sat: 20
                        [value: 160040, color: "#99C2FF"],  // hue: 60,  sat: 40
                        [value: 160060, color: "#66A3FF"],  // hue: 60,  sat: 60
                        [value: 160080, color: "#3385FF"],  // hue: 60,  sat: 80
                        [value: 160100, color: "#0066FF"],  // hue: 60,  sat: 100
                        [value: 165000, color: "#FFFFFF"],  // hue: 65,  sat: 0
                        [value: 165020, color: "#CCD1FF"],  // hue: 65,  sat: 20
                        [value: 165040, color: "#99A3FF"],  // hue: 65,  sat: 40
                        [value: 165060, color: "#6675FF"],  // hue: 65,  sat: 60
                        [value: 165080, color: "#3347FF"],  // hue: 65,  sat: 80
                        [value: 165100, color: "#001AFF"],  // hue: 65,  sat: 100
                        [value: 170000, color: "#FFFFFF"],  // hue: 70,  sat: 0
                        [value: 170020, color: "#D6CCFF"],  // hue: 70,  sat: 20
                        [value: 170040, color: "#AD99FF"],  // hue: 70,  sat: 40
                        [value: 170060, color: "#8566FF"],  // hue: 70,  sat: 60
                        [value: 170080, color: "#5C33FF"],  // hue: 70,  sat: 80
                        [value: 170100, color: "#3300FF"],  // hue: 70,  sat: 100
                        [value: 175000, color: "#FFFFFF"],  // hue: 75,  sat: 0
                        [value: 175020, color: "#E6CCFF"],  // hue: 75,  sat: 20
                        [value: 175040, color: "#CC99FF"],  // hue: 75,  sat: 40
                        [value: 175060, color: "#B366FF"],  // hue: 75,  sat: 60
                        [value: 175080, color: "#9933FF"],  // hue: 75,  sat: 80
                        [value: 175100, color: "#8000FF"],  // hue: 75,  sat: 100
                        [value: 180000, color: "#FFFFFF"],  // hue: 80,  sat: 0
                        [value: 180020, color: "#F5CCFF"],  // hue: 80,  sat: 20
                        [value: 180040, color: "#EB99FF"],  // hue: 80,  sat: 40
                        [value: 180060, color: "#E066FF"],  // hue: 80,  sat: 60
                        [value: 180080, color: "#D633FF"],  // hue: 80,  sat: 80
                        [value: 180100, color: "#CC00FF"],  // hue: 80,  sat: 100
                        [value: 185000, color: "#FFFFFF"],  // hue: 85,  sat: 0
                        [value: 185020, color: "#FFCCFA"],  // hue: 85,  sat: 20
                        [value: 185040, color: "#FF99F5"],  // hue: 85,  sat: 40
                        [value: 185060, color: "#FF66F0"],  // hue: 85,  sat: 60
                        [value: 185080, color: "#FF33EB"],  // hue: 85,  sat: 80
                        [value: 185100, color: "#FF00E5"],  // hue: 85,  sat: 100
                        [value: 190000, color: "#FFFFFF"],  // hue: 90,  sat: 0
                        [value: 190020, color: "#FFCCEB"],  // hue: 90,  sat: 20
                        [value: 190040, color: "#FF99D6"],  // hue: 90,  sat: 40
                        [value: 190060, color: "#FF66C2"],  // hue: 90,  sat: 60
                        [value: 190080, color: "#FF33AD"],  // hue: 90,  sat: 80
                        [value: 190100, color: "#FF0099"],  // hue: 90,  sat: 100
                        [value: 195000, color: "#FFFFFF"],  // hue: 95,  sat: 0
                        [value: 195020, color: "#FFCCDB"],  // hue: 95,  sat: 20
                        [value: 195040, color: "#FF99B8"],  // hue: 95,  sat: 40
                        [value: 195060, color: "#FF6694"],  // hue: 95,  sat: 60
                        [value: 195080, color: "#FF3370"],  // hue: 95,  sat: 80
                        [value: 195100, color: "#FF004D"],  // hue: 95,  sat: 100
                        [value: 200000, color: "#000000"]   // hue: 100, sat: 100	// Out of bound high rendered as black
                    ]
            )
        }

        valueTile("colorTempControlLabel", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: 'White Color Temp.\n${currentValue}K'
        }

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 1, inactiveLabel: false, range: "(2200..7000)") {
            state "colorTemperature", action: "setColorTemperature"
        }

        valueTile("bulbValue", "bulbValue", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "bulbValue", label: '${currentValue}'
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh", icon: "st.secondary.refresh"
        }

        valueTile("reset", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "Reset", action: "configure"
        }

        standardTile("deviceHealthControl", "device.healthStatus", decoration: "flat", width: 2, height: 2, inactiveLabel: false) {
            state "online",  label: "ONLINE", backgroundColor: "#00A0DC", action: "markDeviceOffline", icon: "st.Health & Wellness.health9", nextState: "goingOffline", defaultState: true
            state "offline", label: "OFFLINE", backgroundColor: "#E86D13", action: "markDeviceOnline", icon: "st.Health & Wellness.health9", nextState: "goingOnline"
            state "goingOnline", label: "Going ONLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
            state "goingOffline", label: "Going OFFLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
        }

        main(["switch"])
        details(["switch", "colorTempControlLabel", "colorTempSliderControl", "bulbValue", "colorIndicator", "refresh", "deviceHealthControl", "reset"])
    }
}

//
// interface methods
//

// parse events into attributes
def parse(String description) {
    log.trace "Executing 'parse' $description"
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
    done()
    return parsedEvents
}

def installed() {
    log.trace "Executing 'installed'"
    configure()
}

def updated() {
    log.trace "Executing 'updated'"
    initialize()
}

//
// command methods
//

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
}

def on() {
    log.trace "Executing 'on'"
    turnOn()
    simulateBulbState(state.lastMode)
    done()
}

def off() {
    log.trace "Executing 'off'"
    turnOff()
    simulateBulbState(MODE.OFF)
    done()
}

def setLevel(levelPercent) {
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

def setSaturation(saturationPercent) {
    log.trace "Executing 'setSaturation' ${saturationPercent}/100"
    Integer currentHue = device.currentValue("hue")
    setColor(currentHue, saturationPercent)
    // setColor will call done() for us
}

def setHue(huePercent) {
    log.trace "Executing 'setHue' ${huePercent}/100"
    Integer currentSaturation = device.currentValue("saturation")
    setColor(huePercent, currentSaturation)
    // setColor will call done() for us
}

/**
 * setColor variant accepting discrete hue and saturation percentages
 * @param Integer huePercent            percentace of hue 0-100
 * @param Integer saturationPercent     percentage of saturtion 0-100
 */
def setColor(Integer huePercent, Integer saturationPercent) {
    log.trace "Executing 'setColor' from separate values hue: $huePercent, saturation: $saturationPercent"
    Map colorHSMap = buildColorHSMap(huePercent, saturationPercent)
    setColor(colorHSMap) // call the capability version method overload
}

/**
 * setColor overload which accepts a hex RGB string
 * @param String hex    RGB color donoted as a hex string in format #1F1F1F
 */
def setColor(String rgbHex) {
    log.trace "Executing 'setColor' from hex $rgbHex"
    if (hex == "#000000") {
        // setting to black? turn it off.
        off()
    } else {
        List hsvList = colorUtil.hexToHsv(rgbHex)
        Map colorHSMap = buildColorHSMap(hsvList[0], hsvList[1])
        setColor(colorHSMap) // call the capability version method overload
    }
}

/**
 * setColor as defined by the Color Control capability
 * even if we had a hex RGB value before, we convert back to it from hue and sat percentages
 * @param colorHSMap
 */
def setColor(Map colorHSMap) {
    log.trace "Executing 'setColor' $colorHSMap"
    Integer boundedHue = boundInt(colorHSMap?.hue?:0, PERCENT_RANGE)
    Integer boundedSaturation = boundInt(colorHSMap?.saturation?:0, PERCENT_RANGE)
    String rgbHex = colorUtil.hsvToHex(boundedHue, boundedSaturation)
    log.debug "bounded hue and saturation: $boundedHue, $boundedSaturation; hex conversion: $rgbHex"
    implicitOn()
    sendEvent(name: "hue", value: boundedHue)
    sendEvent(name: "saturation", value: boundedSaturation)
    sendEvent(name: "color", value: rgbHex)
    simulateBulbState(MODE.COLOR)
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

private initialize() {
    log.trace "Executing 'initialize'"

    sendEvent(name: "colorTemperatureRange", value: COLOR_TEMP_RANGE)
    sendEvent(name: "colorTemperature", value: COLOR_TEMP_DEFAULT)

    sendEvent(name: "hue", value: BLACK.h)
    sendEvent(name: "saturation", value: BLACK.s)
    // make sure to set color attribute!
    sendEvent(name: "color", value: BLACK.rgb)

    sendEvent(name: "level", value: 100)

    sendEvent(name: "switch", value: "off")
    state.lastMode = MODE.COLOR
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

private Map buildColorHSMap(hue, saturation) {
    Map colorHSMap = [hue: 0, saturation: 0]
    try {
        colorHSMap.hue = hue.toFloat().toInteger()
        colorHSMap.saturation = saturation.toFloat().toInteger()
    } catch (NumberFormatException nfe) {
        log.warn "Couldn't transform one of hue ($hue) or saturation ($saturation) to integers: $nfe"
    }
    return colorHSmap
}

/**
 * Call this after all events setting attributes have been sent to simulate the bulb's state
 * @param mode  a member of the MODE constant map
 */
private void simulateBulbState(String mode) {
    log.trace "Executing 'simulateBulbState' $mode"
    String valueText = "---"
    String rgbHex = BLACK.rgb
    Integer colorIndicator = 0
    switch (mode) {
        case MODE.COLOR:
            Integer huePct = device?.currentValue("hue")?:0
            Integer saturationPct = device?.currentValue("saturation")?:0
            colorIndicator = flattenHueSat(huePct, saturationPct) // flattened, scaled & offset hue & sat
            rgbHex = colorUtil.hsvToHex(huePct, saturationPct)
            valueText = "$mode\n$rgbHex"
            state.lastMode = mode
            break;
        case MODE.WHITE:
            Integer kelvin = device?.currentValue("colorTemperature")?:0
            colorIndicator = kelvin  // for tunable white, just use the color temperature
            rgbHex = kelvinToHex(kelvin)
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
    sendEvent(name: "bulbValue", value: valueText)
}

private Integer flattenHueSat(Integer hue, Integer sat) {
    Integer flatHueSat = 0
    if (HUE_RANGE.contains(hue) && SAT_RANGE.contains(sat) ) {
        Integer scaledHue = hue * HUE_SCALE
        flatHueSat = scaledHue + sat + COLOR_OFFSET
    }
    log.debug "flattenHueSat for hue: $hue, sat: $sat comes to $flatHueSat"
    return flatHueSat
}

private Map restoreHueSat(Integer flatHueSat) {
    flatHueSat -= COLOR_OFFSET
    Integer sat = flatHueSat % HUE_SCALE
    Integer hue = flatHueSat.intdiv(HUE_SCALE)
    return [hue: hue, sat: sat]
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
    value = Math.max(theRange.getFrom(), value)
    value = Math.min(theRange.getTo(), value)
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
