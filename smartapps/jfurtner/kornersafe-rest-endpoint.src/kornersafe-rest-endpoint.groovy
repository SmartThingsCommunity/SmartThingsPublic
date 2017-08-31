/**
 *  KornerSafe REST endpoint
 *
 *  Copyright 2017 Jamie Furtner
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
    name: "KornerSafe REST endpoint",
    namespace: "jfurtner",
    author: "Jamie Furtner",
    description: "Endpoint for KornerSafe",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section('Virtual devices') {
        input "kornerHubDevice", "capability.contactSensor", multiple: false, required: true, title:'Kornersafe hub device'
        input "updateFrequency", "number", required:true, range: "0-14400", title:"Frequency of updates"
	}
}
mappings {
	path('/status/:status') {
    	action: [
        	GET: 'updateStatus'
        	]
        }
}

def installed() {
	logTrace("Installed with settings: ${settings}")

	initialize()
}

def updated() {
	logTrace("Updated with settings: ${settings}")

	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {
	logTrace('INIT initialize')
    logTrace("Setting update frequency to $updateFrequency")
    switch (updateFrequency)
    {
    	case 0:
        	logTrace('Auto-updating disabled')
        	break
        case 1:
        	logTrace('Auto-updating runEvery1Minute')
        	runEvery1Minute(scheduledUpdate)
            break
        case 5:
        	logTrace('Auto-updating runEvery5Minutes')
            runEvery5Minutes(scheduledUpdate)
            break
        case 15:
        	logTrace('Auto-updating runEvery15Minutes')
        	runEvery15Minutes(scheduledUpdate)
            break
        case 30:
        	logTrace('Auto-updating runEvery30Minutes')
            runEvery30Minutes(scheduledUpdate)
            break
        case 60:
        	logTrace('Auto-updating runEvery1Hour')
            runEvery1Hour(scheduledUpdate)
            break
        case 180:
        	logTrace('Auto-updating runEvery3Hours')
        	runEvery3Hours(scheduledUpdate)
        	break
        default:
        	def freq = updateFrequency * 60
        	logTrace("Auto-updating runin $freq")
        	runIn(freq, scheduledUpdateRunIn)
        	break
    }
    
    updateDeviceEndpoints();
}

def updateDeviceEndpoints()
{
	logTrace('INIT updateDeviceEndpoints')
    def url = "${apiServerUrl('/api/smartapps/installations')}/${app.id}/status"
    createAccessToken()
    def token = state.accessToken
    kornerHubDevice.setAPIEndpoints(url, token)
}

def scheduledUpdate() {
	logTrace('INIT scheduledUpdate')
	updateDeviceEndpoints()
	kornerHubDevice.poll()
}

def scheduledUpdateRunIn() {
	logTrace('INIT scheduledUpdateRunIn')
	scheduledUpdate()
    def freq = updateFrequency*60
    logTrace("Scheduling in $freq seconds")
    runIn(freq, scheduledUpdateRunIn)
}

def updateStatus() {
	logTrace('INIT updateStatus')
	def st = params.status
    logTrace("Status: $st")
    
    switch (st)
    {
    	case 'DISARM':
        	send(false, false)
        	break;
        case 'ARM':
        	send(true, false)
        	break;
        case 'ALARM':
        	send(true, true)
        	break;
        default:
        	logDebug("Unknown status $st")
    }
}

def send(Boolean stateEvent, Boolean alarmEvent)
{
	logTrace('INIT send')
	if (stateEvent)
    	kornerHubDevice.setOn()
    else
    	kornerHubDevice.setOff()
    
    if (alarmEvent)
    	kornerHubDevice.setOpen()
    else
    	kornerHubDevice.setClosed()
}

def logDebug(msg) {
	log.debug msg
}

def logTrace(msg) {
	log.debug msg
}