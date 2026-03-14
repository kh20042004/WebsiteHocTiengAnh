/**
 * File Main Landing Page JavaScript
 * Tập hợp và khởi tạo tất cả các modules cho trang landing
 * 
 * @author English 12 Smart Team
 * @version 1.0.0
 * @description File main quản lý toàn bộ JavaScript cho trang landing page
 */

// ============================================
// LANDING PAGE MAIN CONTROLLER
// ============================================

/**
 * Controller chính cho Landing Page
 * Quản lý việc khởi tạo và phối hợp các modules
 */
class LandingPageController {
    constructor() {
        console.log('🚀 [Landing Page] Khởi tạo Landing Page Controller...');
        
        // Trạng thái ứng dụng
        this.state = {
            isInitialized: false,
            activeSection: 'hero',
            isMobileMenuOpen: false,
            currentTheme: 'light'
        };

        // Configuration
        this.config = {
            enableDebugMode: false, // Set true để bật debug logs
            enableAnalytics: true,  // Tracking user interactions
            autoInitDelay: 100,     // Delay trước khi auto-init
            enableLazyLoading: true // Lazy load các components không quan trọng
        };

        this.init();
    }

    /**
     * Khởi tạo toàn bộ ứng dụng
     * Gọi các modules theo thứ tự ưu tiên
     */
    async init() {
        try {
            console.log('📋 [Landing Page] Bắt đầu quá trình khởi tạo...');

            // 1. Kiểm tra DOM ready
            await this.waitForDOM();

            // 2. Khởi tạo các modules cơ bản (đồng bộ)
            await this.initCoreModules();

            // 3. Khởi tạo các modules nâng cao (bất đồng bộ)
            await this.initAdvancedModules();

            // 4. Setup event listeners global
            this.setupGlobalEventListeners();

            // 5. Khởi tạo analytics và tracking
            if (this.config.enableAnalytics) {
                this.initAnalytics();
            }

            // 6. Finalize và cleanup
            this.finalize();

            this.state.isInitialized = true;
            console.log('✅ [Landing Page] Khởi tạo hoàn tất thành công!');

        } catch (error) {
            console.error('❌ [Landing Page] Lỗi khởi tạo:', error);
            this.handleInitializationError(error);
        }
    }

    /**
     * Chờ DOM ready
     * @returns {Promise}
     */
    waitForDOM() {
        return new Promise((resolve) => {
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', resolve);
            } else {
                resolve();
            }
        });
    }

    /**
     * Khởi tạo các modules cần thiết đầu tiên
     * Những modules này cần hoạt động ngay lập tức
     */
    async initCoreModules() {
        console.log('🔧 [Landing Page] Khởi tạo core modules...');

        // 1. API Module - Cần thiết cho mọi tương tác với server
        if (typeof APIModule !== 'undefined') {
            APIModule.init();
            this.api = APIModule;
        } else {
            console.warn('⚠️ [Landing Page] APIModule không được load');
        }

        // 2. UI Module - Cần cho form validation và user interactions
        if (typeof UIModule !== 'undefined') {
            UIModule.init();
            this.ui = UIModule;
        } else {
            console.warn('⚠️ [Landing Page] UIModule không được load');
        }

        // 3. Navbar Module - Cần cho navigation ngay lập tức
        if (typeof NavbarModule !== 'undefined') {
            NavbarModule.init();
            this.navbar = NavbarModule;
        } else {
            console.warn('⚠️ [Landing Page] NavbarModule không được load');
        }

        console.log('✅ [Landing Page] Core modules đã khởi tạo');
    }

    /**
     * Khởi tạo các modules nâng cao
     * Có thể load bất đồng bộ để không ảnh hưởng performance
     */
    async initAdvancedModules() {
        console.log('🎨 [Landing Page] Khởi tạo advanced modules...');

        // Delay nhỏ để đảm bảo core modules đã sẵn sàng
        await this.delay(this.config.autoInitDelay);

        // Animation Module - Có thể load sau để tối ưu performance
        if (typeof AnimationModule !== 'undefined') {
            if (this.config.enableLazyLoading) {
                // Lazy load animations chỉ khi cần thiết
                this.setupLazyAnimations();
            } else {
                AnimationModule.init();
                this.animation = AnimationModule;
            }
        } else {
            console.warn('⚠️ [Landing Page] AnimationModule không được load');
        }

        // Button Handlers Module - Xử lý tất cả button events
        if (typeof ButtonHandlers !== 'undefined') {
            console.log('✅ [Landing Page] ButtonHandlers module loaded');
            this.buttonHandlers = ButtonHandlers;
        } else {
            console.warn('⚠️ [Landing Page] ButtonHandlers module không được load');
        }

        console.log('✅ [Landing Page] Advanced modules đã khởi tạo');
    }

    /**
     * Setup lazy loading cho animations
     * Chỉ chạy animations khi user scroll đến section đó
     */
    setupLazyAnimations() {
        const animatedSections = document.querySelectorAll('[data-animate]');
        
        if (animatedSections.length === 0) {
            // Nếu không có sections cần animate, init ngay
            if (typeof AnimationModule !== 'undefined') {
                AnimationModule.init();
                this.animation = AnimationModule;
            }
            return;
        }

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && !this.animation) {
                    // Init animation module khi cần thiết
                    if (typeof AnimationModule !== 'undefined') {
                        AnimationModule.init();
                        this.animation = AnimationModule;
                        console.log('🎨 [Landing Page] Lazy loaded AnimationModule');
                    }
                    observer.disconnect(); // Chỉ init 1 lần
                }
            });
        }, { threshold: 0.1 });

        animatedSections.forEach(section => {
            observer.observe(section);
        });
    }

    /**
     * Setup các event listeners global
     * Xử lý các events cần thiết cho toàn bộ trang
     */
    setupGlobalEventListeners() {
        console.log('👂 [Landing Page] Thiết lập global event listeners...');

        // Theo dõi thay đổi kích thước màn hình
        let resizeTimeout;
        window.addEventListener('resize', () => {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(() => {
                this.handleWindowResize();
            }, 250);
        });

        // Theo dõi scroll để cập nhật active section
        let scrollTimeout;
        window.addEventListener('scroll', () => {
            clearTimeout(scrollTimeout);
            scrollTimeout = setTimeout(() => {
                this.updateActiveSection();
            }, 100);
        }, { passive: true });

        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            this.handleKeyboardShortcuts(e);
        });

        // Page visibility changes (user switch tabs)
        document.addEventListener('visibilitychange', () => {
            this.handleVisibilityChange();
        });

        // Online/offline status
        window.addEventListener('online', () => this.handleConnectionChange(true));
        window.addEventListener('offline', () => this.handleConnectionChange(false));

        // Custom events từ các modules
        this.setupCustomEventListeners();

        console.log('✅ [Landing Page] Global event listeners đã được thiết lập');
    }

    /**
     * Setup custom event listeners từ modules khác
     */
    setupCustomEventListeners() {
        // Authentication events
        window.addEventListener('userLoggedIn', (e) => {
            console.log('👤 [Landing Page] User đã đăng nhập:', e.detail);
            this.handleUserLogin(e.detail);
        });

        window.addEventListener('userLoggedOut', () => {
            console.log('👤 [Landing Page] User đã đăng xuất');
            this.handleUserLogout();
        });

        // Animation completion events
        window.addEventListener('animationComplete', (e) => {
            if (this.config.enableDebugMode) {
                console.log('✨ [Landing Page] Animation hoàn thành:', e.target);
            }
        });
    }

    /**
     * Xử lý thay đổi kích thước cửa sổ
     */
    handleWindowResize() {
        const width = window.innerWidth;
        
        if (this.config.enableDebugMode) {
            console.log(`📏 [Landing Page] Window resize: ${width}px`);
        }

        // Đóng mobile menu khi resize về desktop
        if (width >= 768 && this.state.isMobileMenuOpen) {
            if (this.navbar) {
                // Trigger close mobile menu
                const mobileMenu = document.getElementById('mobile-menu');
                if (mobileMenu && !mobileMenu.classList.contains('hidden')) {
                    mobileMenu.classList.add('hidden');
                    this.state.isMobileMenuOpen = false;
                }
            }
        }
    }

    /**
     * Cập nhật section đang active dựa trên scroll position
     */
    updateActiveSection() {
        const sections = document.querySelectorAll('section[id]');
        const scrollPosition = window.scrollY + 100;

        let newActiveSection = 'hero'; // Default

        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            const sectionHeight = section.offsetHeight;
            
            if (scrollPosition >= sectionTop && scrollPosition < sectionTop + sectionHeight) {
                newActiveSection = section.id;
            }
        });

        // Chỉ update state nếu có thay đổi
        if (this.state.activeSection !== newActiveSection) {
            this.state.activeSection = newActiveSection;
            
            // Trigger custom event
            window.dispatchEvent(new CustomEvent('activeSectionChanged', {
                detail: { section: newActiveSection }
            }));

            if (this.config.enableDebugMode) {
                console.log(`📍 [Landing Page] Active section: ${newActiveSection}`);
            }
        }
    }

    /**
     * Xử lý keyboard shortcuts
     * @param {KeyboardEvent} e - Keyboard event
     */
    handleKeyboardShortcuts(e) {
        // ESC key - đóng mọi modal/popup
        if (e.key === 'Escape') {
            this.handleEscapeKey();
        }

        // Ctrl/Cmd + K - Focus search (nếu có)
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            this.focusSearch();
        }

        // Debug mode toggle (Ctrl + Shift + D)
        if (e.ctrlKey && e.shiftKey && e.key === 'D') {
            e.preventDefault();
            this.toggleDebugMode();
        }
    }

    /**
     * Xử lý ESC key
     */
    handleEscapeKey() {
        // Đóng mobile menu
        if (this.state.isMobileMenuOpen && this.navbar) {
            const mobileMenu = document.getElementById('mobile-menu');
            if (mobileMenu && !mobileMenu.classList.contains('hidden')) {
                mobileMenu.classList.add('hidden');
                this.state.isMobileMenuOpen = false;
            }
        }

        // Đóng modals (sử dụng UI Module)
        if (this.ui) {
            const openModal = document.querySelector('.modal.modal-open');
            if (openModal) {
                this.ui.closeModal(openModal.id);
            }
        }
    }

    /**
     * Focus vào ô search (nếu có)
     */
    focusSearch() {
        const searchInput = document.querySelector('input[type="search"], .search-input');
        if (searchInput) {
            searchInput.focus();
            console.log('🔍 [Landing Page] Focused search input');
        }
    }

    /**
     * Toggle debug mode
     */
    toggleDebugMode() {
        this.config.enableDebugMode = !this.config.enableDebugMode;
        console.log(`🐛 [Landing Page] Debug mode: ${this.config.enableDebugMode ? 'ON' : 'OFF'}`);
        
        if (this.ui) {
            this.ui.showToast(
                `Debug mode ${this.config.enableDebugMode ? 'bật' : 'tắt'}`, 
                'info'
            );
        }
    }

    /**
     * Xử lý thay đổi visibility của page
     */
    handleVisibilityChange() {
        if (document.hidden) {
            console.log('👁️ [Landing Page] Page hidden');
            // Tạm dừng các animations không cần thiết
            this.pauseNonEssentialOperations();
        } else {
            console.log('👁️ [Landing Page] Page visible');
            // Resume operations
            this.resumeOperations();
        }
    }

    /**
     * Xử lý thay đổi kết nối mạng
     * @param {boolean} isOnline - Trạng thái kết nối
     */
    handleConnectionChange(isOnline) {
        console.log(`🌐 [Landing Page] Connection: ${isOnline ? 'Online' : 'Offline'}`);
        
        if (this.ui) {
            if (isOnline) {
                this.ui.showToast('Kết nối mạng đã được khôi phục', 'success');
                // Reload dữ liệu nếu cần
            } else {
                this.ui.showToast('⚠️ Mất kết nối mạng - Một số tính năng có thể không khả dụng', 'warning');
            }
        }
    }

    /**
     * Xử lý khi user đăng nhập
     * @param {Object} userData - Thông tin user
     */
    handleUserLogin(userData) {
        // Cập nhật UI để hiển thị user đã đăng nhập
        this.updateUserInterface(userData);
        
        if (this.ui) {
            this.ui.showToast(`Chào mừng ${userData.fullName || userData.username}!`, 'success');
        }
    }

    /**
     * Xử lý khi user đăng xuất
     */
    handleUserLogout() {
        // Reset UI về trạng thái chưa đăng nhập
        this.resetUserInterface();
        
        if (this.ui) {
            this.ui.showToast('Đã đăng xuất thành công', 'info');
        }
    }

    /**
     * Cập nhật giao diện khi user đăng nhập
     * @param {Object} userData - Thông tin user
     */
    updateUserInterface(userData) {
        // Ẩn auth buttons, hiện user menu
        const authButtons = document.querySelectorAll('.auth-button');
        const userMenu = document.querySelector('.user-menu');
        
        authButtons.forEach(btn => btn.style.display = 'none');
        if (userMenu) {
            userMenu.style.display = 'block';
        }

        // Cập nhật tên user nếu có
        const userNameElements = document.querySelectorAll('.user-name');
        userNameElements.forEach(el => {
            el.textContent = userData.fullName || userData.username;
        });
    }

    /**
     * Reset giao diện về trạng thái chưa đăng nhập
     */
    resetUserInterface() {
        // Hiện auth buttons, ẩn user menu  
        const authButtons = document.querySelectorAll('.auth-button');
        const userMenu = document.querySelector('.user-menu');
        
        authButtons.forEach(btn => btn.style.display = '');
        if (userMenu) {
            userMenu.style.display = 'none';
        }
    }

    /**
     * Tạm dừng các operations không cần thiết
     */
    pauseNonEssentialOperations() {
        // Tạm dừng animations
        if (this.animation) {
            document.body.classList.add('pause-animations');
        }
    }

    /**
     * Khôi phục operations
     */
    resumeOperations() {
        // Khôi phục animations
        document.body.classList.remove('pause-animations');
    }

    /**
     * Khởi tạo analytics và tracking
     */
    initAnalytics() {
        console.log('📊 [Landing Page] Khởi tạo analytics...');
        
        // Tracking page view
        this.trackEvent('page_view', {
            page: 'landing',
            timestamp: new Date().toISOString()
        });

        // Setup scroll depth tracking
        this.initScrollDepthTracking();

        console.log('✅ [Landing Page] Analytics đã được khởi tạo');
    }

    /**
     * Tracking scroll depth
     */
    initScrollDepthTracking() {
        const thresholds = [25, 50, 75, 90, 100];
        const triggered = new Set();

        window.addEventListener('scroll', () => {
            const scrollPercent = Math.round(
                (window.scrollY / (document.body.scrollHeight - window.innerHeight)) * 100
            );

            thresholds.forEach(threshold => {
                if (scrollPercent >= threshold && !triggered.has(threshold)) {
                    triggered.add(threshold);
                    this.trackEvent('scroll_depth', {
                        depth: threshold,
                        page: 'landing'
                    });
                }
            });
        }, { passive: true });
    }

    /**
     * Track events
     * @param {string} eventName - Tên event
     * @param {Object} data - Dữ liệu event
     */
    trackEvent(eventName, data = {}) {
        if (!this.config.enableAnalytics) return;

        const eventData = {
            event: eventName,
            timestamp: new Date().toISOString(),
            url: window.location.href,
            userAgent: navigator.userAgent,
            ...data
        };

        // Log for debug
        if (this.config.enableDebugMode) {
            console.log('📊 [Landing Page] Track event:', eventData);
        }

        // Gửi đến analytics service (có thể là Google Analytics, etc.)
        // gtag('event', eventName, data);
    }

    /**
     * Finalize quá trình khởi tạo
     */
    finalize() {
        // Trigger custom event báo app đã ready
        window.dispatchEvent(new CustomEvent('landingPageReady'));

        // Cleanup any initialization artifacts
        const loader = document.getElementById('initial-loader');
        if (loader) {
            loader.style.opacity = '0';
            setTimeout(() => {
                loader.remove();
            }, 500);
        }

        // Track initialization complete
        this.trackEvent('app_initialized', {
            loadTime: Date.now() - window.performance.timeOrigin,
            modules: this.getLoadedModules()
        });
    }

    /**
     * Lấy danh sách modules đã load
     * @returns {Array}
     */
    getLoadedModules() {
        const modules = [];
        
        if (this.api) modules.push('APIModule');
        if (this.ui) modules.push('UIModule');
        if (this.navbar) modules.push('NavbarModule');
        if (this.animation) modules.push('AnimationModule');
        if (this.buttonHandlers) modules.push('ButtonHandlers');
        
        return modules;
    }

    /**
     * Xử lý lỗi khởi tạo
     * @param {Error} error - Error object
     */
    handleInitializationError(error) {
        console.error('💥 [Landing Page] Critical initialization error:', error);
        
        // Hiển thị thông báo lỗi cho user
        if (typeof alert !== 'undefined') {
            alert('Có lỗi xảy ra khi khởi tạo ứng dụng. Vui lòng tải lại trang.');
        }

        // Track error
        this.trackEvent('initialization_error', {
            error: error.message,
            stack: error.stack
        });
    }

    /**
     * Utility delay function
     * @param {number} ms - Milliseconds
     * @returns {Promise}
     */
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * Destroy controller và cleanup
     */
    destroy() {
        console.log('🧹 [Landing Page] Destroying controller...');
        
        // Remove event listeners
        // (Browsers tự cleanup khi page unload, nhưng good practice)
        
        this.state.isInitialized = false;
        console.log('✅ [Landing Page] Controller destroyed');
    }
}

// ============================================
// KHỞI TẠO ỨNG DỤNG
// ============================================

/**
 * Auto-initialize khi DOM ready
 */
(function() {
    'use strict';
    
    console.log('🌟 [Landing Page] Chuẩn bị khởi tạo English 12 Smart Landing Page...');
    
    // Tạo instance controller global
    window.landingPageController = new LandingPageController();
    
    // Cleanup khi page unload
    window.addEventListener('beforeunload', () => {
        if (window.landingPageController) {
            window.landingPageController.destroy();
        }
    });
    
    console.log('🎉 [Landing Page] Landing Page đã sẵn sàng!');
})();