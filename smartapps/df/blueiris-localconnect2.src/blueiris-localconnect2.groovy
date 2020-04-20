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
 *  BlueIris (LocalConnect2)
 *
 *  Author: Nicolas Neverov
 *  Date: 2017-04-30
 */


definition(
    name: "BlueIris (LocalConnect2)",
    namespace: "df",
    author: "df",
    description: "BlueIris local integration",
    category: "Safety & Security",
    singleInstance: true,
    iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.doors.garage.garage-closed",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.doors.garage.garage-closed?displaySize=2x"
)

preferences {
	page(name: "setup", title: "Blue Iris Setup", content: "pageSetupCallback")
	page(name: "mode", title: "Blue Iris Modes Setup", content: "renderModePage")
	page(name: "validate", title: "Blue Iris Setup", content: "pageValidateCallback")

}

def switchHandler(evt) 
{
    log.debug "setupDevice: switch event: $evt.value"
}


private Map getValidators()
{
	return [
		hostAddress: { addr ->
        	return addr ==~ /\d+\.\d+\.\d+\.\d+(:\d+)?/
    	},
        mode: { Map p ->
        	def rc = []

			if (p.aProfileApply) {
            	if (p.aProfile == null) {
                	rc.push("Arming profile is required");
                } else if (p.aProfile < 1 || p.aProfile > 5) {
                	rc.push("Arming profile must be within [1-5] range")
                }
            }

			if (p.dProfileApply) {
            	if (p.dProfile == null) {
                	rc.push("Disarming profile is required");
                } else if (p.dProfile < 1 || p.dProfile > 5) {
                	rc.push("Disarming profile must be within [1-5] range")
                }
            }

			def armProfile = p.aProfileApply ? p.aProfile : 0;
			def disarmProfile = p.dProfileApply ? p.dProfile : 0;
			if (p.aSignal == p.dSignal && armProfile == disarmProfile && armProfile != null) {
            	rc.push("Arming and disarming signal/profile combinations must differ")
            }

            return rc
        }
	]
}

def pageSetupCallback()
{
	if (canInstallLabs()) {
		log.debug("pageSetupCallback: refreshing")

		def v = getValidators()
		return dynamicPage(name:"setup", title:"Setting up Blue Iris integration", nextPage:"", install: false, uninstall: true) {
        
			section("New BlueIris Server setup") {
            		input name:"devicename", type:"text", title: "Device name", required:true, defaultValue: "Blue Iris Server"
    	        	input name:"hub", type:"hub", title: "Hub gateway", required:true 
   	        		input name:"ip", type:"text", title: "IP address:port", required:true, submitOnChange:true 
	                if (!v.hostAddress(ip)) {
                    	paragraph(required:true, "Please specify valid IP address")
                    }
	   	        	input name:"username", type:"text", title: "Username", required:true, autoCorrect:false
    	        	input name:"password", type:"password", title: "Password", required:true, autoCorrect:false
			}
            if(v.hostAddress(ip)) {
            	section("") {
					href(title:"Next", description:"", page:"mode", required:true)
              	}
            }
		}

	} else {

		return dynamicPage(name:"setup", title:"Upgrade needed", nextPage:"", install:false, uninstall: true) {
			section("Upgrade needed") {
				paragraph "Hub firmware needs to be upgraded"
			}
		}
	}

}

private	makeProfileInput(inputName)
{
	input(name:inputName.toString(), type:"number", title:"Select profile [1-5]:", range:"1..5", submitOnChange:true, required:true)
}

def renderModePage() {
	def v = getValidators()

	return dynamicPage(name:"mode", title:"Setting up Blue Iris modes", nextPage:"", install: false, uninstall: true) {
		section(hideable:true, "Arming modes") {
            input(name:"armSignal", type:"enum", title:"When Armed, set signal to", options:["Green","N/A"], defaultValue:"Green", submitOnChange:true, required:false)
			input(name:"armProfileApply", type:"bool", title:"Also, change profile?", defaultValue:false, submitOnChange:true) 
            if (armProfileApply) {
                makeProfileInput("armProfile")
           	}
            
			input(name:"disarmSignal", type:"enum", title:"When Disarmed, set signal to", options: ["Red", "N/A"], defaultValue:"Red", submitOnChange:true, required:false)
			input(name:"disarmProfileApply", type:"bool", title:"Also, change profile?", defaultValue:false, submitOnChange:true) 
            if (disarmProfileApply) {
                makeProfileInput("disarmProfile")
           	}
		}
        
        section(hideable:true, "Location modes") {
        	location.modes.each {mode->
        		input(name:"locationSignal${mode.id}".toString(), type:"enum", title:"When in \"$mode.name\" mode, set signal to", options: ["Green", "Red", "N/A"], defaultValue:"N/A", required:false, submitOnChange:true)
				input(name:"locationProfileApply${mode.id}".toString(), type:"bool", title:"Also, change profile?", defaultValue:false, required:false, submitOnChange:true) 
            	if (settings["locationProfileApply${mode.id}".toString()] == true) {
                    makeProfileInput("locationProfile${mode.id}")
	           	}
        	}
     	}

		def p = [
        	aSignal: armSignal,
        	aProfileApply: armProfileApply,
            aProfile: armProfile,
            dSignal: disarmSignal,
            dProfileApply: disarmProfileApply,
            dProfile: disarmProfile
        ]
       
		def valRc = v.mode(p)
        if (valRc) {
        	section("Please correct errors:") {
            	valRc.each {err ->
                	paragraph(required:true, "*** $err")
                }
            }
        } else {
			section("") {
				href(title:"Next", description:"", page:"validate", required:true)
			}
      	}
        
  	}
}

def pageValidateCallback()
{
	if(ip ==~ /\d+\.\d+\.\d+\.\d+(:\d+)?/) {
    	return dynamicPage(name:"validate", title:"Setting up Blue Iris integration", install:true, uninstall:false) {
        	section() {
            	paragraph( 
                	image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
                	title:"Ready to install", 
                    "Press 'Done' to confirm installation"
             	)
          	}
        }
	} else {
    	return dynamicPage(name:"validate", title:"Setting up Blue Iris", nextPage:"", install: false, uninstall:false) {
        	section("Error validating setup preferences") {
            	paragraph( 
                	image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
                	title:"IP Address", 
                    required:true, 
                    "Should look similar to 111.222.333.555:8001 (port is optional)"
             	)
            }
        }
    }
}


def installed()
{
	log.debug("installed: started") //with $settings
	init()
}

def updated()
{
	log.debug("updated: started"); //with $settings
	uninit();
	init()
}

def uninstalled()
{

	uninit(false);
}

def init()
{
	if(!state.subscribed) {
    	subscribe(location, "mode", modeChangeHandler)
		state.subscribed = true
	}

	state.config = assembleConfig()

    final dni = ipEpToHex(ip)
    def d = getChildDevice(dni)
    
    if(d) {
    	log.debug("init: deleting existing BlueIris Server device, dni:$dni")
    	deleteChildDevice(dni)
    }
    
    if(true) {
		log.debug "init: adding new BlueIris Server device, dni:$dni, username:$username, password:*****, gateway hub id:$hub.id"
		d = addChildDevice("df", "blueiris2", dni, hub.id, 
        	[name:"blueiris", label: devicename, completedSetup:true,
             "preferences":["username":username, "password":password]
            ])
		d.configure()
	    subscribe(d, "switch", switchHandler)
    } else {

		log.debug "init: skipping adding BlueIris Server device, dni:$dni - already exists"
    }
    
}

def uninit(boolean f_unsubscribe = true)
{
	if(state.subscribed) {

		if(f_unsubscribe) {
			unsubscribe()
        }
        
        getAllChildDevices().each {
        }
        
		state.subscribed = false
    }
}


def modeChangeHandler(evt)
{
    def f_arm = (evt.value == 'Away')
	log.debug("modeChangeHandler: detected mode change: $evt.name:$evt.value: ${f_arm ? 'arming' : 'disarming'}")

	def mode = null
	location.modes.each {m->
    	if (m.name == evt.value) {
        	mode = m
      	}
    }

	getAllChildDevices().each {
    	it.location(mode.id)
    }
}

def asyncOpCallback()
{
	log.debug("asyncOpCallback: timeout:$atomicState.asyncOpTimeout, ${now() - atomicState.asyncOpTs}(msec) elapsed")
    if(atomicState.asyncOpTimeout) {
    	getAllChildDevices().each {
        	it.timeout()
        }
    }
}

def onBeginAsyncOp(int timeout_ms)
{
	log.debug("onBeginAsyncOp: ${now()}")
    atomicState.asyncOpTimeout = true
    atomicState.asyncOpTs = now()
	runOnce(new Date(now() + timeout_ms), asyncOpCallback, [overwrite: true])
}

def onEndAsyncOp()
{
	log.debug("onEndAsyncOp: ${now()}")
    atomicState.asyncOpTimeout = false
	runOnce(new Date(now() + 1), asyncOpCallback, [overwrite: true])
}

def onNotification(msg)
{
	log.debug("sendNotification: sending $msg")
	sendNotificationEvent(msg)
}

def onGetConfig()
{
	return state.config
}

private assembleConfig()
{
	def getElementCfg = {prefix, id, name ->
    	def signal = settings["${prefix}Signal${id}".toString()]
        def profileApply = settings["${prefix}ProfileApply${id}".toString()]
        def profile = settings["${prefix}Profile${id}".toString()]
        
    	return signal != 'N/A' || profileApply ? [
        	name: name,
        	signal: signal == 'N/A' ? null : (signal == "Green"),
            profile: profileApply ? profile : null
        ] : null
    }

    def rc = [
    	arming: [
        	arm: getElementCfg('arm', '', 'Arm'),
            disarm: getElementCfg('disarm', '', 'Disarm')
        ],
        location: [:]
    ]

	location.modes.each {mode->
    	rc.location["$mode.id".toString()] = getElementCfg('location', mode.id, mode.name)
    }

	log.info("onGetConfig: assembled config: [$rc]")
	rc
}

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}

private String ipEpToHex(ep) {
	final parts = ep.split(':');
    final ipHex = parts[0].tokenize('.').collect{ String.format('%02X', it.toInteger() ) }.join()
    final portHex = String.format('%04X', (parts[1]?:80).toInteger())
   
    return "$ipHex:$portHex"
}


private String hexToString(String txtInHex)
{
	byte [] txtInByte = new byte [txtInHex.length() / 2];
	int j = 0;
	for (int i = 0; i < txtInHex.length(); i += 2)
	{
			txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
	}
	return new String(txtInByte);
}
