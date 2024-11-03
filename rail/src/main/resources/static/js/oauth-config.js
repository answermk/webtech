const oauthConfig = {
    google: {
        clientId: 'YOUR_GOOGLE_CLIENT_ID',
        clientSecret: 'YOUR_GOOGLE_CLIENT_SECRET',
        redirectUri: 'http://localhost:8080/oauth/google/callback',
        authUrl: 'https://accounts.google.com/o/oauth2/v2/auth',
        tokenUrl: 'https://oauth2.googleapis.com/token',
        scope: 'email profile'
    },
    facebook: {
        clientId: 'YOUR_FACEBOOK_CLIENT_ID',
        clientSecret: 'YOUR_FACEBOOK_CLIENT_SECRET',
        redirectUri: 'http://localhost:8080/oauth/facebook/callback',
        authUrl: 'https://www.facebook.com/v12.0/dialog/oauth',
        tokenUrl: 'https://graph.facebook.com/v12.0/oauth/access_token',
        scope: 'email public_profile'
    },
    twitter: {
        clientId: 'YOUR_TWITTER_CLIENT_ID',
        clientSecret: 'YOUR_TWITTER_CLIENT_SECRET',
        redirectUri: 'http://localhost:8080/oauth/twitter/callback',
        authUrl: 'https://twitter.com/i/oauth2/authorize',
        tokenUrl: 'https://api.twitter.com/2/oauth2/token',
        scope: 'tweet.read users.read'
    }
};