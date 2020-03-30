'use strict'

const DeviceSetting = require('./device-setting')
const OAuthSetting = require('./oauth-setting')
const TimeSetting = require('./time-setting')
const TextSetting = require('./text-setting')
const PasswordSetting = require('./password-setting')
const PhoneSetting = require('./phone-setting')
const EmailSetting = require('./email-setting')
const NumberSetting = require('./number-setting')
const DecimalSetting = require('./decimal-setting')
const BooleanSetting = require('./boolean-setting')
const ParagraphSetting = require('./paragraph-setting')
const LinkSetting = require('./link-setting')
const PageSetting = require('./page-setting')
const ImageSetting = require('./image-setting')
const ImagesSetting = require('./images-setting')
const VideoSetting = require('./video-setting')
const ColorSetting = require('./color-setting')
const EnumSetting = require('./enum-setting')
const SoundSetting = require('./sound-setting')
const SecuritySetting = require('./security-setting')
const ModeSetting = require('./mode-setting')
const SceneSetting = require('./scene-setting')
const MessageGroupSetting = require('./message-group-setting')

module.exports = class Section {
	constructor(page, name) {
		this._page = page
		this._name = page.headers ? this.i18nKey('name', name) : name
		this._settings = []
		this._defaultRequired = page._defaultRequired
	}

	/**
	 * Device Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./device-setting') } Device Setting instance
	 */
	deviceSetting(id) {
		const result = new DeviceSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * OAuth Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./oauth-setting') } OAuth Setting instance
	 */
	oauthSetting(id) {
		const result = new OAuthSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Time Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./time-setting') } Time Setting instance
	 */
	timeSetting(id) {
		const result = new TimeSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Text Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./text-setting') } Text Setting instance
	 */
	textSetting(id) {
		const result = new TextSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Password Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./password-setting') } Password Setting instance
	 */
	passwordSetting(id) {
		const result = new PasswordSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Phone Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./phone-setting') } Phone Setting instance
	 */
	phoneSetting(id) {
		const result = new PhoneSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Email Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./email-setting') } Email Setting instance
	 */
	emailSetting(id) {
		const result = new EmailSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Number Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./number-setting') } Number Setting instance
	 */
	numberSetting(id) {
		const result = new NumberSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Decimal Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./decimal-setting') } Decimal Setting instance
	 */
	decimalSetting(id) {
		const result = new DecimalSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Boolean Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./boolean-setting') } Boolean Setting instance
	 */
	booleanSetting(id) {
		const result = new BooleanSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Paragraph Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./paragraph-setting') } Paragraph Setting instance
	 */
	paragraphSetting(id) {
		const result = new ParagraphSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Link Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./link-setting') } Link Setting instance
	 */
	linkSetting(id) {
		const result = new LinkSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Page Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./page-setting') } Page Setting instance
	 */
	pageSetting(id) {
		const result = new PageSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Image Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./image-setting') } Image Setting instance
	 */
	imageSetting(id) {
		const result = new ImageSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Images Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./images-setting') } Images Setting instance
	 */
	imagesSetting(id) {
		const result = new ImagesSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Video Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./video-setting') } Video Setting instance
	 */
	videoSetting(id) {
		const result = new VideoSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Color Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./color-setting') } Color Setting instance
	 */
	colorSetting(id) {
		const result = new ColorSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Enum Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./enum-setting') } Enum Setting instance
	 */
	enumSetting(id) {
		const result = new EnumSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Security Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./security-setting') } Security Setting instance
	 */
	securitySetting(id) {
		const result = new SecuritySetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Sound Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./sound-setting') } Sound Setting instance
	 */
	soundSetting(id) {
		const result = new SoundSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Mode Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./mode-setting') } Mode Setting instance
	 */
	modeSetting(id) {
		const result = new ModeSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Scene Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./scene-setting') } Scene Setting instance
	 */
	sceneSetting(id) {
		const result = new SceneSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Message Group Setting
	 *
	 * @param {String} id Identifier used for i18n localization key
	 * @returns { import('./message-group-setting') } Message Group Setting instance
	 */
	messageGroupSetting(id) {
		const result = new MessageGroupSetting(this, id)
		this._settings.push(result)
		return result
	}

	/**
	 * Name of the section.
	 *
	 * @param {String} value Max length 128 characters.
	 * @returns {Section} Section instance
	 */
	name(value) {
		this._name = value
		return this
	}

	/**
	 * Whether or not the section can be collapsed.
	 *
	 * @param {Boolean} hideable
	 * @default false
	 * @returns {Section} Section instance
	 */
	hideable(value) {
		this._hideable = value
		return this
	}

	/**
	 * If section can be collapsed, whether or not it defaults to hidden.
	 *
	 * @param {Boolean} value
	 * @default false
	 * @returns {Section} Section instance
	 */
	hidden(value) {
		this._hidden = value
		return this
	}

	/**
	 * Automatically applies the `required: true` field to all section settings
	 *
	 * @param {Boolean} defaultRequired
	 * @default false
	 * @returns {Page} Section instance
	 */
	defaultRequired(defaultRequired) {
		this._defaultRequired = defaultRequired
		return this
	}

	/**
	 * Change how the section is presented
	 *
	 * @param {('NORMAL'|'FOOTER')} style Section style
	 * @default "NORMAL"
	 * @returns {Section} Section instance
	 */
	style(style) {
		this._style = style
		return this
	}

	i18nKey(property, value) {
		return this._page.i18nKey(`sections.${value}.${property}`)
	}

	translate(...args) {
		return this._page.translate(...args)
	}

	toJson() {
		const result = {}

		if (this._name instanceof Function) {
			result.name = this._name()
		} else if (typeof (this._name) === 'string' || this._name instanceof String) {
			result.name = this.translate(this._name)
		}

		if (this._hideable) {
			result.hideable = this._hideable
		}

		if (this._hidden) {
			result.hidden = this._hidden
		}

		if (this._style) {
			result.style = this._style
		}

		result.settings = []
		for (const setting of this._settings) {
			result.settings.push(setting.toJson())
		}

		return result
	}
}
