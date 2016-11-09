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
 *	v2.3 - Added historical power chart for the last 5 days.
 *	v2.3.1 - Fix chart Android compatibility.
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
        
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        htmlTile(name:"chartHTML", action: "getChartHTML", width: 6, height: 4, whiteList: ["www.gstatic.com"])
        
		main (["power"])
		details(["power", "consumptionPrice", "unitPrice", "totalDemand", "totalConsumptionPrice", "yesterdayTotalPower", "yesterdayTotalPowerCost", "chartHTML", "refresh"])
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

	def resp = parent.apiGET("https://live.ovoenergy.com/api/live/meters/${device.deviceNetworkId}/consumptions/instant")
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
    	data.meterlive = resp.data
        
        //update unit price from OVO Live API
        def unitPriceBigDecimal = data.meterlive.consumption.unitPrice.amount as BigDecimal
        def unitPrice = (Math.round((unitPriceBigDecimal) * 100))/100
        def unitPriceCurrency = data.meterlive.consumption.unitPrice.currency
        unitPrice = String.format("%1.2f",unitPrice)
        
        //update unit price and standing charge from more up to date OVO Account API if available
        parent.updateLatestPrices()
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
        
        //Calculate power costs manually without need for terrible OVO API.
        if (data.dailyPowerHistory == null)
        {
        	data.dailyPowerHistory = [:]
        }
        if (data.yesterdayPowerHistory == null)
        {
        	data.yesterdayPowerHistory = [:]
        }
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
        if ((data.hour == null) || (data.hour != currentHour)) {
        	//Reset at midnight or initial call
        	if ((data.hour == null) || (currentHour == 0)) {  
            	//Store the day's power info as yesterdays
            	def totalPower = getTotalDailyPower()
            	data.yesterdayTotalPower = (Math.round((totalPower as BigDecimal) * 1000))/1000
                def newYesterdayTotalPowerCost = (Math.round((((totalPower as BigDecimal) * unitPriceBigDecimal) + standingCharge) * 100))/100
                //Add figures to chart data object
                addYesterdayTotalToChartData(newYesterdayTotalPowerCost)
                def costYesterdayComparison = calculatePercentChange(newYesterdayTotalPowerCost as BigDecimal, data.yesterdayTotalPowerCost as BigDecimal)
                def formattedCostYesterdayComparison = costYesterdayComparison
                if (costYesterdayComparison >= 0) {
        			formattedCostYesterdayComparison = "+" + formattedCostYesterdayComparison
        		}
                data.yesterdayTotalPowerCost = String.format("%1.2f",newYesterdayTotalPowerCost)
            	sendEvent(name: 'yesterdayTotalPower', value: "$data.yesterdayTotalPower", unit: "KWh", displayed: false)
        		sendEvent(name: 'yesterdayTotalPowerCost', value: "£$data.yesterdayTotalPowerCost (" + formattedCostYesterdayComparison + "%)", displayed: false)
                
                //Reset power history
                data.yesterdayPowerHistory =  data.dailyPowerHistory
                data.dailyPowerHistory = [:]
            }       	
        	data.hour = currentHour
            data.currentHourPowerTotal = 0
            data.currentHourPowerEntryNumber = 1
        }
        else {
       		data.currentHourPowerEntryNumber = data.currentHourPowerEntryNumber + 1      
        }
               
        data.currentHourPowerTotal = data.currentHourPowerTotal + (data.meterlive.consumption.demand as BigDecimal)
        data.dailyPowerHistory["Hour $data.hour"] = ((data.currentHourPowerTotal as BigDecimal) / data.currentHourPowerEntryNumber)
        
        def totalDailyPower = getTotalDailyPower()
        def hourCount = 0
        
        def formattedAverageTotalPower = (Math.round((totalDailyPower as BigDecimal) * 1000))/1000
        def formattedCurrentTotalPowerCost = (Math.round((((totalDailyPower as BigDecimal) * unitPriceBigDecimal) + standingCharge) * 100))/100
        //Add figures to chart data object
        addCurrentTotalToChartData(formattedCurrentTotalPowerCost)
        def costDailyComparison = calculatePercentChange(((totalDailyPower as BigDecimal) * unitPriceBigDecimal) + standingCharge, ((getYesterdayPower(data.hour) as BigDecimal) * unitPriceBigDecimal) + standingCharge)
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
        
        log.debug "currentHour: $currentHour, data.hour: $data.hour, data.currentHourPowerTotal: $data.currentHourPowerTotal, data.currentHourPowerEntryNumber: $data.currentHourPowerEntryNumber, data.dailyPowerHistory: $data.dailyPowerHistory"
        log.debug "formattedAverageTotalPower: $formattedAverageTotalPower, formattedCurrentTotalPowerCost: $formattedCurrentTotalPowerCost"
       
}

private def getTotalDailyPower() {
	def totalDailyPower = 0
	data.dailyPowerHistory.each { hour, averagePower ->
    	totalDailyPower += averagePower
	};
    return totalDailyPower
}

private def getYesterdayPower(currentHour) {
	def totalDailyPower = 0
    for (int i=0; i<=currentHour.toInteger(); i++) {
    	if (data.yesterdayPowerHistory["Hour $i"] != null) totalDailyPower += data.yesterdayPowerHistory["Hour $i"]
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

def addYesterdayTotalToChartData(total) {
	if (data.chartData == null) {
    	data.chartData = [0, total, 0, 0, 0, 0, 0]
    }
    else {
    	data.chartData[0] = total
    	data.chartData.add(0, 0)
        data.chartData.pop()
    }
}

def addCurrentTotalToChartData(total) {
	if (data.chartData == null) {
    	data.chartData = [total, 0, 0, 0, 0, 0, 0]
    }
    data.chartData[0] = total
}

def getChartHTML() {
	try {
    	def date = new Date()
		if (data.chartData == null) {
    		data.chartData = [0, 0, 0, 0, 0, 0, 0]
    	}
		def hData = """
			<script type="text/javascript">
				  	google.charts.load('current', {packages: ['corechart', 'bar']});
					google.charts.setOnLoadCallback(drawBasic);

					function drawBasic() {
						var data = google.visualization.arrayToDataTable([
         						['Date', 'Cost', { role: 'style' }],
         						['${(date - 6).format("d MMM")}', ${data.chartData[6]}, '#0a9928'],   
         						['${(date - 5).format("d MMM")}', ${data.chartData[5]}, '#0a9928'],   
         						['${(date - 4).format("d MMM")}', ${data.chartData[4]}, '#0a9928'],            
         						['${(date - 3).format("d MMM")}', ${data.chartData[3]}, '#0a9928'],            
         						['${(date - 2).format("d MMM")}', ${data.chartData[2]}, '#0a9928'],
		 						['${(date - 1).format("d MMM")}', ${data.chartData[1]}, '#0a9928' ], 
         						['Today', ${data.chartData[0]}, '#eda610' ], 
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