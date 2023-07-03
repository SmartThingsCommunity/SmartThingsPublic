/**
 *  Sleep Wizard
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
    name: "Sleep Routine",
    namespace: "smartsolutionsv2/sleeping",
    author: "SmartThings",
    description: "Create rules to control devices based on a sleep sensor, including in bed, out of bed, sleeping, and not sleeping.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartthings-plus/category-icons/sleepsense.png",
    iconX2Url: "https://s3.amazonaws.com/smartthings-plus/category-icons/sleepsense.png",
    iconX3Url: "https://s3.amazonaws.com/smartthings-plus/category-icons/sleepsense.png",
    singleInstance: true
)

preferences {
	page(name: "main", title: getLabel("str_Title"), install: true, uninstall: true) {
    	section {
        	app(name: "smartsleepautomation", appName: "Smart Sleep Automation", namespace: "smartsolutionsv2/sleeping", title: getLabel("str_SectionTitle"), multiple: true, uninstall: true)
        }
    }
}


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def initialize() {
    
    updateSolutionSummary();
}

def updateSolutionSummary() {
	def numChildApps = getChildApps().size();
    def text = "${numChildApps} rules configured";
    sendEvent(
    	linkText: numChildApps.toString(),
        descriptionText: next,
        eventType: "SOLUTION_SUMMARY",
        name: "summary",
        value: numChildApps,
        data: [["icon": "indicator-dot-gray", "iconColor": "#878787", "value":text]],
        displayed: false
    );
}


card("Action History") { 
     tiles { 
         eventTile { }
 
     } 
 } 



def getLabel(value)
{
    def str_Title=[
            "kr": "룰",
            "us": "Rules"
    ]

    def str_SectionTitle=[
            "kr": "새로운 자동화 룰 만들기",
            "us": "New Sleep Routine Automation"
    ]

    
    def lang = "us"

    if(lang == "ko") //clientLocale?.language
    {
        switch(value)
        {
            case "str_Title":
                return str_Title["kr"]
            case "str_SectionTitle":
                return str_SectionTitle["kr"]
        }
    }
    else
    {
        switch(value)
        {
            case "str_Title":
                return str_Title["us"]
            case "str_SectionTitle":
                return str_SectionTitle["us"]
        }
    }
    return "Unknown"
}

