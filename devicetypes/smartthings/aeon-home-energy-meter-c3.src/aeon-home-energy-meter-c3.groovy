/**
 *  Aeon Home Energy Meter + C3
 *
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
 */
metadata {
	// Automatically generated. Make future change here.
	definition(name: "Aeon Home Energy Meter + C3", namespace: "smartthings", author: "SmartThings") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Sensor"

		command "reset"

//		fingerprint deviceId: "0x2101", inClusters: " 0x70,0x31,0x72,0x86,0x32,0x80,0x85,0x60"
	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}
	}

	// tile definitions
	tiles {
		valueTile("power", "device.power", decoration: "flat") {
			state "default", label: '${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat") {
			state "default", label: '${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label: 'reset kWh', action: "reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat") {
			state "configure", label: '', action: "configuration.configure", icon: "st.secondary.configure"
		}

		PLATFORM_graphTile(name: "powerGraph", attribute: "device.power")

		main(["power", "energy"])
		details(["powerGraph", "power", "energy", "reset", "refresh", "configure"])
	}
}

// ========================================================
// PREFERENCES
// ========================================================

preferences {
	input name: "graphPrecision", type: "enum", title: "Graph Precision", description: "Daily", required: true, options: PLATFORM_graphPrecisionOptions(), defaultValue: "Daily"
	input name: "graphType", type: "enum", title: "Graph Type", description: selectedGraphType(), required: false, options: PLATFORM_graphTypeOptions()
}

def selectedGraphPrecision() {
	graphPrecision ?: "Daily"
}

def selectedGraphType() {
	graphType ?: "line"
}

// ========================================================
// MAPPINGS
// ========================================================

mappings {
	path("/graph/:attribute") {
		action:
		[
			GET: "renderGraph"
		]
	}
	path("/graphDataSizes") { // for testing. remove before publishing
		action:
		[
			GET: "graphDataSizes"
		]
	}
}

def graphDataSizes() { // for testing. remove before publishing
	state.findAll { k, v -> k.startsWith("measure.") }.inject([:]) { attributes, attributeData ->
		attributes[attributeData.key] = attributeData.value.inject([:]) { dateTypes, dateTypeData ->
			dateTypes[dateTypeData.key] = dateTypeData.value.size()
			dateTypes
		}
		attributes
	}
}

// ========================================================
// Z-WAVE
// ========================================================

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x31: 1, 0x32: 1, 0x60: 3])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"

	PLATFORM_migrateGraphDataIfNeeded()
	PLATFORM_storeData(result.name, result.value)

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	if (cmd.scale == 0) {
		[name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
	} else if (cmd.scale == 1) {
		[name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
	} else {
		[name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def refresh() {
	delayBetween([
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def reset() {
	// No V1 available
	return [
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet(scale: 0).format()
	]
}

def configure() {
	def cmd = delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4).format(),   // combined power in watts
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8).format(),   // combined energy in kWh
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
		zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0).format(),    // no third report
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 300).format() // every 5 min
	])
	log.debug cmd
	cmd
}

// ========================================================
// GRAPH RENDERING // written by developer. Alter at will
// ========================================================

def renderGraph() {

	def data = PLATFORM_fetchGraphData(params.attribute)

	def totalData = data*.runningSum

	def xValues = data*.unixTime

	def yValues = [
		Total: [color: "#49a201", data: totalData, type: selectedGraphType()]
	]

	PLATFORM_renderGraph(attribute: params.attribute, xValues: xValues, yValues: yValues, focus: "Total", label: "Watts")
}

// TODO: // ========================================================
// TODO: // PLATFORM CODE !!! DO NOT ALTER !!!
// TODO: // ========================================================

// ========================================================
// PLATFORM TILES
// ========================================================

def PLATFORM_graphTile(Map tileParams) {
	def cleanAttribute = tileParams.attribute - "device." - "capability."
	htmlTile([name: tileParams.name, attribute: tileParams.attribute, action: "graph/${cleanAttribute}", width: 3, height: 2] + tileParams)
}

// ========================================================
// PLATFORM GRAPH RENDERING
// ========================================================

private PLATFORM_graphTypeOptions() {
	[
		"line", // DEFAULT
		"spline",
		"step",
		"area",
		"area-spline",
		"area-step",
		"bar",
		"scatter",
		"pie",
		"donut",
		"gauge",
	]
}

private PLATFORM_renderGraph(graphParams) {

	String attribute = graphParams.attribute
	List xValues = graphParams.xValues
	Map yValues = graphParams.yValues
	String focus = graphParams.focus ?: ""
	String label = graphParams.label ?: ""

	/*
	def xValues = [1, 2]

	def yValues = [
			High: [type: "spline", data: [5, 6], color: "#bc2323"],
			Low: [type: "spline", data: [0, 1], color: "#153591"]
	]

	Available type values:
	line 	// DEFAULT
	spline
	step
	area
	area-spline
	area-step
	bar
	scatter
	pie
	donut
	gauge

*/

	def graphData = PLATFORM_buildGraphData(xValues, yValues, label)

	def legendData = yValues*.key
	def focusJS = focus ? "chart.focus('${focus}')" : "// focus not specified"
	def flowColumn = focus ?: yValues ? yValues.keySet().first() : null

	def htmlTitle = "${(device.label ?: device.name)} ${attribute.capitalize()} Graph"
	renderHTML(htmlTitle) { html ->
		html.head {
			"""
			<!-- Load c3.css -->
			<link href="https://www.dropbox.com/s/m6ptp72cw4nx0sp/c3.css?dl=1" rel="stylesheet" type="text/css">

			<!-- Load d3.js and c3.js -->
			<script src="https://www.dropbox.com/s/9x22jyfu5qyacpp/d3.v3.min.js?dl=1" charset="utf-8"></script>
			<script src="https://www.dropbox.com/s/to7dtcn403l7mza/c3.js?dl=1"></script>

			<script>
			function getDocumentHeight() {
				var body = document.body;
				var html = document.documentElement;

				return html.clientHeight;
			}
			function getDocumentWidth() {
				var body = document.body;
				var html = document.documentElement;

				return html.clientWidth;
			}
			</script>

			<style>
				.legend {
					position: absolute;
					width: 80%;
					padding-left: 15%;
					z-index: 999;
					padding-top: 5px;
				}
				.legend span {
					width: ${100 / yValues.size()}%;
					display: inline-block;
					text-align: center;
					cursor: pointer;
					color: white;
				}
			</style>
			"""
		}
		html.body {
			"""
			<div class="legend"></div>
			<div id="chart" style="max-height: 120px; position: relative;"></div>

			<script>

			// Generate the chart
			var chart = c3.generate(${graphData as grails.converters.JSON});

			// Resize the chart to the size of the device tile
			chart.resize({height:getDocumentHeight(), width:getDocumentWidth()});

			// Focus data if specified
			${focusJS}

			// Update the chart when ${attribute} events are received
			function ${attribute}(evt) {
				var newValue = ['${flowColumn}'];
				newValue.push(evt.value);

				var newX = ['x'];
				newX.push(evt.unixTime);

				chart.flow({
					columns: [
						newX,
						newValue
					]
				});
			}

			// Build the custom legend
			d3.select('.legend').selectAll('span')
    		.data(${legendData as grails.converters.JSON})
  			.enter().append('span')
    		.attr('data-id', function (id) { return id; })
    		.html(function (id) { return id; })
    		.each(function (id) {
        	d3.select(this).style('background-color', chart.color(id));
    		})
    		.on('mouseover', function (id) {
     	   chart.focus(id);
    		})
    		.on('mouseout', function (id) {
        	chart.revert();
    		})
    		.on('click', function (id) {
        	chart.toggle(id);
    		});

			</script>
			"""
		}
	}
}

private PLATFORM_buildGraphData(List xValues, Map yValues, String label = "") {

	/*
	def xValues = [1, 2]

	def yValues = [
			High: [type: "spline", data: [5, 6], color: "#bc2323"],
			Low: [type: "spline", data: [0, 1], color: "#153591"]
	]
	*/

	[
		interaction: [
			enabled: false
		],
		bindto     : '#chart',
		padding    : [
			left  : 30,
			right : 30,
			bottom: 0,
			top   : 0
		],
		legend     : [
			show: false,
//			hide    : false,//(yValues.keySet().size() < 2),
//			position: 'inset',
//            inset: [
//            	anchor: "top-right"
//            ],
//            item: [
//            	onclick: "do nothing" // (yValues.keySet().size() > 1) ? null : "do nothing"
//            ]
		],
		data       : [
			x      : "x",
			columns: [(["x"] + xValues)] + yValues.collect { k, v -> [k] + v.data },
			types  : yValues.inject([:]) { total, current -> total[current.key] = current.value.type; return total },
			colors : yValues.inject([:]) { total, current -> total[current.key] = current.value.color; return total }
		],
		axis       : [
			x: [
				type: 'timeseries',
				tick: [
					centered: true,
					culling : [max: 7],
					fit     : true,
					format  : PLATFORM_getGraphDateFormat()
//				format: PLATFORM_getGraphDateFormatFunction() // throws securityException when trying to escape javascript
				]
			],
			y: [
				label  : label,
				padding: [
					top: 50
				]
			]
		]
	]
}

private PLATFORM_getGraphDateFormat(dateType = selectedGraphPrecision()) {
	// https://github.com/mbostock/d3/wiki/Time-Formatting
	def graphDateFormat
	switch (dateType) {
		case "Live":
			graphDateFormat = "%I:%M" // hour (12-hour clock) as a decimal number [00,12] // AM or PM
			break
		case "Hourly":
			graphDateFormat = "%I %p" // hour (12-hour clock) as a decimal number [00,12] // AM or PM
			break
		case "Daily":
			graphDateFormat = "%a" // abbreviated weekday name
			break
		case "Monthly":
			graphDateFormat = "%b" // abbreviated month name
			break
		case "Annually":
			graphDateFormat = "%y" // year without century as a decimal number [00,99]
			break
	}
	graphDateFormat
}

private String PLATFORM_getGraphDateFormatFunction(dateType = selectedGraphPrecision()) {
	def graphDateFunction = "function(date) { return date; }"
	switch (dateType) {
		case "Live":
			graphDateFunction = """
			function(date) {
			 return.getMinutes();
			}
			"""
			break;
		case "Hourly":
			graphDateFunction = """ function(date) {
			  var hour = date.getHours();
			  if (hour == 0) {
			   return String(/12 am/).substring(1).slice(0,-1);
			  } else if (hour > 12) {
			   return hour -12 + String(/ pm/).substring(1).slice(0,-1);
			  } else {
			   return hour + String(/ am/).substring(1).slice(0,-1);
			  }
			 }"""
			break
		case "Daily":
			graphDateFunction = """ function(date) {
			  var day = date.getDay();
			  switch(day) {
			   case 0: return String(/Sun/).substring(1).slice(0,-1);
			   case 1: return String(/Mon/).substring(1).slice(0,-1);
			   case 2: return String(/Tue/).substring(1).slice(0,-1);
			   case 3: return String(/Wed/).substring(1).slice(0,-1);
			   case 4: return String(/Thu/).substring(1).slice(0,-1);
			   case 5: return String(/Fri/).substring(1).slice(0,-1);
			   case 6: return String(/Sat/).substring(1).slice(0,-1);
			  }
			 }"""
			break
		case "Monthly":
			graphDateFunction = """ function(date) {
			  var month = date.getMonth();
			  switch(month) {
			   case 0: return String(/Jan/).substring(1).slice(0,-1);
			   case 1: return String(/Feb/).substring(1).slice(0,-1);
			   case 2: return String(/Mar/).substring(1).slice(0,-1);
			   case 3: return String(/Apr/).substring(1).slice(0,-1);
			   case 4: return String(/May/).substring(1).slice(0,-1);
			   case 5: return String(/Jun/).substring(1).slice(0,-1);
			   case 6: return String(/Jul/).substring(1).slice(0,-1);
			   case 7: return String(/Aug/).substring(1).slice(0,-1);
			   case 8: return String(/Sep/).substring(1).slice(0,-1);
			   case 9: return String(/Oct/).substring(1).slice(0,-1);
			   case 10: return String(/Nov/).substring(1).slice(0,-1);
			   case 11: return String(/Dec/).substring(1).slice(0,-1);
			  }
			 }"""
			break
		case "Annually":
			graphDateFunction = """
			function(date) {
			 return.getFullYear();
			}
			"""
			break
	}
	groovy.json.StringEscapeUtils.escapeJavaScript(graphDateFunction)
}

private jsEscapeString(str = "") {
	"String(/${str}/).substring(1).slice(0,-1);"
}

private PLATFORM_fetchGraphData(attribute) {

	log.debug "PLATFORM_fetchGraphData(${attribute})"

	/*
	[
		[
			dateString: "2014-12-1",
			unixTime: 1421931600000,
			min: 0,
			max: 10,
			average: 5
		],
		...
	]
	*/

	def attributeBucket = state["measure.${attribute}"] ?: [:]
	def dateType = selectedGraphPrecision()
	attributeBucket[dateType]
}

// ========================================================
// PLATFORM DATA STORAGE
// ========================================================

private PLATFORM_graphPrecisionOptions() { ["Live", "Hourly", "Daily", "Monthly", "Annually"] }

private PLATFORM_storeData(attribute, value) {
	PLATFORM_graphPrecisionOptions().each { dateType ->
		PLATFORM_addDataToBucket(attribute, value, dateType)
	}
}

/*
[
	Hourly: [
		[
			dateString: "2014-12-1",
			unixTime: 1421931600000,
			min: 0,
			max: 10,
			average: 5
		],
		...
	],
	...
]
*/

private PLATFORM_addDataToBucket(attribute, value, dateType) {

	def numberValue = value.toBigDecimal()

	def attributeKey = "measure.${attribute}"
	def attributeBucket = state[attributeKey] ?: [:]

	def dateTypeBucket = attributeBucket[dateType] ?: []

	def now = new Date()
	def itemDateString = now.format("PLATFORM_get${dateType}Format"())
	def item = dateTypeBucket.find { it.dateString == itemDateString }

	if (!item) {
		// no entry for this data point yet, fill with initial values
		item = [:]
		item.average = numberValue
		item.runningSum = numberValue
		item.runningCount = 1
		item.min = numberValue
		item.max = numberValue
		item.unixTime = now.getTime()
		item.dateString = itemDateString

		// add the new data point
		dateTypeBucket << item

		// clear out old data points
		def old = PLATFORM_getOldDateString(dateType)
		if (old) { // annual data never gets cleared
			dateTypeBucket.findAll { it.unixTime < old }.each { dateTypeBucket.remove(it) }
		}

		// limit the size of the bucket. Live data can stack up fast
		def sizeLimit = 25
		if (dateTypeBucket.size() > sizeLimit) {
			dateTypeBucket = dateTypeBucket[-sizeLimit..-1]
		}

	} else {
		//re-calculate average/min/max for this bucket
		item.runningSum = (item.runningSum.toBigDecimal()) + numberValue
		item.runningCount = item.runningCount.toInteger() + 1
		item.average = item.runningSum.toBigDecimal() / item.runningCount.toInteger()

		if (item.min == null) {
			item.min = numberValue
		} else if (numberValue < item.min.toBigDecimal()) {
			item.min = numberValue
		}
		if (item.max == null) {
			item.max = numberValue
		} else if (numberValue > item.max.toBigDecimal()) {
			item.max = numberValue
		}
	}

	attributeBucket[dateType] = dateTypeBucket
	state[attributeKey] = attributeBucket
}

private PLATFORM_getOldDateString(dateType) {
	def now = new Date()
	def date
	switch (dateType) {
		case "Live":
			date = now.getTime() - 60 * 60 * 1000 // 1h * 60m * 60s * 1000ms // 1 hour
			break
		case "Hourly":
			date = (now - 1).getTime()
			break
		case "Daily":
			date = (now - 10).getTime()
			break
		case "Monthly":
			date = (now - 30).getTime()
			break
		case "Annually":
			break
	}
	date
}

private PLATFORM_getLiveFormat() { "HH:mm:ss" }

private PLATFORM_getHourlyFormat() { "yyyy-MM-dd'T'HH" }

private PLATFORM_getDailyFormat() { "yyyy-MM-dd" }

private PLATFORM_getMonthlyFormat() { "yyyy-MM" }

private PLATFORM_getAnnuallyFormat() { "yyyy" }

// ========================================================
// PLATFORM GRAPH DATA MIGRATION
// ========================================================

private PLATFORM_migrateGraphDataIfNeeded() {
	if (!state.hasMigratedOldGraphData) {
		def acceptableKeys = PLATFORM_graphPrecisionOptions()
		def needsMigration = state.findAll { k, v -> v.keySet().findAll { !acceptableKeys.contains(it) } }.keySet()
		needsMigration.each { PLATFORM_migrateGraphData(it) }
		state.hasMigratedOldGraphData = true
	}
}

private PLATFORM_migrateGraphData(attribute) {

	log.trace "about to migrate ${attribute}"

	def attributeBucket = state[attribute] ?: [:]
	def migratedAttributeBucket = [:]

	attributeBucket.findAll { k, v -> !PLATFORM_graphPrecisionOptions().contains(k) }.each { oldDateString, oldItem ->

		def dateType = oldDateString.contains('T') ? "Hourly" : PLATFORM_graphPrecisionOptions().find {
			"PLATFORM_get${it}Format"().size() == oldDateString.size()
		}

		def dateTypeFormat = "PLATFORM_get${dateType}Format"()

		def newBucket = attributeBucket[dateType] ?: []
/*
		def existingNewItem = newBucket.find { it.dateString == oldDateString }
		if (existingNewItem) {
			newBucket.remove(existingNewItem)
		}
*/

		def newItem = [
			min         : oldItem.min,
			max         : oldItem.max,
			average     : oldItem.average,
			runningSum  : oldItem.runningSum,
			runningCount: oldItem.runningCount,
			dateString  : oldDateString,
			unixTime    : new Date().parse(dateTypeFormat, oldDateString).getTime()
		]

		newBucket << newItem
		migratedAttributeBucket[dateType] = newBucket
	}

	state[attribute] = migratedAttributeBucket
}
