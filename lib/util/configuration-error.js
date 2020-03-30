'use strict'

module.exports = class ConfigurationError {
	constructor(message) {
		this.message = message
	}

	toString() {
		return this.message
	}
}
