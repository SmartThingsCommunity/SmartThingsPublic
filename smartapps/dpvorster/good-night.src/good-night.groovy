/**
 *  Good Night
 *
 *  Author: dpvorster
 *  Date: 2015-07-07
 */
definition(
    name: "Good Night",
    namespace: "dpvorster",
    author: "dpvorster",
    description: "Runs a routine when no power is detected on a power meter switch after a specific time at night.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/good-night.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/good-night@2x.png"
)

preferences 
{
    page(name: "settings", title: "Settings")
}

def settings() 
{
    dynamicPage(name: "settings", uninstall: true) 
    {		
        section("When there is no power consumed by this device") {
            input "switch1", "capability.powerMeter", title: "Where?"
        }
        section("Between these times") {
            input "startTime", "time", title: "Start time?", required: true
            input "endTime", "time", title: "End time?", required: true
        }
        def phrases = location.helloHome?.getPhrases()*.label
        if (phrases) 
        {
            phrases.sort()
            section("Then run this routine") {
                log.trace phrases
                input "HHRoutine", "enum", title: "Routine", required: true, options: phrases, refreshAfterSelection:true
            }
        }
        section ("") {
            label title: "Assign a name", required: false
            input "modes", "mode", title: "Set for specific mode(s)", multiple: true, required: false
        }
    }
} 

def installed() 
{
	initialize()
}

def updated() 
{
	initialize()
}

def initialize()
{   
	unsubscribe()
	subscribe (switch1, "power", eventHandler)
    log.debug "Subscribed to power event for $switch1"
}

def eventHandler(evt)
{
	if (!correctTime() || !correctMode())
    {
    	log.debug "eventHandler, nothing to do"
    	return
    }
    
	log.debug "eventHandler, wasOn=$state.wasOn"
    
    // Check if switch is on
    if (! state.wasOn) 
    {
    	state.wasOn = (switch1.currentValue('power') > 5)
    }
	
	if (state.wasOn)
    {
		if (isPowerOff())
        {
			takeActions()
            state.wasOn = false;
		}
	}
} 

private takeActions() 
{
    //log.debug "Executing good night"
	//location.helloHome.execute("Good Night!")
    log.debug "Executing \"$settings.HHRoutine\""
    location.helloHome.execute(settings.HHRoutine)
}

private correctTime() 
{
	def t0 = now()
	def start = timeToday(startTime, location.timeZone)
	def end = timeToday(endTime, location.timeZone)
    
    def result = end.time < start.time ? (t0 >= start.time || t0 < end.time) : (t0 >= start.time && t0 <= end.time)
    log.debug "correctTime = $result"
	result
}

private correctMode()
{
	def result = !modes || modes.contains(location.mode)
	log.debug "correctMode = $result"
	result
}

private isPowerOff() 
{
	def power = switch1.currentValue('power')
	log.debug "Power is $power"
	power < 5
}