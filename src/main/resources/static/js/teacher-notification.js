/**
 * Teacher Notification Manager
 * Centralized notification/toast system for all teacher pages
 */

const NotificationManager = (() => {
    const NOTIFICATION_TYPES = {
        success: {
            bg: 'bg-emerald-500',
            icon: 'mdi:check-circle',
            title: 'Thành công'
        },
        error: {
            bg: 'bg-red-500',
            icon: 'mdi:alert-circle',
            title: 'Lỗi'
        },
        warning: {
            bg: 'bg-amber-500',
            icon: 'mdi:alert',
            title: 'Cảnh báo'
        },
        info: {
            bg: 'bg-blue-500',
            icon: 'mdi:information',
            title: 'Thông tin'
        }
    };

    const DEFAULT_DURATION = 5000; // 5 seconds

    /**
     * Show a notification
     * @param {string} type - 'success', 'error', 'warning', 'info'
     * @param {object} options - {title, message, duration}
     */
    function show(type = 'info', options = {}) {
        const typeConfig = NOTIFICATION_TYPES[type] || NOTIFICATION_TYPES.info;
        const {
            title = options.title || typeConfig.title,
            message = options.message || '',
            duration = options.duration !== undefined ? options.duration : DEFAULT_DURATION
        } = options;

        // Create notification from template
        const template = document.getElementById('notification-template');
        const notification = template.content.cloneNode(true);

        // Set content
        notification.getElementById('notification-icon').setAttribute('icon', typeConfig.icon);
        notification.getElementById('notification-title').textContent = title;
        notification.getElementById('notification-message').textContent = message;

        // Add type styling
        const notificationItem = notification.querySelector('.notification-item');
        notificationItem.classList.add(typeConfig.bg);

        // Add to container
        const container = document.getElementById('notification-container');
        container.appendChild(notification);

        // Get the actual element (after it's added to DOM)
        const addedNotification = container.querySelector('.notification-item:last-child');

        // Auto-remove after duration if specified
        if (duration > 0) {
            setTimeout(() => {
                if (addedNotification && addedNotification.parentNode) {
                    addedNotification.classList.add('animate-slide-out');
                    setTimeout(() => {
                        addedNotification.remove();
                    }, 300);
                }
            }, duration);
        }

        return addedNotification;
    }

    /**
     * Show success notification
     */
    function success(message, title = 'Thành công') {
        return show('success', { title, message });
    }

    /**
     * Show error notification
     */
    function error(message, title = 'Lỗi') {
        return show('error', { title, message });
    }

    /**
     * Show warning notification
     */
    function warning(message, title = 'Cảnh báo') {
        return show('warning', { title, message });
    }

    /**
     * Show info notification
     */
    function info(message, title = 'Thông tin') {
        return show('info', { title, message });
    }

    /**
     * Show confirmation dialog (custom modal instead of alert)
     */
    function confirm(message, onConfirm = null, onCancel = null) {
        // Create modal
        const modalHTML = `
            <div id="confirm-modal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
                <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md">
                    <div class="flex items-center justify-between p-6 border-b border-slate-100">
                        <h2 class="text-lg font-semibold text-slate-900">Xác nhận</h2>
                        <button onclick="document.getElementById('confirm-modal').remove()" class="text-slate-400 hover:text-slate-600">
                            <iconify-icon icon="mdi:close" width="24"></iconify-icon>
                        </button>
                    </div>
                    <div class="p-6">
                        <p class="text-slate-600">${message}</p>
                    </div>
                    <div class="flex gap-3 p-6 bg-slate-50 rounded-b-2xl">
                        <button onclick="document.getElementById('confirm-modal').remove()" class="flex-1 px-4 py-2 border border-slate-300 rounded-lg text-slate-700 font-medium hover:bg-slate-100 transition-colors">
                            Hủy
                        </button>
                        <button onclick="document.getElementById('confirm-modal').remove(); window.confirmCallback && window.confirmCallback(true)" class="flex-1 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-medium transition-colors">
                            Xác nhận
                        </button>
                    </div>
                </div>
            </div>
        `;

        // Parse and insert modal
        const parser = new DOMParser();
        const modalElement = parser.parseFromString(modalHTML, 'text/html').body.firstChild;
        document.body.appendChild(modalElement);

        // Set callback
        window.confirmCallback = onConfirm;

        // Handle cancel button
        modalElement.querySelector('button:nth-of-type(1)').addEventListener('click', () => {
            if (onCancel) onCancel();
        });

        // Handle confirm button
        modalElement.querySelector('button:nth-of-type(2)').addEventListener('click', () => {
            if (onConfirm) onConfirm(true);
        });

        // Close on backdrop click
        modalElement.addEventListener('click', (e) => {
            if (e.target === modalElement) {
                modalElement.remove();
                if (onCancel) onCancel();
            }
        });
    }

    /**
     * Clear all notifications
     */
    function clearAll() {
        const container = document.getElementById('notification-container');
        if (container) {
            container.innerHTML = '';
        }
    }

    // Public API
    return {
        show,
        success,
        error,
        warning,
        info,
        confirm,
        clearAll
    };
})();

// Initialize notification container if not exists
document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('notification-container')) {
        console.warn('Notification component not found. Please add the notification fragment to your page.');
    }
});
