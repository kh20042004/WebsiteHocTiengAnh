/**
 * Landing Page JavaScript
 * English 12 Smart - Interactive Features
 */

// ============================================
// 1. NAVBAR SCROLL EFFECT
// ============================================

/**
 * Add glass effect and shadow to navbar on scroll
 */
function initNavbarScrollEffect() {
    const navbar = document.getElementById('navbar');

    window.addEventListener('scroll', () => {
        if (window.scrollY > 10) {
            navbar.classList.add('shadow-sm', 'glass-nav');
            navbar.classList.remove('bg-white');
        } else {
            navbar.classList.remove('shadow-sm', 'glass-nav');
            navbar.classList.add('bg-white');
        }
    });
}

// ============================================
// 2. MOBILE MENU TOGGLE
// ============================================

/**
 * Toggle mobile menu visibility
 */
function initMobileMenu() {
    const mobileMenuToggle = document.getElementById('mobile-menu-toggle');
    const mobileMenu = document.getElementById('mobile-menu');

    if (mobileMenuToggle && mobileMenu) {
        mobileMenuToggle.addEventListener('click', () => {
            mobileMenu.classList.toggle('hidden');
        });

        // Close mobile menu when clicking on a link
        const mobileMenuLinks = mobileMenu.querySelectorAll('a');
        mobileMenuLinks.forEach(link => {
            link.addEventListener('click', () => {
                mobileMenu.classList.add('hidden');
            });
        });
    }
}

// ============================================
// 3. COUNTER ANIMATION
// ============================================

/**
 * Animate counters when they come into viewport
 */
function animateCounters() {
    const counters = document.querySelectorAll('.counter');
    const speed = 200; // Animation speed

    counters.forEach(counter => {
        const updateCount = () => {
            const target = parseFloat(counter.getAttribute('data-target'));
            const count = parseFloat(counter.innerText);
            const increment = target / speed;

            if (count < target) {
                // Handle decimal numbers (like 4.8)
                if (target % 1 !== 0) {
                    counter.innerText = (count + increment).toFixed(1);
                } else {
                    counter.innerText = Math.ceil(count + increment);
                }
                setTimeout(updateCount, 20);
            } else {
                // Ensure final value matches target exactly
                if (target % 1 !== 0) {
                    counter.innerText = target.toFixed(1);
                } else {
                    counter.innerText = target;
                }
            }
        };

        updateCount();
    });
}

/**
 * Trigger counter animation when section is in viewport
 */
function initCounterAnimation() {
    let hasAnimated = false;

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting && !hasAnimated) {
                animateCounters();
                hasAnimated = true;
            }
        });
    }, {
        threshold: 0.5 // Trigger when 50% of section is visible
    });

    const statsSection = document.querySelector('.counter')?.closest('section');
    if (statsSection) {
        observer.observe(statsSection);
    }
}

// ============================================
// 4. SMOOTH SCROLL
// ============================================

/**
 * Smooth scroll to anchor links
 */
function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');

            // Skip if href is just "#"
            if (href === '#') {
                e.preventDefault();
                return;
            }

            const target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                const offsetTop = target.offsetTop - 80; // Account for fixed navbar

                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });
}

// ============================================
// 5. FADE IN ANIMATIONS
// ============================================

/**
 * Add fade-in animation to elements when they enter viewport
 */
function initFadeInAnimations() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in-up');
            }
        });
    }, {
        threshold: 0.1
    });

    // Observe all feature cards, testimonials, etc.
    const animatedElements = document.querySelectorAll('.feature-card, .testimonial-card, .step-card');
    animatedElements.forEach(el => observer.observe(el));
}

// ============================================
// 6. VIDEO DEMO MODAL (Future Implementation)
// ============================================

/**
 * Handle demo video button click
 */
function initDemoVideo() {
    const demoBtn = document.getElementById('demo-video-btn');

    if (demoBtn) {
        demoBtn.addEventListener('click', () => {
            // TODO: Implement video modal
            alert('Video demo sẽ được thêm vào trong phiên bản tiếp theo!');
        });
    }
}

// ============================================
// 7. FORM VALIDATION (Future)
// ============================================

/**
 * Validate registration/login forms
 */
function initFormValidation() {
    // TODO: Add form validation logic when auth pages are created
}

// ============================================
// 8. LAZY LOADING IMAGES
// ============================================

/**
 * Lazy load images for better performance
 */
function initLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');

    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.removeAttribute('data-src');
                observer.unobserve(img);
            }
        });
    });

    images.forEach(img => imageObserver.observe(img));
}

// ============================================
// 9. SCROLL TO TOP BUTTON
// ============================================

/**
 * Show/hide scroll to top button
 */
function initScrollToTop() {
    // Create scroll to top button
    const scrollBtn = document.createElement('button');
    scrollBtn.innerHTML = '<iconify-icon icon="solar:arrow-up-linear" width="24"></iconify-icon>';
    scrollBtn.className = 'fixed bottom-8 right-8 bg-brand-500 text-white p-3 rounded-full shadow-lg hover:bg-brand-600 transition-all opacity-0 pointer-events-none z-50';
    scrollBtn.id = 'scroll-to-top';
    document.body.appendChild(scrollBtn);

    // Show/hide button based on scroll position
    window.addEventListener('scroll', () => {
        if (window.scrollY > 500) {
            scrollBtn.classList.remove('opacity-0', 'pointer-events-none');
        } else {
            scrollBtn.classList.add('opacity-0', 'pointer-events-none');
        }
    });

    // Scroll to top on click
    scrollBtn.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

// ============================================
// 10. ANALYTICS TRACKING (Future)
// ============================================

/**
 * Track user interactions for analytics
 */
function trackEvent(eventName, eventData) {
    // TODO: Implement Google Analytics or similar
    console.log('Event tracked:', eventName, eventData);
}

/**
 * Initialize analytics tracking
 */
function initAnalytics() {
    // Track CTA button clicks
    document.querySelectorAll('a[href*="register"]').forEach(btn => {
        btn.addEventListener('click', () => {
            trackEvent('cta_click', { location: 'register_button' });
        });
    });

    // Track feature card interactions
    document.querySelectorAll('.feature-card').forEach((card, index) => {
        card.addEventListener('click', () => {
            trackEvent('feature_click', { feature_index: index });
        });
    });
}

// ============================================
// 11. PERFORMANCE MONITORING
// ============================================

/**
 * Log page load performance
 */
function logPerformance() {
    window.addEventListener('load', () => {
        const perfData = window.performance.timing;
        const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart;
        console.log(`Page loaded in ${pageLoadTime}ms`);
    });
}

// ============================================
// 12. INITIALIZATION
// ============================================

/**
 * Initialize all features when DOM is ready
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log('English 12 Smart - Landing Page Initialized');

    // Initialize all features
    initNavbarScrollEffect();
    initMobileMenu();
    initCounterAnimation();
    initSmoothScroll();
    initFadeInAnimations();
    initDemoVideo();
    initLazyLoading();
    initScrollToTop();
    initAnalytics();
    logPerformance();
});

// ============================================
// 13. UTILITY FUNCTIONS
// ============================================

/**
 * Debounce function for performance optimization
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Throttle function for scroll events
 */
function throttle(func, limit) {
    let inThrottle;
    return function (...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// ============================================
// 14. EXPORT FOR TESTING (Optional)
// ============================================

// Expose functions for testing if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        initNavbarScrollEffect,
        initMobileMenu,
        animateCounters,
        initSmoothScroll,
        debounce,
        throttle,
    };
}
