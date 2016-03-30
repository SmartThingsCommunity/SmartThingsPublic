/**``
 *  Minimote
 *
 *  Author: huntb
 *  Date: 2015-02-21
 */

// Automatically generated. Make future change here.
definition(
    name: "Minimote Control",
    namespace: "",
    author: "bchunt3@gmail.com",
    description: "Minimote controller handler",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png")



preferences {
	section("Select an Aeon Minimote...") {
		input "minimote", "capability.Button", multiple: false
	}
	section("Pushing button 1 controls this switch...") {
		input "switches1", "capability.switch", multiple: false
	}
	section("Pushing button 2 controls this switch...") {
		input "switches2", "capability.switch", multiple: false
	}
	section("Pushing button 3 controls this switch... ") {
		input "switches3", "capability.switch", multiple: false
	}
	section("Pushing button 4 controls this switch... ") {
		input "switches4", "capability.switch", multiple: false
	}
    section("Holding button 1 controls this switch...") {
		input "switches5", "capability.switch", multiple: false
	}
	section("Holding button 2 controls this switch...") {
		input "switches6", "capability.switch", multiple: false
	}
	section("Holding button 3 controls this switch... ") {
		input "switches7", "capability.switch", multiple: false
	}
	section("Holding button 4 controls this switch... ") {
		input "switches8", "capability.switch", multiple: false
	}
}



def installed()
{
	subscribe(minimote, "button.pushed", buttonPushedHandler)
    subscribe(minimote, "button.held", buttonHeldHandler)
}



def updated()
{
	unsubscribe()
	subscribe(minimote, "button.pushed", buttonPushedHandler)
	subscribe(minimote, "button.held", buttonHeldHandler)
}



def buttonPushedHandler(evt) {
	log.debug "$evt.name: $evt.value, data: $evt.data, descriptionText: $evt.descriptionText"
	if (evt.data == '{"buttonNumber":1}') {
    	// log.debug switches1.currentValue("switch")
    	if ( switches1.currentValue("switch").contains('on') ) {
        	log.debug "Turning switch1 OFF"
        	switches1*.off()
    	}
        else {
        	log.debug "Turning switch1 ON"
    		switches1*.on()
            }
    } else 	if (evt.data == '{"buttonNumber":2}') {
    	if ( switches2.currentValue("switch").contains('on') ) {
        	log.debug "Turning switch2 OFF"
        	switches2*.off()
    	}
        else {
        	log.debug "Turning switch2 ON"
    		switches2*.on() 
            }
	} else 	if (evt.data == '{"buttonNumber":3}') {
    	if ( switches3.currentValue("switch").contains('on') ) {
        	log.debug "Turning switch3 OFF"
        	switches3*.off()
    	}
        else {
        	log.debug "Turning switch3 ON"
    		switches3*.on()
            }
    } else 	if (evt.data == '{"buttonNumber":4}') {
    	if ( switches4.currentValue("switch").contains('on') ) {
        	log.debug "Turning switch4 OFF"
        	switches4*.off()
    	}
        else {
        	log.debug "Turning switch4 ON"
    		switches4*.on()
            }
    }
}



def buttonHeldHandler(evt) {
	log.debug "$evt.name: $evt.value, data: $evt.data, descriptionText: $evt.descriptionText"
	if (evt.data == '{"buttonNumber":1}') {
    	// log.debug switches1.currentValue("switch")
    	if ( switches5.currentValue("switch").contains('on') ) {
        	log.debug "Turning switch5 OFF"
        	switches5*.off()
    	}
        else {
        	log.debug "Turning switch5 ON"
    		switches5*.on()
            }
    } else 	if (evt.data == '{"buttonNumber":2}') {
    	if ( switches6.currentValue("switch").contains('on') ) {
        	log.debug "Turning switch6 OFF"
        	switches6*.off()
    	}
        else {
        	log.debug "Turning switch6 ON"
    		switches6*.on()
            }
    } else 	if (evt.data == '{"buttonNumber":3}') {
    	if ( switches7.currentValue("switch").contains('on') ) {
        	log.debug "Turning switch7 OFF"
        	switches7*.off()
    	}
        else {
        	log.debug "Turning swtich7 ON"
    		switches7*.on()
            }
    } else 	if (evt.data == '{"buttonNumber":4}') {
    	if ( switches8.currentValue("switch").contains('on') ) {
        	log.debug "Turning switch8 OFF"
        	switches8*.off()
    	}
        else {
        	log.debug "Turning switch8 ON"
    		switches8*.on()
            }
    }
}