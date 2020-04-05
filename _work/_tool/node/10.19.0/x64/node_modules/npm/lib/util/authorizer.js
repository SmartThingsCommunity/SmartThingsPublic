'use strict'

const fs = require('fs')
const axios = require('axios')
const sshpk = require('sshpk')
const NodeCache = require('node-cache')
const httpSignature = require('http-signature')
const Log = require('./log')

const invalidPublicKey = 'INVALID'
const smartthingsKeyApiHost = 'https://key.smartthings.com'

/**
 * @typedef KeyResolverOptions
 * @property {Log=} logger Logging utility
 * @property {String|Object=} publicKey Public key
 * @property {number} [keyCacheTTL=86400000] Lifetime of key cache in milliseconds. Default 24 hours.
 * @property {String} [keyApiHost='https://key.smartthings.com'] SmartThings Key API host
 */

class LocalKeyResolver {
	/**
	 * Creates an instance of LocalKeyResolver.
	 * @param {KeyResolverOptions} options Override default options
	 * @memberof LocalKeyResolver
	 */
	constructor(options) {
		this._publicKey = invalidPublicKey
		if (options.publicKey) {
			this.setPublicKey(options.publicKey)
		}
	}

	/**
	 * Set the public key with raw text, or point to a file with the prefix `@`
	 *
	 * @param {String} key Key contents or key path
	 * @returns {Object} Public key
	 */
	setPublicKey(key) {
		if (key.startsWith('@')) {
			this._publicKey = fs.readFileSync(key.slice(1), 'utf8')
		} else {
			this._publicKey = key
		}

		return this._publicKey
	}

	/**
	 * Get Public Key for specified Key ID.
	 *
	 * @param {String} keyId The Key ID as specified on Authorization header.
	 * @returns {Promise.<Object>} Promise of Public key or null if no key available.
	 */
	async getKey(keyId) { // eslint-disable-line no-unused-vars
		return this._publicKey
	}
}

class HttpKeyResolver {
	/**
	 * Creates an instance of HttpKeyResolver.
	 * @param {KeyResolverOptions} [options={}] Override default options
	 * @memberof HttpKeyResolver
	 */
	constructor(options = {}) {
		this._cache = new NodeCache()
		this._cacheTTL = options.keyCacheTTL || (24 * 60 * 60 * 1000) // 24 hours in millis
		this._keyApiHost = options.keyApiHost || smartthingsKeyApiHost
	}

	/**
	 * Get Public Key for specified Key ID.
	 *
	 * @param {String} keyId The Key ID as specified on Authorization header.
	 * @returns {Promise.<Object>} Promise of Public key or null if no key available.
	 */
	async getKey(keyId) {
		const cache = this._cache
		const cacheTTL = this._cacheTTL
		if (!keyId) {
			return null
		}

		let publicKey = cache.get(keyId)

		if (publicKey) {
			return publicKey
		}

		const response = await axios.get(`${this._keyApiHost}${keyId}`)
		const cert = sshpk.parseCertificate(response.data, 'pem')
		if (cert && cert.subjectKey) {
			publicKey = cert.subjectKey
		}

		if (publicKey) {
			cache.set(keyId, publicKey, cacheTTL)
			return publicKey
		}

		return null
	}
}

class DefaultKeyResolver {
	/**
	 * Creates an instance of DefaultKeyResolver.
	 * @param {KeyResolverOptions} options Override default options
	 * @memberof DefaultKeyResolver
	 */
	constructor(options) {
		this._localKeyResolve = new LocalKeyResolver(options)
		this._httpKeyResolve = new HttpKeyResolver(options)
	}

	/**
	 * Set the public key with raw text, or point to a file with the prefix `@`
	 *
	 * @param {String} key Key contents or key path
	 * @returns {Object} Public key
	 */
	setPublicKey(key) {
		return this._localKeyResolve.setPublicKey(key)
	}

	/**
	 * Get Public Key for specified Key ID.
	 *
	 * @param {String} keyId The Key ID as specified on Authorization header.
	 * @returns {Promise.<Object>} Promise of Public key or null if no key available.
	 */
	async getKey(keyId) {
		if (keyId && keyId.startsWith('/SmartThings')) {
			return this._localKeyResolve.getKey(keyId)
		}

		return this._httpKeyResolve.getKey(keyId)
	}
}

/**
 * Resolves public keys from available resources
 */
module.exports = class Authorizer {
	/**
	 * Creates an instance of Authorizer.
	 * @param {KeyResolverOptions} [options={}] Override default options
	 */
	constructor(options = {}) {
		this._logger = options.logger || new Log()
		this._keyResolver = new DefaultKeyResolver(options)
	}

	/**
	 * Set the public key with raw text, or point to a file with the prefix `@`
	 *
	 * @param {String} key Key contents or key path
	 * @returns {Object} Public key
	 */
	setPublicKey(key) {
		return this._keyResolver.setPublicKey(key)
	}

	async getKey(keyId) {
		return this._keyResolver.getKey(keyId)
	}

	/**
	 * @param {any} req Incoming request
	 */
	async isAuthorized(req) {
		try {
			const keyResolver = this._keyResolver
			const parsed = httpSignature.parseRequest(req, undefined)
			const publicKey = await keyResolver.getKey(parsed.keyId)

			if (httpSignature.verifySignature(parsed, publicKey)) {
				return true
			}

			this._logger.error('Forbidden - failed verifySignature')
			return false
		} catch (error) {
			this._logger.exception(error)
			return false
		}
	}
}
