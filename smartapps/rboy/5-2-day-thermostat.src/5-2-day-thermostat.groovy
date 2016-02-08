/* **DISCLAIMER**
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * Without limitation of the foregoing, Contributors/Regents expressly does not warrant that:
 * 1. the software will meet your requirements or expectations;
 * 2. the software or the software content will be free of bugs, errors, viruses or other defects;
 * 3. any results, output, or data provided through or generated by the software will be accurate, up-to-date, complete or reliable;
 * 4. the software will be compatible with third party software;
 * 5. any errors in the software will be corrected.
 * The user assumes all responsibility for selecting the software and for the results obtained from the use of the software. The user shall bear the entire risk as to the quality and the performance of the software.
 */ 

/**
 *  5-2 Day Thermostat
 *
 * Base code from mwoodengr@hotmail.com, bugfixed and enhanced by RBoy
 * Changes Copyright RBoy, redistribution of any changes or modified code is not allowed without permission
 * 2015-2-11 - Fixed issue with fan mode
 * Updated: 2014-12-13
 *
 */
definition(
		name: "5-2 Day Thermostat",
		namespace: "rboy",
		author: "RBoy",
		description: "Weekday and Weekend Thermostat",
    	category: "Green Living",
    	iconUrl: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
    	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png",
    	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@3x.png")

preferences {
	section("Choose thermostat ") {
		input "thermostat", "capability.thermostat", required: true
	}

    section("Switch HVAC mode (auto to cool/heat) based on the outside temperature (optional)") {
		input "temperatureSensor", "capability.temperatureMeasurement", required: false
		input "temperatureH", "number", title: "Switch to heating temperature", required: false, description: "Temperature below which switch to heat mode"
		input "temperatureC", "number", title: "Switch to cooling temperature", required: false, description: "Temperature above which switch to cool mode"
	}

	section("Monday to Friday Schedule") {
		input "time1", "time", title: "Wake Time", required: true
		input "tempSetpoint1", "number", title: "Wake Heat Temp", required: true
		input "tempSetpointA", "number", title: "Wake Cool Temp", required: true
		input "time2", "time", title: "Leave Time", required: true
		input "tempSetpoint2", "number", title: "Leave Heat Temp", required: true
		input "tempSetpointB", "number", title: "Leave Cool Temp", required: true
		input "time3", "time", title: "Return Time", required: true
		input "tempSetpoint3", "number", title: "Return Heat Temp", required: true
		input "tempSetpointC", "number", title: "Return Cool Temp", required: true
		input "time4", "time", title: "Sleep Time", required: true
		input "tempSetpoint4", "number", title: "Sleep Heat Temp", required: true
		input "tempSetpointD", "number", title: "Sleep Cool Temp", required: true
	}
	section("Saturday and Sunday Schedule") {
		input "time11", "time", title: "Wake Time", required: true
		input "tempSetpoint11", "number", title: "Wake Heat Temp", required: true
		input "tempSetpointAA", "number", title: "Wake Cool Temp", required: true
		input "time21", "time", title: "Leave Time", required: true
		input "tempSetpoint21", "number", title: "Leave Heat Temp", required: true
		input "tempSetpointBB", "number", title: "Leave Cool Temp", required: true
		input "time31", "time", title: "Return Time", required: true
		input "tempSetpoint31", "number", title: "Return Heat Temp", required: true
		input "tempSetpointCC", "number", title: "Return Cool Temp", required: true
		input "time41", "time", title: "Sleep Time", required: true
		input "tempSetpoint41", "number", title: "Sleep Heat Temp", required: true
		input "tempSetpointDD", "number", title: "Sleep Cool Temp", required: true
	}
}

def installed()
{
	subscribeToEvents()
}

def updated()
{
    unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(temperatureSensor, "temperature", temperatureHandler)
    subscribe(location, modeChangeHandler)

    initialize()
}

// Handle mode changes, reinitialize the current temperature and timers after a mode change, this is to workaround the issue of the last timer firing while in a non running mode, resume operations when supported modes are set
def modeChangeHandler(evt) {
	log.debug "Reinitializing thermostat on mode change notification, new mode $evt.value"
	//sendNotificationEvent("$thermostat Reinitializing on mode change notification, new mode $evt.value")
    initialize()
}

// This section sets the HVAC mode based outside temperature. HVAC fan mode is set to "auto".
def temperatureHandler(evt) {
	log.debug "Heat mode switch temperature $temperatureH, cool mode switch temperature $temperatureC"

	if (temperatureH == null || temperatureC == null) { // We are in Auto mode or user doesn't want us to switch modes
		return
	}

    def extTemp = temperatureSensor.currentTemperature
	log.debug "External temperature is: $extTemp"
	def thermostatState = thermostat.currentThermostatMode
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "HVAC current mode $thermostatState"
	log.debug "HVAC Fan current mode $thermostatFan"
	if (extTemp < temperatureH) {
		if (thermostatState == "cool") {
			def hvacmode = "heat"
			thermostat.setThermostatMode(hvacmode)
			log.debug "HVAC mode set to $hvacmode"
		}
	}
	else if (extTemp > temperatureC) {
		if (thermostatState == "heat") {
			def hvacmode = "cool"
			thermostat.setThermostatMode(hvacmode)
			log.debug "HVAC mode set to $hvacmode"
		}
	}

	if (thermostatFan != "fanAuto") {
		thermostat.setThermostatFanMode("auto")
		log.debug "HVAC fan mode set to auto"
	}
}

// This section determines which day it is.
def initialize() {

	unschedule()
	def calendar = Calendar.getInstance()
	calendar.setTimeZone(location.timeZone)
	def today = calendar.get(Calendar.DAY_OF_WEEK)
   	def timeNow = now()
	def midnightToday = timeToday("2000-01-01T23:59:59.999-0000", location.timeZone)
   	log.debug("Current time is ${(new Date(timeNow)).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
	log.debug("Midnight today is ${midnightToday.format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
   	log.trace("Weekday schedule1 ${timeToday(time1, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekday schedule2 ${timeToday(time2, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekday schedule3 ${timeToday(time3, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekday schedule4 ${timeToday(time4, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekend schedule1 ${timeToday(time11, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekend schedule2 ${timeToday(time21, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekend schedule3 ${timeToday(time31, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekend schedule4 ${timeToday(time41, location.timeZone).format("HH:mm z", location.timeZone)}")

	// This section is where the time/temperature schedule is set
	switch (today) {
		case Calendar.MONDAY:
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
		case Calendar.THURSDAY:
        	if (timeNow >= timeToday(time1, location.timeZone).time && timeNow < timeToday(time2, location.timeZone).time) { // Are we between 1st time and 2nd time
        		changeTemp1()
            	schedule(timeToday(time2, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time2, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time2, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	}
        	else if (timeNow >= timeToday(time2, location.timeZone).time && timeNow < timeToday(time3, location.timeZone).time) { // Are we between 2nd time and 3rd time
            	changeTemp2()
				schedule(timeToday(time3, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time3, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time3, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= timeToday(time3, location.timeZone).time && timeNow < timeToday(time4, location.timeZone).time) { // Are we between 3rd time and 4th time
            	changeTemp3()
				schedule(timeToday(time4, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time4, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time4, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= timeToday(time4, location.timeZone).time && timeNow < midnightToday.time) { // Are we between 4th time and midnight, schedule next day
            	changeTemp4()
				schedule(timeToday(time1, location.timeZone) + 1, initialize)
                log.info("$thermostat Scheduled next adjustment for ${(timeToday(time1, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${(timeToday(time1, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= (midnightToday - 1).time && timeNow < timeToday(time1, location.timeZone).time) { // Are we between midnight yesterday and 1st time, schedule today
            	changeTemp4()
				schedule(timeToday(time1, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time1, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time1, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
			break

		case Calendar.FRIDAY:
        	if (timeNow >= timeToday(time1, location.timeZone).time && timeNow < timeToday(time2, location.timeZone).time) { // Are we between 1st time and 2nd time
        		changeTemp1()
            	schedule(timeToday(time2, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time2, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time2, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	}
        	else if (timeNow >= timeToday(time2, location.timeZone).time && timeNow < timeToday(time3, location.timeZone).time) { // Are we between 2nd time and 3rd time
            	changeTemp2()
				schedule(timeToday(time3, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time3, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time3, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= timeToday(time3, location.timeZone).time && timeNow < timeToday(time4, location.timeZone).time) { // Are we between 3rd time and 4th time
            	changeTemp3()
				schedule(timeToday(time4, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time4, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time4, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= timeToday(time4, location.timeZone).time && timeNow < midnightToday.time) { // Are we between 4th time Friday and midnight, we schedule Saturday
            	changeTemp4()
				schedule(timeToday(time11, location.timeZone) + 1, initialize)
                log.info("$thermostat Scheduled next adjustment for ${(timeToday(time11, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${(timeToday(time11, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= (midnightToday - 1).time && timeNow < timeToday(time11, location.timeZone).time) { // Are we between midnight Friday and 1st time on Saturday, we schedule Saturday
            	changeTemp4()
				schedule(timeToday(time11, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time11, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time11, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
			break

		case Calendar.SATURDAY:
            if (timeNow >= timeToday(time11, location.timeZone).time && timeNow < timeToday(time21, location.timeZone).time) { // Are we between 1st time and 2nd time
        		changeTemp11()
            	schedule(timeToday(time21, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time21, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time21, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	}
        	else if (timeNow >= timeToday(time21, location.timeZone).time && timeNow < timeToday(time31, location.timeZone).time) { // Are we between 2nd time and 3rd time
            	changeTemp21()
				schedule(timeToday(time31, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time31, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time31, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= timeToday(time31, location.timeZone).time && timeNow < timeToday(time41, location.timeZone).time) { // Are we between 3rd time and 4th time
            	changeTemp31()
				schedule(timeToday(time41, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time41, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time41, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= timeToday(time41, location.timeZone).time && timeNow < midnightToday.time) { // Are we between 4th time and midnight, schedule the next day
            	changeTemp41()
				schedule(timeToday(time11, location.timeZone) + 1, initialize)
                log.info("$thermostat Scheduled next adjustment for ${(timeToday(time11, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${(timeToday(time11, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= (midnightToday - 1).time && timeNow < timeToday(time11, location.timeZone).time) { // Are we between midnight yesterday and 1st time, schedule today
            	changeTemp41()
				schedule(timeToday(time11, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time11, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time11, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
			break

		case Calendar.SUNDAY:
            if (timeNow >= timeToday(time11, location.timeZone).time && timeNow < timeToday(time21, location.timeZone).time) { // Are we between 1st time and 2nd time
        		changeTemp11()
            	schedule(timeToday(time21, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time21, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time21, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	}
        	else if (timeNow >= timeToday(time21, location.timeZone).time && timeNow < timeToday(time31, location.timeZone).time) { // Are we between 2nd time and 3rd time
            	changeTemp21()
				schedule(timeToday(time31, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time31, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time31, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= timeToday(time31, location.timeZone).time && timeNow < timeToday(time41, location.timeZone).time) { // Are we between 3rd time and 4th time
            	changeTemp31()
				schedule(timeToday(time41, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time41, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time41, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= timeToday(time41, location.timeZone).time && timeNow < midnightToday.time) { // Are we between 4th time Sunday and midnight, we schedule Monday
            	changeTemp41()
				schedule(timeToday(time1, location.timeZone) + 1, initialize)
                log.info("$thermostat Scheduled next adjustment for ${(timeToday(time1, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${(timeToday(time1, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
            else if (timeNow >= (midnightToday - 1).time && timeNow < timeToday(time1, location.timeZone).time) { // Are we between midnight Sunday and 1st time on Monday, we schedule Monday
            	changeTemp41()
				schedule(timeToday(time1, location.timeZone), initialize)
                log.info("$thermostat Scheduled next adjustment for ${timeToday(time1, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
                //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time1, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            }
			break
	}
}

// This section is where the thermostat temperature settings are set. 
def changeTemp1() {
	def thermostatState = thermostat.currentThermostatMode
	log.debug "Thermostat mode = $thermostatState"
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "Thermostat fan = $thermostatFan"
	if (thermostatState == "auto") {
		thermostat.setHeatingSetpoint(tempSetpoint1)
		thermostat.setCoolingSetpoint(tempSetpointA)
        log.info "Set $thermostat Heat $tempSetpoint1°, Cool $tempSetpointA°"
	}
	else if (thermostatState == "heat") {
		thermostat.setHeatingSetpoint(tempSetpoint1)
        log.info "Set $thermostat Heat $tempSetpoint1°"
	}
	else {
		thermostat.setCoolingSetpoint(tempSetpointA)
        log.info "Set $thermostat Cool $tempSetpointA°"
	}
}

def changeTemp2() {
	def thermostatState = thermostat.currentThermostatMode
	log.debug "Thermostat mode = $thermostatState"
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "Thermostat fan = $thermostatFan"
	if (thermostatState == "auto") {
		thermostat.setHeatingSetpoint(tempSetpoint2)
		thermostat.setCoolingSetpoint(tempSetpointB)
        log.info "Set $thermostat Heat $tempSetpoint2°, Cool $tempSetpointB°"
	}
	else if (thermostatState == "heat") {
		thermostat.setHeatingSetpoint(tempSetpoint2)
	}
	else {
		thermostat.setCoolingSetpoint(tempSetpointB)
	}
}

def changeTemp3() {
	def thermostatState = thermostat.currentThermostatMode
	log.debug "Thermostat mode = $thermostatState"
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "Thermostat fan = $thermostatFan"
	if (thermostatState == "auto") {
		thermostat.setHeatingSetpoint(tempSetpoint3)
		thermostat.setCoolingSetpoint(tempSetpointC)
        log.info "Set $thermostat Heat $tempSetpoint3°, Cool $tempSetpointC°"
	}
	else if (thermostatState == "heat") {
		thermostat.setHeatingSetpoint(tempSetpoint3)
        log.info "Set $thermostat Heat $tempSetpoint3°"
	}
	else {
		thermostat.setCoolingSetpoint(tempSetpointC)
        log.info "Set $thermostat Cool $tempSetpointC°"
	}
}

def changeTemp4() {
	def thermostatState = thermostat.currentThermostatMode
	log.debug "Thermostat mode = $thermostatState"
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "Thermostat fan = $thermostatFan"
	if (thermostatState == "auto") {
		thermostat.setHeatingSetpoint(tempSetpoint4)
		thermostat.setCoolingSetpoint(tempSetpointD)
        log.info "Set $thermostat Heat $tempSetpoint4°, Cool $tempSetpointD°"
	}
	else if (thermostatState == "heat") {
		thermostat.setHeatingSetpoint(tempSetpoint4)
        log.info "Set $thermostat Heat $tempSetpoint4°"
	}
	else {
		thermostat.setCoolingSetpoint(tempSetpointD)
        log.info "Set $thermostat Cool $tempSetpointD°"
	}
}

def changeTemp11() {
	def thermostatState = thermostat.currentThermostatMode
	log.debug "Thermostat mode = $thermostatState"
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "Thermostat fan = $thermostatFan"
	if (thermostatState == "auto") {
		thermostat.setHeatingSetpoint(tempSetpoint11)
		thermostat.setCoolingSetpoint(tempSetpointAA)
        log.info "Set $thermostat Heat $tempSetpoint11°, Cool $tempSetpointAA°"
	}
	else if (thermostatState == "heat") {
		thermostat.setHeatingSetpoint(tempSetpoint11)
        log.info "Set $thermostat Heat $tempSetpoint11°"
	}
	else {
		thermostat.setCoolingSetpoint(tempSetpointAA)
        log.info "Set $thermostat Cool $tempSetpointAA°"
	}
}

def changeTemp21() {
	def thermostatState = thermostat.currentThermostatMode
	log.debug "Thermostat mode = $thermostatState"
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "Thermostat fan = $thermostatFan"
	if (thermostatState == "auto") {
		thermostat.setHeatingSetpoint(tempSetpoint21)
		thermostat.setCoolingSetpoint(tempSetpointBB)
        log.info "Set $thermostat Heat $tempSetpoint21°, Cool $tempSetpointBB°"
	}
	else if (thermostatState == "heat") {
		thermostat.setHeatingSetpoint(tempSetpoint21)
        log.info "Set $thermostat Heat $tempSetpoint21°"
	}
	else {
		thermostat.setCoolingSetpoint(tempSetpointBB)
        log.info "Set $thermostat Cool $tempSetpointBB°"
	}
}

def changeTemp31() {
	def thermostatState = thermostat.currentThermostatMode
	log.debug "Thermostat mode = $thermostatState"
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "Thermostat fan = $thermostatFan"
	if (thermostatState == "auto") {
		thermostat.setHeatingSetpoint(tempSetpoint31)
		thermostat.setCoolingSetpoint(tempSetpointCC)
        log.info "Set $thermostat Heat $tempSetpoint31°, Cool $tempSetpointCC°"
	}
	else if (thermostatState == "heat") {
		thermostat.setHeatingSetpoint(tempSetpoint31)
        log.info "Set $thermostat Heat $tempSetpoint31°"
	}
	else {
		thermostat.setCoolingSetpoint(tempSetpointCC)
        log.info "Set $thermostat Cool $tempSetpointCC°"
	}
}

def changeTemp41() {
	def thermostatState = thermostat.currentThermostatMode
	log.debug "Thermostat mode = $thermostatState"
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "Thermostat fan = $thermostatFan"
	if (thermostatState == "auto") {
		thermostat.setHeatingSetpoint(tempSetpoint41)
		thermostat.setCoolingSetpoint(tempSetpointDD)
        log.info "Set $thermostat Heat $tempSetpoint41°, Cool $tempSetpointDD°"
	}
	else if (thermostatState == "heat") {
		thermostat.setHeatingSetpoint(tempSetpoint41)
        log.info "Set $thermostat Heat $tempSetpoint41°"
	}
	else {
		thermostat.setCoolingSetpoint(tempSetpointDD)
        log.info "Set $thermostat Cool $tempSetpointDD°"
	}
}