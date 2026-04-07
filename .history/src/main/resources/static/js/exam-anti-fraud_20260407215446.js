/**
 * Anti-Cheat Module cho Exam System
 * Chứa các hàm chống gian lận trong kỳ thi
 * 
 * Tính năng:
 * - Chặn Copy/Paste
 * - Chặn Click chuột phải (context menu)
 * - Chặn Developer Tools (F12, Ctrl+Shift+I, etc.)
 * - Phát hiện rời bỏ tab
 * - Ghi nhật ký các hành động nghi ngờ
 */

class ExamAntiFraudManager {
    constructor(submissionId) {
        this.submissionId = submissionId;
        this.fraudCount = {};
        this.isFullscreen = false;
        
        // Batch logging configuration
        this.fraudEventQueue = [];
        this.batchSize = 5; // Gộp 5 events thành 1 request
        this.batchTimeout = 30000; // 30 giây timeout
        this.batchTimer = null;
        
        // Retry configuration
        this.maxRetries = 3;
        this.retryDelay = 2000; // 2 giây
        
        // Local storage backup
        this.storageKey = `fraud_logs_${submissionId}`;
        
        // Deduplicate
        this.lastEventTimestamps = {};
        this.deduplicateThreshold = 1000; // Không log sự kiện giống nhau trong 1 giây
        
        this.initializeProtections();
    }

    /**
     * Khởi tạo tất cả các hàm bảo vệ chống gian lận
     */
    initializeProtections() {
        console.log('🔒 Khởi tạo hệ thống chống gian lận cho submission:', this.submissionId);
        
        // Restore from local storage if offline
        this.restoreOfflineLogs();
        
        // Chặn Copy
        this.blockCopy();
        
        // Chặn Paste
        this.blockPaste();
        
        // Chặn Cut
        this.blockCut();
        
        // Chặn Click chuột phải
        this.blockRightClick();
        
        // Chặn DevTools
        this.blockDevTools();
        
        // Phát hiện thay đổi tab/cửa sổ
        this.detectTabChange();
        
        // Phát hiện thoát fullscreen
        this.detectFullscreenExit();
        
        // Disable drag & drop
        this.blockDragDrop();
    }

    /**
     * Chặn Copy (Ctrl+C / Cmd+C)
     */
    blockCopy() {
        document.addEventListener('copy', (e) => {
            e.preventDefault();
            const selection = window.getSelection();
            if (selection.toString().length > 0) {
                this.recordFraudEvent('COPY_ATTEMPT', 
                    `Cố gắng copy: "${selection.toString().substring(0, 50)}..."`);
                this.showWarning('⚠️ Copy không được phép trong kỳ thi');
            }
        });

        document.addEventListener('keydown', (e) => {
            // Ctrl+C hoặc Cmd+C
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'c') {
                e.preventDefault();
                this.recordFraudEvent('COPY_ATTEMPT', 'Bấm Ctrl+C/Cmd+C');
                this.showWarning('⚠️ Copy không được phép trong kỳ thi');
            }
        });
    }

    /**
     * Chặn Paste (Ctrl+V / Cmd+V)
     */
    blockPaste() {
        document.addEventListener('paste', (e) => {
            e.preventDefault();
            this.recordFraudEvent('PASTE_ATTEMPT', 'Cố gắng paste từ clipboard');
            this.showWarning('⚠️ Paste không được phép trong kỳ thi');
        });

        document.addEventListener('keydown', (e) => {
            // Ctrl+V hoặc Cmd+V
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'v') {
                e.preventDefault();
                this.recordFraudEvent('PASTE_ATTEMPT', 'Bấm Ctrl+V/Cmd+V');
                this.showWarning('⚠️ Paste không được phép trong kỳ thi');
            }
        });

        // Chặn paste qua click chuột phải
        document.addEventListener('mouseup', () => {
            if (document.querySelector('[data-no-paste]')) {
                const elem = document.activeElement;
                if (elem && elem.hasAttribute('data-no-paste')) {
                    elem.value = elem.value.replace(/[^a-zA-Z0-9]/g, '');
                }
            }
        });
    }

    /**
     * Chặn Cut (Ctrl+X / Cmd+X)
     */
    blockCut() {
        document.addEventListener('cut', (e) => {
            e.preventDefault();
            this.recordFraudEvent('CUT_ATTEMPT', 'Cố gắng cut');
            this.showWarning('⚠️ Cut không được phép trong kỳ thi');
        });

        document.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'x') {
                e.preventDefault();
                this.recordFraudEvent('CUT_ATTEMPT', 'Bấm Ctrl+X/Cmd+X');
                this.showWarning('⚠️ Cut không được phép trong kỳ thi');
            }
        });
    }

    /**
     * Chặn Click chuột phải (Context Menu)
     */
    blockRightClick() {
        document.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            this.recordFraudEvent('RIGHT_CLICK', `Click chuột phải tại: ${e.target.tagName}`);
            this.showWarning('⚠️ Click chuột phải không được phép');
            return false;
        });

        // Chặn các shortcut mở menu dev tools qua chuột phải
        document.addEventListener('mousedown', (e) => {
            if (e.button === 2) { // Right mouse button
                e.preventDefault();
                return false;
            }
        });
    }

    /**
     * Chặn DevTools
     * Phát hiện:
     * - F12
     * - Ctrl+Shift+I (DevTools)
     * - Ctrl+Shift+J (Console)
     * - Ctrl+Shift+C (Element Inspector)
     * - Ctrl+Shift+K (DevTools Console)
     */
    blockDevTools() {
        document.addEventListener('keydown', (e) => {
            // F12
            if (e.key === 'F12') {
                e.preventDefault();
                this.recordFraudEvent('DEV_TOOLS', 'Cố gắng mở DevTools (F12)');
                this.showWarning('⚠️ DevTools không được phép');
                return false;
            }

            // Ctrl+Shift+I (DevTools)
            if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key.toLowerCase() === 'i') {
                e.preventDefault();
                this.recordFraudEvent('DEV_TOOLS', 'Cố gắng mở DevTools (Ctrl+Shift+I)');
                this.showWarning('⚠️ DevTools không được phép');
                return false;
            }

            // Ctrl+Shift+J (Console trên Chrome)
            if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key.toLowerCase() === 'j') {
                e.preventDefault();
                this.recordFraudEvent('DEV_TOOLS', 'Cố gắng mở Console (Ctrl+Shift+J)');
                this.showWarning('⚠️ DevTools không được phép');
                return false;
            }

            // Ctrl+Shift+C (Element Inspector)
            if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key.toLowerCase() === 'c') {
                e.preventDefault();
                this.recordFraudEvent('DEV_TOOLS', 'Cố gắng mở Element Inspector (Ctrl+Shift+C)');
                this.showWarning('⚠️ DevTools không được phép');
                return false;
            }

            // Ctrl+Shift+K (DevTools Console Firefox)
            if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key.toLowerCase() === 'k') {
                e.preventDefault();
                this.recordFraudEvent('DEV_TOOLS', 'Cố gắng mở DevTools (Ctrl+Shift+K)');
                this.showWarning('⚠️ DevTools không được phép');
                return false;
            }

            // Ctrl+I (Inspect Element - Firefox)
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'i') {
                e.preventDefault();
                this.recordFraudEvent('DEV_TOOLS', 'Cố gắng mở DevTools (Ctrl+I)');
                this.showWarning('⚠️ DevTools không được phép');
                return false;
            }
        });

        // Phát hiện devtools mở bằng cách kiểm tra console size
        setInterval(() => {
            this.detectDevTools();
        }, 1000);
    }

    /**
     * Phát hiện DevTools mở bằng cách kiểm tra kích thước console
     */
    detectDevTools() {
        const threshold = 160;
        if (window.outerHeight - window.innerHeight > threshold ||
            window.outerWidth - window.innerWidth > threshold) {
            if (!this.devToolsDetected) {
                this.devToolsDetected = true;
                this.recordFraudEvent('DEV_TOOLS', 'DevTools được phát hiện là đang mở');
                this.showWarning('⚠️ Vui lòng đóng DevTools');
                console.warn('⚠️ DevTools Detection: Developer console has been opened!');
            }
        } else {
            this.devToolsDetected = false;
        }
    }

    /**
     * Phát hiện người dùng rời bỏ tab/cửa sổ
     */
    detectTabChange() {
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.recordFraudEvent('TAB_CHANGE', 'Thay đổi tab - bài thi không nằm trong view');
                this.showWarning('⚠️ Đừng rời khỏi tab bài thi!');
                console.warn('⚠️ Học sinh đã rời bỏ tab!');
            } else {
                console.log('✓ Học sinh quay lại tab bài thi');
            }
        });

        // Phát hiện focus/blur
        window.addEventListener('blur', () => {
            this.recordFraudEvent('TAB_CHANGE', 'Focus rời khỏi cửa sổ bài thi');
            this.showWarning('⚠️ Vui lòng giữ focus trên cửa sổ làm bài!');
        });

        window.addEventListener('focus', () => {
            console.log('✓ Focus trở lại cửa sổ làm bài');
        });
    }

    /**
     * Phát hiện thoát chế độ fullscreen
     */
    detectFullscreenExit() {
        document.addEventListener('fullscreenchange', () => {
            if (!document.fullscreenElement) {
                this.recordFraudEvent('FULLSCREEN_EXIT', 'Thoát chế độ fullscreen');
                this.showWarning('⚠️ Vui lòng giữ chế độ fullscreen');
            }
        });
    }

    /**
     * Chặn Drag & Drop
     */
    blockDragDrop() {
        document.addEventListener('dragstart', (e) => {
            e.preventDefault();
            return false;
        });

        document.addEventListener('dragover', (e) => {
            e.preventDefault();
            return false;
        });

        document.addEventListener('drop', (e) => {
            e.preventDefault();
            this.recordFraudEvent('DROP_ATTEMPT', 'Cố gắng drag & drop');
            this.showWarning('⚠️ Drag & drop không được phép');
            return false;
        });
    }

    /**
     * Ghi nhật ký hoạt động nghi ngờ
     */
    recordFraudEvent(fraudType, details) {
        // Kiểm tra deduplication - không log cùng loại events trong 1 giây
        const now = Date.now();
        const lastTimestamp = this.lastEventTimestamps[fraudType] || 0;
        
        if (now - lastTimestamp < this.deduplicateThreshold) {
            console.debug(`⏭️ Skipped duplicate fraud event: ${fraudType} (within 1s)`);
            return; // Skip duplicate event
        }
        
        this.lastEventTimestamps[fraudType] = now;

        // Tăng bộ đếm
        if (!this.fraudCount[fraudType]) {
            this.fraudCount[fraudType] = 0;
        }
        this.fraudCount[fraudType]++;

        console.warn(`🚨 Fraud Event: ${fraudType} - ${details}`, this.fraudCount);

        // Thêm vào queue để gửi batch
        const eventData = {
            fraudType: fraudType,
            details: details,
            timestamp: new Date().toISOString()
        };
        
        this.fraudEventQueue.push(eventData);

        // Nếu queue đủ, gửi ngay lập tức
        if (this.fraudEventQueue.length >= this.batchSize) {
            this.sendFraudEventBatch();
        } else {
            // Nếu đây là event đầu tiên, start timer
            if (this.fraudEventQueue.length === 1 && !this.batchTimer) {
                this.batchTimer = setTimeout(() => {
                    this.sendFraudEventBatch();
                }, this.batchTimeout);
            }
        }
    }

    /**
     * Hiển thị cảnh báo cho học sinh
     */
    showWarning(message) {
        // Tạo toast notification
        const toast = document.createElement('div');
        toast.className = 'fixed top-4 right-4 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg z-9999 animate-pulse';
        toast.textContent = message;
        document.body.appendChild(toast);

        // Xóa sau 3 giây
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }

    /**
     * Lấy báo cáo gian lận hiện tại
     */
    getFraudReport() {
        return {
            submissionId: this.submissionId,
            fraudEvents: this.fraudCount,
            totalFraudAttempts: Object.values(this.fraudCount).reduce((a, b) => a + b, 0),
            timestamp: new Date().toISOString()
        };
    }

    /**
     * Restore offline logs từ localStorage khi khởi tạo
     */
    restoreOfflineLogs() {
        try {
            const storedLogs = localStorage.getItem(this.storageKey);
            if (storedLogs) {
                const logs = JSON.parse(storedLogs);
                console.log(`📦 Restored ${logs.length} offline fraud logs from storage`);
                
                // Thêm stored logs vào queue
                this.fraudEventQueue.push(...logs);
                
                // Xóa khỏi localStorage
                localStorage.removeItem(this.storageKey);
                
                // Cố gắng gửi ngay
                if (this.fraudEventQueue.length > 0) {
                    console.log(`📤 Attempting to send ${this.fraudEventQueue.length} queued events`);
                    this.sendFraudEventBatch();
                }
            }
        } catch (error) {
            console.error('❌ Error restoring offline logs:', error);
        }
    }

    /**
     * Lưu trữ events vào localStorage cho offline scenarios
     * @param {Array} events - Array of events to store
     */
    saveToLocalStorage(events) {
        try {
            const existing = localStorage.getItem(this.storageKey);
            let allEvents = existing ? JSON.parse(existing) : [];
            allEvents.push(...events);
            
            // Giới hạn 100 sự kiện tối đa
            if (allEvents.length > 100) {
                allEvents = allEvents.slice(-100);
            }
            
            localStorage.setItem(this.storageKey, JSON.stringify(allEvents));
            console.log(`💾 Saved ${events.length} events to localStorage (total: ${allEvents.length})`);
        } catch (error) {
            console.error('❌ Error saving to localStorage:', error);
        }
    }

    /**
     * Gửi batch of fraud events với retry logic
     */
    async sendFraudEventBatch() {
        // Clear existing timer
        if (this.batchTimer) {
            clearTimeout(this.batchTimer);
            this.batchTimer = null;
        }

        // Nếu queue rỗng, không gửi
        if (this.fraudEventQueue.length === 0) {
            return;
        }

        // Lấy batch events (tối đa batchSize)
        const batchEvents = this.fraudEventQueue.splice(0, this.batchSize);
        console.log(`📤 Sending batch of ${batchEvents.length} fraud events...`);

        // Thử gửi với retry
        const success = await this.sendWithRetry(batchEvents, 0);

        if (!success) {
            // Nếu thất bại, lưu vào localStorage
            console.log(`💾 Batch failed, saving to offline storage...`);
            this.saveToLocalStorage(batchEvents);
        }

        // Nếu vẫn còn events trong queue, schedule gửi tiếp
        if (this.fraudEventQueue.length > 0) {
            console.log(`⏳ Scheduling next batch (${this.fraudEventQueue.length} events remaining)...`);
            this.batchTimer = setTimeout(() => {
                this.sendFraudEventBatch();
            }, this.batchTimeout);
        }
    }

    /**
     * Gửi batch events với retry logic và exponential backoff
     * @param {Array} events - Events to send
     * @param {Integer} retryCount - Current retry attempt
     * @returns {Promise<Boolean>} - True if success, false if all retries failed
     */
    async sendWithRetry(events, retryCount = 0) {
        try {
            const response = await fetch(`/api/exams/submissions/${this.submissionId}/log-fraud-batch`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    fraudEvents: events,
                    batchTimestamp: new Date().toISOString()
                })
            });

            if (response.ok) {
                console.log(`✅ Successfully sent batch of ${events.length} fraud events`);
                return true;
            } else if (response.status >= 500) {
                // Server error - retry
                throw new Error(`Server error: ${response.status}`);
            } else {
                // Client error - don't retry
                console.error(`❌ Client error ${response.status}: ${response.statusText}`);
                return false;
            }
        } catch (error) {
            console.error(`⚠️ Error sending batch (attempt ${retryCount + 1}/${this.maxRetries}):`, error);

            // Retry if we haven't reached max retries
            if (retryCount < this.maxRetries) {
                // Exponential backoff: 2s, 4s, 8s
                const delay = this.retryDelay * Math.pow(2, retryCount);
                console.log(`⏳ Retrying in ${delay}ms...`);
                
                await new Promise(resolve => setTimeout(resolve, delay));
                return this.sendWithRetry(events, retryCount + 1);
            } else {
                console.error(`❌ Max retries (${this.maxRetries}) reached, batch will be saved offline`);
                return false;
            }
        }
    }

    /**
     * Disable right-click, inspect, view source
     * Thêm layer bảo vệ bổ sung
     */
    enableStrictMode() {
        // Disable inspect element
        document.addEventListener('keydown', function(e) {
            if (e.ctrlKey && e.shiftKey && e.keyCode == 67) {
                e.returnValue = false;
            }
        }, false);

        // Disable F11 (fullscreen browser)
        document.addEventListener('keydown', (e) => {
            if (e.key === 'F11') {
                e.preventDefault();
            }
        });

        // Disable Ctrl+U (view source)
        document.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.keyCode === 85) {
                e.preventDefault();
                this.recordFraudEvent('VIEW_SOURCE', 'Cố gắng xem source code');
                this.showWarning('⚠️ Xem source code không được phép');
            }
        });

        console.log('✓ Strict anti-fraud mode activated');
    }
}

// Export để sử dụng trong modules khác
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ExamAntiFraudManager;
}
