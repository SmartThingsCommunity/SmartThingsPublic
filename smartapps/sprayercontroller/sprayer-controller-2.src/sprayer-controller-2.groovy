/**
 *  Sprayer Controller 2
 *
 *  Copyright 2014 Cooper Lee
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
    name: "Sprayer Controller 2",
    namespace: "sprayercontroller",
    author: "Cooper Lee",
    description: "Control Sprayers for a period of time a number of times per hour",
    category: "My Apps",
    iconUrl: "http://www.mountpleasantwaterworks.com/images/ground_sprinkler.png",
    iconX2Url: "http://www.mountpleasantwaterworks.com/images/ground_sprinkler.png"
)


preferences {
	section("Select First Valve(s):") {
		input name: "valves1", type: "capability.switch", multiple: true
		input name: "startHour1", title: "Start Hour", type: "number"
		input name: "stopHour1", title: "Stop Hour", type: "number"
		input "minutes", "enum", title: "Run how many times an Hour?", expanded: true,
        options: ["1","2","3","4","5","6","12","20","30","60"] /*/
        options: ["0", "0,30", "0,20,40", "0,15,30,45", "0, 10, 15, 20, 25,30,35,40,45,50,55", "6", "7"] */
		input "duration", "number", title: "For how many seconds?"
	}

}


def installed() {
	log.debug "Installed with settings: ${settings}"
    def startHour = startHour1
    def stopHour = stopHour1
    def startTime = minutes
    if (minutes == "1") {
		startTime = "0 0 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "2") {
		startTime = "0 0,30 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "3") {
		startTime = "0 0,20,40 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "4") {
		startTime = "0 0,15,30,45 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "5") {
		startTime = "0 0,12,24,36,48 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "6") {
		startTime = "0 0,10,20,30,40,50 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "12") {
		startTime = "0 0,5,10,15,20,25,30,35,40,45,50,55 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "20") {
		startTime = "0 0,3,6,9,12,15,18,21,24,27,30,33,36,39,42,45,48,51,54,57 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "30") {
		startTime = "0 0,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58 " + startHour + "-" + stopHour + " * * ?"
	} else {
		startTime = "0 0 " + startHour + "-" + stopHour + " * * ?"
	}
	log.debug "${startTime}"   
    /*
    def stopTime = "0 $minutes $stopHour * * ?"   */
	schedule(startTime, openValve)
/*	schedule("0 0,5,10,15,20,25,30,35,40,45,50,55 " + startHour + "-" + stopHour + " * * ?", openValve) */
/*	schedule(stopTime, closeValve)  */
   	subscribe(valves1, "switch.on", valveOnHandler, [filterEvents: false])

}

def updated(settings) {
	unschedule()
	unsubscribe()
	log.debug "Installed with settings: ${settings}"
    def startHour = startHour1
    def stopHour = stopHour1
    def startTime = minutes
    if (minutes == "1") {
		startTime = "0 0 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "2") {
		startTime = "0 0,30 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "3") {
		startTime = "0 0,20,40 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "4") {
		startTime = "0 0,15,30,45 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "5") {
		startTime = "0 0,12,24,36,48 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "6") {
		startTime = "0 0,10,20,30,40,50 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "12") {
		startTime = "0 0,5,10,15,20,25,30,35,40,45,50,55 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "20") {
		startTime = "0 0,3,6,9,12,15,18,21,24,27,30,33,36,39,42,45,48,51,54,57 " + startHour + "-" + stopHour + " * * ?"
	} else if (minutes == "30") {
		startTime = "0 0,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58 " + startHour + "-" + stopHour + " * * ?"
	} else {
		startTime = "0 0 " + startHour + "-" + stopHour + " * * ?"
	}
	log.debug "${startTime}"   
    /*
    def stopTime = "0 $minutes $stopHour * * ?"   */
	schedule(startTime, openValve)
/*	schedule(stopTime, closeValve)  */
   	subscribe(valves1, "switch.on", valveOnHandler, [filterEvents: false])
/*	schedule("0 0,5,10,15,20,25,30,35,40,45,50,55 " + startHour + "-" + stopHour + " * * ?", openValve)  */

}

def openValve() {
	log.debug "Turning on Sprinklers ${valves1}"
	valves1.on()

}

def closeValve() {
	log.debug "Turning off Sprinklers ${valves1}"
	valves1.off()
}

def valveOnHandler(evt) {
	log.debug "Valve ${valves1} turned: ${evt.value}"
	def delay = duration
	log.debug "Turning off in ${duration/60} minutes (${delay}seconds)"
	runIn(delay, closeValve)
}

def setStartTime() {
    if (minutes == "1") {
		def startTime = "0 0 $startHour * * ?"
	} else if (minutes == "2") {
		def startTime = "0 0,30 $startHour * * ?"
	} else if (minutes == "3") {
		def startTime = "0 0,20,40 $startHour * * ?"
	} else if (minutes == "4") {
		def startTime = "0 0,15,30,45 $startHour * * ?"
	} else if (minutes == "5") {
		def startTime = "0 0,12,24,36,48 $startHour * * ?"
	} else if (minutes == "6") {
		def startTime = "0 0,10,20,30,40,50 $startHour * * ?"
	} else if (minutes == "12") {
		def startTime = "0 0,5,10,15,20,25,30,35,40,45,50,55 $startHour * * ?"
	} else if (minutes == "20") {
		def startTime = "0 0,3,6,9,12,15,18,21,24,27,30,33,36,39,42,45,48,51,54,57 $startHour * * ?"
	} else if (minutes == "30") {
		def startTime = "0 0,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58 $startHour * * ?"
	} else {
		def startTime = "0 0 $startHour * * ?"
	}
}
