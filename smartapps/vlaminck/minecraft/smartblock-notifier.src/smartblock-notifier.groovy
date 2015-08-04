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
 *  SmartBlock Notifier
 *
 *  Author: Steve Vlaminck
 *
 *  Date: 2013-12-27
 */

definition(
    name: "SmartBlock Notifier",
    namespace: "vlaminck/Minecraft",
    author: "SmartThings",
    description: "A SmartApp that notifies you when things are happening around your SmartBlocks",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	page(name: "firstPage")
	page(name: "redstonePage")
	page(name: "neighborBlockPage")
	page(name: "messageBuilderPage")
	page(name: "destroyedPage")
}

def firstPage() {

	def defaultLabelValue = smartBlock ? (smartBlock.label ?: smartBlock.name) : null


	def destroyedPageName = "destroyedPage"
	def destroyedComplete = pageStateComplete(destroyedPageName)
	def destroyedState = destroyedComplete ? "complete" : null
	def destroyedDescription = destroyedComplete ? messageDescriptionForPage(destroyedPageName) : null

	def redstonePageName = "redstonePage"
	def redstoneComplete = pageStateComplete(redstonePageName)
	def redstoneState = redstoneComplete ? "complete" : null
	def redstoneDescription = redstoneComplete ? messageDescriptionForPage(redstonePageName) : null

	def neighborPageName = "neighborBlockPage"
	def neighborComplete = pageStateComplete(neighborPageName)
	def neighborState = neighborComplete ? "complete" : null
	def neighborDescription = neighborComplete ? messageDescriptionForPage(neighborPageNamePageName) : null

	dynamicPage(name: "firstPage", title: "Setup your notifications", install: true, uninstall: true) {

		section("Get notifications for this SmartBlock") {
			input(name: "smartBlock", type: "capability.switch", title: "Which SmartBlock would you like to monitor?", multiple: false)
			// TODO: type: "device.smartBlock",
		}

		section("Why would you like to be notified?") {
			href(name: "toDestroyedPage", page: destroyedPageName, title: "Because it was destroyed", description: destroyedDescription, state: destroyedState)

			href(name: "toRedstonePage", page: redstonePageName, title: "Because its redstone signal changed", description: redstoneDescription, state: redstoneState)
			href(name: "toNeighborPage", page: neighborPageName, title: "Because a block next to it changed", description: neighborDescription, state: neighborState)
		}

		section("Other Options") {
			label(title: "Label this notification", description: app.name, required: false, defaultValue: defaultLabelValue)
			mode(title: "Only send notifications when in one of these modes", description: "All modes")
		}
	}
}

def destroyedPage() {
	def pageName = "destroyedPage"
	dynamicPage(name: pageName, title: "For when your block is destroyed") {
		smartPhoneNotificationSection(pageName)
		chatSection(pageName)
		messageBuilderSection(pageName)
		chatClosestPlayerSection(pageName)
	}
}

def redstonePage() {
	def pageName = "redstonePage"
	dynamicPage(name: pageName, title: "Get Notified For Redstone Changes") {
		section("When Redstone Is") {
			input(name: "redstoneGreaterThan", type: "enum", required: false, title: "Greater Than", options: (0..15).collect {
				"${it}"
			})
			input(name: "redstoneLessThan", type: "enum", required: false, title: "Less than", options: (0..15).collect {
				"${it}"
			})
			input(name: "redstoneEqualTo", type: "enum", required: false, title: "Equal to", options: (0..15).collect {
				"${it}"
			})
		}
		smartPhoneNotificationSection(pageName)
		chatSection(pageName)
		messageBuilderSection(pageName)
	}
}

def neighborBlockPage() {
	def pageName = "neighborBlockPage"
	dynamicPage(name: pageName, title: "Get Notified When a neighbor block updates") {
		section("Not all blocks send updates, but Chests definitely do") {
			input(type: "enum", name: "neighborBlockParsed", title: "When any of these blocks are updated", required: false, multiple: true, options: allBlocksParsed())
		}

		smartPhoneNotificationSection(pageName)
		chatSection(pageName)
		messageBuilderSection(pageName)
		chatClosestPlayerSection(pageName)

		section(title: "More Info", hideable: true, hidden: true) {
			href(name: "allIds", title: "A full list of blocks and items can be found here", url: "http://minecraft.gamepedia.com/Ids", style: "external", description: null)
		}
	}
}

def messageBuilderPage(params) {

	def pageName = params.pageName
	def size = messageBuilderOptions().size() * 2

	dynamicPage(name: "messageBuilderPage", title: "Build your message") {
		section("These will be combined to form the final message.") {
			(0..size).each {
				input(
					name: "${pageName}MessagePart${it}",
					type: (it % 2) ? "enum" : "text",
					defaultValue: messagePartDefaultValue(pageName, it),
					options: (it % 2) ? messageBuilderOptions() : null,
					title: null, description: null, required: false, multiple: false
				)
			}
		}
	}
}

def smartPhoneNotificationSection(pageName) {
	section("SmartPhone notifications") {
        input("recipients", "contact", title: "Send notifications to") {
            input(name: "${pageName}WantsPush", title: "Push Notification", description: null, type: "bool", required: false, defaultValue: "false")
            input(name: "${pageName}WantsSms", title: "Text Message", description: "phone number", type: "phone", required: false)
        }
		input(name: "${pageName}WantsHH", title: "Hello Home only", description: null, type: "bool", required: false)
	}
}

def chatSection(pageName) {
	section("Minecraft Chat Message") {
		input(name: "${pageName}ChatAllUsers", title: "Chat all users", type: "bool", required: false)
		input(name: "${pageName}ChatUsername", title: "Or chat to a specific username", type: "text", required: false)
	}
}

def messageBuilderSection(pageName) {
	section("What should your message say?") {
		messageBuilderHref(pageName)
	}
}

def messageBuilderHref(pageName) {
	def partsAreSet = messagePartsSet(pageName)
	def messageState = partsAreSet ? "complete" : ""
	def messageDescription = partsAreSet ? messageDescriptionForPage(pageName) : defaultMessageDescription(pageName)

	href(
		name: "toBuilder",
		page: "messageBuilderPage",
		title: null,
		description: messageDescription ?: "Construct your message",
		state: messageState,
		params: [pageName: pageName]
	)
}

def chatClosestPlayerSection(pageName) {
	section("Chat the closest player to the block. (usually the player that destroyed it)") {
		messageBuilderHref("${pageName}ClosestPlayer")
	}
}

def pageStateComplete(pageName) {

	if (pageName == "redstonePage") {
		if (redstoneGreaterThan) return true
		if (redstoneLessThan) return true
		if (redstoneEqualTo) return true
		return false
	}

	if (pageName == "neighborBlockPage") {
		if (neighborBlockParsed) return true
		return false
	}

	if (app."${pageName}WantsPush") return true
	if (app."${pageName}WantsSms") return true
	if (app."${pageName}WantsHH") return true
	if (app."${pageName}ChatAllUsers") return true
	if (app."${pageName}ChatUsername") return true
	if (app."${pageName}ClosestPlayer") return true

	return false
}

/*
* INITIALIZE
*/

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "initializing"
	subscribe(smartBlock, "redstoneSignalStrength", redstoneSignalStrengthHandler)
	subscribe(smartBlock, "smartBlockNeighborChanged", smartBlockNeighborChangedHandler, [filterEvents: false])
	subscribe(smartBlock, "smartBlockNeighborChanged", smartBlockNeighborChangedHandler, [filterEvents: false])
	subscribe(smartBlock, "blockDestroyed.true", smartBlockDestroyedHandler, [filterEvents: false])
}

/*
* EVENT HANDLERS
*/

def smartBlockDestroyedHandler(evt) {
	log.debug "smartBlockDestroyedHandler evt.value: ${evt.value}"

	def pageName = "destroyedPage"
	def message = message(pageName)
	notifyUser(pageName, message)
}

def smartBlockNeighborChangedHandler(evt) {
	log.debug "smartBlockNeighborChangedHandler evt.value: ${evt.value}"
	log.debug "neighborBlockParsed: ${neighborBlockParsed}"

	if (neighborBlockParsed?.contains(evt.value)) {
		notifyUserOfNeighborChange(evt.value)
	}
}

def redstoneSignalStrengthHandler(evt) {
	log.debug "redstoneSignalStrengthHandler: ${evt.value}"

	int newValue = evt.value as int
	int lastValue = smartBlock.latestState("redstoneSignalStrength").value as int

	if (redstoneGreaterThan) {
		int gt = redstoneGreaterThan as int
//		log.debug "$newValue > $gt"
		if (newValue > gt) {
			log.debug "greater than ${gt}. send notification"
			notifyUserOfRedstoneChange(newValue)
		}
	}

	if (redstoneLessThan) {
		int lt = redstoneLessThan as int
//		log.debug "$newValue < $lt"
		if (newValue < lt) {
			log.debug "less than ${lt}. send notification"
			notifyUserOfRedstoneChange(newValue)
		}
	}

	if (redstoneEqualTo) {
		int et = redstoneEqualTo as int
//		log.debug "$newValue == $et"
		if (newValue == et) {
			log.debug "equal to ${et}. send notification"
			notifyUserOfRedstoneChange(newValue)
		}
	}

}

/*
* NOTIFICATIONS
*/

def notifyUserOfRedstoneChange(value) {
	def msg = message("redstonePage")
	log.debug "message: ${msg}"
	def notificationMessage = msg ?: "${smartBlock} redstone signal is ${value}"
	notifyUser(notificationMessage)
}

def notifyUserOfNeighborChange(value) {
	def msg = message("neighborPage")
	log.debug "message: ${msg}"
	def notificationMessage = msg ?: "${smartBlock} was updated by ${value}"
	notifyUser(notificationMessage)
}

def notifyUser(pageName, messageToSend) {
	log.debug "notifyUser pageName: ${pageName}"

	def closestPlayerMessage = message("${pageName}ClosestPlayer")
	log.debug "closestPlayerMessage = ${closestPlayerMessage}"
	def latestClosePlayer = getLatestClosePlayer()
	log.debug "latestClosePlayer = ${latestClosePlayer}"
	if (closestPlayerMessage && latestClosePlayer != "unknown") {
		log.debug "chatting closestPlayer"
		chatMessageToMC(closestPlayerMessage, latestClosePlayer)
	}


	def wantsHH = app."${pageName}WantsHH"
	log.debug "wantsHH = ${wantsHH}"
	if (wantsHH) {

		log.debug "sending HH"
		sendNotificationEvent(messageToSend)

	} else {
        if (location.contactBookEnabled) {
            sendNotificationToContacts(messageToSend, recipients)
        }
        else {

            def wantsPush = app."${pageName}WantsPush"
            log.debug "wantsPush = ${wantsPush}"
            if (wantsPush && wantsPush != "false") {
                log.debug "sending push"
                sendPush(messageToSend)
            }

            def wantsSms = app."${pageName}WantsSms"
            log.debug "wantsSms = ${wantsSms}"
            if (wantsSms) {
                log.debug "sending sms to: ${wantsSms}"
                sendSms(wantsSms, messageToSend)
            }
        }
	}

	def username = app."${pageName}ChatUsername"
	def allUsers = app."${pageName}ChatAllUsers"

	log.debug "username = ${username}"
	log.debug "allUsers = ${allUsers}"

	if (username && username != "") {
		log.debug "chatting username: ${username}"
		chatMessageToMC(messageToSend, username)
	} else if (allUsers) {
		log.debug "chatting all users"
		chatMessageToMC(messageToSend, null)
	}

}

def chatMessageToMC(message, username) {
	log.debug "chatMessageToMC"

	def url = "${app.getParent().getServerURL()}/chat?message=${message.encodeAsURL()}"
	if (username) {
		url = "${url}&username=${username.encodeAsURL()}"
	}

	log.debug "POST to ${url}"

	httpPost(url, "foo=bar") {}
}

def messageDescriptionPartsForPage(pageName) {
	def size = messageBuilderOptions().size() * 2
	(0..size).collect { app."${pageName}MessagePart${it}" }
}

def messagePartsSet(pageName) { // are any set?
	messageDescriptionPartsForPage(pageName).collect { !it }.unique().contains(false)
}

def defaultMessageDescription(pageName) {
	def description = ""

	if (pageName == "destroyedPage" || pageName == "redstonePage" || pageName == "neighborBlockPage") {
		def second = messageBuilderOptions()[messagePartDefaultValue(pageName, 1)]
		if (second) description = "\${${second}}"

		def third = messagePartDefaultValue(pageName, 2)
		if (third) description = "${description} ${third}"

		def fourth = messageBuilderOptions()[messagePartDefaultValue(pageName, 3)]
		if (fourth) description = "${description} \${${fourth}}"
	}

	return description
}

def messageDescriptionForPage(pageName) {

	def parts = messageDescriptionPartsForPage(pageName)
	def messageParts = []
	parts.eachWithIndex { part, idx ->
		if (part != null && part != "null") {
			if (idx % 2) {
				messageParts << "\${${messageBuilderOptions()[part]}}"
			} else {
				messageParts << part
			}
		}
	}

	if (messageParts) {
		return messageParts.join(" ").trim()
	} else {
		return defaultMessageDescription()
	}
}

def messagePartDefaultValue(pageName, part) {
	if (pageName == "destroyedPage") {
		if (part == 1) return "name"
		if (part == 2) return "was destroyed by"
		if (part == 3) return "closestPlayer"
	}

	if (pageName == "neighborBlockPage") {
		if (part == 1) return "name"
		if (part == 2) return "has a redstone signal of"
		if (part == 3) return "redstoneSignalStrength"
	}

	if (pageName == "redstonePage") {
		if (part == 1) return "name"
		if (part == 2) return "was updated by"
		if (part == 3) return "closestPlayer"
	}

	return null
}

def message(pageName) {
	log.debug "building message"
	def messageParts = []

	messageDescriptionPartsForPage(pageName).eachWithIndex { part, idx ->
		if (idx % 2) {
//			def option = messageBuilderOptions()[part]
			def optionPart = getMessagePartFromOption(part)
			if (optionPart) messageParts << optionPart
		} else {
			if (part) messageParts << part
		}
	}

	def message = messageParts.join(" ").trim()
	log.debug "message: ${message}"
	return message
}

def messageBuilderOptions() {
	return [
		"name": "SmartBlock name",
		"neighborBlockName": "Neighbor block name",
		"blockDestroyed": "Destroyed State ('destroyed' / 'OK')",
		"redstoneSignalStrength": "Redstone signal strength",
		"worldSeed": "World seed",
		"dimensionName": "Dimension name (World, Nether, End)",
		"coordinates": "Block coordinates",
		"closestPlayer": "Username of Closest player (within the past minute)",
		"placedBy": "Username of who placed the block"
	]
}

def getMessagePartFromOption(optionKey) {
	log.debug "optionKey: ${optionKey}"
	if (optionKey == "name") return smartBlock.label ?: smartBlock.name
	if (optionKey == "closestPlayer") return getLatestClosePlayer()
	if (optionKey == "blockDestroyed") return smartBlock.latestValue("blockDestroyed") ? "OK" : "destroyed"
	return smartBlock.latestValue(optionKey)
}

def getLatestClosePlayer() {
	def now = new Date()
	def minusOne = new Date(minutes: now.minutes - 1)
	def latestStates = smartBlock.statesSince("closestPlayer", minusOne)
	if (latestStates.size) {
		return latestStates[0].value
	}
	return "unknown"
}

/*
* BLOCKS
*/

def settingsAsIds() {
	log.debug "settingsAsIds"
	log.debug "neighborBlockParsed: $neighborBlockParsed"

	def subscribedIds = []

	neighborBlockParsed.each {
		subscribedIds << convertBlockSettingToBlockId(it)
	}

	return subscribedIds
}

def convertBlockSettingToBlockId(setting) {
	def id = setting.substring(0, setting.indexOf(" "))
	def name = allBlocks()[id]
	log.debug "id: $id, name:${name}"
	return id
}

def allBlocksParsed() {
	allBlocks().collect { k, v -> "${k} ${v}" }
}

def allBlocks() {
	[
		"0": "Air",
		"1": "Stone",
		"2": "Grass",
		"3": "Dirt",
		"4": "Cobblestone",
		"5": "Oak Wood Plank",
		"5:1": "Spruce Wood Plank",
		"5:2": "Birch Wood Plank",
		"5:3": "Jungle Wood Plank",
		"6": "Oak Sapling",
		"6:1": "Spruce Sapling",
		"6:2": "Birch Sapling",
		"6:3": "Jungle Sapling",
		"7": "Bedrock",
		"8": "Water",
		"9": "Stationary Water",
		"10": "Lava",
		"11": "Stationary Lava",
		"12": "Sand",
		"13": "Gravel",
		"14": "Gold Ore",
		"15": "Iron Ore",
		"16": "Coal Ore",
		"17": "Oak Wood",
		"17:1": "Spruce Wood",
		"17:2": "Birch Wood",
		"17:3": "Jungle Wood",
		"18": "Oak Leaves",
		"18:1": "Spruce Leaves",
		"18:2": "Birch Leaves",
		"18:3": "Jungle Leaves",
		"19": "Sponge",
		"20": "Glass",
		"21": "Lapis Lazuli Ore",
		"22": "Lapis Lazuli Block",
		"23": "Dispenser",
		"24": "Sandstone",
		"24:1": "Chiseled Sandstone",
		"24:2": "Smooth Sandstone",
		"25": "Note Block",
		"26": "Bed Block",
		"27": "Powered Rail",
		"28": "Detector Rail",
		"29": "Sticky Piston",
		"30": "Web",
		"31": "Dead Shrub",
		"31:1": "Grass",
		"31:2": "Fern",
		"32": "Dead Shrub",
		"33": "Piston",
		"34": "Piston Head",
		"35": "White Wool",
		"35:1": "Orange Wool",
		"35:2": "Magenta Wool",
		"35:3": "Light Blue Wool",
		"35:4": "Yellow Wool",
		"35:5": "Lime Wool",
		"35:6": "Pink Wool",
		"35:7": "Gray Wool",
		"35:8": "Light Gray Wool",
		"35:9": "Cyan Wool",
		"35:10": "Purple Wool",
		"35:11": "Blue Wool",
		"35:12": "Brown Wool",
		"35:13": "Green Wool",
		"35:14": "Red Wool",
		"35:15": "Black Wool",
		"37": "Dandelion",
		"38": "Rose",
		"39": "Brown Mushroom",
		"40": "Red Mushroom",
		"41": "Gold Block",
		"42": "Iron Block",
		"43": "Double Stone Slab",
		"43:1": "Double Sandstone Slab",
		"43:2": "Double Wooden Slab",
		"43:3": "Double Cobblestone Slab",
		"43:4": "Double Brick Slab",
		"43:5": "Double Stone Brick Slab",
		"43:6": "Double Nether Brick Slab",
		"43:7": "Double Quartz Slab",
		"44": "Stone Slab",
		"44:1": "Sandstone Slab",
		"44:2": "Wooden Slab",
		"44:3": "Cobblestone Slab",
		"44:4": "Brick Slab",
		"44:5": "Stone Brick Slab",
		"44:6": "Nether Brick Slab",
		"44:7": "Quartz Slab",
		"45": "Brick",
		"46": "TNT",
		"47": "Bookshelf",
		"48": "Mossy Cobblestone",
		"49": "Obsidian",
		"50": "Torch",
		"51": "Fire",
		"52": "Monster Spawner",
		"53": "Oak Wood Stairs",
		"54": "Chest",
		"55": "Redstone Wire",
		"56": "Diamond Ore",
		"57": "Diamond Block",
		"58": "Workbench",
		"59": "Wheat Crops",
		"60": "Soil",
		"61": "Furnace",
		"62": "Burning Furnace",
		"63": "Sign Post",
		"64": "Wooden Door Block",
		"65": "Ladder",
		"66": "Rails",
		"67": "Cobblestone Stairs",
		"68": "Wall Sign",
		"69": "Lever",
		"70": "Stone Pressure Plate",
		"71": "Iron Door Block",
		"72": "Wooden Pressure Plate",
		"73": "Redstone Ore",
		"74": "Glowing Redstone Ore",
		"75": "Redstone Torch(off)",
		"76": "Redstone Torch(on)",
		"77": "Stone Button",
		"78": "Snow",
		"79": "Ice",
		"80": "Snow Block",
		"81": "Cactus",
		"82": "Clay",
		"83": "Sugar Cane",
		"84": "Jukebox",
		"85": "Fence",
		"86": "Pumpkin",
		"87": "Netherrack",
		"88": "Soul Sand",
		"89": "Glowstone",
		"90": "Portal",
		"91": "Jack - O - Lantern",
		"92": "Cake Block",
		"93": "Redstone Repeater Block(off)",
		"94": "Redstone Repeater Block(on)",
		"95": "Locked Chest",
		"96": "Trapdoor",
		"97": "Stone(Silverfish)",
		"97:1": "Cobblestone(Silverfish)",
		"97:2": "Stone Brick(Silverfish)",
		"98": "Stone Brick",
		"98:1": "Mossy Stone Brick",
		"98:2": "Cracked Stone Brick",
		"98:3": "Chiseled Stone Brick",
		"99": "Red Mushroom Cap",
		"100": "Brown Mushroom Cap",
		"101": "Iron Bars",
		"102": "Glass Pane",
		"103": "Melon Block",
		"104": "Pumpkin Stem",
		"105": "Melon Stem",
		"106": "Vines",
		"107": "Fence Gate",
		"108": "Brick Stairs",
		"109": "Stone Brick Stairs",
		"110": "Mycelium",
		"111": "Lily Pad",
		"112": "Nether Brick",
		"113": "Nether Brick Fence",
		"114": "Nether Brick Stairs",
		"115": "Nether Wart",
		"116": "Enchantment Table",
		"117": "Brewing Stand",
		"118": "Cauldron",
		"119": "End Portal",
		"120": "End Portal Frame",
		"121": "End Stone",
		"122": "Dragon Egg",
		"123": "Redstone Lamp(inactive)",
		"124": "Redstone Lamp(active)",
		"125": "Double Oak Wood Slab",
		"125:1": "Double Spruce Wood Slab",
		"125:2": "Double Birch Wood Slab",
		"125:3": "Double Jungle Wood Slab",
		"126": "Oak Wood Slab",
		"126:1": "Spruce Wood Slab",
		"126:2": "Birch Wood Slab",
		"126:3": "Jungle Wood Slab",
		"127": "Cocoa Plant",
		"128": "Sandstone Stairs",
		"129": "Emerald Ore",
		"130": "Ender Chest",
		"131": "Tripwire Hook",
		"132": "Tripwire",
		"133": "Emerald Block",
		"134": "Spruce Wood Stairs",
		"135": "Birch Wood Stairs",
		"136": "Jungle Wood Stairs",
		"137": "Command Block",
		"138": "Beacon Block",
		"139": "Cobblestone Wall",
		"139:1": "Mossy Cobblestone Wall",
		"140": "Flower Pot",
		"141": "Carrots",
		"142": "Potatoes",
		"143": "Wooden Button",
		"144": "Mob Head",
		"145": "Anvil",
		"146": "Trapped Chest",
		"147": "Weighted Pressure Plate(light)",
		"148": "Weighted Pressure Plate(heavy)",
		"149": "Redstone Comparator(inactive)",
		"150": "Redstone Comparator(active)",
		"151": "Daylight Sensor",
		"152": "Redstone Block",
		"153": "Nether Quartz Ore",
		"154": "Hopper",
		"155": "Quartz Block",
		"155:1": "Chiseled Quartz Block",
		"155:2": "Pillar Quartz Block",
		"156": "Quartz Stairs",
		"157": "Activator Rail",
		"158": "Dropper",
		"159": "White Stained Clay",
		"159:1": "Orange Stained Clay",
		"159:2": "Magenta Stained Clay",
		"159:3": "Light Blue Stained Clay",
		"159:4": "Yellow Stained Clay",
		"159:5": "Lime Stained Clay",
		"159:6": "Pink Stained Clay",
		"159:7": "Gray Stained Clay",
		"159:8": "Light Gray Stained Clay",
		"159:9": "Cyan Stained Clay",
		"159:10": "Purple Stained Clay",
		"159:11": "Blue Stained Clay",
		"159:12": "Brown Stained Clay",
		"159:13": "Green Stained Clay",
		"159:14": "Red Stained Clay",
		"159:15": "Black Stained Clay",
		"170": "Hay Bale",
		"171": "White Carpet",
		"171:1": "Orange Carpet",
		"171:2": "Magenta Carpet",
		"171:3": "Light Blue Carpet",
		"171:4": "Yellow Carpet",
		"171:5": "Lime Carpet",
		"171:6": "Pink Carpet",
		"171:7": "Gray Carpet",
		"171:8": "Light Gray Carpet",
		"171:9": "Cyan Carpet",
		"171:10": "Purple Carpet",
		"171:11": "Blue Carpet",
		"171:12": "Brown Carpet",
		"171:13": "Green Carpet",
		"171:14": "Red Carpet",
		"171:15": "Black Carpet",
		"172": "Hardened Clay",
		"173": "Block of Coal",
		"256": "Iron Shovel",
		"257": "Iron Pickaxe",
		"258": "Iron Axe",
		"259": "Flint and Steel",
		"260": "Apple",
		"261": "Bow",
		"262": "Arrow",
		"263": "Coal",
		"263:1": "Charcoal",
		"264": "Diamond",
		"265": "Iron Ingot",
		"266": "Gold Ingot",
		"267": "Iron Sword",
		"268": "Wooden Sword",
		"269": "Wooden Shovel",
		"270": "Wooden Pickaxe",
		"271": "Wooden Axe",
		"272": "Stone Sword",
		"273": "Stone Shovel",
		"274": "Stone Pickaxe",
		"275": "Stone Axe",
		"276": "Diamond Sword",
		"277": "Diamond Shovel",
		"278": "Diamond Pickaxe",
		"279": "Diamond Axe",
		"280": "Stick",
		"281": "Bowl",
		"282": "Mushroom Soup",
		"283": "Gold Sword",
		"284": "Gold Shovel",
		"285": "Gold Pickaxe",
		"286": "Gold Axe",
		"287": "String",
		"288": "Feather",
		"289": "Sulphur",
		"290": "Wooden Hoe",
		"291": "Stone Hoe",
		"292": "Iron Hoe",
		"293": "Diamond Hoe",
		"294": "Gold Hoe",
		"295": "Wheat Seeds",
		"296": "Wheat",
		"297": "Bread",
		"298": "Leather Helmet",
		"299": "Leather Chestplate",
		"300": "Leather Leggings",
		"301": "Leather Boots",
		"302": "Chainmail Helmet",
		"303": "Chainmail Chestplate",
		"304": "Chainmail Leggings",
		"305": "Chainmail Boots",
		"306": "Iron Helmet",
		"307": "Iron Chestplate",
		"308": "Iron Leggings",
		"309": "Iron Boots",
		"310": "Diamond Helmet",
		"311": "Diamond Chestplate",
		"312": "Diamond Leggings",
		"313": "Diamond Boots",
		"314": "Gold Helmet",
		"315": "Gold Chestplate",
		"316": "Gold Leggings",
		"317": "Gold Boots",
		"318": "Flint",
		"319": "Raw Porkchop",
		"320": "Cooked Porkchop",
		"321": "Painting",
		"322": "Golden Apple",
		"322:1": "Enchanted Golden Apple",
		"323": "Sign",
		"324": "Wooden Door",
		"325": "Bucket",
		"326": "Water Bucket",
		"327": "Lava Bucket",
		"328": "Minecart",
		"329": "Saddle",
		"330": "Iron Door",
		"331": "Redstone",
		"332": "Snowball",
		"333": "Boat",
		"334": "Leather",
		"335": "Milk Bucket",
		"336": "Clay Brick",
		"337": "Clay Balls",
		"338": "Sugarcane",
		"339": "Paper",
		"340": "Book",
		"341": "Slimeball",
		"342": "Storage Minecart",
		"343": "Powered Minecart",
		"344": "Egg",
		"345": "Compass",
		"346": "Fishing Rod",
		"347": "Clock",
		"348": "Glowstone Dust",
		"349": "Raw Fish",
		"350": "Cooked Fish",
		"351": "Ink Sack",
		"351:1": "Rose Red",
		"351:2": "Cactus Green",
		"351:3": "Coco Beans",
		"351:4": "Lapis Lazuli",
		"351:5": "Purple Dye",
		"351:6": "Cyan Dye",
		"351:7": "Light Gray Dye",
		"351:8": "Gray Dye",
		"351:9": "Pink Dye",
		"351:10": "Lime Dye",
		"351:11": "Dandelion Yellow",
		"351:12": "Light Blue Dye",
		"351:13": "Magenta Dye",
		"351:14": "Orange Dye",
		"351:15": "Bone Meal",
		"352": "Bone",
		"353": "Sugar",
		"354": "Cake",
		"355": "Bed",
		"356": "Redstone Repeater",
		"357": "Cookie",
		"358": "Map",
		"359": "Shears",
		"360": "Melon",
		"361": "Pumpkin Seeds",
		"362": "Melon Seeds",
		"363": "Raw Beef",
		"364": "Steak",
		"365": "Raw Chicken",
		"366": "Cooked Chicken",
		"367": "Rotten Flesh",
		"368": "Ender Pearl",
		"369": "Blaze Rod",
		"370": "Ghast Tear",
		"371": "Gold Nugget",
		"372": "Nether Wart Seeds",
		"373": "Potion",
		"374": "Glass Bottle",
		"375": "Spider Eye",
		"376": "Fermented Spider Eye",
		"377": "Blaze Powder",
		"378": "Magma Cream",
		"379": "Brewing Stand",
		"380": "Cauldron",
		"381": "Eye of Ender",
		"382": "Glistering Melon",
		"383:50": "Spawn Creeper",
		"383:51": "Spawn Skeleton",
		"383:52": "Spawn Spider",
		"383:54": "Spawn Zombie",
		"383:55": "Spawn Slime",
		"383:56": "Spawn Ghast",
		"383:57": "Spawn Pigman",
		"383:58": "Spawn Enderman",
		"383:59": "Spawn Cave Spider",
		"383:60": "Spawn Silverfish ",
		"383:61": "Spawn Blaze",
		"383:62": "Spawn Magma Cube ",
		"383:65": "Spawn Bat",
		"383:66": "Spawn Witch",
		"383:90": "Spawn Pig",
		"383:91": "Spawn Sheep",
		"383:92": "Spawn Cow",
		"383:93": "Spawn Chicken",
		"383:94": "Spawn Squid",
		"383:95": "Spawn Wolf",
		"383:96": "Spawn Mooshroom",
		"383:98": "Spawn Ocelot",
		"383:100": "Spawn Horse",
		"383:120": "Spawn Villager",
		"384": "Bottle o' Enchanting",
		"385": "Fire Charge",
		"386": "Book and Quill",
		"387": "Written Book",
		"388": "Emerald",
		"389": "Item Frame",
		"390": "Flower Pot",
		"391": "Carrots",
		"392": "Potato",
		"393": "Baked Potato",
		"394": "Poisonous Potato",
		"395": "Map",
		"396": "Golden Carrot",
		"397": "Mob Head (Skeleton)",
		"397:1": "Mob Head (Wither Skeleton)",
		"397:2": "Mob Head (Zombie)",
		"397:3": "Mob Head (Human)",
		"397:4": "Mob Head (Creeper)",
		"398": "Carrot on a Stick",
		"399": "Nether Star",
		"400": "Pumpkin Pie",
		"401": "Firework Rocket",
		"402": "Firework Star",
		"403": "Enchanted Book",
		"404": "Redstone Comparator",
		"405": "Nether Brick",
		"406": "Nether Quartz",
		"407": "Minecart with TNT",
		"408": "Minecart with Hopper",
		"417": "Iron Horse Armor",
		"418": "Gold Horse Armor",
		"419": "Diamond Horse Armor",
		"420": "Lead",
		"421": "Name Tag"
	]
}
