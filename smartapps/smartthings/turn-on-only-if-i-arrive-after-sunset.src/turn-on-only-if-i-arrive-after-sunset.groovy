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
		input "people", "capability.presenceSensor", title: "Who?", multiple: true
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
	def pplPresent = people.findAll{person -> person.currentPresence == "present"}
    
    	log.debug "nowTime: $now"
	log.debug "riseTime: $sunTime.sunrise"
	log.debug "setTime: $sunTime.sunset"
	log.debug "presenceHandler $evt.name: $evt.value"
	log.debug "pplPresent: $pplPresent"
	
	if(pplPresent.size > 0) {// First presence to arrive
		if(now > sunTime.sunset || now < sunTime.sunrise) {// Arrive at Night
			switch1.on()
			log.debug "Welcome home at night!"
		}
		else if(now > sunTime.sunrise && now < sunTime.sunset) {// Arrive at Day
			log.debug "Welcome home at daytime!"
		}
	}
	if(pplPresent.size == 0) {// Everyone left
	    switch1.off()
	    log.debug "Everyone's away"
	}
}

