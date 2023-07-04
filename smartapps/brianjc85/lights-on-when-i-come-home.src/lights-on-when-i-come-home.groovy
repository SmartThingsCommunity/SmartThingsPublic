/**
 *  Turn on the lights when I come home
 *
 *  Author: Brian Carlson
 */

definition(
    name: "Lights on When I Come Home",
    namespace: "brianjc85",
    author: "Brian Carlson",
    description: "Turn your lights on (e.g. outside lights) when you come home, after dark.  Then, turn them off after specified minutes.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@2x.png"
)

preferences {
	section("Turn on/off light(s)..."){
		input "switches", "capability.switch", multiple: true
	}
	section("When one of these people arrive at home") {
		input "presence", "capability.presenceSensor", multiple: true
	}
	section("And turn off after how many minutes..."){
		input "minutes1", "number", title: "Minutes?"
	}

}

def installed() {
	subscribe(presence, "presence", presenceHandler)
}

def updated() {
	unsubscribe()
	subscribe(presence, "presence", presenceHandler)
}

def presenceHandler(evt) 
{
	//log.debug "$evt.name: $evt.value"
    switches.removeAll { it.currentValue("switch") == "on" } //Exclude lights already on, so we don't then turn them off after X minutes.
    if ( settings.switches.size() == 0 )
    {
    	log.debug "No switches to turn on. Nothing to do."
    }
    else
    {
		switches.each { log.debug "Turning on $it.displayName" }
	    
    	if (evt.value == "present") 
    	{
    		def now = new Date()
			def sunTime = getSunriseAndSunset();
    		/*log.debug "nowTime: $now"
			log.debug "riseTime: $sunTime.sunrise"
			log.debug "setTime: $sunTime.sunset" */
    
    		if(now > sunTime.sunset) 
        	{
				switches.on()
        		runIn(minutes1 * 60, turnOff, [overwrite: false])
			}
		}
	}
}

def turnOff() {
	log.debug "Duration has passed. Turning off lights..."
	switches.each 
    {
    	log.debug "Turning off $it.displayName"
	}
    switches.off()
}