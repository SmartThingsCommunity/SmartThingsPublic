metadata {
    definition(name: "Roku", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "Switch"

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
        command "keyPress_Menu"
        
    }

	// UI tile definitions
	 tiles(scale: 2) {

         valueTile("home", "device.button", width: 4, height: 1) {
             state "play", label: 'Home', action: "keyPress_home", defaultState: true
         }

        standardTile("up", "device.playButton", width: 2, height: 2) {
            state "play", label: 'Up', action: "keyPress_Up", icon: "st.Home.home2"
        }

        standardTile("down", "device.downButton", width: 2, height: 2) {
            state "play", label: 'Down', action: "keyPress_Down", icon: "st.Home.home2"
        }

        standardTile("left", "device.leftButton", width: 2, height: 2) {
            state "play", label: 'Left', action: "keyPress_Left", icon: "st.Home.home2"
        }

        standardTile("right", "device.rightButton", width: 2, height: 2) {
            state "play", label: 'Right', action: "keyPress_Right", icon: "st.Home.home2"
        }

        standardTile("select", "device.enterButton", width: 2, height: 2) {
            state "play", label: 'Select', action: "keyPress_Select", icon: "st.Home.home2"
        }

        standardTile("replay", "device.replayButton", width: 1, height: 1) {
            state "play", label: 'Replay', action: "keyPress_InstantReplay", icon: "st.Home.home2"
        }

        standardTile("asterisk", "device.asteriskButton", width: 1, height: 1) {
            state "play", label: '*', action: "keyPress_Menu", icon: "st.Home.home2"
        }

        valueTile("blank1x1", "device.doNothing1x1", width: 1, height: 1)
        valueTile("blank1x2", "device.doNothing1x2", width: 1, height: 2)
        valueTile("blank2x2", "device.doNothing2x2", width: 2, height: 2)

        standardTile("rewind", "device.asteriskButton", width: 2, height: 1) {
            state "play", label: '*', action: "keyPress_Rev", icon: "st.Home.home2"
        }

        standardTile("play", "device.asteriskButton", width: 2, height: 1) {
            state "play", label: 'Play', action: "keyPress_Play", icon: "st.Home.home2"
        }

        standardTile("fastForward", "device.asteriskButton", width: 2, height: 1) {
            state "play", label: '> >', action: "keyPress_Fwd", icon: "st.Home.home2"
        }

        main("play")

        details(["blank1x1", "home", "blank1x1",
                 "replay", "blank1x2", "up", "blank1x2", "asterisk",
                  "left", "select", "right",
                  "blank2x2", "down", "blank2x2",
                  "rewind", "play", "fastForward"])
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
def keyPress_Menu() {
    remoteKeyPress("Lit_*")
}


private def remoteKeyPress(String key){

    log.debug "remoteKeyPress(${key})"

    def hosthex = convertIPtoHex(rokuIpAddress)
    def porthex = convertPortToHex(rokuPort)

    device.deviceNetworkId = "$hosthex:$porthex" 

    def result = new physicalgraph.device.HubAction(
        method: "POST",
        path: "/keypress/" + key,
        headers: [
            HOST: "${rokuIpAddress}:${rokuPort}"
        ]
    )

    log.debug result

    return result
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

def on(){
    keyPress_Play()
}

def off(){
    keyPress_Play()
}