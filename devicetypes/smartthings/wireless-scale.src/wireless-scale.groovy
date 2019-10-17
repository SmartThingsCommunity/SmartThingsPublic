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
 *	Smart Body Analyzer
 *
 *	Author: SmartThings
 *	Date: 2013-09-27
 */
metadata {
	definition (name: "Wireless Scale", namespace: "smartthings", author: "SmartThings") {
		capability "Polling"

		attribute "weight", "string"
		attribute "leanMass", "string"
		attribute "fatRatio", "string"
		attribute "fatMass", "string"

		command "storeGraphImage"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Withings", action: "", icon: "st.Bath.bath2", backgroundColor: "#FFFFFF"
		}

		plotTile("weightGraph", "device.weight", "scatter", width:2, height:1, content:"weightPoints")
		valueTile("weight", "device.weight", width: 1, height: 1) {
			state("weight", label:'${currentValue} lbs', unit:"lbs", inactiveLabel: false)
		}

		plotTile("fatGraph", "device.fatRatio", "scatter", width:2, height:1, content:"fatPoints")
		valueTile("fatRatio", "device.fatRatio", width: 1, height: 1) {
			state("fatRatio", label:'${currentValue}% bodyfat', unit:"bodyfat", inactiveLabel: false)
		}

		standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"polling.poll", icon:"st.secondary.refresh"
		}

		main "icon"
		details(["weightGraph", "weight", "fatGraph", "fatRatio", "refresh"])
	}
}

mappings {
	path("/weightPoints") {
		action: [
			GET: "weightPoints"
		]
	}
	path("/fatPoints") {
		action: [
			GET: "fatPoints"
		]
	}
}

def parse(String description) {
	log.debug "Parsing '${description}'"
}

def parse(Map event) {
	log.debug "Parsing '${event}'"

	if(event.status == 200 && event.data)
	{
		parent.parse(event)
		return null
	}
	else if(["weight", "leanMass", "fatRatio", "fatMass"].contains(event.name) && event.date)
	{
		def dateString = event.date
		data["measure.${event.name}.$dateString"] = event.value

		def old = "measure.${event.name}." + (new Date() - 30).format('yyyy-MM-dd')
		data.findAll { it.key.startsWith("measure.${event.name}.") && it.key < old }.collect { it.key }.each { state.remove(it) }
	}

	return event
}

def storeGraphImage(String name, ByteArrayInputStream is, String contentType) {
	storeImage(name, is, contentType)
}

def poll() {
	log.debug "Executing 'poll'"
	parent.poll()
}

def refresh() {
	log.debug "Executing 'refresh'"
	sendEvent(name:"refreshing", description:"Refreshing Withings data", displayed:true)

	return null
}

def measurementPoints(name) {
	def points = []

	def wdata = normalizeMeasurementPoints(name)
	log.debug "data: ${wdata}"
	def allValues = wdata.collect { it.y }
	log.debug "allValues: ${allValues}"

	def min = allValues.min()
	def max = allValues.max()

	def minMax = [:]
	minMax[min] = min
	minMax[max] = max
	log.debug "minMax: $minMax"

	wdata.reverse().each { it ->
		points << plotPoint(it.x, it.y, minMax)
	}
	log.debug "points: ${points}"

	return points.reverse()
}

private normalizeMeasurementPoints(name) {
	def measurementData = data.findAll { it.key.startsWith("measure.${name}.") }
	log.debug "measurementData: ${measurementData}"

	def normalizedData = []
	measurementData.each { k, v ->
		def d = Date.parse('yyyy-MM-dd', k - "measure.${name}.")
		Calendar cal = Calendar.getInstance();
		cal.setTime(d)
		// BUG: DOES NOT HANDLE NEW YEAR PROPERLY
		// Should concat YEAR + (PAD_LEFT(DAY_OF_YEAR))
		// 2013365 == Dec. 31, 2013
		// 2014001 == Jan. 1, 2014
		normalizedData << [x:cal.get(Calendar.DAY_OF_YEAR), y:v]
	}
	log.debug "normalizedData: ${normalizedData}"

	normalizedData.sort{ it.x }
}

private plotPoint(x, y, minMax) {
	def removed = minMax.remove(y) != null

	return [
		color:"",
		fillColor: removed ? "" : "#f3f3f3",
		symbolStyle:"elipse",
		point:y,
		label: removed ? "${y}" : "",
		x:x,
		y:y
	]
}

def weightPoints() {
	return [
		"title":"My Weight",
		"plots":[
			"weight":[
				"points":measurementPoints("weight")
			]
		]
	]
}

def fatPoints() {
	return [
		"title":"Bodyfat %",
		"plots":[
			"fat":[
				"points":measurementPoints("fatRatio")
			]
		]
	]
}