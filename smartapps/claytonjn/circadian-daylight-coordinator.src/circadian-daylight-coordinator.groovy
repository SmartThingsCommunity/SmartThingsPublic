/**
* Circadian Daylight Coordinator 4.0
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
name: "Circadian Daylight Coordinator",
namespace: "claytonjn",
author: "claytonjn",
description: "Sync your color changing lights and dimmers with natural daylight hues to improve your cognitive functions and restfulness.",
category: "Green Living",
iconUrl: "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight.png",
iconX2Url: "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight@2x.png",
iconX3Url: "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight@3x.png",
singleInstance: true
)

preferences {
    page(name: "childInstances", content: "childInstances")
    page(name: "locationPreferences", content: "locationPreferences")
    page(name: "updatePreferences", content:"updatePreferences")
}

def childInstances() {
    if(settings.updateNotifications != NULL) { //use this because boolean input should always have some value
        return dynamicPage(name: "childInstances", nextPage: "locationPreferences", install: false, uninstall: true) {
            section {
                app(appName: "Circadian Daylight", namespace: "claytonjn", title: "New Circadian Daylight Setup", multiple: true)
            }
        }
    } else {
        return locationPreferences()
    }
}

def locationPreferences() {
    return dynamicPage(name: "locationPreferences", nextPage: "updatePreferences", install: false, uninstall: true) {
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
}

def updatePreferences() {
    return dynamicPage(name: "updatePreferences", install: true, uninstall: true) {
        section("Update Notifications") {
			paragraph 	"Get push notifications when an update is pushed to GitHub."
			input(		name: 			"updateNotifications",
						type:			"bool",
						title:			"Update Notifications",
                        defaultValue:   false,
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
    subscribe(app, setHandler)
    subscribe(location, "sunrise", setHandler)
    subscribe(location, "sunset", setHandler)
    subscribe(location, "mode", setHandler)
    schedule("0 */15 * * * ?", setHandler)

    def branches = ["Stable", "Beta"]
    for (branch in branches) {
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
                state."last${branch}Update" = result.data.commit.commit.author.date
            }
        }
        catch (e) {
            log.warn e
        }
    }

    setHandler() //Set state variables from initial install
}

void setHandler(evt) {
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
    def nowDate = new Date()
    def bMin = 1
    def bMax = 100
    if (nowDate > sunriseAndSunset.sunrise && nowDate < sunriseAndSunset.sunset) { state.brightness = bMax } //Before sunset/after sunrise
    else {
        def nowTime = nowDate.getTime()
        def sunsetTime = sunriseAndSunset.sunset.getTime()
        def sunriseTime = sunriseAndSunset.sunrise.getTime()
        if(nowTime < sunriseTime) { //If it's morning, use estimated sunset from the night before
            sunsetTime = sunriseAndSunset.sunset.getTime() - (1000*60*60*24)
        }
        if(nowTime > sunsetTime) { //If it's evening, use estimated sunset for the next day
            sunriseTime = sunriseAndSunset.sunrise.getTime() + (1000*60*60*24)
        }
        def nightLength = sunriseTime - sunsetTime

        //Generate brightness parabola from points
        //Specify double type or calculations fail
        double x1 = sunsetTime
        double y1 = bMax
        double x2 = sunsetTime+(nightLength/2)
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
}

def bulbsHandler() {
    def children = getChildApps()
    children.each { child ->
        child.bulbsHandler()
    }
}

def getBrightness() { return state.brightness }

def getColorTemperature() { return state.colorTemperature }

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