/**
 *  Copyright 2017-2018 SmartThings
 *
 *  Device Handler for a simulated mixed-mode RGB light bulb
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
@Field final Integer  COLOR_OFFSET = 0

@Field final Map MODE = [
    COLOR:	"Color",
    OFF: 	"Off"
]

metadata {
    definition (name: "Simulated RGB Bulb", namespace: "smartthings/testing", author: "SmartThings") {
        capability "HealthCheck"
        capability "Actuator"
        capability "Sensor"
        capability "Light"

        capability "Switch"
        capability "Switch Level"
        capability "Color Control"
        capability "Refresh"
        capability "Configuration"

        attribute  "bulbMode", "enum", ["Color", "Off"]
        attribute  "bulbValue", "string"
        attribute  "colorIndicator", "number"
        command    "simulateBulbState"
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
            tileAttribute ("brightnessLabel", key: "SECONDARY_CONTROL") {
                attributeState "Brightness", label: '${name}', defaultState: true
            }
        }

        valueTile("colorIndicator", "device.colorIndicator", width: 4, height: 2) {
            state("colorIndicator", label: 'Virtual Bulb',
                    // value is simply the color temp in kelvin for color temperature
                    // for color, value is an offset plus the saturation pct plus the  hue pct * 1000
                    // Hues are represented evey 5% from 0-100
                    // Saturations are represented every 20% from 0-100
                    backgroundColors: [
                        [value: 0,     color: "#FFFFFF"],  // hue: 0, 	 sat: 0		// Begin color
                        [value: 20,    color: "#FFCCCC"],  // hue: 0, 	 sat: 20
                        [value: 40,    color: "#FF9999"],  // hue: 0, 	 sat: 40
                        [value: 60,    color: "#FF6666"],  // hue: 0, 	 sat: 60
                        [value: 80,    color: "#FF3333"],  // hue: 0, 	 sat: 80
                        [value: 100,   color: "#FF0000"],  // hue: 0, 	 sat: 100
                        [value: 5000,  color: "#FFFFFF"],  // hue: 5, 	 sat: 0
                        [value: 5020,  color: "#FFDBCC"],  // hue: 5, 	 sat: 20
                        [value: 5040,  color: "#FFB899"],  // hue: 5, 	 sat: 40
                        [value: 5060,  color: "#FF9466"],  // hue: 5, 	 sat: 60
                        [value: 5080,  color: "#FF7033"],  // hue: 5, 	 sat: 80
                        [value: 5100,  color: "#FF4D00"],  // hue: 5, 	 sat: 100
                        [value: 10000, color: "#FFFFFF"],  // hue: 10,  sat: 0
                        [value: 10020, color: "#FFEBCC"],  // hue: 10,  sat: 20
                        [value: 10040, color: "#FFD699"],  // hue: 10,  sat: 40
                        [value: 10060, color: "#FFC266"],  // hue: 10,  sat: 60
                        [value: 10080, color: "#FFAD33"],  // hue: 10,  sat: 80
                        [value: 10100, color: "#FF9900"],  // hue: 10,  sat: 100
                        [value: 15000, color: "#FFFFFF"],  // hue: 15,  sat: 0
                        [value: 15020, color: "#FFFACC"],  // hue: 15,  sat: 20
                        [value: 15040, color: "#FFF599"],  // hue: 15,  sat: 40
                        [value: 15060, color: "#FFF066"],  // hue: 15,  sat: 60
                        [value: 15080, color: "#FFEB33"],  // hue: 15,  sat: 80
                        [value: 15100, color: "#FFE600"],  // hue: 15,  sat: 100
                        [value: 20000, color: "#FFFFFF"],  // hue: 20,  sat: 0
                        [value: 20020, color: "#F5FFCC"],  // hue: 20,  sat: 20
                        [value: 20040, color: "#EBFF99"],  // hue: 20,  sat: 40
                        [value: 20060, color: "#E0FF66"],  // hue: 20,  sat: 60
                        [value: 20080, color: "#D6FF33"],  // hue: 20,  sat: 80
                        [value: 20100, color: "#CCFF00"],  // hue: 20,  sat: 100
                        [value: 25000, color: "#FFFFFF"],  // hue: 25,  sat: 0
                        [value: 25020, color: "#E6FFCC"],  // hue: 25,  sat: 20
                        [value: 25040, color: "#CCFF99"],  // hue: 25,  sat: 40
                        [value: 25060, color: "#B3FF66"],  // hue: 25,  sat: 60
                        [value: 25080, color: "#99FF33"],  // hue: 25,  sat: 80
                        [value: 25100, color: "#80FF00"],  // hue: 25,  sat: 100
                        [value: 30000, color: "#FFFFFF"],  // hue: 30,  sat: 0
                        [value: 30020, color: "#D6FFCC"],  // hue: 30,  sat: 20
                        [value: 30040, color: "#ADFF99"],  // hue: 30,  sat: 40
                        [value: 30060, color: "#85FF66"],  // hue: 30,  sat: 60
                        [value: 30080, color: "#5CFF33"],  // hue: 30,  sat: 80
                        [value: 30100, color: "#33FF00"],  // hue: 30,  sat: 100
                        [value: 35000, color: "#FFFFFF"],  // hue: 35,  sat: 0
                        [value: 35020, color: "#CCFFD1"],  // hue: 35,  sat: 20
                        [value: 35040, color: "#99FFA3"],  // hue: 35,  sat: 40
                        [value: 35060, color: "#66FF75"],  // hue: 35,  sat: 60
                        [value: 35080, color: "#33FF47"],  // hue: 35,  sat: 80
                        [value: 35100, color: "#00FF19"],  // hue: 35,  sat: 100
                        [value: 40000, color: "#FFFFFF"],  // hue: 40,  sat: 0
                        [value: 40020, color: "#CCFFE0"],  // hue: 40,  sat: 20
                        [value: 40040, color: "#99FFC2"],  // hue: 40,  sat: 40
                        [value: 40060, color: "#66FFA3"],  // hue: 40,  sat: 60
                        [value: 40080, color: "#33FF85"],  // hue: 40,  sat: 80
                        [value: 40100, color: "#00FF66"],  // hue: 40,  sat: 100
                        [value: 45000, color: "#FFFFFF"],  // hue: 45,  sat: 0
                        [value: 45020, color: "#CCFFF0"],  // hue: 45,  sat: 20
                        [value: 45040, color: "#99FFE0"],  // hue: 45,  sat: 40
                        [value: 45060, color: "#66FFD1"],  // hue: 45,  sat: 60
                        [value: 45080, color: "#33FFC2"],  // hue: 45,  sat: 80
                        [value: 45100, color: "#00FFB2"],  // hue: 45,  sat: 100
                        [value: 50000, color: "#FFFFFF"],  // hue: 50,  sat: 0
                        [value: 50020, color: "#CCFFFF"],  // hue: 50,  sat: 20
                        [value: 50040, color: "#99FFFF"],  // hue: 50,  sat: 40
                        [value: 50060, color: "#66FFFF"],  // hue: 50,  sat: 60
                        [value: 50080, color: "#33FFFF"],  // hue: 50,  sat: 80
                        [value: 50100, color: "#00FFFF"],  // hue: 50,  sat: 100
                        [value: 55000, color: "#FFFFFF"],  // hue: 55,  sat: 0
                        [value: 55020, color: "#CCF0FF"],  // hue: 55,  sat: 20
                        [value: 55040, color: "#99E0FF"],  // hue: 55,  sat: 40
                        [value: 55060, color: "#66D1FF"],  // hue: 55,  sat: 60
                        [value: 55080, color: "#33C2FF"],  // hue: 55,  sat: 80
                        [value: 55100, color: "#00B2FF"],  // hue: 55,  sat: 100
                        [value: 60000, color: "#FFFFFF"],  // hue: 60,  sat: 0
                        [value: 60020, color: "#CCE0FF"],  // hue: 60,  sat: 20
                        [value: 60040, color: "#99C2FF"],  // hue: 60,  sat: 40
                        [value: 60060, color: "#66A3FF"],  // hue: 60,  sat: 60
                        [value: 60080, color: "#3385FF"],  // hue: 60,  sat: 80
                        [value: 60100, color: "#0066FF"],  // hue: 60,  sat: 100
                        [value: 65000, color: "#FFFFFF"],  // hue: 65,  sat: 0
                        [value: 65020, color: "#CCD1FF"],  // hue: 65,  sat: 20
                        [value: 65040, color: "#99A3FF"],  // hue: 65,  sat: 40
                        [value: 65060, color: "#6675FF"],  // hue: 65,  sat: 60
                        [value: 65080, color: "#3347FF"],  // hue: 65,  sat: 80
                        [value: 65100, color: "#001AFF"],  // hue: 65,  sat: 100
                        [value: 70000, color: "#FFFFFF"],  // hue: 70,  sat: 0
                        [value: 70020, color: "#D6CCFF"],  // hue: 70,  sat: 20
                        [value: 70040, color: "#AD99FF"],  // hue: 70,  sat: 40
                        [value: 70060, color: "#8566FF"],  // hue: 70,  sat: 60
                        [value: 70080, color: "#5C33FF"],  // hue: 70,  sat: 80
                        [value: 70100, color: "#3300FF"],  // hue: 70,  sat: 100
                        [value: 75000, color: "#FFFFFF"],  // hue: 75,  sat: 0
                        [value: 75020, color: "#E6CCFF"],  // hue: 75,  sat: 20
                        [value: 75040, color: "#CC99FF"],  // hue: 75,  sat: 40
                        [value: 75060, color: "#B366FF"],  // hue: 75,  sat: 60
                        [value: 75080, color: "#9933FF"],  // hue: 75,  sat: 80
                        [value: 75100, color: "#8000FF"],  // hue: 75,  sat: 100
                        [value: 80000, color: "#FFFFFF"],  // hue: 80,  sat: 0
                        [value: 80020, color: "#F5CCFF"],  // hue: 80,  sat: 20
                        [value: 80040, color: "#EB99FF"],  // hue: 80,  sat: 40
                        [value: 80060, color: "#E066FF"],  // hue: 80,  sat: 60
                        [value: 80080, color: "#D633FF"],  // hue: 80,  sat: 80
                        [value: 80100, color: "#CC00FF"],  // hue: 80,  sat: 100
                        [value: 85000, color: "#FFFFFF"],  // hue: 85,  sat: 0
                        [value: 85020, color: "#FFCCFA"],  // hue: 85,  sat: 20
                        [value: 85040, color: "#FF99F5"],  // hue: 85,  sat: 40
                        [value: 85060, color: "#FF66F0"],  // hue: 85,  sat: 60
                        [value: 85080, color: "#FF33EB"],  // hue: 85,  sat: 80
                        [value: 85100, color: "#FF00E5"],  // hue: 85,  sat: 100
                        [value: 90000, color: "#FFFFFF"],  // hue: 90,  sat: 0
                        [value: 90020, color: "#FFCCEB"],  // hue: 90,  sat: 20
                        [value: 90040, color: "#FF99D6"],  // hue: 90,  sat: 40
                        [value: 90060, color: "#FF66C2"],  // hue: 90,  sat: 60
                        [value: 90080, color: "#FF33AD"],  // hue: 90,  sat: 80
                        [value: 90100, color: "#FF0099"],  // hue: 90,  sat: 100
                        [value: 95000, color: "#FFFFFF"],  // hue: 95,  sat: 0
                        [value: 95020, color: "#FFCCDB"],  // hue: 95,  sat: 20
                        [value: 95040, color: "#FF99B8"],  // hue: 95,  sat: 40
                        [value: 95060, color: "#FF6694"],  // hue: 95,  sat: 60
                        [value: 95080, color: "#FF3370"],  // hue: 95,  sat: 80
                        [value: 95100, color: "#FF004D"],  // hue: 95,  sat: 100
                        [value: 100000, color: "#000000"]  // hue: 100, sat: 100	// Out of bound high rendered as black
                    ]
            )
        }

        valueTile("bulbValue", "bulbValue", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "bulbValue", label: '${currentValue}'
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: "", action: "refresh", icon: "st.secondary.refresh"
        }

        valueTile("reset", "device.switch", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label: "Reset", action: "configure"
        }

        main(["switch"])
        details(["switch", "bulbValue", "colorIndicator", "refresh"])
    }
}

//
// interface methods
//

// parse events into attributes
def parse(String description) {
    log.trace "Executing parse $description"
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
    initialize()
}

def updated() {
    log.trace "Executing 'updated'"
    initialize()
}

//
// command methods
//

def ping() {
    log.trace "Executing 'ping'"
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
    done()
}

def configure() {
    log.trace "Executing 'configure'"
    // this would be for a physical device when it gets a handler assigned to it
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
    log.trace "Executing 'setLevel' ${boundedPercent}%"
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
    log.debug "setColor: bounded hue and saturation: $boundedHue, $boundedSaturation; hex conversion: $rgbHex"
    implicitOn()
    sendEvent(name: "hue", value: boundedHue)
    sendEvent(name: "saturation", value: boundedSaturation)
    sendEvent(name: "color", value: rgbHex)
    simulateBulbState(MODE.COLOR)
    done()
}

private initialize() {
    log.trace "Executing 'initialize'"

    // for HealthCheck
    sendEvent(name: "checkInterval", value: 12 * 60, displayed: false, data: [protocol: "cloud", scheme: "untracked"])

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
            state.lastMode = MODE.COLOR
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
    log.debug "restoreHueSat for $flatHueSat comes to hue: $hue, sat: $sat"
    return [hue: hue, sat: sat]
}

/**
 * Just mark the end of the execution in the log
 */
private void done() {
    log.trace "---- DONE ----"
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
