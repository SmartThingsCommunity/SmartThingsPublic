/**
 *  Backup Test
 *
 *  Copyright 2016 Chris Kitch
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
    name: "Backup Test",
    namespace: "kriskit",
    author: "Chris Kitch",
    description: "Testing the possibility of backing up an app to JSON and restoring it.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: true)

preferences {
	page(name: "configure")
    page(name: "backupPage")
    page(name: "doBackupPage")
    page(name: "restorePage")
    page(name: "doRestorePage")
}

def configure() {
	dynamicPage(name: "configure", uninstall: true, install: true) {
        section {
        	app(name: "kids", appName: "Backup Child", namespace: "kriskit", title: "Create Item...", multiple: true)
        }
        
        section(title: "Backup / Restore") {
        	href (name: "backup", title: "Backup", page: "backupPage")
            href (name: "restore", title: "Restore", page: "restorePage")
        }
    }
}

def backupPage() {
	dynamicPage(name: "backupPage", uninstall: false, install: false) {
    	def kids = getChildAppOptions()

        section {
        	if (kids) {
        		paragraph "Hello!"
                input "backupSpecific", "bool", title: "Backup specific things", required: true, defaultValue: false, submitOnChange: true
                
                if (backupSpecific)
            		input "toBackup", "enum", title: "Select what to back up", multiple: true, options: kids, submitOnChange: true	
                
                if (toBackup || !backupSpecific)
                	href (name: "doBackup", title: "Do Backup", page: "doBackupPage")
            } else {
            	paragraph "You have nothing to back up!"
            }
        }
    }
}

def doBackupPage() {
	dynamicPage(name: "doBackupPage", uninstall: false, install: false) {
        section {
        	def backupDescription = "everything"
            
            if (toBackup && !backupSpecific)
            	backupDescription = "${toBackup?.size()} thing(s)"

        	paragraph "This would otherwise be performing the backup of ${backupDescription}..."
        }
        section(title: "Data") {
        	def backupData = getBackupData()
            def uri = performBackup(backupData)
            state.backupUri = uri
        	paragraph "${backupData}", title: "The Backup Data"
            paragraph "${uri}", title: "The Backup Data URL"
        }
    }
}

def restorePage() {
	dynamicPage(name: "restorePage", uninstall: false, install: false) {
        section {
        	if (state.backupUri) {
        		paragraph "${state.backupUri}", title: "Restore from"
				href (name: "doRestore", title: "Do Restore", page: "doRestorePage")
            } else 
            	paragraph "No backup URL found."
        }
    }
}

def doRestorePage() {
	dynamicPage(name: "doRestorePage", uninstall: false, install: false) {
        section {
        	paragraph "This would otherwise be performing the restore..."
        }
        section(title: "Result") {
        	def restoreData = getRestoreData()
            performRestore(restoreData)
        	paragraph "${restoreData}"
        }
    }
}

def getChildAppOptions() {
	return childApps?.collect {
    	["${it.id}": it.label]
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

def getBackupData() {
	def apps = childApps

	if (backupSpecific && toBackup)
    	apps = getChildAppsByIds(toBackup)
    
    def result = []
    
    apps?.each {
    	result.add(it.getDescriptor())
    }
    
    def json = new groovy.json.JsonBuilder(result)    
	log.debug json
    
    return json.toPrettyString()
}

def getRestoreData() {
	if (!state.backupUri)
    	return

	def params = [
    	uri: state.backupUri,
        contentType: "application/json",
        requestContentType: "application/json"
    ]
    
    def result = null
    
    httpGet(params) { resp -> 
    	result = resp.data
    }

	return result
}

def getChildAppsByIds(ids) {
    return childApps?.findAll {
    	ids?.contains(it.id)
    }
}

def performBackup(data) {
	def params = [
    	uri: "https://api.myjson.com",
        path: "/bins",
        body: data,
        contentType: "application/json",
        requestContentType: "application/json"
    ]
    
    def result = null
    
	httpPost(params) { resp ->
     	result = resp.data.uri
    }
    
    return result
}

def performRestore(data) {
	if (!data)
    	return

	data?.each { bkp ->
    	def existingApp = childApps?.find { app -> app.label == bkp.label }
       	
        if (existingApp) {
        	log.debug "Existing app found ${bkp.label} updating settings..."
            existingApp.update(bkp.settings)
        } else
    		addChildApp("Backup Child", "kriskit", bkp)
    }
}
