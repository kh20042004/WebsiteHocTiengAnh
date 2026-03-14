/**
 * Module quản lý tích hợp API
 * Xử lý các API calls, authentication, data fetching
 * 
 * @author English 12 Smart Team
 * @version 1.0.0
 */

// ============================================
// API MODULE - QUẢN LÝ TÍCH HỢP API
// ============================================

const APIModule = {
    /**
     * Cấu hình API base
     */
    config: {
        baseURL: '/api', // Base URL cho API endpoints
        timeout: 10000,  // Timeout 10 giây
        retryAttempts: 3, // Số lần retry khi failed
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    },

    /**
     * Khởi tạo API module
     */
    init() {
        console.log('🌐 [API Module] Đang khởi tạo...');
        this.initAuthInterceptor();
        this.initErrorHandler();
        this.initLoadingIndicator();
        console.log('✅ [API Module] Khởi tạo thành công!');
    },

    /**
     * Interceptor cho authentication
     * Tự động thêm JWT token vào headers
     */
    initAuthInterceptor() {
        // Lưu original fetch function
        const originalFetch = window.fetch;

        window.fetch = async (url, options = {}) => {
            // Thêm JWT token nếu có
            const token = this.getAuthToken();
            if (token) {
                options.headers = {
                    ...options.headers,
                    'Authorization': `Bearer ${token}`
                };
            }

            // Thêm base headers
            options.headers = {
                ...this.config.headers,
                ...options.headers
            };

            console.log(`📡 [API Module] Request: ${url}`);
            
            return originalFetch(url, options);
        };

        console.log('✅ [API Module] Auth interceptor đã được thiết lập');
    },

    /**
     * Xử lý lỗi global cho API calls
     */
    initErrorHandler() {
        window.addEventListener('unhandledrejection', (event) => {
            if (event.reason && event.reason.name === 'APIError') {
                console.error('🚨 [API Module] Unhandled API Error:', event.reason);
                this.handleAPIError(event.reason);
            }
        });

        console.log('✅ [API Module] Error handler đã được khởi tạo');
    },

    /**
     * Khởi tạo loading indicator global
     */
    initLoadingIndicator() {
        // Tạo loading overlay nếu chưa có
        if (!document.getElementById('api-loading-overlay')) {
            const overlay = document.createElement('div');
            overlay.id = 'api-loading-overlay';
            overlay.className = 'fixed inset-0 bg-black bg-opacity-50 z-50 hidden items-center justify-center';
            overlay.innerHTML = `
                <div class="bg-white rounded-lg p-6 shadow-xl">
                    <div class="flex items-center gap-3">
                        <iconify-icon icon="solar:refresh-linear" width="24" class="animate-spin text-blue-500"></iconify-icon>
                        <span class="text-gray-700 font-medium">Đang tải...</span>
                    </div>
                </div>
            `;
            document.body.appendChild(overlay);
        }

        console.log('✅ [API Module] Loading indicator đã được khởi tạo');
    },

    /**
     * Hiển thị loading indicator
     */
    showLoading() {
        const overlay = document.getElementById('api-loading-overlay');
        if (overlay) {
            overlay.classList.remove('hidden');
            overlay.classList.add('flex');
        }
    },

    /**
     * Ẩn loading indicator
     */
    hideLoading() {
        const overlay = document.getElementById('api-loading-overlay');
        if (overlay) {
            overlay.classList.add('hidden');
            overlay.classList.remove('flex');
        }
    },

    /**
     * GET request với error handling và retry
     * @param {string} endpoint - API endpoint
     * @param {Object} options - Tùy chọn request
     * @returns {Promise} Response data
     */
    async get(endpoint, options = {}) {
        return this.makeRequest('GET', endpoint, null, options);
    },

    /**
     * POST request
     * @param {string} endpoint - API endpoint
     * @param {Object} data - Dữ liệu gửi lên
     * @param {Object} options - Tùy chọn request
     * @returns {Promise} Response data
     */
    async post(endpoint, data, options = {}) {
        return this.makeRequest('POST', endpoint, data, options);
    },

    /**
     * PUT request  
     * @param {string} endpoint - API endpoint
     * @param {Object} data - Dữ liệu cập nhật
     * @param {Object} options - Tùy chọn request
     * @returns {Promise} Response data
     */
    async put(endpoint, data, options = {}) {
        return this.makeRequest('PUT', endpoint, data, options);
    },

    /**
     * DELETE request
     * @param {string} endpoint - API endpoint
     * @param {Object} options - Tùy chọn request
     * @returns {Promise} Response data
     */
    async delete(endpoint, options = {}) {
        return this.makeRequest('DELETE', endpoint, null, options);
    },

    /**
     * Core method để thực hiện request với retry và error handling
     * @param {string} method - HTTP method
     * @param {string} endpoint - API endpoint
     * @param {Object} data - Request data
     * @param {Object} options - Request options
     * @returns {Promise} Response data
     */
    async makeRequest(method, endpoint, data = null, options = {}) {
        const url = endpoint.startsWith('http') ? endpoint : `${this.config.baseURL}${endpoint}`;
        const requestOptions = {
            method,
            headers: {
                ...this.config.headers,
                ...options.headers
            },
            ...options
        };

        // Thêm body cho POST/PUT requests
        if (data && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
            requestOptions.body = JSON.stringify(data);
        }

        // Hiển thị loading nếu được yêu cầu
        if (options.showLoading !== false) {
            this.showLoading();
        }

        let lastError;
        const maxRetries = options.retryAttempts || this.config.retryAttempts;

        // Retry logic
        for (let attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                console.log(`📤 [API Module] ${method} ${url} (Attempt ${attempt + 1})`);
                
                const response = await fetch(url, requestOptions);
                
                // Kiểm tra response status
                if (!response.ok) {
                    throw new APIError(
                        `HTTP ${response.status}: ${response.statusText}`,
                        response.status,
                        endpoint
                    );
                }

                // Parse JSON response
                let result;
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    result = await response.json();
                } else {
                    result = await response.text();
                }

                console.log(`✅ [API Module] ${method} ${url} - Success`);
                
                // Ẩn loading
                if (options.showLoading !== false) {
                    this.hideLoading();
                }

                return result;

            } catch (error) {
                lastError = error;
                console.warn(`⚠️ [API Module] ${method} ${url} - Attempt ${attempt + 1} failed:`, error.message);

                // Không retry cho một số lỗi cụ thể
                if (error.status === 401 || error.status === 403 || error.status === 404) {
                    break;
                }

                // Đợi trước khi retry (exponential backoff)
                if (attempt < maxRetries) {
                    const delay = Math.pow(2, attempt) * 1000; // 1s, 2s, 4s, ...
                    await this.delay(delay);
                }
            }
        }

        // Ẩn loading khi thất bại
        if (options.showLoading !== false) {
            this.hideLoading();
        }

        // Handle final error
        this.handleAPIError(lastError);
        throw lastError;
    },

    /**
     * Authentication API calls
     */
    auth: {
        /**
         * Đăng nhập
         * @param {Object} credentials - Thông tin đăng nhập
         * @returns {Promise} User data và token
         */
        async login(credentials) {
            try {
                const response = await APIModule.post('/auth/login', credentials);
                
                if (response.token) {
                    APIModule.setAuthToken(response.token);
                    console.log('🔐 [API Module] Đăng nhập thành công');
                    
                    // Trigger login event
                    window.dispatchEvent(new CustomEvent('userLoggedIn', { 
                        detail: response.user 
                    }));
                }
                
                return response;
            } catch (error) {
                console.error('🔐 [API Module] Lỗi đăng nhập:', error);
                throw error;
            }
        },

        /**
         * Đăng ký
         * @param {Object} userData - Thông tin người dùng
         * @returns {Promise} User data
         */
        async register(userData) {
            try {
                const response = await APIModule.post('/auth/register', userData);
                console.log('📝 [API Module] Đăng ký thành công');
                
                // Trigger registration event
                window.dispatchEvent(new CustomEvent('userRegistered', { 
                    detail: response 
                }));
                
                return response;
            } catch (error) {
                console.error('📝 [API Module] Lỗi đăng ký:', error);
                throw error;
            }
        },

        /**
         * Đăng xuất
         * @returns {Promise}
         */
        async logout() {
            try {
                await APIModule.post('/auth/logout');
                APIModule.removeAuthToken();
                console.log('🔓 [API Module] Đăng xuất thành công');
                
                // Trigger logout event
                window.dispatchEvent(new CustomEvent('userLoggedOut'));
                
                // Redirect đến trang chủ
                window.location.href = '/';
            } catch (error) {
                console.error('🔓 [API Module] Lỗi đăng xuất:', error);
                // Vẫn clear token local kể cả khi API fail
                APIModule.removeAuthToken();
            }
        },

        /**
         * Refresh token
         * @returns {Promise} New token
         */
        async refreshToken() {
            try {
                const response = await APIModule.post('/auth/refresh');
                if (response.token) {
                    APIModule.setAuthToken(response.token);
                    console.log('🔄 [API Module] Token refresh thành công');
                }
                return response;
            } catch (error) {
                console.error('🔄 [API Module] Lỗi refresh token:', error);
                // Redirect đến login nếu refresh fail
                APIModule.auth.logout();
                throw error;
            }
        }
    },

    /**
     * Quản lý token authentication
     */
    getAuthToken() {
        return localStorage.getItem('authToken');
    },

    setAuthToken(token) {
        localStorage.setItem('authToken', token);
        console.log('🔐 [API Module] Token đã được lưu');
    },

    removeAuthToken() {
        localStorage.removeItem('authToken');
        console.log('🔐 [API Module] Token đã được xóa');
    },

    /**
     * Kiểm tra trạng thái authentication
     * @returns {boolean}
     */
    isAuthenticated() {
        const token = this.getAuthToken();
        if (!token) return false;

        try {
            // Kiểm tra token có hết hạn không (nếu là JWT)
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Math.floor(Date.now() / 1000);
            
            if (payload.exp && payload.exp < currentTime) {
                console.log('🔐 [API Module] Token đã hết hạn');
                this.removeAuthToken();
                return false;
            }
            
            return true;
        } catch (error) {
            console.error('🔐 [API Module] Lỗi parse token:', error);
            this.removeAuthToken();
            return false;
        }
    },

    /**
     * Xử lý lỗi API
     * @param {Error} error - Error object
     */
    handleAPIError(error) {
        console.error('🚨 [API Module] API Error:', error);

        // Handle specific error types
        switch (error.status) {
            case 401:
                // Unauthorized - redirect to login
                console.log('🔐 [API Module] Unauthorized - redirecting to login');
                this.removeAuthToken();
                window.location.href = '/auth/login';
                break;
                
            case 403:
                // Forbidden - show error message
                this.showErrorMessage('Bạn không có quyền thực hiện hành động này');
                break;
                
            case 404:
                // Not found
                this.showErrorMessage('Không tìm thấy dữ liệu yêu cầu');
                break;
                
            case 422:
                // Validation error
                this.showErrorMessage('Dữ liệu không hợp lệ. Vui lòng kiểm tra lại');
                break;
                
            case 500:
                // Server error
                this.showErrorMessage('Lỗi máy chủ. Vui lòng thử lại sau');
                break;
                
            default:
                // Generic error
                this.showErrorMessage('Có lỗi xảy ra. Vui lòng thử lại');
                break;
        }
    },

    /**
     * Hiển thị thông báo lỗi
     * @param {string} message - Thông báo lỗi
     */
    showErrorMessage(message) {
        // Sử dụng UI Module để hiển thị toast
        if (window.UIModule) {
            window.UIModule.showToast(message, 'error');
        } else {
            alert(message);
        }
    },

    /**
     * Utility function tạo delay
     * @param {number} ms - Milliseconds
     * @returns {Promise}
     */
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    },

    /**
     * Upload file
     * @param {File} file - File object
     * @param {string} endpoint - Upload endpoint
     * @param {Object} options - Upload options
     * @returns {Promise} Upload result
     */
    async uploadFile(file, endpoint = '/upload', options = {}) {
        const formData = new FormData();
        formData.append('file', file);

        // Thêm additional fields nếu có
        if (options.fields) {
            Object.keys(options.fields).forEach(key => {
                formData.append(key, options.fields[key]);
            });
        }

        const requestOptions = {
            method: 'POST',
            body: formData,
            headers: {
                // Không set Content-Type để browser tự set với boundary
                ...options.headers
            },
            showLoading: options.showLoading !== false
        };

        // Remove Content-Type for file upload
        delete requestOptions.headers['Content-Type'];

        return this.makeRequest('POST', endpoint, null, requestOptions);
    }
};

/**
 * Custom Error class cho API errors
 */
class APIError extends Error {
    constructor(message, status, endpoint) {
        super(message);
        this.name = 'APIError';
        this.status = status;
        this.endpoint = endpoint;
    }
}

// Export module và Error class
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { APIModule, APIError };
}