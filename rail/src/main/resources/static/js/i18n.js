// Language switcher implementation
const i18n = {
    translations: null,
    currentLocale: 'en',

    async init() {
        // Load translations
        try {
            const response = await fetch('/translations.json');
            this.translations = await response.json();

            // Set initial language from localStorage or browser preference
            const savedLocale = localStorage.getItem('preferred-locale');
            if (savedLocale && this.translations[savedLocale]) {
                this.currentLocale = savedLocale;
            } else {
                const browserLang = navigator.language.split('-')[0];
                this.currentLocale = this.translations[browserLang] ? browserLang : 'en';
            }

            this.updateUI();
            this.bindLanguageSelectors();
        } catch (error) {
            console.error('Failed to load translations:', error);
        }
    },

    setLanguage(locale) {
        if (this.translations[locale]) {
            this.currentLocale = locale;
            localStorage.setItem('preferred-locale', locale);
            this.updateUI();
        }
    },

    translate(key) {
        const keys = key.split('.');
        let value = this.translations[this.currentLocale];

        for (const k of keys) {
            value = value?.[k];
        }

        return value || key;
    },

    updateUI() {
        // Update all elements with data-i18n attribute
        document.querySelectorAll('[data-i18n]').forEach(element => {
            const key = element.getAttribute('data-i18n');
            element.textContent = this.translate(key);
        });

        // Update all elements with data-i18n-placeholder attribute
        document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
            const key = element.getAttribute('data-i18n-placeholder');
            element.placeholder = this.translate(key);
        });

        // Update HTML lang attribute
        document.documentElement.lang = this.currentLocale;

        // Update active state of language selectors
        document.querySelectorAll('.language-selector').forEach(btn => {
            btn.classList.toggle('active', btn.getAttribute('data-locale') === this.currentLocale);
        });
    },

    bindLanguageSelectors() {
        document.querySelectorAll('.language-selector').forEach(btn => {
            btn.addEventListener('click', () => {
                const locale = btn.getAttribute('data-locale');
                this.setLanguage(locale);
            });
        });
    }
};

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => i18n.init());