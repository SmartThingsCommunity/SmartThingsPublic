/**
 *  Copyright 2015 SmartThings
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
 *  Smart Block
 *
 *  Author: steve
 *  Date: 2013-12-26
 */

metadata {

    definition (name: "Smart Block", namespace: "vlaminck/Minecraft", author: "SmartThings") {
		capability "Switch Level"
		capability "Switch"

		attribute "redstoneSignalStrength", "string"
		attribute "smartBlockNeighborChanged", "string"
		attribute "blockDestroyed", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	preferences {
		input title: "If you want SmartThings to update your SmartBlock, you must enter your Server's Address into your \"SmartBlock Manager\" SmartApp prior to placing the SmartBlock in Minecraft (Don't worry, it's safe to destroy a block and place it again).", type: "paragraph", element: "paragraph"
//		input name: "serverIp", type: "text", title: "Server Address", description: "Where is your server located?", required: false
	}

	tiles {
		standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', icon: "st.switches.switch.off", backgroundColor: "#ffffff", action: "switch.on", nextState: "turningOn"
			state "turningOn", label: '${name}', icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
			state "on", label: '${name}', icon: "st.switches.switch.on", backgroundColor: "#00A0DC", action: "switch.off", nextState: "turningOff"
			state "turningOff", label: '${name}', icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		valueTile("level", "device.level", height: 1, width: 1, inactiveLabel: false) {
			state "level", label: '${currentValue}%', backgroundColor: "#ffffff"
		}
		valueTile("redstoneSignalStrength", "redstoneSignalStrength", inactiveLabel: false, decoration: "flat", width: 2) {
			state "redstoneSignalStrength", label: 'Redstone Signal:\n${currentValue}', backgroundColor: "#ffffff"
		}
		standardTile("blockDestroyed", "device.blockDestroyed") {
			state "false", label: 'OK', icon: "st.Health & Wellness.health9"
			state "true", label: 'Destroyed', icon: "st.alarm.alarm.alarm"
		}
		valueTile("worldSeed", "device.worldSeed", decoration: "flat", width: 2) {
			state "worldSeed", label: 'World Seed:\n${currentValue}'
		}
		valueTile("dimensionName", "device.dimensionName", decoration: "flat", width: 2) {
			state "dimensionName", label: 'Dimension Name:\n${currentValue}'
		}
		valueTile("coordinates", "device.coordinates", decoration: "flat", width: 2) {
			state "coordinates", label: 'Block Coordinates:\n${currentValue}'
		}
		valueTile("smartBlockNeighborChanged", "device.smartBlockNeighborChanged", decoration: "flat", width: 2) {
			state "smartBlockNeighborChanged", label: 'Updated By:\n${currentValue}'
		}
		valueTile("blockServer", "device.blockServer", decoration: "flat", width: 2) {
			state "blockServer", label: 'Server Address:\n${currentValue}'
		}
		valueTile("placedBy", "device.placedBy", decoration: "flat", width: 2) {
			state "placedBy", label: 'Placed by:\n${currentValue}'
		}
		valueTile("closestPlayer", "device.closestPlayer", decoration: "flat", width: 2) {
			state "closestPlayer", label: 'Last player close by:\n${currentValue}'
		}

		main([
			"coordinates",
			"switch",
			"redstoneSignalStrength",
			"level",
			"smartBlockNeighborChanged",
			"blockDestroyed"
		])
		details([
			"coordinates",
			"switch",
			"redstoneSignalStrength",
			"level",
			"smartBlockNeighborChanged",
			"blockDestroyed",
			"placedBy",
			"closestPlayer",
			"dimensionName",
			"worldSeed",
			"blockServer"
		])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle '' attribute

}


def on() {
	sendSwitchStateToMC("on")
}

def off() {
	sendSwitchStateToMC("off")
}

def setLevel(newLevel) {
	def signal = convertLevelToSignal(newLevel as int)

	sendSignalToMC(signal)
}

def sendSignalToMC(newSignal) {
	def url = "http://${state.serverIp}:3333/block?x=${state.x}&y=${state.y}&z=${state.z}&name=level&value=${newSignal}"
	log.debug "POST to ${url}"

	httpPost(url, "foo=bar") { response ->
		content = response.data
		log.debug "response: ${content}"
	}
}

def sendSwitchStateToMC(switchState) {
	def url = "http://${state.serverIp}:3333/block?x=${state.x}&y=${state.y}&z=${state.z}&name=switch&value=${switchState}"
	log.debug "POST to ${url}"

	httpPost(url, "foo=bar") { response ->
		content = response.data
		log.debug "response: ${content}"
	}
}

def setCoordinates(x, y, z) {
	state.x = x
	state.y = y
	state.z = z
	log.debug "set block coordinates to: ${getCoordinates()}"
	sendEvent(name: "coordinates", value: getCoordinates())
}

def getCoordinates() {
	return "(${state?.x},${state?.y},${state?.z})"
}

def getCoordinate(axis) {
	if (!["x", "y", "z"].contains(axis))
	{
		return null;
	}

	return state."${axis}"
}

def isDestroyed() {
	state.destroyed
}

def setDestroyed(isDestroyed) {
	state.destroyed = isDestroyed
	sendEvent(name: "blockDestroyed", value: isDestroyed)
}

def setWorldSeed(worldSeed) {
	state.worldSeed = worldSeed
	sendEvent(name: "worldSeed", value: worldSeed)
}

def setDimensionName(dimensionName) {
	state.dimensionName = dimensionName
	sendEvent(name: "dimensionName", value: dimensionName)
}

def setPlacedBy(placedBy) {
	state.placedBy = placedBy
	sendEvent(name: "placedBy", value: placedBy)
}

def setClosestPlayer(closestPlayer) {
	state.closestPlayer = closestPlayer
	state.closestPlayerChanged = new Date()
	sendEvent(name: "closestPlayer", value: closestPlayer, isStateChange: true)
}

def setSignalStrength(int signalStrength) {
	state.signalStrength = signalStrength

	sendEvent(name: "switch", value: signalStrength > 0 ? "on" : "off")
	sendEvent(name: "redstoneSignalStrength", value: signalStrength)
	sendEvent(name: "level", value: convertSignalToLevel(signalStrength))
}

def setLastNeighborChanged(blockId, blockName) {
	state.neighborBlockId = blockId
	state.neighborBlockName = blockName
	sendEvent(name: "smartBlockNeighborChanged", value: "${blockId} ${blockName}", isStateChange: true)
}

def setServerIp(ip) {

	if (!ip)
	{
		return
	}

	ip = ip.replace("https://", "")
	ip = ip.replace("http://", "")

	def serverParts = ip.split(":")
	if (serverParts.size() > 1)
	{
		state.serverPort = serverParts[1]
	}

	if (serverParts.size() > 0)
	{
		state.serverIp = serverParts[0]
	}

	if (state.serverIp)
	{
		state.blockServer = state.serverIp
	}
	if (state.serverPort)
	{
		state.blockServer = "${state.blockServer}:${state.serverPort}"
	}

	sendEvent(name: "blockServer", value: "${state.blockServer}")

}


def calculateDNI() { // not currently used, and probably not necessary
	"${state.worldSeed}|${state.dimensionName}|${getCoordinates()}".encodeAsMD5()
}

def neighborBlockChange(data) {
// data:{signalStrength:0,blockId:55,blockName:Redstone Dust,z:321,y:63,x:-135,worldSeed:<long>,dimensionName:Overworld}

	log.debug "neighborBlockChange($data)"

	if (data?.closestPlayer)
	{
		setClosestPlayer(data?.closestPlayer)
	}

	int signalStrength = data?.signalStrength as int
	setSignalStrength(signalStrength)

	setLastNeighborChanged(data?.blockId, data?.blockName)

	if (data?.worldSeed)
	{
		setWorldSeed(data?.worldSeed)
	}

	if (data?.dimensionName)
	{
		setDimensionName(data?.dimensionName)
	}

	if (data?.placedBy)
	{
		setPlacedBy(data?.placedBy)
	}

	sendEvent(name: "coordinates", value: getCoordinates())
}

def convertLevelToSignal(int level = 0) {
	if (level <= 0) return 0
	if (level >= 99) return 15

	int signal = (15 - ((99 - level) / 7))
	log.debug "converted level to signalStrength: $signal"
	return signal
}

def convertSignalToLevel(int signal = 0) {
	if (signal <= 0) return 0
	if (signal >= 15) return 99

	int level = (99 - ((15 - signal) * 7))
	log.debug "converted level to signalStrength: $signal"
	return level
}
