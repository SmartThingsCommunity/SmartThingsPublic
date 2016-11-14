/**
 *  Light Color and Brightness
 *
 *  Copyright 2016 Sam Storino
 *
 */
 
definition(
    name: "Light Color and Brightness",
    namespace: "JeorgeLeatherly/ColorControl",
    parent: "JeorgeLeatherly:Lighting Control",
    author: "Sam Storino",
    description: "Control the color and brightness of one or more lights, either by selecting from the predefined color choices, or providing the hue and saturation values yourself.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light20-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light20-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light20-icn@2x.png")


preferences {
	page(name: "actionPage", title: "Light Color and Brightness", install: true, uninstall: true) {
        section("Action:") {
            input "selectedSwitch", "capability.switch", title: "When switch is turned on:", required: false
            input "selectedMode", "mode", title: "When mode changes to:", required: false
        }

        section("Bulb(s):") {
            input "bulbs", "capability.colorControl", title: "Select bulb(s)", required: true, multiple: true
        }

        section("Light effects:") {
            input "selectedColor", "enum", title: "Color?", required: false, multiple: false, options: buildColorOptions()    
            input "selectedLightLevel", "enum", title: "Light Level?", required: false, 
                options: [[10:"10%"],
                          [20:"20%"],
                          [30:"30%"],
                          [40:"40%"],
                          [50:"50%"],
                          [60:"60%"],
                          [70:"70%"],
                          [80:"80%"],
                          [90:"90%"],
                          [100:"100%"]]
        }

        section("Advanced settings (optional)", hideable: true, hidden: true) {    	
            paragraph "If you choose to use these custom settings, the lighting effects selected above will be overriden."
            input "useCustomSettings", "bool", title: "Use custom settings?"
            input "customHue", "decimal", title: "Custom hue:", range: "0..100", required: false, hideWhenEmpty: "useCustomSettings"
            input "customSaturation", "decimal", title: "Custom saturation:", range: "0..100", required: false, hideWhenEmpty: "useCustomSettings"
            input "customLevel", "decimal", title: "Custom brightness:", range: "0..100", required: false, hideWhenEmpty: "useCustomSettings"
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(location, triggerUpdateByMode)
    subscribe(selectedSwitch, "switch.on", triggerUpdateBySwitch)
}

Map buildColorOptions() {
	log.debug "building color options map"
	def optionsMap = [:]
    
	Map colorOptions = getColors()
    for (color in colorOptions) {
        optionsMap.put(color.key, color.value.description)
    }
        
	return optionsMap
}

def triggerUpdateByMode(evt) {
	log.debug "triggerUpdateByMode called: $evt"
    if (location.mode == selectedMode) {
     //   if (location.modes?.find{it.name == selectedMode}) {
            changeBulbSettings();
      //  } else {
       //     log.warn "Undefined mode $selectedMode"
       // }
    }
}

def triggerUpdateBySwitch(evt) {
    log.debug "triggerUpdateBySwitch called"
    changeBulbSettings();
}

private changeBulbSettings() {
	
    
    if(useCustomSettings) {
    	log.debug "using custom settings: [hue: $customHue, saturation: $customSaturation, level: $customLevel]"
    	pushBulbSettings(customHue, customSaturation, customLevel)
        return
    }
    
    log.debug "changeBulbSettings called: [color: $selectedColor, lightLevel: $selectedLightLevel]"
    Map colorOptions = getColors()
    def colorExists = colorOptions.containsKey(selectedColor)
    
    log.debug "selected color exists: $colorExists"
    if(colorExists) {
    	bulbs?.on()
    	def color = colorOptions[selectedColor]
        if(color.setTemperature) {
        	log.debug "setting bulb(s) temperature: [temperature: $color.temperature, saturation: $color.saturation, lightLevel: $selectedLightLevel]"
        	pushBulbSettings(color.temperature, color.saturation)
        } else {
        	log.debug "setting bulb(s) color: [hue: $color.hue, saturation: $color.saturation, lightLevel: $selectedLightLevel]"
        	pushBulbSettings(color.hue, color.saturation, selectedLightLevel)
        }
    }
}

private pushBulbSettings(hue, saturation, level) {
	def bulbSettings = [hue: hue, saturation: saturation, level: level as Integer ?: 100]
    bulbs?.setColor(bulbSettings)
}

private pushBulbSettings(temperature, saturation) {
	bulbs?.setColorTemperature(temperature)
    pushBulbSettings(100, saturation, level)
}

Map getColors() {
	log.debug "getting map of available colors"
	final String Blue = "Blue"
    final String Green = "Green"
    final String Orange = "Orange"
    final String Purple = "Purple"
    final String Pink = "Pink"
    final String Red = "Red"
    final String WhiteCold = "White (Cold)"
    final String WhiteNeutral = "White (Neutral)"
    final String WhiteWarm = "White (Warm)"
    final String Yellow = "Yellow"
    
     return [Blue:[description: Blue, hue: 69.23, saturation: 96.86, setTemperature: false],
             Green:[description: Green, hue: 33.40, saturation: 96.65, setTemperature: false],
             Orange:[description: Orange, hue: 9.04, saturation: 96.86, setTemperature: false],
             Purple:[description: Purple, hue: 75.53, saturation: 92.52, setTemperature: false],
             Pink:[description: Pink, hue: 83.73, saturation: 97.64, setTemperature: false],
             Red:[description: Red, hue: 0, saturation: 98.82, setTemperature: false],
             WhiteCold:[description: WhiteCold, hue: 0, saturation: 0, setTemperature: true, temperature: 9000],
             WhiteNeutral:[description: WhiteNeutral, hue: 0, saturation: 0, setTemperature: true, temperature: 3500],
             WhiteWarm:[description: WhiteWarm, hue: 0, saturation: 0, setTemperature: true, temperature: 2700],
             Yellow:[description: Yellow, hue: 16.28, saturation: 99.61, setTemperature: false]]
}

//
//	TODO section
//

private persistOriginalBulbSettings() {
	//	TODO: implement ability to persist original settings and, optionally, revert to those settings
    //		  when switch or mode state reverts
    
	/*bulbs?.each {
		state.previous[it.id] = [
			"switch": it.currentValue("switch"),
			"level" : it.currentValue("level"),
			"hue": it.currentValue("hue"),
			"saturation": it.currentValue("saturation"),
            "temperature": it?.currentValue("temperature")
		]
	}*/
}