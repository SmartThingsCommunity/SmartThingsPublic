/*
	Zone Motion Manager
    
 	Author: Mike Maxwell 2016
	    
	This software if free for Private Use. You may use and modify the software without distributing it.
 
	This software and derivatives may not be used for commercial purposes.
	You may not distribute or sublicense this software.
	You may not grant a sublicense to modify and distribute this software to third parties not included in the license.

	Software is provided without warranty and the software author/license owner cannot be held liable for damages.        
        
*/

definition(
    name: "Zone Motion Manager",
    singleInstance: true,
    namespace: "MikeMaxwell",
    author: "Mike Maxwell",
    description: "Installs and triggers a Simulated Motion Sensor using multiple physical motion sensors, optional inputs and triggers to enable zone, three zone types are available depending on the use case.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Solution/areas.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Solution/areas@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Solution/areas@2x.png")


preferences {
	page(name: "main")	
}

def main(){
	def installed = app.installationState == "COMPLETE"
	return dynamicPage(
    	name		: "main"
        ,title		: "Motion Control Zones"
        ,install	: true
        ,uninstall	: installed
        ){
 
            if (installed){
            	section {
                    app(name: "childZones", appName: "zoneMotionChild", namespace: "MikeMaxwell", title: "Create New Motion Zone...", multiple: true)
            	}
				section (getVersionInfo()) { }
            } else {
            	section(){
                	paragraph("Tap done to finish the initial installation.\nRe-open the app from the smartApps flyout to create your motion zones.")
                }
            }
        }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
	state.vParent = "2.0.0"
}
def getVersionInfo(){
	return "Versions:\n\tZone Motion Manager: ${state.vParent ?: "No data available yet."}\n\tZone Configuration: ${state.vChild ?: "No data available yet."}"
}

def updateVer(vChild){
    state.vChild = vChild
}