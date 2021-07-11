/**
 *  Fan Dehumidifier
 *
 */
definition(
    name: "Fan Dehumidifier",
    namespace: "KristopherKubicki",
    author: "kristopher@acm.org",
    description: "Turns on a fan if the humidity exceeds 10% of outside",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan@2x.png")



preferences {
	section("Inside humidifiers..."){
		input "ihumids", "capability.relativeHumidityMeasurement", title: "Where?", required: true, multiple: false
	}
    section("Outside humidifiers..."){
		input "ohumids", "capability.relativeHumidityMeasurement", title: "Where?", required: true, multiple: false
	}
	section("Turn off/on switch(s)..."){
		input "switches", "capability.switch", multiple: true, required: true
	}
}


def installed() {
   initialized()
}

def updated() {
	unsubscribe()
    initialized()
}

def initialized() {
    subscribe(ihumids, "humidity", humidityHandler)
    subscribe(ohumids, "humidity", humidityHandler)
}

def humidityHandler(evt) {
//	log.debug "$evt.name: $evt.value"

	for(dswitch in switches) {
		if(dswitch.currentValue("switch") == "off" && (ihumids.currentValue("humidity") > ohumids.currentValue("humidity") + 10 && ihumids.currentValue("humidity") > 30)) { 
    		dswitch.on()
    	}
    	else if(dswitch.currentValue("switch") == "on")  {
    		dswitch.off()
    	}
    }
}
