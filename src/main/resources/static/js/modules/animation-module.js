/**
 * Module quản lý hiệu ứng và animations
 * Xử lý các hiệu ứng trực quan trên trang landing
 * 
 * @author English 12 Smart Team
 * @version 1.0.0
 */

// ============================================
// ANIMATION MODULE - QUẢN LÝ HIỆU ỨNG
// ============================================

const AnimationModule = {
    /**
     * Khởi tạo toàn bộ hiệu ứng animation
     */
    init() {
        console.log('🎨 [Animation Module] Đang khởi tạo...');
        this.initCounterAnimation();
        this.initScrollFadeIn();
        this.initTypingEffect();
        this.initParallaxEffect();
        this.initHoverEffects();
        console.log('✅ [Animation Module] Khởi tạo thành công!');
    },

    /**
     * Animation cho các số đếm (counter)
     * Tăng dần từ 0 đến số mục tiêu khi element xuất hiện trong viewport
     */
    initCounterAnimation() {
        const counters = document.querySelectorAll('.counter');
        
        if (counters.length === 0) {
            console.log('ℹ️ [Animation Module] Không có counters để animate');
            return;
        }

        /**
         * Animate một counter từ 0 đến target value
         * @param {Element} counter - Element chứa số cần animate
         */
        const animateCounter = (counter) => {
            const target = parseFloat(counter.getAttribute('data-target'));
            const duration = parseInt(counter.getAttribute('data-duration')) || 2000; // 2s default
            const isDecimal = target % 1 !== 0;
            const increment = target / (duration / 16); // 60fps
            
            let current = 0;
            counter.textContent = isDecimal ? '0.0' : '0';
            
            const timer = setInterval(() => {
                current += increment;
                
                if (current >= target) {
                    // Hoàn thành animation
                    counter.textContent = isDecimal ? target.toFixed(1) : target;
                    clearInterval(timer);
                    
                    // Thêm class completed để có thể style khác
                    counter.classList.add('counter-completed');
                    
                    console.log(`📊 [Animation Module] Counter hoàn thành: ${target}`);
                } else {
                    // Cập nhật giá trị hiện tại
                    counter.textContent = isDecimal ? current.toFixed(1) : Math.floor(current);
                }
            }, 16);
        };

        // Sử dụng Intersection Observer để trigger animation
        const observerOptions = {
            threshold: 0.5,
            rootMargin: '0px 0px -10% 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && !entry.target.classList.contains('animated')) {
                    entry.target.classList.add('animated');
                    animateCounter(entry.target);
                    
                    // Unobserve sau khi animate để tránh lặp lại
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        counters.forEach(counter => observer.observe(counter));
        
        console.log(`✅ [Animation Module] Counter animation đã khởi tạo cho ${counters.length} elements`);
    },

    /**
     * Fade in animation khi scroll đến elements
     * Tạo hiệu ứng hiện dần các elements khi chúng vào viewport
     */
    initScrollFadeIn() {
        const fadeElements = document.querySelectorAll('.fade-in-up, .fade-in-left, .fade-in-right, .fade-in');
        
        if (fadeElements.length === 0) {
            console.log('ℹ️ [Animation Module] Không có fade elements để animate');
            return;
        }

        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -5% 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const element = entry.target;
                    const delay = element.getAttribute('data-delay') || 0;
                    
                    setTimeout(() => {
                        element.classList.add('animate-fade-in');
                        
                        // Trigger custom event sau khi animation hoàn thành
                        setTimeout(() => {
                            element.dispatchEvent(new CustomEvent('animationComplete'));
                        }, 600);
                        
                    }, parseInt(delay));
                    
                    observer.unobserve(element);
                    
                    console.log(`✨ [Animation Module] Fade in triggered cho element`);
                }
            });
        }, observerOptions);

        fadeElements.forEach(element => {
            observer.observe(element);
        });

        console.log(`✅ [Animation Module] Scroll fade in đã khởi tạo cho ${fadeElements.length} elements`);
    },

    /**
     * Hiệu ứng typing (đánh máy) cho text
     * Tạo hiệu ứng chữ xuất hiện từng chữ cái như đang được gõ
     */
    initTypingEffect() {
        const typingElements = document.querySelectorAll('.typing-effect');
        
        if (typingElements.length === 0) {
            console.log('ℹ️ [Animation Module] Không có typing elements');
            return;
        }

        /**
         * Tạo typing effect cho một element
         * @param {Element} element - Element cần tạo typing effect
         */
        const createTypingEffect = (element) => {
            const text = element.textContent;
            const speed = parseInt(element.getAttribute('data-typing-speed')) || 100;
            const cursor = element.getAttribute('data-show-cursor') !== 'false';
            
            element.textContent = '';
            
            if (cursor) {
                element.classList.add('typing-cursor');
            }

            let i = 0;
            const timer = setInterval(() => {
                element.textContent = text.slice(0, i + 1);
                i++;
                
                if (i === text.length) {
                    clearInterval(timer);
                    
                    // Xóa cursor sau khi hoàn thành (tuỳ chọn)
                    const removeCursor = element.getAttribute('data-remove-cursor');
                    if (removeCursor === 'true' && cursor) {
                        setTimeout(() => {
                            element.classList.remove('typing-cursor');
                        }, 2000);
                    }
                    
                    console.log(`⌨️ [Animation Module] Typing effect hoàn thành`);
                }
            }, speed);
        };

        // Trigger typing effect khi element vào viewport
        const observerOptions = {
            threshold: 0.3
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && !entry.target.classList.contains('typing-started')) {
                    entry.target.classList.add('typing-started');
                    
                    const delay = parseInt(entry.target.getAttribute('data-delay')) || 0;
                    setTimeout(() => {
                        createTypingEffect(entry.target);
                    }, delay);
                    
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        typingElements.forEach(element => observer.observe(element));
        
        console.log(`✅ [Animation Module] Typing effect đã khởi tạo cho ${typingElements.length} elements`);
    },

    /**
     * Hiệu ứng parallax cho background elements
     * Tạo hiệu ứng chuyển động khác nhau khi scroll
     */
    initParallaxEffect() {
        const parallaxElements = document.querySelectorAll('.parallax');
        
        if (parallaxElements.length === 0) {
            console.log('ℹ️ [Animation Module] Không có parallax elements');
            return;
        }

        let isScrolling = false;

        /**
         * Cập nhật position của parallax elements
         */
        const updateParallax = () => {
            const scrollY = window.pageYOffset;
            
            parallaxElements.forEach(element => {
                const speed = parseFloat(element.getAttribute('data-parallax-speed')) || 0.5;
                const yOffset = scrollY * speed;
                
                element.style.transform = `translate3d(0, ${yOffset}px, 0)`;
            });
            
            isScrolling = false;
        };

        // Throttle scroll event cho hiệu năng
        window.addEventListener('scroll', () => {
            if (!isScrolling) {
                requestAnimationFrame(updateParallax);
                isScrolling = true;
            }
        }, { passive: true });

        console.log(`✅ [Animation Module] Parallax effect đã khởi tạo cho ${parallaxElements.length} elements`);
    },

    /**
     * Hiệu ứng hover cho các interactive elements
     * Thêm các hiệu ứng khi hover vào buttons, cards, etc.
     */
    initHoverEffects() {
        // Hover effect cho buttons
        const buttons = document.querySelectorAll('.btn-hover-effect');
        buttons.forEach(button => {
            button.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-2px) scale(1.02)';
                this.style.boxShadow = '0 10px 25px rgba(0,0,0,0.15)';
            });
            
            button.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';
                this.style.boxShadow = '';
            });
        });

        // Hover effect cho cards
        const cards = document.querySelectorAll('.card-hover-effect');
        cards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-5px) rotateX(2deg)';
                this.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) rotateX(0)';
            });
        });

        // Ripple effect cho buttons
        const rippleButtons = document.querySelectorAll('.ripple-effect');
        rippleButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                const ripple = document.createElement('span');
                const rect = this.getBoundingClientRect();
                const size = Math.max(rect.width, rect.height);
                const x = e.clientX - rect.left - size / 2;
                const y = e.clientY - rect.top - size / 2;
                
                ripple.style.cssText = `
                    width: ${size}px;
                    height: ${size}px;
                    left: ${x}px;
                    top: ${y}px;
                `;
                ripple.classList.add('ripple');
                
                this.appendChild(ripple);
                
                // Xóa ripple sau khi animation hoàn thành
                setTimeout(() => {
                    ripple.remove();
                }, 600);
            });
        });

        console.log('✅ [Animation Module] Hover effects đã được khởi tạo');
    },

    /**
     * Utility function để tạo custom animations
     * @param {Element} element - Element cần animate
     * @param {Object} options - Tuỳ chọn animation
     */
    createCustomAnimation(element, options = {}) {
        const defaults = {
            duration: 600,
            easing: 'cubic-bezier(0.4, 0, 0.2, 1)',
            delay: 0,
            fillMode: 'forwards'
        };

        const config = { ...defaults, ...options };

        return new Promise((resolve) => {
            setTimeout(() => {
                element.style.transition = `all ${config.duration}ms ${config.easing}`;
                
                // Apply styles
                Object.keys(config.styles || {}).forEach(prop => {
                    element.style[prop] = config.styles[prop];
                });

                setTimeout(() => {
                    resolve(element);
                }, config.duration);
                
            }, config.delay);
        });
    }
};

// Export module
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AnimationModule;
}