import grails.converters.JSON

/**
*  JSON API Access App
*/

definition(
  name: "Quantum",
    namespace: "Quantum",
    author: "Qblinks",
    description: "Quantum is an iPaaS (integration platform as a service) designed for your Smart Home products. With Quantum, you can easily integrate your product with different 3rd party smart home products around the world through a single API call. Smart home features of Qblinks Inc. products are powered by Quantum.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true
)

preferences {
  section("Allow these things to be exposed via JSON...") {
    input "switches", "capability.colorControl", title: "Switches", multiple: true, required: false, hideWhenEmpty: true
    input "outlets", "capability.outlet", title: "Outlets", multiple: true, required: false, hideWhenEmpty: true
  }
}

mappings {
      path("/switchDiscovery") {
        action: [
          GET: "listSwitches"
        ]
      }
      path("/outletDiscovery") {
        action: [
          GET: "listOutlets"
        ]
      }
      path("/switchAction/:id/:command/:value") {
        action: [
          PUT: "actionSwitches"
        ]
      }
      path("/outletAction/:id/:command") {
        action: [
          PUT: "actionOutlets"
        ]
      }
      path("/switchStat/:id") {
        action: [
          GET: "statSwitches"
        ]
      }
      path("/outletStat/:id") {
        action: [
          GET: "statOutlets"
        ]
      }
}

def listSwitches() {
	[ switches: switches.collect{device(it, "switch")} ]
}

def listOutlets() {
	[ outlets: outlets.collect{device(it, "outlet")} ]
}

void actionSwitches() {
	def device = switches.find { it.id == params.id }
	def command = params.command
    
    switch(command) {
		case "on":
        	if (!device) {
				httpError(404, "Device not found")
            } else {
            	device.on()
                //def newValue = [level: body.level  as Integer ?: 100]
                //device.setColor(newValue)
            }
			break
		case "off":
			if (!device) {
				httpError(404, "Device not found")
            } else {
            	device.off()
            }
			break
        case "bri":
        if (!device) {
            httpError(404, "Device not found")
        } else {
            def newValue = [level: params.value as Integer ?: 0]
            device.setColor(newValue)
        }
        break
        case "sat":
        if (!device) {
            httpError(404, "Device not found")
        } else {
            def newValue = [saturation: params.value  as Integer ?: 0]
            device.setColor(newValue)
        }
        break
        case "hue":
        if (!device) {
            httpError(404, "Device not found")
        } else {
            def newValue = [hue: params.value as Integer ?: 0]
            device.setColor(newValue)
        }
        break
		case "allOn":
        	switches.on()
        	break
        case "allOff":
        	switches.off()
        	break
      	default:
			httpError(404, "$command is not a valid command for all outlets specified")
	}
}

def statSwitches() {
	show(switches, "switch")
}

void actionOutlets() {
	def device = outlets.find { it.id == params.id }
	def command = params.command

	switch(command) {
    	case "on":
        	if (!device) {
            	httpError(404, "Device not found")
            } else {
            	device.on()
                result:true
            }
			break
        case "off":
            if (!device) {
            	httpError(404, "Device not found")
            } else {
            	device.off()
                result:true
            }
            break
        case "onoff":
        	if (!device) {
            	httpError(404, "Device not found")
            } else {
            	device.on()
            	device.off()
                result:true
            }
            break
		case "allOn":
        	outlets.on()
        	break
		case "allOff":
        	outlets.off()
        	break
		default:
			httpError(404, "$command is not a valid command for all outlets specified")
	}
}

def statOutlets() {
	show(outlets, "outlet")
}

private show(devices, type) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
    	def device_state = [ label: device.name, type: type, id: device.id, network: device.getDeviceNetworkId() ]
        
        for (attribute in device.supportedAttributes) {
    		device_state."${attribute}" = device.currentValue("${attribute}")
  		}

  		device_state ? device_state : null
	}
}

private device(it, type) {
  def device_state = [ label: it.name, type: type, id: it.id, network: it.getDeviceNetworkId()]


  for (attribute in it.supportedAttributes) {
    device_state."${attribute}" = it.currentValue("${attribute}")
  }

  device_state ? device_state : null
}

def installed() {
}

def updated() {
}
