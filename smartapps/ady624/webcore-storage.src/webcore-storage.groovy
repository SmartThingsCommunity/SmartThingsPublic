/*
 *  webCoRE - Community's own Rule Engine - Web Edition
 *
 *  Copyright 2016 Adrian Caramaliu <ady624("at" sign goes here)gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
public static String version() { return "v0.3.110.20191009" }
/******************************************************************************/
/*** webCoRE DEFINITION														***/
/******************************************************************************/
private static String handle() { return "webCoRE" }
definition(
	name: "${handle()} Storage",
	namespace: "ady624",
	author: "Adrian Caramaliu",
	description: "Do not install this directly, use webCoRE instead",
	parent: "ady624:${handle()}",
	category: "Convenience",
    /* icons courtesy of @chauger - thank you */
	iconUrl: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE.png",
	iconX2Url: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE@2x.png",
	iconX3Url: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE@3x.png"
)

preferences {
	//UI pages
	page(name: "pageSettings")
	page(name: "pageSelectDevices")
	page(name: "pageSelectMoreDevices1")
	page(name: "pageSelectMoreDevices2")
	page(name: "pageSelectMoreDevices3")
}


/******************************************************************************/
/*** 																		***/
/*** CONFIGURATION PAGES													***/
/*** 																		***/
/******************************************************************************/
def pageSettings() {
    //clear devices cache
	dynamicPage(name: "pageSettings", title: "", install: false, uninstall: false) {
        if (!parent || !parent.isInstalled()) {
            section() {
                paragraph "Sorry, you cannot install a piston directly from the Marketplace, please use the webCoRE SmartApp instead."
            }
            section("Installing webCoRE") {
                paragraph "If you are trying to install webCoRE, please go back one step and choose webCoRE, not webCoRE Piston. You can also visit wiki.webcore.co for more information on how to install and use webCoRE"
                if (parent) href "", title: "More information", description: parent.getWikiUrl(), style: "external", url: parent.getWikiUrl(), image: "https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE.png", required: false
            }
        } else {
            section("Available devices") {
                href "pageSelectDevices", title: "Available devices", description: "Tap here to select which devices are available to pistons"
            }
        }
	}
}
private pageSelectDevices() {
	parent.refreshDevices()
	dynamicPage(name: "pageSelectDevices", title: "") {
		section() {
			paragraph "Select the devices you want ${handle()} to have access to."
            paragraph "It is a good idea to only select the devices you plan on using with ${handle()} pistons. Pistons will only have access to the devices you selected."
        }

		section ('Select devices by type') {
        	paragraph "Most devices should fall into one of these two categories"
			input "dev:actuator", "capability.actuator", multiple: true, title: "Which actuators", required: false, submitOnChange: true
			input "dev:sensor", "capability.sensor", multiple: true, title: "Which sensors", required: false, submitOnChange: true
		}
        
        section ('Select devices by capability') {
			def capSegments = capabilitiesSegments()
			capSegments.eachWithIndex { capabilities, page ->
				href "pageSelectMoreDevices${page + 1}", title: "Capability group ${page + 1}", description: "${capabilities.values()[0].d} through ${capabilities.values()[-1].d}"
			}
        }
	}
}

private pageSelectMoreDevices(page) {
	dynamicPage(name: "pageSelectMoreDevices${page}", title: "") {
		section ("Select devices by capability (group ${page})") {
			for (capability in capabilitiesSegments()[page - 1]) {
				input "dev:${capability.key}", "capability.${capability.key}", multiple: true, title: "Which ${capability.value.d}", required: false, submitOnChange: true
			}
		}
	}
}

private pageSelectMoreDevices1() {
	pageSelectMoreDevices(1)
}

private pageSelectMoreDevices2() {
	pageSelectMoreDevices(2)
}

private pageSelectMoreDevices3() {
	pageSelectMoreDevices(3)
}

private capabilitiesSegments(segments = 3) {
	def caps = parent.capabilities().findAll{ (!(it.value.d in [null, 'actuators', 'sensors'])) }.sort{ it.value.d }
	def capsPerPage = caps.size() / segments as Integer
	def keys = caps.keySet() as List
	return keys.collate(capsPerPage).collect{ caps.subMap(it) }
}

/******************************************************************************/
/*** 																		***/
/*** INITIALIZATION ROUTINES												***/
/*** 																		***/
/******************************************************************************/


private installed() {
	initialize()
	return true
}

private updated() {
	unsubscribe()
	initialize()
	return true
}

private initialize() {
	//update parent
    parent.refreshDevices()
}


/******************************************************************************/
/*** 																		***/
/*** PUBLIC METHODS															***/
/*** 																		***/
/******************************************************************************/

def initData(devices, contacts) {
    if (devices) {
		for(item in devices) {
	    	if (item) {
	    		def deviceType = item.key.replace('dev:', 'capability.')
	    		def deviceIdList = item.value.collect{ it.id }
	    		app.updateSetting(item.key, [type: deviceType, value: deviceIdList])
	        }
	    }
	}
}

def Map listAvailableDevices(raw = false, offset = 0) {
	def time = now()
	def response = [:]
	def devices = settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().sort{ it.getDisplayName() }
	def deviceCount = devices.size()
	if (raw) {
		response = devices.collectEntries{ dev -> [(hashId(dev.id)): dev]}
	} else {
		devices = devices[offset..-1]
		response.devices = [:]
		response.complete = !devices.indexed().find{ idx, dev ->
			response.devices[hashId(dev.id)] = [
				n: dev.getDisplayName(), 
				cn: dev.getCapabilities()*.name, 
				a: dev.getSupportedAttributes().unique{ it.name }.collect{[
					n: it.name, 
					t: it.getDataType(), 
					o: it.getValues()
				]}, 
				c: dev.getSupportedCommands().unique{ it.getName() }.collect{[
					n: it.getName(), 
					p: it.getArguments()
				]} 
			]
			// Stop after 10 seconds
			if (idx < devices.size() - 1 && now() - time > 10000) {
				response.nextOffset = offset + idx + 1
				return true
			}
			false
		}
	}
	log.debug "Generated list of ${offet}-${offset + devices.size()} of ${deviceCount} devices in ${now() - time}ms. Data size is ${response.toString().size()}"
	return response
}

def Map getDashboardData() {
    boolean ok
    def value
    def item
    def start = now()
	return settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().collectEntries{ dev -> [(hashId(dev.id)): dev]}.collectEntries{ id, dev ->
        [ (id): dev.getSupportedAttributes().collect{ it.name }.unique().collectEntries{
	    	try { value = dev.currentValue(it); } catch (all) { value = null};
			return [ (it) : value]
	    }]
    }
}

public String mem(showBytes = true) {
	def bytes = state.toString().length()
	return Math.round(100.00 * (bytes/ 100000.00)) + "%${showBytes ? " ($bytes bytes)" : ""}"
}

/******************************************************************************/
/***																		***/
/*** SECURITY METHODS														***/
/***																		***/
/******************************************************************************/
def String md5(String md5) {
   try {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5")
        byte[] array = md.digest(md5.getBytes())
        def result = ""
        for (int i = 0; i < array.length; ++i) {
          result += Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3)
       }
        return result
    } catch (java.security.NoSuchAlgorithmException e) {
    }
    return null;
}

def String hashId(id) {
	//enabled hash caching for faster processing
	def result = state.hash ? state.hash[id] : null
    if (!result) {
		result = ":${md5("core." + id)}:"
        def hash = state.hash ?: [:]
        hash[id] = result
        state.hash = hash
    }
    return result
}

/******************************************************************************/
/***																		***/
/*** END OF CODE															***/
/***																		***/
/******************************************************************************/