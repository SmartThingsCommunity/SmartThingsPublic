/**
 *  MiHome Adapter Plus
 *
 *  Copyright 2016 Alex Lee Yuk Cheung
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
 *	VERSION HISTORY
 *	09.11.2016:	2.0 BETA Release 5.1 - Shift from data to state to hold variables.
 *	09.11.2016:	2.0 BETA Release 5 - Try to fix chart Android compatibility.
 *	08.11.2016:	2.0 BETA Release 4 - Added historical power chart data. RENAME TO ADAPTER PLUS.
 *	08.11.2016:	2.0 BETA Release 3 - Added ON and OFF buttons for devices that do not report state.
 *	06.11.2016:	2.0 BETA Release 2 - Various tile and formatting updates for Android.
 *	06.11.2016:	2.0 BETA Release 1 - Support for MiHome (Connect) v2.0. Inital version of device.
 */
metadata {
	definition (name: "MiHome Adapter Plus", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
        capability "Sensor"
        capability "Power Meter"
        
        command "on"
        command "off"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"rich-control", type:"lighting", width:6, height:4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                 attributeState "on", label:'${name}', action:"switch.off", icon:"st.Appliances.appliances17", backgroundColor:"#79b821", nextState:"on"
                 attributeState "off", label:'${name}', action:"switch.on", icon:"st.Appliances.appliances17", backgroundColor:"#ffffff", nextState:"off"
                 attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Appliances.appliances17", backgroundColor:"#79b821", nextState:"turningOff"
                 attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Appliances.appliances17", backgroundColor:"#ffffff", nextState:"turningOn"
                 attributeState "offline", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ff0000"
 			}
            tileAttribute("device.power", key: "SECONDARY_CONTROL") {
        		attributeState("default", label:'${currentValue} Wh', unit:"Wh")
    		}
        }
        
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.Appliances.appliances17", backgroundColor:"#79b821", nextState:"off"
            state "off", label:'${name}', action:"switch.on", icon:"st.Appliances.appliances17", backgroundColor:"#ffffff", nextState:"on"
            state "turningOn", label:'${name}', action:"switch.off", icon:"st.Appliances.appliances17", backgroundColor:"#79b821", nextState:"turningOff"
            state "turningOff", label:'${name}', action:"switch.on", icon:"st.Appliances.appliances17", backgroundColor:"#ffffff", nextState:"turningOn"
            state "offline", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ff0000"
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        valueTile("totalPower", "device.totalPower", decoration: "flat", width: 3, height: 1) {
			state "default", label: 'Today Total Power:\n${currentValue} Wh'
		}
        
        valueTile("yesterdayTotalPower", "device.yesterdayTotalPower", decoration: "flat", width: 3, height: 1) {
			state "default", label: 'Yesterday Total Power:\n${currentValue} Wh'
		}
        
        standardTile("onButton", "device.onButton", inactiveLabel: false, width: 2, height: 2) {
			state("default", label:'On', action:"on")
        }
        
        standardTile("offButton", "device.offButton", inactiveLabel: false, width: 2, height: 2) {
			state("default", label:'Off', action:"off")
        }
        
        htmlTile(name:"chartHTML", action: "getChartHTML", width: 6, height: 4, whiteList: ["www.gstatic.com"])

        main(["switch"])
        details(["rich-control", "onButton", "offButton", "refresh", "totalPower", "yesterdayTotalPower", "chartHTML"])
	}
}

mappings {
	path("/getChartHTML") {action: [GET: "getChartHTML"]}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute

}

// handle commands
def poll() {
	log.debug "Executing 'poll' for ${device} ${this} ${device.deviceNetworkId}"
    
    def resp = parent.apiGET("/subdevices/show?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger()]).toString()))
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
        sendEvent(name: "switch", value: "offline", descriptionText: "The device is offline")
		return []
	}
    
    log.debug "***ADAPTER PLUS JSON for ${device.name}: " + resp.data.data
    
    def power_state = resp.data.data.power_state
    //def power_state = 1
    if (power_state != null) {
    	sendEvent(name: "switch", value: power_state == 0 ? "off" : "on")
    }
    def real_power = resp.data.data.real_power
    //def real_power = Math.abs(new Random().nextInt() % 600 + 1)
    if (real_power != null) {
   		sendEvent(name: "power", value: real_power as BigDecimal, unit: "Wh")
    }
    def today_wh = resp.data.data.today_wh
    //def today_wh = Math.abs(new Random().nextInt() % 3000 + 1)
    if (today_wh != null) {
    
    	//Calculate change of day
    	def df = new java.text.SimpleDateFormat("D")
        if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("Europe/London"))
		}
        def currentDay = df.format(new Date()).toInteger()
        def changeOfDay = false
        if ((state.day != null) && (currentDay != state.day)) {
        	changeOfDay = true
        }
        state.day = currentDay
    	
        if (state.last_wh_reading != null) {
        	//Determine when the days total wh reading has been reset and store as chart data.
        	if (((today_wh as BigDecimal) < state.last_wh_reading) || (today_wh == 0 && changeOfDay)) { 
            	addYesterdayTotalToChartData(state.last_wh_reading)
                sendEvent(name: "yesterdayTotalPower", value: state.last_wh_reading, unit: "Wh")
            }
        }
    	state.last_wh_reading = today_wh as BigDecimal
    	sendEvent(name: "totalPower", value: today_wh as BigDecimal, unit: "Wh")
        addCurrentTotalToChartData(today_wh as BigDecimal)
    }
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}

def on() {
	log.debug "Executing 'on'"
	def resp = parent.apiGET("/subdevices/power_on?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger()]).toString()))
    if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
	}
   	else {
    	refresh()
    } 
}

def off() {
	log.debug "Executing 'off'"
	def resp = parent.apiGET("/subdevices/power_off?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger()]).toString()))
    if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
	}
   	else {
    	refresh()
    }
}

def addYesterdayTotalToChartData(total) {
	if (state.chartData == null) {
    	state.chartData = [0, total, 0, 0, 0, 0, 0]
    }
    else {
    	state.chartData[0] = total
    	state.chartData.add(0, 0)
        state.chartData.pop()
    }
}

def addCurrentTotalToChartData(total) {
	if (state.chartData == null) {
    	state.chartData = [total, 0, 0, 0, 0, 0, 0]
    }
    state.chartData[0] = total
}

def getChartHTML() {
	try {
    	def date = new Date()
		if (state.chartData == null) {
    		state.chartData = [0, 0, 0, 0, 0, 0, 0]
    	}
        def hData = ""
        if (state.last_wh_reading != null && state.last_wh_reading > 0) {
			hData = """
			<script type="text/javascript">
				  	google.charts.load('current', {packages: ['corechart', 'bar']});
					google.charts.setOnLoadCallback(drawBasic);

					function drawBasic() {
						var data = google.visualization.arrayToDataTable([
         						['Date', 'Power', { role: 'style' }],
         						['${(date - 6).format("d MMM")}', ${state.chartData[6]}, '#0a9928'],   
         						['${(date - 5).format("d MMM")}', ${state.chartData[5]}, '#0a9928'],   
         						['${(date - 4).format("d MMM")}', ${state.chartData[4]}, '#0a9928'],            
         						['${(date - 3).format("d MMM")}', ${state.chartData[3]}, '#0a9928'],            
         						['${(date - 2).format("d MMM")}', ${state.chartData[2]}, '#0a9928'],
		 						['${(date - 1).format("d MMM")}', ${state.chartData[1]}, '#0a9928' ], 
         						['Today', ${state.chartData[0]}, '#eda610' ], 
      					]);

      					var options = {
        						title: "Total Power in the Last 7 Days",
        						width: 410,
        						height: 220,
       					 		bar: {groupWidth: "75%"},
        						legend: { position: "none" },
        						vAxis: {
          							title: 'Power (Wh)'
        						}
      					};

      					var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));
      					chart.draw(data, options);
    				}
					
			</script>
			  
			"""
        } else {
        	hData = """
            	<div class="centerText" style="font-family: helvetica, arial, sans-serif;">
				  <p>Not enough data to create power history chart.</p>
				  <p>Currently collecting data. Come back in a couple of hours.</p>
				</div>
            """
        }

		def mainHtml = """
		<!DOCTYPE html>
		<html>
			<head>
				<meta charset="utf-8"/>
				<meta http-equiv="cache-control" content="max-age=0"/>
				<meta http-equiv="cache-control" content="no-cache"/>
				<meta http-equiv="expires" content="0"/>
				<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
				<meta http-equiv="pragma" content="no-cache"/>
				<meta name="viewport" content="width = device-width, user-scalable=no, initial-scale=1.0">
				<script type="text/javascript" src="${getChartJsData()}"></script>
			</head>
			<body>
  				<div id="chart_div"></div>
                ${hData}
				</body>
			</html>
		"""
		render contentType: "text/html", data: mainHtml, status: 200
	}
	catch (ex) {
		log.error "getChartHTML Exception:", ex
	}
}

def getChartJsData() {
	def chartJsData = null
	//def htmlInfo = state?.htmlInfo
	def htmlInfo
	state.chartJsData = null
	if(htmlInfo?.chartJsUrl && htmlInfo?.chartJsVer) {
		if(state?.chartJsData) {
			if (state?.chartJsVer?.toInteger() == htmlInfo?.chartJsVer?.toInteger()) {
				//LogAction("getChartJsData: Chart Javascript Data is Current | Loading Data from State...")
				chartJsData = state?.chartJsData
			} else if (state?.chartJsVer?.toInteger() < htmlInfo?.chartJsVer?.toInteger()) {
				//LogAction("getChartJsData: Chart Javascript Data is Outdated | Loading Data from Source...")
				chartJsData = getFileBase64(htmlInfo.chartJsUrl, "text", "css")
				state.chartJsData = chartJsData
				state?.chartJsVer = htmlInfo?.chartJsVer
			}
		} else {
			//LogAction("getChartJsData: Chart Javascript Data is Missing | Loading Data from Source...")
			chartJsData = getFileBase64(htmlInfo.chartJsUrl, "text", "css")
			state?.chartJsData = chartJsData
			state?.chartJsVer = htmlInfo?.chartJsVer
		}
	} else {
		//LogAction("getChartJsData: No Stored Chart Javascript Data Found for Device... Loading for Static URL...")
		chartJsData = getFileBase64(chartJsUrl(), "text", "javascript")
	}
	return chartJsData
}

def getFileBase64(url, preType, fileType) {
	try {
		def params = [
			uri: url,
			contentType: '$preType/$fileType'
		]
		httpGet(params) { resp ->
			if(resp.data) {
				def respData = resp?.data
				ByteArrayOutputStream bos = new ByteArrayOutputStream()
				int len
				int size = 4096
				byte[] buf = new byte[size]
				while ((len = respData.read(buf, 0, size)) != -1)
					bos.write(buf, 0, len)
				buf = bos.toByteArray()
				//LogAction("buf: $buf")
				String s = buf?.encodeBase64()
				//LogAction("resp: ${s}")
				return s ? "data:${preType}/${fileType};base64,${s.toString()}" : null
			}
		}
	}
	catch (ex) {
		log.error "getFileBase64 Exception:", ex
	}
}

def chartJsUrl() { return "https://www.gstatic.com/charts/loader.js" }