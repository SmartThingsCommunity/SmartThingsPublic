/* eslint no-undef: 'off' */
const assert = require('assert').strict
const SmartApp = require('../../lib/smart-app')

describe('event-type-handler-spec', () => {
	/** @type {SmartApp} */
	let app
	let receivedEvent
	let receivedEventTime

	beforeEach(() => {
		app = new SmartApp({logUnhandledRejections: false})
	})

	it('should handle MODE_EVENT', () => {
		const expectedEvent = {
			'eventId': 'e9ede6a6-ade1-11e9-8a15-5998645d79ea',
			'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
			'modeId': '57ae8db9-9187-4293-bf7c-c0218825e94e'
		}
		const expectedEventTime = '2019-07-24T07:09:01Z'

		app.subscribedModeEventHandler('modeHandler', (_, event, eventTime) => {
			receivedEvent = event
			receivedEventTime = eventTime
		})
		app.handleMockCallback({
			'lifecycle': 'EVENT',
			'executionId': '66D91548-8643-444F-B402-F5CE832A5120',
			'locale': 'en-US',
			'version': '0.1.0',
			'eventData': {
				'authToken': 'WVlMWZjNThkNSIsInNjb3BlIjpbImk6ZGV2aWNlcHJvZmlsZXMiLCJyOmRldmljZXM6KiIsInI6bG9jYXRpb25zOioiLCJ4OmRldmljZXM6KiJdLCJleHAiOjE1NjM5NTI0NDEsImNsaWVudF9pZCI6ImVhODM5ZGJhLTVkZGEtNDFmYS1iMGU0LTgzOWYyOTRiNjJhMiJ9.mJ3cHGncbk6BCt-eEHOb-w3_F6RB_O2A48MzgCphfnOIHkF8WDoDPW-Hhu6c40kXGpGVAryQDHrNWB5NauYVztuYxHBrjFVpxgFDcVOd1zZjUbba4Aeflde8Q0cfPHWezJB3udsXCbRxO0BOXArQmWL_orVb1SX4U-A59anHOQ-x8zvKEYB_-swFSMF3bL2Sbz7-tCKREzMoWWXvJ5mVdek_QBQE0kHLYH0i-GOiKKNjTz2w8RrCOI9FR70cuSWyqG47Q5EtF7ESAHjIVmqtQcnuchK2zhhWnrJbicJ_XzvtPrJs1DBKzJUkNAsgR4RAA6GpixQZLnCrIw0HJU7oRA',
				'installedApp': {
					'installedAppId': 'a7dfaa78-cce7-459c-8d0c-d593f0d55b76',
					'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
					'config': {},
					'permissions': [
						'r:locations:*',
						'x:devices:*',
						'i:deviceprofiles',
						'r:devices:*'
					]
				},
				'events': [
					{
						'eventTime': '2019-07-24T07:09:01Z',
						'eventType': 'MODE_EVENT',
						'modeEvent': expectedEvent
					}
				]
			},
			'settings': {}
		})

		assert.deepStrictEqual(receivedEvent, expectedEvent)
		assert.equal(receivedEventTime, expectedEventTime)
	})

	it('should handle DEVICE_LIFECYCLE_EVENT', () => {
		const expectedEvent = {
			'lifecycle': 'UPDATE',
			'eventId': '091bd080-ade3-11e9-aa79-415d41e7ce77',
			'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
			'deviceId': 'ba0767c3-f883-438d-9db1-b1a84b8d347b',
			'deviceName': '',
			'principal': '',
			'update': {}
		}
		const expectedEventTime = '2019-07-24T07:17:03Z'

		app.subscribedDeviceLifecycleEventHandler('deviceLifecycle', (_, event, eventTime) => {
			receivedEvent = event
			receivedEventTime = eventTime
		})
		app.handleMockCallback({
			'lifecycle': 'EVENT',
			'executionId': '6b85d1aa-a059-fe2f-ac71-f863df37da65',
			'locale': 'en-US',
			'version': '0.1.0',
			'eventData': {
				'authToken': 'IjoiUlMyNTYifQ.eyJwcmluY2lwYWwiOiJpbnN0YWxsZWRhcHA6YTdkZmFhNzgtY2NlNy00NTljLThkMGMtZDU5M2YwZDU1Yjc2OjVmMjc4YmFhLWFmZjAtNGNmMC1hMzIzLTNkOWVlMWZjNThkNSIsInNjb3BlIjpbImk6ZGV2aWNlcHJvZmlsZXMiLCJyOmRldmljZXM6KiIsInI6bG9jYXRpb25zOioiLCJ4OmRldmljZXM6KiJdLCJleHAiOjE1NjM5NTI5MjMsImNsaWVudF9pZCI6ImVhODM5ZGJhLTVkZGEtNDFmYS1iMGU0LTgzOWYyOTRiNjJhMiJ9.FOZFZCBMMG-kMmRK8y_QDeA4722GuXEb86sKgDBeJN2WrizJr6Id8XKMvJAP9dR_yMzc4FlykZ0ln51YrLQ6DIUlzHuKL28dSU_RkJhkIMqTx8XxSOe7ae2nRaMdhtMnjx6t9NaxTMNkSFGYCG_McwHTHPGTDu0AxONHTzOjqECyYfSXR7BASkQqaDca0hbyfp0kqRnb_9HBdnE7gHYgwAMij_co2yGeBD4aMPIbr04OJSJsJZ89jfJ17CX6hsKKRkMwicauIQKzxgJnLLORuRX8amc_2fLEgbJSz1YKpJgSNLs555Kb3B31OU9LuqtsbNBTYg9MT9xnS4C90nNdFw',
				'installedApp': {
					'installedAppId': 'a7dfaa78-cce7-459c-8d0c-d593f0d55b76',
					'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
					'config': {},
					'permissions': [
						'r:locations:*',
						'x:devices:*',
						'i:deviceprofiles',
						'r:devices:*'
					]
				},
				'events': [
					{
						'eventTime': '2019-07-24T07:17:03Z',
						'eventType': 'DEVICE_LIFECYCLE_EVENT',
						'deviceLifecycleEvent': expectedEvent
					}
				]
			},
			'settings': {}
		})
		assert.deepStrictEqual(receivedEvent, expectedEvent)
		assert.equal(receivedEventTime, expectedEventTime)
	})

	it('should handle DEVICE_HEALTH_EVENT', () => {
		const expectedEvent = {
			'eventId': '0bde7df0-ade4-11e9-b187-3f8238130d63',
			'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
			'deviceId': '1ed46054-c643-4153-b50d-7509de9b237b',
			'hubId': '',
			'status': 'OFFLINE',
			'reason': 'SERVICE_UNAVAILABLE'
		}
		const expectedEventTime = '2019-07-24T07:23:17Z'

		app.subscribedDeviceHealthEventHandler('deviceHealth', (_, event, eventTime) => {
			receivedEvent = event
			receivedEventTime = eventTime
		})
		app.handleMockCallback({
			'lifecycle': 'EVENT',
			'executionId': '66b5208f-f3e2-403a-b2da-e2e34e009828',
			'locale': 'en-US',
			'version': '0.1.0',
			'eventData': {
				'authToken': 'LdnBwVkFNNTYtOFIwX21BIiwiYWxnIjoiUlMyNTYifQ.eyJwcmluY2lwYWwiOiJpbnN0YWxsZWRhcHA6YTdkZmFhNzgtY2NlNy00NTljLThkMGMtZDU5M2YwZDU1Yjc2OjVmMjc4YmFhLWFmZjAtNGNmMC1hMzIzLTNkOWVlMWZjNThkNSIsInNjb3BlIjpbImk6ZGV2aWNlcHJvZmlsZXMiLCJyOmRldmljZXM6KiIsInI6bG9jYXRpb25zOioiLCJ4OmRldmljZXM6KiJdLCJleHAiOjE1NjM5NTMzNTcsImNsaWVudF9pZCI6ImVhODM5ZGJhLTVkZGEtNDFmYS1iMGU0LTgzOWYyOTRiNjJhMiJ9.AJxVO9dlboeuRUBPbUKmaZ2IXYVDYkN5E4Usq6V3vuhPWcl57R5hkEmnREkjwih5FoxhRxbTvQotrm18K9Z-zTIHd9oOH2_pYsyWyaQIa_nPRlYxmu6s6gBM4uxHklS0Ez43bFSbUYMD9jSBPjbujnTPL1WUqF_x5o-zhXCwNXUN-yQ84pkHuuFCilNNCEqj5WTPFW-OxId4VDk3kx3snrk6w2bJzgbJ3G97v1EgXXMmbqGuzW0qwJNYv9jcCQTlE4WEsfsnyNmEmkzMGNls0e79v-0C_X0ES5EszoP6zNA7xPS0BOdZvWWDTfSILtTlnh6uJ7KOMulncqGZkl9ShQ',
				'installedApp': {
					'installedAppId': 'a7dfaa78-cce7-459c-8d0c-d593f0d55b76',
					'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
					'config': {},
					'permissions': [
						'r:locations:*',
						'x:devices:*',
						'i:deviceprofiles',
						'r:devices:*'
					]
				},
				'events': [
					{
						'eventTime': '2019-07-24T07:23:17Z',
						'eventType': 'DEVICE_HEALTH_EVENT',
						'deviceHealthEvent': expectedEvent
					}
				]
			},
			'settings': {}
		})
		assert.deepStrictEqual(receivedEvent, expectedEvent)
		assert.equal(receivedEventTime, expectedEventTime)
	})

	it('should handle HUB_HEALTH_EVENT', () => {
		const expectedEvent = {
			'eventId': '12347df0-ade4-11e9-b187-3f8238130d63',
			'locationId': '66b5208f-f3e2-4a1a-c3eb-e2e34e009828',
			'hubId': '1935208f-f4d2-4a1a-c3eb-e2e45e008928',
			'status': 'OFFLINE'
		}
		const expectedEventTime = '2019-07-24T07:23:17Z'

		app.subscribedHubHealthEventHandler('hubHealth', (_, event, eventTime) => {
			receivedEvent = event
			receivedEventTime = eventTime
		})
		app.handleMockCallback({
			'lifecycle': 'EVENT',
			'executionId': '66b5208f-f3e2-403a-b2da-e2e34e009828',
			'locale': 'en-US',
			'version': '0.1.0',
			'eventData': {
				'installedApp': {
					'installedAppId': 'a7dfaa78-cce7-459c-8d0c-d593f0d55b76',
					'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
					'config': {},
					'permissions': [
						'r:locations:*',
						'x:devices:*',
						'i:deviceprofiles',
						'r:devices:*'
					]
				},
				'events': [
					{
						'eventTime': '2019-07-24T07:23:17Z',
						'eventType': 'HUB_HEALTH_EVENT',
						'hubHealthEvent': expectedEvent
					}
				]
			},
			'settings': {}
		})
		assert.deepStrictEqual(receivedEvent, expectedEvent)
		assert.equal(receivedEventTime, expectedEventTime)
	})

	it('should handle SECURITY_ARM_STATE_EVENT', () => {
		const expectedEvent = {
			'eventId': '12347df0-ade4-11e9-b187-3f8238130d63',
			'armState': 'UNKNOWN',
			'locationId': '66b5208f-f3e2-4a1a-c3eb-e2e34e009828',
			'optionalArguments': {
				'property1': {
					'valueType': 'NULL_VALUE',
					'intValue': 0,
					'doubleValue': 0,
					'stringValue': 'string',
					'boolValue': true
				}
			}
		}
		const expectedEventTime = '2019-07-24T07:24:17Z'

		app.subscribedSecurityArmStateEventHandler('securityArmState', (_, event, eventTime) => {
			receivedEvent = event
			receivedEventTime = eventTime
		})
		app.handleMockCallback({
			'lifecycle': 'EVENT',
			'executionId': '66b5208f-f3e2-403a-b2da-e2e34e009828',
			'locale': 'en-US',
			'version': '0.1.0',
			'eventData': {
				'installedApp': {
					'installedAppId': 'a7dfaa78-cce7-459c-8d0c-d593f0d55b76',
					'locationId': '5f278baa-aff0-4cf0-a323-3d9ee1fc58d5',
					'config': {},
					'permissions': [
						'r:locations:*',
						'x:devices:*',
						'i:deviceprofiles',
						'r:devices:*'
					]
				},
				'events': [
					{
						'eventTime': '2019-07-24T07:24:17Z',
						'eventType': 'SECURITY_ARM_STATE_EVENT',
						'securityArmStateEvent': expectedEvent
					}
				]
			},
			'settings': {}
		})
		assert.deepStrictEqual(receivedEvent, expectedEvent)
		assert.equal(receivedEventTime, expectedEventTime)
	})
})
