'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class PasswordSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'PASSWORD'
		this._description = 'Tap to set'
	}

	/**
	 * The maximum length the password can have.
	 *
	 * @param {number} value
	 * @returns {PasswordSetting} Password Setting instance
	 */
	maxLength(value) {
		this._maxLength = value
		return this
	}

	/**
	 * The minimum length the password can have.
	 *
	 * @param {number} value
	 * @returns {PasswordSetting} Password Setting instance
	 */
	minLength(value) {
		this._minLength = value
		return this
	}

	/**
	 * Set an image icon
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {PasswordSetting} Password Setting instance
	 */
	image(source) {
		this._image = source
		return this
	}

	toJson() {
		const result = super.toJson()
		if (this._maxLength) {
			result.maxLength = this._maxLength
		}

		if (this._minLength) {
			result.minLength = this._minLength
		}

		if (this._image) {
			result.image = this._image
		}

		return result
	}
}
