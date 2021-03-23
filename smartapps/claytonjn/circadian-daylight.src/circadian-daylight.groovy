/**
* Circadian Daylight 4.2
*
* This SmartApp synchronizes your color changing lights with perceived color
* temperature of the sky throughout the day. This gives your environment a more
* natural feel, with cooler whites during the midday and warmer tints near
* twilight and dawn.
*
* In addition, the SmartApp sets your lights to a nice warm white at 1% in
* "Sleep" mode, which is far brighter than starlight but won't reset your
* circadian rhythm or break down too much rhodopsin in your eyes.
*
* Human circadian rhythms are heavily influenced by ambient light levels and
* hues. Hormone production, brainwave activity, mood and wakefulness are
* just some of the cognitive functions tied to cyclical natural light.
* http://en.wikipedia.org/wiki/Zeitgeber
*
* Here's some further reading:
*
* http://www.cambridgeincolour.com/tutorials/sunrise-sunset-calculator.htm
* http://en.wikipedia.org/wiki/Color_temperature
*
* Technical notes: I had to make a lot of assumptions when writing this app
* * There are no considerations for weather or altitude, but does use your
* hub's zip code to calculate the sun position.
* * The app doesn't calculate a true "Blue Hour" -- it just sets the lights to
* 2700K (warm white) until your hub goes into Night mode
*
* Version 4.2: April 20, 2017 - Update Dynamic Brightness description
* Version 4.1: June 30, 2016 - Fix checking mode, streamline bulb handlers, fix for allowing night brightness independant from sleep, change dimming bulbs to respect dynamic brightness setting, attempt to return to previous brightness after sleep, fix brightness algorighm.
* Version 4.0: June 13, 2016 - Complete re-write of app. Parent/Child setup; with new ct/brightness algorithms, separate handlers for scheduled and bulb events, and additional settings.
* Version 3.1: May 7, 2016 - Fix a bunch of copy/paste errors resulting in references to the wrong bulb types. No longer need to prevent CD from disabling itself.
* Version 3.0: April 22, 2016 - Taking over the project from Kristopher. Original repo was https://github.com/KristopherKubicki/smartapp-circadian-daylight/
* Version 2.6: March 26, 2016 - Fixes issue with hex colors.  Move your color changing bulbs to Color Temperature instead
* Version 2.5: March 14, 2016 - Add "disabled" switch
* Version 2.4: February 18, 2016 - Mode changes
* Version 2.3: January 23, 2016 - UX Improvements for publication, makes Campfire default instead of Moonlight
* Version 2.2: January 2, 2016 - Add better handling for off() schedules
* Version 2.1: October 27, 2015 - Replace motion sensors with time
* Version 2.0: September 19, 2015 - Update for Hub 2.0
* Version 1.5: June 26, 2015 - Merged with SANdood's optimizations, breaks unofficial LIGHTIFY support
* Version 1.4: May 21, 2015 - Clean up mode handling
* Version 1.3: April 8, 2015 - Reduced Hue IO, increased robustness
* Version 1.2: April 7, 2015 - Add support for LIGHTIFY bulbs, dimmers and user selected "Sleep"
* Version 1.1: April 1, 2015 - Add support for contact sensors
* Version 1.0: March 30, 2015 - Initial release
*
* The latest version of this file can be found at
* https://github.com/claytonjn/SmartThingsPublic/tree/Circadian-Daylight
*
*/

definition(
name: "Circadian Daylight",
namespace: "claytonjn",
author: "claytonjn",
parent: "claytonjn:Circadian Daylight Coordinator",
description: "DO NOT SELECT, USE COORDINATOR!",
category: "Green Living",
iconUrl: "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight.png",
iconX2Url: "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight@2x.png",
iconX3Url: "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight@3x.png"
)

preferences {
    page(name: "bulbsPreferences", nextPage: "dimmingPreferences", install: false, uninstall: true) {
        section("Select each bulb in only one section. Color Temperature bulbs should be most accurate at reflecting natural light.") {
            input "ctBulbs", "capability.colorTemperature", title: "Color Temperature Bulbs", multiple: true, required: false
            input "cBulbs", "capability.colorControl", title: "Color Bulbs", multiple: true, required: false
            input "dBulbs", "capability.switchLevel", title: "Dimming Bulbs", multiple: true, required: false
        }
    }
    page(name: "dimmingPreferences", content: "dimmingPreferences")
    page(name: "sleepPreferences", title: "Sleep Settings", nextPage: "disablePreferences", install: false, uninstall: true) {
        section {
            input "sModes", "mode", title: "When in the selected mode(s), Circadian Daylight will follow the behavior specified below.", multiple:true, required: false
            paragraph "Protip: You can pick 'Nap' modes as well!"
        }
        section("Color Temperature") {
            input "sTemp", "enum", title: "Campfire is easier on your eyes with a yellower hue, Moonlight is a whiter light.", options: ["Campfire", "Moonlight"], required: false, defaultValue: "Campfire"
            paragraph "Note: Moonlight will likely disrupt your circadian rhythm."
        }
        section("Brightness") {
            input "sBright", "enum", title: "Select the desired bulb brightness during sleep modes.", options: ["Don't adjust brightness in Sleep modes", "Automatic", "1%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%", ], defaultValue: "1%", required: false
            paragraph "Note: Anything other than 1% may result in Rhodopsin Bleaching."
        }
    }
    page(name: "disablePreferences", nextPage: "miscPreferences", install: false, uninstall: true) {
        section {
            input "dModes", "mode", title: "Set for specific mode(s)", multiple:true, required: false
            input "dSwitches","capability.switch", title: "Disable Circadian Daylight when these switches are on", multiple:true, required: false
        }
    }
    page(name: "miscPreferences", install: true, uninstall: true) {
        section("Child SmartApp Name"){
            label title: "Assign a name", required: false
        }
    }
}

def dimmingPreferences() {
    return dynamicPage(name: "dimmingPreferences", nextPage: "sleepPreferences", install: false, uninstall: true) {
        section {
            paragraph "Dynamic Brightness automatically dims your lights to mimic natural light.";
            input "dBright", "bool", title: "Dynamic Brightness", required: false, defaultValue: false, submitOnChange: true
            if (dBright) {
                input "bMin", "number", title: "Minimum Brightness (1-100)", required: true, defaultValue: 1
                input "bMax", "number", title: "Maximum Brightness (1-100)", required: true, defaultValue: 100
            }
        }
    }
}

def installed() {
    unsubscribe()
    unschedule()
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

private def initialize() {
    log.debug("initialize() with settings: ${settings}")

    subscribe(app, bulbsHandler)
    if(settings.dSwitches) { subscribe(settings.dSwitches, "switch.off", bulbsHandler) }

    if(settings.ctBulbs) {
		subscribe(settings.ctBulbs, "switch.on", bulbHandler)
		subscribe(settings.ctBulbs, "cdBrightness.true", bulbHandler)
		subscribe(settings.ctBulbs, "cdColor.true", bulbHandler)
	}
    if(settings.cBulbs) {
		subscribe(settings.cBulbs, "switch.on", bulbHandler)
		subscribe(settings.cBulbs, "cdBrightness.true", bulbHandler)
		subscribe(settings.cBulbs, "cdColor.true", bulbHandler)
	}
    if(settings.dBulbs) {
		subscribe(settings.dBulbs, "switch.on", bulbHandler)
		subscribe(settings.dBulbs, "cdBrightness.true", bulbHandler)
	}

    subscribe(location, "mode", modeChangeHandler)
}

private void calcBrightness(sunriseAndSunset) {
    if (settings.dBright == true) {
        def nowDate = new Date()
        if (nowDate > sunriseAndSunset.sunrise && nowDate < sunriseAndSunset.sunset) { state.brightness = settings.bMax } //Before sunset/after sunrise
        else {
            def nowTime = nowDate.getTime()
            def sunsetTime = sunriseAndSunset.sunset.getTime()
            def sunriseTime = sunriseAndSunset.sunrise.getTime()
            if(nowTime < sunriseTime) { //If it's morning, use estimated sunset from the night before
                sunsetTime = sunriseAndSunset.sunset.getTime() - (1000*60*60*24)
            } else if(nowTime > sunsetTime) { //If it's evening, use estimated sunset for the next day
                sunriseTime = sunriseAndSunset.sunrise.getTime() + (1000*60*60*24)
            }
            def nightLength = sunriseTime - sunsetTime

            //Generate brightness parabola from points
            //Specify double type or calculations fail
            double x1 = sunsetTime
            double y1 = settings.bMax
            double x2 = sunsetTime+(nightLength/2)
            double y2 = settings.bMin
            double x3 = sunriseTime
            double y3 = settings.bMax
            double a1 = -x1**2+x2**2
            double b1 = -x1+x2
            double d1 = -y1+y2
            double a2 = -x2**2+x3**2
            double b2 = -x2+x3
            double d2 = -y2+y3
            double bm = -(b2/b1)
            double a3 = bm*a1+a2
            double d3 = bm*d1+d2
            double a = d3/a3
            double b = (d1-a1*a)/b1
            double c = y1-a*x1**2-b*x1
            double brightness = a*nowTime**2+b*nowTime+c
            double stRange = (100 - 1)
        	double hueRange = (254 - 1)
        	double hueBri = Math.round((((brightness - 1) * hueRange) / stRange) + 1) as Integer //Round to Hue brightness range
            state.brightness = Math.round((((hueBri - 1) * stRange) / hueRange) + 1) as Integer
            log.debug "Brightness set to ${state.brightness}"
        }
    } else { state.brightness = NULL }
}

private void calcSleepColorTemperature() {
    switch (settings.sTemp) {
        case "Campfire":
            state.colorTemperature = 2000
            break
        case "Moonlight":
            state.colorTemperature = 4100
            break
    }
    log.debug "Color Temperature set to ${state.colorTemperature}"
}

private void calcSleepBrightness() {
    switch (settings.sBright) {
        case "Don't adjust brightness in Sleep modes":
            state.brightness = NULL
            break
        case "Automatic":
            break
        case "1%":
            state.brightness = 1
            break
        case "10%":
            state.brightness = 10
            break
        case "20%":
            state.brightness = 20
            break
        case "30%":
            state.brightness = 30
            break
        case "40%":
            state.brightness = 40
            break
        case "50%":
            state.brightness = 50
            break
        case "60%":
            state.brightness = 60
            break
        case "70%":
            state.brightness = 70
            break
        case "80%":
            state.brightness = 80
            break
        case "90%":
            state.brightness = 90
            break
        case "100%":
            state.brightness = 100
            break
    }
    log.debug "Brightness set to ${state.brightness}"
}

def bulbsHandler(evt = NULL, sunriseAndSunset = NULL) {
    if (settings.dModes != NULL) {
        if (!settings.dModes.contains(location.mode)) {
            if (settings.sModes == NULL) { return }
            if (settings.sModes != NULL) {
                if (!settings.sModes.contains(location.mode)) { return }
            }
        }
    }

    for (dSwitch in settings.dSwitches) {
        if(dSwitch.currentSwitch == "on") { return }
    }

    if (sunriseAndSunset != NULL) { calcBrightness(sunriseAndSunset) }
    state.colorTemperature = parent.getColorTemperature()
    log.debug "Color Temperature set to ${state.colorTemperature}"

    //Behavior in sleep mode
    if(location.mode in settings.sModes) {
        calcSleepBrightness()
        calcSleepColorTemperature()
    }

    //Minimize reading state variables
    def brightness = state.brightness
    def colorTemperature = state.colorTemperature
    def hex = rgbToHex(ctToRGB(colorTemperature)).toUpperCase()
    def hsv = rgbToHSV(ctToRGB(colorTemperature))

    for(ctBulb in settings.ctBulbs) { setCTBulb(ctBulb, brightness, colorTemperature) }
    for(cBulb in settings.cBulbs) { setCBulb(cBulb, brightness, hex, hsv) }
    for(dBulb in settings.dBulbs) { setDBulb(dBulb, brightness) }
}

def bulbHandler(evt) {
    if (settings.dModes != NULL) {
        if (!settings.dModes.contains(location.mode)) {
            if (settings.sModes == NULL) { return }
            if (settings.sModes != NULL) {
                if (!settings.sModes.contains(location.mode)) { return }
            }
        }
    }

    for (dSwitch in settings.dSwitches) {
        if(dSwitch.currentSwitch == "on") { return }
    }

    state.colorTemperature = parent.getColorTemperature()
    log.debug "Color Temperature set to ${state.colorTemperature}"

    //Behavior in sleep mode
    if(location.mode in settings.sModes) {
        calcSleepBrightness()
        calcSleepColorTemperature()
    }

    if(evt.device.deviceNetworkId in settings.ctBulbs?.deviceNetworkId) {
        //Minimize reading state variables
        def brightness = state.brightness
        def colorTemperature = state.colorTemperature

        setCTBulb(evt.device, brightness, colorTemperature)
    }
    if(evt.device.deviceNetworkId in settings.cBulbs?.deviceNetworkId) {
        //Minimize reading state variables
        def brightness = state.brightness
        def colorTemperature = state.colorTemperature
        def hex = rgbToHex(ctToRGB(colorTemperature)).toUpperCase()
        def hsv = rgbToHSV(ctToRGB(colorTemperature))

        setCBulb(evt.device)
    }
    if(evt.device.deviceNetworkId in settings.dBulbs?.deviceNetworkId) {
        //Minimize reading state variables
        def brightness = state.brightness

        setDBulb(evt.device, brightness)
    }
}

private void setCTBulb(ctBulb, brightness = state.brightness, colorTemperature = state.colorTemperature) {
    if(ctBulb.currentValue("switch") == "on") {
        if(brightness != NULL && ctBulb.currentValue("cdBrightness") != "false") {
            if(ctBulb.currentValue("level") != brightness) {
                if(settings.dBright != true && settings.sModes != NULL && !settings.sModes.contains(location.currentMode) && state.sBulbs.containsKey(ctBulb.deviceNetworkId)) {
                    ctBulb.setLevel(state.sBulbs.get(ctBulb.deviceNetworkId))
                    log.debug "${ctBulb} level restored to ${state.sBulbs.get(ctBulb.deviceNetworkId)}"
                    state.sBulbs.remove(ctBulb.deviceNetworkId)
                } else {
                    ctBulb.setLevel(brightness)
                    log.debug "${ctBulb} level set to ${brightness}"
                }
            }
        }
        if(ctBulb.currentValue("cdColor") != "false") {
            if(ctBulb.currentValue("colormode") != "ct" || ctBulb.currentValue("colorTemperature") != Math.round(colorTemperature) as Integer) {
                ctBulb.setColorTemperature(colorTemperature)
                log.debug "${ctBulb} color temperature set to ${colorTemperature}"
            }
        }
    }
}

private void setCBulb(cBulb, brightness = state.brightness, hex = rgbToHex(ctToRGB(state.colorTemperature)).toUpperCase(), hsv = rgbToHSV(ctToRGB(state.colorTemperature))) {
    def color = [hex: hex, hue: hsv.h, saturation: hsv.s]

    if(cBulb.currentValue("switch") == "on") {
        if(brightness != NULL && cBulb.currentValue("cdBrightness") != "false") {
            if(settings.dBright != true && settings.sModes != NULL && !settings.sModes.contains(location.currentMode) && state.sBulbs.containsKey(cBulb.deviceNetworkId)) {
                color.level = state.sBulbs.get(cBulb.deviceNetworkId)
                log.debug "${cBulb} level restored to ${state.sBulbs.get(cBulb.deviceNetworkId)}"
                state.sBulbs.remove(cBulb.deviceNetworkId)
            } else {
                color.level = brightness
                log.debug "${cBulb} level set to ${brightness}"
            }
        }
        if(cBulb.currentValue("cdColor") != "false") {
            if((cBulb.currentValue("colormode") != "xy" && cBulb.currentValue("colormode") != "hs") || cBulb.currentValue("color") != hex) {
                cBulb.setColor(color)
                log.debug "${cBulb} color set to ${color}"
            }
        }
    }
}

private void setDBulb(dBulb, brightness = state.brightness) {
    if(dBulb.currentValue("switch") == "on") {
        if(brightness != NULL && dBulb.currentValue("cdBrightness") != "false") {
            if(dBulb.currentValue("level") != brightness) {
                if(settings.dBright != true && settings.sModes != NULL && !settings.sModes.contains(location.currentMode) && state.sBulbs.containsKey(dBulb.deviceNetworkId)) {
                    dBulb.setLevel(state.sBulbs.get(dBulb.deviceNetworkId))
                    log.debug "${dBulb} level restored to ${state.sBulbs.get(dBulb.deviceNetworkId)}"
                    state.sBulbs.remove(dBulb.deviceNetworkId)
                } else {
                    dBulb.setLevel(brightness)
                    log.debug "${dBulb} level set to ${brightness}"
                }
            }
        }
    }
}

def modeChangeHandler(evt) {
    if (evt.value in settings.sModes) {
        for (ctBulb in settings.ctBulbs) {
            if (state.sBulbs == NULL || !state.sBulbs.containsKey(ctBulb.deviceNetworkId)) {
                state.sBulbs.put(ctBulb.deviceNetworkId, ctBulb.currentValue("level"))
            }
        }
        for (cBulb in settings.cBulbs) {
            if (state.sBulbs == NULL || !state.sBulbs.containsKey(cBulb.deviceNetworkId)) {
                state.sBulbs.put(cBulb.deviceNetworkId, cBulb.currentValue("level"))
            }
        }
        for (dBulb in settings.dBulbs) {
            if (state.sBulbs == NULL || !state.sBulbs.containsKey(dBulb.deviceNetworkId)) {
                state.sBulbs.put(dBulb.deviceNetworkId, dBulb.currentValue("level"))
            }
        }
        state.sModes = true
    }
}

// Based on color temperature converter from
// http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
// This will not work for color temperatures below 1000 or above 40000
def ctToRGB(ct) {

    if(ct < 1000) { ct = 1000 }
    if(ct > 40000) { ct = 40000 }

    ct = ct / 100

    //red
    def r
    if(ct <= 66) { r = 255 }
    else { r = 329.698727446 * ((ct - 60) ** -0.1332047592) }
    if(r < 0) { r = 0 }
    if(r > 255) { r = 255 }

    //green
    def g
    if (ct <= 66) { g = 99.4708025861 * Math.log(ct) - 161.1195681661 }
    else { g = 288.1221695283 * ((ct - 60) ** -0.0755148492) }
    if(g < 0) { g = 0 }
    if(g > 255) { g = 255 }

    //blue
    def b
    if(ct >= 66) { b = 255 }
    else if(ct <= 19) { b = 0 }
    else { b = 138.5177312231 * Math.log(ct - 10) - 305.0447927307 }
    if(b < 0) { b = 0 }
    if(b > 255) { b = 255 }

    def rgb = [:]
    rgb = [r: Math.round(r) as Integer, g: Math.round(g) as Integer, b: Math.round(b) as Integer]
    rgb
}

def rgbToHex(rgb) {
	return "#" + Integer.toHexString(rgb.r).padLeft(2,'0') + Integer.toHexString(rgb.g).padLeft(2,'0') + Integer.toHexString(rgb.b).padLeft(2,'0')
}

//http://www.rapidtables.com/convert/color/rgb-to-hsv.htm
def rgbToHSV(rgb) {
	def h, s, v

    def r = rgb.r / 255
    def g = rgb.g / 255
    def b = rgb.b / 255

    def max = [r, g, b].max()
    def min = [r, g, b].min()

    def delta = max - min

    //hue
    if(delta == 0) { h = 0}
    else if(max == r) {
    	double dub = (g - b) / delta
        h = 60 * (dub % 6)
	}
    else if(max == g) { h = 60 * (((b - r) / delta) + 2) }
    else if(max == b) { h = 60 * (((r - g) / delta) + 4) }

    //saturation
    if(max == 0) { s = 0 }
    else { s = (delta / max) * 100 }

    //value
    v = max * 100

    def degreesRange = (360 - 0)
    def percentRange = (100 - 0)

    return [h: Math.round((h * percentRange) / degreesRange) as Integer, s: Math.round((s * percentRange) / degreesRange) as Integer, v: Math.round(v) as Integer]
}