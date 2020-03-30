'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class NumberSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'NUMBER'
		this._description = 'Tap to set'
	}

	/**
	 * The maximum inclusive value the number can be set to.
	 *
	 * @param {number} value Maximum value
	 * @returns {NumberSetting} Number Setting instance
	 */
	max(value) {
		this._max = value
		return this
	}

	/**
	 * The minimum inclusive value the number can be set to.
	 *
	 * @param {number} value Minimum value
	 * @returns {NumberSetting} Number Setting instance
	 */
	min(value) {
		this._min = value
		return this
	}

	/**
	 * The increment to step by.
	 *
	 * @param {number} value Increment value
	 * @default 1
	 * @returns {NumberSetting} Number Setting instance
	 */
	step(value) {
		this._step = value
		return this
	}

	/**
	 * The way to style the number input.
	 *
	 * @param {String} value Style value
	 * @default 'SLIDER'
	 * @returns {NumberSetting} Number Setting instance
	 */
	style(value) {
		this._style = value
		return this
	}

	/**
	 * Set an image icon
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {NumberSetting} Number Setting instance
	 */
	image(source) {
		this._image = source
		return this
	}

	/**
	 * A string to be shown after the text input field.
	 *
	 * @param {String} value Max length 10 characters
	 * @returns {NumberSetting} Number Setting instance
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

		if (this._step) {
			result.step = this._step
		}

		if (this._style) {
			result.style = this._style
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
