/**
 *  Trigger Routines by Presence with Optional Sun Preconditions
 *
 *  Author: Danny De Leo
 */
definition(
    name: "Trigger Routines by Presence with Optional Sun Preconditions",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Trigger Routines by Presence with Optional Sun Preconditions",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2x.png"
)

preferences {
    page(name: "configure")
}

def configure() {
    dynamicPage(name: "configure", title: "Configure Switch and Phrase", install: true, uninstall: true) {
        def actions = location.helloHome?.getPhrases()*.label
        if (actions) {
            actions.sort()
            section("Routines to Trigger") {
                input "arriveDayAction", "enum", title: "Action to execute when first presence arrives before sunset", options: actions, required: false
                input "arriveNightAction", "enum", title: "Action to execute when first presence arrives after sunset", options: actions, required: false
                input "arriveAlwaysAction", "enum", title: "Action to execute when first presence arrives at any time", options: actions, required: false
                input "departAction", "enum", title: "Action to execute when last presence leaves", options: actions, required: false
            }
        }
        section("Who Lives Here..."){
            input "people", "capability.presenceSensor", title: "Who?", multiple: true
        }
        section("Change to this mode when...") {
            input "homeMode", "mode", title: "Arriving"
            input "awayMode", "mode", title: "Leaving"
        }
	}
}

def installed()
{
	initialize()
}

def initialize() 
{
	subscribe(people, "presence", presenceHandler)
}

def updated()
{
	unsubscribe()
	initialize()
}

def presenceHandler(evt)
{
	def now = new Date()
	def sunTime = getSunriseAndSunset(sunsetOffset: -30);
    def pplPresent = people.findAll{person -> person.currentPresence == "present"}
    def actions = location.helloHome?.getPhrases()*.label
    actions.sort()
    
    log.debug "nowTime: $now"
	log.debug "riseTime: $sunTime.sunrise"
	log.debug "setTime: $sunTime.sunset"
	log.debug "presenceHandler $evt.name: $evt.value"
    log.debug "pplPresent: $pplPresent"
    log.debug "actions: $actions"
    
    if(location.mode != homeMode && pplPresent.size > 0) {// First presence to arrive
    	location.setMode(homeMode)
        if(now > sunTime.sunset || now < sunTime.sunrise) {// Arrive at Night
            if(arriveNightAction != null) {
                sendNotificationEvent("Executing: " + arriveNightAction)
                location.helloHome?.execute(arriveNightAction)
            }
        }
        else if(now > sunTime.sunrise && now < sunTime.sunset) {// Arrive at Day
            if(arriveDayAction != null) {
                sendNotificationEvent("Executing: " + arriveDayAction)
                location.helloHome?.execute(arriveDayAction)
            }
        }
        if(arriveAlwaysAction != null) {// Arrive anytime
            sendNotificationEvent("Executing: " + arriveAlwaysAction)
            location.helloHome?.execute(arriveAlwaysAction)
        }
	}
    if(location.mode != awayMode && pplPresent.size == 0) {// Everyone left
    	location.setMode(awayMode)
        if(departAction) {
            sendNotificationEvent("Executing: " + departAction)
            location.helloHome?.execute(departAction)
        }
	}
}