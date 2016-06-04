/**
 *  Copyright 2015 SmartThings
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
 *  It's Too Cold
 *
 *  Author: SmartThings
 */
definition(
    name: "TempMinder",
    namespace: "dancoffey",
    author: "dcoffey3296",
    description: "Monitor the temperature and turns on/off a heater or AC unit.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences 
{
	section("Settings") {
		input "temperatureSensor1", "capability.temperatureMeasurement", title: "Which Temperature Sensor?"
		input "temperature1", "number", title: "If the temp drops below..."
        input "temperature2", "number", title: "If the temp rises above..."
        input "switch1", "capability.switch", required: true, title: "The outlet to toggle..."
    }

    section("Global Override") {
		input "override", "bool", title: "Turn off outlet and ignore temp."
        input "summerMode", "bool", title: "Summer Mode, use with A/C."
	}
}


def installed() 
{
	// setup the app on first install
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def updated() 
{

	// updated settings, re-subscribe
	unsubscribe()
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
    
    // if the override is on, turn off the switch and exit
    if (override)
    {
    	switch1.off()
        return
    }
    else
    {
    	// use with heater
    	if (!settings.summerMode)
        {
            // if the current temp is lower than the threshold, turn on the outlet
            if (settings.temperatureSensor1.currentValue("temperature") < temperature1)
            {
                log.trace("Updated prefs, turning on switch since the current temp is below $temperature1")
                switch1.on()
            }
        }
        // use with AC
        else
        {
        	// if the current temp is lower than the threshold, turn on the outlet
            if (settings.temperatureSensor1.currentValue("temperature") > temperature1)
            {
                log.trace("Updated prefs, turning on switch since the current temp is above $temperature1 (summerMode active)")
                switch1.on()
            }
        }
    }
}

def temperatureHandler(evt) 
{
	log.trace "temperature: $evt.value, $evt"

	def switchOn = false
	if (settings.switch1.currentValue("switch") == "on")
    {
    	log.debug("Switch is on " + settings.switch1.currentValue("switch"))
    	switchOn = true
    }
    else
    {
    	log.debug("Switch is off " + settings.switch1.currentValue("switch"))
        switchOn = false
    }

	// setup vars
	def tooCold = temperature1
    def tooHot = temperature2
	def mySwitch = settings.switch1
   
   // winder mode, use with heater
   if (!summerMode)
   {
   		log.trace("Winter Mode")
        
   		// if the temp is below the low threshold, turn on the switch
        if (evt.doubleValue < tooCold) 
        {
            if (switchOn)
            {
                log.debug("Switch already on, exit")
                return
            }

            def msg = "Temperature dropped below $tooCold, turning on $mySwitch"
            log.debug(msg)
            switch1.on()
            sendPush(msg)
        } 
        else if (evt.doubleValue > tooHot) 
        {
            if (!switchOn)
            {
                log.debug("Switch already off, exit")
                return
            }

            def msg = "Temperature rose above $tooHot, turning off $mySwitch"
            log.debug(msg)
            switch1.off()
            sendPush(msg)
        }
        else 
        {
            log.debug("Temp is $evt.doubleValue, not too hot nor too cold")
        }	
   }
   // summer mode, use with AC
   else
   {
   		log.trace("Summer Mode")
   		// if the temp is above the high threshold, turn on the switch
        if (evt.doubleValue > tooHot) 
        {
            if (switchOn)
            {
                log.debug("Switch already on, exit")
                return
            }

            def msg = "Temperature rose above below $tooHot, turning on $mySwitch"
            log.debug(msg)
            switch1.on()
            sendPush(msg)
        } 
        else if (evt.doubleValue < tooCold) 
        {
            if (!switchOn)
            {
                log.debug("Switch already off, exit")
                return
            }

            def msg = "Temperature dropped below $tooCold, turning off $mySwitch"
            log.debug(msg)
            switch1.off()
            sendPush(msg)
        }
        else 
        {
            log.debug("Temp is $evt.doubleValue, not too hot nor too cold")
        }
   }	
}
