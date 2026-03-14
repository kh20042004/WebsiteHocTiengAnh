/**
 * Module quản lý form và tương tác người dùng
 * Xử lý các form, validation, modal, toast notifications
 * 
 * @author English 12 Smart Team
 * @version 1.0.0
 */

// ============================================
// UI MODULE - QUẢN LÝ GIAO DIỆN NGƯỜI DÙNG
// ============================================

const UIModule = {
    /**
     * Khởi tạo toàn bộ chức năng UI
     */
    init() {
        console.log('🎯 [UI Module] Đang khởi tạo...');
        this.initToastSystem();
        this.initModalSystem();
        this.initFormValidation();
        this.initLoadingStates();
        this.initThemeToggle();
        this.initToolTips();
        console.log('✅ [UI Module] Khởi tạo thành công!');
    },

    /**
     * Hệ thống Toast Notifications
     * Hiển thị thông báo success, error, warning, info
     */
    initToastSystem() {
        // Tạo container cho toasts nếu chưa có
        if (!document.getElementById('toast-container')) {
            const container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'fixed top-4 right-4 z-50 space-y-2';
            document.body.appendChild(container);
        }

        console.log('✅ [UI Module] Toast system đã được khởi tạo');
    },

    /**
     * Hiển thị toast notification
     * @param {string} message - Nội dung thông báo
     * @param {string} type - Loại thông báo (success, error, warning, info)
     * @param {number} duration - Thời gian hiển thị (ms)
     */
    showToast(message, type = 'info', duration = 4000) {
        const container = document.getElementById('toast-container');
        
        const toast = document.createElement('div');
        toast.className = `toast toast-${type} transform translate-x-full opacity-0 transition-all duration-300`;
        
        // Icon mapping
        const icons = {
            success: 'solar:check-circle-bold',
            error: 'solar:close-circle-bold', 
            warning: 'solar:danger-triangle-bold',
            info: 'solar:info-circle-bold'
        };

        // Color mapping
        const colors = {
            success: 'bg-green-500 text-white',
            error: 'bg-red-500 text-white',
            warning: 'bg-yellow-500 text-black',
            info: 'bg-blue-500 text-white'
        };

        toast.innerHTML = `
            <div class="flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg ${colors[type]} min-w-80 max-w-96">
                <iconify-icon icon="${icons[type]}" width="20" class="flex-shrink-0"></iconify-icon>
                <span class="text-sm font-medium flex-1">${message}</span>
                <button class="toast-close ml-2 hover:opacity-70 transition-opacity">
                    <iconify-icon icon="solar:close-linear" width="16"></iconify-icon>
                </button>
            </div>
        `;

        // Thêm vào container
        container.appendChild(toast);

        // Animation hiện toast
        requestAnimationFrame(() => {
            toast.classList.remove('translate-x-full', 'opacity-0');
            toast.classList.add('translate-x-0', 'opacity-100');
        });

        // Xử lý nút đóng
        const closeBtn = toast.querySelector('.toast-close');
        const closeToast = () => {
            toast.classList.add('translate-x-full', 'opacity-0');
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.remove();
                }
            }, 300);
        };

        closeBtn.addEventListener('click', closeToast);

        // Tự động đóng sau duration
        if (duration > 0) {
            setTimeout(closeToast, duration);
        }

        console.log(`🔔 [UI Module] Toast hiển thị: ${type} - ${message}`);
        
        return toast;
    },

    /**
     * Hệ thống Modal Dialog
     * Quản lý việc mở/đóng modal và overlay
     */
    initModalSystem() {
        // Xử lý các nút mở modal
        document.addEventListener('click', (e) => {
            const modalTrigger = e.target.closest('[data-modal-target]');
            if (modalTrigger) {
                e.preventDefault();
                const modalId = modalTrigger.getAttribute('data-modal-target');
                this.openModal(modalId);
            }

            // Xử lý đóng modal
            const modalClose = e.target.closest('[data-modal-close]');
            if (modalClose) {
                e.preventDefault();
                const modal = modalClose.closest('.modal');
                if (modal) {
                    this.closeModal(modal.id);
                }
            }
        });

        // Đóng modal khi click overlay
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal-overlay')) {
                const modal = e.target.querySelector('.modal-content').closest('.modal');
                if (modal) {
                    this.closeModal(modal.id);
                }
            }
        });

        // Đóng modal khi nhấn ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                const openModal = document.querySelector('.modal.modal-open');
                if (openModal) {
                    this.closeModal(openModal.id);
                }
            }
        });

        console.log('✅ [UI Module] Modal system đã được khởi tạo');
    },

    /**
     * Mở modal theo ID
     * @param {string} modalId - ID của modal cần mở
     */
    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) {
            console.warn(`⚠️ [UI Module] Không tìm thấy modal: ${modalId}`);
            return;
        }

        // Đóng modal khác nếu đang mở
        const openModal = document.querySelector('.modal.modal-open');
        if (openModal && openModal !== modal) {
            this.closeModal(openModal.id);
        }

        modal.classList.add('modal-open');
        modal.classList.remove('hidden');
        document.body.classList.add('modal-open');

        // Focus vào modal cho accessibility
        modal.focus();

        console.log(`📋 [UI Module] Modal mở: ${modalId}`);
    },

    /**
     * Đóng modal theo ID
     * @param {string} modalId - ID của modal cần đóng
     */
    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) {
            return;
        }

        modal.classList.remove('modal-open');
        setTimeout(() => {
            modal.classList.add('hidden');
        }, 300); // Đợi animation hoàn thành

        document.body.classList.remove('modal-open');

        console.log(`📋 [UI Module] Modal đóng: ${modalId}`);
    },

    /**
     * Validation cho forms
     * Kiểm tra và hiển thị lỗi real-time
     */
    initFormValidation() {
        const forms = document.querySelectorAll('form[data-validate]');

        forms.forEach(form => {
            const inputs = form.querySelectorAll('input, textarea, select');

            inputs.forEach(input => {
                // Validation khi blur
                input.addEventListener('blur', () => {
                    this.validateField(input);
                });

                // Xóa lỗi khi bắt đầu nhập lại
                input.addEventListener('input', () => {
                    this.clearFieldError(input);
                });
            });

            // Validation khi submit form
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                
                let isValid = true;
                inputs.forEach(input => {
                    if (!this.validateField(input)) {
                        isValid = false;
                    }
                });

                if (isValid) {
                    this.handleFormSubmit(form);
                } else {
                    this.showToast('Vui lòng kiểm tra lại thông tin nhập vào', 'error');
                }
            });
        });

        console.log(`✅ [UI Module] Form validation đã khởi tạo cho ${forms.length} forms`);
    },

    /**
     * Validate một field cụ thể
     * @param {Element} field - Input field cần validate
     * @returns {boolean} - True nếu hợp lệ
     */
    validateField(field) {
        const value = field.value.trim();
        const type = field.type;
        const required = field.hasAttribute('required');
        const pattern = field.getAttribute('pattern');
        const minLength = field.getAttribute('minlength');
        const maxLength = field.getAttribute('maxlength');

        // Clear previous errors
        this.clearFieldError(field);

        // Check required
        if (required && !value) {
            this.showFieldError(field, 'Trường này là bắt buộc');
            return false;
        }

        if (value) {
            // Check email format
            if (type === 'email' && !this.isValidEmail(value)) {
                this.showFieldError(field, 'Email không hợp lệ');
                return false;
            }

            // Check pattern
            if (pattern && !new RegExp(pattern).test(value)) {
                this.showFieldError(field, 'Định dạng không hợp lệ');
                return false;
            }

            // Check min length
            if (minLength && value.length < parseInt(minLength)) {
                this.showFieldError(field, `Tối thiểu ${minLength} ký tự`);
                return false;
            }

            // Check max length
            if (maxLength && value.length > parseInt(maxLength)) {
                this.showFieldError(field, `Tối đa ${maxLength} ký tự`);
                return false;
            }
        }

        return true;
    },

    /**
     * Hiển thị lỗi cho field
     * @param {Element} field - Input field
     * @param {string} message - Thông báo lỗi
     */
    showFieldError(field, message) {
        field.classList.add('field-error');
        
        // Tạo hoặc cập nhật error message
        let errorElement = field.parentNode.querySelector('.field-error-message');
        if (!errorElement) {
            errorElement = document.createElement('div');
            errorElement.className = 'field-error-message text-red-500 text-sm mt-1';
            field.parentNode.appendChild(errorElement);
        }
        
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    },

    /**
     * Xóa lỗi của field
     * @param {Element} field - Input field
     */
    clearFieldError(field) {
        field.classList.remove('field-error');
        
        const errorElement = field.parentNode.querySelector('.field-error-message');
        if (errorElement) {
            errorElement.style.display = 'none';
        }
    },

    /**
     * Kiểm tra email hợp lệ
     * @param {string} email - Email cần kiểm tra
     * @returns {boolean}
     */
    isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    },

    /**
     * Xử lý submit form
     * @param {Element} form - Form element
     */
    async handleFormSubmit(form) {
        const action = form.getAttribute('action') || '#';
        const method = form.getAttribute('method') || 'POST';
        const formData = new FormData(form);

        // Hiển thị loading state
        this.setFormLoading(form, true);

        try {
            // Giả lập API call
            await this.simulateAPICall();
            
            this.showToast('Gửi thành công!', 'success');
            form.reset();
            
        } catch (error) {
            this.showToast('Có lỗi xảy ra. Vui lòng thử lại!', 'error');
            console.error('[UI Module] Form submission error:', error);
            
        } finally {
            this.setFormLoading(form, false);
        }
    },

    /**
     * Quản lý trạng thái loading cho forms và buttons
     */
    initLoadingStates() {
        console.log('✅ [UI Module] Loading states đã được khởi tạo');
    },

    /**
     * Set loading state cho form
     * @param {Element} form - Form element  
     * @param {boolean} isLoading - Trạng thái loading
     */
    setFormLoading(form, isLoading) {
        const submitButton = form.querySelector('button[type="submit"]');
        const inputs = form.querySelectorAll('input, textarea, select');

        if (isLoading) {
            // Disable form
            inputs.forEach(input => input.disabled = true);
            
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.innerHTML = `
                    <iconify-icon icon="solar:refresh-linear" width="16" class="animate-spin mr-2"></iconify-icon>
                    Đang gửi...
                `;
            }
        } else {
            // Enable form
            inputs.forEach(input => input.disabled = false);
            
            if (submitButton) {
                submitButton.disabled = false;
                submitButton.innerHTML = submitButton.getAttribute('data-original-text') || 'Gửi';
            }
        }
    },

    /**
     * Theme toggle (Light/Dark mode)
     */
    initThemeToggle() {
        const themeToggle = document.getElementById('theme-toggle');
        
        if (themeToggle) {
            const currentTheme = localStorage.getItem('theme') || 'light';
            document.documentElement.setAttribute('data-theme', currentTheme);

            themeToggle.addEventListener('click', () => {
                const currentTheme = document.documentElement.getAttribute('data-theme');
                const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
                
                document.documentElement.setAttribute('data-theme', newTheme);
                localStorage.setItem('theme', newTheme);
                
                console.log(`🌓 [UI Module] Theme changed to: ${newTheme}`);
            });
        }

        console.log('✅ [UI Module] Theme toggle đã được khởi tạo');
    },

    /**
     * Tooltip system
     */
    initToolTips() {
        const tooltipElements = document.querySelectorAll('[data-tooltip]');
        
        tooltipElements.forEach(element => {
            element.addEventListener('mouseenter', (e) => {
                const message = e.target.getAttribute('data-tooltip');
                const position = e.target.getAttribute('data-tooltip-position') || 'top';
                this.showTooltip(e.target, message, position);
            });

            element.addEventListener('mouseleave', () => {
                this.hideTooltip();
            });
        });

        console.log(`✅ [UI Module] Tooltips đã khởi tạo cho ${tooltipElements.length} elements`);
    },

    /**
     * Hiển thị tooltip
     * @param {Element} element - Element trigger
     * @param {string} message - Nội dung tooltip
     * @param {string} position - Vị trí (top, bottom, left, right)
     */
    showTooltip(element, message, position = 'top') {
        // Xóa tooltip cũ nếu có
        this.hideTooltip();

        const tooltip = document.createElement('div');
        tooltip.id = 'active-tooltip';
        tooltip.className = `absolute z-50 px-2 py-1 text-xs bg-gray-900 text-white rounded shadow-lg pointer-events-none tooltip-${position}`;
        tooltip.textContent = message;

        document.body.appendChild(tooltip);

        // Tính toán vị trí
        const rect = element.getBoundingClientRect();
        const tooltipRect = tooltip.getBoundingClientRect();

        let top, left;

        switch (position) {
            case 'top':
                top = rect.top - tooltipRect.height - 8;
                left = rect.left + (rect.width - tooltipRect.width) / 2;
                break;
            case 'bottom':
                top = rect.bottom + 8;
                left = rect.left + (rect.width - tooltipRect.width) / 2;
                break;
            case 'left':
                top = rect.top + (rect.height - tooltipRect.height) / 2;
                left = rect.left - tooltipRect.width - 8;
                break;
            case 'right':
                top = rect.top + (rect.height - tooltipRect.height) / 2;
                left = rect.right + 8;
                break;
        }

        tooltip.style.top = `${top + window.scrollY}px`;
        tooltip.style.left = `${left + window.scrollX}px`;
    },

    /**
     * Ẩn tooltip
     */
    hideTooltip() {
        const tooltip = document.getElementById('active-tooltip');
        if (tooltip) {
            tooltip.remove();
        }
    },

    /**
     * Utility: Giả lập API call
     * @returns {Promise}
     */
    simulateAPICall() {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                // 90% success rate cho demo
                if (Math.random() > 0.1) {
                    resolve('Success');
                } else {
                    reject(new Error('Simulated API error'));
                }
            }, 1500);
        });
    }
};

// Export module
if (typeof module !== 'undefined' && module.exports) {
    module.exports = UIModule;
}