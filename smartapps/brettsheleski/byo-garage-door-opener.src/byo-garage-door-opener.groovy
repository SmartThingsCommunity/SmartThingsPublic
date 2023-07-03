
definition(
    name: "BYO Garage Door Opener",
    namespace: "BrettSheleski",
    author: "Brett Sheleski",
    description: "Bring Your Own Garage Door Opener by specifying real sensors to control be controlled by a virtual garage door opener device.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {

	section("Input Devices") {
		input "contactSensor", "capability.contactSensor", title: "Sensor", required: true, multiple: false
		input "button", "capability.momentary", title: "Button", required: true, multiple: false
	}
}

def installed(){
	initialize();
}

def updated(){
	unsubscribe();
	initialize();
}

def initialize(){
	subscribe(contactSensor, "contact.open", handleSensorOpened);
	subscribe(contactSensor, "contact.closed", handleSensorClosed);

	def garageDoorOpener = getGarageDoorOpener();
	
	subscribeToCommand(garageDoorOpener, "open", garageDoorOpen);
	subscribeToCommand(garageDoorOpener, "close", garageDoorClose);

	garageDoorOpener.sendEvent(name: "door", value: "unknown");
}

def getGarageDoorOpener(){
	def deviceId = "${app.id}-garage-door";

	log.debug "Device ID: ${deviceId}";

	def childDevice = childDevices.find {
		it.deviceNetworkId == deviceId
	};

	if (childDevice == null){

		def theHubId = location.hubs[0].id
        def label = null;
        def properties = [:];
		def namespace = "BrettSheleski";
		def deviceTypeName = "BYO Garage Door Opener";

		log.debug "DEVICE : ${e}"
		
		properties = [
			label : "BYO Garage Door",
			name : "BYO Garage Door",
			completedSetup : true
		];

		log.debug "Creating Child Device"

		childDevice = addChildDevice(namespace, deviceTypeName, deviceId, theHubId, properties)

		log.debug "Created Child Device: ${childDevice}"
	}
	else{
		log.debug "Found Child Device: ${childDevice}"
	}

    return childDevice;
}

def handleSensorOpened(evt){
	log.debug "The Sensor is opened"

	def garageDoorOpener = getGarageDoorOpener();

	garageDoorOpener.sendEvent(name: "door", value: "open");
}

def handleSensorClosed(evt){
	log.debug "The Sensor is closed"

	def garageDoorOpener = getGarageDoorOpener();

	garageDoorOpener.sendEvent(name: "door", value: "closed");
}

def garageDoorOpen(evt){

	log.debug "Opening the garage door"
	if (!isDoorOpen()){
		outputButton.push();

		def garageDoorOpener = getGarageDoorOpener();

		garageDoorOpener.sendEvent(name: "door", value: "opening");
	}
	else{
		log.debug "The door is already open.  Nothing to do."
	}
}

def garageDoorClose(evt){

	log.debug "Closing the garage door"
	if (!isDoorClosed()){
		outputButton.push();

		def garageDoorOpener = getGarageDoorOpener();

		garageDoorOpener.sendEvent(name: "door", value: "closing");
	}
	else{
		log.debug "The door is already closed.  Nothing to do."
	}
}

def isDoorOpen(){
	def currentVal = contactSensor.currentValue("contact")

	log.debug "Sensor state: ${currentVal}"

	return currentVal == "open";
}

def isDoorClosed(){
	def currentVal = contactSensor.currentValue("contact")

	log.debug "Sensor state: ${currentVal}"

	return currentVal == "closed";
}