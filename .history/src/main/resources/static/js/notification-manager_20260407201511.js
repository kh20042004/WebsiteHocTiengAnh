/**
 * Quản lý tính năng Notification trên Navbar
 */
document.addEventListener('DOMContentLoaded', function() {
    
    // Các UI Elements
    const notificationBtn = document.getElementById('notification-button');
    const notificationDropdown = document.getElementById('notification-dropdown');
    const notificationBadge = document.getElementById('notification-badge');
    const notificationList = document.getElementById('notification-list');
    const markAllReadBtn = document.getElementById('mark-all-read-btn');
    
    if (!notificationBtn || !notificationDropdown) return;

    let isDropdownOpen = false;

    // 1. Fetch số lượng chưa đọc khi tải trang
    fetchUnreadCount();

    // 2. Toggle Dropdown khi click vào nút chuông
    notificationBtn.addEventListener('click', function(e) {
        e.stopPropagation(); // Ngăn sự kiện click lan truyền ra ngoài
        isDropdownOpen = !isDropdownOpen;
        
        if (isDropdownOpen) {
            notificationDropdown.classList.remove('hidden');
            // Fetch danh sách thông báo khi mở
            fetchNotifications();
            
            // Đóng các dropdown khác nếu có (như User Menu)
            const userDropdown = document.getElementById('user-dropdown');
            if (userDropdown && !userDropdown.classList.contains('hidden')) {
                userDropdown.classList.add('hidden');
            }
        } else {
            notificationDropdown.classList.add('hidden');
        }
    });

    // Clicks external to dropdown -> close it
    document.addEventListener('click', function(e) {
        if (isDropdownOpen && !notificationDropdown.contains(e.target) && !notificationBtn.contains(e.target)) {
            notificationDropdown.classList.add('hidden');
            isDropdownOpen = false;
        }
    });

    // 3. Đánh dấu tất cả đã đọc
    if (markAllReadBtn) {
        markAllReadBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            markAllAsRead();
        });
    }

    // ================= Hàm gọi API =================
    
    // Lấy số lượng thông báo chưa đọc
    function fetchUnreadCount() {
        fetch('/api/notifications/unread-count')
            .then(res => {
                if (!res.ok) throw new Error('Unauthorized or Error');
                return res.json();
            })
            .then(data => {
                if (data.count > 0) {
                    notificationBadge.textContent = data.count > 99 ? '99+' : data.count;
                    notificationBadge.classList.remove('hidden');
                } else {
                    notificationBadge.classList.add('hidden');
                }
            })
            .catch(err => console.error('Lỗi khi fetch unread count:', err));
    }

    // Lấy danh sách thông báo (trang 0, size 5)
    function fetchNotifications() {
        notificationList.innerHTML = '<div class="p-4 text-center text-sm text-slate-500">Đang tải...</div>';
        
        fetch('/api/notifications?page=0&size=5')
            .then(res => {
                if (!res.ok) throw new Error('Failed to fetch');
                return res.json();
            })
            .then(data => {
                renderNotifications(data.content);
            })
            .catch(err => {
                console.error(err);
                notificationList.innerHTML = '<div class="p-4 text-center text-sm text-red-500">Lỗi khi tải thông báo.</div>';
            });
    }

    // Đánh dấu 1 thông báo là đã đọc
    function markAsRead(id, targetUrl) {
        fetch(`/api/notifications/${id}/read`, {
            method: 'PUT'
        })
        .then(res => {
            if(res.ok) {
                // Điều hướng nếu có
                if(targetUrl && targetUrl !== 'null' && targetUrl !== '') {
                    window.location.href = targetUrl;
                } else {
                    // Update UI (ví dụ fetch lại danh sách)
                    fetchNotifications();
                    fetchUnreadCount();
                }
            }
        })
        .catch(err => console.error(err));
    }

    // Đánh dấu toàn bộ là đã đọc
    function markAllAsRead() {
        fetch('/api/notifications/read-all', {
            method: 'PUT'
        })
        .then(res => {
            if(res.ok) {
                fetchNotifications();
                fetchUnreadCount();
            }
        })
        .catch(err => console.error(err));
    }

    // ================= Hàm Render Giao Diện =================

    function renderNotifications(notifications) {
        if (!notifications || notifications.length === 0) {
            notificationList.innerHTML = `
                <div class="p-6 text-center">
                    <iconify-icon icon="solar:bell-bing-bold-duotone" width="40" class="text-slate-200 mb-2"></iconify-icon>
                    <p class="text-sm text-slate-500">Chưa có thông báo nào.</p>
                </div>`;
            return;
        }

        let html = '';
        notifications.forEach(noti => {
            const isUnread = !noti.isRead;
            const bgClass = isUnread ? 'bg-brand-50' : 'bg-white';
            const dotClass = isUnread ? '<div class="w-2 h-2 rounded-full bg-brand-600 mt-2"></div>' : '';
            
            // Xử lý logic format thời gian từ timestamp (noti.createdAt)
            const timeString = formatTimeAgo(noti.createdAt);
            
            // Chọn icon dựa trên type
            let iconStr = getIconForType(noti.type);

            html += `
                <div class="notification-item flex gap-3 p-3 border-b border-slate-50 hover:bg-slate-50 cursor-pointer transition-colors ${bgClass}" 
                     data-id="${noti.id}" ${noti.targetUrl ? `data-url="${noti.targetUrl}"` : ''}>
                    <div class="flex-shrink-0 mt-1">
                        ${iconStr}
                    </div>
                    <div class="flex-grow">
                        <p class="text-sm font-medium ${isUnread ? 'text-slate-900' : 'text-slate-700'}">${noti.title}</p>
                        <p class="text-xs text-slate-500 mt-0.5 line-clamp-2">${noti.message}</p>
                        <p class="text-[10px] text-slate-400 mt-1">${timeString}</p>
                    </div>
                    ${dotClass}
                </div>
            `;
        });

        notificationList.innerHTML = html;

        // Gắn sự kiện click cho từng item
        document.querySelectorAll('.notification-item').forEach(item => {
            item.addEventListener('click', function(e) {
                const id = this.getAttribute('data-id');
                const url = this.getAttribute('data-url');
                markAsRead(id, url);
            });
        });
    }

    // Helper: Định dạng thời gian (ví dụ: "5 phút trước", "2 ngày trước")
    function formatTimeAgo(timestamp) {
        if (!timestamp) return '';
        const now = new Date().getTime();
        const diff = now - timestamp;
        
        const seconds = Math.floor(diff / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        const days = Math.floor(hours / 24);

        if (days > 0) return `${days} ngày trước`;
        if (hours > 0) return `${hours} giờ trước`;
        if (minutes > 0) return `${minutes} phút trước`;
        return `Vừa xong`;
    }

    // Helper: Lấy icon dựa trên type của notification
    function getIconForType(type) {
        let iconName = 'solar:bell-bing-bold';
        let colorClass = 'text-slate-500 bg-slate-100';

        switch(type) {
            case 'ASSIGNMENT':
                iconName = 'solar:document-text-bold';
                colorClass = 'text-blue-500 bg-blue-100';
                break;
            case 'EXAM':
                iconName = 'solar:diploma-bold';
                colorClass = 'text-emerald-500 bg-emerald-100';
                break;
            case 'SYSTEM':
                iconName = 'solar:info-circle-bold';
                colorClass = 'text-brand-500 bg-brand-100';
                break;
            case 'CLASSROOM':
                iconName = 'solar:users-group-rounded-bold';
                colorClass = 'text-purple-500 bg-purple-100';
                break;
        }

        return `<div class="w-8 h-8 rounded-full flex items-center justify-center ${colorClass}">
                    <iconify-icon icon="${iconName}" width="16"></iconify-icon>
                </div>`;
    }
});

/**
 * ============================================
 * NOTIFICATION MANAGER API
 * ============================================
 * Global API để hiển thị success/error messages
 * Sử dụng cho toast notifications hoặc alerts
 */
window.NotificationManager = (() => {
    /**
     * Tạo toast notification element
     * @param {String} message - Nội dung thông báo
     * @param {String} type - 'success' hoặc 'error'
     */
    function createToast(message, type) {
        // Nếu container chưa tồn tại, tạo mới
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'fixed top-4 right-4 z-50 flex flex-col gap-2';
            document.body.appendChild(container);
        }

        // Tạo toast element
        const toast = document.createElement('div');
        const isSuccess = type === 'success';
        const bgColor = isSuccess ? 'bg-emerald-500' : 'bg-red-500';
        const icon = isSuccess ? '✅' : '❌';

        toast.className = `${bgColor} text-white px-4 py-3 rounded-lg shadow-lg animate-slide-in flex items-center gap-2 max-w-md`;
        toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;

        container.appendChild(toast);

        // Auto remove sau 3 giây
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(400px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    /**
     * Hiển thị success message
     * @param {String} message - Nội dung thông báo
     */
    function success(message) {
        console.log('✅ Success:', message);
        
        // Nếu không có DOM elements (loading page sớm), sử dụng alert
        if (!document.body) {
            alert(message);
            return;
        }

        createToast(message, 'success');
    }

    /**
     * Hiển thị error message
     * @param {String} message - Nội dung thông báo
     */
    function error(message) {
        console.error('❌ Error:', message);
        
        // Nếu không có DOM elements (loading page sớm), sử dụng alert
        if (!document.body) {
            alert(message);
            return;
        }

        createToast(message, 'error');
    }

    /**
     * Public API
     */
    return {
        success,
        error
    };
})();
