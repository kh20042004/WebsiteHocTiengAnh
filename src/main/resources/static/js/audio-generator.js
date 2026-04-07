/**
 * Audio Generator for Lessons
 * Handles generation of TTS audio for lesson content
 */

// ========== AXIOS INTERCEPTOR SETUP ==========
// Automatically add JWT token to all axios requests (if available)
if (typeof axios !== 'undefined') {
    axios.interceptors.request.use(function (config) {
        // Try to get token from multiple sources
        const token = 
            // 1. From meta tag (Thymeleaf injected)
            document.querySelector('meta[name="jwt-token"]')?.getAttribute('content') ||
            // 2. From localStorage
            localStorage.getItem('jwtToken') || 
            // 3. From sessionStorage
            sessionStorage.getItem('jwtToken') ||
            // 4. From cookie
            getCookieValue('jwtToken');
        
        // Add token if available, otherwise rely on Spring Security session cookie
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    }, function (error) {
        return Promise.reject(error);
    });
}

/**
 * Helper function to get cookie value by name
 */
function getCookieValue(name) {
    const nameEQ = name + "=";
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
        let cookie = cookies[i].trim();
        if (cookie.indexOf(nameEQ) === 0) {
            return cookie.substring(nameEQ.length);
        }
    }
    return null;
}

class AudioGenerator {
    constructor() {
        this.isGenerating = false;
        this.currentLessonId = null;
    }

    /**
     * Initialize audio generator UI
     * Call this when page loads
     */
    init() {
        console.log('AudioGenerator initialized');
        this.setupEventListeners();
    }

    /**
     * Setup event listeners for audio buttons
     */
    setupEventListeners() {
        // Generate audio button
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-action="generate-audio"]')) {
                const lessonId = e.target.getAttribute('data-lesson-id');
                this.generateAudio(lessonId);
            }

            if (e.target.matches('[data-action="delete-audio"]')) {
                const lessonId = e.target.getAttribute('data-lesson-id');
                this.deleteAudio(lessonId);
            }

            if (e.target.matches('[data-action="regenerate-audio"]')) {
                const lessonId = e.target.getAttribute('data-lesson-id');
                if (confirm('Bạn có chắc muốn tạo lại audio? Audio cũ sẽ bị xóa.')) {
                    this.deleteAudio(lessonId, () => this.generateAudio(lessonId));
                }
            }
        });
    }

    /**
     * Generate audio for a lesson
     * @param {string} lessonId - The lesson ID
     * @param {string} voiceType - Optional voice type (default: en-US-AriaNeural)
     */
    async generateAudio(lessonId, voiceType = 'en-US-AriaNeural') {
        if (this.isGenerating) {
            this.showWarning('Đang tạo audio, vui lòng chờ...');
            return;
        }

        console.log('Starting audio generation for lesson:', lessonId);

        this.currentLessonId = lessonId;
        this.isGenerating = true;

        try {
            // Show loading state
            this.updateButtonState(lessonId, 'loading');

            // Make API call
            const response = await this.callGenerateAudioAPI(lessonId, voiceType);

            if (response.success) {
                console.log('Audio generated successfully:', response.data);

                // Show success toast
                this.showSuccess('Audio đã được tạo thành công! ✨', 'Bạn có thể nghe audio trong phần nội dung bài học.');

                // Update button state
                this.updateButtonState(lessonId, 'success');

                // Reload lesson content to show new audio
                setTimeout(() => {
                    location.reload();
                }, 2000);
            } else {
                this.showError('Lỗi tạo audio', response.error || 'Không xác định');
                this.updateButtonState(lessonId, 'error');
            }
        } catch (error) {
            console.error('Error generating audio:', error);

            let errorMessage = 'Lỗi kết nối tới dịch vụ TTS. Vui lòng thử lại.';
            if (error.response?.data?.error) {
                errorMessage = error.response.data.error;
            } else if (error.message) {
                errorMessage = error.message;
            }

            this.showError('Lỗi tạo audio', errorMessage);
            this.updateButtonState(lessonId, 'error');
        } finally {
            this.isGenerating = false;
        }
    }

    /**
     * Delete audio for a lesson
     * @param {string} lessonId - The lesson ID
     * @param {function} callback - Optional callback after deletion
     */
    async deleteAudio(lessonId, callback = null) {
        console.log('Deleting audio for lesson:', lessonId);

        try {
            this.updateButtonState(lessonId, 'loading');

            const response = await axios.delete(`/api/lessons/${lessonId}/audio`);

            if (response.data.success) {
                this.showSuccess('Audio đã được xóa thành công!');
                this.updateButtonState(lessonId, 'deleted');

                if (callback) {
                    setTimeout(callback, 1000);
                } else {
                    setTimeout(() => location.reload(), 1500);
                }
            } else {
                this.showError('Lỗi xóa audio', response.data.error);
                this.updateButtonState(lessonId, 'error');
            }
        } catch (error) {
            console.error('Error deleting audio:', error);
            this.showError('Lỗi xóa audio', error.message);
            this.updateButtonState(lessonId, 'error');
        }
    }

    /**
     * Call the generate audio API
     */
    async callGenerateAudioAPI(lessonId, voiceType) {
        try {
            const response = await axios.post(`/api/lessons/${lessonId}/generate-audio`, {
                voice: voiceType,
                rate: '+0%',
                pitch: '+0Hz'
            }, {
                timeout: 120000 // 2 minutes timeout for TTS generation
            });

            return {
                success: response.data.success !== false,
                data: response.data.data || response.data,
                error: response.data.message || response.data.error
            };
        } catch (error) {
            throw error;
        }
    }

    /**
     * Update button state (loading, success, error, etc.)
     */
    updateButtonState(lessonId, state) {
        const button = document.querySelector(`[data-action="generate-audio"][data-lesson-id="${lessonId}"]`);
        if (!button) return;

        const originalHTML = button.innerHTML;

        switch (state) {
            case 'loading':
                button.disabled = true;
                button.classList.add('opacity-65');
                button.innerHTML = `
                    <span class="inline-flex items-center gap-2">
                        <span class="inline-block animate-spin">
                            <i class="fas fa-spinner"></i>
                        </span>
                        Đang tạo audio...
                    </span>
                `;
                break;

            case 'success':
                button.classList.remove('btn-primary');
                button.classList.add('btn-success');
                button.innerHTML = `
                    <span class="inline-flex items-center gap-2">
                        <i class="fas fa-check-circle"></i>
                        Audio đã tạo
                    </span>
                `;
                setTimeout(() => {
                    button.disabled = false;
                    button.classList.remove('opacity-65', 'btn-success');
                    button.classList.add('btn-primary');
                    button.innerHTML = originalHTML;
                }, 3000);
                break;

            case 'error':
                button.classList.remove('btn-primary');
                button.classList.add('btn-danger');
                button.innerHTML = `
                    <span class="inline-flex items-center gap-2">
                        <i class="fas fa-times-circle"></i>
                        Lỗi, thử lại
                    </span>
                `;
                setTimeout(() => {
                    button.disabled = false;
                    button.classList.remove('opacity-65', 'btn-danger');
                    button.classList.add('btn-primary');
                    button.innerHTML = originalHTML;
                }, 3000);
                break;

            case 'deleted':
                button.classList.remove('btn-primary');
                button.classList.add('btn-warning');
                button.innerHTML = `
                    <span class="inline-flex items-center gap-2">
                        <i class="fas fa-trash-alt"></i>
                        Audio đã xóa
                    </span>
                `;
                break;
        }
    }

    /**
     * Show success toast notification
     */
    showSuccess(title, message = '') {
        const toast = document.createElement('div');
        toast.className = 'fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg flex items-center gap-3 z-50';
        toast.innerHTML = `
            <i class="fas fa-check-circle text-lg"></i>
            <div>
                <div class="font-semibold">${title}</div>
                ${message ? `<div class="text-sm opacity-90">${message}</div>` : ''}
            </div>
        `;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('animate-fade-out');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    /**
     * Show error toast notification
     */
    showError(title, message = '') {
        const toast = document.createElement('div');
        toast.className = 'fixed top-4 right-4 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg flex items-center gap-3 z-50';
        toast.innerHTML = `
            <i class="fas fa-exclamation-circle text-lg"></i>
            <div>
                <div class="font-semibold">${title}</div>
                ${message ? `<div class="text-sm opacity-90">${message}</div>` : ''}
            </div>
        `;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('animate-fade-out');
            setTimeout(() => toast.remove(), 300);
        }, 5000);
    }

    /**
     * Show warning toast notification
     */
    showWarning(message) {
        const toast = document.createElement('div');
        toast.className = 'fixed top-4 right-4 bg-yellow-500 text-white px-6 py-3 rounded-lg shadow-lg flex items-center gap-3 z-50';
        toast.innerHTML = `
            <i class="fas fa-info-circle text-lg"></i>
            <div>${message}</div>
        `;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('animate-fade-out');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }
}

// Initialize when document is ready
document.addEventListener('DOMContentLoaded', () => {
    window.audioGenerator = new AudioGenerator();
    window.audioGenerator.init();
});

// Shortcut functions for inline onclick handlers
function generateLessonAudio(lessonId, voiceType = 'en-US-AriaNeural') {
    if (window.audioGenerator) {
        window.audioGenerator.generateAudio(lessonId, voiceType);
    }
}

function deleteLessonAudio(lessonId) {
    if (confirm('Bạn có chắc muốn xóa audio?')) {
        if (window.audioGenerator) {
            window.audioGenerator.deleteAudio(lessonId);
        }
    }
}

function regenerateLessonAudio(lessonId, voiceType = 'en-US-AriaNeural') {
    if (confirm('Tạo lại audio sẽ xóa audio cũ. Tiếp tục?')) {
        if (window.audioGenerator) {
            window.audioGenerator.deleteAudio(lessonId, () => {
                window.audioGenerator.generateAudio(lessonId, voiceType);
            });
        }
    }
}
