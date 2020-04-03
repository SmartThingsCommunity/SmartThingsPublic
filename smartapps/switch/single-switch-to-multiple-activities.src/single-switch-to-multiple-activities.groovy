//Use one wall switch to control multiple devices.

definition(
	name: "Single Switch to multiple activities",
	namespace: "Switch",
	author: "Michael Leal",
	description: "Single Switch to multiple activities.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("Switch 1") {
		input "switch1", "capability.switch", required: true
	}
	section("Switch 2") {
		input "switch2", "capability.switch", required: true
	}  
	section("Switch 3") {
		input "switch3", "capability.switch", required: false
	} 
    section("Switch 4") {
		input "switch3", "capability.switch", required: false
	} 
}

def installed()
{   
	subscribe(switch1, "switch.on", fan1, [filterEvents: false])
    subscribe(switch1, "switch.off", light1, [filterEvents: false])
    //subscribe(switch2, "speed")
	//subscribe(switch3, "switch")
    //subscribe(switch2, "switch.off", offHandler2)
}

def updated()
{
	subscribe(switch1, "switch.on", fan1, [filterEvents: false])
    subscribe(switch1, "switch.off", light1, [filterEvents: false])
    subscribe(switch2, "speed", [filterEvents: false])
	subscribe(switch3, "switch", [filterEvents: false])
}

def light1(evt) {
	log.debug "Wall switch pressed off"
    if(switch3.latestValue("switch").contains("on")){
    	switch3.off();
        log.debug "Set light off"
    }
    else{
    	switch3.on();
        log.debug "Set light on"
   }
}

def fan1(evt) {
	log.debug "Wall switch pressed on"
    if(switch2.currentValue("speed").contains("off")){
    	log.debug "Set fan to high"
    	switch2.high()
    }
    else if(switch2.currentValue("speed").contains("low")){
    	log.debug "Set fan to off"
        switch2.off()
    }
    else if(switch2.currentValue("speed").contains("med")){
    	log.debug "Set fan to low"
        switch2.low()
    }
    else{ 
    	log.debug "Set fan to med"
        switch2.med()
    }
}

def onHandler2(evt) {
	if(switch2.latestValue("switch").contains("on")){
    	switch1.on()
    }
}

def offHandler2(evt) {
	if(switch2.latestValue("switch").contains("off")){
    	switch1.off()
    }
}