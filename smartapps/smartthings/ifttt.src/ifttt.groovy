/**
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
 *  IFTTT API Access Application
 *
 *  Author: SmartThings
 *
 *  ---------------------+----------------+--------------------------+------------------------------------
 *  Device Type          | Attribute Name | Commands                 | Attribute Values
 *  ---------------------+----------------+--------------------------+------------------------------------
 *  switches             | switch         | on, off                  | on, off
 *  motionSensors        | motion         |                          | active, inactive
 *  contactSensors       | contact        |                          | open, closed
 *  presenceSensors      | presence       |                          | present, 'not present'
 *  temperatureSensors   | temperature    |                          | <numeric, F or C according to unit>
 *  accelerationSensors  | acceleration   |                          | active, inactive
 *  waterSensors         | water          |                          | wet, dry
 *  lightSensors         | illuminance    |                          | <numeric, lux>
 *  humiditySensors      | humidity       |                          | <numeric, percent>
 *  alarms               | alarm          | strobe, siren, both, off | strobe, siren, both, off
 *  locks                | lock           | lock, unlock             | locked, unlocked
 *  ---------------------+----------------+--------------------------+------------------------------------
 */

import groovy.json.JsonBuilder;
import java.text.DecimalFormat;
import java.util.regex.*;

definition(
    name: "IFTTT",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Put the internet to work for you.",
    category: "SmartThings Internal",
    iconUrl: "https://ifttt.com/images/channels/ifttt.png",
    iconX2Url: "https://ifttt.com/images/channels/ifttt_med.png",
    oauth: [displayName: "IFTTT", displayLink: "https://ifttt.com"],
) {
	appSetting "notificationURL"
	appSetting "channelKey"
}

preferences {
	section("Allow IFTTT to control these things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
		input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
		input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
		input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
		input "temperatureSensors", "capability.temperatureMeasurement", title: "Which Temperature Sensors?", multiple: true, required: false
		input "accelerationSensors", "capability.accelerationSensor", title: "Which Vibration Sensors?", multiple: true, required: false
		input "waterSensors", "capability.waterSensor", title: "Which Water Sensors?", multiple: true, required: false
		input "lightSensors", "capability.illuminanceMeasurement", title: "Which Light Sensors?", multiple: true, required: false
		input "humiditySensors", "capability.relativeHumidityMeasurement", title: "Which Relative Humidity Sensors?", multiple: true, required: false
		input "alarms", "capability.alarm", title: "Which Sirens?", multiple: true, required: false
		input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
	}
}

mappings {
	// Endpoint to return location name & ID
	path("/ifttt/v1/user/info") {
    	action: [
        	GET: "info"
        ]
    }

	// Endpoint to check status
	path("/ifttt/v1/status") {
    	action: [
        	GET: "status"
        ]
    }
    
    // Endpoint for setting up test
    path("/ifttt/v1/test/setup") {
    	action: [
        	POST: "initSetup"
        ]
    }
    
    /*** TRIGGERS ***/

    // TRIGGER  -- Endpoint to retreive all devices of a certain type
    path("/ifttt/v1/triggers/:trigger_name/fields/:which_device/options") {
    	action: [
        	POST: "list"
        ]
    }
    
    // TESTING ONLY - TRIGGER  -- Endpoint to retreive all devices of a certain type
    path("/ifttt/triggers/:trigger_name/:which_device") {
    	action: [
        	POST: "list"
        ]
    }
    
    // TRIGGER -- Endpoint to get current state of all triggers
    path("/ifttt/v1/triggers/:trigger_name") {
    	action: [
        	GET: "triggerCall"
        ]
    }
    
    // TESTING ONLY - TRIGGER -- Endpoint to get current state of all triggers
    path("/ifttt/:trigger_name/states") {
		action: [
			POST: "triggerCall"
		]
	}
    
    // TESTING ONLY - TRIGGER -- Endpoint to setup a subscription to a device
    path("/ifttt/:trigger_name/subscribe") {
    	action: [
        	POST: "addSubscription"
        ]
    }
    
    // TRIGGER -- Endpoint to delete a triggerID from state
    path("/ifttt/v1/triggers/:trigger_name/trigger_identity/:trigger_identity") {
    	action: [
        	DELETE: "deleteTrigger"
        ]
    }
    
    // TESTING ONLY - TRIGGER -- Endpoint to delete a triggerID from state
    path("/ifttt/:trigger_name/trigger_identity/:trigger_identity") {
    	action: [
        	DELETE: "deleteTrigger"
        ]
    }    
    
    /*** ACTIONS ***/
    
    // ACTION -- Endpoint to handle actions
    path("/ifttt/v1/actions/:action_name") {
        action: [
            POST: "handleAction"
        ]
    }
    
    // TESTING - ACTION -- Endpoint to delete all trigger identities
    path("/ifttt/v1/delete_all") {
    	action: [
        	DELETE: "deleteStateParams"
        ]
    }
}

/**
 * Creates & initializes the state.triggerIdentities to an empty map.
 * Called when the service manager is installed.
 */
def installed() {
	log.debug settings
    state.triggerIdentities = [:];
}

/**
 * Checks if state.triggerIdentities hasn't been created. If so, creates & 
 * initializes the state.triggerIdentities to an empty map. Called when the
 * service manager is updated.
 */
def updated() {
	log.debug settings
    if(!state.triggerIdentities) {
		state.triggerIdentities = [:];
    }
}

/*** Endpoint Functions ***/

/**
 * Returns map containing the location name and id. Used to associate
 * the service manager with a user.
 *
 * @return Map containing the location ID & name
 */
def info() {
	[data: [name: "${location.name}", id: "${location.id}"]]
}

/**
 * Returns map containing valid access token & sample data for each trigger,
 * & action.
 *
 * TO-DO: Fill in triggers and actions with valid device IDs
 *
 * @return Map containing access token & device IDs for triggers and actions.               
 */
def initSetup() {
	[
        data: [
            accessToken: "1ac18b09-c5d0-4d93-a6cb-95d7859c152b", 
            samples: [
                triggers: [

                ],
                actions: [

                ]
            ]
        ]
    ];
}

/**
 * Return the HTTP status of the integration. Either 200 - Status OK or
 * 503 - Status Error.
 *
 * @return                    HTTP response code 200 or 503
 */
def status() {}

/**
 * Returns a map of the available devices for the device type based on
 * the name of the trigger/action.
 *
 * @param params.trigger_name String with the trigger/action
 * @param params.which_device String with the device id
 * @return                    Map of the available devices 
 */
def list() {
	log.debug "[PROD] list, params: ${params}"
    def triggerName = params.trigger_name
    def deviceType = getDeviceType(triggerName);
    def devicesForDTH = settings[deviceType];    
    def returnedData = [data: settings[deviceType]?.collect{deviceItem(it)} ?: []]
    return returnedData;
}

/**
 * Creates a map of events, based on the attribute associated with the recipe trigger name,
 * for a specific device over the last 7 days.
 *
 * @param params.trigger_name           String of the trigger/action name
 * @param request.JSON.trigger_identity String of the unique ID for recipe ingredients
 * @param request.JSON.triggerFields    Map of variable fields and associated values
 * @param request.JSON.limit            Integer representing the max of events to be returned
 * @return                              Map of events for specific device
 */
def triggerCall() {
	log.debug "[DEV] triggerCall -- params:${params}, request:${request.JSON}"
    def triggerIdentity = request.JSON.trigger_identity? request.JSON.trigger_identity : "";
    def triggerFields = request.JSON.triggerFields? request.JSON.triggerFields : "";
    def limit = request.JSON.limit? request.JSON.limit : 50;
    def triggerName = params.trigger_name;
    
    // params.trigger_identity is the unqiue to that combination of fields/values
    if(!state.triggerIdentities || !state.triggerIdentities[triggerIdentity]) {
        state.triggerIdentities << [(triggerIdentity) : [triggerFields : triggerFields, limit : limit, triggerName: triggerName]];
    }
    
    addSubscription(triggerIdentity);
    createTriggerResponse(triggerIdentity);
}

/**
 * Receive a request to take action on a device, parse the params and pass them along
 * to be handled.                          
 */
def handleAction() {
	log.debug "[DEV] handleAction -- params:${params}, request${request.JSON}"
    def actionFields = request.JSON.actionFields ? request.JSON.actionFields : null;
    parseActionFields(actionFields, params.action_name);
}

/**
 * Remove all the state parameters.
 */
def deleteStateParams() {
	def keys = [];
    
    state.each {
        keys << it.getKey();
    };
    keys.each {
    	state.remove(it);
    }
    state.triggerIdentities = [:];
    unsubscribe();
}

/**
 *  Removes the subscription to a device for a specific recipe, which is stored 
 *  by an association with the recipe ID. Also, removes the state variable for
 *  that recipe & any associated data.
 *  
 *  If no device is found an 404 error will be thrown.
 *
 * @param  trigger_identity  String of the trigger's unique identifier
 * @param  trigger_name      String of the trigger name
 */
def deleteTrigger() {
	log.debug "[DEV] deleteTrigger -- params:${params}, request:${request.JSON}"

	def triggerIdentity = params.trigger_identity? params.trigger_identity : null;
    def triggerName = params.trigger_name? params.trigger_name : null;
	
    def trigger =  state.triggerIdentities[triggerIdentity];
    def fields = state.triggerIdentities[triggerIdentity].triggerFields;
    
    def deviceType = getDeviceType(triggerName);
    def devicesForDTH = settings[deviceType];
    def device = devicesForDTH?.find { it.id == fields.device }
    
    if(!device || device == null) {
 		httpError(404, "Device not found - id : ${fields.device}")
    } else {
	    def deviceRecipe = state[device.id]?.find { "${it}" == "${triggerIdentity}" };
        
        if(deviceRecipe) {
    		state[device.id].remove(deviceRecipe);
            if(state[device.id] == null || state[device.id].isEmpty()) {
            	unsubscribe(device);
                state.remove(device.id);
            }
        	log.info "Recipe removed. New state[device.id] : ${state[device.id]}";
    	}
    }
    
    state.triggerIdentities.remove(triggerIdentity);
}

/*** Main Functions ***/

/**
 * Sends the appropriate command to a device based on the action name.
 * Any action's added to IFTTT channel should be added here to be handled.
 *
 * @param actionFields Map of all parameters associated with the action
 * @param actionName   String of the action name representing the action that should occur
 */
private parseActionFields(actionFields, actionName) {
    log.debug "[DEV] parseActionFields(${actionFields}, ${actionName})";
    def deviceType = getDeviceType(actionName);
    def devicesForDTH = settings[deviceType];
    def device = actionFields.device? devicesForDTH?.find { it.id == actionFields.device } : devicesForDTH?.find { it.id == actionFields.switch } //
    
    if(actionFields.size() == 1 && (actionFields.containsKey("device") || actionFields.containsKey("switch"))) {
        // Action command options are binary.
        switch(actionName) {
            case ~/.*on.*/:
                device.on();
                break;
            case ~/.*off|deactivate.*/:
                device.off();
                break;
            case "lock_smartthings":
                device.lock();
                break;
            case "unlock_smartthings":
                device.unlock();
                break;          
        }
    } else {
        // Action command options have more
        switch(actionName) {
            case ~/.*siren.*/:
                switch(actionFields.alert_type) {
                    case "strobe":
                        device.strobe();
                        break;
                    case "siren":
                        device.siren();
                        break;
                    case "both":
                        device.both();
                        break;
                }
                break;
        }
    }
}

/**
 *	Creates a JSON response containing unique events in descending order for a 
 *  specific device. If no device can be found a 404 HTTP response will be returned.
 *
 *  @param  triggerIdentity     String of the trigger's unique identifier
 *  @return response            JSON object containing unique events for a device
 */
def createTriggerResponse(triggerIdentity) {
   	log.debug "[DEV] createTriggerResponse(${triggerIdentity})"
    def response = [data : []];
	
    def trigger =  state.triggerIdentities[triggerIdentity];
    def fields = state.triggerIdentities[triggerIdentity].triggerFields

    def events = [];
    def deviceType = getDeviceType(trigger.triggerName);
    def devicesForDTH = settings[deviceType];
    def device = devicesForDTH?.find { it.id == fields.device }
    
    if (!device || device == null) {
		httpError(404, "Device not found - id : ${fields.device}")
	} else {
		def attributeName = attributeFor(deviceType)
		//def s = device.currentState(attributeName)
		events = getDeviceEvents(device, attributeName, fields, trigger.limit, trigger.triggerName)
	}
    
   	def ingredientsKeys = ingredientsFor(trigger.triggerName);
	
    if(events) {
    	events.each {
        	def ingredientsMap = getDeviceIngredientValues(ingredientsKeys, it, trigger);
        	ingredientsMap << [meta : getParsedEvent(it)];
        	response.data << ingredientsMap;
    	}
    } else {
    	log.debug "No events found for ${device.displayName}"
    }
    
    return response;
}
 
/**
 *	Creates a JSON object containing events for an attribute of a specific device.
 *
 *  @param  device          String respresenting the specific device that events should be captured from
 *  @param  attributeName   String of the type of events that should be captured
 *  @param  fields          JSON object of variables associated with the trigger
 *  @param  limit           String of the number of events to be returned. This defaults to 50 on IFTTT side
 *  @param  triggerName     String representing the type of trigger that events should be captured for
 *  @return attributeEvents JSON object of events
 */
private getDeviceEvents(device, attributeName, fields, limit, triggerName) {
	log.debug "[DEV] getDeviceEvents(${device}, ${attributeName}, ${fields}, ${limit}, ${triggerName})"
    def allEvents = device.events([max:10000000000]);
    def atrributeEvents = [];
   
    if(fields.size() == 1 && fields.containsKey("device")) {
        // Fields only contain deivce ID
        def count = 0;
        def command = deviceItem(triggerName);
        for(int i=0; i<allEvents.size() && count<(limit); i++) {
            //log.debug "command : ${command}, allEvents[i].value : ${allEvents[i].value}";
            if(command == (allEvents[i].value)) {
   	        	//log.debug "triggerName contains event value - count : ${count}, triggerName: ${triggerName}, .name : ${allEvents[i].name}, .value : ${allEvents[i].value}";
                log.debug "allEvents[i].value : ${allEvents[i].value}";
                atrributeEvents << allEvents[i]; 
                count++;
            }
    	}
    } else {
    	// Fields contain additional information
        log.debug "Fields contain additional information"
        def thresholdAboveOrBelow = aboveBelow(triggerName);
        def value = thresholdAboveOrBelow? fields.threshold : null
        def unitsMap = value? getUnits(fields) : null;
        def count = 0;
        
        for(int i=0; i<allEvents.size() && count<(limit); i++) {
            if(allEvents[i].name == attributeName) {
                if(thresholdAboveOrBelow && value) { 
            		def currValue = null;
                    
                    try {
                    	currValue = allEvents[i].integerValue;
                    } catch(e) {
                    	currValue = allEvents[i].value;
                    }
                    
                    switch(thresholdAboveOrBelow) {
            	    	case "above":
               				log.debug "above, currValue : ${currValue}, value: ${value.toInteger()}"
                            if(currValue > value.toInteger()) {
                                atrributeEvents << allEvents[i];
                                count++;
                            }
            	            break;
            	        case "below":
			   				log.debug "below, currValue : ${currValue}, value: ${value.toInteger()}"
                            if(currValue < value.toInteger()) {
                                atrributeEvents << allEvents[i];
                                count++;
                            }
            	            break;
            	    }
            	}
			}
    	}
    }
    
	return atrributeEvents;
}

/**
 *	Assign values to required ingredients and return them as map
 *
 *  @param  ingredients         Array containing ingredient keys
 *  @param  evt                 Event that helps populate ingredient values
 *  @param  trigger             JSON object of trigger that helps populate ingredient values
 *  @return assignedIngredients Map containing appropriate ingredients and their values   
 */
private getDeviceIngredientValues(ingredients, evt, trigger) {
	//log.debug "[DEV] getDeviceIngredientValues(${ingredients}, ${evt})"
    def assignedIngredients = [:];
    for(int i=0; i<ingredients.size(); i++) {
    	def currentIngredient = ingredients[i];
        switch(currentIngredient) {
        	case ~/.*_at.*/ :
		        assignedIngredients << [(ingredients[i]) : evt.date.time];
                break;
           	case ~/.*_name.*/ :
            	assignedIngredients << [(ingredients[i]) : evt.displayName];
                break;
            case "TemperatureFahrenheit" :
                if(trigger.triggerFields.threshold_units == 'C') {
					assignedIngredients << [(ingredients[i]) : convertTemp(evt.value, 'C')];
                } else {
                	assignedIngredients << [(ingredients[i]) : evt.value];
                }
                break;
            case "TemperatureCelsius" : 
            	if(trigger.triggerFields.threshold_units == 'F') {
					assignedIngredients << [(ingredients[i]) : convertTemp(evt.value, 'F')];
                } else {
                	assignedIngredients << [(ingredients[i]) : evt.value];
                }
                break;
            case "Threshold" :
            	assignedIngredients << [(ingredients[i]) : trigger.triggerFields.threshold];
                break;
            case "ThresholdUnits" : 
            	assignedIngredients << [(ingredients[i]) : trigger.triggerFields.threshold_units];
            	break;
            case "Humidity" :
            	assignedIngredients << [(ingredients[i]) : evt.value];
                break;
            case "Brightness" :
            	assignedIngredients << [(ingredients[i]) : evt.value];
            	break;
            default :
            	log.info "No ingredient declared for ${currentIngredient}";
                break;
        }
    }
    
    return assignedIngredients;
}

/**
 *  Creates a subscription for a new IFTTT trigger. Also, adds the triggerId to a state variable
 *  to track if there are any recipes current subscriped to that devices, so that a user cannot
 *  remove the device from the smartapp while recipes are dependedent upon it. If no device can 
 *  be found this will return a 404 HTTP response. This allows the use of IFTTT's Realtime API.
 *  
 *  @param  triggerIdentity     String of an unique trigger identity
 */ 
private addSubscription(triggerIdentity) {
	log.debug "[DEV] addSubscription(${triggerIdentity})"
	
    def trigger =  state.triggerIdentities[triggerIdentity];
    def fields = state.triggerIdentities[triggerIdentity].triggerFields
    def deviceType = getDeviceType(trigger.triggerName);
    def devicesForDTH = settings[deviceType];
    def device = devicesForDTH?.find { it.id == fields.device }
    
    if (!device || device == null) {
		httpError(404, "Device not found - id : ${fields.device}")
	} else {
		def attributeName = attributeFor(deviceType)
        if(!state[device.id] || state[device.id] == null) {
        	state[device.id] = [];
        }
        if(state[device.id].find { it == triggerIdentity } == null) {
            state[device.id] << (triggerIdentity);
        	log.info "Adding ${attributeName} subscription for ${device.displayName}";
			subscribe(device, attributeName, deviceHandler)
        } else {
        	log.info "Subscription for recipe already exists";
        }
	}
}

/**
 *  Creates a POST request to IFTTT's servers containing appropriate information when an event that's
 *  being subscribed to occurs.
 *  
 *  @param  evt     Event that has a recipe subscription that occured on the device 
 */ 
def deviceHandler(evt) {
	log.debug "[DEV] deviceHandler"
    def deviceInfo = state.get(evt.deviceId);
    def triggerName = null;
    def triggerIdentities = [:];
	triggerIdentities = state.triggerIdentities;
    def triggerIdentityKeys = [];
    
    triggerIdentities.each {
    	triggerIdentityKeys << it.getKey();
    }
    
    triggerIdentityKeys.each { key ->
       	if(state.triggerIdentities[key]) {
       		def trigger =  triggerIdentities[key];
    		triggerName = trigger.triggerName;
       	}
    
    	def command = commandFor(triggerName);
        log.debug "evt.${evt.descriptionText}.contains(${command}) : ${evt.descriptionText.contains(command)}"
    	if((evt.descriptionText).contains(command)) {
    		def bodyData = [];
    		bodyData << [user_id: "user"];
    		if (deviceInfo) {
				def params = [
    				uri: "https://realtime.ifttt.com",
    		        path: "/v1/notifications",
    				headers: [           
						"IFTTT-Channel-Key": "${appSettings.channelKey}",
						"Accept": "application/json",
						"Accept-Charset":	"utf-8",
						"Accept-Encoding": "gzip, deflate",
						"Content-Type": "application/json",
						"X-Request-ID": "cbe15346-2dbb-4b0b-8de2-ff6303700aec"
    		        ],
    		        body: [
    		    		data: bodyData
					]
    		    ]
    		    try {
					httpPostJson(params) { resp ->
						if("${resp.getStatus()}" == "200") {
    		            	log.info "[DEV IFTTT] Event data successfully posted"
    		            } else {
    		            	log.info "[DEV IFTTT] Response code : ${resp.getStatus()}"
    		            }
					}
				} catch (e) {
					log.info("Error parsing ifttt payload ${e}")
				}
			} else {
				log.info "[PROD] No subscribed device found"
			}
    	}
    }
}

/*** Helper Functions ***/

/**
 *  Selects & formats required metadata for an device
 *  
 *  @param  it    Device that data should be read from
 *  @return       Map containing appropriate data for a device 
 */
private deviceItem(it) {
	log.debug "[DEV] deviceItem"
    it ? [label: it.displayName, value: it.id] : null
}

/**
 *  Selects & formats required metadata for an event
 *
 *  @param  event           Event from a device
 *  @return parsedEvents    JSON object containing appropriate data for an event
 */
private getParsedEvent(event) {
    //log.debug "[DEV] getParsedEvent(${event})";
    def parsedEvents = [:];
    parsedEvents << [id: "${event.id}", timestamp: event.date.time];    
    return parsedEvents;
}

/**
 *  Checks, based on an IFTTT trigger name, if the recipe is checking for a
 *  threshold and if so, check to see if it's look for above or below a threshold.
 *
 *  @param  trigger_name    String of the trigger name
 *  @return                 String of above or below if threshold; otherwise, null.
 */ 
private aboveBelow(trigger_name) {
    log.debug "[DEV] aboveBelow(${trigger_name})"
    if(trigger_name.contains("above")) {
        return "above";
    } else if(trigger_name.contains("below")) {
        return "below";
    } else {
        return null;
    }
}

/**
 *  Identifies the units if a recipe's fields have any
 * 
 *  @param  fields  JSON object containing variables associated with a recipe
 *  @return units   Map of the units in a recipe, where key is association
 *  to-do : The map returned should only have one value since triggers are singular; map these to attributes for multiple
 */
private getUnits(fields) {
    log.debug "[DEV] getUnits(${fields})"
    def units = []
    for ( String key : fields.keySet() ) {
        if(key.contains("Units")) {
            units << [(key.minus("units")) : fields[key]];
        }
    }
    
    return units != [] ? units : null;
}

/**
 *  Return the temperature in either Metric (°C) or Fahrenheit (°F), 
 *  whichever is opposite to the temperature's current units
 *  
 *  @param  temp        Temperature that should be converted
 *  @param  currUnit    String of the current unit being used
 *  @return             String of the converted temperature in new unit
 */
private convertTemp(temp, currUnit) {
    log.debug "convertTemp(${temp}, ${currUnit})"; 
    try {
        temp = temp.toInteger();
    } catch(e) {
        temp = temp.toDouble();
    }
    
    switch(currUnit) {
        case 'C':
            return "${(temp * (9/5)) + 32}";
            break;
        case 'F':
            return "${(temp - 32) * (5/9)}";
            break;
    }
}

/**
 *  Identifies the SmartThings attribute that should be used based on an IFTTT trigger's name.
 *  
 *  @param  triggerName    String of an IFTTT trigger name
 *  @return                String of what SmartThings attribute should be listened for 
 */
private commandFor(triggerName) {
    //log.debug "commandFor(${triggerName})"
    switch(triggerName) {
        case "switched_on_smartthings":
            return "on"
        case "switched_off_smartthings":
            return "off"
        case "opened_smartthings":
            return "open"
        case "closed_smartthings":
            return "closed"
        case "locked_smartthings":
            return "locked"
        case "unlocked_smartthings":
            return "unlocked"
        case "any_new_motion_smartthings":
            return "active"
        case "new_present_smartthings":
            return "present"
        case "new_not_present_smartthings":
            return "not present"
        case ~/.*temperature.*/:
            return "temperature"
        case ~/.*humidity.*/:
            return "humidity"
        case ~/.*moisture.*/:
            return "wet"
        case ~/.*brightness.*/:
            return "illuminance"
    }

}

/**
 *  Identifies the ingredients that should be used based on an IFTTT trigger's name.
 *  
 *  @param  trigger_name    String of an IFTTT trigger
 *  @return                 Array of ingredients associated on IFTTT side with that trigger 
 */ 
private ingredientsFor(trigger_name) {
    log.debug "[DEV] ingredientsFor(${trigger_name})"
    switch(trigger_name) {
        case "switched_on_smartthings":
            return ["SwitchedOnAt", "SwitchName"];
        case "switched_off_smartthings":
            return ["SwitchedOffAt", "SwitchName"];
        case "opened_smartthings":
            return ["OpenedAt", "DeviceName"];
        case "closed_smartthings":
            return ["ClosedAt", "DeviceName"];
        case "locked_smartthings":
            return ["LockedAt", "DeviceName"];
        case "unlocked_smartthings":
            return ["UnlockedAt", "DeviceName"];
        case "any_new_motion_smartthings":
            return ["DetectedAt", "DeviceName"];
        case "new_present_smartthings":
            return ["ArrivedAt", "DeviceName"];
        case "new_not_present_smartthings":
            return ["DepartedAt", "DeviceName"];
        case "temperature_rises_above_smartthings":
            return ["TemperatureFahrenheit", "TemperatureCelsius", "Threshold", "ThresholdUnits", "MeasuredAt", "DeviceName"];
        case "temperature_drops_below_smartthings":
            return ["TemperatureFahrenheit", "TemperatureCelsius", "Threshold", "ThresholdUnits", "MeasuredAt", "DeviceName"];
        case "humidity_rises_above_smartthings":
            return ["Humidity", "Threshold", "MeasuredAt", "DeviceName"];
        case "humidity_drops_below_smartthings":
            return ["Humidity", "Threshold", "MeasuredAt", "DeviceName"];
        case "moisture_detected_smartthings":
            return ["MoistureDetectedAt", "DeviceName"];
        case "brightness_rises_above_smartthings":
            return ["Brightness", "Threshold", "MeasuredAt", "DeviceName"];
        case "brightness_drops_below_smartthings":
            return ["Brightness", "Threshold", "MeasuredAt", "DeviceName"];
        default:
            log.debug "Ingredients not found";
            return [];
    }
}

/**
 *  Identifies the variable, set in the input fields, containing the correct devices that should
 *  be used based on an IFTTT trigger's name.
 *  
 *  @param  trigger_name    String of an IFTTT trigger
 *  @return                 String of variable containing the correct devices  
 */ 
private getDeviceType(triggerName) {
    log.debug "getDeviceType(${triggerName})";
    switch(triggerName) {
        case ~/.*switch.*/:
            return "switches"
        case ~/.*open.*|.*close.*/:
            return "contactSensors"
        case ~/.*lock.*/:
            return "locks"
        case ~/.*motion.*/:
            return "motionSensors"
        case ~/.*presence.*/:
            return "presenceSensors"
        case ~/.*temperature.*/:
            return "temperatureSensors"
        case ~/.*humidity.*/:
            return "humiditySensors"
        case ~/.*moisture.*/:
            return "waterSensors"
        case ~/.*brightness.*/:
            return "lightSensors"
        case ~/.*siren.*/:
            return "alarms"
        default:
            log.info "Cannot find device type for ${triggerName}"
            break;
        
    }
}

/**
 *  Converts a IFTTT's recipe name to a string of the proper attribute name.
 *  
 *  @param  type    String of the IFTTT reipce attribute name
 *  @return         String of the SmartThings platform attribute name
 */
private attributeFor(type) {
    log.debug "[DEV] attributeFor(${type})"
    switch (type) {
        case "switches":
            return "switch"
        case "locks":
            return "lock"
        case "alarms":
            return "alarm"
        case "lightSensors":
            return "illuminance"
        case "waterSensors":
            return "wet"
        default:
            return type - "Sensors"
    }
}

