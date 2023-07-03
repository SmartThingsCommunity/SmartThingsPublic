/** 

 * TCP Bulb

 *

 *

 */



metadata {

	definition (name: "TCP Bulb", namespace: "mujica", author: "Todd Wackford - Ule") {

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

			state "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"

			state "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"

			state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"

			state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"

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



def parse(description) {



	def results = []



	if ( description == "updated" )

		return



	if (description?.name && description?.value)

	{

		results << createEvent(name: "${description?.name}", value: "${description?.value}")

	}

}





def setBulbPower(value) {

	state.bulbPower = value

	log.debug "In child with bulbPower of ${state.bulbPower}"

}



def on() {

	log.debug "Executing 'on'"

	sendEvent(name:"switch",value:on)

	parent.on(this)

	parent.poll(this)



}



def off() {

	log.debug "Executing 'off'"

	sendEvent(name:"switch",value:off)

	parent.off(this)

	sendEvent(name: "power", value: 0.0)

    parent.poll(this)

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



def setLevel(value) {

	log.debug "in setLevel with value: ${value}"

	def level = value as Integer



	sendEvent( name: "level", value: level )

	sendEvent( name: "switch.setLevel", value:level )

	parent.setLevel( this, level )





	if (( level > 0 ) && ( level <= 100 ))

		on()

	else

		off()

	parent.poll(this)

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



def uninstalled() {

	log.debug "Executing 'uninstall' in device type"

	parent.uninstallFromChildDevice(this)

}