/**
 * Teacher Header & Navigation Scripts
 * Handles dropdown menus, mobile navigation, and common actions
 */

// ==================== DROPDOWN TOGGLE ====================

/**
 * Toggle user dropdown menu visibility
 * @param {Event} event - Click event
 */
function toggleUserDropdown(event) {
    event.stopPropagation();
    const dropdown = document.getElementById('user-dropdown');
    if (dropdown) {
        dropdown.classList.toggle('hidden');
    }
}

/**
 * Close user dropdown menu
 */
function closeUserDropdown() {
    const dropdown = document.getElementById('user-dropdown');
    if (dropdown) {
        dropdown.classList.add('hidden');
    }
}

// ==================== MOBILE MENU ====================

/**
 * Toggle mobile menu visibility
 */
function toggleMobileMenu() {
    const menu = document.getElementById('mobile-menu');
    if (menu) {
        menu.classList.toggle('hidden');
    }
}

/**
 * Close mobile menu
 */
function closeMobileMenu() {
    const menu = document.getElementById('mobile-menu');
    if (menu) {
        menu.classList.add('hidden');
    }
}

// ==================== EVENT LISTENERS ====================

// Close dropdowns when clicking outside
document.addEventListener('click', function(event) {
    const dropdown = document.getElementById('user-dropdown');
    const userMenu = document.getElementById('userMenuContainer');
    
    if (userMenu && !userMenu.contains(event.target) && dropdown && !dropdown.classList.contains('hidden')) {
        dropdown.classList.add('hidden');
    }
});

// Close mobile menu when clicking a link
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('#mobile-menu a').forEach(link => {
        link.addEventListener('click', closeMobileMenu);
    });
});

// ==================== LOGOUT HANDLER ====================

/**
 * Handle user logout
 */
function handleLogout() {
    // Optional: Show confirmation dialog
    if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
        window.location.href = '/api/auth/logout';
    }
}

// ==================== ACTIVE MENU HIGHLIGHT ====================

/**
 * Set active menu item based on current URL
 */
function setActiveMenuItem() {
    const currentPath = window.location.pathname;
    document.querySelectorAll('#navbar nav a').forEach(link => {
        const href = link.getAttribute('href');
        if (currentPath.includes(href) && href !== '/') {
            link.classList.remove('hover:text-brand-600', 'transition-colors');
            link.classList.add('text-brand-600');
        }
    });
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', setActiveMenuItem);

// ==================== UTILITY FUNCTIONS ====================

/**
 * Navigate to a specific teacher page
 * @param {string} path - The path to navigate to
 */
function navigateTo(path) {
    window.location.href = `/dashboard/teacher/${path}`;
}

/**
 * Open user profile page
 */
function goToProfile() {
    window.location.href = '/profile';
}

/**
 * Open settings page
 */
function goToSettings() {
    window.location.href = '/settings';
}
