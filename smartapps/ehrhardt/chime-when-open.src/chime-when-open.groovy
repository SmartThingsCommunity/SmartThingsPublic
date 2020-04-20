/**
 *  Chime When Open
 *
 */
definition(
    name: "Chime When Open",
    namespace: "ehrhardt",
    author: "John Ehrhardt",
    description: "Play a tone when some sensors being open",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
    )


preferences {
	section("When any of these are open...") {
		input "doorSensor", "capability.contactSensor", title: "Door Sensor", required: true, multiple: true
	}
	section("Play a tone on this device:") {
		input "toneDevice", "capability.audioNotification", title: "Audio Playback Device", required: true
		input "toneTrack", "number", title: "Tone Track (1-100)"
	}
    section("Play Tones During this timeframe")
	{
		input "starting", "time", title: "Start time", required: true
		input "ending", "time", title: "End time", required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(doorSensor, "contact", contactHandler)
}

def contactHandler(evt) {
	log.trace "contactHandler: $evt ${evt.value}"

    def openSensor = doorSensor.find{it.currentValue("contact") == "open"}
    if (openSensor != null) {
    	def result = true
   		if (starting && ending) {
   			def currTime = now()
   			def start = timeToday(starting).time
   			def stop = timeToday(ending).time
   			result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
   		}
 		log.trace "timeOk = $result"
    	if ( result ) { 
			log.debug "At least one sensor (${openSensor.label}) is open, play track $toneTrack"
    	    toneDevice.playTrack(toneTrack)
			log.debug( "OnHandler: timeOK and dimSetting: $dimSetting" )
	    } else {
    		log.debug( "OnHandler: !timeOK" )
		}
    } else {
    	log.debug "No sensors are open."
    }
}