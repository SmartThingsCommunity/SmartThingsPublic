/**
 *  Enlighten Solar System
 *
 *  Copyright 2015 Umesh Sirsiwal with contribution from Ronald Gouldner
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
         definition (name: "Enlighten Solar System 1", namespace: "usirsiwal", author: "Umesh Sirsiwal", ocfDeviceType: "x.com.st.d.energymeter") {
                    capability "Power Meter"
                    capability "Refresh"
                    capability "Energy Meter"
                    capability "Polling"
                    

                    attribute "energy_today", "STRING"
                    attribute "energy_life", "STRING"
                    
                    }

         simulator {
                      // TODO: define status and reply messages here
                   }

         tiles {
                   valueTile("energy", "device.energy", width: 1, height: 1, canChangeIcon: true) {
                         state("energy_today", label: '${currentValue}KWh', unit:"KWh", 
                          //icon: "https://raw.githubusercontent.com/usirsiwal/smartthings-enlighten/master/enphase.jpg",
                          backgroundColors: [
                           [value: 2, color: "#bc2323"],
                           [value: 5, color: "#d04e00"],
                           [value: 10, color: "#f1d801"],
                           [value: 20, color: "#90d2a7"],
                           [value: 30, color: "#44b621"],
                           [value: 40, color: "#1e9cbb"],
                           [value: 50, color: "#153591"],
                          ],
                       )
                   }
                   valueTile("energy_life", "device.energy_life", width: 1, height: 1, canChangeIcon: true) {
                         state("energy_life", label: '${currentValue}MWh', unit:"MWh", backgroundColors: [
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
                   valueTile("power", "device.power", width: 1, height: 1) {
                     state("power", label: '${currentValue}W', unit:"W", 
                     //icon: "https://raw.githubusercontent.com/usirsiwal/smartthings-enlighten/master/enphase.jpg",
                       backgroundColors: [
                       [value: 600, color: "#bc2323"],
                       [value: 1200, color: "#d04e00"],
                       [value: 1800, color: "#1e9cbb"],
                       [value: 2900, color: "#153591"]
                      ],
                     )
                   }

                   chartTile(name: "powerChart", attribute: "power")
                   
                   standardTile("refresh", "device.energy", inactiveLabel: false, decoration: "flat") {
                      state "default", action:"polling.poll", icon:"st.secondary.refresh"
                   }

                   main (["power"])
                   details(["power", "energy",  "energy_life", "refresh"])
        }

}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"

}
def installed() {

	log.debug "Installing Solaredge Monitoring..."

    refresh()

}

def updated() {

	log.debug "Executing 'updated'"

    unschedule()

    runEvery15Minutes(refresh)

    runIn(2, refresh)

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

  def cmd = "https://region03eu5.fusionsolar.huawei.com/pvmswebsite/assets/build/index.html#/view/device/NE=35088645/inverter/details";
  log.debug "Sending request cmd[${cmd}]"

 httpGet(cmd) {resp ->
        if (resp.data) {
        	log.debug "${resp.data}"
            def energy = resp.data.result.yieldtoday
            def energyLife = resp.data.result.yieldtotal
            def currentPower = resp.data.result.power
			def systemSize = resp.data.size_w
			def systemId = resp.data.system_id
			def now=new Date()
			def tz = location.timeZone
			def todayDay = now.format("dd",tz)
			def today_max_day = device.currentValue("today_max_day")
            def today_max_prod = device.currentValue("today_max_prod")
			def todayMaxProd=today_max_prod
            log.debug "todayMaxProd was ${todayMaxProd}"
			
            
	    log.debug "System Id ${system_id}"
            log.debug "Energy today ${energy}"
            log.debug "Energy life ${energyLife}"
            log.debug "Current Power Level ${curPower}"
	    log.debug "System Size ${systemSize}"
	    log.debug "Production Level ${power}"
	    log.debug "todayDay ${todayDay}"
			
			// If day has changed set today_max_day to new value
			if (today_max_day == null || today_max_day != todayDay) {
				log.debug "Setting today_max_day=${todayDay}"
				sendEvent(name: 'today_max_day', value: (todayDay))
				// New day reset todayMaxProd
				todayMaxProd = productionLevel
			}
            
            // String.format("%5.2f", energyToday)
            delayBetween([sendEvent(name: 'energy', value: (energy))
                          ,sendEvent(name: 'energy_life', value: (energyLife))
                          ,sendEvent(name: 'power', value: (curPower))
						  ,sendEvent(name: 'production_level', value: (String.format("%5.2f",productionLevel)))
						  ,sendEvent(name: 'today_max_prod', value: (todayMaxProd))
						  ,sendEvent(name: 'today_max_prod_str', value: (String.format("%5.2f",todayMaxProd)))
						  ,sendEvent(name: 'reported_id', value: (systemId))
	                     ])
                         
			
			
        
}
			
           
    }
}

def getVisualizationData(attribute) {	
	log.debug "getChartData for $attribute"
	def keyBase = "measure.${attribute}"
    log.debug "getChartData state = $state"
	
	def dateBuckets = state[keyBase]
	
	//convert to the right format
	def results = dateBuckets?.sort{it.key}.collect {[
		date: Date.parse("yyyy-MM-dd", it.key),
		average: it.value.average,
		min: it.value.min,
		max: it.value.max
		]}
	
	log.debug "getChartData results = $results"
	results
}

private getKeyFromDate(date = new Date()){
	date.format("yyyy-MM-dd")
}

private storeData(attribute, value) {
	log.debug "storeData initial state: $state"
	def keyBase = "measure.${attribute} ${value}"
	
	// create bucket if it doesn't exist
	if(!state[keyBase]) {
		state[keyBase] = [:]
		log.debug "storeData - attribute not found. New state: $state"
	}

	log.debug "storeData after min/max calculations. New state: $state"
}
