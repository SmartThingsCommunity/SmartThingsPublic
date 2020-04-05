/* eslint no-undef: 'off' */
const assert = require('assert').strict
const SmartApp = require('../../../lib/smart-app')

describe('device-event-spec', () => {
	/** @type {SmartApp} */
	let app
	let receivedEvent
	let receivedEventTime

	beforeEach(() => {
		app = new SmartApp({logUnhandledRejections: false})
	})

	it('should handle DEVICE_EVENT lifecycle', () => {
		const expectedEvent = {
			'eventId': '0eabc76a-c366-11e9-84dd-3bdf0e366b96',
			'locationId': 'e1e66eab-1eab-4f09-9bb6-91da6585576d',
			'deviceId': '6cc2a018-a918-484e-a405-97838d874623',
			'componentId': 'main',
			'capability': 'contactSensor',
			'attribute': 'contact',
			'value': 'open',
			'valueType': 'string',
			'stateChange': true,
			'data': {},
			'subscriptionName': 'contactHandler_0'
		}
		const expectedEventTime = '1970-01-01T00:00:00Z'

		app.subscribedEventHandler('contactHandler', (_, event, eventTime) => {
			receivedEvent = event
			receivedEventTime = eventTime
		})
		app.handleMockCallback({
			'lifecycle': 'EVENT',
			'executionId': '3070a655-2094-469b-a2c8-6f0ac70a175e',
			'locale': 'en-US',
			'version': '0.1.0',
			'eventData': {
				'authToken': 'eyJraWQiOiI5dmtsOTkyYTE4ZmVNX045X2tuQ2I2dHh6Ri1LdnBwVkFNNTYtOFIwX21BIiwiYWxnIjoiUlMyNTYifQ.eyJwcmluY2lwYWwiOiJpbnN0YWxsZWRhcHA6ZDQ2YTFjNjAtYjZiZC00ZjgyLWIxMjQtMDI4ZTBmMTRhNGY0OmUxZTY2ZWFiLTFlYWItNGYwOS05YmI2LTkxZGE2NTg1NTc2ZCIsInNjb3BlIjpbInI6ZGV2aWNlczo2Y2MyYTAxOC1hOTE4LTQ4NGUtYTQwNS05NzgzOGQ4NzQ2MjMiLCJyOmRldmljZXM6NzJmMjM1YTMtMTc1Ni00YWZiLWI1ZWEtMGI4NjRkYTAyMDkxIiwieDpkZXZpY2VzOjcyZjIzNWEzLTE3NTYtNGFmYi1iNWVhLTBiODY0ZGEwMjA5MSJdLCJleHAiOjE1NjYzMTgxNzEsImNsaWVudF9pZCI6IjcyODg5NjhmLWQ1MjQtNDFhYy1iN2FiLTAyZDBmNzlhZmI1OCJ9.Xz5VlfQROXcveO9p1RtnFyE9QNVuClDsXvFt_ekFCtUtkgWJRcAUr3NyKzM7tqdsuryua0BaFLGGF2mnEYrKdrIbMYxCfzDwK9nSKa66yInH7LEpVSXRre6-0ZQUuPJhjYM7JOy3yTvCiuBbTazqhvTpcCXQTxDEFtDZRMw7FrZDOrPFbRa_QTm1fN2MkVKrbAxFbl98cgKRm4fY-y0mEBymUf14oe6YCAArLupMbAd6rgWCht99LwsXexalH0ytELVdph2eiAcepOhUzj9C77Ov05OSlj_-OOqULwFu2oY4B-vumZ9KtWjBXyhn3YEAqwaZiXK_uY_4Nd1B_McKZQ',
				'installedApp': {
					'installedAppId': 'd46a1c60-b6bd-4f82-b124-028e0f14a4f4',
					'locationId': 'e1e66eab-1eab-4f09-9bb6-91da6585576d',
					'config': {
						'contactSensor': [
							{
								'valueType': 'DEVICE',
								'deviceConfig': {
									'deviceId': '6cc2a018-a918-484e-a405-97838d874623',
									'componentId': 'main'
								}
							}
						],
						'lights': [
							{
								'valueType': 'DEVICE',
								'deviceConfig': {
									'deviceId': '72f235a3-1756-4afb-b5ea-0b864da02091',
									'componentId': 'main'
								}
							}
						]
					},
					'permissions': [
						'r:devices:72f235a3-1756-4afb-b5ea-0b864da02091',
						'r:devices:6cc2a018-a918-484e-a405-97838d874623',
						'x:devices:72f235a3-1756-4afb-b5ea-0b864da02091'
					]
				},
				'events': [
					{
						'eventTime': '1970-01-01T00:00:00Z',
						'eventType': 'DEVICE_EVENT',
						'deviceEvent': {
							'eventId': '0eabc76a-c366-11e9-84dd-3bdf0e366b96',
							'locationId': 'e1e66eab-1eab-4f09-9bb6-91da6585576d',
							'deviceId': '6cc2a018-a918-484e-a405-97838d874623',
							'componentId': 'main',
							'capability': 'contactSensor',
							'attribute': 'contact',
							'value': 'open',
							'valueType': 'string',
							'stateChange': true,
							'data': {},
							'subscriptionName': 'contactHandler_0'
						}
					}
				]
			},
			'settings': {}
		})

		assert.deepStrictEqual(receivedEvent, expectedEvent)
		assert.equal(receivedEventTime, expectedEventTime)
	})

	it('should handle DEVICE_EVENT lifecycle', () => {
		const expectedEvent = {
			'eventId': '4a8445f0-c360-11e9-9945-392003a092e5',
			'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
			'deviceId': '2e4b6630-41ad-4527-8b91-135021b0dbb7',
			'componentId': 'main',
			'capability': 'switch',
			'attribute': 'switch',
			'value': 'on',
			'valueType': 'string',
			'stateChange': true,
			'subscriptionName': 'switchHandler'
		}
		const expectedEventTime = '2019-08-20T15:36:34Z'

		app.subscribedEventHandler('switchHandler', (_, event, eventTime) => {
			receivedEvent = event
			receivedEventTime = eventTime
		})
		app.handleMockCallback({
			'messageType': 'EVENT',
			'eventData': {
				'installedApp': {
					'installedAppId': '07891f14-82da-4239-9900-42e437c49f45',
					'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5'
				},
				'events': [
					{
						'eventTime': '2019-08-20T15:36:34Z',
						'eventType': 'DEVICE_EVENT',
						'deviceEvent': {
							'eventId': '4a8445f0-c360-11e9-9945-392003a092e5',
							'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
							'deviceId': '2e4b6630-41ad-4527-8b91-135021b0dbb7',
							'componentId': 'main',
							'capability': 'switch',
							'attribute': 'switch',
							'value': 'on',
							'valueType': 'string',
							'stateChange': true,
							'subscriptionName': 'switchHandler'
						}
					}
				]
			}
		})

		assert.deepStrictEqual(receivedEvent, expectedEvent)
		assert.equal(receivedEventTime, expectedEventTime)
	})
})
