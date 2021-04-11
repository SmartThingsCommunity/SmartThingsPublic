metadata {
	definition(name: "Tasmota-Base", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.smartplug") {
		// define appropriate capabilities, commands, etc. here
	}

	// UI tile definitions
	tiles(scale: 2) {
		valueTile("name", "device.label", width: 2, height: 2) {
            state "val", label:'My Device', defaultState: true
        }
		main "name"
		details(["name"])
	}
}

def initializeChild(Map options){
    // when the 'Master' device creates child devices, this method is called passing configuration
}

def updateStatus(status){
    // when the 'Master' device refreshes it passes the retrieved status to all children, thus calling this method
    // update the status of this device accordingly
}
