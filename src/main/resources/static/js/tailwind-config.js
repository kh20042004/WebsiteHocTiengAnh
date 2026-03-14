/**
 * Tailwind CSS Configuration
 * English 12 Smart
 */

tailwind.config = {
    theme: {
        extend: {
            // Custom Font Family
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },

            // Custom Colors
            colors: {
                // Custom Slate Color
                slate: {
                    850: '#151f2e', // Custom dark for footer
                },

                // Brand Colors (Blue Theme)
                brand: {
                    50: '#eff6ff',
                    100: '#dbeafe',
                    200: '#bfdbfe',
                    300: '#93c5fd',
                    400: '#60a5fa',
                    500: '#3b82f6', // Primary Blue
                    600: '#2563eb',
                    700: '#1d4ed8',
                    800: '#1e40af',
                    900: '#1e3a8a',
                }
            },

            // Custom Background Images
            backgroundImage: {
                'hero-gradient': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                'gradient-blue': 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)',
                'gradient-purple': 'linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%)',
                'gradient-pink': 'linear-gradient(135deg, #ec4899 0%, #be185d 100%)',
            },

            // Custom Box Shadows
            boxShadow: {
                'brand': '0 10px 40px -10px rgba(59, 130, 246, 0.3)',
                'brand-lg': '0 20px 60px -15px rgba(59, 130, 246, 0.4)',
            },

            // Custom Border Radius
            borderRadius: {
                '4xl': '2rem',
            },

            // Custom Spacing
            spacing: {
                '18': '4.5rem',
                '88': '22rem',
                '100': '25rem',
                '112': '28rem',
                '128': '32rem',
            },

            // Custom Z-Index
            zIndex: {
                '60': '60',
                '70': '70',
                '80': '80',
                '90': '90',
                '100': '100',
            },

            // Custom Animation
            animation: {
                'fade-in': 'fadeIn 0.5s ease-in-out',
                'fade-in-up': 'fadeInUp 0.8s ease-out',
                'slide-in': 'slideIn 0.3s ease-out',
                'bounce-slow': 'bounce 3s infinite',
                'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
            },

            // Custom Keyframes
            keyframes: {
                fadeIn: {
                    '0%': { opacity: '0' },
                    '100%': { opacity: '1' },
                },
                fadeInUp: {
                    '0%': {
                        opacity: '0',
                        transform: 'translateY(20px)'
                    },
                    '100%': {
                        opacity: '1',
                        transform: 'translateY(0)'
                    },
                },
                slideIn: {
                    '0%': { transform: 'translateX(-100%)' },
                    '100%': { transform: 'translateX(0)' },
                },
            },

            // Custom Typography
            fontSize: {
                'xxs': '0.625rem',
            },

            // Custom Line Height
            lineHeight: {
                'extra-loose': '2.5',
            },

            // Custom Max Width
            maxWidth: {
                '8xl': '88rem',
                '9xl': '96rem',
            },
        }
    },

    // Plugins (if needed in future)
    plugins: [],
}
