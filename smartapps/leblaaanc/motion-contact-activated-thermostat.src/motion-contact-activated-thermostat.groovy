/**
 *  Motion/Contact Activated Thermostat
 *
 *  Author: Chris LeBlanc
 */
definition(
    name: "Motion/Contact Activated Thermostat",
    namespace: "LeBlaaanc",
    author: "Chris LeBlanc",
    description: "Changes your thermostat settings automatically in response to motion or contact sensor activities.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences {

		section("On this thermostat... ") {
			input "thermostat", "capability.thermostat"
		}
 		section("When movement is detected by any of these...") {
			input "motion", "capability.motionSensor", title: "Motion Sensor(s)", multiple: true,  required: false
		}
 		section("Or when any of these are open...") {
			input "contact", "capability.contactSensor", title: "Contact Sensor", multiple: true,  required: false
		}
 		section("Delay the no motion event...") {
			input "delayNoMotionEvent", "number", title: "Minutes", description: "60 minutes",  required: false
		}
		section("Set motion air conditioning setting..."){
			input "coolingSetpointMotion", "number", title: "Degrees?", required: false
		}
		section("Set no motion air conditioning setting..."){
			input "coolingSetpointNoMotion", "number", title: "Degrees?", required: false
		}
		section("Set motion heat setting...") {
			input "heatingSetpointMotion", "number", title: "Degrees?", required: false
		}
		section("Set no motion heat setting...") {
			input "heatingSetpointNoMotion", "number", title: "Degrees?", required: false
		}

}

def installed()
{
	subscribe(motion, "motion", eventHandler)
	subscribe(contact, "contact", eventHandler)
    
    if (coolingSetpointMotion != null) log.debug "coolingSetpointMotion: $coolingSetpointMotion heatingSetpointMotion: $heatingSetpointMotion"
}

def updated()
{
	unsubscribe()

	subscribe(motion, "motion", eventHandler)
	subscribe(contact, "contact", eventHandler)

	if (coolingSetpointMotion != null) log.debug "coolingSetpointMotion: $coolingSetpointMotion heatingSetpointMotion: $heatingSetpointMotion"    
}

def eventHandler(evt)
{
	log.debug "device: $device, value: $evt.value, event: $evt, settings: $settings, handlerName: ${evt.handlerName}"
    
    // set the delay, defaulting to 60 minutes
	def delay = (delayNoMotionEvent != null && delayNoMotionEvent != "") ? delayNoMotionEvent * 60 : 3600
    
    // unschedule delay since motion is detected and set temp to active state
	if (evt.value == 'active' || evt.value == 'open') 
    {
    	log.debug "unscheduling no motion temp timer"
	    unschedule(setTemp)
		log.debug "setting motion temp"
        setTemp('motion')
	} 
    else
    {
    	// schedule delay since no motion on any device is found
		if (motion.find{it.currentState('motion').value == "active"} == null &&	
        	contact.find{it.currentState('contact').value == "open"} == null) 
        {
	    	log.debug "will set no motion temp in $delay secs"        
            runIn(delay, setTemp)		
        }
	}
}

def setTemp(state)
{

    if (state == "motion") 
    {
		if (coolingSetpointMotion != null) thermostat.setCoolingSetpoint(coolingSetpointMotion)
		if (heatingSetpointMotion != null) thermostat.setHeatingSetpoint(heatingSetpointMotion)
    }
    else 
    { 
		if (coolingSetpointNoMotion != null) thermostat.setCoolingSetpoint(coolingSetpointNoMotion)
		if (heatingSetpointNoMotion != null) thermostat.setHeatingSetpoint(heatingSetpointNoMotion)
	}
}