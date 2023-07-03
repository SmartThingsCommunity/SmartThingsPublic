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
 *  lg kahn smart app to turn device on when temp is between two values
 *  and off otherwise. Also has to be between 2 date ranges.
 *  also give alerts when turning on off. 
 *  I use for a roof heater wire/coil.
 *
 *  Author: LGKahn kahn-st@lgk.com
 */
 
definition(
    name: "Roof Coil Controller",
    namespace: "smartthings",
    author: "lgkahn",
    description: "Control a roof coil or othe device(s) when temperature is between two values turns on and also has to be within a date range. Automatically turns off if one of the conditions is not met. Alerting option also.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Choose a temperature sensor... "){
		input "sensor", "capability.temperatureMeasurement", title: "Sensor"
	}
	section("Select the outlet(s)... "){
		input "outlets", "capability.switch", title: "Outlets", multiple: true
	}
	section("Turn on when temp is above ..."){
		input "onSetPoint", "decimal", title: "Set On Temp"
	}
	section("Turn off when temp is above ..."){
		input "offSetPoint", "decimal", title: "Set Off Temp"
	}
	section("Start after Date format (yyyymmdd)..."){
		input "startDate", "number", title: "Date?"
	}
	section("End after Date format (yyyymmdd)..."){
		input "endDate", "number", title: "Date?"
	}
	section("Time Zone Offset ie -5 etc...."){
		input "tzOffset", "number", title: "Offset?", range: "-12..12"
	}
     section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
        }
    }

}

def installed()
{
log.debug "in coil controller installed ... currenttemp = $sensor.currentTemperature"
	subscribe(sensor, "temperature", temperatureHandler)
}

def updated()
{
log.debug "in coil controller updated ... currenttemp = $sensor.currentTemperature"
	unsubscribe()
	subscribe(sensor, "temperature", temperatureHandler)
  // for debugging and testing uncomment  temperatureHandlerTest()
}


def temperatureHandler(evt)
{
def currenttemp = sensor.currentTemperature
/*
log.debug "in temp handler"
log.debug "current temp = $currenttemp"
log.debug "onSetPoint = $onSetPoint"
log.debug "offSetPoint = $offSetPoint"
log.debug "set offset = $tzOffset"
*/
	def today = new Date();

		def ltf = new java.text.SimpleDateFormat("yyyyMMdd")
		ltf.setTimeZone(TimeZone.getTimeZone("GMT${tzOffset}"))
		 
         String date1 = ltf.format(today);
         int intdate = Integer.parseInt(date1)
        // log.debug "int date = $intdate"
         //log.debug "enddate = $endDate"
        // log.debug "startdate = $startDate"
         def currSwitches = outlets.currentSwitch
         def onOutlets = currSwitches.findAll { switchVal ->
         switchVal == "on" ? true : false }
         
         if  (((intdate >= startDate) && (intdate <= endDate))
            && ((currenttemp > onSetPoint) && (currenttemp < offSetPoint)))
           {
                     // dont do anything if already on
                     if (onOutlets.size() != outlets.size())
                       {
                         log.debug "turning outlets On as $sensor.displayName is reporting $currenttemp which is between $onSetPoint and $offSetPoint, and we are within the date range ($startDate - $endDate)!"
            	         mysend("Turning device(s) On as $sensor.displayName is reporting a temperature of $currenttemp which is between $onSetPoint and $offSetPoint, and we are within the date range ($startDate - $endDate)!")
                         outlets.on()
                       }
                      else log.debug "Not turning on again, all already on!"
                 }
             else 
                 {
                    // dont do anything if already off
                     if (onOutlets.size() != 0)
                       {
                 	    log.debug "turning outlets Off! as $sensor.displayName is reporting $currenttemp which is Not between $onSetPoint and $offSetPoint, or we are no longer within the date range ($startDate - $endDate)!"
                        mysend("Turning device(s) Off as $sensor.displayName is reporting a temperature of $currenttemp which is not between $onSetPoint and $offSetPoint, or we are no longer within the date range ($startDate - $endDate)!")
                        outlets.off()
                       }
                      else log.debug "All outlets already off!"
                 }
}


def temperatureHandlerTest()
{
//log.trace "temperature: $evt.value, $evt"
// this routine is only for testing and debugging. to test or make changes uncomment the call in update.
// this is so we dont have to wait 15 minutes till a half an hour for the event to fire for testing.

def currenttemp = sensor.currentTemperature

log.debug "in temp handler test"
log.debug "current temp = $currenttemp"
log.debug "onSetPoint = $onSetPoint"
log.debug "offSetPoint = $offSetPoint"
log.debug "set offset = $tzOffset"
	def today = new Date();

		def ltf = new java.text.SimpleDateFormat("yyyyMMdd")
		ltf.setTimeZone(TimeZone.getTimeZone("GMT${tzOffset}"))
		 
         String date1 = ltf.format(today);
        int intdate = Integer.parseInt(date1)
         log.debug "int date = $intdate"
         log.debug "enddate = $endDate"
         log.debug "startdate = $startDate"
         def currSwitches = outlets.currentSwitch
         def onOutlets = currSwitches.findAll { switchVal ->
         switchVal == "on" ? true : false }

        log.debug "how many on = ${onOutlets.size()} "

      if  (((intdate >= startDate) && (intdate <= endDate))
            && ((currenttemp > onSetPoint) && (currenttemp < offSetPoint)))
           {
                     // dont do anything if already on
                     if (onOutlets.size() != outlets.size())
                       {
                         log.debug "turning outlets On as $sensor.displayName is reporting $currenttemp which is between $onSetPoint and $offSetPoint, and we are within the date range ($startDate - $endDate)!"
            	         //mysend("Turning device(s) On as $sensor.displayName is reporting a temperature of $currenttemp which is between $onSetPoint and $offSetPoint, and we are within the date range ($startDate - $endDate)!")
                         outlets.on()
                       }
                      else log.debug "Not turning on again, all already on!"
                 }
             else 
                 {
                    // dont do anything if already off
                     if (onOutlets.size() != 0)
                       {
                 	    log.debug "turning outlets Off! as $sensor.displayName is reporting $currenttemp which is Not between $onSetPoint and $offSetPoint, or we are no longer within the date range ($startDate - $endDate)!"
                        //mysend("Turning device(s) Off as $sensor.displayName is reporting a temperature of $currenttemp which is not between $onSetPoint and $offSetPoint, or we are no longer within the date range ($startDate - $endDate)!")
                        outlets.off()
                       }
                      else log.debug "All outlets already off!"
                 }
}


private mysend(msg) {
    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            log.debug("sending text message")
            sendSms(phone1, msg)
        }
    }

    log.debug msg
}
