/**
 *  Consumption Metering
 *
 *  Copyright 2016 FortrezZ, LLC
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
definition(
    name: "Consumption Metering",
    namespace: "FortrezZ",
    author: "FortrezZ, LLC",
    description: "Child SmartApp for Consumption Metering rules",
    category: "Green Living",
    parent: "FortrezZ:FortrezZ Water Consumption Metering",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	page(name: "prefsPage", title: "Choose the detector behavior", install: true, uninstall: true)

    // Do something here like update a message on the screen,
    // or introduce more inputs. submitOnChange will refresh
    // the page and allow the user to see the changes immediately.
    // For example, you could prompt for the level of the dimmers
    // if dimmers have been selected:
    //log.debug "Child Settings: ${settings}"
}

def prefsPage() {
	def dailySchedule = 0
	def daysOfTheWeek = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]
    dynamicPage(name: "prefsPage") {
        section("Set Water Usage Goals") {
            input(name: "type", type: "enum", title: "Set a new goal?", submitOnChange: true, options: ruleTypes())
        }
		def measurementType = "water"
        if(type)
        {
            switch (type) {
                case "Daily Goal":
                	section("Water Measurement Preference"){
        				input(name: "measurementType", type: "enum", title: "Press to change water measurement options", submitOnChange: true, options: waterTypes())}
                
                    section("Threshold settings") {
                        input(name: "waterGoal", type: "decimal", title: "Daily ${measurementType} Goal", required: true, defaultValue: 0.5)
                    }

                                      
                    break

                case "Weekly Goal":
                	section("Water Measurement Preference"){
        				input(name: "measurementType", type: "enum", title: "Press to change water measurement options", submitOnChange: true, options: waterTypes())}
                    section("Threshold settings") {
                        input(name: "waterGoal", type: "decimal", title: "Weekly ${measurementType} Goal", required: true, defaultValue: 0.1)
                    }

                    
                    break

                case "Monthly Goal":
                	section("Water Measurement Preference"){
        				input(name: "measurementType", type: "enum", title: "Press to change water measurement options", submitOnChange: true, options: waterTypes())}
                    section("Threshold settings") {
                        input(name: "waterGoal", type: "decimal", title: "Monthly ${measurementType} Goal", required: true, defaultValue: 0.1)
                    }

                    
                    break

                default:
                    break
            }
        }
    }
}

def ruleTypes() {
	def types = []
    types << "Daily Goal"
    types << "Weekly Goal"
    types << "Monthly Goal"
   
    return types
}

def waterTypes()
{
	def watertype = []
    
    watertype << "Gallons"
    watertype << "Cubic Feet"
    watertype << "Liters"
    watertype << "Cubic Meters"
    return watertype
}
/*
def setDailyGoal(measurementType3)
{
    return parent.setDailyGoal(measurementType3)
} 
def setWeeklyGoal()
{
    return parent.setWeeklyGoal(measurementType)
} 
def setMonthlyGoal()
{
    return parent.setMonthlyGoal(measurementType)
} 
*/

def actionTypes() {
	def types = []
    types << [name: "Switch", capability: "capabilty.switch"]
    types << [name: "Water Valve", capability: "capability.valve"]
    
    return types
}

def deviceCommands(dev)
{
	def cmds = []
	dev.supportedCommands.each { command ->
    	cmds << command.name
    }
    
    return cmds
}

def installed() {
	state.Daily = 0
	log.debug "Installed with settings: ${settings}"
	app.updateLabel("${type} - ${waterGoal} ${measurementType}")
    //schedule("	0 0/1 * 1/1 * ? *", setDailyGoal())
	initialize()
}




    
def updated() {
	log.debug "Updated with settings: ${settings}"
	app.updateLabel("${type} - ${waterGoal} ${measurementType}")
    

	unsubscribe()
	initialize()
    //unschedule()
}

def settings() {
	def set = settings
    if (set["dev"] != null)
    {
    	log.debug("dev set: ${set.dev}")
    	set.dev = set.dev.id
    }
    if (set["valve"] != null)
    {
    	log.debug("valve set: ${set.valve}")
    	set.valve = set.valve.id
    }
    
    log.debug(set)

	return set
}

def devAction(action)
{
	if(dev)
    {
    	log.debug("device: ${dev}, action: ${action}")
		dev."${action}"()
    }
}

def isValveStatus(status)
{
	def result = false
    log.debug("Water Valve ${valve} has status ${valve.currentState("contact").value}, compared to ${status.toLowerCase()}")
	if(valve)
    {
    	if(valve.currentState("contact").value == status.toLowerCase())
        {
        	result = true
        }
    }
    return result
}

def initialize() {
    
    
    // TODO: subscribe to attributes, devices, locations, etc.
}
def uninstalled() {
    // external cleanup. No need to unsubscribe or remove scheduled jobs
}
// TODO: implement event handlers