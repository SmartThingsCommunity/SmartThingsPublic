/* eslint no-undef: 'off' */
const {expect} = require('chai')
const sinon = require('sinon')
const SmartApp = require('../../../lib/smart-app')

describe('confirmation-spec', () => {
	it('auto confirmation default (disabled)', () => {
		const app = new SmartApp({logUnhandledRejections: false})
		const stub = sinon.stub(app._log, 'info')
		app.handleMockCallback({
			'messageType': 'CONFIRMATION',
			'confirmationData': {
				'appId': 'f9a665e7-5a76-4b1e-bdfe-31135eccc2f3',
				'confirmationUrl': 'https://api.smartthings.com/apps/f9a665e7-5a76-4b1e-bdfe-31135eccc2f3/confirm-registration?token=fd9581b5-628c-4cd7-b1c2-dc14761234f3'
			}
		})

		expect(stub.calledOnce).to.equal(true)
		expect(stub.calledWith('CONFIRMATION request for app f9a665e7-5a76-4b1e-bdfe-31135eccc2f3, to enable events visit https://api.smartthings.com/apps/f9a665e7-5a76-4b1e-bdfe-31135eccc2f3/confirm-registration?token=fd9581b5-628c-4cd7-b1c2-dc14761234f3')).to.equal(true)
		stub.restore()
	})

	it('auto confirmation enabled', () => {
		const app = new SmartApp({logUnhandledRejections: false, appId: 'f9a665e7-5a76-4b1e-bdfe-31135eccc2f3'})
		const stub = sinon.stub(app._log, 'info')
		app.handleMockCallback({
			'messageType': 'CONFIRMATION',
			'confirmationData': {
				'appId': 'f9a665e7-5a76-4b1e-bdfe-31135eccc2f3',
				'confirmationUrl': 'https://api.smartthings.com/apps/f9a665e7-5a76-4b1e-bdfe-31135eccc2f3/confirm-registration?token=fd9581b5-628c-4cd7-b1c2-dc14761234f3'
			}
		})

		expect(stub.calledOnce).to.equal(true)
		expect(stub.calledWith('CONFIRMATION request for app f9a665e7-5a76-4b1e-bdfe-31135eccc2f3, to enable events visit https://api.smartthings.com/apps/f9a665e7-5a76-4b1e-bdfe-31135eccc2f3/confirm-registration?token=fd9581b5-628c-4cd7-b1c2-dc14761234f3')).to.equal(true)
		stub.restore()
	})

	it('auto confirmation enabled, wrong appId', () => {
		const app = new SmartApp({logUnhandledRejections: false, appId: 'f9a665e7-5a76-4b1e-bdfe-31135ecccdef'})
		const stub = sinon.stub(app._log, 'warn')
		const body = {
			'messageType': 'CONFIRMATION',
			'confirmationData': {
				'appId': 'f9a665e7-5a76-4b1e-bdfe-31135eccc2f3',
				'confirmationUrl': 'https://api.smartthings.com/apps/f9a665e7-5a76-4b1e-bdfe-31135eccc2f3/confirm-registration?token=fd9581b5-628c-4cd7-b1c2-dc14761234f3'
			}
		}
		app.handleMockCallback(body)

		expect(stub.calledOnce).to.equal(true)
		expect(stub.calledWith(`Unexpected CONFIRMATION request for app f9a665e7-5a76-4b1e-bdfe-31135eccc2f3, received ${JSON.stringify(body)}`)).to.equal(true)
		stub.restore()
	})

	it('invalid confirmation event', () => {
		const app = new SmartApp({logUnhandledRejections: false, appId: 'f9a665e7-5a76-4b1e-bdfe-31135ecccdef'})
		const stub = sinon.stub(app._log, 'warn')
		const body = {
			'messageType': 'CONFIRMATION',
			'confirmationData': {
				'otherId': 'f9a665e7-5a76-4b1e-bdfe-31135eccc2f3',
				'confirmationUrl': 'https://api.smartthings.com/apps/f9a665e7-5a76-4b1e-bdfe-31135eccc2f3/confirm-registration?token=fd9581b5-628c-4cd7-b1c2-dc14761234f3'
			}
		}
		app.handleMockCallback(body)

		expect(stub.calledOnce).to.equal(true)
		expect(stub.calledWith(`Invalid CONFIRMATION request ${JSON.stringify(body)}`)).to.equal(true)
		stub.restore()
	})
})
