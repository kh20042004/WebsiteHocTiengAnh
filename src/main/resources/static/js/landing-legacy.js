/**
 * Landing.js - Compatibility Bridge
 * 
 * File này đảm bảo backward compatibility với hệ thống cũ
 * và redirect sang hệ thống module mới.
 * 
 * ⚠️ DEPRECATED: File này sẽ được loại bỏ trong version tương lai
 * Vui lòng sử dụng hệ thống module mới trong landing-main.js
 */

console.warn('🔄 [Landing.js] DEPRECATED: Đang sử dụng landing.js cũ. Vui lòng chuyển sang hệ thống module mới!');

/**
 * Kiểm tra xem hệ thống module mới đã được load chưa
 */
function checkModuleSystem() {
    const requiredModules = [
        'APIModule',
        'UIModule', 
        'NavbarModule',
        'AnimationModule'
    ];
    
    const missingModules = requiredModules.filter(module => typeof window[module] === 'undefined');
    
    if (missingModules.length > 0) {
        console.error('❌ [Landing.js] Thiếu modules:', missingModules);
        return false;
    }
    
    if (typeof window.landingPageController === 'undefined') {
        console.error('❌ [Landing.js] Thiếu landingPageController');
        return false;
    }
    
    console.log('✅ [Landing.js] Hệ thống module mới đã sẵn sàng');
    return true;
}

/**
 * Legacy functions để backward compatibility
 * Redirect sang module tương ứng
 */

// Navbar functions -> NavbarModule
function initNavbarScrollEffect() {
    console.warn('🔄 [Landing.js] initNavbarScrollEffect is deprecated. Được xử lý bởi NavbarModule');
    if (window.NavbarModule) {
        return window.NavbarModule.handleScroll();
    }
}

function initMobileMenu() {
    console.warn('🔄 [Landing.js] initMobileMenu is deprecated. Được xử lý bởi NavbarModule');
    if (window.NavbarModule) {
        return window.NavbarModule.setupMobileMenu();
    }
}

function initSmoothScrolling() {
    console.warn('🔄 [Landing.js] initSmoothScrolling is deprecated. Được xử lý bởi NavbarModule');
    if (window.NavbarModule) {
        return window.NavbarModule.setupSmoothScrolling();
    }
}

// Animation functions -> AnimationModule  
function initCounterAnimations() {
    console.warn('🔄 [Landing.js] initCounterAnimations is deprecated. Được xử lý bởi AnimationModule');
    if (window.AnimationModule) {
        return window.AnimationModule.initCounters();
    }
}

function initScrollAnimations() {
    console.warn('🔄 [Landing.js] initScrollAnimations is deprecated. Được xử lý bởi AnimationModule');
    if (window.AnimationModule) {
        return window.AnimationModule.initScrollAnimations();
    }
}

// UI functions -> UIModule
function showToast(message, type) {
    console.warn('🔄 [Landing.js] showToast is deprecated. Sử dụng UIModule.showToast()');
    if (window.UIModule) {
        return window.UIModule.showToast(message, type);
    }
}

function validateForm(formElement) {
    console.warn('🔄 [Landing.js] validateForm is deprecated. Sử dụng UIModule.validateForm()');
    if (window.UIModule) {
        return window.UIModule.validateForm(formElement);
    }
}

// API functions -> APIModule
function makeAPIRequest(url, options) {
    console.warn('🔄 [Landing.js] makeAPIRequest is deprecated. Sử dụng APIModule.request()');
    if (window.APIModule) {
        return window.APIModule.request(options.method || 'GET', url, options);
    }
}

/**
 * Main initialization - Compatibility bridge
 */
function initLandingPage() {
    console.warn('🔄 [Landing.js] initLandingPage is deprecated. Sử dụng hệ thống module mới');
    
    // Kiểm tra module system
    if (!checkModuleSystem()) {
        console.error('❌ [Landing.js] Không thể khởi tạo - thiếu modules');
        return;
    }
    
    console.log('✅ [Landing.js] Compatibility bridge activated');
}

/**
 * Auto-initialization với warning
 */
document.addEventListener('DOMContentLoaded', () => {
    console.warn(`
🔄 DEPRECATED WARNING
===================
Bạn đang sử dụng landing.js cũ (deprecated).
Vui lòng chuyển sang hệ thống module mới:

1. Loại bỏ <script src="landing.js">
2. Sử dụng module system mới trong landing-main.js

Xem thêm: /js/README.md
===================
    `);
    
    initLandingPage();
});