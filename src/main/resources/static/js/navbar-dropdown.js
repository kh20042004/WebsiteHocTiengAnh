/**
 * Navbar Dropdown Interaction
 * Quản lý dropdown menu cho student navbar
 */

function initializeNavbarDropdown() {
    const userMenuButton = document.getElementById('user-menu-button');
    const userDropdown = document.getElementById('user-dropdown');

    if (!userMenuButton || !userDropdown) {
        console.warn('Navbar elements not found');
        return;
    }

    // Click button to toggle dropdown
    userMenuButton.addEventListener('click', function (e) {
        e.stopPropagation();
        userDropdown.classList.toggle('hidden');
    });

    // Click outside to close dropdown
    document.addEventListener('click', function (e) {
        if (!userMenuButton.contains(e.target) && !userDropdown.contains(e.target)) {
            userDropdown.classList.add('hidden');
        }
    });

    // Close dropdown on Escape key
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            userDropdown.classList.add('hidden');
        }
    });

    console.log('Navbar dropdown initialized');
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeNavbarDropdown);
} else {
    initializeNavbarDropdown();
}
