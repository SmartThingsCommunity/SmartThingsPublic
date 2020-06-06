/**
 *  Nest Protect (Direct)
 *  Author: chad@monroe.io
 *  Author: nick@nickhbailey.com
 *  Author: dianoga7@3dgo.net
 *  Date: 2016.01.24
 *
 *
 * INSTALLATION
 * =========================================
 * 1) Create a new device type from code (https://graph.api.smartthings.com/ide/devices)
 *      Copy and paste the below, save, publish "For Me"
 *
 * 2) Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: Nest Protect (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * 3) Update device preferences
 *     Click on the new device to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your information.
 *     To find your serial number, login to http://home.nest.com. Click on the smoke detector
 *     you want to see. Under settings, go to Technical Info. Your serial number is
 *     the second item.
 *
 * Original design/inspiration provided by:
 *  -> https://github.com/sidjohn1/SmartThings-NestProtect
 *  -> https://gist.github.com/Dianoga/6055918
 *
 * Copyright (C) 2016 Chad Monroe <chad@monroe.io>
 * Copyright (C) 2014 Nick Bailey <nick@nickhbailey.com>
 * Copyright (C) 2013 Brian Steere <dianoga7@3dgo.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/

 /**
  * Static info
  */
  private NEST_LOGIN_URL()		{ "https://home.nest.com/user/login" }
  private USER_AGENT_STR()		{ "Nest/1.1.0.10 CFNetwork/548.0.4" }

preferences 
{
	input( "username", "text", title: "Username", description: "Your Nest Username (usually an email address)", required: true, displayDuringSetup: true )
	input( "password", "password", title: "Password", description: "Your Nest Password", required: true, displayDuringSetup: true )
	input( "mac", "text", title: "MAC Address", description: "The MAC address of your smoke detector", required: true, displayDuringSetup: true )
}

metadata 
{
	definition( name: "Nest Protect - Direct", author: "chad@monroe.io", namespace: "cmonroe" ) 
	{
		capability "Polling"
		capability "Refresh"
		capability "Battery"
		capability "Smoke Detector"
		capability "Carbon Monoxide Detector"
        
		attribute "alarm_state", "string"
		attribute "night_light", "string"
		attribute "line_power", "string"
		attribute "co_previous_peak", "string"
		attribute "wifi_ip", "string"
		attribute "version_hw", "string"
		attribute "version_sw", "string"
		attribute "secondary_status", "string"
	}

	simulator 
	{
		/* TODO */
	}

	tiles( scale: 2 ) 
	{
		multiAttributeTile( name:"alarm_state", type: "lighting", width: 6, height: 4 ) 
		{
			tileAttribute( "device.alarm_state", key: "PRIMARY_CONTROL" ) 
			{
				attributeState( "default", label:'--', icon: "st.unknown.unknown.unknown" )
				attributeState( "clear", label:"CLEAR", icon:"st.alarm.smoke.clear", backgroundColor:"#44b621" )
				attributeState( "smoke", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13" )
				attributeState( "co", label:"CO", icon:"st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor:"#e86d13" )
				attributeState( "tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13" )
			}

			tileAttribute( "device.status_text", key: "SECONDARY_CONTROL" ) 
			{
				attributeState( "status_text", label: '${currentValue}', unit:"" )
			}
		}
        
		standardTile( "smoke", "device.smoke", width: 2, height: 2 ) 
		{
			state( "default", label:'UNK', icon: "st.unknown.unknown.unknown" )
			state( "clear", label:"OK", icon:"st.alarm.smoke.clear", backgroundColor:"#44B621" )
			state( "detected", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13" )
			state( "tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13" )
		}

		standardTile( "carbonMonoxide", "device.carbonMonoxide", width: 2, height: 2 ) 
		{
			state( "default", label:'UNK', icon: "st.unknown.unknown.unknown" )
			state( "clear", label:"OK", icon:"st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor:"#44B621" )
			state( "detected", label:"CO", icon:"st.alarm.carbon-monoxide.clear", backgroundColor:"#e86d13" )
			state( "tested", label:"TEST", icon:"st.alarm.carbon-monoxide.test", backgroundColor:"#e86d13" )
		}

		standardTile( "night_light", "device.night_light", width: 2, height: 2 ) 
		{
			state( "default", label:'UNK', icon: "st.unknown.unknown.unknown" )
			state( "unk", label:'UNK', icon: "st.unknown.unknown.unknown" )
			state( "on", label: 'ON', icon: "st.switches.light.on", backgroundColor: "#44B621" )
			state( "low", label: 'LOW', icon: "st.switches.light.on", backgroundColor: "#44B621" )
			state( "med", label: 'MED', icon: "st.switches.light.on", backgroundColor: "#44B621" )
			state( "high", label: 'HIGH', icon: "st.switches.light.on", backgroundColor: "#44B621" )
			state( "off", label: 'OFF', icon: "st.switches.light.off", backgroundColor: "#ffffff" )
		}
        
		valueTile( "version_hw", "device.version_hw", width: 2, height: 2, decoration: "flat" ) 
		{
			state( "default", label: 'Hardware ${currentValue}' )
		}

		valueTile( "co_previous_peak", "device.co_previous_peak", width: 2, height: 2 ) 
		{
			state( "co_previous_peak", label: '${currentValue}', unit: "ppm",
				backgroundColors: [
					[value: 69, color: "#44B621"],
					[value: 70, color: "#e86d13"]
				]
			)
 		}

		valueTile( "version_sw", "device.version_sw", width: 2, height: 2, decoration: "flat" ) 
		{
			state( "default", label: 'Software ${currentValue}' )
		}
        
		standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat")
		{
			state( "default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon" )
		}

		valueTile("wifi_ip", "device.wifi_ip", inactiveLabel: false, width: 4, height: 2, decoration: "flat")
		{
			state( "default", label:'IP: ${currentValue}', height: 1, width: 2, inactiveLabel: false )
		}

	main "alarm_state"
	details( ["alarm_state", "smoke", "carbonMonoxide", "night_light", "version_hw", "co_previous_peak", "version_sw", "wifi_ip", "refresh"] )
    }
}


/**
 * handle commands
 */
def installed()
{
	log.info "Nest Protect - Direct ${textVersion()}: ${textCopyright()} Installed"
	do_update()
}

def initialize() 
{
	log.info "Nest Protect - Direct ${textVersion()}: ${textCopyright()} Initialized"
	do_update()
}

def updated() 
{
	log.info "Nest Protect - Direct ${textVersion()}: ${textCopyright()} Updated"
	data.auth = null
}

def poll() 
{
	log.debug "poll for protect with MAC: " + settings.mac.toUpperCase()
	do_update()
}

def refresh() 
{
	log.debug "refresh for protect with MAC: " + settings.mac.toUpperCase()
	do_update()
}

def reschedule()
{
	log.debug "re-scheduling update for protect with MAC: " + settings.mac.toUpperCase()
	runIn( 300, 'do_update' )
}

def do_update()
{
	log.debug "refresh for device with MAC: " + settings.mac.toUpperCase()

	api_exec( 'status', [] ) 
	{
		def status_text = ""
        
		data.topaz = it.data.topaz.getAt( settings.mac.toUpperCase() )

		//log.debug data.topaz

		data.topaz.smoke_status = data.topaz.smoke_status == 0 ? 'clear' : 'detected'
		data.topaz.co_status = data.topaz.co_status == 0 ? 'clear' : 'detected'
		data.topaz.battery_health_state = data.topaz.battery_health_state  == 0 ? 'ok' : 'low'
		data.topaz.kl_software_version = "v" + data.topaz.kl_software_version.split('Software ')[-1]
		data.topaz.model = "v" + data.topaz.model.split('-')[-1]

		if ( data.topaz.night_light_enable )
		{
			switch ( data.topaz.night_light_brightness )
			{
				case 1:
					data.topaz.night_light_brightness = "low"
					break
				case 2:
					data.topaz.night_light_brightness = "med"
					break
 				case 3:
					data.topaz.night_light_brightness = "high"
					break
				default:
					data.topaz.night_light_brightness = "on"
					break
			}
		}
		else
		{
			data.topaz.night_light_brightness = "off"
		}

		if ( data.topaz.line_power_present )
		{
			data.topaz.line_power_present = "ok"
		}
		else
		{
			data.topaz.line_power_present = "dead"
		}

		if ( !data.topaz.co_previous_peak )
		{
			/* protect 2.0 units do not support this */
			data.topaz.co_previous_peak = 'N/A'
		}
		else
		{
			data.topaz.co_previous_peak = "${data.topaz.co_previous_peak}ppm"
		}

		sendEvent( name: 'smoke', value: data.topaz.smoke_status, descriptionText: "${device.displayName} smoke ${data.topaz.smoke_status}", displayed: false )
		sendEvent( name: 'carbonMonoxide', value: data.topaz.co_status, descriptionText: "${device.displayName} carbon monoxide ${data.topaz.co_status}", displayed: false )
		sendEvent( name: 'battery', value: data.topaz.battery_health_state, descriptionText: "${device.displayName} battery is ${data.topaz.battery_health_state}", displayed: false )
		sendEvent( name: 'night_light', value: data.topaz.night_light_brightness, descriptionText: "${device.displayName} night light is ${data.topaz.night_light_brightness}", displayed: true )
		sendEvent( name: 'line_power', value: data.topaz.line_power_present, descriptionText: "${device.displayName} line power is ${data.topaz.line_power_present}", displayed: false )
		sendEvent( name: 'co_previous_peak', value: data.topaz.co_previous_peak, descriptionText: "${device.displayName} previous CO peak (PPM) is ${data.topaz.co_previous_peak}", displayed: false )
		sendEvent( name: 'wifi_ip', value: data.topaz.wifi_ip_address, descriptionText: "${device.displayName} WiFi IP is ${data.topaz.wifi_ip_address}", displayed: false )
		sendEvent( name: 'version_hw', value: data.topaz.model, descriptionText: "${device.displayName} hardware model is ${data.topaz.model}", displayed: false )
		sendEvent( name: 'version_sw', value: data.topaz.kl_software_version, descriptionText: "${device.displayName} software version is ${data.topaz.kl_software_version}", displayed: false )
        
		app_alarm_sm()
        
		status_text = "Line Power: ${device.currentState('line_power').value}   Battery: ${device.currentState('battery').value}"
		sendEvent( name: 'status_text', value: status_text, descriptionText: status_text, displayed: false )

		log.debug "Smoke: ${data.topaz.smoke_status}"
		log.debug "CO: ${data.topaz.co_status}"
		log.debug "Battery: ${data.topaz.battery_health_state}"
		log.debug "Night Light: ${data.topaz.night_light_brightness}"
		log.debug "Line Power: ${data.topaz.line_power_present}"
		log.debug "CO Previous Peak (PPM): ${data.topaz.co_previous_peak}"
		log.debug "WiFi IP: ${data.topaz.wifi_ip_address}"
		log.debug "Hardware Version: ${data.topaz.model}"
		log.debug "Software Version: ${data.topaz.kl_software_version}"
    }
    
    reschedule()
}

/**
 * state machine for setting global alarm state of app
 */
 def app_alarm_sm()
 {
 	def alarm_state = "clear"
 	def smoke = data.topaz.smoke_status
 	def co = data.topaz.co_status

 	switch( smoke )
 	{
 		case 'clear':
 			if ( co != "clear" )
 			{
 				alarm_state = "co"
 			}
 			break
 		case 'detected':
 			alarm_state = "smoke"
 			break
 		case 'tested':
 		default:
 			/**
 			 * ensure that real co alarm is not set before sending tested alarm for smoke
 			 */
 			if ( co == 'detected' )
 			{
 				alarm_state = "co"
 			}
 			break
 	}

 	log.info "alarm state machine finished, sending event.."
 	log.info "alarm_state: ${alarm_state} smoke: ${smoke} CO: ${co}"

 	sendEvent( name: 'alarm_state', value: alarm_state, descriptionText: "Alarm: ${alarm_state} (Smoke/CO: ${smoke}/${co})", type: "physical", displayed: true, isStateChange: true )
 }

/**
 * main entry point for nest API calls
 */
def api_exec(method, args = [], success = {}) 
{
	log.debug "API exec method: ${method} with args: ${args}"

	if( !logged_in() ) 
	{
		log.debug "login required"

		login(method, args, success)
		return
	}
    
	if( method == null ) 
	{
		log.info "API exec with no method passed and we are already logged in; bailing"
		return
	}

	def methods = 
	[
		'status': 
		[
			uri: "/v2/mobile/${data.auth.user}", type: 'get' 
		],
	]

	def request = methods.getAt( method )

	log.debug "already logged in"

	handle_request( request.uri, args, request.type, success )
}

/**
 * handle_request() only works once logged in, therefor
 * call api_exec() rather than this method directly.
 */
def handle_request(uri, args, type, success) 
{
	log.debug "handling request type: ${type} at URI: ${uri} with args: ${args}"

	if( uri.charAt(0) == '/' ) 
	{
		uri = "${data.auth.urls.transport_url}${uri}"
	}

	def params = 
	[
		uri: uri,
		headers: 
		[
			'X-nl-protocol-version': 1,
			'X-nl-user-id': data.auth.userid,
			'Authorization': "Basic ${data.auth.access_token}",
			'Accept-Language': 'en-us',
			'userAgent':  USER_AGENT_STR()
		],
		body: args
	]

	def post_request = { response ->
		
		if( response.getStatus() == 302 ) 
		{
			def locations = response.getHeaders( "Location" )
			def location = locations[0].getValue()
			
			log.debug "redirecting to ${location}"
			
			handle_request( location, args, type, success )
		} 
		else
		{
			success.call( response )
		}
	}

	try 
	{
		if( type == 'get' ) 
		{
			httpGet( params, post_request )
		}
	} 
	catch( Throwable e ) 
	{
		login()
	}
}

def login(method = null, args = [], success = {}) 
{
	def params = 
	[
		uri: NEST_LOGIN_URL(),
		body: [ username: settings.username, password: settings.password ]
	]

	httpPost( params ) { response ->
		
		data.auth = response.data
		data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
		log.debug data.auth

		api_exec( method, args, success )
	}
}

def logged_in() 
{
	if( !data.auth ) 
	{
		log.debug "data.auth is missing, not logged in"
		return false
	}

	def now = new Date().getTime();
	
	return( data.auth.expires_in > now )
}

private def textVersion() 
{
	def text = "Version 1.6"
}

private def textCopyright() 
{
	def text = "Copyright © 2016 Chad Monroe <chad@monroe.io>"
}
