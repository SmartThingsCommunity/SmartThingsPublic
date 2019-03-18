/**
 *  TCP Bulb.groovy
 *
 *  Author: todd@wackford.net
 *  Date: 2014-03-07
 *
 *
 *****************************************************************
 *                       Changes
 *****************************************************************
 *
 *  Change 1:	2014-03-10
 *				Documented Header
 *
 *  Change 2:	2014-03-15
 *				Fixed bug where we weren't coming on when changing
 *				levels down.
 *
 *  Change 3:   2014-04-02 (lieberman)
 *              Changed sendEvent() to createEvent() in parse()
 *
 *  Change 4:	2014-04-12 (wackford)
 *				Added current power usage tile
 *
 *  Change 5:	2014-09-14 (wackford)
 *				a. Changed createEvent() to sendEvent() in parse() to
 *				   fix tile not updating.
 *				b. Call IP checker for DHCP environments from refresh. Parent
 *				   service manager has method to call every 5 minutes too.
 *
 *  Change 6:	2014-10-17 (wackford)
 *				a. added step size input to settings of device
 *				b. added refresh on udate
 *				c. added uninstallFromChildDevice to handle removing from settings
 *				d. Changed to allow bulb to 100%, was possible to get past logic at 99
 *
 *  Change 7:	2014-11-09 (wackford)
 *				a. Added bulbpower calcs to device. TCP is broken
 *				b. Changed to set dim level first then on. Much easier on the eys coming from bright.
 *
 *****************************************************************
 *                       Code
 *****************************************************************
 */
// for the UI
metadata {
	definition (name: "TCP Bulb", namespace: "wackford", author: "Todd Wackford") {
		capability "Switch"
		capability "Polling"
		capability "Power Meter"
		capability "Refresh"
		capability "Switch Level"

		attribute "stepsize", "string"

		command "levelUp"
		command "levelDown"
		command "on"
		command "off"
		command "setBulbPower"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	preferences {
		input "stepsize", "number", title: "Step Size", description: "Dimmer Step Size", defaultValue: 5
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}
		standardTile("lUp", "device.switchLevel", inactiveLabel: false,decoration: "flat", canChangeIcon: false) {
			state "default", action:"levelUp", icon:"st.illuminance.illuminance.bright"
		}
		standardTile("lDown", "device.switchLevel", inactiveLabel: false,decoration: "flat", canChangeIcon: false) {
			state "default", action:"levelDown", icon:"st.illuminance.illuminance.light"
		}
		valueTile( "power", "device.power", inactiveLabel: false, decoration: "flat") {
			state "power", label: '${currentValue} Watts'
		}

		main(["switch"])
		details(["switch", "lUp", "lDown", "levelSliderControl", "level" , "power", "refresh" ])
	}
}

// parse events into attributes
def parse(description) {
	//log.debug "parse() - $description"
	def results = []

	if ( description == "updated" )
		return

	if (description?.name && description?.value)
	{
		results << createEvent(name: "${description?.name}", value: "${description?.value}")
	}
}

// handle commands
def setBulbPower(value) {
	state.bulbPower = value
	log.debug "In child with bulbPower of ${state.bulbPower}"
}

def on() {
	log.debug "Executing 'on'"
	sendEvent(name:"switch",value:on)
	parent.on(this)

	def levelSetting = device.latestValue("level") as Float ?: 1.0
	def bulbPowerMax = device.latestValue("setBulbPower") as Float
	def calculatedPower = bulbPowerMax * (levelSetting / 100)
	sendEvent(name: "power", value: calculatedPower.round(1))

	if (device.latestValue("level") == null) {
		sendEvent( name: "level", value: 1.0 )
	}
}

def off() {
	log.debug "Executing 'off'"
	sendEvent(name:"switch",value:off)
	parent.off(this)
	sendEvent(name: "power", value: 0.0)
}

def levelUp() {
	def level = device.latestValue("level") as Integer ?: 0
	def step = state.stepsize as float

	level+= step

	if ( level > 100 )
		level = 100

	setLevel(level)
}

def levelDown() {
	def level = device.latestValue("level") as Integer ?: 0
	def step = state.stepsize as float

	level-= step

	if ( level <  1 )
		level = 1

	setLevel(level)
}

def setLevel(value, rate = null) {
	log.debug "in setLevel with value: ${value}"
	def level = value as Integer

	sendEvent( name: "level", value: level )
	sendEvent( name: "switch.setLevel", value:level )
	parent.setLevel( this, level )


	if (( level > 0 ) && ( level <= 100 ))
		on()
	else
		off()

	def levelSetting = level as float
	def bulbPowerMax = device.latestValue("setBulbPower") as float
	def calculatedPower = bulbPowerMax * (levelSetting / 100)
	sendEvent(name: "power", value: calculatedPower.round(1))
}

def poll() {
	log.debug "Executing poll()"
	parent.poll(this)
}

def refresh() {
	log.debug "Executing refresh()"
	parent.poll(this)
}

def installed() {
	initialize()
}

def updated() {
	initialize()
	refresh()
}

def initialize() {
	if ( !settings.stepsize )
		state.stepsize = 10 //set the default stepsize
	else
		state.stepsize = settings.stepsize
}

/*******************************************************************************
 Method :uninstalled(args)
 (args) :none
 returns:Nothing
 ERRORS :No error handling is done

 Purpose:This is standard ST method.
 Gets called when "remove" is selected in child device "preferences"
 tile. It also get's called when "deleteChildDevice(child)" is
 called from parent service manager app.
 *******************************************************************************/
def uninstalled() {
	log.debug "Executing 'uninstall' in device type"
	parent.uninstallFromChildDevice(this)
}
