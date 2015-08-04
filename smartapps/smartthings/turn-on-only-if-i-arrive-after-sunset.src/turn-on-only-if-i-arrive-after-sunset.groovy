/**
 *  Turn On Only If I Arrive After Sunset
 *
 *  Author: Danny De Leo
 */
definition(
    name: "Turn On Only If I Arrive After Sunset",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn something on only if you arrive after sunset and back off anytime you leave.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2x.png"
)

preferences {
	section("When I arrive and leave..."){
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Turn on/off a light..."){
		input "switch1", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(presence1, "presence", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presenceHandler)
}

def presenceHandler(evt)
{
	def now = new Date()
	def sunTime = getSunriseAndSunset();
    
	log.debug "nowTime: $now"
	log.debug "riseTime: $sunTime.sunrise"
	log.debug "setTime: $sunTime.sunset"
	log.debug "presenceHandler $evt.name: $evt.value"
    
	def current = presence1.currentValue("presence")
	log.debug current
	def presenceValue = presence1.find{it.currentPresence == "present"}
	log.debug presenceValue
	if(presenceValue && (now > sunTime.sunset)) {
		switch1.on()
		log.debug "Welcome home at night!"
	}
    else if(presenceValue && (now < sunTime.sunset)) {
    	log.debug "Welcome home at daytime!"
    }
	else {
		switch1.off()
		log.debug "Everyone's away."
	}
}

