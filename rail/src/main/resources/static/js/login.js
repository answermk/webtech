document.addEventListener('DOMContentLoaded', () => {
    const socialButtons = document.querySelectorAll('.social-login a');

    socialButtons.forEach(button => {
        button.addEventListener('click', async (e) => {
            e.preventDefault();
            const provider = button.getAttribute('href').split('/').pop();
            const oauthHandler = new OAuthHandler(provider);
            window.location.href = oauthHandler.getAuthUrl();
        });
    });
});