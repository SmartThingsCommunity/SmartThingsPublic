/**
 *  Enlighten Solar System
 *
 *  Copyright 2015 Ronald Gouldner based on original version by Umesh Sirsiwal
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
 
preferences {
    input("user_id", "text", title: "Enphase Dev Account User ID")
    input("system_id", "text", title: "Enphase System ID")
    input("key", "text", title: "Enphase Dev Account Key")
    
}
metadata {
	definition (name: "Enlighten Solar System", namespace: "gouldner", author: "Mj Gouldner") {
	capability "Actuator" /* Added for Rule Manager */
    capability "Power Meter" 
    capability "Refresh"
	capability "Polling"
        
    attribute "energy_today", "STRING"
    attribute "energy_life", "STRING"
	attribute "production_level", "STRING"
	attribute "today_max_prod", "NUMBER"
	attribute "today_max_prod_str", "STRING"
	attribute "today_max_day", "STRING"
	attribute "reported_id", "STRING"
        
    fingerprint deviceId: "RRGEnlightenPV"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
            valueTile("reported_id", "device.reported_id") {
				state ("reported_id", label: '${currentValue}\nPowered By Enphase', unit:"", backgroundColor: "#0000ff"
					   //icon:"https://github.com/gouldner/smartthings-enlighten/raw/master/PoweredByLogo.jpg"
                       )
            }
            valueTile("energy_today", "device.energy_today") {
   	         state("energy_today", label: '${currentValue}K\nToday', unit:"KWh", backgroundColors: [
                    [value: 2, color: "#bc2323"],
                    [value: 5, color: "#d04e00"],
                    [value: 10, color: "#f1d801"],
                    [value: 20, color: "#90d2a7"],
		            [value: 30, color: "#44b621"],
                    [value: 40, color: "#1e9cbb"],
                    [value: 50, color: "#153591"]
    	            ]
            	)
        	}
            valueTile("power", "device.power") {
   	         state("Power", label: '${currentValue}W\nPower', unit:"W", backgroundColor: "#000000")
        	}
			valueTile("productionLevel", "device.production_level") {
				state("productionLevel", label: '${currentValue}%\nProduction', unit:"%", backgroundColor: "#0000FF")
			}
			valueTile("todayMaxProd", "device.today_max_prod_str") {
				state("todayMaxProd", label: '${currentValue}%\nMax', unit:"%", backgroundColor: "#0000FF")
			}
            valueTile("energy_life", "device.energy_life", width: 1, height: 1, canChangeIcon: true) {
   	         state("energy_life", label: '${currentValue}M\nLife', unit:"MWh", backgroundColors: [
                    [value: 2, color: "#bc2323"],
                    [value: 5, color: "#d04e00"],
                    [value: 10, color: "#f1d801"],
                    [value: 20, color: "#90d2a7"],
		            [value: 30, color: "#44b621"],
                    [value: 40, color: "#1e9cbb"],
                    [value: 50, color: "#153591"],
    	            ]
            	)
        	}    

            standardTile("refresh", "device.energy_today", inactiveLabel: false, decoration: "flat") {
                state "default", action:"polling.poll", icon:"st.secondary.refresh"
            }

        
        main (["power","energy_today"])
        details(["power","energy_today", "energy_life", "productionLevel", "todayMaxProd", "refresh","reported_id"])

	}
}


// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

def poll() {
	refresh()
}

def refresh() {
  log.debug "Executing 'refresh'"
  energyRefresh()
}


def energyRefresh() {  
  log.debug "Executing 'energyToday'"
  
  def cmd = "https://api.enphaseenergy.com/api/v2/systems/${settings.system_id}/summary?key=${settings.key}&user_id=${settings.user_id}";
  log.debug "Sending request cmd[${cmd}]"
  
  httpGet(cmd) {resp ->
        if (resp.data) {
        	log.debug "${resp.data}"
            def energyToday = resp.data.energy_today/1000
            def energyLife = resp.data.energy_lifetime/1000000
            def currentPower = resp.data.current_power
			def systemSize = resp.data.size_w
			def productionLevel = currentPower/systemSize * 100
			def systemId = resp.data.system_id
			def now=new Date()
			def tz = location.timeZone
			def todayDay = now.format("dd",tz)
			def today_max_day = device.currentValue("today_max_day")
            def today_max_prod = device.currentValue("today_max_prod")
			def todayMaxProd=today_max_prod
            log.debug "todayMaxProd was ${todayMaxProd}"
			
			// if today max prod less than production level then set to production level
			if (todayMaxProd < productionLevel) {
				todayMaxProd = productionLevel
			}
            
			log.debug "System Id ${system_id}"
            log.debug "Energy today ${energyToday}"
            log.debug "Energy life ${energyLife}"
            log.debug "Current Power Level ${currentPower}"
			log.debug "System Size ${systemSize}"
			log.debug "Production Level ${currentPower}"
			log.debug "todayMaxProd is now ${todayMaxProd}"
			log.debug "today_max_day ${today_max_day}"
			log.debug "todayDay ${todayDay}"
			
			// If day has changed set today_max_day to new value
			if (today_max_day == null || today_max_day != todayDay) {
				log.debug "Setting today_max_day=${todayDay}"
				sendEvent(name: 'today_max_day', value: (todayDay))
				// New day reset todayMaxProd
				todayMaxProd = productionLevel
			}
            
            // String.format("%5.2f", energyToday)
            delayBetween([sendEvent(name: 'energy_today', value: (String.format("%5.2f", energyToday)))
                          ,sendEvent(name: 'energy_life', value: (String.format("%5.2f",energyLife)))
                          ,sendEvent(name: 'power', value: (currentPower))
						  ,sendEvent(name: 'production_level', value: (String.format("%5.2f",productionLevel)))
						  ,sendEvent(name: 'today_max_prod', value: (todayMaxProd))
						  ,sendEvent(name: 'today_max_prod_str', value: (String.format("%5.2f",todayMaxProd)))
						  ,sendEvent(name: 'reported_id', value: (systemId))
	                     ])
			
			
        }
        if(resp.status == 200) {
            	log.debug "poll results returned"
        }
         else {
            log.error "polling children & got http status ${resp.status}"
        }
    }
}