'use strict'

class HttpResponder {
	constructor(response, log) {
		this._response = response
		this._log = log
	}

	respond(data) {
		this._log.response(data)
		if (data && data.statusCode) {
			this._response.statusCode = data.statusCode
		}

		this._response.json(data)
	}
}

class LambdaResponder {
	constructor(callback, log) {
		this._callback = callback
		this._log = log
	}

	respond(data) {
		this._log.response(data)
		this._callback(null, data)
	}
}

class MockResponder {
	constructor(log) {
		this._log = log
	}

	respond(data) {
		this._log.response(data)
		this.response = data
	}
}

module.exports = {
	httpResponder(response, log) {
		return new HttpResponder(response, log)
	},

	lambdaResponse(callback, log) {
		return new LambdaResponder(callback, log)
	},

	mockResponder(log) {
		return new MockResponder(log)
	}
}
