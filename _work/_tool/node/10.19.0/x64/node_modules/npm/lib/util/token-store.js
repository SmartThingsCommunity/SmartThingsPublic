'use strict'

class TokenStore {
	constructor(installedAppId, contextStore, clientId, clientSecret) {
		this.installedAppId = installedAppId
		this.contextStore = contextStore
		this.clientId = clientId
		this.clientSecret = clientSecret
	}

	async getRefreshData() {
		const data = await this.contextStore.get(this.installedAppId)
		return {
			refreshToken: data.refreshToken,
			clientId: this.clientId,
			clientSecret: this.clientSecret
		}
	}

	putAuthData(authData) {
		return this.contextStore.update(this.installedAppId, authData)
	}
}

module.exports = TokenStore
