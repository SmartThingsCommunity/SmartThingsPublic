/* eslint-disable no-undef, no-unused-expressions */
const sinon = require('sinon')
const {expect} = require('chai')
const Log = require('../../../lib/util/log')

const event = {
	lifecycle: 'CONFIGURATION',
	executionId: 'e6903fe6-f88f-da69-4c12-e2802606ccbc',
	locale: 'en',
	version: '0.1.0',
	client: {
		os: 'ios',
		version: '0.0.0',
		language: 'en-US'
	},
	configurationData: {
		installedAppId: '7d7fa36d-0ad9-4893-985c-6b75858e38e4',
		phase: 'INITIALIZE',
		pageId: '',
		previousPageId: '',
		config: {}
	},
	settings: {}
}

describe('log-spec', () => {
	it('should call console.log once', () => {
		const logger = new Log(console, 2, true)
		const cspy = sinon.spy(console, 'log')
		logger.event(event)
		expect(cspy.calledOnce).to.be.true
		cspy.restore()
	})

	it('should call event() once per event', () => {
		const log = new Log(undefined, 0, true)
		const spy = sinon.spy(log, 'event')
		log.event(event)
		expect(spy.calledOnce).to.be.true
		expect(spy.name).to.equal('event')
		expect(spy.calledWithExactly(event)).to.be.true
		sinon.restore()
	})

	it('should call response() once per response', () => {
		const log = new Log(undefined, 0, true)
		const spy = sinon.spy(log, 'response')
		log.response(event)
		expect(spy.calledOnce).to.be.true
		expect(spy.name).to.equal('response')
		expect(spy.calledWithExactly(event)).to.be.true
		sinon.restore()
	})

	it('should call debug() once', () => {
		const log = new Log()
		const spy = sinon.spy(log, 'debug')
		log.debug('debug')
		expect(spy.calledOnce).to.be.true
		expect(spy.name).to.equal('debug')
		expect(spy.calledWithExactly('debug')).to.be.true
		sinon.restore()
	})

	it('should call error() once', () => {
		const log = new Log()
		const spy = sinon.spy(log, 'error')
		log.error('error')
		expect(spy.calledOnce).to.be.true
		expect(spy.name).to.equal('error')
		expect(spy.calledWithExactly('error')).to.be.true
		sinon.restore()
	})

	it('should call error() once', () => {
		const log = new Log()
		const spy = sinon.spy(log, 'exception')
		log.exception('exception')
		expect(spy.calledOnce).to.be.true
		expect(spy.name).to.equal('exception')
		expect(spy.calledWithExactly('exception')).to.be.true
		sinon.restore()
	})

	it('should call info() once', () => {
		const log = new Log()
		const spy = sinon.spy(log, 'info')
		log.info('info')
		expect(spy.calledOnce).to.be.true
		expect(spy.name).to.equal('info')
		expect(spy.calledWithExactly('info')).to.be.true
		sinon.restore()
	})

	it('should call warn() once', () => {
		const log = new Log()
		const spy = sinon.spy(log, 'warn')
		log.warn('warn')
		expect(spy.calledOnce).to.be.true
		expect(spy.name).to.equal('warn')
		expect(spy.calledWithExactly('warn')).to.be.true
		sinon.restore()
	})
})
