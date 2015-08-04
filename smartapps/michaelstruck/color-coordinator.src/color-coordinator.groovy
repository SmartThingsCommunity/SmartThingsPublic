/**
 * 	Color Coordinator 
 *  Version 1.0.0 - 7/4/15
 *  By Michael Struck
 *
 *  1.0.0 - Initial release
 *
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
	name: "Color Coordinator",
	namespace: "MichaelStruck",
	author: "Michael Struck",
	description: "Ties multiple colored lights to one specific light's settings",
	category: "Convenience",
	iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/ColorCoordinator/CC.png",
	iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/ColorCoordinator/CC@2x.png"
)

preferences {
	page name: "mainPage"
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
		section("Master Light") {
			input "master", "capability.colorControl", title: "Colored Light"
		}
		section("Lights that follow the master settings") {
			input "slaves", "capability.colorControl", title: "Colored Lights",  multiple: true, required: false
		}
    	section([mobileOnly:true], "Options") {
			label(title: "Assign a name", required: false)
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license and instructions"
        }
	}
}

page(name: "pageAbout", title: "About ${textAppName()}") {
	section {
    	paragraph "${textVersion()}\n${textCopyright()}\n\n${textLicense()}\n"
	}
	section("Instructions") {
		paragraph textHelp()
	}
}

def installed() {   
	init() 
}

def updated(){
	unsubscribe()
    init()
}

def init() {
	subscribe(master, "switch", onOffHandler)
	subscribe(master, "level", colorHandler)
    subscribe(master, "hue", colorHandler)
    subscribe(master, "saturation", colorHandler)
    subscribe(master, "colorTemperature", tempHandler)
}
//-----------------------------------
def onOffHandler(evt){
	if (master.currentValue("switch") == "on"){
    	slaves?.on()
    }
    else {
		slaves?.off()  
    }
}

def colorHandler(evt) {
   	def dimLevel = master.currentValue("level")
    def hueLevel = master.currentValue("hue")
    def saturationLevel = master.currentValue("saturation")
	def newValue = [hue: hueLevel, saturation: saturationLevel, level: dimLevel as Integer]
    slaves?.setColor(newValue)
}

def tempHandler(evt){
    if (evt.value != "--") {
    	def tempLevel = master.currentValue("colorTemperature")
    	slaves?.setColorTemperature(tempLevel)
    }
}

//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Color Coordinator"
}	

private def textVersion() {
    def text = "Version 1.0.0 (07/04/2015)"
}

private def textCopyright() {
    def text = "Copyright © 2015 Michael Struck"
}

private def textLicense() {
    def text =
		"Licensed under the Apache License, Version 2.0 (the 'License'); "+
		"you may not use this file except in compliance with the License. "+
		"You may obtain a copy of the License at"+
		"\n\n"+
		"    http://www.apache.org/licenses/LICENSE-2.0"+
		"\n\n"+
		"Unless required by applicable law or agreed to in writing, software "+
		"distributed under the License is distributed on an 'AS IS' BASIS, "+
		"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "+
		"See the License for the specific language governing permissions and "+
		"limitations under the License."
}

private def textHelp() {
	def text =
    	"This application will allow you to control the settings of multiple colored lights with one control. " +
        "Simply choose a master control light, and then choose the lights that will follow the settings of the master, "+
        "including on/off conditions, hue, saturation, level and color temperature."
}