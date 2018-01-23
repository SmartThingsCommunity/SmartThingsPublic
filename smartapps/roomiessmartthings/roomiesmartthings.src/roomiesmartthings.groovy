/**
 *  RoomieApp-demo
 *
 *  Copyright 2017 MariachiDevs
 *
 */
definition(
    name: "RoomieSmartThings",
    namespace: "roomiessmartthings",
    author: "MariachiDevs",
    description: "App for roomie-bot control",
    category: "Family",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

mappings {
  path("/sockets") {
    action: [
      GET: "listSockets"
    ]
  }
  path("/contacts") {
    action: [
      GET: "listContacts"
    ]
  }
  path("/motion") {
    action: [
      GET: "motion"
    ]
  }
  path("/sockets/:number/:command") {
    action: [
      PUT: "updateSockets"
    ]
  }
}

preferences {
	section ("Permitir control de los siguientes sockets.") {
    	input "sockets", "capability.switch", multiple: true, required: false, title: "¿Cuáles?"
  	}
  	section("Permitir lectura de los siguientes contactos:") {
    	input "contacts", "capability.contactSensor", required: false, multiple: true, title: "¿Cuáles?"
  	}
  	section("Sensor movimiento:") {
    	input "themotion", "capability.motionSensor", required: false, title: "Ubicación"
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
	// TODO: subscribe to attributes, devices, locations, etc.
}

def listSockets() {
    def resp = []
    sockets.each {
      resp << [name: it.displayName, value: it.currentValue("switch")]
    }
    return resp
}
def listContacts() {
    def resp = []
    contacts.each {
      resp << [name: it.displayName, value: it.currentValue("contact")]
    }
    return resp
}
def motion() {
    def motionState = themotion.currentState("motion")
    def elapsed = now() - motionState.date.time
	def resp = [name: themotion.displayName, value: themotion.currentState("motion")]
    log.debug "Movimiento inactivo desde hace ($elapsed ms)"
    return resp
}

void updateSockets() {
    def command = params.command
    def number = params.number
    log.debug "Parametros: ${params}"
    log.debug "Numero: ${number.reverse().substring(1,2)}"
    number = number.reverse().substring(1,2)
    switch(command) {
        case "on":
        	log.debug "sockectsx: ${sockets}"
            sockets[number.toInteger()].on()
            log.debug "Encendidos"
            break
        case "off":
        	log.debug "sockectsx: ${sockets}"
            sockets[number.toInteger()].off()
            log.debug "Apagados"
            break
        default:
            httpError(400, "$command is not a valid command for all sockets specified")
    }
}