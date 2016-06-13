/**
* Circadian Daylight 3.1
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
* * There are no considerations for weather or altitude, but does use your
* hub's zip code to calculate the sun position.
* * The app doesn't calculate a true "Blue Hour" -- it just sets the lights to
* 2700K (warm white) until your hub goes into Night mode
*
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
description: "Sync your color changing lights and dimmers with natural daylight hues to improve your cognitive functions and restfulness.",
category: "Green Living",
iconUrl: "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight.png",
iconX2Url: "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight@2x.png",
iconX3Url: "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight@3x.png"
)

preferences {
    page(name: "bulbsPreferences", nextPage: "dimmingPreferences", install: false, uninstall: true) {
        section {
            paragraph "Thank you for installing Circadian Daylight! This application adjusts your lights to simulate the light of the sun, which has been proven to aid in cognitive functions and restfulness."
        }
        section("Select each bulb in only one section. Color Temperature bulbs should be most accurate at reflecting natural light.") {
            input "ctBulbs", "capability.colorTemperature", title: "Color Temperature Bulbs", multiple: true, required: false
            input "cBulbs", "capability.colorControl", title: "Color Bulbs", multiple: true, required: false
            input "dBulbs", "capability.switchLevel", title: "Dimming Bulbs", multiple: true, required: false
        }
    }
    page(name: "dimmingPreferences", nextPage: "sleepPreferences", install: false, uninstall: true) {
        section {
            paragraph "Dynamic Brightness automatically dims your lights based on natural light.";
            input "dBright", "bool", title: "Dynamic Brightness", required: false, defaultValue: false
        }
    }
    page(name: "sleepPreferences", title: "Sleep Settings", nextPage: "locationPreferences", install: false, uninstall: true) {
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
    page(name: "locationPreferences", nextPage: "disablePreferences", install: false, uninstall: true) {
        section("Zip Code Override") {
            input "lZip", "number", title: "Change if you want to simulate behavior of a zip code other than the one set for your SmartThings hub, or if you don't have a location set for your SmartThings hub.", required: false, defaultValue: location.zipCode
        }
        section("Sunrise Offset") {
            input "lSunriseOffset", "number", title: "Number of minutes you want to offset from sunrise to adjust Circadian Daylight behavior.", required: false, defaultValue: "0"
        }
        section("Sunset Offset") {
            input "lSunsetOffset", "number", title: "Number of minutes you want to offset from sunset to adjust Circadian Daylight behavior.", required: false, defaultValue: "0"
        }
        section {
            paragraph "The following settings cause Circadian Daylight to behave consistantly throughout the year, rather than matching the natural change in daylight."
        }
        section("Sunrise Time") {
            input "lSunriseTime", "time", title: "Enter a specific time you want Circadian Daylight to use for sunrise.", required: false
        }
        section("Sunset Time") {
            input "lSunsetTime", "time", title: "Enter a specific time you want Circadian Daylight to use for sunset.", required: false
        }
    }
    page(name: "disablePreferences", nextPage: "miscPreferences", install: false, uninstall: true) {
        section {
            input "dModes", "mode", title: "Set for specific mode(s)", multiple:true, required: false
            input "dSwitches","capability.switch", title: "Disable Circadian Daylight when these switches are on", multiple:true, required: false
        }
    }
    page(name: "miscPreferences", content:"miscPreferences")
}

def miscPreferences() {
    return dynamicPage(name: "miscPreferences", install: true, uninstall: true) {
        section("SmartApp Name"){
            label title: "Assign a name", required: false
        }
        section("Update Notifications") {
			paragraph 	"Get push notifications when an update is pushed to GitHub."
			input(		name: 			"updateNotifications",
						type:			"bool",
						title:			"Update Notifications",
						submitOnChange:	true	)
			if (updateNotifications) {
				input("recipients", "contact", title: "Send notifications to") {
					input "updatePush", "bool", title: "Send push notifications", required: false
				}
				input(		name:			"gitHubBranch",
							type:			"enum",
							title: 			"Branch",
							description:	"Get notifications for the stable or beta branch?",
							options:		["Stable", "Beta"],
							defaultValue:	"Stable",
							multiple:		true,
							required:		true	)
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

    // revamped for sunset handling instead of motion events
    subscribe(location, "sunset", setHandler)
    subscribe(location, "sunrise", setHandler)
    schedule("0 */15 * * * ?", setHandler)
    subscribe(location, "mode", setHandler)
    setHandler() //Set state variables from initial install

    if(settings.dSwitches) { subscribe(settings.dSwitches, "switch.off", bulbsHandler) }
    subscribe(app, bulbsHandler)

    if(settings.ctBulbs) {
		subscribe(settings.ctBulbs, "switch.on", ctBulbHandler)
		subscribe(settings.ctBulbs, "cdBrightness.true", ctBulbHandler)
		subscribe(settings.ctBulbs, "cdColor.true", ctBulbHandler)
	}
    if(settings.cBulbs) {
		subscribe(settings.cBulbs, "switch.on", cBulbHandler)
		subscribe(settings.cBulbs, "cdBrightness.true", cBulbHandler)
		subscribe(settings.cBulbs, "cdColor.true", cBulbHandler)
	}
    if(settings.dBulbs) {
		subscribe(settings.dBulbs, "switch.on", dBulbHandler)
		subscribe(settings.dBulbs, "cdBrightness.true", dBulbHandler)
	}
}

void setHandler(evt = NULL) {
    if(!settings.dModes.contains(location.mode) && !settings.sModes.contains(location.mode)) { return }

    def sunriseAndSunset = getSunriseAndSunset(zipCode: settings.lZip, sunriseOffset: settings.lSunriseOffset, sunsetOffset: settings.lSunsetOffset)
    if(lSunriseTime) { sunriseAndSunset.sunrise = lSunriseTime }
    if(lSunsetTime) { sunriseAndSunset.sunset = lSunsetTime }

    if (settings.updateNotifications == true) { checkForUpdates() }

    calcColorTemperature(sunriseAndSunset)
    calcBrightness(sunriseAndSunset)
    bulbsHandler()
}

private void calcColorTemperature(sunriseAndSunset) {
    def nowDate = new Date()
    def ctMin = 2700
    def ctMax = 5500
    if (nowDate < sunriseAndSunset.sunrise || nowDate > sunriseAndSunset.sunset) { state.colorTemperature = ctMin } //before sunrise / after sunset
    else {
        def nowTime = nowDate.getTime()
        def sunriseTime = sunriseAndSunset.sunrise.getTime()
        def sunsetTime = sunriseAndSunset.sunset.getTime()
        def dayLength = sunsetTime - sunriseTime

        //Generate color temperature parabola from points
        //Specify double type or calculations fail
        double x1 = sunriseTime
        double y1 = ctMin
        double x2 = sunriseTime+(dayLength/2)
        double y2 = ctMax
        double x3 = sunsetTime
        double y3 = ctMin
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
        state.colorTemperature = a*nowTime**2+b*nowTime+c
    }
}

private void calcBrightness(sunriseAndSunset) {
    if(settings.dBright == true) {
        def nowDate = new Date()
        def bMin = 1
        def bMax = 100
        if (nowDate > sunriseAndSunset.sunrise && nowDate < sunriseAndSunset.sunset) { state.brightness = bMax } //Before sunset/after sunrise
        else {
            def nowTime = nowDate.getTime()
            def sunsetTime = sunriseAndSunset.sunset.getTime()
            def sunriseTime = sunriseAndSunset.sunrise.getTime()
            if((nowDate..sunriseAndSunset.sunrise).size() == 1) { //If sunrise time hasn't updated yet for the next day, estimate tomorrow's sunrise from today's
                sunriseTime = (sunsetTime - sunriseAndSunset.sunrise.getTime()) + sunsetTime
            }
            def dayLength = sunriseTime - sunsetTime

            //Generate brightness parabola from points
            //Specify double type or calculations fail
            double x1 = sunsetTime
            double y1 = bMax
            double x2 = sunsetTime+(dayLength/2)
            double y2 = bMin
            double x3 = sunriseTime
            double y3 = bMax
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
            state.brightness = a*nowTime**2+b*nowTime+c
        }
    } else { settings.brightness = NULL }
}

private void calcSleepColorTemperature() {
    switch (sTemp) {
        case "Campfire":
            state.colorTemperature = 2000
            break
        case "Moonlight":
            state.colorTemperature = 4100
            break
    }
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
}

def bulbsHandler(evt = NULL) {
    if(!settings.dModes.contains(location.mode) && !settings.sModes.contains(location.mode)) { return }

    for (dSwitch in settings.dSwitches) {
        if(dSwitch.currentSwitch == "on") { return }
    }

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

def bulbHandler(evt, type) {
    if(!settings.dModes.contains(location.mode) && !settings.sModes.contains(location.mode)) { return }

    for (dSwitch in settings.dSwitches) {
        if(dSwitch.currentSwitch == "on") { return }
    }

    //Behavior in sleep mode
    if(location.mode in settings.sModes) {
        calcSleepBrightness()
        calcSleepColorTemperature()
    }

    if(type == "ctBulbs") {
        //Minimize reading state variables
        def brightness = state.brightness
        def colorTemperature = state.colorTemperature

        setCTBulb(evt.device, brightness, colorTemperature)
    }
    if(type == "cBulbs") {
        //Minimize reading state variables
        def brightness = state.brightness
        def colorTemperature = state.colorTemperature
        def hex = rgbToHex(ctToRGB(colorTemperature)).toUpperCase()
        def hsv = rgbToHSV(ctToRGB(colorTemperature))

        setCBulb(evt.device)
    }
    if(type == "dBulbs") {
        //Minimize reading state variables
        def brightness = state.brightness

        setDBulb(evt.device, brightness)
    }
}

def ctBulbHandler(evt) {
    bulbHandler(evt, "ctBulbs")
}

def cBulbHandler(evt) {
    bulbHandler(evt, "cBulbs")
}

def dBulbHandler(evt) {
    bulbHandler(evt, "dBulbs")
}

private void setCTBulb(ctBulb, brightness = state.brightness, colorTemperature = state.colorTemperature) {
    if(ctBulb.currentValue("switch") == "on") {
        if((brightness != NULL && ctBulb.currentValue("cdBrightness") != "false")) {
            if(ctBulb.currentValue("level") != brightness) {
                ctBulb.setLevel(brightness)
            }
        }
        if(ctBulb.currentValue("cdColor") != "false") {
            if(ctBulb.currentValue("colormode") != "ct" || ctBulb.currentValue("colorTemperature") != colorTemperature) {
                ctBulb.setColorTemperature(colorTemperature)
            }
        }
    }
}

private void setCBulb(cBulb, brightness, hex = rgbToHex(ctToRGB(state.colorTemperature)).toUpperCase(), hsv = rgbToHSV(ctToRGB(state.colorTemperature))) {
    def color = [hex: hex, hue: hsv.h, saturation: hsv.s]

    if(cBulb.currentValue("switch") == "on") {
        if(cBulb.currentValue("cdColor") != "false") {
            if((cBulb.currentValue("colormode") != "xy" && cBulb.currentValue("colormode") != "hs") || (cBulb.currentValue("color") != hex && cBulb.currentValue("level") != brightness)) {
                if(brightness != NULL && cBulb.currentValue("cdBrightness") != "false") {
                    color.level = brightness
                }
                cBulb.setColor(color)
            }
        }
    }
}

private void setDBulb(dBulb, brightness = state.brightness) {
    if(dBulb.currentValue("switch") == "on") {
        if(dBulb.currentValue("cdBrightness") != "false") {
            if(dBulb.currentValue("level") != brightness) {
                dBulb.setLevel(brightness)
            }
        }
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

void checkForUpdates() {
	for (branch in settings.gitHubBranch) {
		def branchName
		if (branch == "Stable") { branchName = "Circadian-Daylight" }
		if (branch == "Beta") { branchName = "Circadian-Daylight-Development" }

		def url = "https://api.github.com/repos/claytonjn/SmartThingsPublic/branches/${branchName}"

		def result = null

		try {
			httpGet(uri: url) {response ->
				result = response
			}
			def latestCommitTime = result.data.commit.commit.author.date
			if (latestCommitTime != state."last${branch}Update") {
				def message = "Circadian Daylight ${branch} branch updated with message: ${result.data.commit.commit.message}"
				// check that contact book is enabled and recipients selected
				if (location.contactBookEnabled && recipients) {
				    sendNotificationToContacts(message, recipients, [event: false])
				} else if (updatePush) { // check that the user did select a phone number
				    sendPushMessage(message)
				}
				state."last${branch}Update" = result.data.commit.commit.author.date
			}
		}
		catch (e) {
			log.warn e
		}
	}
}