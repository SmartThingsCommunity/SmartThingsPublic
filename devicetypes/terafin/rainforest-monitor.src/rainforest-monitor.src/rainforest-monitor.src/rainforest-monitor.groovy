/**
 *  Rainforest Monitor
 *
 *  Copyright 2015 Justin Wood
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
	 import groovy.json.JsonSlurper

	preferences {
        input "cloud_id", "text", title: "Cloud ID", required: true
        input "user_name", "text", title: "User Name", required: true
        input "password", "text", title: "Password", required: true
        input "pollingInterval", "decimal", title: "Number of seconds", required: false
	}

	metadata {
	definition (name: "Rainforest Monitor", namespace: "terafin", author: "terafin") {
	    capability "Energy Meter"
        capability "Power Meter"
        capability "Polling"
	}

	simulator { }
	
	tiles {
    	valueTile("energy", "device.power") {
			state "default", label: '${currentValue} kWh',
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 1000, color: "#ffffff"]
            	], 
            	foregroundColor: "#000000",
				backgroundColors:[
					[value: 1, color: "#00cc00"],
                    [value: 1000, color: "#79b821"],
                   	[value: 1800, color: "#ffa81e"],
					[value: 4000, color: "#fb1b42"]
				]
        }
        
        
        valueTile("readingUpdated", "device.readingUpdated", width: 2, height: 1, decoration: "flat") {
			state "default", label:'Reading: ${currentValue} Wh'
	    }
            

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}


        main (["energy"])
        details(["energy","readingUpdated","refresh"])
		}
 	}

	def poll() { getReading() }
	def refresh() { getReading() }

    def installed() { initialize() } 
    def updated() { initialize() } 
    def initialize() { getReading() } 

    private Integer convertHexToInt(hex) {
        Integer.parseInt(hex.split(/x/)[1],16)
    }

	private getReading() {
        def readingClosure = { 
            response -> 
//                log.debug "response from rainforest: $response"
//                log.debug "                    data: $response.data"

                
                def multiplier = convertHexToInt(response.data.InstantaneousDemand.Multiplier)
                def demand = convertHexToInt(response.data.InstantaneousDemand.Demand)
				def divisor = convertHexToInt(response.data.InstantaneousDemand.Divisor)

                def kw_usage = (demand.toFloat() * (multiplier.toFloat() / divisor.toFloat()))
                log.debug "kw_usage: $kw_usage"

                def w_usage = kw_usage * 1000
                log.debug "w_usage: $w_usage"

                sendEvent(name: "energy", value: kw_usage)
                sendEvent(name: "power", value: kw_usage)
                sendEvent(name: "readingUpdated", value: w_usage)

		}
        
        def params = [
                uri: "https://rainforestcloud.com:9445",
                path: "/cgi-bin/post_manager",
                method: "POST",
                headers: [
                	'Cloud-ID': cloud_id,
                	'User': user_name,
                	'Password': password,

                ],
                contentType: 'application/json',
                requestContentType: 'application/json',
				body: "<Command><Name>get_instantaneous_demand</Name><Format>JSON</Format></Command>",
            ]
            
        
		try {
        	httpPostJson(params, readingClosure)
        } catch (e) {
		    log.error "Exception hit loading URL: $e"
		    log.error "    params:" + params
		}
	   	runIn( pollingInterval, "getReading",)
	}