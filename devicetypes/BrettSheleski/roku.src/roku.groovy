metadata {
    definition(name: "Roku", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "Momentary"

        attribute "ipAddress", "string"

        command "keyPress_home"
        command "keyPress_Rev"
        command "keyPress_Fwd"
        command "keyPress_Play"
        command "keyPress_Select"
        command "keyPress_Left"
        command "keyPress_Right"
        command "keyPress_Down"
        command "keyPress_Up"
        command "keyPress_Back"
        command "keyPress_InstantReplay"
        command "keyPress_InfoBackspace"
        command "keyPress_Search"
        command "keyPress_Enter"
        
    }

	// UI tile definitions
	 tiles(scale: 2) {
        standardTile("home", "device.switch", width: 2, height: 2) {
            state "home", label: 'Home', action: "keyPress_home", icon: "st.Home.home2"
        }

        main("home")

        details(["home"])
    }

     preferences {
        input "rokuIpAddress", "string", title: "IP Address", description: "IP Address of Roku", displayDuringSetup: true, required: true
        input "rokuPort", "string", title: "Port", description: "Port", displayDuringSetup: true, defaultValue: "8060", required: true
    }
}

def parse(String description) {
    log.debug "parse()"
}


def keyPress_home() {
    remoteKeyPress("home")
}
def keyPress_Rev() {
    remoteKeyFwd("Rev")
}
def keyPress_Fwd() {
    remoteKeyPress("Fwd")
}
def keyPress_Play() {
    remoteKeyPress("Play")
}
def keyPress_Select() {
    remoteKeyPress("Select")
}
def keyPress_Left() {
    remoteKeyPress("Left")
}
def keyPress_Right() {
    remoteKeyPress("Right")
}
def keyPress_Down() {
    remoteKeyPress("Down")
}
def keyPress_Up() {
    remoteKeyPress("Up")
}
def keyPress_Back() {
    remoteKeyPress("Back")
}
def keyPress_InstantReplay() {
    remoteKeyPress("InstantReplay")
}
def keyPress_InfoBackspace() {
    remoteKeyPress("InfoBackspace")
}
def keyPress_Search() {
    remoteKeyPress("Search")
}
def keyPress_Enter() {
    remoteKeyPress("Enter")
}


private def remoteKeyPress(String key){

    log.debug "remoteKeyPress(${key})"

    def result = new physicalgraph.device.HubAction(
        method: "POST",
        path: "/keypress/" + key,
        headers: [
            HOST: "${rokuIpAddress}:${rokuPort}"
        ]
    )

    log.debug result
}