definition(
	name: "Particle Sync Virtual Switches",
	namespace: "Virtual Relay",
	author: "Michael Leal",
	description: "SA to sync VS to PS.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

/**
 *  VirtualSwitchParent
 *
 *  Author: michael.lea82@gmail.com
 *  Date: 2014-03-26
 */
preferences {
	section("Connect these virtual switches to the Particle board") {
		input "Vdevice1", title: "Virtual for garage remote", "capability.momentary"
        input "Vcontact1", title: "Virtual contact switch for garage door", "capability.contactSensor", required: false
        input "temphumid_1", title: "1st Temp-Humidity Sensor", "capability.temperatureMeasurement", required: false
	}
    section("Select the particle board device") {
		input "particle", "device.ParticleGarage"
 
	}    
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe()
}


def subscribe() {

	// Listen to the virtual switches
	subscribe(Vdevice1, "momentary.pushed", device1)
 	// Listen to device conntect to the particle board.
    subscribe(particle, "contact1", contact1)
    
    
    }

def device1(evt)
{
	log.debug "switchOn1($evt.name: $evt.value: $evt.deviceId)"
	log.debug "Sending Push event to particle switch"
    Pdevice1.push()

}


def contact1(evt)
{
	log.debug "contact1($evt.name: $evt.value: $evt.deviceId)"
	if (evt.value  == "open"){
    	log.debug "Setting Virtual contact to open"
    	Vcontact1.open()
    }
    else {
    	log.debug "Setting Virtual contact to closed"
    	Vcontact1.close()
    }
}