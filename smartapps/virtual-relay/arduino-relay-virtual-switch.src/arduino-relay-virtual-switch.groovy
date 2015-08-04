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
		input "switch1", title: "Switch for relay 1", "capability.momentary"
        input "switch2", title: "Switch for relay 2", "capability.switch", required: false
        input "switch3", title: "Switch for relay 3", "capability.switch", required: false
        input "switch4", title: "Switch for relay 4", "capability.switch", required: false 
        input "switch5", title: "Switch for relay 5", "capability.switch", required: false
        input "switch6", title: "Switch for relay 6", "capability.switch", required: false
        input "switch7", title: "Switch for relay 7", "capability.switch", required: false
        input "switch8", title: "Switch for relay 8", "capability.switch", required: false 
	}
    section("Which Arduino relay board to control?") {
		input "arduino", "device.arduinoRelayBoard"
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
	subscribe(switch1, "momentary.pushed", switchOn1)
	//subscribe(switch1, "switch.off", switchOff1)
    subscribe(switch2, "switch", switchOn2)
	//subscribe(switch2, "switch.off", switchOff2)
    subscribe(switch3, "speed", switchOn3)
	//subscribe(switch3, "switch", switchOff3)
    subscribe(switch4, "switch.on", switchOn4)
	subscribe(switch4, "switch.off", switchOff4)
    subscribe(switch5, "switch.on", switchOn5)
	subscribe(switch5, "switch.off", switchOff5)
    subscribe(switch6, "switch.on", switchOn6)
	subscribe(switch6, "switch.off", switchOff6)
    subscribe(switch7, "switch.on", switchOn7)
	subscribe(switch7, "switch.off", switchOff7)
    subscribe(switch8, "switch.on", switchOn8)
	subscribe(switch8, "switch.off", switchOff8)
    //subscribe(contact1, "switch.on", switchOn8)
	//ubscribe(contact1, "switch.off", switchOff8)
    
    
    /* Listen to anything which happens on the device
    subscribe(arduino, "relay1.on", relayTurnOn1)
    subscribe(arduino, "relay1.off", relayTurnOff1)
    subscribe(arduino, "relay2.on", relayTurnOn2)
    subscribe(arduino, "relay2.off", relayTurnOff2)
    subscribe(arduino, "relay3.on", relayTurnOn3)
    subscribe(arduino, "relay3.off", relayTurnOff3)
    subscribe(arduino, "relay4.on", relayTurnOn4)
    subscribe(arduino, "relay4.off", relayTurnOff4)
    subscribe(arduino, "relay5.on", relayTurnOn5)
    subscribe(arduino, "relay5.off", relayTurnOff5)
    subscribe(arduino, "relay6.on", relayTurnOn6)
    subscribe(arduino, "relay6.off", relayTurnOff6)
    subscribe(arduino, "relay7.on", relayTurnOn7)
    subscribe(arduino, "relay7.off", relayTurnOff7)
    subscribe(arduino, "relay8.on", relayTurnOn8)
    subscribe(arduino, "relay8.off", relayTurnOff8)
    */
    
}

def switchOn1(evt)
{
	log.debug "switchOn1($evt.name: $evt.value: $evt.deviceId)"
	log.debug "Sending Push1 event to Arduino"
    arduino.Push1()

}

//def switchOff1(evt)
//{
//    log.debug "switchOff1($evt.name: $evt.value: $evt.deviceId)"
//    if (arduino.currentValue("relay1") != "off")
//    {
//        log.debug "Sending RelayOff1 event to Arduino"
//    	arduino.RelayOff1()
//    }
//}
def switchOn2(evt)
{
	log.debug "switchOn2($evt.name: $evt.value: $evt.deviceId)"
	log.debug "Sending Push2 event to Arduino"
    arduino.Push2()
    
}

def switchOff2(evt)
{
	log.debug "switchOff2($evt.name: $evt.value: $evt.deviceId)"
	log.debug "Sending RelayOff2 event to Arduino"
	arduino.Push2()
    
}

def switchOn3(evt)
{
	log.debug "switchOn3($evt.name: $evt.value: $evt.deviceId)"
    log.debug "Sending push3 event to Arduino"
    arduino.Push3()
    
}

/*
def switchOff3(evt)
{
	log.debug "switchOff3($evt.name: $evt.value: $evt.deviceId)"
        if (arduino.currentValue("relay3") != "off")
    {
        log.debug "Sending RelayOff3 event to Arduino"
    	arduino.RelayOff3()
    }
}
*/
def switchOn4(evt)
{
	log.debug "switchOn4($evt.name: $evt.value: $evt.deviceId)"
    if (arduino.currentValue("relay4") != "on")
    {
    	log.debug "Sending RelayOn4 event to Arduino"
    	arduino.RelayOn4()
    }
}

def switchOff4(evt)
{
	log.debug "switchOff4($evt.name: $evt.value: $evt.deviceId)"
        if (arduino.currentValue("relay4") != "off")
    {
        log.debug "Sending RelayOff4 event to Arduino"
    	arduino.RelayOff4()
    }
}

def switchOn5(evt)
{
	log.debug "switchO5($evt.name: $evt.value: $evt.deviceId)"
    if (arduino.currentValue("relay5") != "on")
    {
    	log.debug "Sending RelayOn5 event to Arduino"
    	arduino.RelayOn5()
    }
}

def switchOff5(evt)
{
	log.debug "switchOff5($evt.name: $evt.value: $evt.deviceId)"
        if (arduino.currentValue("relay5") != "off")
    {
        log.debug "Sending RelayOff5 event to Arduino"
    	arduino.RelayOff5()
    }
}
def switchOn6(evt)
{
	log.debug "switchOn6($evt.name: $evt.value: $evt.deviceId)"
    if (arduino.currentValue("relay6") != "on")
    {
    	log.debug "Sending RelayOn6 event to Arduino"
    	arduino.RelayOn6()
    }
}

def switchOff6(evt)
{
	log.debug "switchOff6($evt.name: $evt.value: $evt.deviceId)"
        if (arduino.currentValue("relay6") != "off")
    {
        log.debug "Sending RelayOff6 event to Arduino"
    	arduino.RelayOff6()
    }
}

def switchOn7(evt)
{
	log.debug "switchOn7($evt.name: $evt.value: $evt.deviceId)"
    if (arduino.currentValue("relay7") != "on")
    {
    	log.debug "Sending RelayOn7 event to Arduino"
    	arduino.RelayOn7()
    }
}

def switchOff7(evt)
{
	log.debug "switchOff7($evt.name: $evt.value: $evt.deviceId)"
        if (arduino.currentValue("relay7") != "off")
    {
        log.debug "Sending RelayOff7 event to Arduino"
    	arduino.RelayOff7()
    }
}
def switchOn8(evt)
{
	log.debug "switchOn8($evt.name: $evt.value: $evt.deviceId)"
    if (arduino.currentValue("relay8") != "on")
    {
    	log.debug "Sending RelayOn8 event to Arduino"
    	arduino.RelayOn8()
    }
}

def switchOff8(evt)
{
	log.debug "switchOff8($evt.name: $evt.value: $evt.deviceId)"
    if (arduino.currentValue("relay8") != "off")
    {
        log.debug "Sending RelayOff8 event to Arduino"
    	arduino.RelayOff8()
    }
}





/*
def relayTurnOn1(evt)
{
	log.debug "Relay 1 was turned on"
    if (switch1.currentValue("switch") != "on")
    {
    	log.debug "Flipping virtual switch 1 on"
    	switch1.on()
    } 	
}


def relayTurnOff1(evt)
{
	log.debug "Relay 1 was turned off"
	if (switch1.currentValue("switch") != "off")
	{
    	log.debug "Flipping virtual switch 1 off"
    	switch1.off()
    }
}

def relayTurnOn2(evt)
{
	log.debug "Relay 2 was turned on"
    if (switch2.currentValue("switch") != "on")
    {
    	log.debug "Flipping virtual switch 2 on"
    	switch2.on()
    } 	
}


def relayTurnOff2(evt)
{
	log.debug "Relay 2 was turned off"
	if (switch2.currentValue("switch") != "off")
	{
    	log.debug "Flipping virtual switch 2 off"
    	switch2.off()
    }
}

def relayTurnOn3(evt)
{
	log.debug "Relay 3 was turned on"
    if (switch3.currentValue("switch") != "on")
    {
    	log.debug "Flipping virtual switch 3 on"
    	switch3.on()
    } 	
}


def relayTurnOff3(evt)
{
	log.debug "Relay 3 was turned off"
	if (switch3.currentValue("switch") != "off")
	{
    	log.debug "Flipping virtual switch 3 off"
    	switch3.off()
    }
}

def relayTurnOn4(evt)
{
	log.debug "Relay 4 was turned on"
    if (switch4.currentValue("switch") != "on")
    {
    	log.debug "Flipping virtual switch 4 on"
    	switch4.on()
    } 	
}


def relayTurnOff4(evt)
{
	log.debug "Relay 4 was turned off"
	if (switch4.currentValue("switch") != "off")
	{
    	log.debug "Flipping virtual switch 4 off"
    	switch4.off()
    }
}

def relayTurnOn5(evt)
{
	log.debug "Relay 5 was turned on"
    if (switch5.currentValue("switch") != "on")
    {
    	log.debug "Flipping virtual switch 5 on"
    	switch5.on()
    } 	
}


def relayTurnOff5(evt)
{
	log.debug "Relay 5 was turned off"
	if (switch5.currentValue("switch") != "off")
	{
    	log.debug "Flipping virtual switch 5 off"
    	switch5.off()
    }
}

def relayTurnOn6(evt)
{
	log.debug "Relay 6 was turned on"
    if (switch6.currentValue("switch") != "on")
    {
    	log.debug "Flipping virtual switch 6 on"
    	switch6.on()
    } 	
}


def relayTurnOff6(evt)
{
	log.debug "Relay 6 was turned off"
	if (switch6.currentValue("switch") != "off")
	{
    	log.debug "Flipping virtual switch 6 off"
    	switch6.off()
    }
}

def relayTurnOn7(evt)
{
	log.debug "Relay 7 was turned on"
    if (switch7.currentValue("switch") != "on")
    {
    	log.debug "Flipping virtual switch 7 on"
    	switch7.on()
    } 	
}


def relayTurnOff7(evt)
{
	log.debug "Relay 7 was turned off"
	if (switch7.currentValue("switch") != "off")
	{
    	log.debug "Flipping virtual switch 7 off"
    	switch7.off()
    }
}


def relayTurnOn8(evt)
{
	log.debug "Relay 8 was turned on"
    if (switch8.currentValue("switch") != "on")
    {
    	log.debug "Flipping virtual switch 8 on"
    	switch8.on()
    } 	
}


def relayTurnOff8(evt)
{
	log.debug "Relay 8 was turned off"
	if (switch8.currentValue("switch") != "off")
	{
    	log.debug "Flipping virtual switch 8 off"
    	switch8.off()
    }
}
*/