class OAuthHandler {
    constructor(provider) {
        this.config = oauthConfig[provider];
    }

    getAuthUrl() {
        const params = new URLSearchParams({
            client_id: this.config.clientId,
            redirect_uri: this.config.redirectUri,
            response_type: 'code',
            scope: this.config.scope,
            state: this.generateState()
        });
        return `${this.config.authUrl}?${params.toString()}`;
    }

    async handleCallback(code) {
        const tokenResponse = await this.getAccessToken(code);
        const userInfo = await this.getUserInfo(tokenResponse.access_token);
        return userInfo;
    }

    async getAccessToken(code) {
        const params = new URLSearchParams({
            client_id: this.config.clientId,
            client_secret: this.config.clientSecret,
            code: code,
            redirect_uri: this.config.redirectUri,
            grant_type: 'authorization_code'
        });

        const response = await fetch(this.config.tokenUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
        });

        return response.json();
    }

    async getUserInfo(accessToken) {
        // Provider-specific user info endpoints
        const endpoints = {
            google: 'https://www.googleapis.com/oauth2/v2/userinfo',
            facebook: 'https://graph.facebook.com/me?fields=id,name,email',
            twitter: 'https://api.twitter.com/2/users/me'
        };

        const response = await fetch(endpoints[this.provider], {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        return response.json();
    }

    generateState() {
        return Math.random().toString(36).substring(2);
    }
}