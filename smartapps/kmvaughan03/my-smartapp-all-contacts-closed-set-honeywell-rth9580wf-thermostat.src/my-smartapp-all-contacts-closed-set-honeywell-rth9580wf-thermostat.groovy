/**
 *  All contacts closed - set thermostat
 *
 *  Copyright 2016 Kevin Vaughan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "My SmartApp: All contacts closed - set Honeywell RTH9580WF thermostat",
    namespace: "kmvaughan03",
    author: "Kevin Vaughan",
    description: "when a sensor is closed - verify all other open/close sensors are closed - if so, set thermostat back on (to revert changes made by thermostat mode director (thermostat and doors).",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Check when any of these contacts close:") {
		input "contact", "capability.contactSensor", title: "pick contact sensors", required: true, multiple: true
	}
    section("If all of these contacts are closed:") {
		input "allcontacts", "capability.contactSensor", title: "pick contact sensors", required: true, multiple: true
    }
    section("Set this thermostat:") {
        input "thermostat", "capability.thermostat", title: "pick a thermostat", required: true
    }
    section("To the thermostat mode to:") {
        input "tmode", "enum", required: true, title: "pick a thermostat mode", options: ["autocool","autoheat","idle","heat","cool","off"]
		// NOTE: using different possible options than specified by 'Smartthings Capabilities Reference'in http://docs.smartthings.com/en/latest/capabilities-reference.html
        // NOTE: the options used here are specific to work with the Honeywell RTH9580WF thermostat
}
    section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input("recipients", "contact", title: "Send notifications to") {
			input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
			paragraph "If outside the US please make sure to enter the proper country code"
			input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, options: ["Yes", "No"]
		}
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

    subscribe(contact, "contact", contactHandler)
}

def contactHandler(evt)
{
 	log.debug "contactHandler called: $evt"


    
    if("open" == evt.value)
    // contact was opened - do nothing
	log.debug "Contact is in ${evt.value} state - DO NOTHING"
  
    if("closed" == evt.value)
    // contact was closed, turn on the thermostat.
    {
    
    //check all sensors closed
    def currentcontact = allcontacts.currentValue("contact")
	log.debug currentcontact
    def contactValue = allcontacts.find{it.currentcontact == "open"}
	log.debug contactValue
    
    if(!contactValue)
        {
			
             log.debug "thermostat.thermostatMode = ${thermostat.currentValue("thermostatMode")}"
   			 log.debug "Attmpting to set thermostat mode to ${tmode.value}"
             
             if(thermostat.currentValue("thermostatMode").equalsIgnoreCase(tmode))
             {
             	// do nothing - thermostat already set to desired mode
             	log.debug "thermostat already set to desired mode - do nothing"
             }
             else{
             
                thermostat.setThermostatMode(tmode)
            	log.debug "thermostat.thermostatMode = ${thermostat.currentValue("thermostatMode")}"
             	sendMessage(evt)
             }
    	}
       else{
			log.debug "Another contact is open - DO NOTHING"
		}

    }
}

private sendMessage(evt) {
	String msg = messageText
	Map options = [:]

	if (!messageText) {
		msg = "My SmartApp:" +
              "'All contacts closed - set Honeywell RTH9580WF thermostat' executed!" + 
              "Thermostat set to ${tmode}"
              
		options = [translatable: true, triggerEvent: evt]
	}
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients, options)
	} else {
		if (!phone || pushAndPhone != 'No') {
			log.debug 'sending push'
			options.method = 'push'
			//sendPush(msg)
		}
		if (phone) {
			options.phone = phone
			log.debug 'sending SMS'
			//sendSms(phone, msg)
		}
		sendNotification(msg, options)
	}

	if (frequency) {
		state[evt.deviceId] = now()
	}
}

private defaultText(evt) {
	if (evt.name == 'presence') {
		if (evt.value == 'present') {
			if (includeArticle) {
				'{{ triggerEvent.linkText }} has arrived at the {{ location.name }}'
			}
			else {
				'{{ triggerEvent.linkText }} has arrived at {{ location.name }}'
			}
		} else {
			if (includeArticle) {
				'{{ triggerEvent.linkText }} has left the {{ location.name }}'
			}
			else {
				'{{ triggerEvent.linkText }} has left {{ location.name }}'
			}
		}
	} else {
		'{{ triggerEvent.descriptionText }}'
	}
}