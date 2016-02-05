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
 *  Wattvision
 *
 *  Author: steve
 *  Date: 2014-02-13
 */
metadata {

	definition(name: "Wattvision", namespace: "smartthings", author: "Steve Vlaminck") {
		capability "Power Meter"
		capability "Refresh"
		attribute "powerContent", "string"
	}

	simulator {
		// define status and reply messages here
	}

	tiles {

		valueTile("power", "device.power", canChangeIcon: true) {
			state "power", label: '${currentValue} W'
		}

		htmlTile(name: "powerContent", attribute: "powerContent", type: "HTML", whitelist: "www.wattvision.com" , url: '${currentValue}', width: 3, height: 2)

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "power"
		details(["powerContent", "power", "refresh"])

	}
}

def refresh() {
	parent.getDataFromWattvision()
	setGraphUrl(parent.getGraphUrl(device.deviceNetworkId))
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

public setGraphUrl(graphUrl) {

	log.trace "setting url for Wattvision graph"

	sendEvent([
		date           : new Date(),
		value          : graphUrl,
		name           : "powerContent",
		displayed      : false,
		isStateChange  : true,
		description    : "Graph updated",
		descriptionText: "Graph updated"
	])
}

public addWattvisionData(json) {

	log.trace "Adding data from Wattvision"

	def data = parseJson(json.data.toString())
	def units = json.units ?: "watts"

	if (data.size() > 0) {
		def latestData = data[-1]
		data.each {
			sendPowerEvent(it.t, it.v, units, (latestData == it))
		}
	}

}

private sendPowerEvent(time, value, units, isLatest = false) {
	def wattvisionDateFormat = parent.wattvisionDateFormat()

	def eventData = [
		date           : new Date().parse(wattvisionDateFormat, time),
		value          : value,
		name           : "power",
		displayed      : isLatest,
		isStateChange  : isLatest,
		description    : "${value} ${units}",
		descriptionText: "${value} ${units}"
	]

	log.debug "sending event: ${eventData}"
	sendEvent(eventData)

}

def parseJson(String s) {
	new groovy.json.JsonSlurper().parseText(s)
}
