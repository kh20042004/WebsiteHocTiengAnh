/**
 * Teacher Dropdown Menu Manager
 * Handles all dropdown menu interactions for teacher dashboard
 * 
 * Features:
 * - Toggle dropdown with proper event handling
 * - Auto-close on outside click
 * - Auto-close on link click
 * - Active link highlighting
 * - Smooth transitions
 */

// ==================== DROPDOWN STATE ====================

const DropdownManager = {
    isOpen: false,
    dropdownEl: null,
    buttonEl: null,
    containerEl: null,
    
    /**
     * Initialize dropdown manager
     */
    init() {
        this.dropdownEl = document.getElementById('user-dropdown');
        this.buttonEl = document.getElementById('user-menu-button');
        this.containerEl = document.getElementById('userMenuContainer');
        
        if (!this.dropdownEl || !this.buttonEl) {
            console.warn('Dropdown elements not found');
            return;
        }
        
        this.setupEventListeners();
    },
    
    /**
     * Setup all event listeners
     */
    setupEventListeners() {
        // Button click
        if (this.buttonEl) {
            this.buttonEl.addEventListener('click', (e) => this.toggle(e));
        }
        
        // Document click (close on outside click)
        document.addEventListener('click', (e) => this.handleOutsideClick(e));
        
        // Dropdown links
        if (this.dropdownEl) {
            this.setupDropdownLinks();
        }
    },
    
    /**
     * Setup dropdown link handlers
     */
    setupDropdownLinks() {
        const links = this.dropdownEl.querySelectorAll('a');
        links.forEach(link => {
            link.addEventListener('click', () => this.close());
        });
    },
    
    /**
     * Toggle dropdown visibility
     * @param {Event} event - Click event
     */
    toggle(event) {
        event.stopPropagation();
        if (this.isOpen) {
            this.close();
        } else {
            this.open();
        }
    },
    
    /**
     * Open dropdown
     */
    open() {
        if (this.dropdownEl) {
            this.dropdownEl.classList.remove('hidden');
            this.isOpen = true;
            // Add animation
            this.dropdownEl.style.animation = 'fadeIn 0.2s ease-in';
        }
    },
    
    /**
     * Close dropdown
     */
    close() {
        if (this.dropdownEl) {
            this.dropdownEl.classList.add('hidden');
            this.isOpen = false;
        }
    },
    
    /**
     * Handle outside click
     * @param {Event} event - Click event
     */
    handleOutsideClick(event) {
        if (!this.containerEl) return;
        
        // If click is outside the dropdown container, close it
        if (!this.containerEl.contains(event.target) && this.isOpen) {
            this.close();
        }
    },
    
    /**
     * Toggle state
     */
    toggleMenu() {
        this.isOpen ? this.close() : this.open();
    }
};

// ==================== DROPDOWN FUNCTIONS (Legacy Support) ====================

/**
 * Toggle user dropdown menu - Legacy support
 * @param {Event} event - Click event
 */
function toggleUserDropdown(event) {
    if (event) {
        event.stopPropagation();
    }
    DropdownManager.toggleMenu();
}

/**
 * Close user dropdown menu - Legacy support
 */
function closeUserDropdown() {
    DropdownManager.close();
}

/**
 * Open user dropdown menu - Legacy support
 */
function openUserDropdown() {
    DropdownManager.open();
}

// ==================== MOBILE MENU ====================

const MobileMenuManager = {
    isOpen: false,
    menuEl: null,
    buttonEl: null,
    
    init() {
        this.menuEl = document.getElementById('mobile-menu');
        this.buttonEl = document.getElementById('mobile-menu-button');
        
        if (!this.menuEl || !this.buttonEl) {
            return;
        }
        
        this.buttonEl.addEventListener('click', () => this.toggle());
        
        // Close menu on link click
        this.menuEl.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => this.close());
        });
    },
    
    toggle() {
        this.isOpen ? this.close() : this.open();
    },
    
    open() {
        if (this.menuEl) {
            this.menuEl.classList.remove('hidden');
            this.isOpen = true;
        }
    },
    
    close() {
        if (this.menuEl) {
            this.menuEl.classList.add('hidden');
            this.isOpen = false;
        }
    }
};

// Legacy support
function toggleMobileMenu() {
    MobileMenuManager.toggle();
}

function closeMobileMenu() {
    MobileMenuManager.close();
}

// ==================== ACTIVE PAGE HIGHLIGHTING ====================

const NavigationManager = {
    /**
     * Set active menu item based on current URL
     */
    setActivePage() {
        const currentPath = window.location.pathname;
        
        // Map paths to page names
        const pathMap = {
            '/dashboard/teacher': 'dashboard',
            '/dashboard/teacher/content': 'content',
            '/dashboard/teacher/classes': 'classes',
            '/dashboard/teacher/students': 'students',
            '/dashboard/teacher/assignments': 'assignments'
        };
        
        // Get current page
        let currentPage = null;
        for (const [path, page] of Object.entries(pathMap)) {
            if (currentPath.includes(path)) {
                currentPage = page;
                break;
            }
        }
        
        if (!currentPage) return;
        
        // Update nav links
        document.querySelectorAll('#navbar nav a').forEach(link => {
            const href = link.getAttribute('href');
            link.classList.remove('text-brand-600');
            link.classList.add('hover:text-brand-600', 'transition-colors');
            
            if (href && currentPath.includes(href) && href !== '/') {
                link.classList.remove('hover:text-brand-600', 'transition-colors');
                link.classList.add('text-brand-600');
            }
        });
        
        // Update dropdown links
        const dropdownLinks = document.querySelectorAll('#user-dropdown a[data-page]');
        dropdownLinks.forEach(link => {
            const page = link.getAttribute('data-page');
            if (page === currentPage) {
                link.classList.remove('hover:bg-slate-50');
                link.classList.add('bg-slate-100');
            } else {
                link.classList.remove('bg-slate-100');
                link.classList.add('hover:bg-slate-50');
            }
        });
    }
};

// ==================== LOGOUT HANDLER ====================

/**
 * Handle user logout with confirmation
 */
function handleLogout() {
    const confirmed = confirm('Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?');
    if (confirmed) {
        window.location.href = '/api/auth/logout';
    }
}

// ==================== KEYBOARD SHORTCUTS ====================

const KeyboardManager = {
    init() {
        document.addEventListener('keydown', (e) => {
            // ESC to close dropdown
            if (e.key === 'Escape') {
                DropdownManager.close();
                MobileMenuManager.close();
            }
        });
    }
};

// ==================== INITIALIZATION ====================

/**
 * Initialize all menu managers when DOM is ready
 */
function initializeMenus() {
    DropdownManager.init();
    MobileMenuManager.init();
    NavigationManager.setActivePage();
    KeyboardManager.init();
}

// Initialize on DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeMenus);
} else {
    initializeMenus();
}

// Re-initialize on page navigation (for AJAX/PJAX scenarios)
window.addEventListener('popstate', () => {
    NavigationManager.setActivePage();
});

// ==================== UTILITY FUNCTIONS ====================

/**
 * Navigate to a specific teacher page
 * @param {string} path - The path to navigate to
 */
function navigateToTeacher(path) {
    window.location.href = `/dashboard/teacher/${path}`;
}

/**
 * Navigate to profile
 */
function goToProfile() {
    window.location.href = '/profile';
}

/**
 * Navigate to settings
 */
function goToSettings() {
    window.location.href = '/settings';
}

/**
 * Export for use in other modules
 */
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        DropdownManager,
        MobileMenuManager,
        NavigationManager,
        KeyboardManager
    };
}
