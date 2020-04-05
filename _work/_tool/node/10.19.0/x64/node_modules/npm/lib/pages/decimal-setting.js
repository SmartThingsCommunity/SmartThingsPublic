'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class DecimalSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'DECIMAL'
		this._description = 'Tap to set'
		this._postMessage = this.i18nKey('postMessage')
	}

	/**
	 * The maximum inclusive value the decimal can be set to.
	 *
	 * @param {number} value Maximum value
	 * @returns {DecimalSetting} Decimal Setting instance
	 */
	max(value) {
		this._max = value
		return this
	}

	/**
	 * The minimum inclusive value the decimal can be set to.
	 *
	 * @param {number} value Minimum value
	 * @returns {DecimalSetting} Decimal Setting instance
	 */
	min(value) {
		this._min = value
		return this
	}

	/**
	 * Set an image icon
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {DecimalSetting} Decimal Setting instance
	 */
	image(source) {
		this._image = source
		return this
	}

	/**
	 * A string to be shown after the text input field.
	 *
	 * @param {String} value Max length 10 characters
	 * @returns {DecimalSetting} Decimal Setting instance
	 */
	postMessage(value) {
		this._postMessage = value
		return this
	}

	toJson() {
		const result = super.toJson()
		if (this._max) {
			result.max = this._max
		}

		if (this._min) {
			result.min = this._min
		}

		if (this._image) {
			result.image = this._image
		}

		if (this._postMessage) {
			result.postMessage = this._postMessage
		}

		return result
	}
}
