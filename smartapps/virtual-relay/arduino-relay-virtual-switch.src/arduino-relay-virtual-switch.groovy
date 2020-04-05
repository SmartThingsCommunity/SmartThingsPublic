definition(
	name: "Arduino Relay Virtual Switch",
	namespace: "Virtual Relay",
	author: "Michael Leal",
	description: "Arduino Relay Virtual Switch.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

/**
 *  VirtualSwitchParent
 *
 *  Author: badgermanus@gmail.com
 *  Date: 2014-03-26
 */
preferences {
	section("Connect these virtual switches to the Arduino's relays") {
        //Relay 3 LR Fan
		input "switch1", title: "Relay 1 - LR Fan", "capability.switch", required: false
        //Relay 2 LR Light
        input "switch2", title: "Relay 2 - LR Light", "capability.switch", required: false
        //LR Z-wave Switch
        input "LRSwitch", title: "LR z-wave switch", "capability.switch", required: false
	}
    
    section("Which Arduino relay board to control?") {
		input "arduino", "device.arduinoRelayFanControl"
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
	subscribe(switch1, "speed", Relay1)
	//subscribe(switch1, "switch.off", switchOff1)
    subscribe(switch2, "switch", Relay2)    
}

def Relay1(evt)
{
	LRSwitch.on()
    log.debug "Livingroom fan event received: $evt.value"
	if(evt.value == "off"){arduino.off()}
    else if(evt.value == "high") {arduino.high()}
    else if(evt.value == "med") {arduino.med()}
    else {arduino.low()}
}


def Relay2(evt)
{
	LRSwitch.on()
	log.debug "switchOn2($evt.name: $evt.value: $evt.deviceId)"
	log.debug "Sending LRLight event to Arduino"
    arduino.LRLight()
}