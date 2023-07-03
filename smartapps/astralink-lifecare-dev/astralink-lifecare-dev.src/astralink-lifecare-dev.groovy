/**
 *  My app
 *
 *  Copyright 2015 Astralink
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
 *
 */
 
 def smartAppNameFull() {
    return  "Astralink Lifecare Dev for SmartThings"
}

def smartAppNameShort() {
    return  "Astralink Lifecare Dev"
}

def smartAppVersion() {
    return  "Version 0.0.2"
}

def smartAppAuthor() {
    return  "Author Cornelius Cety Lim"
}

def smartAppCopyright() {
    return  "Copyright (c) 2015 Cornelius Cety Lim"
}

def smartAppDescription() {
    return  "This SmartApp push the data to Astralink Server."
}

def smartAppRevision () {
    return  '2015-11-11  v0.0.1\n' +
            ' * Initial release\n\n' +
            '2016-01-14  v0.0.2\n' +
            ' * Changed Hub ID to ZigbeeID during commisioning\n' +
            ' * Added master bedroom field\n' +
            ' * Added disclaimer page\n' +
            ' * Change SmartApp name to Astralink Lifecare\n' +
            ' * Added Checking for devices\n' +
            ' * Added WakeUp attribute\n\n'
}

private getAppID() 		{ appSettings.appID }
private getToken() 		{ appSettings.token }


definition(
    name: "Astralink Lifecare Dev",
    namespace: "Astralink Lifecare Dev",
    author: "Astralink",
    description: "Astralink Lifecare Dev",
    category: "My Apps",
    iconUrl: "http://52.74.131.217/imgo.jpg",
    iconX2Url: "http://52.74.131.217/imgo.jpg",
    oauth: true){
    appSetting "appID"
    appSetting "token"
    }


preferences {
    page(name: "firstPage", title: "Astralink Lifecare Dev Disclaimer", nextPage: "door", uninstall: true) {
        section {
			paragraph "This is a subscription based service, please ensure you have subscribed for this service with Singtel to continue.  Please note to provide the service, your data will be sent to Astralink, where the use of such data will be governed by Singtel’s Data Protection Policy."
			paragraph "If you are subscriber tap 'Next' to continue..."
        }
    }
    page(name: "door", title: "Door Sensor", nextPage: "checkAuthorizedDoorSensor", uninstall: true) {
        section {
			input "door", "capability.contactSensor", title: "Select a Door sensor", required: false, multiple: true
        }
    }
    page name:"checkAuthorizedDoorSensor"  
    page name:"checkAuthorizedLivingroomSensor"    
    page name:"checkAuthorizedBedroomSensor"    
    page name:"checkAuthorizedBathroomSensor"    
    page name:"checkAuthorizedKitchenSensor"    
    page name:"checkAuthorizedPanicButton"    
    page(name: "last", title: "Please Enter SmartHomeID",nextPage: "validatePage") {
        section {
            input(name: "singnetId", type: "text",
              title: "Enter SmartHomeID",
              required: true,
              multiple: false)
            input(name: "singnetIdConfirm", type: "text",
              title: "Confirm SmartHomeID",
              required: true,
              multiple: false)  
        }
    } 
    page name:"validatePage"
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}


def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def uninstalled() {
    log.debug "uninstalled"
    deregisterSamsungGateway()
    // external cleanup. No need to unsubscribe or remove scheduled jobs
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe door, "contact", contactHandler
    subscribe door, "WakeUp", contactHandler    
    subscribe livingMotion1, "motion ", livingMotionHandler1
    subscribe livingMotion1, "WakeUp ", livingMotionHandler1
    subscribe livingMotion2, "motion", livingMotionHandler2
    subscribe livingMotion2, "WakeUp", livingMotionHandler2
    subscribe livingMotion3, "motion", livingMotionHandler3
    subscribe livingMotion3, "WakeUp", livingMotionHandler3
    subscribe masterBedroomMotion, "motion", masterBedroomMotionHandler
    subscribe masterBedroomMotion, "WakeUp", masterBedroomMotionHandler
    subscribe bedroomMotion1, "motion", bedroomMotionHandler1
    subscribe bedroomMotion1, "WakeUp", bedroomMotionHandler1
    subscribe bedroomMotion2, "motion", bedroomMotionHandler2
    subscribe bedroomMotion2, "WakeUp", bedroomMotionHandler2
    subscribe bedroomMotion3, "motion", bedroomMotionHandler3
    subscribe bedroomMotion3, "WakeUp", bedroomMotionHandler3
    subscribe bathroomMotion1, "motion", bathroomMotionHandler1
    subscribe bathroomMotion1, "WakeUp", bathroomMotionHandler1
    subscribe bathroomMotion2, "motion", bathroomMotionHandler2
    subscribe bathroomMotion2, "WakeUp", bathroomMotionHandler2
    subscribe bathroomMotion3, "motion", bathroomMotionHandler3
    subscribe bathroomMotion3, "WakeUp", bathroomMotionHandler3
    subscribe kitchenMotion1, "motion", kitchenMotionHandler1
    subscribe kitchenMotion1, "WakeUp", kitchenMotionHandler1
    subscribe kitchenMotion2, "motion", kitchenMotionHandler2
    subscribe kitchenMotion2, "WakeUp", kitchenMotionHandler2
    subscribe kitchenMotion3, "motion", kitchenMotionHandler3
    subscribe kitchenMotion3, "WakeUp", kitchenMotionHandler3
    subscribe panicButton, "button", PanicHandler
    subscribe panicButton, "WakeUp", PanicHandler
}

def PanicHandler(evt) {
	def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone) 
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
   
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
 	if (evt.value == "pushed") {
     	postToASTServer("SmartApp", "Others", "OT", "2", "PANIC", now, "38", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Others", "OT", "20010", "Sensor Update Data", now, "38", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
  	} else if (evt.value == "wakeup") {
        postToASTServer("SmartApp", "Others", "OT", "20010", "Sensor Update Data", now, "38", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Others", "OT", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}


def contactHandler(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
        
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}

    if("open" == evt.value){
        postToASTServer("SmartApp", "Livingroom", "LR", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Livingroom", "LR", "20001", "Alarm Report", now, "11", evt.displayName, "Alarm On", ManuCode, ProdCode, ProdTypeCode, wConfigCode)        
    }else if("closed" == evt.value){
        postToASTServer("SmartApp", "Livingroom", "LR", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Livingroom", "LR", "20001", "Alarm Report", now, "11", evt.displayName, "Alarm Off", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    	
    }else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Livingroom", "LR", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)      
        postToASTServer("SmartApp", "Livingroom", "LR", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def masterBedroomMotionHandler(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Master Bedroom", "MB", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Master Bedroom", "MB", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Master Bedroom", "MB", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Master Bedroom", "MB", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Master Bedroom", "MB", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Master Bedroom", "MB", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def bedroomMotionHandler1(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Bedroom", "BR", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bedroom", "BR", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Bedroom", "BR", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bedroom", "BR", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Bedroom", "BR", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Bedroom", "BR", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def bedroomMotionHandler2(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""

    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Bedroom 2", "BR2", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bedroom 2", "BR2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Bedroom 2", "BR2", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bedroom 2", "BR2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Bedroom 2", "BR2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Bedroom 2", "BR2", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def bedroomMotionHandler3(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Bedroom 3", "BR3", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bedroom 3", "BR3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Bedroom 3", "BR3", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bedroom 3", "BR3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Bedroom 3", "BR3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Bedroom 3", "BR3", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}


def bathroomMotionHandler1(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Bathroom 1", "BT1", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bathroom 1", "BT1", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Bathroom 1", "BT1", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bathroom 1", "BT1", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Bathroom 1", "BT1", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Bathroom 1", "BT1", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def bathroomMotionHandler2(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Bathroom 2", "BT2", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bathroom 2", "BT2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Bathroom 2", "BT2", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bathroom 2", "BT2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Bathroom 2", "BT2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Bathroom 2", "BT2", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def bathroomMotionHandler3(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Bathroom 3", "BT3", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bathroom 3", "BT3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Bathroom 3", "BT3", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Bathroom 3", "BT3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Bathroom 3", "BT3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Bathroom 3", "BT3", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def kitchenMotionHandler1(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Kitchen", "KI", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Kitchen", "KI", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Kitchen", "KI", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Kitchen", "KI", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Kitchen", "KI", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Kitchen", "KI", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def kitchenMotionHandler2(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Kitchen 2", "KI2", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Kitchen 2", "KI2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Kitchen 2", "KI2", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Kitchen 2", "KI2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Kitchen 2", "KI2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Kitchen 2", "KI2", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def kitchenMotionHandler3(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Kitchen 3", "KI3", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Kitchen 3", "KI3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Kitchen 3", "KI3", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Kitchen 3", "KI3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Kitchen 3", "KI3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Kitchen 3", "KI3", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def livingMotionHandler1(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Livingroom", "LR", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Livingroom", "LR", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Livingroom", "LR", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Livingroom", "LR", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Livingroom", "LR", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Livingroom", "LR", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}

def livingMotionHandler2(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Livingroom 2", "LR2", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Livingroom 2", "LR2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Livingroom 2", "LR2", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Livingroom 2", "LR2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Livingroom 2", "LR2", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Livingroom 2", "LR2", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    
    }
}

def livingMotionHandler3(evt) {
    def now = new Date().format("dd MMM yyyy hh:mm:ss a", location.timeZone)
    def ManuCode = ""
    def ProdCode = ""
    def ProdTypeCode = ""
    def wConfigCode = ""
    def BatteryStatus = ""
    
    if (evt.device?.hasAttribute("ManufacturerCode")) {	ManuCode = evt.device.currentState("ManufacturerCode").stringValue	}
    if (evt.device?.hasAttribute("ProductCode")) {	ProdCode = evt.device.currentState("ProductCode").stringValue	}
    if (evt.device?.hasAttribute("ProduceTypeCode")) {	ProdTypeCode = evt.device.currentState("ProduceTypeCode").stringValue	}
    if (evt.device?.hasAttribute("WirelessConfig")) {	wConfigCode = evt.device.currentState("WirelessConfig").stringValue	}
    if (evt.device?.hasAttribute("battery")) {	BatteryStatus = evt.device.currentState("battery").stringValue	}
    
  	if("active" == evt.value) {
        postToASTServer("SmartApp", "Livingroom 3", "LR3", "20004", "Sensor On", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Livingroom 3", "LR3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
  	} else if("inactive" == evt.value) {
        postToASTServer("SmartApp", "Livingroom 3", "LR3", "20005", "Sensor Off", now, "11", evt.displayName, "none", ManuCode, ProdCode, ProdTypeCode, wConfigCode)    
        postToASTServer("SmartApp", "Livingroom 3", "LR3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    } else if("wakeup" == evt.value) {
        postToASTServer("SmartApp", "Livingroom 3", "LR3", "20010", "Sensor Update Data", now, "11", evt.displayName, "Wake Up Notification", ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    	postToASTServer("SmartApp", "Livingroom 3", "LR3", "20009", "Battery Report", now, "3", evt.displayName, BatteryStatus, ManuCode, ProdCode, ProdTypeCode, wConfigCode)  
    }
}


def postToASTServer(deviceId, zone, zoneCode, eventTypeId, eventTypeName, addedTime, nodeId, nodeName, extraData, manufacturerCode, productCode, productTypeCode, wConfigCode){
        def hub = location.hubs[0]  
        if (hub.type.toString() == "VIRTUAL") {hub = location.hubs[1]	}
        def params = [
        uri: "http://lifecare2.eyeorcas.com/eyeCare/eyeCareEventDetectedDev.php",
        body: [
            homeId: "SmartThing",
            extraData: extraData,
            eventTypeName: eventTypeName,
            eventTypeId: eventTypeId,
            nodeName: nodeName,
            nodeId: nodeId,
            zone: zone,
            time: addedTime,
            zoneCode: zoneCode,
            deviceId: hub.zigbeeId,
            manufacturerCode: manufacturerCode,
            productCode: productCode,
            productTypeCode: productTypeCode,
            wConfigCode: wConfigCode
        	]
    	]
           
        try {
            httpPostJson(params) 
            log.debug "Data Posted to Ast server"
        } catch (e) {
            log.debug "something went wrong: $e"
        }
}


def validatePage() {
	if (settings.singnetId.equalsIgnoreCase(settings.singnetIdConfirm) && !(settings.singnetId == null)) {
        def deviceListjsonStr = getDeviceList()
    	def dataReturnError = registerSamsungGateway(deviceListjsonStr)    

		if (dataReturnError){
        	def pageProperties = [
                name: "validatePage",
                title: "Validation",
                nextPage: "last",
                install: false
            ]
            return dynamicPage(pageProperties) {
                section() {
                        paragraph dataReturnError
                }
            }
        }else{
        	def pageProperties = [
                name: "validatePage",
                title: "Validation",
                install: true,
                uninstall: true
    		]
            return dynamicPage(pageProperties) {
                section() {
                        paragraph "Press 'Done' to complete Setup"
                }
            }        
        }
	} else {
		def pageProperties = [
    		name: "validatePage",
			title: "Validation",
           	nextPage: "last",
        	install: false,
			uninstall: true
    		]

    		return dynamicPage(pageProperties) {
    			section() {
    				paragraph "The Singnet ID don't match. Press 'Next' to re-enter Singnet ID."
        		}
		}
	}
}


def checkAuthorizedDoorSensor() {
	def ManuCode
    def unauthorizedDevice
    settings.door.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    if (unauthorizedDevice){
       	def pageProperties = [
            name: "checkAuthorizedDoorSensor",
            title: "Validation",
            nextPage: "door",             
            install: false,
            uninstall: true
    	]
        return dynamicPage(pageProperties) {
            section() {
                    paragraph "$unauthorizedDevice is not an authorized sensor"
            }
        }     
    }else{
        def pageProperties = [
        	name: "checkAuthorizedDoorSensor", 
            title: "Livingroom Motion Sensor", 
            nextPage: "checkAuthorizedLivingroomSensor", 
            install: false,
            uninstall: true
        ] 
        return dynamicPage(pageProperties) {
            section {
                input "livingMotion1", "capability.motionSensor", title: "Select Sensor for Livingroom 1", required: false, multiple: true
                input "livingMotion2", "capability.motionSensor", title: "Select Sensor for Livingroom 2", required: false, multiple: true
                input "livingMotion3", "capability.motionSensor", title: "Select Sensor for Livingroom 3", required: false, multiple: true
           }
        }        
    }
}


def checkAuthorizedLivingroomSensor() {
	def ManuCode
    def unauthorizedDevice
    settings.livingMotion1.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    settings.livingMotion2.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    settings.livingMotion3.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    if (unauthorizedDevice){
       	def pageProperties = [
            name: "checkAuthorizedLivingroomSensor",
            title: "Validation",
            nextPage: "checkAuthorizedDoorSensor",             
            install: false,
            uninstall: true
    	]
        return dynamicPage(pageProperties) {
            section() {
                    paragraph "$unauthorizedDevice is not an authorized sensor"
            }
        }     
    }else{
        def pageProperties = [
        	name: "checkAuthorizedLivingroomSensor", 
            title: "Bedroom Motion Sensor", 
            nextPage: "checkAuthorizedBedroomSensor", 
            install: false,
            uninstall: true
        ] 
        return dynamicPage(pageProperties) {
            section {
                input "bedroomMotion1", "capability.motionSensor", title: "Select Sensor for Bedroom 1", required: false, multiple: true
                input "bedroomMotion2", "capability.motionSensor", title: "Select Sensor for Bedroom 2", required: false, multiple: true
                input "bedroomMotion3", "capability.motionSensor", title: "Select Sensor for Bedroom 3", required: false, multiple: true
                input "masterBedroomMotion", "capability.motionSensor", title: "Select Sensor for Master Bedroom", required: false, multiple: true
            }
        }        
    }
}


def checkAuthorizedBedroomSensor() {
	def ManuCode
    def unauthorizedDevice
    settings.bedroomMotion1.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    settings.bedroomMotion2.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    settings.bedroomMotion3.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    if (unauthorizedDevice){
       	def pageProperties = [
            name: "checkAuthorizedBedroomSensor",
            title: "Validation",
            nextPage: "checkAuthorizedLivingroomSensor",             
            install: false,
            uninstall: true
    	]
        return dynamicPage(pageProperties) {
            section() {
                    paragraph "$unauthorizedDevice is not an authorized sensor"
            }
        }     
    }else{
        def pageProperties = [
        	name: "checkAuthorizedBedroomSensor", 
            title: "Bathroom Motion Sensor", 
            nextPage: "checkAuthorizedBathroomSensor", 
            install: false,
            uninstall: true
        ] 
        return dynamicPage(pageProperties) {
            section {
                input "bathroomMotion1", "capability.motionSensor", title: "Select Sensor for Bathroom 1", required: false, multiple: true
                input "bathroomMotion2", "capability.motionSensor", title: "Select Sensor for Bathroom 2", required: false, multiple: true
                input "bathroomMotion3", "capability.motionSensor", title: "Select Sensor for Bathroom 3", required: false, multiple: true
            }
        }        
    }
}


def checkAuthorizedBathroomSensor() {
	def ManuCode
    def unauthorizedDevice
    settings.bathroomMotion1.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    settings.bathroomMotion2.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    settings.bathroomMotion3.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    if (unauthorizedDevice){
       	def pageProperties = [
            name: "checkAuthorizedBathroomSensor",
            title: "Validation",
            nextPage: "checkAuthorizedBedroomSensor",             
            install: false,
            uninstall: true
    	]
        return dynamicPage(pageProperties) {
            section() {
                    paragraph "$unauthorizedDevice is not an authorized sensor"
            }
        }     
    }else{
        def pageProperties = [
        	name: "checkAuthorizedBathroomSensor", 
            title: "Kitchen Motion Sensor", 
            nextPage: "checkAuthorizedKitchenSensor", 
            install: false,
            uninstall: true
        ] 
        return dynamicPage(pageProperties) {
            section {
                input "kitchenMotion1", "capability.motionSensor", title: "Select Sensor for Kitchen 1", required: false, multiple: true
                input "kitchenMotion2", "capability.motionSensor", title: "Select Sensor for Kitchen 2", required: false, multiple: true
                input "kitchenMotion3", "capability.motionSensor", title: "Select Sensor for Kitchen 3", required: false, multiple: true
            }
        }        
    }
}


def checkAuthorizedKitchenSensor() {
	def ManuCode
    def unauthorizedDevice
    settings.kitchenMotion1.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    settings.kitchenMotion2.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    settings.kitchenMotion3.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    if (unauthorizedDevice){
       	def pageProperties = [
            name: "checkAuthorizedKitchenSensor",
            title: "Validation",
            nextPage: "checkAuthorizedBathroomSensor",             
            install: false,
            uninstall: true
    	]
        return dynamicPage(pageProperties) {
            section() {
                    paragraph "$unauthorizedDevice is not an authorized sensor"
            }
        }     
    }else{
        def pageProperties = [
        	name: "checkAuthorizedKitchenSensor", 
            title: "Panic Button", 
            nextPage: "checkAuthorizedPanicButton", 
            install: false,
            uninstall: true
        ] 
        return dynamicPage(pageProperties) {
            section {
                input "panicButton", "capability.button", title: "Select Panic Button", required: false, multiple: true
            }
        }        
    }
}


def checkAuthorizedPanicButton() {
	def ManuCode
    def unauthorizedDevice
    settings.panicButton.each() {
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
       	}catch (e){
       		unauthorizedDevice = it.displayName
       	}    
    }
    
    if (unauthorizedDevice){
       	def pageProperties = [
            name: "checkAuthorizedPanicButton",
            title: "Validation",
            nextPage: "checkAuthorizedKitchenSensor",             
            install: false,
            uninstall: true
    	]
        return dynamicPage(pageProperties) {
            section() {
                    paragraph "$unauthorizedDevice is not an authorized sensor"
            }
        }     
    }else{
        def pageProperties = [
        	name: "checkAuthorizedPanicButton", 
            title: "Please Enter SmartHomeID", 
            nextPage: "validatePage", 
            install: false,
            uninstall: true
        ] 
        return dynamicPage(pageProperties) {
            section {
                input(name: "singnetId", type: "text",
                  title: "Enter SmartHomeID",
                  required: true,
                  multiple: false)
                input(name: "singnetIdConfirm", type: "text",
                  title: "Confirm SmartHomeID",
                  required: true,
                  multiple: false)  
            }
        }    
    } 
}


def registerSamsungGateway(deviceList){
        def hub = location.hubs[0]  
        if (hub.type.toString() == "VIRTUAL") {hub = location.hubs[1]	}
        def params = [
        uri: "http://dev.lifecare.sg/mlifecare/singtel/commissionSystem",
        body: [
            SamsungGatewayId: hub.zigbeeId,
            SamsungDevices: deviceList,
            SingnetId: settings.singnetId,            
            Token: getToken(),
            AppId: getAppID()
        	]
        ]
           
        try {
            httpPostJson(params) { resp ->
                log.debug "registerSamsungGateway response data: ${resp.data}"
                def responseData = resp.data.toString()
                if(responseData.contains("ErrorCode:500") || responseData.contains("ErrorCode:401")){
                    def responseDataErrorDesc = resp.data.ErrorDesc.toString()
                	return responseDataErrorDesc
                }
            }
        } catch (e) {
            log.debug "something went wrong: $e"
            return "something went wrong: $e"
        }
}


def deregisterSamsungGateway(){
        def hub = location.hubs[0]  
        if (hub.type.toString() == "VIRTUAL") {hub = location.hubs[1]	}
        def params = [
        uri: "http://dev.lifecare.sg/mlifecare/singtel/decommissionSystem",
        body: [
            SamsungGatewayId: hub.zigbeeId,
            SingnetId: settings.singnetId,            
            Token: getToken(),
            AppId: getAppID()
        	]
    	]
           
        try {
            httpPostJson(params)
            log.debug "deregisterSamsungGateway"
        } catch (e) {
            log.debug "something went wrong: $e"
        }
}

def getDeviceList(){
    def deviceList = []    //AN empty list to store all the device
    
    settings.door.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "LR", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.livingMotion1.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "LR", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.livingMotion2.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "LR2", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.livingMotion3.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "LR3", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    
    settings.bedroomMotion1.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "BR", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.bedroomMotion2.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "BR2", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.bedroomMotion3.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "BR3", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.masterBedroomMotion.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "MB", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }    
    
    
    settings.kitchenMotion1.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "KI", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.kitchenMotion2.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "KI2", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.kitchenMotion3.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "KI3", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    
    settings.bathroomMotion1.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "BT1", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.bathroomMotion2.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""

		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}    
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "BT2", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.bathroomMotion3.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	}      
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "BT3", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
    
    settings.panicButton.each() {
        def ManuCode = ""
        def ProdCode = ""
        def ProdTypeCode = ""
        def WirelessConfig = ""
		try{
            ManuCode = it.device.currentState("ManufacturerCode").stringValue
            ProdCode = it.device.currentState("ProductCode").stringValue	
            ProdTypeCode = it.device.currentState("ProduceTypeCode").stringValue
            WirelessConfig = it.device.currentState("WirelessConfig").stringValue
       	}catch (e){
       		log.debug "Manufacturer Code Not Found"
       	} 
     	deviceList.push([DeviceName: it.displayName, ZoneCode: "OT", WConfigCode: WirelessConfig, SystemManufacturerCode: ManuCode, SystemProductCode: ProdCode, SystemProductTypeCode: ProdTypeCode])
    }
      
    def builder = new groovy.json.JsonBuilder(deviceList)
	def jsonStr = builder.toString()
    
    return jsonStr

}