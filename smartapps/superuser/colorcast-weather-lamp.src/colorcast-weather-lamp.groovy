/**
 *	ColorCast Weather Lamp
 *
 *	Inspired by and based in large part on the original Color Changing Smart Weather lamp by Jim Kohlenberger.
 *	See Jim's original SmartApp at http://community.smartthings.com/t/color-changing-smart-weather-lamp-app/12046 which includes an option for high pollen notifications
 *	
 *	This weather lantern app turns a Phillips hue (or LifX) lamp different colors based on the weather.	 
 *	It uses dark sky's weather API to micro-target weather. 
 *
 *	With special thanks to insights from the SmartThings Hue mood lighting script and the light on motion script by kennyyork@centralite.com
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
import java.util.regex.*

definition(
	name: "ColorCast Weather Lamp",
	namespace: "",
	author: "Joe DiBenedetto",
	description: "Get a simple visual indicator of the days weather before you leave home. ColorCast will change the color of one or more Hue or LIFX lights to match the weather forecast whenever it's triggered.",
	category: "Convenience",
	iconUrl: "http://apps.shiftedpixel.com/weather/images/icons/colorcast2.png",
	iconX2Url: "http://apps.shiftedpixel.com/weather/images/icons/colorcast2@2x.png",
	iconX3Url: "http://apps.shiftedpixel.com/weather/images/icons/colorcast2@2x.png"
)

preferences {
	page(name: "pageMain")
	page(name: "pageAPI")
	page(name: "pageDisplayTriggers")
	page(name: "pageWeatherTriggers")
	page(name: "pageLightSettings")
	page(name: "pageUnderTheHood")
  	page(name: "certainTime")
    page(name: "atCertainTime")
}

def	 pageMain() {
	dynamicPage(
		name: "pageMain", 
		title: "App Settings", 
		install: true, 
		uninstall: true
	) {

		section("App Status") {
			input (
				name:			"enabled", 
				type:			"bool", 
				title: 			"Enabled",
				required:		false,
				defaultValue:	true
			)
		}
		
		section (
			title:		"API Setup",
			hideable:	isValidApiKey() ? true : false,
			hidden:		isValidApiKey() ? true : false
		) {			
			href(
				title: 			"Forecast.io API Key",
				name: 			"hrefApi", 
				page: 			"pageAPI",
				description:	getKey(),
				required: 		!isValidApiKey(),
				state:			isValidApiKey() ? "complete" : ""
			)
		}
		
		section("When to display") {
			href(
				title: 			"Display weather when...",
				name: 			"toPageDisplayTriggers", 
				page: 			"pageDisplayTriggers",
				description:	getDisplayTriggers(),
				required:		!isValidDisplayTriggers(),
				state:			isValidDisplayTriggers() ? "complete" : ""
			)
		}
		
		section("What to display") {
			href(
				title: 			"Display forecast for...",
				name: 			"toPageWeatherTriggers", 
				page: 			"pageWeatherTriggers",
				description:	getWeatherTriggers(),
				required:		!isValidWeatherTriggers(),
				state:			isValidWeatherTriggers() ? "complete" : ""
			)
		}
		
		section("Where to display") {
			href(
				title: 			"Use these lights...",
				name: 			"toPageLightSettings", 
				page: 			"pageLightSettings",
				description:	getLightSettings(),
				required:		!isValidLights(),
				state:			isValidLights() ? "complete" : ""
			)
		}

		section("Additional Settings", mobileOnly:true) {
			label ( //Allow custom name for app. Usefull if the app is installed multiple times for different modes
				title: 		"Assign a name",
				required:	false
			)
			mode ( //Allow app to be assigned to different modes. Usefull if user wants different setting for different modes
				name:		"modeName",
				title: 		"Set for specific mode(s)",
				required:	false
			)
		}
		
		section("Under the hood") {
			href(
				title: 			"Advanced options",
				name: 			"toPageUnderTheHood", 
				page: 			"pageUnderTheHood",
				description:	"",
				required:		false
			)
		}
		

	}
}
def	 pageAPI() {
	dynamicPage(
		name: "pageAPI", 
		title: "API Key", 
		install: false, 
		uninstall: false
	) {
	
		section("First Things First") {
			paragraph "To use this SmartApp you need an API Key from forecast.io (https://developer.forecast.io/). To obtain your key, you will need to register a free account on their site."
			paragraph "You will be asked for payment information, but you can ignore that part. Payment is only required if you access the data more than 1,000 times per day. If you don't give a credit card number and you somehow manage to exceed the 1,000 calls, the app will stop working until the following day when the counter resets."
		}
	
		section("API Key") {
			href (
				name: 			"hrefNotRequired",
				title: 			"Get your free Forecast.io API key",
				required: 		false,
				style: 			"external",
				url: 			"https://developer.forecast.io/",
				image:			"http://forecast.io/images/icons/54.png",
				description: 	"tap to view Forecast.io website in mobile browser"
			)
			input (
				name:			"apiKey", 
				type:			"text", 
				title: 			"Enter your new key",
				required: 		true
			)
		}
	}
}
def	 pageDisplayTriggers() {
	dynamicPage(
		name: "pageDisplayTriggers",
		install: false, 
		uninstall: false
	) {
		
		section("Display weather when...") { //Select motion sensor(s). Optional because app can be triggered manually
   			def atTimeLabel = atTimeLabel()
            href "atCertainTime", title: "At a certain time", description: atTimeLabel ?: "Tap to set", state: atTimeLabel ? "complete" : null
            
			input (
				name:			"motion_detector", 
				type:			"capability.motionSensor", 
				title: 			"Motion is detected",
				multiple:		true,
				required: 		false
			)
			
			input (
				name:			"contact", 
				type:			"capability.contactSensor", 
				title:			"Door is opened", 
				required: 		false, 
				multiple: 		true
			)
			
			input (
				name:			"tapTrigger", 
				type:			"bool", 
				title: 			"Enable App Tap Trigger?",
				required:		false,
				defaultValue:	false
			)
			def timeLabel = timeIntervalLabel()
            href "certainTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
					options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
		}
		section("Always On [BETA]") { //Select motion sensor(s). Optional because app can be triggered manually
			paragraph "When enabled, the light(s) will remain on continuosly while the app is active. If more than one weather condition is met, the light(s) will cycle through all applicable colors and each color will be displayed for 2 minutes.\n\n2 minutes seems to be the shortest stable schedule interval at the moment. Shorter schedules have a high rate of failure.\n\nEnabling this option disables all other triggers. Weather alerts will NOT be displayed when this option is enabled."
			input (
				name:			"alwaysOn", 
				type:			"bool", 
				title: 			"Always On [BETA]",
				required:		false,
				defaultValue:	false
			)
		}

	}
}
def	 pageWeatherTriggers() {
	dynamicPage(
		name: "pageWeatherTriggers", 
		title: "Set Weather Triggers", 
		install: false, 
		uninstall: false
	) {
	
		def colors=["Blue","Purple","Red","Pink","Orange","Yellow","Green","White"]

		section ("Forecast Range") {
			// Get the number of hours to look ahead. Weather for the next x hours will be parsed to compare against user specified values.
			input (
				name:			"forecastRange", 
				type:			"enum", 
				title: 			"Get weather for the next...",
				defaultValue:	"Current conditions",
				options: [
					"Current conditions", 
					"1 Hour", 
					"2 Hours", 
					"3 Hours", 
					"4 Hours", 
					"5 Hours", 
					"6 Hours", 
					"7 Hours", 
					"8 Hours", 
					"9 Hours", 
					"10 Hours", 
					"11 Hours", 
					"12 Hours", 
					"13 Hours", 
					"14 Hours", 
					"15 Hours", 
					"16 Hours", 
					"17 Hours", 
					"18 Hours", 
					"19 Hours", 
					"20 Hours", 
					"21 Hours", 
					"22 Hours", 
					"23 Hours", 
					"24 Hours"
				],
				required:		true
			)
		}
		
		section (
			title:		"All Clear",
			hideable:	true,
			hidden:		((settings.allClearEnabled instanceof Boolean) && !settings.allClearEnabled) ? true : false
		) {			
				input (
				name:			"allClearEnabled", 
				type:			"bool", 
				title: 			"Enabled",
				defaultValue:	true,
				required:		false
			)
		
			input (
				name:			"allClearColor", 
				type:			"enum", 
				title: 			"Color",
				options:		colors,
				defaultValue:	"Green",
				required:		false,
				multiple:		false
			)			 
		}
		
		section (
			title:		"Low Temperature",
			hideable:	true,
			hidden:		((settings.lowTempEnabled instanceof Boolean) && !settings.lowTempEnabled) ? true : false
		) {
			input (
				name:			"lowTempEnabled", 
				type:			"bool", 
				title: 			"Enabled",
				defaultValue:	true,
				required:		false
			)
			input (
				name:			"tempMinTrigger", 
				type:			"number", 
				title: 			"Low Temperature - °F",
				defaultValue:	35,
				range:			"-20..120",
				required:		true
			)
			input (
				name:			"tempMinType", 
				type:			"enum", 
				title: 			"Temperature Type",
				defaultValue:	"Actual",
				options: [
					"Actual",
					"Feels like"
				],
				required:		true
			)
			input (
				name:			"tempMinColor", 
				type:			"enum", 
				title: 			"Color",
				options:		colors,
				defaultValue:	"Blue",
				required:		true,
				multiple:		false
			)
		}
		
		section (
			title:		"High Temperature",
			hideable:	true,
			hidden:		((settings.highTempEnabled instanceof Boolean) && !settings.highTempEnabled) ? true : false
		) {
			input (
				name:			"highTempEnabled", 
				type:			"bool", 
				title: 			"Enabled",
				defaultValue:	true,
				required:		false
			)
			input (
				name:			"tempMaxTrigger", 
				type:			"number", 
				title: 			"High Temperature - °F",
				defaultValue:	80,
				range:			"-20..120",
				required:		true
			)
			input (
				name:			"tempMaxType", 
				type:			"enum", 
				title: 			"Temperature Type",
				defaultValue:	"Actual",
				options: [
					"Actual",
					"Feels like"
				],
				required:		true
			)
			input (
				name:			"tempMaxColor", 
				type:			"enum", 
				title: 			"Color",
				options:		colors,
				defaultValue:	"Red",
				required:		true,
				multiple:		false
			)
		}
		
		section (
			title:		"Rain",
			hideable:	true,
			hidden:		((settings.rainEnabled instanceof Boolean) && !settings.rainEnabled) ? true : false
		) {
			input (
				name:			"rainEnabled", 
				type:			"bool", 
				title: 			"Enabled",
				defaultValue:	true,
				required:		false
			)
			input (
				name:			"rainAmount", 
				type:			"enum", 
				title: 			"Trigger for...",
				defaultValue:	"Any Amount",
				options: [
					"Any Amount",
					"Light Rain", 		//0.017
					"Moderate Rain",	//0.1
					"Heavy Rain"		//0.4
				],
				required:		true
			)
			input (
				name:			"rainColor", 
				type:			"enum", 
				title: 			"Color",
				options:		colors,
				defaultValue:	"Purple",
				required:		true,
				multiple:		false
			)
		}
		
		section (
			title:		"Snow",
			hideable:	true,
			hidden:		((settings.snowEnabled instanceof Boolean) && !settings.snowEnabled) ? true : false
		) {
			input (
				name:			"snowEnabled", 
				type:			"bool", 
				title: 			"Enabled",
				defaultValue:	false,
				required:		false
			)
			if (forecastRange!="Current conditions") {
				input (
					name:			"snowTrigger", 
					type:			"enum", 
					title: 			"Minimum Accumulation (inches)",
					defaultValue:	"Any Amount",
					options: 		["Any Amount", "0.1", "0.2", "0.3", "0.4", "0.5", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"],
					required:		true
				)
			}
			input (
				name:			"snowColor", 
				type:			"enum", 
				title: 			"Color",
				options:		colors,
				defaultValue:	"Pink",
				required:		true,
				multiple:		false
			)
		}
		
		section (
			title:		"Sleet\r\n(applies to freezing rain, ice pellets, wintery mix, or hail)",
			hideable:	true,
			hidden:		((settings.sleetEnabled instanceof Boolean) && !settings.sleetEnabled) ? true : false
		) {
			input (
				name:			"sleetEnabled", 
				type:			"bool", 
				title: 			"Enabled",
				defaultValue:	false,
				required:		false
			)
			input (
				name:			"sleetColor", 
				type:			"enum", 
				title: 			"Color",
				options:		colors,
				defaultValue:	"Pink",
				required:		true,
				multiple:		false
			)
		}
		
		section (
			title:		"Cloudy",
			hideable:	true,
			hidden:		((settings.cloudyEnabled instanceof Boolean) && !settings.cloudyEnabled) ? true : false
		) {
			input (
				name:			"cloudyEnabled", 
				type:			"bool", 
				title: 			"Enabled",
				defaultValue:	false,
				required:		false
			)
			input (
				name:			"cloudPercentTrigger", 
				type:			"number", 
				title: 			"Cloud Cover %",
				defaultValue:	50,
				range:			"1..100",
				required:		true
			)
			input (
				name:			"cloudPercentColor", 
				type:			"enum", 
				title: 			"Color",
				options:		colors,
				defaultValue:	"White",
				required:		true,
				multiple:		false
			)
		}
		
		section (
			title:		"Dew Point\r\n(Sometimes refered to as humidity)",
			hideable:	true,
			hidden:		((settings.dewPointEnabled instanceof Boolean) && !settings.dewPointEnabled) ? true : false
		) {
			input (
				name:			"dewPointEnabled", 
				type:			"bool", 
				title: 			"Enabled",
				defaultValue:	false,
				required:		false
			)
			input (
				name:			"dewPointTrigger", 
				type:			"number", 
				title: 			"Dew Point - °F",
				defaultValue:	65,
				range:			"50..120",
				required:		true
			)
			input (
				name:			"dewPointColor", 
				type:			"enum", 
				title: 			"Color",
				options:		colors,
				defaultValue:	"Orange",
				required:		true,
				multiple:		false
			)
			href (
				name: "hrefNotRequired",
				title: "Learn more about \"Dew Point\"",
				required: false,
				style: "external",
				url: "http://www.washingtonpost.com/blogs/capital-weather-gang/wp/2013/07/08/weather-weenies-prefer-dew-point-over-relative-humidity-and-you-should-too/",
				description: "A Dew Point above 65° is generally considered \"muggy\"\r\nTap here to learn more about dew point"
			)
		}
		
		section (
			title:		"Wind",
			hideable:	true,
			hidden:		((settings.windEnabled instanceof Boolean) && !settings.windEnabled) ? true : false
		) {
			input (
				name:			"windEnabled", 
				type:			"bool", 
				title: 			"Enabled",
				defaultValue:	false,
				required:		false
			)
			input (
				name:			"windTrigger", 
				type:			"number", 
				title: 			"High Wind Speed",
				defaultValue:	24,
				range:			"1..100",
				required:		true
			)
			input (
				name:			"windColor", 
				type:			"enum", 
				title: 			"Color",
				options:		colors,
				defaultValue:	"Yellow",
				required:		true,
				multiple:		false
			)
		}

		section ("Weather Alerts") {	 
			input (
				name:			"alertFlash", 
				type:			"enum", 
				title: 			"Flash Lights For...",
				options:		[
					"warning":"Warnings", 
					"watch":"Watches", 
					"advisory":"Advisories"
				],
				required:		false,
				multiple:		true
			)			
		}	 
	}
}
def	 pageLightSettings() {
	dynamicPage(
		name: "pageLightSettings", 
		title: "Set up lights", 
		install: false, 
		uninstall: false
	) {
		section("Control these bulbs...") {
			input ( //Select bulbs
				name:			"hues", 
				type:			"capability.colorControl", 
				title: 			"Select Hue/LIFX Bulbs?",
				required: 		true,
				multiple:		true
			)
			input ( //Select brightness
				name:			"brightnessLevel", 
				type:			"number", 
				title: 			"Brightness Level (1-100)?",
				required: 		false,
				range:			"1..100",
				defaultValue:	100
			)
			paragraph	"Do you want to set the light(s) back to the color/level they were at before the weather was displayed? Due to the way SmartThings polls devices this may not always work as expected."
			input (
				name:			"rememberLevel", 
				type:			"bool", 
				title: 			"Remember light settings",
				required:		false,
				defaultValue:	true
			)
		}
	
	}
}
def	 pageUnderTheHood() {
	dynamicPage(
		name: "pageUnderTheHood", 
		title: "Advanced options", 
		install: false, 
		uninstall: false
	) {
		section("Color Display Duration") {
			paragraph "Seconds to display each color. If total display time exceeds 20 seconds, display time will be automatically reduced. It's advisable to keep this number low, especially if you'll be using multiple lights. Multiple lights can cause additional delays that could cause the execution limit to be exceeded resulting in errors."
			input (
				name:			"displayDuration", 
				type:			"enum", 
				title: 			"Seconds to display each color.",
				defaultValue:	"2",
				options: 		["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
				required:		true
			)
		}
		section("Debug Mode") {
			paragraph "Enabling debug mode will cause select debug messages to be sent to the notifications section of the app."
			input (
				name:			"debugMode", 
				type:			"bool", 
				title: 			"Debug Mode",
				required:		false,
				defaultValue:	false
			)		
		}
		section("Color Preview") {
			paragraph "Cycle through colors on app button press. This is helpful when altering the color values in the code to get the best possible output for your light. All other triggers will be disabled while this is active so be sure to disable it when you're done. Due to app execution time limits, testing all colors at once may cause the app to timeout before all selected lights have been displayed."
			input (
				name:			"colorCycleEnabled", 
				type:			"bool", 
				title: 			"Color Cycle",
				required:		false,
				defaultValue:	false
			)	
			def colors=["Blue","Purple","Red","Pink","Orange","Yellow","Green","White"]
			input (
				name:			"colorCycleColors", 
				type:			"enum", 
				title: 			"Colors to cycle",
				options:		colors,
				required:		false,
				multiple:		true
			)	
		}
	}
}

def getKey() {
	if (apiKey instanceof String) {
		return apiKey
	} else {
		return "Create/Enter API Key"
	}
}
def getDisplayTriggers() {
	def output = ""
	if (isValidDisplayTriggers()) {
		if (alwaysOn) {
			output += "Weather is displayed continuously."
		} else {
			if (tapTrigger) output += "App button is pressed"
			if(timeX in [null, "A specific time"]) {
            if (atTime instanceof Object) {
				if (output != "") output += "\n"
				output += "Time is:\n" + hhmm(atTime)
			}}
			if (motion_detector instanceof Object) {
				if (output != "") output += "\n"
				output += "Motion is detected:\n"
				motion_detector.eachWithIndex { it, i -> 
					if (i > 0) output += "\n"
					output += "	 " + it.displayName
				}
			}
			if (contact instanceof Object) {
				if (output != "") output += "\n"
				output += "Door is opened:\n"
				contact.eachWithIndex { it, i -> 
					if (i > 0) output += "\n"
					output += "	 " + it.displayName
				}
			}
		}
	} else {
		output = "Choose when to display weather"
	}
	return output
}
def getWeatherTriggers() {
	def output = ""
	if (isValidWeatherTriggers()) {
	
		if (forecastRange != "Current conditions") {
			output = "The next " + forecastRange
		} else {
			output = "The current weather conditions"
		}
		
		output += "\n"
		
		if (allClearEnabled) {
			output += "\n" + allClearColor + "\t- all clear"
		}
		
		if (lowTempEnabled) {
			output += "\n" + tempMinColor + "\t- temperature " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + tempMinTrigger + "° or below"
		}
		
		if (highTempEnabled) {
			output += "\n" + tempMaxColor + "\t- temperature " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + tempMaxTrigger + "° or above"
		}
		
		if (rainEnabled) {
			if (forecastRange == "Current conditions") {
				output += "\n" + rainColor + "\t- it's raining"
			} else {
				output += "\n" + rainColor + "\t- " + ((rainAmount == "Any Amount") ? "any amount of rain" : rainAmount) + " is expected"
			}
		}
		if (snowEnabled) {
			if (forecastRange == "Current conditions") {
				output += "\n" + snowColor + "\t- it's snowing"
			} else if (snowTrigger == "Any Amount") {
				output += "\n" + snowColor + "\t- " + snowTrigger + " of snow is expected"
			} else {
				output += "\n" + snowColor + "\t- " + snowTrigger + "\" or more of snow is expected"
			}
		}
		
		if (sleetEnabled) {
			if (forecastRange == "Current conditions") {
				output += "\n" + sleetColor + "\t- it's sleeting, hailing, etc."
			} else {
				output += "\n" + sleetColor + "\t- sleet, hail, etc is expected"
			}
		}
		
		if (cloudyEnabled) {
			output += "\n" + cloudPercentColor + "\t- cloud cover " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + cloudPercentTrigger + "% or above"
		}
		
		if (dewPointEnabled) {
			output += "\n" + dewPointColor + "\t- dew point " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + dewPointTrigger + "° or above"
		}
		
		if (windEnabled) {
			output += "\n" + windColor + "\t- wind " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + windTrigger + "mph or greater"
		}
		
		if (alwaysOn == false && (alertFlash instanceof Object) && alertFlash.size() > 0) {
			def alertOutput = ""
			def i = 1
			alertFlash.each{ //Iterate through all user specified alert types
				
				switch (it) {
					case "warning":
						alertOutput += "\n	 Warnings"
						break
				   	case "watch":
						alertOutput += "\n	 Watches"
							break
				   	case "advisory":
						alertOutput += "\n	 Advisories"
							break
				}
				
				i++
			}
			
			if (alertOutput != "") output += "\n\nFlash lights for" + alertOutput
			
		}
	} else {
		output = "Choose weather conditions to display"
	}
	
	return output
	
}
def getLightSettings() {
	def output = ""
	
	if (hues instanceof Object) {
		hues.eachWithIndex { it, i -> 
			if (i > 0) output += "\n"
			output += it.displayName
		}
	} else {
		output = "No lights selected"
	}
	return output
}

def isValidApiKey() {
	if (apiKey instanceof String) {
		return true
	} else {
		return false
	}
}
def isValidLights() {
	if (hues instanceof Object) {
		return true
	} else {
		return false
	}
}
def isValidDisplayTriggers() {
	if (motion_detector instanceof Object || contact instanceof Object || tapTrigger || alwaysOn || atTime) {
		return true
	} else {
		return false
	}
}
def isValidWeatherTriggers() {
	if (allClearEnabled || lowTempEnabled || highTempEnabled || rainEnabled || snowEnabled || sleetEnabled || cloudyEnabled || dewPointEnabled || windEnabled) {
		return true
	} else {
		return false
	}
}

def installed() {
	debug "Installed with settings: ${settings}"
	initialize()
}

def initialize() {

	if (enabled) {

		state.current = []
		state.colors = []
		state.isDisplaying = false
		
		schedule("0 0/15 * * * ?", getWeather)

		if (colorCycleEnabled) {
			debug("Color Cycle")
			subscribe(app, colorCycleHandler)
		} else if (!alwaysOn) {

			getWeather()
			if (motion_detector != null) subscribe(motion_detector, "motion", motionHandler)
			if (contact != null) subscribe(contact, "contact", contactHandler)
            if(timeX in [null, "A specific time"]) {
            def myTime = timeToday(atTime, location.timeZone).time
            if (atTime != null) {schedule(myTime, scheduledTimeHandler)}
            //if (atTime != null) {schedule(atTime, scheduledTimeHandler)}
            }
			if (tapTrigger) subscribe(app, appTouchHandler)
		} else {
			state.colorIndex = 0
			getWeather(true)
			state.isDisplaying = false
			subscribe(location, modeChangeHandler)
		}
	} else {
		debug ("App is disabled. Forecast will not be displayed.", true)
		if (alwaysOn) hues.off()
	}
}

def modeChangeHandler(evt) {
	updated()
}

def updated() {
	debug "Updated with settings: ${settings}"
	unsubscribe()
	try{unschedule()} catch(err) {debug("-----Unschedule failed")}
	initialize()
}

def alwaysOnDisplay() {
	
	
	debug ("Running Always On")
	debug ('state.colors.size: ' + state.colors.size())
	debug ('state.colorIndex: ' + state.colorIndex)
	
	if (state.colors.size() > 0) {
		
		sendcolor(state.colors[state.colorIndex])
		state.colorIndex = state.colorIndex + 1
		if (state.colorIndex >= state.colors.size()) state.colorIndex = 0
	}
	
	if (state.colors.size() > 1) {
		debug('canSchedule(): ' + canSchedule())
		schedule("0 0/2 * * * ?", alwaysOnDisplay)
		debug ("Multiple weather conditions exist. Scheduling color cycling.", true)
	} else {
		debug ("Single weather condition. Color cycling disabled until forecast refresh", true)
	}
	
	debug ('state.colorIndex: ' + state.colorIndex)
}

// Weather Processing
def getWeather(firstRun) {
	def forecastUrl="https://api.forecast.io/forecast/$apiKey/$location.latitude,$location.longitude?exclude=daily,flags,minutely" //Create api url. Exclude unneeded data 

	//Exclude additional unneeded data from api url.
	if (forecastRange=='Current conditions') {
		forecastUrl+=',hourly' //If we're checking current conditions we can exclude hourly data
	} else {
		forecastUrl+=',currently' //If we're checking hourly conditions we can exclude current data
	}
	
	if (alertFlash==null) {
		forecastUrl+=',alerts' //If alert event is disabled then we can also exclude alert data
	}
	
	debug (forecastUrl)
	
	httpGet(forecastUrl) {response -> 
		if (response.data) {
			state.weatherData = response.data
			def d = new Date()
			state.forecastTime = d.getTime()
			debug("Successfully retrieved weather.", true)
			if (alwaysOn) {
				 //alwaysOnDisplay
				displayWeather(true)
			}
		} else {
			runIn(60, getWeather)
			debug("Failed to retrieve weather.", true)
		}
	}
	
}

def displayWeather(newCycle) {

	debug('isDisplaying: ' + state.isDisplaying)

	if ((!alwaysOn && !state.isDisplaying) || newCycle) {

		state.isDisplaying = true;
		hues*.refresh()

		def d = new Date()
		if ((d.getTime() - state.forecastTime) / 1000 / 60 > 30) {
			try {unschedule()} catch(err){debug("-Unschedule failed.", true)}
			schedule("0 0/15 * * * ?", getWeather)
			getWeather()
		}
		
		if (!alwaysOn && rememberLevel) {
			state.current.clear()
			hues.each {
				state.current.add([switch: it.currentValue('switch'), hue: it.currentValue('hue'), saturation: it.currentValue('saturation'), level: it.currentValue('level')] )
			}
		}

		//Initialize weather events
		def willRain=false;
		def willSnow=false;
		def willSleet=false;
		def windy=false;
		def tempLow
		def tempHigh
		def cloudy=false;
		def humid=false;
		def weatherAlert=false
		double snowAccumulation = 0.0
		def rainAmount
		
		def rainTrigger
		switch(rainAmount) {
			case "Light Rain":
				rainTrigger	 = 0.017
				break
			case "Moderate Rain":
				rainTrigger = 0.1
				break
			default:
				rainTrigger = 0
		}

		def response = state.weatherData

		if (state.weatherData) { //API response was successful

			state.colors.clear()
			state.colorIndex = 0
			debug ("Unscheduling alwaysOn")
			try{
				unschedule("alwaysOnDisplay")
			} catch(err){
				debug("Cant unschedule always on", true)
				debug(err)
			}

			def i=0
			def lookAheadHours=1
			def forecastData=[]
			if (forecastRange=="Current conditions") { //Get current weather conditions
				forecastData.push(response.currently)
			} else {
				forecastData=response.hourly.data
				lookAheadHours=forecastRange.replaceAll(/\D/,"").toInteger()
			}

			for (hour in forecastData){ //Iterate over hourly data
				if (lookAheadHours<++i) { //Break if we've processed all of the specified look ahead hours. Need to strip non-numeric characters(i.e. "hours") from string so we can cast to an integer
					break
				} else {
					if (snowEnabled || rainEnabled || sleetEnabled) {
						if (hour.precipProbability.floatValue()>=0.15) { //Consider it raining/snowing if precip probabilty is greater than 15%
							if (hour.precipType=='rain' && hour.precipIntensity >= rainTrigger) {
								willRain=true //Precipitation type is rain
							} else if (hour.precipType=='snow') {
								if (forecastRange=="Current conditions") {
									willSnow = true
								} else if (hour.precipAccumulation) {
									snowAccumulation += hour.precipAccumulation
								}
							} else {
								willSleet=true
							}
						}
					}

					if (lowTempEnabled) {
						if (tempMinType=='Actual') {
							if (tempLow==null || tempLow>hour.temperature) tempLow=hour.temperature //Compare the stored low temp to the current iteration temp. If it's lower overwrite the stored low with this temp
						} else {
							if (tempLow==null || tempLow>hour.apparentTemperature) tempLow=hour.apparentTemperature //Compare the stored low temp to the current iteration temp. If it's lower overwrite the stored low with this temp
						}
					}

					if (highTempEnabled) {
						if (tempMaxType=='Actual') {
							if (tempHigh==null || tempHigh<hour.temperature) tempHigh=hour.temperature //Compare the stored low temp to the current iteration temp. If it's lower overwrite the stored low with this temp
						} else {
							if (tempHigh==null || tempHigh<hour.apparentTemperature) tempHigh=hour.apparentTemperature //Compare the stored low temp to the current iteration temp. If it's lower overwrite the stored low with this temp
						}
					}

					if (windEnabled && hour.windSpeed>=windTrigger) windy=true //Compare to user defined value for wind speed.
					if (cloudyEnabled && hour.cloudCover*100>=cloudPercentTrigger) cloudy=true //Compare to user defined value for wind speed.
					if (dewPointEnabled && hour.dewPoint>=dewPointTrigger) humid=true //Compare to user defined value for wind speed.
				}
			}
					
			if (forecastRange!="Current conditions") {
				double snowTriggerDouble = 0.001

				if (snowTrigger != "Any Amount") {
					snowTriggerDouble = Double.parseDouble(snowTrigger)
				}

				if (snowAccumulation >= snowTriggerDouble) willSnow = true
				
				debug("Snow inches forecast: " + snowAccumulation)
			}

			if (response.alerts) { //See if Alert data is included in response
				response.alerts.each { //If it is iterate through all Alerts
					def thisAlert=it.title;
					debug thisAlert
					alertFlash.each{ //Iterate through all user specified alert types
						if (thisAlert.toLowerCase().indexOf(it)>=0) { //If this user specified alert type matches this alert response
							debug ("ALERT: "+it, true)
							weatherAlert=true //Is there currently a weather alert
						}
					}
				}
			}

			//Add color strings to the colors array to be processed later
			if (lowTempEnabled && tempLow<=tempMinTrigger.floatValue()) {
				state.colors.push(tempMinColor)
				debug ("Cold - " + tempMinColor, true)
			}
			if (highTempEnabled && tempHigh>=tempMaxTrigger.floatValue()) {
				state.colors.push(tempMaxColor)
				debug ("Hot - " + tempMaxColor, true)
			}
			if (dewPointEnabled && humid) {
				state.colors.push(dewPointColor)
				debug ("Humid - " + dewPointColor, true)
			}
			if (snowEnabled && willSnow) {
				state.colors.push(snowColor)
				debug ("Snow - " + snowColor, true)	
			}
			if (sleetEnabled && willSleet) {
				state.colors.push(sleetColor)
				debug ("Sleet - " + sleetColor, true)
			}
			if (rainEnabled && willRain) {
				state.colors.push(rainColor)
				debug ("Rain - " + rainColor, true)
			}
			if (windEnabled && windy) {
				state.colors.push(windColor)
				debug ("Windy - " + windColor, true)
			}
			if (cloudyEnabled && cloudy) {
				state.colors.push(cloudPercentColor)
				debug ("Cloudy - " + cloudPercentColor, true)
			}
		}

		//If the colors array is empty, assign the "all clear" color
		if (state.colors.size()==0 && allClearEnabled) state.colors.push(allClearColor)
		state.colors.unique()
		debug state.colors
			 
		int duration = 3 //The amount of time to leave each color on
		if (displayDuration instanceof String) duration = displayDuration.toInteger()
		int maxDisplay = 18

		int flashDelay = 500

		duration *= 1000
		maxDisplay *= 1000
		def displayCount = state.colors.size()

		if (duration * displayCount >= maxDisplay) {
			duration = Math.floor(maxDisplay / displayCount)
		}

		def iterations=1 //The number of times to show each color
		if (weatherAlert) {
			//When there's an active weather alert, shorten the duration that each color is shown but show the color multiple times. This will cause individual colors to flash when there is a weather alert
			iterations = Math.floor(duration / (flashDelay * 2))
			duration = flashDelay
		}

		if (!alwaysOn) {
			state.colors.each { //Iterate over each color
				for (int i = 0; i<iterations; i++) {
					sendcolor(it) //Turn light on with specified color
					pause(duration) //leave the light on for the specified time
					if (weatherAlert) {
						//If theres a weather alert, turn off the light for the same amount of time it was on
						//When a weather alert is active, each color will be looped x times, creating the blinking effect by turning the light on then off x times
						hues.off()
						pause(duration)
					}
				}
			}

			state.isDisplaying = false

			setLightsToOriginal() //The colors have been sent to the lamp and all colors have been shown. Now revert the lights to their original settings

		} else {
			alwaysOnDisplay()
		}

	}
}


// Light Control
def sendcolor(color) {
	//Initialize the hue and saturation
	def hueColor = 0
	def saturation = 100

	//Use the user specified brightness level. If they exceeded the min or max values, overwrite the brightness with the actual min/max
	if (brightnessLevel<1) {
		brightnessLevel=1
	} else if (brightnessLevel>100) {
		brightnessLevel=100
	}

	//Set the hue and saturation for the specified color.
	switch(color) {
		case "White":
			hueColor = 0
			saturation = 0
			break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80 
			break;
		case "Blue":
			hueColor = 72
			break;
		case "Green":
			hueColor = 39
			break;
		case "Yellow":
			hueColor = 25
			saturation = 90
			break;
		case "Orange":
			hueColor = 19
			break;
		case "Purple":
			hueColor = 84
			saturation = 100
			break;
		case "Pink":
			hueColor = 100
			saturation = 55
			break;
		case "Red":
			hueColor = 0
			break;
	}
	
	debug ("Setting color to " + color, true)

	//Change the color of the light
	try {
		hues*.on()
		hues*.setHue(hueColor)
		hues*.setSaturation(saturation)
		hues*.setLevel(brightnessLevel)
	} catch (err) {
		debug(err)
		debug("There was a problem changing bulb color", true)
	}
}

def setLightsToOriginal() {
	if (rememberLevel) {	 
		hues.eachWithIndex { it, i -> 			
			
			it.setHue(state.current[i].hue)
			it.setSaturation(state.current[i].saturation)
			it.setLevel(state.current[i].level)
			if (state.current[i].switch == "off") {
				it.off()
			}

			debug ("RESET: " + state.current[i])
		}
		//hues.refresh()
	} else {
		hues.off()
	}
}

// HANDLE EVENT
def motionHandler(evt) {
	if (evt.value == "active") {// If there is movement then trigger the weather display
		debug ("Motion detected, turning on light", true) 
    	if (getDaysOk() && getTimeOk()) {displayWeather() }
    }
}

def contactHandler(evt) {
	if (evt.value == "open") {
		debug ("Contact sensor open, turning on light", true)
    	if (getDaysOk() && getTimeOk()) {displayWeather() }
	}
}

def appTouchHandler(evt) {// If the button is pressed then trigger the weather display
	displayWeather()	
	debug ("App triggered with button press.", true)
}

def colorCycleHandler(evt) {
	debug("Cycle Colors")
	def firstRun = true;
	colorCycleColors.each {
		if (firstRun) {
			firstRun = false
		} else {
			pause(1000)
		}
		debug(it instanceof String)
		debug(it)
		sendcolor(it)
	}
	hues*.off()
}

// Debug
def debug(msg) {
	debug(msg, false)
}

def debug(msg, toNotifications) {
	//Enable debugging. Comment out line below to disable output.
	//log.debug(msg)
	log.debug(msg)
	
	//Uncomment the next line to send debugging messages to hello, home. I use this when live logging breaks, which is often for me, and when I need a way to view data that's logged when I'm not logged in. 
	if (debugMode && toNotifications) sendNotificationEvent("DEBUG COLORCAST: " + msg)
}

def scheduledTimeHandler() {
    	if (getDaysOk()) {displayWeather() }
}



private dayString(Date date) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}
	else {
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	}
	df.format(date)
}

private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

def certainTime() {
	dynamicPage(name: "certainTime", title: "Only during a certain time", uninstall: false) {
		section() {
			input "startingX", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true, required: false
			if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false
			else {
				if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(startingX == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
		section() {
			input "endingX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true, required: false
			if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false
			else {
				if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(endingX == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
	}
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) df.setTimeZone(location.timeZone)
		else df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		def day = df.format(new Date())
		result = days.contains(day)
	}
	return result
}

private getTimeOk() {
	def result = true
	if((starting && ending) ||
	(starting && endingX in ["Sunrise", "Sunset"]) ||
	(startingX in ["Sunrise", "Sunset"] && ending) ||
	(startingX in ["Sunrise", "Sunset"] && endingX in ["Sunrise", "Sunset"])) {
		def currTime = now()
		def start = null
		def stop = null
		def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
		if(startingX == "Sunrise") start = s.sunrise.time
		else if(startingX == "Sunset") start = s.sunset.time
		else if(starting) start = timeToday(starting, location.timeZone).time
		s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
		if(endingX == "Sunrise") stop = s.sunrise.time
		else if(endingX == "Sunset") stop = s.sunset.time
		else if(ending) stop = timeToday(ending,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	return result
}

def atCertainTime() {
	dynamicPage(name: "atCertainTime", title: "At a certain time", uninstall: false) {
		section() {
			input "timeX", "enum", title: "At time or None ?", options: ["A specific time", "None"], defaultValue: "A specific time", submitOnChange: true
			if(timeX in [null, "A specific time"]) input "atTime", "time", title: "At this time", required: false
		}
	}
}

private atTimeLabel() {
	def result = ''
	if(atTime) result = hhmm(atTime)
	if(timeX in ["None"]) {result="None"}
}

private timeIntervalLabel() {
	def result = ""
	if (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " and Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " and Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " and Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " and Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " and " + hhmm(ending, "h:mm a z")
	else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " and " + hhmm(ending, "h:mm a z")
	else if (starting && endingX == "Sunrise") result = hhmm(starting) + " and Sunrise" + offset(endSunriseOffset)
	else if (starting && endingX == "Sunset") result = hhmm(starting) + " and Sunset" + offset(endSunsetOffset)
	else if (starting && ending) result = hhmm(starting) + " and " + hhmm(ending, "h:mm a z")
}