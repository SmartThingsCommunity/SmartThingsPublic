/**
 *  Every Element
 *
 *  Copyright 2014 SmartThings
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
 */
definition(
	name: "Every Element",
	namespace: "smartthings/examples",
	author: "SmartThings",
	description: "Every element demonstration app",
	category: "SmartThings Internal",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	page(name: "firstPage")
	page(name: "inputPage")
	page(name: "appPage")
	page(name: "labelPage")
	page(name: "modePage")
	page(name: "paragraphPage")
	page(name: "iconPage")
	page(name: "hrefPage")
	page(name: "buttonsPage")
	page(name: "imagePage")
	page(name: "videoPage")
	page(name: "deadEnd", title: "Nothing to see here, move along.", content: "foo")
	page(name: "flattenedPage")
}

def firstPage() {
	dynamicPage(name: "firstPage", title: "Where to first?", install: true, uninstall: true) {
		section() {
			href(page: "inputPage", title: "Element: 'input'")
			href(page: "appPage", title: "Element: 'app'")
			href(page: "labelPage", title: "Element: 'label'")
			href(page: "modePage", title: "Element: 'mode'")
			href(page: "paragraphPage", title: "Element: 'paragraph'")
			href(page: "iconPage", title: "Element: 'icon'")
			href(page: "hrefPage", title: "Element: 'href'")
			href(page: "buttonsPage", title: "Element: 'buttons'")
			href(page: "imagePage", title: "Element: 'image'")
			href(page: "videoPage", title: "Element: 'video'")
		}
		section() {
			href(page: "flattenedPage", title: "All of the above elements on a single page")
		}
	}
}

def inputPage() {
	dynamicPage(name: "inputPage", title: "Every 'input' type") {
		section("enum") {
			input(type: "enum", name: "enumRefresh", title: "submitOnChange:true", required: false, multiple: true, options: ["one", "two", "three"], submitOnChange: true)
			if (enumRefresh) {
				paragraph "${enumRefresh}"
			}
			input(type: "enum", name: "enumSegmented", title: "style:segmented", required: false, multiple: true, options: ["one", "two", "three"], style: "segmented")
			input(type: "enum", name: "enum", title: "required:false, multiple:false", required: false, multiple: false, options: ["one", "two", "three"])
			input(type: "enum", name: "enumRequired", title: "required:true", required: true, multiple: false, options: ["one", "two", "three"])
			input(type: "enum", name: "enumMultiple", title: "multiple:true", required: false, multiple: true, options: ["one", "two", "three"])
			input(type: "enum", name: "enumWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, multiple: true, options: ["one", "two", "three"], image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
			input(type: "enum", name: "enumWithGroupedOptions", title: "groupedOptions", description: "This enum has grouped options", required: false, multiple: true, groupedOptions: [
				[
					title : "the group title that is displayed",
					order : 0, // the order of the group; 0-based
					image : "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", // not yet supported
					values: [
						[
							key  : "the value that will be placed in SmartApp settings.", // such as a device id
							value: "the title of the selectable option that is displayed", // such as a device name
							order: 0 // the order of the option
						]
					]
				],
				[
					title : "the second group title that is displayed",
					order : 1, // the order of the group; 0-based
					image : null, // not yet supported
					values: [
						[
							key  : "some_device_id",
							value: "some_device_name",
							order: 1 // the order of the option. This option will appear second in the list even though it is the first option defined in this map
						],
						[
							key  : "some_other_device_id",
							value: "some_other_device_name",
							order: 0 // the order of the option. This option will appear first in the list even though it is not the first option defined in this map
						]
					]
				]
			])
		}
		section("text") {
			input(type: "text", name: "text", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "text", name: "textRequired", title: "required:true", required: true, multiple: false)
			input(type: "text", name: "textWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("number") {
			input(type: "number", name: "number", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "number", name: "numberRequired", title: "required:true", required: true, multiple: false)
			input(type: "number", name: "numberWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("boolean") {
			input(type: "boolean", name: "boolean", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "boolean", name: "booleanWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("password") {
			input(type: "password", name: "password", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "password", name: "passwordRequired", title: "required:true", required: true, multiple: false)
			input(type: "password", name: "passwordWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("phone") {
			input(type: "phone", name: "phone", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "phone", name: "phoneRequired", title: "required:true", required: true, multiple: false)
			input(type: "phone", name: "phoneWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("email") {
			input(type: "email", name: "email", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "email", name: "emailRequired", title: "required:true", required: true, multiple: false)
			input(type: "email", name: "emailWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("decimal") {
			input(type: "decimal", name: "decimal", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "decimal", name: "decimalRequired", title: "required:true", required: true, multiple: false)
			input(type: "decimal", name: "decimalWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("mode") {
			input(type: "mode", name: "mode", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "mode", name: "modeRequired", title: "required:true", required: true, multiple: false)
			input(type: "mode", name: "modeMultiple", title: "multiple:true", required: false, multiple: true)
			input(type: "mode", name: "iconWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, multiple: true, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("icon") {
			input(type: "icon", name: "icon", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "icon", name: "iconRequired", title: "required:true", required: true, multiple: false)
			input(type: "icon", name: "iconWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("capability") {
			input(type: "capability.switch", name: "capability", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "capability.switch", name: "capabilityRequired", title: "required:true", required: true, multiple: false)
			input(type: "capability.switch", name: "capabilityMultiple", title: "multiple:true", required: false, multiple: true)
			input(type: "capability.switch", name: "capabilityWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, multiple: true, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("hub") {
			input(type: "hub", name: "hub", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "hub", name: "hubRequired", title: "required:true", required: true, multiple: false)
			input(type: "hub", name: "hubMultiple", title: "multiple:true", required: false, multiple: true)
			input(type: "hub", name: "hubWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, multiple: true, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("device") {
			input(type: "device.switch", name: "device", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "device.switch", name: "deviceRequired", title: "required:true", required: true, multiple: false)
			input(type: "device.switch", name: "deviceMultiple", title: "multiple:true", required: false, multiple: true)
			input(type: "device.switch", name: "deviceWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, multiple: true, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("time") {
			input(type: "time", name: "time", title: "required:false, multiple:false", required: false, multiple: false)
			input(type: "time", name: "timeRequired", title: "required:true", required: true, multiple: false)
			input(type: "time", name: "timeWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
		section("contact-book") {
			input("recipients", "contact", title: "Notify", description: "Send notifications to") {
				input(type: "phone", name: "phone", title: "Send text message to", required: false, multiple: false)
				input(type: "boolean", name: "boolean", title: "Send push notification", required: false, multiple: false)
			}
		}
	}
}

def appPage() {
	dynamicPage(name: "appPage", title: "Every 'app' type") {
		section {
			paragraph "These won't work unless you create a child SmartApp to link to... Sorry."
		}
		section("app") {
			app(
				name: "app",
				title: "required:false, multiple:false",
				required: false,
				multiple: false,
				namespace: "Steve",
				appName: "Child SmartApp"
			)
			app(name: "appRequired", title: "required:true", required: true, multiple: false, namespace: "Steve", appName: "Child SmartApp")
			app(name: "appComplete", title: "state:complete", required: false, multiple: false, namespace: "Steve", appName: "Child SmartApp", state: "complete")
			app(name: "appWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, multiple: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", namespace: "Steve", appName: "Child SmartApp")
		}
		section("multiple:true") {
			app(name: "appMultiple", title: "multiple:true", required: false, multiple: true, namespace: "Steve", appName: "Child SmartApp")
		}
		section("multiple:true with image") {
			app(name: "appMultipleWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, multiple: true, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", namespace: "Steve", appName: "Child SmartApp")
		}
	}
}

def labelPage() {
	dynamicPage(name: "labelPage", title: "Every 'Label' type") {
		section("label") {
			label(name: "label", title: "required:false, multiple:false", required: false, multiple: false)
			label(name: "labelRequired", title: "required:true", required: true, multiple: false)
			label(name: "labelWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
	}
}

def modePage() {
	dynamicPage(name: "modePage", title: "Every 'mode' type") { // TODO: finish this
		section("mode") {
			mode(name: "mode", title: "required:false, multiple:false", required: false, multiple: false)
			mode(name: "modeRequired", title: "required:true", required: true, multiple: false)
			mode(name: "modeMultiple", title: "multiple:true", required: false, multiple: true)
			mode(name: "modeWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, multiple: true, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
	}
}

def paragraphPage() {
	dynamicPage(name: "paragraphPage", title: "Every 'paragraph' type") {
		section("paragraph") {
			paragraph "This us how you should make a paragraph element"
			paragraph image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", "This is a long description, blah, blah, blah."
		}
	}
}

def iconPage() {
	dynamicPage(name: "iconPage", title: "Every 'icon' type") { // TODO: finish this
		section("icon") {
			icon(name: "icon", title: "required:false, multiple:false", required: false, multiple: false)
			icon(name: "iconRequired", title: "required:true", required: true, multiple: false)
			icon(name: "iconWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
	}
}

def hrefPage() {
	dynamicPage(name: "hrefPage", title: "Every 'href' type") {
		section("page") {
			href(name: "hrefPage", title: "required:false, multiple:false", required: false, multiple: false, page: "deadEnd")
			href(name: "hrefPageRequired", title: "required:true", required: true, multiple: false, page: "deadEnd", description: "Don't make hrefs required")
			href(name: "hrefPageComplete", title: "state:complete", required: false, multiple: false, page: "deadEnd", state: "complete")
			href(name: "hrefPageWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", page: "deadEnd",)
		}
		section("external") {
			href(name: "hrefExternal", title: "required:false, multiple:false", required: false, multiple: false, style: "external", url: "http://smartthings.com/")
			href(name: "hrefExternalRequired", title: "required:true", required: true, multiple: false, style: "external", url: "http://smartthings.com/", description: "Don't make hrefs required")
			href(name: "hrefExternalComplete", title: "state:complete", required: false, multiple: true, style: "external", url: "http://smartthings.com/", state: "complete")
			href(name: "hrefExternalWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", url: "http://smartthings.com/")
		}
		section("embedded") {
			href(name: "hrefEmbedded", title: "required:false, multiple:false", required: false, multiple: false, style: "embedded", url: "http://smartthings.com/")
			href(name: "hrefEmbeddedRequired", title: "required:true", required: true, multiple: false, style: "embedded", url: "http://smartthings.com/", description: "Don't make hrefs required")
			href(name: "hrefEmbeddedComplete", title: "state:complete", required: false, multiple: true, style: "embedded", url: "http://smartthings.com/", state: "complete")
			href(name: "hrefEmbeddedWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", url: "http://smartthings.com/")
		}
	}
}

def buttonsPage() {
	dynamicPage(name: "buttonsPage", title: "Every 'button' type") {
		section("buttons") {
			buttons(name: "buttons", title: "required:false, multiple:false", required: false, multiple: false, buttons: [
				[label: "foo", action: "foo"],
				[label: "bar", action: "bar"]
			])
			buttons(name: "buttonsRequired", title: "required:true", required: true, multiple: false, buttons: [
				[label: "foo", action: "foo"],
				[label: "bar", action: "bar"]
			])
			buttons(name: "buttonsWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", buttons: [
				[label: "foo", action: "foo"],
				[label: "bar", action: "bar"]
			])
		}
		section("Colored Buttons") {
			buttons(name: "buttonsColoredSpecial", title: "special strings", description: "SmartThings highly recommends using these colors", buttons: [
				[label: "complete", action: "bar", backgroundColor: "complete"],
				[label: "required", action: "bar", backgroundColor: "required"]
			])
			buttons(name: "buttonsColoredHex", title: "hex values work", buttons: [
				[label: "bg: #000dff", action: "foo", backgroundColor: "#000dff"],
				[label: "fg: #ffac00", action: "foo", color: "#ffac00"],
				[label: "both fg and bg", action: "foo", color: "#ffac00", backgroundColor: "#000dff"]
			])
			buttons(name: "buttonsColoredString", title: "strings work too", buttons: [
				[label: "green", action: "foo", backgroundColor: "green"],
				[label: "red", action: "foo", backgroundColor: "red"],
				[label: "both fg and bg", action: "foo", color: "red", backgroundColor: "green"]
			])
		}
	}

}

def imagePage() {
	dynamicPage(name: "imagePage", title: "Every 'image' type") { // TODO: finish thise
		section("image") {
			image "http://f.cl.ly/items/1k1S0A0m3805402o3O12/20130915-191127.jpg"
			image(name: "imageWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
		}
	}
}

def videoPage() {
	dynamicPage(name: "imagePage", title: "Every 'image' type") { // TODO: finish this
		section("video") {
			// TODO: update this when there is a videoElement method
			element(name: "videoElement", element: "video", type: "video", title: "this is a video!", description: "I am setting long title and descriptions to test the offset", required: false, image: "http://ec2-54-161-144-215.compute-1.amazonaws.com:8081/jesse/cam1/54aafcd1c198347511c26321.jpg", video: "http://ec2-54-161-144-215.compute-1.amazonaws.com:8081/jesse/cam1/54aafcd1c198347511c2631f.mp4")
		}
	}
}

def flattenedPage() {
	def allSections = []
	firstPage().sections.each { section ->
		section.body.each { hrefElement ->
			if (hrefElement.page != "flattenedPage") {
				allSections += "${hrefElement.page}"().sections
			}
		}
	}
	def flattenedPage = dynamicPage(name: "flattenedPage", title: "All elements in one page!") {}
	flattenedPage.sections = allSections
	return flattenedPage
}

def foo() {
	dynamicPage(name: "deadEnd") {

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
	// TODO: subscribe to attributes, devices, locations, etc.
}
