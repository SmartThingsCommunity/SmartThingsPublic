/**
 *  Concord 4 Device Handler
 *
 *  Scott Dozier 4/1/2016
 */

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Concord4", author: "scdozier", namespace: "scdozier") {
        capability "Polling"
        capability "Lock"
        capability "Refresh"
        command "armstay"
        command "armaway"
        command "Disarm"
        command "armSilent"
        command "armLoud"
        command "armBypass"
        command "armRegular"
        command "update"
        command "setZonesClosed"
        
        attribute "armStatus", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}
    preferences {
        input("concord_server_ip_address", "text", title: "IP", description: "Concord 4 Server IP Address",defaultValue: "8.8.8.8")
        input("concord_server_port", "number", title: "Port", description: "Concord 4 Server Port Number (8066)",defaultValue: 8066)
        input("concord_server_api_password", "text", title: "API Password", description: "Concord 4 Server API PW",defaultValue: "")
    }
	tiles (scale: 2){
      multiAttributeTile(name:"ArmTile", type:"generic", width:6, height:4) {
        tileAttribute("device.lock", key: "PRIMARY_CONTROL") {
            attributeState("unlocked", label: 'DISARMED', action: "armstay", icon: "st.security.alarm.off", backgroundColor: "#cccccc", nextState: "locking")
 			attributeState("locking", label: 'ARMING', action: "armstay", icon: "st.security.alarm.partial", backgroundColor: "#e86d13")
 			attributeState("unlocking", label: 'DISARMING', action: "armstay", icon: "st.security.alarm.partial", backgroundColor: "#e86d13")
            attributeState("locked", label: 'ARMED', action: "Disarm", icon: "st.security.alarm.on", backgroundColor: "#bc2323")
        }
   			tileAttribute("device.armStatus", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'${currentValue}')
  			}
        }
        standardTile("silent", "device.silent", width: 2, height: 2,canChangeIcon: true, inactiveLabel: false) {        						
			state "silent", label: 'silent', action:"armLoud", icon: "st.custom.sonos.muted", backgroundColor: "#ffffff" , nextState : "loud"   
            state "loud", label: 'loud', action: "armSilent", icon: "st.custom.sonos.unmuted", backgroundColor: "#00a0dc" , nextState: "silent"           
		}        
        standardTile("bypass", "device.bypass", width: 2, height: 2, canChangeIcon: true,inactiveLabel: false) {        						
			state "disable", label: 'Disabled', action: "armBypass", icon: "st.secondary.tools", backgroundColor: "#ffffff" , nextState: "enable"
			state "enable", label: 'Enabled', action:"armRegular", icon: "st.secondary.tools", backgroundColor: "#00a0dc" , nextState: "disable"  
		}                
        standardTile("Zone 1", "device.zone1", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "closed", label: 'Garage Door\n', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
			state "open", label: 'Garage Door\n', icon: "st.contact.contact.open", backgroundColor: "#e86d13"			
		}  
        standardTile("Zone 2", "device.zone2",  width: 2, height: 2,inactiveLabel: false, decoration: "flat") {			
			state "closed", label: 'Front Door\n', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
            state "open", label: 'Front Door\n', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
		}  
        standardTile("Zone 3", "device.zone3", width: 2, height: 2, inactiveLabel: false,decoration: "flat") {
        	state "closed", label: 'Back Door\n', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
			state "open", label: 'Back Door\n', icon: "st.contact.contact.open", backgroundColor: "#e86d13"			
		}  
        standardTile("Zone 4", "device.zone4",  width: 2, height: 2,inactiveLabel: false) {			
			state "closed", label: 'Living Room\n', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
            state "open", label: 'Living Room\n', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
		}  
        standardTile("Zone 5", "device.zone5",  width: 2, height: 2,inactiveLabel: false) {			
			state "closed", label: 'Family Room\n', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
            state "open", label: 'Family Room\n', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
		}  
        standardTile("Zone 6", "device.zone6", width: 2, height: 2, inactiveLabel: false) {			
			state "closed", label: 'Kitchen Window\n', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
            state "open", label: 'Kitchen Window', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
		}  
        standardTile("Zone 7", "device.zone7", width: 2, height: 2, inactiveLabel: false) {
        	state "closed", label: 'Kitchen Glass Break', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
			state "open", label: 'Kitchen Glass Break', icon: "st.contact.contact.open", backgroundColor: "#e86d13"			
		}  
        standardTile("Zone 8", "device.zone8",  width: 2, height: 2,inactiveLabel: false) {			
			state "closed", label: 'Foyer Motion\n', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
            state "open", label: 'Foyer Motion\n', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
		}  
        standardTile("Zone 9", "device.zone9",  width: 2, height: 2,inactiveLabel: false) {
        	state "closed", label: 'Downstairs Fire\n', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
			state "open", label: 'Downstairs Fire\n', icon: "st.contact.contact.open", backgroundColor: "#e86d13"			
		}  
        standardTile("Zone 10", "device.zone10", width: 2, height: 2, inactiveLabel: false) {			
			state "closed", label: 'Bedroom Fire\n', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
            state "open", label: 'Bedroom Fire\n', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
		}
        standardTile("Zone 13", "device.zone13",  width: 2, height: 2,inactiveLabel: false) {			
			state "closed", label: 'Office Window 1', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
            state "open", label: 'Office Window 1', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
		}  
        standardTile("Zone 14", "device.zone14", width: 2, height: 2, inactiveLabel: false) {			
			state "closed", label: 'Office Window 2', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
            state "open", label: 'Office Window 2', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
		}  
        standardTile("Zone 15", "device.zone15",  width: 2, height: 2,inactiveLabel: false) {			
			state "closed", label: 'Office Window 3', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
            state "open", label: 'Office Window 3', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
		}  

		standardTile("refresh", "device.alarmMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
        	}
	standardTile("setZonesClosedTile", "device.alarmMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label: 'Zones Closed', action:"setZonesClosed", icon:"st.secondary.refresh"
        	}
	}
    
    main "ArmTile"
    details(["ArmTile","Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5", "Zone 6", "Zone 7", "Zone 8", "Zone 9", "Zone 10", "Zone 13",
    "Zone 14","Zone 15", "refresh", "silent","setZonesClosedTile" ])
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
 
}

def setZonesClosed()
{
	sendEvent(name: "zone1", value: "closed")
	sendEvent(name: "zone2", value: "closed")
	sendEvent(name: "zone3", value: "closed")
	sendEvent(name: "zone4", value: "closed")
	sendEvent(name: "zone5", value: "closed")
	sendEvent(name: "zone6", value: "closed")
	sendEvent(name: "zone7", value: "closed")
	sendEvent(name: "zone8", value: "closed")
	sendEvent(name: "zone9", value: "closed")
	sendEvent(name: "zone10", value: "closed")
	sendEvent(name: "zone11", value: "closed")
	sendEvent(name: "zone12", value: "closed")
	sendEvent(name: "zone13", value: "closed")
	sendEvent(name: "zone14", value: "closed")
	sendEvent(name: "zone15", value: "closed")

	sendEvent(name: "zone1", value: "inactive")
	sendEvent(name: "zone2", value: "inactive")
	sendEvent(name: "zone3", value: "inactive")
	sendEvent(name: "zone4", value: "inactive")
	sendEvent(name: "zone5", value: "inactive")
	sendEvent(name: "zone6", value: "inactive")
	sendEvent(name: "zone7", value: "inactive")
	sendEvent(name: "zone8", value: "inactive")
	sendEvent(name: "zone9", value: "inactive")
	sendEvent(name: "zone10", value: "inactive")
	sendEvent(name: "zone11", value: "inactive")
	sendEvent(name: "zone12", value: "inactive")
	sendEvent(name: "zone13", value: "inactive")
	sendEvent(name: "zone14", value: "inactive")
	sendEvent(name: "zone15", value: "inactive")


	sendEvent(name: "zone1", value: "clear")
	sendEvent(name: "zone2", value: "clear")
	sendEvent(name: "zone3", value: "clear")
	sendEvent(name: "zone4", value: "clear")
	sendEvent(name: "zone5", value: "clear")
	sendEvent(name: "zone6", value: "clear")
	sendEvent(name: "zone7", value: "clear")
	sendEvent(name: "zone8", value: "clear")
	sendEvent(name: "zone9", value: "clear")
	sendEvent(name: "zone10", value: "clear")
	sendEvent(name: "zone11", value: "clear")
	sendEvent(name: "zone12", value: "clear")
	sendEvent(name: "zone13", value: "clear")
	sendEvent(name: "zone14", value: "clear")
	sendEvent(name: "zone15", value: "clear")

}

def update(attribute,state) {
    log.debug "update state, request: attribute: ${attribute}  state: ${state}"
    def currentValues = device.currentValue(attribute)
    if(state != currentValues as String) {
    	log.debug "changing state.."
    	sendEvent(name: attribute, value: state)
    }
    if(attribute == "armstatus") {
        	log.debug "changing armstatus.."
    	sendEvent(name: "currentState", value: state)
        if(state == "disarmed")
        {
        	sendEvent(name: "lock", value: "unlocked")
        }
        else if (state == "stay" )
        {
        	sendEvent(name: "lock", value: "locked")
        }
        else if (state == "away") 
        {
            sendEvent(name: "lock", value: "locked")

        }
        
    }
    if(attribute.startsWith("zone")) {
       	log.debug "changing zone staus.."
		sendEvent(name: attribute, value: state)
        }
        

    }

def installed()
{
	state.bLoud = "False"
}

def updated()
{

}


// handle commands
def poll()
{
    return request('/refresh')
}

def armSilent()
{
	state.bLoud = "False"
}

def armLoud()
{
	state.bLoud = "True"
}

def armBypass()
{
	state.bBypass = "True"
}

def armRegular()
{
	state.bBypass = "False"
}

def lock() {
	armstay()
}

def unlock(){
	Disarm()
}

def armstay() {
    if (device.currentValue("lock") == "unlocked")
    {
    	sendEvent(name: "lock", value: "locking")
		log.debug "Executing 'ArmStay'"
        if( state.bLoud == "False" )
        {
            return request('/arm/stay')
        }
        else 
        {

            return request('/arm/stay/loud')
        }
    }
}

def armaway() {
    if (device.currentValue("lock") == "unlocked")
    {
        log.debug "Executing 'ArmAway'"
     	sendEvent(name: "lock", value: "locking")
        if( state.bLoud == "False" )
        {
            return request('/arm/away')
        }
        else 
        {
            return request('/arm/away/loud')
        }
    }
}

def Disarm() {
    if (device.currentValue("lock") == "locked")
    {
        log.debug "Executing 'Disarm'"
    	sendEvent(name: "lock", value: "unlocking")
        if( state.bLoud == "False" )
        {
            return request('/disarm')
        }
        else
        {
            return request('/disarm/loud')
        }
    }
}

def request(request) {
	log.debug("Request:'${request}'")
	def userpassascii = "admin:${concord_server_api_password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def hosthex = convertIPtoHex(concord_server_ip_address)
    def porthex = convertPortToHex(concord_server_port)
    log.debug("${device.deviceNetworkId}")
    def hubAction = new physicalgraph.device.HubAction(
   	 		'method': 'POST',
    		'path': "/concord${request}"+"&apiserverurl="+java.net.URLEncoder.encode(apiServerUrl("/api/smartapps/installations"), "UTF-8"),
        	'body': '',
        	'headers': [ HOST: "${hosthex}:${porthex}" , Authorization:userpass]
		)

    log.debug hubAction
    return hubAction
}


private String convertIPtoHex(ipAddress) {
	log.debug('convertIPtoHex:'+"${ipAddress}")
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	log.debug('convertIPtoHex:'+"${port}")
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}