/* eslint no-undef: 'off' */
const assert = require('assert').strict
const SmartApp = require('../../../lib/smart-app')

describe('uninstalled-event-spec', () => {
	/** @type {SmartApp} */
	let app
	let receivedEvent

	beforeEach(() => {
		app = new SmartApp({logUnhandledRejections: false})
	})

	it('should handle UNINSTALL lifecycle', () => {
		const expectedEvent = {
			'installedApp': {
				'installedAppId': 'd46a1c60-b6bd-4f82-b124-028e0f14a4f4',
				'locationId': 'e1e66eab-1eab-4f09-9bb6-91da6585576d',
				'config': {},
				'permissions': []
			}
		}
		app.uninstalled((_, event) => {
			receivedEvent = event
		})
		app.handleMockCallback({
			'lifecycle': 'UNINSTALL',
			'executionId': 'e139dc1f-ee24-3c1a-309e-48669006817f',
			'locale': 'en-US',
			'version': '0.1.0',
			'uninstallData': {
				'installedApp': {
					'installedAppId': 'd46a1c60-b6bd-4f82-b124-028e0f14a4f4',
					'locationId': 'e1e66eab-1eab-4f09-9bb6-91da6585576d',
					'config': {},
					'permissions': []
				}
			},
			'settings': {}
		})

		assert.deepStrictEqual(receivedEvent, expectedEvent)
	})

	it('should handle UNINSTALL event', () => {
		const expectedEvent = {
			'installedApp': {
				'installedAppId': '8f8004ac-789e-40a3-84db-896545a112f8',
				'locationId': 'e1e66eab-1eab-4f09-9bb6-91da6585576d'
			}
		}
		app.uninstalled((_, event) => {
			receivedEvent = event
		})
		app.handleMockCallback({
			'messageType': 'EVENT',
			'eventData': {
				'installedApp': {
					'installedAppId': '8f8004ac-789e-40a3-84db-896545a112f8',
					'locationId': 'e1e66eab-1eab-4f09-9bb6-91da6585576d'
				},
				'events': [
					{
						'eventTime': '2019-08-20T16:36:06Z',
						'eventType': 'INSTALLED_APP_LIFECYCLE_EVENT',
						'installedAppLifecycleEvent': {
							'eventId': '9ba30d8d-c368-11e9-80a8-1fbacc5808f3',
							'locationId': 'e1e66eab-1eab-4f09-9bb6-91da6585576d',
							'installedAppId': '8f8004ac-789e-40a3-84db-896545a112f8',
							'appId': '4406788a-c401-4752-a8ce-af7cf75d06d3',
							'lifecycle': 'DELETE',
							'delete': {}
						}
					}
				]
			}
		})

		assert.deepStrictEqual(receivedEvent, expectedEvent)
	})
})
