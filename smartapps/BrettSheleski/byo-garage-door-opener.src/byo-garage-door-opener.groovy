
definition(
    name: "BYO Garage Door Opener",
    namespace: "BrettSheleski",
    author: "Brett Sheleski",
    description: "Bring Your Own Garage Door Opener by specifying real sensors to control be controlled by a virtial garage door opener device.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {

	section("Input Devices") {
		input "inputSensor", "capability.contactSensor", title: "Sensor", required: true, multiple: false
		input "outputButton", "capability.momentary", title: "Button", required: true, multiple: false
	}

	section("Output Device") {
		input "garageDoorOpener", "capability.garageDoorControl", title: "Garage Door Opener", required: true, multiple: false
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

	subscribeToCommand(garageDoorOpener, "open", garageDoorOpen);
	subscribeToCommand(garageDoorOpener, "close", garageDoorClose);

	sendEvent(garageDoorOpener, name: "door", value: "unknown");
}

def handleSensorOpened(evt){
	sendEvent(garageDoorOpener, name: "door", value: "open");
}

def handleSensorClosed(evt){
	sendEvent(garageDoorOpener, name: "door", value: "closed");
}

def garageDoorOpen(evt){
	if (!isDoorOpen()){
		outputButton.push();
		sendEvent(garageDoorOpener, name: "door", value: "opening");
	}
}

def garageDoorClose(evt){
	if (!isDoorClosed()){
		outputButton.push();
		sendEvent(garageDoorOpener, name: "door", value: "closing");
	}
}

def isDoorOpen(){
	return inputSensor.currentValue("contact") == "open";
}

def isDoorClosed(){
	return inputSensor.currentValue("contact") == "closed";
}