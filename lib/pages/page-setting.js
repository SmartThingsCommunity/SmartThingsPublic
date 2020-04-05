'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class PageSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'PAGE'
		this._page = id
	}

	/**
	 * The page to navigate to.
	 *
	 * @param {String} id Page id. Max length 10 characters.
	 * @returns {PageSetting} Page Setting instance
	 */
	page(id) {
		this._page = id
		return this
	}

	/**
	 * Set an image icon
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {PageSetting} Page Setting instance
	 */
	image(source) {
		this._image = source
		return this
	}

	/**
	 * Style of the setting
	 *
	 * @param {('COMPLETE', 'ERROR', 'DEFAULT', 'BUTTON')} value
	 * @returns {PageSetting} Page Setting instance
	 */
	style(value) {
		this._style = value
		return this
	}

	toJson() {
		const result = super.toJson()
		delete result.required
		if (this._page) {
			result.page = this._page
		}

		if (this._image) {
			result.image = this._image
		}

		if (this._style) {
			result.style = this._style
		}

		return result
	}
}
