/**
 *  OVO Energy Meter
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
 *
 * 	1. Create a new device type 'from code' and paste this code in.(https://graph.api.smartthings.com/ide/devices)
 *
 * 	2. Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: OVO Energy Meter (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * 	3. Update device preferences
 *     Click on the new device to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your your OVO user name, OVO password.
 *	   You need to fill in your meter's MPAN number (Meter Point Administration Number for electricity meter) or MPRN number (Meter Point Reference Number for gas meter).
 *	   MPAN and MPRN can be found in your OVO statements within the App under the sections 'Electricity Used' and 'Gas Used'
 *
 *	4. Save preferences and go into the SmartThings app and click refresh. Use an SmartApp like Pollster to refresh status every 1 minute.
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
 *  26.11.2015
 *	v1.0 - Initial Release
 *	v1.0.1 - Tile layout update. Ability to change icon!!!
 *	v1.0.2 - SmartTiles compatability as Power Meter
 *	v1.1 - Calculates cumulative daily costs without OVO API
 *
 *	05.01.2016
 *	v1.1.1 - Remove requirement for Pollster app.
 */
preferences {
	input("username", "text", title: "Username", description: "Your OVO username (usually an email address)")
	input("password", "password", title: "Password", description: "Your OVO password")
    input("meterId", "text", title: "MPAN / MPRN Number", description: "The MPAN (Electricity) or MPRN (Gas) number of the meter found on your OVO correspondence")
} 

metadata {
	definition (name: "OVO Energy Meter", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Polling"
		capability "Power Meter"
		capability "Refresh"
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
        
        valueTile("totalDemand", "device.averageDailyTotalPower", decoration: "flat", width: 3, height: 2) {
			state "default", label: 'Total Power:\n${currentValue} kWh'
		}
        valueTile("totalConsumptionPrice", "device.currentDailyTotalPowerCost", decoration: "flat", width: 3, height: 2) {
			state "default", label: 'Total Price:\n${currentValue}'
		}
        
        valueTile("yesterdayTotalPower", "device.yesterdayTotalPower", decoration: "flat", width: 3, height: 1) {
			state "default", label: 'Prev. Daily Power :\n${currentValue} kWh'
		}
        valueTile("yesterdayTotalPowerCost", "device.yesterdayTotalPowerCost", decoration: "flat", width: 3, height: 1) {
			state "default", label: 'Prev. Daily Cost:\n${currentValue}'
		}
        
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main (["power"])
		details(["power", "consumptionPrice", "unitPrice", "totalDemand", "totalConsumptionPrice", "yesterdayTotalPower", "yesterdayTotalPowerCost", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'power' attribute

}

def installed() {
	log.debug "Executing 'installed'"
	// execute handlerMethod every 10 minutes.
    schedule("0 0/1 * * * ?", poll)
}

def updated() {
	log.debug "Executing 'updated'"
	// execute handlerMethod every 10 minutes.
    unschedule(poll)
    schedule("0 0/1 * * * ?", poll)
}

def uninstalled() {
	log.debug "Executing 'unsinstalled'"
	unschedule(poll)
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
	refreshLiveData()
    
    //OVO historical data API takes too long and SmartThings complains. Commented out for now.
    //refreshHistoricalData()
}

def refreshLiveData() {
	api('live', []) {
    	data.meterlive = it.data
        
        // get electricity readings
        def demand = ((int)Math.round((data.meterlive.consumption.demand as BigDecimal) * 1000))
        def consumptionPrice = (Math.round((data.meterlive.consumption.consumptionPrice.amount as BigDecimal) * 100))/100
        def consumptionPriceCurrency = data.meterlive.consumption.consumptionPrice.currency
        def unitPrice = (Math.round((data.meterlive.consumption.unitPrice.amount as BigDecimal) * 100))/100
        def unitPriceCurrency = data.meterlive.consumption.unitPrice.currency
        
        //demand = String.format("%4f",demand)
        consumptionPrice = String.format("%1.2f",consumptionPrice)
        unitPrice = String.format("%1.2f",unitPrice)
        
        // set local variables       
        sendEvent(name: 'power', value: "$demand", unit: "W")
        sendEvent(name: 'consumptionPrice', value: "£$consumptionPrice", displayed: false)
        sendEvent(name: 'unitPrice', value: "£$unitPrice", displayed: false)
        
        //Calculate power costs manually without need for terrible OVO API.
        if (data.dailyPowerHistory == null)
        {
        	data.dailyPowerHistory = [:]
        }
        //Get current hour
        
        //data.hour = null
        def currentHour = new Date().getAt(Calendar.HOUR_OF_DAY)
        if ((data.hour == null) || (data.hour != currentHour)) {
        	//Reset at midnight or initial call
        	if ((data.hour == null) || (currentHour == 0)) {  
            	//Store the day's power info as yesterdays
            	def totalPower = getTotalDailyPower()
            	data.yesterdayTotalPower = (Math.round((totalPower as BigDecimal) * 1000))/1000
                data.yesterdayTotalPowerCost = (Math.round(((totalPower as BigDecimal) * (data.meterlive.consumption.unitPrice.amount as BigDecimal)) * 100))/100
            	sendEvent(name: 'yesterdayTotalPower', value: "$data.yesterdayTotalPower", unit: "KWh", displayed: false)
        		sendEvent(name: 'yesterdayTotalPowerCost', value: "£$data.yesterdayTotalPowerCost", displayed: false)
                
                //Reset power history
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
        def formattedCurrentTotalPowerCost = (Math.round(((totalDailyPower as BigDecimal) * (data.meterlive.consumption.unitPrice.amount as BigDecimal)) * 100))/100
        
        formattedAverageTotalPower = String.format("%1.2f",formattedAverageTotalPower)
        formattedCurrentTotalPowerCost = String.format("%1.2f",formattedCurrentTotalPowerCost)
        
        sendEvent(name: 'averageDailyTotalPower', value: "$formattedAverageTotalPower", unit: "KWh", displayed: false)
        sendEvent(name: 'currentDailyTotalPowerCost', value: "£$formattedCurrentTotalPowerCost", displayed: false)
        
        log.debug "currentHour: $currentHour, data.hour: $data.hour, data.currentHourPowerTotal: $data.currentHourPowerTotal, data.currentHourPowerEntryNumber: $data.currentHourPowerEntryNumber, data.dailyPowerHistory: $data.dailyPowerHistory"
        log.debug "formattedAverageTotalPower: $formattedAverageTotalPower, formattedCurrentTotalPowerCost: $formattedCurrentTotalPowerCost"
        
    }

}

private def getTotalDailyPower() {
	def totalDailyPower = 0
	data.dailyPowerHistory.each { hour, averagePower ->
    	totalDailyPower = totalDailyPower + averagePower
	};
    return totalDailyPower
}

def refreshHistoricalData() {
	api('historical', []) {
    	data.meterhistorical = it.data
        log.debug = "$it.data"
        
        //{"totalConsumption":{"demand":"14.05009999999993","timestamp":1448496000000,"consumptionPrice":{"amount":"1.6538372709999916","currency":"GBP"},"unitPrice":{"amount":"0.11771","currency":"GBP"}}}
        // get historical electricity readings
        def totalDemand = (Math.round((data.meterhistorical.totalConsumption.demand as BigDecimal) * 1000))/1000
        def totalConsumptionPrice = (Math.round((data.meterhistorical.totalConsumption.consumptionPrice.amount as BigDecimal) * 100))/100
        def totalCconsumptionPriceCurrency = data.meterhistorical.totalConsumption.consumptionPrice.currency  
        
        totalDemand = String.format("%1.2f",totalDemand)
        totalConsumptionPrice = String.format("%1.2f",totalConsumptionPrice)
        
        // set local variables       
        sendEvent(name: 'totalPower', value: "$totalDemand KW", displayed: false)
        sendEvent(name: 'totalConsumptionPrice', value: "£$totalConsumptionPrice", displayed: false)
    }
}


def api(method, args = [], success = {}) {
	log.debug "Executing 'api'"
	
	if(!isLoggedIn()) {
		log.debug "Need to login"
		login(method, args, success)
		return
	}
    
	def methods = [
		'live': [uri: "https://live.ovoenergy.com/api/live/meters/${settings.meterId}/consumptions/instant", type: 'get'],
		'historical': [uri: "https://live.ovoenergy.com/api/live/meters/${settings.meterId}/consumptions/historical?startTimestamp=${(int)(new Date().getTime())/1000}&period=DAY&dataSource=CAD", type: 'get'],
    ]
	
	def request = methods.getAt(method)
	
	log.debug "Starting $method : $args"
	doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
	log.debug "Calling doRequest()"
	log.debug "Calling $type : $uri : $args"
	
	def params = [
		uri: uri,
        contentType: 'application/json',
		headers: [
              'Accept': 'application/json, text/plain, */*',
              'Content-Type': 'application/json;charset=UTF-8',
              'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36',
              'Origin': 'https://my.ovoenergy.com',
              'Authorization': "${data.auth.token}"
        ],
        body: args
	]
	
	log.debug params
	
	def postRequest = { response ->
		success.call(response)
	}

	try {
		if (type == 'post') {
			httpPostJson(params, postRequest)
        } else if (type == 'put') {
        	httpPutJson(params, postRequest)
		} else if (type == 'get') {
			httpGet(params, postRequest)
		}
    } catch(SocketTimeoutException e) {
    	log.debug("OVO data API is too slow for SmartThings!")
    }
	
}

def login(method = null, args = [], success = {}) {
	log.debug "Calling login()"
	def params = [
		uri: 'https://my.ovoenergy.com/api/auth/login',
        contentType: 'application/json;charset=UTF-8',
        headers: [
              'Accept': 'application/json, text/plain, */*',
              'Content-Type': 'application/json;charset=UTF-8',
              'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36',
              'Origin': 'https://my.ovoenergy.com'
        ],
        body: [
        		username: settings.username,
                password: settings.password,           
        ]
    ]

	state.cookie = ''
	
	httpPostJson(params) {response ->
		log.debug "Request was successful, $response.status"
		log.debug response.headers
		data.auth = response.data
		
		// set the expiration to 15 minutes
		data.auth.expires_at = new Date().getTime() + 600000;
        //data.auth.expires_at = new Date().getTime() + 10000;
        
        state.cookie = response?.headers?.'Set-Cookie'?.split(";")?.getAt(0)
		log.debug "Adding cookie to collection: $cookie"
        log.debug "auth: $data.auth"
		log.debug "cookie: $state.cookie"
		
		api(method, args, success)

	}
}

def isLoggedIn() {
	log.debug "Calling isLoggedIn()"
	log.debug "isLoggedIn state $data.auth"
	if(!data.auth) {
		log.debug "No data.auth"
		return false
	}

	def now = new Date().getTime();
    return data.auth.expires_at > now
}

