/**
 *  Every Element
 *
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
    // landing page
    page(name: "firstPage")

    // PageKit
    page(name: "buttonsPage")
    page(name: "imagePage")
    page(name: "inputPage")
    page(name: "inputBooleanPage")
    page(name: "inputIconPage")
    page(name: "inputImagePage")
    page(name: "inputDevicePage")
    page(name: "inputCapabilityPage")
    page(name: "inputRoomPage")
    page(name: "inputModePage")
    page(name: "inputSelectionPage")
    page(name: "inputHubPage")
    page(name: "inputContactBookPage")
    page(name: "inputTextPage")
    page(name: "inputTimePage")
    page(name: "appPage")
    page(name: "hrefPage")
    page(name: "paragraphPage")
    page(name: "videoPage")
    page(name: "labelPage")
    page(name: "modePage")

    // Every element helper pages
    page(name: "deadEnd", title: "Nothing to see here, move along.", content: "foo")
    page(name: "flattenedPage")
}

def firstPage() {
    dynamicPage(name: "firstPage", title: "Where to first?", install: true, uninstall: true) {
        section {
            href(page: "appPage", title: "Element: 'app'")
            href(page: "buttonsPage", title: "Element: 'buttons'")
            href(page: "hrefPage", title: "Element: 'href'")
            href(page: "imagePage", title: "Element: 'image'")
            href(page: "inputPage", title: "Element: 'input'")
            href(page: "labelPage", title: "Element: 'label'")
            href(page: "modePage", title: "Element: 'mode'")
            href(page: "paragraphPage", title: "Element: 'paragraph'")
            href(page: "videoPage", title: "Element: 'video'")
        }
        section {
            href(page: "flattenedPage", title: "All of the above elements on a single page")
        }
    }
}

def inputPage() {
    dynamicPage(name: "inputPage", title: "Links to every 'input' element") {
        section {
            href(page: "inputBooleanPage", title: "to boolean page")
            href(page: "inputIconPage", title: "to icon page")
            href(page: "inputImagePage", title: "to image page")
            href(page: "inputSelectionPage", title: "to selection page")
            href(page: "inputTextPage", title: "to text page")
            href(page: "inputTimePage", title: "to time page")
        }
        section("subsets of selection input") {
            href(page: "inputDevicePage", title: "to device selection page")
            href(page: "inputCapabilityPage", title: "to capability selection page")
            href(page: "inputRoomPage", title: "to room selection page")
            href(page: "inputModePage", title: "to mode selection page")
            href(page: "inputHubPage", title: "to hub selection page")
            href(page: "inputContactBookPage", title: "to contact-book selection page")
        }
    }
}

def inputBooleanPage() {
    dynamicPage(name: "inputBooleanPage") {
        section {
            paragraph "The `required` and `multiple` attributes have no effect because the value will always be either `true` or `false`"
        }
        section {
            input(type: "boolean", name: "booleanWithoutDescription", title: "without description", description: null)
            input(type: "boolean", name: "booleanWithDescription", title: "with description", description: "This has a description")
        }
        section("defaultValue: 'true'") {
            input(type: "boolean", name: "booleanWithDefaultValue", title: "", description: "", defaultValue: "true")
        }
        section("with image") {
            input(type: "boolean", name: "booleanWithoutDescriptionWithImage", title: "without description", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", description: null)
            input(type: "boolean", name: "booleanWithDescriptionWithImage", title: "with description", description: "This has a description", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
        }
    }
}
def inputIconPage() {
    dynamicPage(name: "inputIconPage") {
        section {
            paragraph "`description` is not displayed for icon elements"
            paragraph "`multiple` has no effect because you can only choose a single icon"
        }
        section("required: true") {
            input(type: "icon", name: "iconRequired", title: "without description", required: true)
            input(type: "icon", name: "iconRequiredWithDescription", title: "with description", description: "this is a description", required: true)
        }
        section("with image") {
            paragraph "The image specified will be replaced after an icon is selected"
            input(type: "icon", name: "iconwithImage", title: "without description", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
        }
    }
}
def inputImagePage() {
    dynamicPage(name: "inputImagePage") {
        section {
            paragraph "This only exists in DeviceTypes. Someone should do something about that. (glares at MikeDave)"
            paragraph "Go to the device preferences of a Mobile Presence device to see it in action"
            paragraph "If you try to set the value of this, it will not behave as it would in Device Preferences"
            input(type: "image", title: "This is kind of what it looks like", required: false)
        }
    }
}


def optionsGroup(List groups, String title) {
    def group = [values:[], order: groups.size()]
    group.title = title ?: ""
    groups << group
    return groups
}
def addValues(List groups, String key, String value) {
    def lastGroup = groups[-1]
    lastGroup["values"] << [
            key: key,
            value: value,
            order: lastGroup["values"].size()
    ]
    return groups
}
def listToMap(List original) {
    original.inject([:]) { result, v ->
        result[v] = v
        return result
    }
}
def addGroup(List groups, String title, values) {
    if (values instanceof List) {
        values = listToMap(values)
    }

    values.inject(optionsGroup(groups, title)) { result, k, v ->
        return addValues(result, k, v)
    }
    return groups
}
def addGroup(values) {
    addGroup([], null, values)
}
/* Example usage of options builder

// Creating grouped options
    def newGroups = []
    addGroup(newGroups, "first group", ["foo", "bar", "baz"])
    addGroup(newGroups, "second group", [zero: "zero", one: "uno", two: "dos", three: "tres"])

// simple list
    addGroup(["a", "b", "c"])

// simple map
    addGroup(["a": "yes", "b": "no", "c": "maybe"])​​​​
*/


def inputSelectionPage() {

    def englishOptions = ["One", "Two", "Three"]
    def spanishOptions = ["Uno", "Dos", "Tres"]
    def groupedOptions = []
    addGroup(groupedOptions, "English", englishOptions)
    addGroup(groupedOptions, "Spanish", spanishOptions)

    dynamicPage(name: "inputSelectionPage") {

        section("options variations") {
            paragraph "tap these elements and look at the differences when selecting an option"
            input(type: "enum", name: "selectionSimple", title: "Simple options", description: "no separators in the selectable options", options: ["Thing 1", "Thing 2", "(Complicated) Thing 3"])
            input(type: "enum", name: "selectionSimpleGrouped", title: "Simple (Grouped) options", description: "no separators in the selectable options", groupedOptions: addGroup(englishOptions + spanishOptions))
            input(type: "enum", name: "selectionGrouped", title: "Grouped options", description: "separate groups of options with headers", groupedOptions: groupedOptions)
        }

        section("list vs map") {
            paragraph "These should be identical in UI, but are different in code and will produce different settings"
            input(type: "enum", name: "selectionList", title: "Choose a device", description: "settings will be something like ['Device1 Label']", groupedOptions: addGroup(["Device1 Label", "Device2 Label"]))
            input(type: "enum", name: "selectionMap", title: "Choose a device", description: "settings will be something like ['device1-id']", groupedOptions: addGroup(["device1-id": "Device1 Label", "device2-id": "Device2 Label"]))
        }

        section("segmented") {
            paragraph "segmented should only work if there are either 2 or 3 options to choose from"
            input(type: "enum", name: "selectionSegmented1", style: "segmented", title: "1 option", options: ["One"])
            input(type: "enum", name: "selectionSegmented4", style: "segmented", title: "4 options", options: ["One", "Two", "Three", "Four"])

            paragraph "multiple and required will have no effect on segmented selection elements. There will always be exactly 1 option selected"
            input(type: "enum", name: "selectionSegmented2", style: "segmented", title: "2 options", options: ["One", "Two"])
            input(type: "enum", name: "selectionSegmented3", style: "segmented", title: "3 options", options: ["One", "Two", "Three"])

            paragraph "specifying defaultValue still works with segmented selection elements"
            input(type: "enum", name: "selectionSegmentedWithDefault", style: "segmented", title: "defaulted to 'two'", options: ["One", "Two", "Three"], defaultValue: "Two")
        }

        section("required: true") {
            input(type: "enum", name: "selectionRequired", title: "This is required", description: "It should look different when nothing is selected", groupedOptions: addGroup(["only option"]), required: true)
        }

        section("multiple: true") {
            input(type: "enum", name: "selectionMultiple", title: "This allows multiple selections", description: "It should look different when nothing is selected", groupedOptions: addGroup(["an option", "another option", "no way, one more?"]), multiple: true)
            input(type: "enum", name: "selectionMultipleDefault1", title: "This allows multiple selections with a single default", description: "It should look different when nothing is selected", groupedOptions: addGroup(["an option", "another option", "no way, one more?"]), multiple: true, defaultValue: "an option")
            input(type: "enum", name: "selectionMultipleDefault2", title: "This allows multiple selections with multiple defaults", description: "It should look different when nothing is selected", groupedOptions: addGroup(["an option", "another option", "no way, one more?"]), multiple: true, defaultValue: ["an option", "another option"])
        }

        section("with image") {
            input(type: "enum", name: "selectionWithImage", title: "This has an image", description: "and a description", groupedOptions: addGroup(["an option", "another option", "no way, one more?"]), image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
        }
    }
}
def inputTextPage() {
    dynamicPage(name: "inputTextPage", title: "Every 'text' variation") {
        section("style and functional differences") {
            input(type: "text", name: "textRequired", title: "required: true", description: "This should look different when nothing has been entered", required: true)
            input(type: "text", name: "textWithImage", title: "with image", description: "This should look different when nothing has been entered", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", required: false)
        }
        section("text") {
            input(type: "text", name: "text", title: "This has an alpha-numeric keyboard", description: "no special formatting", required: false)
        }
        section("password") {
            input(type: "password", name: "password", title: "This has an alpha-numeric keyboard", description: "masks value", required: false)
        }
        section("email") {
            input(type: "email", name: "email", title: "This has an email-specific keyboard", description: "no special formatting", required: false)
        }
        section("phone") {
            input(type: "phone", name: "phone", title: "This has a numeric keyboard", description: "formatted for phone numbers", required: false)
        }
        section("decimal") {
            input(type: "decimal", name: "decimal", title: "This has an numeric keyboard with decimal point", description: "no special formatting", required: false)
        }
        section("number") {
            input(type: "number", name: "number", title: "This has an numeric keyboard without decimal point", description: "no special formatting", required: false)
        }

        section("specified ranges") {
            paragraph "You can limit number and decimal inputs to a specific range."
            input(range: "50..150", type: "decimal", name: "decimalRange50..150", title: "only values between 50 and 150 will pass validation", description: "no special formatting", required: false)
            paragraph "Negative limits will add a negative symbol to the keyboard."
            input(range: "-50..50", type: "number", name: "numberRange-50..50", title: "only values between -50 and 50 will pass validation", description: "no special formatting", required: false)
            paragraph "Specify * to not limit one side or the other."
            input(range: "*..0", type: "decimal", name: "decimalRange*..0", title: "only negative values will pass validation", description: "no special formatting", required: false)
            input(range: "*..*", type: "number", name: "numberRange*..*", title: "only positive values will pass validation", description: "no special formatting", required: false)
            paragraph "If you don't specify a range, it defaults to 0..*"
        }
    }
}
def inputTimePage() {
    dynamicPage(name: "inputTimePage") {
        section {
            input(type: "time", name: "timeWithDescription", title: "a time picker", description: "with a description", required: false)
            input(type: "time", name: "timeWithoutDescription", title: "without a description", description: null, required: false)
            input(type: "time", name: "timeRequired", title: "required: true", required: true)
        }
    }
}

/// selection subsets
def inputDevicePage() {

    dynamicPage(name: "inputDevicePage") {

        section("required: true") {
            input(type: "device.switch", name: "deviceRequired", title: "This is required", description: "It should look different when nothing is selected")
        }

        section("multiple: true") {
            input(type: "device.switch", name: "deviceMultiple", title: "This is required", description: "It should look different when nothing is selected", multiple: true)
        }

        section("with image") {
            input(type: "device.switch", name: "deviceRequired", title: "This has an image", description: "and a description", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
        }
    }
}
def inputCapabilityPage() {

    dynamicPage(name: "inputCapabilityPage") {

        section("required: true") {
            input(type: "capability.switch", name: "capabilityRequired", title: "This is required", description: "It should look different when nothing is selected")
        }

        section("multiple: true") {
            input(type: "capability.switch", name: "capabilityMultiple", title: "This is required", description: "It should look different when nothing is selected", multiple: true)
        }

        section("with image") {
            input(type: "capability.switch", name: "capabilityRequired", title: "This has an image", description: "and a description", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
        }
    }
}
def inputRoomPage() {

    dynamicPage(name: "inputRoomPage") {

        section("required: true") {
            input(type: "room", name: "roomRequired", title: "This is required", description: "It should look different when nothing is selected")
        }

        section("multiple: true") {
            input(type: "room", name: "roomMultiple", title: "This is required", description: "It should look different when nothing is selected", multiple: true)
        }

        section("with image") {
            input(type: "room", name: "roomRequired", title: "This has an image", description: "and a description", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
        }
    }
}
def inputModePage() {

    dynamicPage(name: "inputModePage") {

        section("required: true") {
            input(type: "mode", name: "modeRequired", title: "This is required", description: "It should look different when nothing is selected")
        }

        section("multiple: true") {
            input(type: "mode", name: "modeMultiple", title: "This is required", description: "It should look different when nothing is selected", multiple: true)
        }

        section("with image") {
            input(type: "mode", name: "modeRequired", title: "This has an image", description: "and a description", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
        }
    }
}
def inputHubPage() {

    dynamicPage(name: "inputHubPage") {

        section("required: true") {
            input(type: "hub", name: "hubRequired", title: "This is required", description: "It should look different when nothing is selected")
        }

        section("multiple: true") {
            input(type: "hub", name: "hubMultiple", title: "This is required", description: "It should look different when nothing is selected", multiple: true)
        }

        section("with image") {
            input(type: "hub", name: "hubRequired", title: "This has an image", description: "and a description", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
        }
    }
}
def inputContactBookPage() {

    dynamicPage(name: "inputContactBookPage") {

        section("required: true") {
            input(type: "contact", name: "contactRequired", title: "This is required", description: "It should look different when nothing is selected")
        }

        section("multiple: true") {
            input(type: "contact", name: "contactMultiple", title: "This is required", description: "It should look different when nothing is selected", multiple: true)
        }

        section("with image") {
            input(type: "contact", name: "contactRequired", title: "This has an image", description: "and a description", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
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
            paragraph "The difference between a label element and a text input element is that the label element will effect the SmartApp directly by setting the label. An input element will place the set value in the SmartApp's settings."
            paragraph "There are 3 here as an example. Never use more than 1 label element on a page."
            label(name: "label", title: "required:false, multiple:false", required: false, multiple: false)
            label(name: "labelRequired", title: "required:true", required: true, multiple: false)
            label(name: "labelWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
        }
    }
}

def modePage() {
    dynamicPage(name: "modePage", title: "Every 'mode' type") { // TODO: finish this
        section("mode") {
            paragraph "The difference between a mode element and a mode input element is that the mode element will effect the SmartApp directly by setting the modes it executes in. A mode input element will place the set value in the SmartApp's settings."
            paragraph "Another difference is that you can select 'All Modes' when choosing which mode the SmartApp should execute in. This is the same as selecting no modes. When a SmartApp does not have modes specified, it will execute in all modes."
            paragraph "There are 4 here as an example. Never use more than 1 mode element on a page."
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
            paragraph "This is how you should make a paragraph element"
            paragraph image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", "This is a long description, blah, blah, blah."
        }
    }
}

def hrefPage() {
    dynamicPage(name: "hrefPage", title: "Every 'href' variation") {
        section("stylistic differences") {
            href(page: "deadEnd", title: "state: 'complete'", description: "gives the appearance of an input that has been filled out", state: "complete")
            href(page: "deadEnd", title: "with image", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png")
            href(page: "deadEnd", title: "with image and description", description: "and state: 'complete'", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", state: "complete")
        }
        section("functional differences") {
            href(page: "deadEnd", title: "to a page within the app")
            href(url: "http://www.google.com", title: "to a url using all defaults")
            href(url: "http://www.google.com", title: "external: true", description: "takes you outside the app", external: true)
        }
    }
}

def buttonsPage() {
    dynamicPage(name: "buttonsPage", title: "Every 'button' type") {
        section("Simple Buttons") {
            paragraph "If there are an odd number of buttons, the last button will span the entire view area."
            buttons(name: "buttons1", title: "1 button", buttons: [
                    [label: "foo", action: "foo"]
            ])
            buttons(name: "buttons2", title: "2 buttons", buttons: [
                    [label: "foo", action: "foo"],
                    [label: "bar", action: "bar"]
            ])
            buttons(name: "buttons3", title: "3 buttons", buttons: [
                    [label: "foo", action: "foo"],
                    [label: "bar", action: "bar"],
                    [label: "baz", action: "baz"]
            ])
            buttons(name: "buttonsWithImage", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", buttons: [
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
            image(name: "imageWithMultipleImages", title: "This element has an image and a long title.", description: "I am setting long title and descriptions to test the offset", required: false, images: ["https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", "http://f.cl.ly/items/1k1S0A0m3805402o3O12/20130915-191127.jpg"])
        }
    }
}

def videoPage() {
    dynamicPage(name: "videoPage", title: "Every 'video' type") { // TODO: finish this
        section("video") {
            // TODO: update this when there is a videoElement method
            element(name: "videoElement", element: "video", type: "video", title: "this is a video!", description: "I am setting long title and descriptions to test the offset", required: false, image: "http://f.cl.ly/items/0w0D1p0K2D0d190F3H3N/Image%202015-12-14%20at%207.57.27%20AM.jpg", video: "http://f.cl.ly/items/3O2L03471l2K3E3l3K1r/Zombie%20Kid%20Likes%20Turtles.mp4")
        }
    }
}

def flattenedPage() {
    def allSections = []
    firstPage().sections[0].body.each { hrefElement ->
        if (hrefElement.name != "inputPage") {
            // inputPage is a bunch of hrefs
            allSections += "${hrefElement.page}"().sections
        }
    }
    // collect the input elements
    inputPage().sections.each { section ->
        section.body.each { hrefElement ->
            allSections += "${hrefElement.page}"().sections
        }
    }
    def flattenedPage = dynamicPage(name: "flattenedPage", title: "All elements in one page!") {}
    flattenedPage.sections = allSections
    return flattenedPage
}

def foo() {
    dynamicPage(name: "deadEnd") {
        section {  }
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
