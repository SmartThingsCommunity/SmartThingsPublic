/**
 *  TRIGGERcmdSwitch.groovy
 *
 *  Author: vanmeyconsulting@gmail.com
 *  Date: 2017-04-22
 *
 *
 */
// for the UI
metadata {
	definition (name: "TRIGGERcmd Switch", namespace: "vandermeyconsulting", author: "Russell VanderMey") {
		capability "Switch"

		command "on"
		command "off"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	preferences {
		// input "stepsize", "number", title: "Step Size", description: "Dimmer Step Size", defaultValue: 5
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
		main(["switch"])
		details(["switch", "refresh" ])
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
def on() {
	log.debug "Executing 'on'"
	sendEvent(name:"switch",value:on)
	parent.on(this)

    // off()
}

def off() {
	log.debug "Executing 'off'"
	sendEvent(name:"switch",value:off)
	parent.off(this)
	// sendEvent(name: "power", value: 0.0)
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
    // off()
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