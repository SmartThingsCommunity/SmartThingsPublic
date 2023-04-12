/**
 *  Siren Beep
 *
 */
definition(
    name: "Siren Beep",
    namespace: "KristopherKubicki",
    author: "kristopher@acm.org",
    description: "Quickly Pulse a Siren",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan@2x.png")



preferences {
	section("Sirens"){
		input "sirens", "capability.alarm", title: "Which?", required: true, multiple: true
	}
	section("Virtual Switch"){
		input "dswitch", "capability.switch", title: "Which?", required: true, multiple: false
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
    subscribe(dswitch, "switch.on", switchHandler)

}

def switchHandler(evt) {
    sirens?.siren()
  	dswitch.off()
    sirens?.off()

}