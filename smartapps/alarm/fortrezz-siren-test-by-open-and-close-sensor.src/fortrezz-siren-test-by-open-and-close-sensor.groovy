/**
 *  Fortrezz siren test by open and close sensor
 *
 *  Copyright 2016 Cz
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
    name: "Fortrezz siren test by open and close sensor",
    namespace: "alarm",
    author: "Cz",
    description: "alarm",
    category: "My Apps",
    iconUrl: "https://static.wixstatic.com/media/15eeff_b050ea350d0f4733aa6a881660a2c49a.png/v1/fill/w_200,h_200,al_c/15eeff_b050ea350d0f4733aa6a881660a2c49a.png",
    iconX2Url: "https://static.wixstatic.com/media/15eeff_b050ea350d0f4733aa6a881660a2c49a.png/v1/fill/w_200,h_200,al_c/15eeff_b050ea350d0f4733aa6a881660a2c49a.png",
    iconX3Url: "https://static.wixstatic.com/media/15eeff_b050ea350d0f4733aa6a881660a2c49a.png/v1/fill/w_200,h_200,al_c/15eeff_b050ea350d0f4733aa6a881660a2c49a.png")


preferences {
	section("Siren by open and close sensor")
     section("When Multipurpose Sencor do something"){
    	input "theMultipurpose","capability.contactSensor",required:true,title:"which Multipurpose sensor?"
    }
    section("Siren or alarm"){
        input "theAlarm","capability.alarm",required:true,title:"which switch Siren or alarm?"
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
	subscribe(theMultipurpose,"contact",contactHandler)
}
def contactHandler(evt)
{
	if(evt.value=="open")
    {
    	//theAlarm.strobe()
        //theAlarm.siren()
        theAlarm.both()
        runIn(1,theAlarmoff)
    }
    else if(evt.value=="closed")
    {
    	theAlarm.off()
    }
}

def theAlarmoff()
{
	theAlarm.off()
}
// TODO: implement event handlers