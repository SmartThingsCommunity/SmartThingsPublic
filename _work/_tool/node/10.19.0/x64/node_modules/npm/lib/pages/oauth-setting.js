'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class OAuthSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'OAUTH'
	}

	/**
	 * The url to use for the OAuth service. Use __SmartThingsOAuthCallback__
	 * in the template for the callback/redirect url you need to provide
	 * to the OAuth service.
	 *
	 * @param {*} url Max length 2048 characters
	 * @returns {OAuthSetting} OAuth Setting instance
	 * @example
	 * var url = 'https://api.smartthings.com/oauth/callback?param1=1&param2=2&callback=__SmartThingsOAuthCallback__'
	 * section.oauthSetting('id').url(url)
	 */
	urlTemplate(url) {
		this._urlTemplate = url
		return this
	}

	/**
	 * Style of the setting
	 *
	 * @param {('COMPLETE','ERROR','DEFAULT')} style
	 * @returns {OAuthSetting} OAuth Setting instance
	 */
	style(style) {
		this._style = style
		return this
	}

	toJson() {
		const result = super.toJson()
		result.urlTemplate = this._urlTemplate

		if (this._style) {
			result.style = this._style
		}

		return result
	}
}
