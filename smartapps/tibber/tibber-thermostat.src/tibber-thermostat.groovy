/**
 *  Tibber Thermostat
 *
 *  Copyright 2017 Tibber AS
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
    name: "Tibber Thermostat",
    namespace: "Tibber",
    author: "Tibber developer",
    description: "Enable smart heating through Tibber. Make use of the Tibber and your SmartThings thermostats (you select which ones) are automatically adjusted based on your daily schedules, energy prices, your home\u2019s thermal capacity and weather information.",
    category: "Convenience",
    iconUrl: "https://store.tibber.com/no/wp-content/uploads/sites/8/2017/12/tibber_app_logo.png",
    iconX2Url: "https://store.tibber.com/no/wp-content/uploads/sites/8/2017/12/tibber_app_logo.png",
    iconX3Url: "https://store.tibber.com/no/wp-content/uploads/sites/8/2017/12/tibber_app_logo.png",
    oauth:true)

mappings {
  path("/thermostats") {
    action: [
      GET: "listThermostats"
    ]
  }
  path("/thermostats/:id/:setpoint") {
    action: [
      PUT: "updateThermostats"
    ]
  }
}

def listThermostats() {
    def resp = []
    def tempScale = location.temperatureScale
    thermostats.each {
        def temperatureState = it.temperatureState
        def heatingSetpointState = it.heatingSetpointState
        def range = it.currentValue("heatingSetpointRange")
        def rangeLow = 0
        def rangeHigh = 100
        if(range!=null){
        	rangeLow = range[0]
			    rangeHigh = range[1]
        }
      	resp << [
        id: it.id, 
        name: it.displayName, 
        heatingSetpoint: it.currentValue("heatingSetpoint"),
        rangeLow: rangeLow,
        rangeHigh: rangeHigh,
        temperature: it.currentValue("temperature"), 
        mode: it.currentValue("thermostatMode"),
        heatingSetpointState: heatingSetpointState, 
        temperatureState: temperatureState,
        unit:tempScale]
        
    }
    
    return resp
}
void updateThermostats() {
    // use the built-in request object to get the command parameter
    def id = params.id
    def setpoint = Float.parseFloat(params.setpoint)
    log.debug(id)
    log.debug(setpoint)
    thermostats.each{
    	if(it.id==id){
        	it.setHeatingSetpoint(setpoint);
        }
    }
}

preferences {
    page(name: "createConfig")
}

def createConfig() {
    if (!state.accessToken) {
        createAccessToken()
    }
    dynamicPage(name: "createConfig", title: "Configuration", install:true, uninstall:true) {
        section ("Allow external service to control these things...") {
            href url:"${apiServerUrl("/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}")}", style:"embedded", required:false, title:"Access Token", description:"Tap, select, copy, then click \"Done\"."
            input "thermostats", "capability.thermostat", multiple: true, required: false, title:"Which devices?", description:"Select the devices you want Tibber to have access to."
        }
    }
}
def installed() {
	if (!state.accessToken) {
        createAccessToken()
    }
    log.info "Access token is ${state.accessToken}"
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	if (!state.accessToken) {
      createAccessToken()
  }
  log.info "Access token is ${state.accessToken}"
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
}

def wrapHtml(body, title){
	return  "<!DOCTYPE html><html><head><meta charset='utf-8' /><meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, shrink-to-fit=no' />" +
          "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.1.1/css/bootstrap.css' integrity='sha256-KeWggbCyRNU5k8MgZ7Jf8akh/OtL7Qu/YloCBpayj40=' crossorigin='anonymous' />" +
          "<title>$title</title>" +
          "</head>" +
          "<body>" +
          "<div class='container'><div class='row'><div class='col'>" +
          body +
          "</div></div></body></html>"
}

def renderConfig() {
    def body =  "<p class='lead mt-5 mb-4 text-center'>Copy this and paste it into the Tibber app</p>" +
                "<p><pre class='access_token w100 text-center font-weight-bold'>${state.accessToken}</pre></p>"
    def html = wrapHtml(body, "Tibber Access Token")
    render contentType: "text/html", data: html
}

mappings {
	path("/config") { action: [GET: "renderConfig"] }
}