/**
* Circadian Daylight 2.6
*
* This SmartApp synchronizes your color changing lights with local perceived color
* temperature of the sky throughout the day. This gives your environment a more
* natural feel, with cooler whites during the midday and warmer tints near twilight
* and dawn.
*
* In addition, the SmartApp sets your lights to a nice cool white at 1% in
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
* * The Hue bulbs are only capable of producing a true color spectrum from
* 2700K to 6000K. The Hue Pro application indicates the range is
* a little wider on each side, but I stuck with the Philips
*  documentation
* * I aligned the color space to CIE with white at D50. I suspect "true"
* white for this application might actually be D65, but I will have
* to recalculate the color temperature if I move it.
* * There are no considerations for weather or altitude, but does use your
* hub's zip code to calculate the sun position.
* * The app doesn't calculate a true "Blue Hour" -- it just sets the lights to
* 2700K (warm white) until your hub goes into Night mode
*
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
* https://github.com/KristopherKubicki/smartapp-circadian-daylight/
*
*/

definition(
name: "Circadian Daylight",
namespace: "KristopherKubicki",
author: "kristopher@acm.org",
description: "Sync your color changing lights and dimmers with natural daylight hues to improve your cognitive functions and restfulness.",
category: "Green Living",
iconUrl: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/mindcontrol.png",
iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/mindcontrol@2x.png"
)

preferences {
    section("Thank you for installing Circadian Daylight! This application dims and adjusts the color temperature of your lights to match the state of the sun, which has been proven to aid in cognitive functions and restfulness. The default options are well suited for most users, but feel free to tweak accordingly!") {
    }
    section("Control these bulbs; Select each bulb only once") {
        input "ctbulbs", "capability.colorTemperature", title: "Which Temperature Changing Bulbs?", multiple:true, required: false
        input "bulbs", "capability.colorControl", title: "Which Color Changing Bulbs?", multiple:true, required: false
        input "dimmers", "capability.switchLevel", title: "Which Dimmers?", multiple:true, required: false
    }
    section("What are your 'Sleep' modes? The modes you pick here will dim your lights and filter light to a softer, yellower hue to help you fall asleep easier. Protip: You can pick 'Nap' modes as well!") {
        input "smodes", "mode", title: "What are your Sleep modes?", multiple:true, required: false
    }
    section("Override Constant Brightness (default) with Dynamic Brightness? If you'd like your lights to dim as the sun goes down, override this option. Most people don't like it, but it can look good in some settings.") {
        input "dbright","bool", title: "On or off?", required: false
    }
    section("Override night time Campfire (default) with Moonlight? Circadian Daylight by default is easier on your eyes with a yellower hue at night. However if you'd like a whiter light instead, override this option. Note: this will likely disrupt your circadian rhythm.") {
        input "dcamp","bool", title: "On or off?", required: false
    }
    section("Override night time Dimming (default) with Rhodopsin Bleaching? Override this option if you would not like Circadian Daylight to dim your lights during your Sleep modes. This is definitely not recommended!") {
        input "ddim","bool", title: "On or off?", required: false
    }
    section("Disable Circadian Daylight when the following switches are on:") {
        input "dswitches","capability.switch", title: "Switches", multiple:true, required: false
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
    if(ctbulbs) { subscribe(ctbulbs, "switch.on", modeHandler) }
    if(bulbs) { subscribe(bulbs, "switch.on", modeHandler) }
    if(dimmers) { subscribe(dimmers, "switch.on", modeHandler) }
    if(dswitches) { subscribe(dswitches, "switch.off", modeHandler) }
    subscribe(location, "mode", modeHandler)
    
    // revamped for sunset handling instead of motion events
    subscribe(location, "sunset", modeHandler)
    subscribe(location, "sunrise", modeHandler)
    schedule("0 */15 * * * ?", modeHandler)
    subscribe(app,modeHandler)
    subscribe(location, "sunsetTime", scheduleTurnOn)
    // rather than schedule a cron entry, fire a status update a little bit in the future recursively
    scheduleTurnOn()
}

def scheduleTurnOn() {
    def int iterRate = 20
    
    // get sunrise and sunset times
    def sunRiseSet = getSunriseAndSunset()
    def sunriseTime = sunRiseSet.sunrise
    log.debug("sunrise time ${sunriseTime}")
    def sunsetTime = sunRiseSet.sunset
    log.debug("sunset time ${sunsetTime}")
    
    if(sunriseTime.time > sunsetTime.time) {
        sunriseTime = new Date(sunriseTime.time - (24 * 60 * 60 * 1000))
    }
    
    def runTime = new Date(now() + 60*15*1000)
    for (def i = 0; i < iterRate; i++) {
        def long uts = sunriseTime.time + (i * ((sunsetTime.time - sunriseTime.time) / iterRate))
        def timeBeforeSunset = new Date(uts)
        if(timeBeforeSunset.time > now()) {
            runTime = timeBeforeSunset
            last
        }
    }
    
    log.debug "checking... ${runTime.time} : $runTime"
    if(state.nextTime != runTime.time) {
        state.nextTimer = runTime.time
        log.debug "Scheduling next step at: $runTime (sunset is $sunsetTime) :: ${state.nextTimer}"
        runOnce(runTime, modeHandler)
    }
}


// Poll all bulbs, and modify the ones that differ from the expected state
def modeHandler(evt) {
    for (dswitch in dswitches) {
        if(dswitch.currentSwitch == "on") {
            return
        }
    }
    
    def ct = getCT()
    def hex = getHex()
    def hsv = getHSV()
    def bright = getBright()
    
    for(ctbulb in ctbulbs) {
        if(ctbulb.currentValue("switch") == "on") {
            if((settings.dbright == true || location.mode in settings.smodes) && ctbulb.currentValue("level") != bright) {
                ctbulb.setLevel(bright)
            }
            if(ctbulb.currentValue("colorTemperature") != ct) {
                ctbulb.setColorTemperature(ct)
            }
        }
    }
    def color = [hex: hex, hue: hsv.h, saturation: hsv.s, level: bright]
    for(bulb in bulbs) {
        if(bulb.currentValue("switch") == "on") {
			def tmp = bulb.currentValue("color")
            if(bulb.currentValue("color") != hex) {
            	if(settings.dbright == true || location.mode in settings.smodes) { 
	            	color.value = bright
                } else {
					color.value = bulb.currentValue("level")
				}
            	def ret = bulb.setColor(color)
			}
        }
    }
    for(dimmer in dimmers) {
        if(dimmer.currentValue("switch") == "on") {
        	if(dimmer.currentValue("level") != bright) {
            	dimmer.setLevel(bright)
            }
        }
    }
    
    scheduleTurnOn()
}

def getCTBright() {
    def after = getSunriseAndSunset()
    def midDay = after.sunrise.time + ((after.sunset.time - after.sunrise.time) / 2)
    
    def currentTime = now()
    def float brightness = 1
    def int colorTemp = 2700
    if(currentTime > after.sunrise.time && currentTime < after.sunset.time) {
        if(currentTime < midDay) {
            colorTemp = 2700 + ((currentTime - after.sunrise.time) / (midDay - after.sunrise.time) * 3800)
            brightness = ((currentTime - after.sunrise.time) / (midDay - after.sunrise.time))
        }
        else {
            colorTemp = 6500 - ((currentTime - midDay) / (after.sunset.time - midDay) * 3800)
            brightness = 1 - ((currentTime - midDay) / (after.sunset.time - midDay))
            
        }
    }
    
    if(settings.dbright == false) {
        brightness = 1
    }
    
	if(location.mode in settings.smodes) {
		if(currentTime > after.sunset.time) {
			if(settings.dcamp == true) {
				colorTemp = 6500
			}
			else {
				colorTemp = 2700
			}
		}
		if(settings.ddim == false) {
			brightness = 0.01
		}
	}
    
    def ct = [:]
    ct = [colorTemp: colorTemp, brightness: Math.round(brightness * 100)]
    ct
}

def getCT() {
	def ctb = getCTBright()
    //log.debug "Color Temperature: " + ctb.colorTemp
    return ctb.colorTemp
}

def getHex() {
	def ct = getCT()
    //log.debug "Hex: " + rgbToHex(ctToRGB(ct)).toUpperCase()
    return rgbToHex(ctToRGB(ct)).toUpperCase()
}

def getHSV() {
	def ct = getCT()
    //log.debug "HSV: " + rgbToHSV(ctToRGB(ct))
    return rgbToHSV(ctToRGB(ct))
}

def getBright() {
	def ctb = getCTBright()
    //log.debug "Brightness: " + ctb.brightness
    return ctb.brightness
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
    rgb = [r: r as Integer, g: g as Integer, b: b as Integer]
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
    
    return [h: ((h * percentRange) / degreesRange) as Integer, s: ((s * percentRange) / degreesRange) as Integer, v: v as Integer]
}