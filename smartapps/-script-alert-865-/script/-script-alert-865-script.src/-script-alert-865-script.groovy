/**
 *  &gt;&quot;&#39;&gt;&lt;script&gt;alert(865)&lt;/script&gt;
 *
 *  Copyright 2016 &gt;&quot;&#39;&gt;&lt;script&gt;alert(865)&lt;/script&gt;
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
    name: "&gt;&quot;&#39;&gt;&lt;script&gt;alert(865)&lt;/script&gt;",
    namespace: "&gt;&quot;&#39;&gt;&lt;script&gt;alert(865)&lt;/script&gt;",
    author: "&gt;&quot;&#39;&gt;&lt;script&gt;alert(865)&lt;/script&gt;",
    description: "&gt;\&quot;&#39;&gt;&lt;script&gt;alert(865)&lt;/script&gt;",
    category: "",
    iconUrl: "&gt;&quot;&#39;&gt;&lt;script&gt;alert(865)&lt;/script&gt;",
    iconX2Url: "&gt;&quot;&#39;&gt;&lt;script&gt;alert(865)&lt;/script&gt;",
    iconX3Url: "&gt;&quot;&#39;&gt;&lt;script&gt;alert(865)&lt;/script&gt;",
    oauth: [displayName: ">"'><script>alert(865)</script>", displayLink: ">"'><script>alert(865)</script>"])


preferences {
	section("Title") {
		// TODO: put inputs here
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

// TODO: implement event handlers