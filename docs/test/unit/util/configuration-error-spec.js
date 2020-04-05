/* eslint-disable no-undef, no-unused-expressions */
const {expect} = require('chai')
const ConfigurationError = require('../../../lib/util/configuration-error')

describe('configuration-error-spec', () => {
	it('should return an undefined message if not set', () => {
		const error = new ConfigurationError()
		expect(error.message).to.be.undefined
	})

	it('should return an undefined message if not set', () => {
		const error = new ConfigurationError('My Error')
		expect(error.message).to.be.equal('My Error')
	})

	it('should return the message by `toString()` usage', () => {
		const error = new ConfigurationError('My Error')
		expect(error.toString()).to.be.equal('My Error')
	})

	it('should include the message key', () => {
		const error = new ConfigurationError()
		expect(error).to.include.keys('message')
	})
})
