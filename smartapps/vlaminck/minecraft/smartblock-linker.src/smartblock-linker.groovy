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
 *  SmartBlock Linker
 *
 *  Author: Steve Vlaminck
 *
 *  Date: 2013-12-26
 */

definition(
    name: "SmartBlock Linker",
    namespace: "vlaminck/Minecraft",
    author: "SmartThings",
    description: "A SmartApp that links SmartBlocks to switches",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {

	page(name: "linkerPage")
}

def linkerPage(params) {

	log.debug "linkerPage params: ${params}"

	dynamicPage(name: "linkerPage", title: "Link your SmartBlock to a physical device", install: true, uninstall: false) {

		section {
			input(
				name: "linkedSmartBlock",
				type: "capability.switch",
//				type: "device.SmartBlock",
				title: "Linked SmartBlock",
				required: true,
				multiple: false
			)
			input(
				name: "switchUpdatesBlock",
				type: "bool",
				title: "Update this SmartBlock when the switch below changes state",
				description: "",
				defaultValue: "false"
			)
		}
		section {
			input(
				name: "linkedSwitch",
				type: "capability.switch",
				title: "Linked Switch",
				required: true,
				multiple: false
			)
			input(
				name: "blockUpdatesSwitch",
				type: "bool",
				title: "Update this switch when the SmartBlock above changes state",
				description: "",
				defaultValue: "true"
			)
		}

		section {
			label(
				title: "Label this Link",
				required: false
			)
			mode(
				title: "Only link these devices when in one of these modes",
				description: "All modes"
			)
		}

		section("When \"Update this SmartBlock...\" is on") {
			paragraph "If you place a Redstone Lamp next to your SmartBlock, it will turn on/off when \"Linked Switch\" turns on/off"
		}

		section("When \"Update this switch...\" is on") {
			paragraph "If you place a lever on your Minecraft SmartBlock, it will control \"Linked Switch\""
		}

		section("Why turning both on can be bad") {
			paragraph "Because there can be latency."
			paragraph "Flipping the lever will send a signal from Minecraft to SmartThings. SmartThings will then send the signal back when the light has turned on."
			paragraph "If you flip the lever again before that round trip is complete, you can get into an infinite loop of signals being sent back and forth."
			paragraph "You've been warned ;)"
		}
	}
}

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

	if (blockUpdatesSwitch)
	{
		subscribe(linkedSmartBlock, "level", updateSwitchLevel)
		subscribe(linkedSmartBlock, "switch", updateSwitchState)
	}

	if (switchUpdatesBlock)
	{
		subscribe(linkedSwitch, "level", updateBlockLevel)
		subscribe(linkedSwitch, "switch", updateBlockState)
	}

}

def updateSwitchLevel(evt) {
	int level = evt.value as int
	log.debug "matching level: ${level}"
	linkedSwitch.setLevel(level)
}

def updateBlockLevel(evt) {
	int level = evt.value as int
	log.debug "matching level: ${level}"
	linkedSmartBlock.setLevel(level)
}

def updateSwitchState(evt) {
	log.debug "setting linkedSwitch to ${evt.value}"
	linkedSwitch."${evt.value}"()
}

def updateBlockState(evt) {
	log.debug "setting linkedSmartBlock to ${evt.value}"
	linkedSmartBlock."${evt.value}"()
}

def getBlockId() {
	return linkedSmartBlock.id
}

def getLinkerDescription() {

	def left = linkedSmartBlock ? "${linkedSmartBlock.label ?: linkedSmartBlock.name}" : ""
	def right = linkedSwitch ? "${linkedSwitch.label ?: linkedSwitch.name}" : ""

	log.debug "left: ${left}, right: ${right}"

	def leftLink = switchUpdatesBlock ? "<" : ""
	def rightLink = blockUpdatesSwitch ? ">" : ""

	log.debug "leftLink: ${leftLink}, rightLink: ${rightLink}"

	log.debug "switchUpdatesBlock: ${switchUpdatesBlock}"
	log.debug "blockUpdatesSwitch: ${blockUpdatesSwitch}"

	if (leftLink == "" && rightLink == "")
	{
		return null
	}

	"${left} ${leftLink}--${rightLink} ${right}"
}
