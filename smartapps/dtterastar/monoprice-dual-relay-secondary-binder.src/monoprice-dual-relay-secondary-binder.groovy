definition(
    name: "MonoPrice Dual Relay Secondary Binder",
    namespace: "dtterastar",
    author: "Darrell Turner",
    description: "You can use this to bind the secondary relay to a virtual switch",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
  section("Which Dual Relay"){
    input "dualrelay", "capability.switch", multiple: true, icon: "st.switches.switch.on"
  }
  section("Select a virtual switch to bind to secondary relay"){
    input "switch2", "capability.switch"
  }
}

def installed(settings) {
  	log.debug "Installed with settings: ${settings}"
    subscribe(switch2, "switch", switchHandler)
    subscribe(dualrelay, "switch2", dualrelayHandler)
}

def updated(settings) {
  unsubscribe()
  installed()
}

def switchHandler(evt) {
    switch (evt.value) {
        case 'on':
            dualrelay.on2()
        	break
        case 'off':
            dualrelay.off2()
            break
    }
}

def dualrelayHandler(evt) {
    	switch (evt.value) {
        	case 'on':
            	switch2.on()
                break
            case 'off':
            	switch2.off()
                break
        }
}