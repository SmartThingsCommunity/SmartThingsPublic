/**
 *  Space Weather 
 *    Read more about our space weather at http://www.swpc.noaa.gov/
 */
metadata {
	definition (name: "Space Weather", namespace: "KristopherKubicki", author: "kristopher@acm.org") {

		capability "Sensor"
		capability "Polling"
        
		attribute "Geomagnetic Storm", "number"
		attribute "Solar Radiation Storm", "number"
		attribute "Radio Blackout", "number"

		command "refresh"
	}

	tiles {
		valueTile("geomagnetic", "device.geomagnetic") {
			state "default", label:'G${currentValue}',
				backgroundColors:[
					[value: 0, color: "#92D050"],
					[value: 1, color: "#F6EB14"],
					[value: 2, color: "#FFC800"],
					[value: 3, color: "#FF9600"],
					[value: 4, color: "#FF0000"],
					[value: 5, color: "#C80000"]
				]
		}

		valueTile("radiation", "device.radiation") {
			state "default", label:'S${currentValue}',
				backgroundColors:[
					[value: 0, color: "#92D050"],
					[value: 1, color: "#F6EB14"],
					[value: 2, color: "#FFC800"],
					[value: 3, color: "#FF9600"],
					[value: 4, color: "#FF0000"],
					[value: 5, color: "#C80000"]
				]
		}
	
		valueTile("radioblackout", "device.radioblackout") {
			state "default", label:'R${currentValue}',
				backgroundColors:[
					[value: 0, color: "#92D050"],
					[value: 1, color: "#F6EB14"],
					[value: 2, color: "#FFC800"],
					[value: 3, color: "#FF9600"],
					[value: 4, color: "#FF0000"],
					[value: 5, color: "#C80000"]
				]
		}
        standardTile("poll", "device.poll", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "poll", label: "", action: "polling.poll", icon: "st.secondary.refresh", backgroundColor: "#FFFFFF"
		}

		main(["geomagnetic", "radiation","radioblackout"])
		details(["geomagnetic", "radiation","radioblackout","poll"])}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def installed() {
	runPeriodically(3600, poll)
}

def uninstalled() {
	unschedule()
}


def poll() {
        
	def pollParams = [
        uri: "http://services.swpc.noaa.gov",
        path: "/products/noaa-scales.json",
        requestContentType: "application/json",
        query: [format:"json",body: jsonRequestBody]
        ]

        httpGet(pollParams) { resp ->
        	sendEvent(name: "geomagnetic", value: resp.data['0'].G.Scale)
            sendEvent(name: "radiation", value: resp.data['0'].S.Scale)
            sendEvent(name: "radioblackout", value: resp.data['0'].R.Scale) 
            log.debug "${resp.data['0']}";
        }
}

def refresh() {
	poll()
}

def configure() {
	poll()
}

