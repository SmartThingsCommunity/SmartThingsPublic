/* eslint no-undef: 'off' */
const assert = require('assert').strict
const SmartApp = require('../../../lib/smart-app')

describe('execute-event-spec', () => {
	/** @type {SmartApp} */
	let app
	let receivedEvent

	beforeEach(() => {
		app = new SmartApp({logUnhandledRejections: false})
	})

	it('should handle EXECUTE lifecycle', async () => {
		const expectedEvent = {
			'authToken': 'xxxxx',
			'installedApp': {
				'installedAppId': 'd692699d-e7a6-400d-a0b7-d5be96e7a564',
				'locationId': 'e675a3d9-2499-406c-86dc-8a492a886494',
				'config': {
					'contactSensor': [
						{
							'valueType': 'DEVICE',
							'deviceConfig': {
								'deviceId': 'e457978e-5e37-43e6-979d-18112e12c961',
								'componentId': 'main'
							}
						}
					],
					'lightSwitch': [
						{
							'valueType': 'DEVICE',
							'deviceConfig': {
								'deviceId': '74aac3bb-91f2-4a88-8c49-ae5e0a234d76',
								'componentId': 'main'
							}
						}
					]
				},
				'permissions': [
					'r:devices:e457978e-5e37-43e6-979d-18112e12c961',
					'x:devices:74aac3bb-91f2-4a88-8c49-ae5e0a234d76'
				]
			},
			'parameters': {
				'property1': 'Property 1 value',
				'property2': 'Property 2 value'
			}
		}
		const expectedResponse = {
			statusCode: 200,
			executeData: {
				item1: 'Returned value 1',
				item2: 'Returned value 2'
			}
		}
		app.executeHandler((_, event) => {
			receivedEvent = event
			return {
				item1: 'Returned value 1',
				item2: 'Returned value 2'
			}
		})
		const response = await app.handleMockCallback({
			'lifecycle': 'EXECUTE',
			'executionId': 'b328f242-c602-4204-8d73-33c48ae180af',
			'appId': '9871bcc7-36d8-4b5a-ad46-de1645b8ff3e',
			'locale': 'en',
			'executeData': {
				'authToken': 'xxxxx',
				'installedApp': {
					'installedAppId': 'd692699d-e7a6-400d-a0b7-d5be96e7a564',
					'locationId': 'e675a3d9-2499-406c-86dc-8a492a886494',
					'config': {
						'contactSensor': [
							{
								'valueType': 'DEVICE',
								'deviceConfig': {
									'deviceId': 'e457978e-5e37-43e6-979d-18112e12c961',
									'componentId': 'main'
								}
							}
						],
						'lightSwitch': [
							{
								'valueType': 'DEVICE',
								'deviceConfig': {
									'deviceId': '74aac3bb-91f2-4a88-8c49-ae5e0a234d76',
									'componentId': 'main'
								}
							}
						]
					},
					'permissions': [
						'r:devices:e457978e-5e37-43e6-979d-18112e12c961',
						'x:devices:74aac3bb-91f2-4a88-8c49-ae5e0a234d76'
					]
				},
				'parameters': {
					'property1': 'Property 1 value',
					'property2': 'Property 2 value'
				}
			},
			'settings': {}
		})

		assert.deepStrictEqual(receivedEvent, expectedEvent)
		assert.deepStrictEqual(response, expectedResponse)
	})
})
