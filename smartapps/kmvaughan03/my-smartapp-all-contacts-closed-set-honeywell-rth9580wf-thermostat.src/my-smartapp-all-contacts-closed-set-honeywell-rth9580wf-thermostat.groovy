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
 *  --------------------+---------------------------+-------------------------------+------------------------------------
 *  Device Type         | Attribute Name			| Commands                  	| Attribute Values
 *  --------------------+---------------------------+-------------------------------+------------------------------------
 *  --------------------+---------------------------+-------------------------------+------------------------------------
 *  contactSensors     	| contact       			|                           	| open, closed
 *  --------------------+---------------------------+-------------------------------+------------------------------------
 *  temperatureSensors  | temperature   			|                           	|	 
 *						| heatingSetpoint			| setHeatingSetpoint(number)	|
 *						| coolingSetpoint			| setCoolingSetpoint(number)	|
 *						| thermostatSetpoint		|								|
 *						| thermostatMode			| setThermostatMode(string)		| auto,emergency heat,heat,off,cool
 *						| thermostatFanMode			| setThermostatFanMode(string)	| auto,on,circulate
 *						| thermostatOperatingState	| setThermostatFanMode(string)	| heating,idle,pending cool,
 *						|							|								| vent economizer,cooling,pending heat
 *						|							|								| fan only
 *						|							| off()							|
 *						|							| heat()						|
 *						|							| emergencyheat()				|
 *						|							| cool()						|
 *						|							| fanOn()						|
 *						|							| fanAuto()						|
 *						|							| fanCirculate()				|
 *						|							| auto()						|
 *  --------------------+---------------------------+-------------------------------+------------------------------------
 *	NOTE: FOR "Honeywell RTH9580WF thermostat" the thermostatMode Attribute Values are: 
 *        "autocool,autoheat,idle,heat,cool,off"
 */
 
definition(
    name: 			"My SmartApp: All contacts closed - set Honeywell RTH9580WF thermostat",
    namespace: 		"kmvaughan03",
    author: 		"Kevin Vaughan",
    description:	"when a CONTACT sensor is closed - verify all other CONTACT sensors are closed. " +
					"If so, set thermostat back on (to revert changes made by thermostat mode director smartapp " +
					"(thermostat and doors).",
    category: 		"Convenience",
    iconUrl: 		"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: 		"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: 		"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {


	section("When any of these contacts close") {
		input "contact", "capability.contactSensor", title: "Contact Sensors:", required: true, multiple: true
	}
    
    
    section("Check if all of these contacts are also closed") {
		input "allcontacts", "capability.contactSensor", title: "Contact Sensors:", required: true, multiple: true
    }
    
    
    section("If all contacts are closed, set this thermostat") {
        input "thermostat", "capability.thermostat", title: "Thermostat:", required: true
    }
    
    
    section("To this thermostat mode") {
        paragraph "NOTE: These are the possible values specfic for the Honeywell RTH9580WF thermostat."
        input "tmode", "enum", required: true, title: "Thermostat Mode:", options: ["autocool","autoheat","idle","heat","cool","off"]

		/**
         *	NOTE: using different possible options than specified by 
         *	'Smartthings Capabilities Reference'in http://docs.smartthings.com/en/latest/capabilities-reference.html
         *	NOTE: the options used here are specific to the Honeywell RTH9580WF thermostat
         */
	}
    
    
    section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text:", required: false
	}
	
    
    section("Via a push notification and/or an SMS message"){
		input("recipients", "contact", title: "Send notifications to") {
			input "phone", "phone", title: "Phone Number (for SMS, optional):", required: false
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


def contactHandler(evt) {

 	log.debug "contactHandler called: $evt"


    if("open" == evt.value){
    
    	// contact was opened - do nothing
		log.debug "Contact is in ${evt.value} state - DO NOTHING"
	
    }
    
    
    log.debug "thermostat.thermostatMode = ${thermostat.currentValue("thermostatMode")}"  
    
    if(("closed" == evt.value) && (thermostat.currentValue("thermostatMode").equalsIgnoreCase("off"))){
    
    	// contact was closed and thermostat is off, check if all contacts are closed.
    	
        //check if all contacts are closed
    	def currentcontact = allcontacts.currentValue("contact")
		log.debug currentcontact
    	def contactValue = allcontacts.find{it.currentcontact == "open"}
		log.debug contactValue
    

    	if(!contactValue){
    
    		//no contacts are open, set the thermostat mode			
	  	
        	log.debug "thermostat.thermostatMode = ${thermostat.currentValue("thermostatMode")}"
   	 		log.debug "Attmpting to set thermostat mode to ${tmode.value}"
        	     
        	thermostat.setThermostatMode(tmode)
        
        	log.debug "thermostat.thermostatMode = ${thermostat.currentValue("thermostatMode")}"
        
        	//started thermostat, send notification
        	sendMessage(evt)

    	}
    	else{
    
    		//A contact closed, but there are other contacts open, do nothing
			log.debug "Another contact is open - DO NOTHING"
		
		}

    }
   	else{
    
    		//Thermostat is not off - nothing to do
			log.debug "Thermostat is not off - DO NOTHING"
		
	}
}


private sendMessage(evt) {

	String msg = messageText
	Map options = [:]

	if (!messageText) {
		
        msg = "My SmartApp: All contacts closed - Honeywell RTH9580WF Thermostat set to ${tmode}"              
		options = [translatable: true, triggerEvent: evt]
        
        log.info msg
        
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