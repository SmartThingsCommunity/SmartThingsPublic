definition(
name: "Sweet Lights",
namespace: "piratemedia",
author: "EliotS",
description: "Loop Sweet colours on your colored bulbs",
category: "Convenience",
)

preferences {

	section("Choose hue lights you wish to control?") {
            input "hues", "capability.colorControl", title: "Which Color Changing Bulbs?", multiple:true, required: true
        	input "brightnessLevel", "number", title: "Brightness Level (1-100)?", required:false, defaultValue:100
            input "saturationLevel", "number", title: "Saturation Level (1-100)?", required:false, defaultValue:100
	}
    
    section( "Speed" ) {
     input "speed", "number", title: "Speed (1-10)", required:false, defaultValue:5
    }
    
    section( "Enabled/Disabled" ) {
            input "enableswitch", "capability.switch", title: "Optional: Do you have a switch/virtual switch which the application enable/disable functionality should follow? If you do not want this feature leave blank.", multiple:false, required: true
    }
}
	
def installed() {
    unsubscribe()
    
     if ((enableswitch) && (hues))
     {
       subscribe(enableswitch,"switch",EnableSwitchHandler)
     }
     state.hueVal = 0.0
}

def updated() {
    unsubscribe()
    
     if ((enableswitch) && (hues))
     {
       subscribe(enableswitch,"switch",EnableSwitchHandler)
     }
}

def EnableSwitchHandler(evt)
{
    if (evt.value == "on")
     {
       log.debug "Enabling App!"
       StartLoop()
      }
    else
     {
       log.debug "Disabling App!"
       StopLoop()
     }
}

def StartLoop() {
    state.running = true
    state.hueVal = 0.0
    TurnOn()
    ChangeColor()
}

def StopLoop() {
    state.running = false
}

def TurnOn()
{
     hues.on()
}

def ChangeColor() {
    if (!state.running) {
        return
    }
    def newValue = [hue: state.hueVal, saturation: saturationLevel, level: brightnessLevel, speed: 1000]  
    log.debug "Set Hue To: $state.hueVal"
	hues.setColor(newValue)
    def ammount = (3.0 / 10) * speed
    state.hueVal = state.hueVal + ammount
    if(state.hueVal > 100) {
        state.hueVal = 0
    }
    runIn(1, ChangeColor)
}