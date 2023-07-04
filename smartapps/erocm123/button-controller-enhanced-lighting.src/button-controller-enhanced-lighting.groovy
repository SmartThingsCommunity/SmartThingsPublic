/**
 *  
 *	Button Controller - Enhanced Lighting
 *  
 *	Author: Eric Maycock (erocm123)
 *	email: erocmail@gmail.com
 *	Date: 2015-10-22
 *  2016-03-28: Changed how toggle works. Now it checks to see if all lights are on/off and does the opposite.
 *  2016-03-08: Better configuration layout. Configurable number of buttons. Ability to rename app.
 *  2016-02-04: Found an execution bug.
 *  2016-02-04: Slight change to still allow choosing on & off for lights  
 *  2016-02-04: Added the ability to toggle lights.
 *  2015-11-30: Changed the app to handle double button presses. You can change the debuounce time
 *              in the config.
 * 	2015-10-26: Added a "Random" option when choosing a color.
 *  
 *	This SmartApp allows you to, in great detail, control your lighting with button based 
 *	devices like the Aeon Labs Minimote or Enerwave ZWN-SC7. Want a button to turn one
 *  	hue bulb red, one green, one blue, and one orange? This can do it. Want a single button to 
 *	turn on your overhead lights and dim your lamps to 50%. This can do it. The combinations 
 *	are limitless.
 *   
 *	Note: The app treats the Aeon Minimote buttons as 1,2,3,4 for a single press and
 *	5,6,7,8 when the buttons are pressed and held. This should be pretty self explanatory but
 *	holding 1 is equivalent to button 5, holding 2 is equivalent to button 6, and so on.
 */
 


definition(
    name: "Button Controller - Enhanced Lighting",
    namespace: "erocm123",
    author: "Eric Maycock (erocm123)",
    description: "Control lights with buttons like the Aeon Labs Minimote or Enerwave ZWN-SC7.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps@2x.png"
)

preferences {
	page(name: "selectButton")
	page(name: "configureButton")
	page(name: "configureLight")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def selectButton() {
	dynamicPage(name: "selectButton", title: "First, select your button device", nextPage: null, uninstall: true, install: true) {
		section {
			input "buttonDevice", "capability.button", title: "Button", multiple: false, required: true, submitOnChange: true
		}
        section("Menu") {
                input "numberOfButtons", "enum", title: "Number of Buttons?", required: true, value: 8, defaultValue: 8, submitOnChange: true, options: [
                1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24]
                def configDescription = ""
                for (int i = 1; i <= (numberOfButtons as Integer); i++){
                   configDescription = ""
                   if (settings["lights_${i}"] != null) { 
                      settings["lights_${i}"].each {
                         configDescription += "${it.displayName}, "
                      }
                      configDescription = configDescription.substring(0, configDescription.length() - 2)
                   } else {
                      configDescription = "Click to configure"
                   }
                   href "configureButton", title:"Configure Button $i", description:"$configDescription", params: [button: i]
                }
        }
        
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
            input "debounce", "number", title: "Debounce time in milliseconds (set to 0 to disable)", required: true, value: 3000, defaultValue: 3000
            def timeLabel = timeIntervalLabel()
            href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
            input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            input "modes", "mode", title: "Only when mode is", multiple: true, required: false
		}
        section([title:"Available Options", mobileOnly:true]) {
			label title:"Assign a name for your app (optional)", required:false
		}
	}
}

def configureButton(params) {
    if (params?.button || params?.params?.button) {
      if (params.button) {
         state.currentButton = params.button.toInteger()
      } else {
         state.currentButton = params.params.button.toInteger()
      }
    } 
    dynamicPage(name: "configureButton", title: "Choose the lights that you would like button ${state.currentButton} to control.",
	uninstall: false, getButtonSections(state.currentButton))
}

def configureLight(params) {
    if (params?.lightId || params?.params?.lightId) {
      if (params.lightId) {
         state.lightId = params.lightId
      } else {
         state.lightId = params.params.lightId
      }
    }
    def thisLight = settings["lights_${state.currentButton}"].find{it.id == state.lightId} 

    dynamicPage(name: "configureLight", uninstall: false, install: false) {
       
        section ("${thisLight}") {
            def switchType = "Switch"
            if (thisLight?.hasCapability("Switch Level")) {
            	switchType = "Dimmer"
            }
            if (thisLight?.hasCapability("Color Control")) {
            	switchType = "Color"
            }
            switch(switchType) {
				case ~/.*Switch.*/:
                    input "lights_${state.currentButton}_${state.lightId}_power", "bool", title: "Turn the light on or off", submitOnChange: false
					break
				case ~/.*Dimmer.*/:
                    input "lights_${state.currentButton}_${state.lightId}_power", "bool", title: "Turn the light on or off", submitOnChange: false
                    input "lights_${state.currentButton}_${state.lightId}_lightLevel", "number", title: "Light Level?", required: false, range: "1..100"
					break
				case ~/.*Color.*/:
                    input "lights_${state.currentButton}_${state.lightId}_power", "bool", title: "Turn the light on or off", submitOnChange: false
                    input "lights_${state.currentButton}_${state.lightId}_color", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink","Random"]
					input "lights_${state.currentButton}_${state.lightId}_lightLevel", "number", title: "Light Level?", required: false, range: "1..100"
					break
            }
        }
    }
}

def getButtonSections(button) {
	return {
		section("Lights") {
			input "lights_${state.currentButton}", "capability.switch", multiple: true, required: false, submitOnChange: true
		}
        section ("Toggle"){
            input "lights_${state.currentButton}_toggle", "enum", title: "Toggle Type", submitOnChange: false, defaultValue: 0, options: [[0:"None"],[1:"Single Light On, Turn All On"],[2:"Single Light Off, Turn All Off"],[3:"Same Button Pressed 2 or More Times in a Row"]]
        }
        def lightsConfigured = settings["lights_${state.currentButton}"]
        if (lightsConfigured != null) {
        def map = [:]
        	lightsConfigured.each {light ->
                def inDescription = "Click to Configure"
                
                if (settings["lights_${state.currentButton}_${light.id}_power"])
                	inDescription = "Power: on"
                else
                    inDescription = "Power: off"

                if (settings["lights_${state.currentButton}_${light.id}_lightLevel"] != null)
                	inDescription = "$inDescription, Level: ${settings["lights_${state.currentButton}_${light.id}_lightLevel"]}"
                    
                if (settings["lights_${state.currentButton}_${light.id}_color"] != null)
                	inDescription = "$inDescription, Color: ${settings["lights_${state.currentButton}_${light.id}_color"]}"
                
            	section ("$light"){
           			href(name: "Configure $light",
                 	page: "configureLight",
                 	params: [ lightId: "$light.id" ],
                 	description: "$inDescription")
           		}
                
                def value = "${light.displayName}"
		        def key = "${light.id}"
		        map["${key}"] = value
                
        }
        section("Master Switch") {
           input "lights_${state.currentButton}_master_first", "enum", title: "Turn this switch on first", description: "", multiple: false, required: false, submitOnChange: false, options: map
           input "lights_${state.currentButton}_master_last", "enum", title: "Turn this switch off last", description: "", multiple: false, required: false, submitOnChange: false, options: map
        }
        }
        
		
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(buttonDevice, "button", buttonEvent)
}

def buttonConfigured(idx) {
	return settings["lights_$idx"]
}

def buttonEvent(evt){
	if(allOk) {
        def buttonNumber = evt.jsonData.buttonNumber
        def firstEventId = 0
		def value = evt.value
		//log.debug "buttonEvent: $evt.name = $evt.value ($evt.data)"
		log.debug "button: $buttonNumber, value: $value"
        if (value == "held")
        	buttonNumber = buttonNumber + numberOfButtons.toInteger()/2
        if (value == "double")
        	buttonNumber = buttonNumber + numberOfButtons.toInteger() 
        
        if(debounce != null && debounce != "" && debounce > 0) {
            def recentEvents = buttonDevice.eventsSince(new Date(now() - debounce)).findAll{it.value == evt.value && it.data == evt.data}
            log.debug "Found ${recentEvents.size()?:0} events in past ${debounce/1000} seconds"
            if (recentEvents.size() != 0){
                log.debug "First Event ID: ${recentEvents[0].id}"
                firstEventId = recentEvents[0].id
            }
            else {
                firstEventId = 0
            }
        } else {
            firstEventId = evt.id
        }
        
        log.debug "This Event ID: ${evt.id}"

		if(firstEventId == evt.id){
			executeHandlers(buttonNumber)
		} else if (firstEventId == 0) {
      		log.debug "No events found. Possible SmartThings latency"
    	} else {
      		log.debug "Duplicate button press found. Not executing handlers"
    	}
	}
}

def executeHandlers(buttonNumber) {
    log.debug "executeHandlers: $buttonNumber"
    def lightsConfigured = settings["lights_${buttonNumber}"]
    def toggle = false

    if ((settings["lights_${buttonNumber}_toggle"] == "3") && !(state.previousState) && state.previousScene == buttonNumber) {
       log.debug "toggle 3"
       toggle = true
    } else if (settings["lights_${buttonNumber}_toggle"] == "1") {
       log.debug "toggle 1"
       if (lightsConfigured != null) {
          //If another source turned the lights on or off, we need to do the opposite
           def numberOfOnLights = 0
           lightsConfigured.each { light ->
              if (light.currentValue("switch") == "on") {
                  numberOfOnLights++
              }
           }
          if (numberOfOnLights == 0) toggle = false
          else toggle = true
       }
    } else if (settings["lights_${buttonNumber}_toggle"] == "2") {
       log.debug "toggle 2"
       if (lightsConfigured != null) {
          //If another source turned the lights on or off, we need to do the opposite
           def numberOfOnLights = 0
           lightsConfigured.each {light ->
              if (light.currentValue("switch") == "on") {
                  numberOfOnLights++
              }
           }
           if (numberOfOnLights != lightsConfigured.size()) toggle = false
           else toggle = true
       }
    }
    if (lightsConfigured != null) {
        def master_light_first
        def master_light_last
        if(settings["lights_${buttonNumber}_master_first"] != null){
            master_light_first = lightsConfigured.find{it.id == settings["lights_${buttonNumber}_master_first"]}
        }
        if(settings["lights_${buttonNumber}_master_last"] != null){
            master_light_last = lightsConfigured.find{it.id == settings["lights_${buttonNumber}_master_last"]}
        }
        
        if(master_light_first) setLight(master_light_first, "$buttonNumber", toggle, "first")

        lightsConfigured.each {light ->
            if(light.id != settings["lights_${buttonNumber}_master_first"] && light.id != settings["lights_${buttonNumber}_master_last"])
                setLight(light, "$buttonNumber", toggle)
        }
        
        if(master_light_last) setLight(master_light_last, "$buttonNumber", toggle, "last")

        state.previousScene = buttonNumber
        state.previousState = toggle

        
    }
}

def setLight(light, buttonNumber, toggle, sequence = null) {
    def power
    def level
    def color
    
    def switchType = "Switch"

    if (settings["lights_${buttonNumber}_${light.id}_power"] != null) {
    	power = settings["lights_${buttonNumber}_${light.id}_power"]
    } else {
        power = false
    }
    if (settings["lights_${buttonNumber}_${light.id}_lightLevel"] != null) {
    	level = settings["lights_${buttonNumber}_${light.id}_lightLevel"]
        switchType = "Dimmer"
    }
    if (settings["lights_${buttonNumber}_${light.id}_color"] != null) {
    	color = settings["lights_${buttonNumber}_${light.id}_color"]
        switchType = "Color"
    }
    
    if (toggle) power = false
    
    if (power == true && sequence != "last") {
    	switch(switchType) {
        	case ~/.*Switch.*/:
                light.on()
        		break
        	case ~/.*Dimmer.*/:
                if (level)
                	light.setLevel(level as Integer)
                else
                	light.on()
        		break
        	case ~/.*Color.*/:
                def hueColor = 0
                def saturation = 100
                def colorTemperature

                switch(color) {
                    case "White":
                    hueColor = 63
                    saturation = 28
                    colorTemperature = 8000
                    break;
                    case "Daylight":
                    hueColor = 63
                    saturation = 43
                    colorTemperature = 6000
                    break;
                    case "Soft White":
                    hueColor = 5
                    saturation = 4
                    colorTemperature = 3200
                    break;
                    case "Warm White":
                    hueColor = 79
                    saturation = 7
                    colorTemperature = 2500
                    break;
                    case "Blue":
                    hueColor = 70
                    break;
                    case "Green":
                    hueColor = 39
                    break;
                    case "Yellow":
                    hueColor = 25
                    break;
                    case "Orange":
                    hueColor = 10
                    break;
                    case "Purple":
                    hueColor = 75
                    break;
                    case "Pink":
                    hueColor = 83
                    break;
                    case "Red":
                    hueColor = 100
                    break;
                    case "Random":
		    		Random rand = new Random()
		    		int max = 100
		    		hueColor = rand.nextInt(max+1)
                    break;
                }
                def rgbValue = huesatToRGB(hueColor, saturation)
                def hexValue = rgbToHex(r: rgbValue[0], g: rgbValue[1], b: rgbValue[2])
                def colorValue
                if (colorTemperature != null && light.typeName.toUpperCase().indexOf("LIFX") >= 0) {
                    colorValue = [hue: hueColor as Integer, saturation: saturation, level: level as Integer ?: 100, colorTemperature: colorTemperature]
                    delayBetween(light.setColorTemperature(colorTemperature),
                    light.setLevel(level as Integer ?: 100), 1000)
                } else {
                    colorValue = [hue: hueColor as Integer, saturation: saturation, level: level as Integer ?: 100]
                    light.setColor(colorValue)
                }
                
        		break
		}
    }
    else if(power == false && sequence != "first") {
    	light.off()
    }
}

// execution filter methods
private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}

private timeIntervalLabel() {
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}

def huesatToRGB(float hue, float sat) {
	while(hue >= 100) hue -= 100
	int h = (int)(hue / 100 * 6)
	float f = hue / 100 * 6 - h
	int p = Math.round(255 * (1 - (sat / 100)))
	int q = Math.round(255 * (1 - (sat / 100) * f))
	int t = Math.round(255 * (1 - (sat / 100) * (1 - f)))
	switch (h) {
		case 0: return [255, t, p]
		case 1: return [q, 255, p]
		case 2: return [p, 255, t]
		case 3: return [p, q, 255]
		case 4: return [t, p, 255]
		case 5: return [255, p, q]
	}
}
def rgbToHex(rgb) {
    def r = hex(rgb.r)
    def g = hex(rgb.g)
    def b = hex(rgb.b)
    def hexColor = "#${r}${g}${b}"
    
    hexColor
}
private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}