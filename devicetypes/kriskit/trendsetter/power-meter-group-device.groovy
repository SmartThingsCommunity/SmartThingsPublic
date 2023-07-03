/**
 *  Power Meter Group Device
 *
 *  Copyright 2015 Chris Kitch
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
	definition (name: "Power Meter Group Device", namespace: "kriskit.trendsetter", author: "Chris Kitch") {
		capability "Power Meter"
		capability "Sensor"
        capability "Refresh"
        
        attribute "powerUsage", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"main", type: "lighting", width: 6, height: 4) {
			tileAttribute ("device.powerUsage", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', backgroundColor: "#ffffff", icon: "st.Appliances.appliances17"
                attributeState "low", label: '${name}', backgroundColor: "#5CB85C", icon: "st.Appliances.appliances17"
                attributeState "medium", label: '${name}', backgroundColor: "#ff7b00", icon: "st.Appliances.appliances17"
                attributeState "high", label: '${name}', backgroundColor: "#c90000", icon: "st.Appliances.appliances17"
                attributeState "veryHigh", label: '${name}', backgroundColor: "#ff0000", icon: "st.Appliances.appliances17"
			}
        
			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "default", label: '${currentValue} W'
			}
        }
        
        standardTile("refresh", "refresh", height:2, width:6, inactiveLabel: false, decoration: "flat") {
        	state "default", action: "refresh.refresh", icon:"st.secondary.refresh"
        }
        
        main("main")
        details(["main", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
}

def groupSync(name, values) {
	try {
    	"sync${name.capitalize()}"(values)	
    } catch(ex) {
    	log.error "Error executing 'sync${name.capitalize()}' method: $ex"
    }
}

def refresh() {
	def powerValues = parent.getGroupCurrentValues("power")
    syncPower(powerValues)
}

// POWER
def syncPower(values) {   
    log.debug "syncPower(): $values"
	def total = values?.sum()
        
    if (total == 0) {
    	sendEvent(name: "power", value: 0)
        sendEvent(name: "powerUsage", value: "off")
        return
    }
    
	def aggregate = state.powerAggregate ?: []
	state.powerSyncCount = state.powerSyncCount + 1
    
    if (state.powerSyncCount != null && state.powerSyncCount % 5 != 0) {
        aggregate.push(total)
        state.powerAggregate = aggregate
        return
    }

    def aggregatedAverage = getAverage(aggregate)
    log.debug "Aggregated Average Power: $aggregatedAverage"
    sendEvent(name: "power", value: aggregatedAverage)

    def level = getPowerUsageLevel(aggregatedAverage)
    log.debug "Power usage level: $level"
    sendEvent(name: "powerUsage", value: level)
    
    state.powerAggregate = []
}

def getPowerUsageLevel(value) {    
    if (value == 0)
    	return "off"
        
    def boundaries = getPowerUsageBoundaries()
    
    if (!boundaries)    
    	return "low"
        
    log.debug "Determining power usage level with boundaries: $boundaries for value $value"
        
   	if (value > 0 && value <= boundaries.bottom)
    	return "low"
     
    if (value > boundaries.bottom && value < boundaries.top)
    	return "medium"
        
    if (value >= boundaries.top && value <= boundaries.max)
    	return "high"
        
    if (value > boundaries.max) {
    	state.powerUsageBoundaries = null
    	return "veryHigh"
	}
}

def getPowerUsageBoundaries() {
	if (state.powerUsageBoundaries && state.powerSyncCount < 100)
    	return state.powerUsageBoundaries
        
	def events = device.events([max: 500])
    def powerEvents = events?.findAll {
    	it.name == "power" && it.doubleValue > 0
    }
    
    def powerValues = powerEvents*.doubleValue
    powerValues.sort()
    
    def eventCount = powerValues?.size()
    def eventChunkSize = (int)Math.round(eventCount / 2)
    def chunkedEvents = powerValues.collate(eventChunkSize)
   
   if (chunkedEvents.size() < 2)
   		return null

	def boundaries = [
    	top: getAverage(chunkedEvents[1]),
    	bottom: getAverage(chunkedEvents[0]),
        max: powerValues.max()
	]        
    
   	state.powerSyncCount = 0
    state.powerUsageBoundaries = boundaries
    
    log.debug "New boundaries: $boundaries"
    
    return boundaries
}

def getAverage(values) {
	return Math.round((values.sum() / values.size()) * 100) / 100
}
