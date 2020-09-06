/**
 *  Copyright 2020 Jose Augusto Baranauskas
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
 * This empty DH is used as a childDevice by Parents DHs ou SmartApps
 */
 metadata {
  definition (
    name: "Empty Contact Sensor",
    namespace: "baranauskas",
    author: "Jose Augusto Baranauskas",
    runLocally: true,
    minHubCoreVersion: '000.021.00001',
    vid:"generic-contact"
  ) {
  capability "Contact Sensor"
  }
}