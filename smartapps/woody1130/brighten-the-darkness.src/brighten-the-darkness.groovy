/**
 *  Brighten The Darkness
 *
 *  Copyright 2016 Michael Wood
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
    name: "Brighten The Darkness",
    namespace: "woody1130",
    author: "Michael Wood",
    description: "Turns lights on for X minutes when something happens, with the option to only run after sunset or before sunrise.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
		 page(name: "whenHappens", title: "When Something Happens",
         nextPage: "doSomething", uninstall: true){
	
			section("When Movement Starts...") {
				input "motion1", "capability.motionSensor", 
            	title: "Where?", multiple: true, required: false
			}
    		section("Or A Door Opens...") {
    			input "contactSensors1", "capability.contactSensor",
            	title: "Open/close sensors", multiple: true, required: false
    		}
    }
    	page(name: "doSomething", title: "Turn On The Lights",
         nextPage: "nameOfSomeOtherPage", uninstall: true){
			section("Turn on a light...") {
				input "switch1", "capability.switch", multiple: true
			}
    		section("Time to keep the light on...") {
    			input "mins", "number", title: "Minutes"
    		}
            section("Use Only When Its Dark...") {
    			input(name: "boolDark", type: "enum", title: "Yes/No", options: ["Yes","No"])
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

	subscribe(motion1, "motion.active", motionActiveHandler)
    subscribe(contactSensors1, "contact", contactHandler)
    
}    

def contactHandler(evt) {

	if("open" == evt.value) {
    
    runActions()
    
    }

}

def motionActiveHandler(evt) {
    
	runActions()
    
}

def runActions() {

	if (isItDarkOut()) 
    	{
			turnOn()
    	}
    	else
    	{
    	log.debug("Not Time For Light!!")
    	}

}

def turnOn() {
		
        //Calculate delay till off in seconds
		log.debug("Turning on...")
    
    	//Turn lights on
		switch1.on()
    
    	//Wait and then turn lights off
    	log.debug("Waiting...")
    	runIn(60 * mins, timedTurnOff)
        
}

def timedTurnOff() {

	log.debug("Turning Off...")
    switch1.off()
    
}

def isItDarkOut() {

	def sunRiseStr = (location.currentValue("sunriseTime"))
	def sunSetStr = (location.currentValue("sunsetTime"))
    
    Date sunRise = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunRiseStr).plus(-1)
    Date sunSet = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunSetStr).plus(-1)
    Date now = new Date()

	if ((now < sunRise) || (now > sunSet) || (boolDark == "No")){
    	
        return true
        
    }
    else{
    
    return false
    
    }
}