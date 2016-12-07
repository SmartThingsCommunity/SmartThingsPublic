/**
 *  OVO Energy Meter V2.0
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
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
 *  VERSION HISTORY
 *  v2.0 - Initial V2.0 Release with OVO Energy (Connect) app
 *
 *  v2.1 - Improve pricing calculations using contract info from OVO. Notification framework for high costs.
 *		   Enable alert for specified daily cost level breach.
 *	v2.1b - Allow cost alert level to be decimal
 *
 *	v2.2 - Percentage comparison from previous cost values added into display
 *	v2.2.1 - Add current consumption price based on unit price from OVO account API not OVO live API
 *	v2.2.1b - Remove double negative on percentage values.
 *	v2.2.2 - Change current hour logic to accommodate GMT/BST.
 *	v2.2.2b - Alter Simple Date Format hour string
 *
 *	10.11.2016: v2.3 - Added historical power chart for the last 5 days.
 *	10.11.2016: v2.3.1 - Fix chart Android compatibility.
 *	11.11.2016: v2.3.2 - Move chart data into state variable
 *	11.11.2016: v2.3.3 - Prevent potential executeAction() error when adding device.
 *	11.11.2016: v2.3.4 - Migrate variable from data to state.
 *	11.11.2016: v2.3.5 - Bug Fix. Silly state variable not initialised on first run.
 *	11.11.2016: v2.3.6 - Reduce number of calls to account API.
 *	12.11.2016: v2.3.7 - Stop yesterday cost comparison being 0%.
 *
 *	06.12.2016: v2.4 - Better API failure handling and recovery. Historical and yesterday power feed from OVO API.
 *  06.12.2016: v2.4.1 - Relax setting offline mode to 60 minute down time.
 */
preferences 
{
	input( "costAlertLevel", "decimal", title: "Set cost alert level (£)", description: "Send alert when daily cost reaches amount", required: false, defaultValue: 10.00 )
}

metadata {
	definition (name: "OVO Energy Meter V2.0", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Polling"
		capability "Power Meter"
		capability "Refresh"
        capability "Sensor"
        
        attribute "network","string"
	}

	tiles(scale: 2) {
  		multiAttributeTile(name:"power", type:"generic", width:6, height:4, canChangeIcon: true) {
    		tileAttribute("device.power", key: "PRIMARY_CONTROL") {
      			attributeState "default", label: '${currentValue} W', icon:"st.Appliances.appliances17", backgroundColor:"#0a9928"
    		}
  		}
        
        valueTile("consumptionPrice", "device.consumptionPrice", decoration: "flat", width: 3, height: 2) {
			state "default", label: 'Curr. Cost:\n${currentValue}/h'
		}
        valueTile("unitPrice", "device.unitPrice", decoration: "flat", width: 3, height: 2) {
			state "default", label: 'Unit Price:\n${currentValue}'
		}
        
        valueTile("totalDemand", "device.averageDailyTotalPower", decoration: "flat", width: 3, height: 1) {
			state "default", label: 'Total Power:\n${currentValue} kWh'
		}
        valueTile("totalConsumptionPrice", "device.currentDailyTotalPowerCost", decoration: "flat", width: 3, height: 1) {
			state "default", label: 'Total Cost:\n${currentValue}'
		}
        
        valueTile("yesterdayTotalPower", "device.yesterdayTotalPower", decoration: "flat", width: 3, height: 1) {
			state "default", label: 'Yesterday Total Power :\n${currentValue} kWh'
		}
        valueTile("yesterdayTotalPowerCost", "device.yesterdayTotalPowerCost", decoration: "flat", width: 3, height: 1) {
			state "default", label: 'Yesterday Total Cost:\n${currentValue}'
		}
        
        standardTile("network", "device.network", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
			state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state ("Connected", label:'Online', icon: "st.Health & Wellness.health9", backgroundColor: "#79b821")
			state ("Not Connected", label:'Offline', icon: "st.Health & Wellness.health9", backgroundColor: "#bc2323")
		}
        
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        htmlTile(name:"chartHTML", action: "getChartHTML", width: 6, height: 4, whiteList: ["www.gstatic.com"])
        
		main (["power"])
		details(["power", "consumptionPrice", "unitPrice", "totalDemand", "totalConsumptionPrice", "yesterdayTotalPower", "yesterdayTotalPowerCost", "chartHTML", "network", "refresh"])
	}
}

mappings {
	path("/getChartHTML") {action: [GET: "getChartHTML"]}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'power' attribute

}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	refreshLiveData()
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()    
}

def refreshLiveData() {
	//Get current hour
    //data.hour = null
    def df = new java.text.SimpleDateFormat("HH")
    if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}
	else {
		df.setTimeZone(TimeZone.getTimeZone("Europe/London"))
	}
	def currentHour = df.format(new Date()).toInteger()

	def resp = parent.apiGET("https://live.ovoenergy.com/api/live/meters/${device.deviceNetworkId}/consumptions/instant")
	if (resp.status != 200) {
    	log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
        sendEvent(name: 'power', value: "N/A", unit: "W")
        if (state.offlineMode) {
        	//Refresh historical power chart at midnight in offline mode
        	if ((state.hour == null) || (state.hour != currentHour)) {
        	//Reset at midnight or initial call
        		if ((state.hour == null) || (currentHour == 0)) { 
            		//Recalcualte historical data at midnight in offline mode
            		addHistoricalPowerToChartData()
                	setYesterdayPowerValues()
            	}
            	state.hour = currentHour
        	}
        } else {
        	if (!state.offlineScheduled) {
        		runIn(60*60, setOffline)
                state.offlineScheduled = true
            }
        }
		return []
	}
    unschedule("setOffline")
    state.offlineScheduled = false
    if (state.offlineMode) {
        //Offline mode is set when API is unavailable
        state.offlineMode = false
        //Recovery mode is set when API has been offline as is turned off at midnight when a full power data set can be collected.
        state.recoveryMode = true
        sendEvent(name: 'costAlertLevelPassed', value: "online", displayed: false)
        sendEvent(name: 'currentDailyTotalPowerCost', value: "RECOVERING", displayed: false)
        //Recalcualte historical data
        addHistoricalPowerToChartData()
        setYesterdayPowerValues()
    }
    sendEvent(name: 'network', value: "Connected" as String)
    data.meterlive = resp.data
        
        //update unit price from OVO Live API
        def unitPriceBigDecimal = data.meterlive.consumption.unitPrice.amount as BigDecimal
        def unitPrice = (Math.round((unitPriceBigDecimal) * 100))/100
        def unitPriceCurrency = data.meterlive.consumption.unitPrice.currency
        unitPrice = String.format("%1.2f",unitPrice)
        
        //update unit price and standing charge from more up to date OVO Account API if available
        def latestUnitPrice = ((device.name.contains('Gas')) ? parent.getUnitPrice('GAS') : parent.getUnitPrice('ELECTRICITY')) as BigDecimal
        if (latestUnitPrice > 0) {
        	unitPriceBigDecimal = latestUnitPrice
        	unitPrice = String.format("%1.5f", unitPriceBigDecimal)
        }
        
        // get electricity readings
        def demand = ((int)Math.round((data.meterlive.consumption.demand as BigDecimal) * 1000))
        def consumptionPrice = (Math.round(((unitPriceBigDecimal as BigDecimal) * (data.meterlive.consumption.demand as BigDecimal)) * 100))/100
        def consumptionPriceCurrency = data.meterlive.consumption.consumptionPrice.currency
        
        //demand = String.format("%4f",demand)
        consumptionPrice = String.format("%1.2f",consumptionPrice)
        
        def standingCharge = ((device.name.contains('Gas')) ? parent.getStandingCharge('GAS') : parent.getStandingCharge('ELECTRICITY')) as BigDecimal
        log.debug "unitPrice: ${unitPriceBigDecimal} standingCharge: ${standingCharge}"
        // set local variables  
        
        sendEvent(name: 'power', value: "$demand", unit: "W")
        sendEvent(name: 'consumptionPrice', value: "£$consumptionPrice", displayed: false)
        sendEvent(name: 'unitPrice', value: "£$unitPrice", displayed: false)
        
        if (state.dailyPowerHistory == null)
        {
        	state.dailyPowerHistory = [:]
        }
        if (state.yesterdayPowerHistory == null)
        {
        	state.yesterdayPowerHistory = [:]
        }
        
        if ((state.hour == null) || (state.hour != currentHour)) {
        	//Update latest standard charges and unit prices
            parent.updateLatestPrices()
            //Add historical figures to chart data object
            addHistoricalPowerToChartData()
            setYesterdayPowerValues()
            
        	//Reset at midnight or initial call
        	if ((state.hour == null) || (currentHour == 0)) { 
                if (!state.recoveryMode) {
                	//Reset power history
                	state.yesterdayPowerHistory =  state.dailyPowerHistory
                }
                state.recoveryMode = false
                state.dailyPowerHistory = [:]
            }       	
        	state.hour = currentHour
            state.currentHourPowerTotal = 0
            state.currentHourPowerEntryNumber = 1
        }
        else {
        	if (!state.recoveryMode) {
       			state.currentHourPowerEntryNumber = state.currentHourPowerEntryNumber + 1    
            }
        }
        if (!state.recoveryMode) {       
        	state.currentHourPowerTotal = state.currentHourPowerTotal + (data.meterlive.consumption.demand as BigDecimal)
        	state.dailyPowerHistory["Hour $state.hour"] = ((state.currentHourPowerTotal as BigDecimal) / state.currentHourPowerEntryNumber)
        
        	def totalDailyPower = getTotalDailyPower()
        	def hourCount = 0
        
        	def formattedAverageTotalPower = (Math.round((totalDailyPower as BigDecimal) * 1000))/1000
        	def formattedCurrentTotalPowerCost = (Math.round((((totalDailyPower as BigDecimal) * unitPriceBigDecimal) + standingCharge) * 100))/100
        	//Add figures to chart data object
        	addCurrentTotalToChartData(formattedCurrentTotalPowerCost)
        	def costDailyComparison = calculatePercentChange(((totalDailyPower as BigDecimal) * unitPriceBigDecimal) + standingCharge, ((getYesterdayPower(state.hour) as BigDecimal) * unitPriceBigDecimal) + standingCharge)
        	def formattedCostDailyComparison = costDailyComparison
        	if (costDailyComparison >= 0) {
        		formattedCostDailyComparison = "+" + formattedCostDailyComparison
        	}
        
        	//Send event to raise notification on high cost
        	if (formattedCurrentTotalPowerCost > (getCostAlertLevelValue() as BigDecimal)) {
        		sendEvent(name: 'costAlertLevelPassed', value: "£${getCostAlertLevelValue()}")
        	} else {
        		sendEvent(name: 'costAlertLevelPassed', value: "false")
        	}
        
        	formattedAverageTotalPower = String.format("%1.2f",formattedAverageTotalPower)
        	formattedCurrentTotalPowerCost = String.format("%1.2f",formattedCurrentTotalPowerCost)
        	formattedCurrentTotalPowerCost += " (" + formattedCostDailyComparison + "%)"
        
        	sendEvent(name: 'averageDailyTotalPower', value: "$formattedAverageTotalPower", unit: "KWh", displayed: false)
        	sendEvent(name: 'currentDailyTotalPowerCost', value: "£$formattedCurrentTotalPowerCost", displayed: false)
       
        	log.debug "currentHour: $currentHour, state.hour: $state.hour, state.currentHourPowerTotal: $state.currentHourPowerTotal, state.currentHourPowerEntryNumber: $state.currentHourPowerEntryNumber, state.dailyPowerHistory: $state.dailyPowerHistory"
        	log.debug "formattedAverageTotalPower: $formattedAverageTotalPower, formattedCurrentTotalPowerCost: $formattedCurrentTotalPowerCost"
       	}
}

def setOffline() {
	state.offlineScheduled = false
	//Refresh historical power chart when first entering offline mode
    if (!state.offlineMode) {
    	addHistoricalPowerToChartData()
        setYesterdayPowerValues()
    }
	state.offlineMode = true
    sendEvent(name: 'averageDailyTotalPower', value: "N/A", unit: "KWh", displayed: false)
    sendEvent(name: 'currentDailyTotalPowerCost', value: "OFFLINE", displayed: false)
    sendEvent(name: 'costAlertLevelPassed', value: "offline", displayed: false)
    sendEvent(name: 'network', value: "Not Connected" as String)
}

private def getTotalDailyPower() {
	def totalDailyPower = 0
	state.dailyPowerHistory.each { hour, averagePower ->
    	totalDailyPower += averagePower
	};
    return totalDailyPower
}

private def getYesterdayPower(currentHour) {
	def totalDailyPower = 0
    for (int i=0; i<=currentHour.toInteger(); i++) {
    	if (state.yesterdayPowerHistory["Hour $i"] != null) totalDailyPower += state.yesterdayPowerHistory["Hour $i"]
    }    
    return totalDailyPower
}

private def calculatePercentChange(current, previous) {
	def delta = current - previous
    if (previous != 0) {
    	return  Math.round((delta / previous) * 100)
    } else {
    	if (delta > 0) return 1000
        else if (delta == 0) return 0
        else return -1000
    }    
}

def getCostAlertLevelValue() {
	if (settings.costAlertLevel == null) {
    	return "10"
    } 
    return settings.costAlertLevel
}

def getAggregatePower(fromDate, toDate) {
	return parent.apiGET("https://live.ovoenergy.com/api/live/meters/${device.deviceNetworkId}/consumptions/aggregated?from=${fromDate.format("yyyy-MM-dd")}T00%3A00%3A00.000Z&to=${toDate.format("yyyy-MM-dd")}T00%3A00%3A00.000Z&granularity=DAY")
}

def setYesterdayPowerValues() {
	//Store the day's power info as yesterdays
    def date = new Date()
    def resp = getAggregatePower((date - 2), date)
    if (resp.status != 200) {
    	log.error("Unexpected result in setYesterdayPowerValues(): [${resp.status}] ${resp.data}")
	} else {
    	def consumptions = resp.data.consumptions
        if (consumptions[1].dataError != "NotFound") {
    		//consumptions[1].price, consumptions[0].price
        	def yesterdayTotalPower = (Math.round((consumptions[1].consumption as BigDecimal) * 1000))/1000
        	sendEvent(name: 'yesterdayTotalPower', value: "$yesterdayTotalPower", unit: "KWh", displayed: false)
        
        	def yesterdayTotalPowerCost = (Math.round((consumptions[1].price as BigDecimal) * 100))/100
        
        	def formattedCostYesterdayComparison = 0
        	//Calculate cost difference between days
        	def costYesterdayComparison = calculatePercentChange(consumptions[1].price as BigDecimal, consumptions[0].price as BigDecimal)
        	formattedCostYesterdayComparison = costYesterdayComparison
        	if (costYesterdayComparison >= 0) {
        		formattedCostYesterdayComparison = "+" + formattedCostYesterdayComparison
        	}
                    
        	yesterdayTotalPowerCost = String.format("%1.2f",yesterdayTotalPowerCost)
        	sendEvent(name: 'yesterdayTotalPowerCost', value: "£$yesterdayTotalPowerCost (" + formattedCostYesterdayComparison + "%)", displayed: false)
    	}
    }
}

def addHistoricalPowerToChartData() {
    def date = new Date()
	def resp = getAggregatePower((date - 6), date)
    if (resp.status != 200) {
    	log.error("Unexpected result in addHistoricalPowerToChartData(): [${resp.status}] ${resp.data}")
	}
    else {
    	def consumptions = resp.data.consumptions
        if (consumptions[5].dataError != "NotFound") {
    		state.chartData = [0, consumptions[5].price, consumptions[4].price, consumptions[3].price, consumptions[2].price, consumptions[1].price, consumptions[0].price]
    	}
    }
}

def addCurrentTotalToChartData(total) {
	if (state.chartData == null) {
    	state.chartData = [total, 0, 0, 0, 0, 0, 0]
    }
    state.chartData.putAt(0, total)
}

def getChartHTML() {
	try {
    	def date = new Date()
		if (state.chartData == null) {
    		state.chartData = [0, 0, 0, 0, 0, 0, 0]
    	}
		def hData = """
			<script type="text/javascript">
				  	google.charts.load('current', {packages: ['corechart', 'bar']});
					google.charts.setOnLoadCallback(drawBasic);

					function drawBasic() {
						var data = google.visualization.arrayToDataTable([
         						['Date', 'Cost', { role: 'style' }],
         						['${(date - 6).format("d MMM")}', ${state.chartData.getAt(6)}, '#0a9928'],   
         						['${(date - 5).format("d MMM")}', ${state.chartData.getAt(5)}, '#0a9928'],   
         						['${(date - 4).format("d MMM")}', ${state.chartData.getAt(4)}, '#0a9928'],            
         						['${(date - 3).format("d MMM")}', ${state.chartData.getAt(3)}, '#0a9928'],            
         						['${(date - 2).format("d MMM")}', ${state.chartData.getAt(2)}, '#0a9928'],
		 						['${(date - 1).format("d MMM")}', ${state.chartData.getAt(1)}, '#0a9928' ], 
         						['Today', ${state.chartData.getAt(0)}, '#eda610' ], 
      					]);

      					var options = {
        						title: "Total Cost in the Last 7 Days",
        						width: 410,
        						height: 220,
       					 		bar: {groupWidth: "75%"},
        						legend: { position: "none" },
        						vAxis: {
          							title: 'Cost (£)',
          							format: '0.00'
        						}
      					};

      					var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));
      					chart.draw(data, options);
    				}
					
			</script>
			  
			"""

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