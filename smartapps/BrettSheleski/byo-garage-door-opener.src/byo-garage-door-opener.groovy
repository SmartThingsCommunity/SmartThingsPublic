
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
		input "inputSensor", "capability.contactSensor", title: "Sensor", required: true, multiple: false
		input "outputButton", "capability.momentary", title: "Button", required: true, multiple: false
	}

	section ("Device ID"){
		input "garageDoorNetworkId", "string", title:"Device ID", required: true, defaultValue: UUID.randomUUID().toString()
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
	subscribe(inputSensor, "contact.open", handleSensorOpened);
	subscribe(inputSensor, "contact.close", handleSensorClosed);

	def garageDoorOpener = getGarageDoorOpener();

	if (garageDoorOpener == null){
		addChildDevice("BrettSheleski", "BYO Garage Door Opener", garageDoorNetworkId);
	}
	
	subscribeToCommand(garageDoorOpener, "open", garageDoorOpen);
	subscribeToCommand(garageDoorOpener, "close", garageDoorClose);

	garageDoorOpener.sendEvent(name: "door", value: "unknown");
}

def getGarageDoorOpener(){
	getChildDevices(true).each
	{
		if (it.getDeviceNetworkId() == garageDoorNetworkId)
        {
			return it;
        }
	}

    return null;
}

def handleSensorOpened(evt){

	def garageDoorOpener = getGarageDoorOpener();

	garageDoorOpener.sendEvent(name: "door", value: "open");
}

def handleSensorClosed(evt){

	def garageDoorOpener = getGarageDoorOpener();

	garageDoorOpener.sendEvent(name: "door", value: "closed");
}

def garageDoorOpen(evt){
	if (!isDoorOpen()){
		outputButton.push();

		def garageDoorOpener = getGarageDoorOpener();

		garageDoorOpener.sendEvent(name: "door", value: "opening");
	}
}

def garageDoorClose(evt){
	if (!isDoorClosed()){
		outputButton.push();

		def garageDoorOpener = getGarageDoorOpener();

		garageDoorOpener.sendEvent(name: "door", value: "closing");
	}
}

def isDoorOpen(){
	return inputSensor.currentValue("contact") == "open";
}

def isDoorClosed(){
	return inputSensor.currentValue("contact") == "closed";
}