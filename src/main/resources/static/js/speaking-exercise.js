/**
 * ========== SPEAKING EXERCISE - WEB SPEECH API ==========
 * Hỗ trợ ghi âm, trích xuất text, và nộp bài tập nói
 * 
 * Phụ thuộc:
 * - Web Speech API (hỗ trợ bởi hầu hết trình duyệt hiện đại)
 * - Fetch API
 * - Cloudinary (backend xử lý upload)
 */

class SpeakingExerciseManager {
    constructor(exerciseId) {
        this.exerciseId = exerciseId;
        this.isRecording = false;
        this.mediaRecorder = null;
        this.audioChunks = [];
        this.scriptRecognition = null;
        this.transcript = '';
        this.recordingStartTime = null;
        this.timerInterval = null;

        // Browser compatibility check
        this.initBrowserSupport();
        this.setupEventListeners();
    }

    /**
     * Kiểm tra hỗ trợ trình duyệt cho Web Speech API
     */
    initBrowserSupport() {
        // Kiểm tra Web Speech API
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRecognition) {
            console.error('Web Speech API không được hỗ trợ trên trình duyệt này');
            this.showError('Trình duyệt của bạn không hỗ trợ ghi âm. Vui lòng sử dụng Chrome, Edge hoặc Firefox.');
            return false;
        }

        this.scriptRecognition = new SpeechRecognition();
        this.scriptRecognition.lang = 'en-US'; // English
        this.scriptRecognition.continuous = true;
        this.scriptRecognition.interimResults = true;

        // Khi nhận được kết quả
        this.scriptRecognition.onresult = (event) => this.handleRecognitionResult(event);
        this.scriptRecognition.onerror = (event) => this.handleRecognitionError(event);
        this.scriptRecognition.onend = () => this.handleRecognitionEnd();

        return true;
    }

    /**
     * Setup event listeners cho UI buttons
     */
    setupEventListeners() {
        const startBtn = document.getElementById('startRecordingBtn');
        const stopBtn = document.getElementById('stopRecordingBtn');
        const submitBtn = document.getElementById('submitSpeakingBtn');
        const retryBtn = document.getElementById('retrySpeakingBtn');

        if (startBtn) {
            startBtn.addEventListener('click', () => this.startRecording());
        }
        if (stopBtn) {
            stopBtn.addEventListener('click', () => this.stopRecording());
        }
        if (submitBtn) {
            submitBtn.addEventListener('click', () => this.submitExercise());
        }
        if (retryBtn) {
            retryBtn.addEventListener('click', () => this.resetRecording());
        }
    }

    /**
     * Bắt đầu ghi âm + nhận dạng giọng nói
     */
    async startRecording() {
        try {
            this.audioChunks = [];
            this.transcript = '';
            this.recordingStartTime = Date.now();

            // Yêu cầu quyền truy cập microphone
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

            // Setup MediaRecorder để ghi audio
            this.mediaRecorder = new MediaRecorder(stream);

            this.mediaRecorder.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    this.audioChunks.push(event.data);
                }
            };

            this.mediaRecorder.onstart = () => {
                this.isRecording = true;
                this.updateUIState('recording');
                console.log('🎤 Bắt đầu ghi âm...');
            };

            this.mediaRecorder.onstop = () => {
                this.isRecording = false;
                this.createAudioBlob();
            };

            // Bắt đầu ghi âm
            this.mediaRecorder.start();

            // Bắt đầu nhận dạng giọng nói
            if (this.scriptRecognition) {
                this.scriptRecognition.start();
            }

            // Bắt đầu timer
            this.startTimer();

            this.showSuccess('Đang ghi âm... hãy nói tiếng Anh');

        } catch (error) {
            console.error('Lỗi khi truy cập microphone:', error);
            this.showError('Không thể truy cập microphone. Vui lòng kiểm tra quyền truy cập.');
        }
    }

    /**
     * Dừng ghi âm
     */
    stopRecording() {
        if (!this.isRecording) return;

        try {
            // Dừng ghi âm
            if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
                this.mediaRecorder.stop();
            }

            // Dừng nhận dạng giọng nói
            if (this.scriptRecognition) {
                this.scriptRecognition.stop();
            }

            // Dừng timer
            this.stopTimer();

            // Dừng stream microphone
            if (this.mediaRecorder && this.mediaRecorder.stream) {
                this.mediaRecorder.stream.getTracks().forEach(track => track.stop());
            }

            console.log('⏹️ Đã dừng ghi âm');
            console.log('📝 Transcript:', this.transcript);

        } catch (error) {
            console.error('Lỗi khi dừng ghi âm:', error);
        }
    }

    /**
     * Xử lý kết quả nhận dạng giọng nói
     */
    handleRecognitionResult(event) {
        let interimTranscript = '';

        for (let i = event.resultIndex; i < event.results.length; i++) {
            const transcript = event.results[i][0].transcript;

            if (event.results[i].isFinal) {
                this.transcript += transcript + ' ';
            } else {
                interimTranscript += transcript;
            }
        }

        // Cập nhật UI với transcript hiện tại
        const transcriptDisplay = document.getElementById('transcriptDisplay');
        if (transcriptDisplay) {
            transcriptDisplay.innerHTML = `
                <div class="final-transcript">${this.transcript}</div>
                <div class="interim-transcript text-muted">${interimTranscript}</div>
            `;
        }

        console.log('Final:', this.transcript);
        console.log('Interim:', interimTranscript);
    }

    /**
     * Xử lý lỗi nhận dạng giọng nói
     */
    handleRecognitionError(event) {
        console.error('Speech Recognition Error:', event.error);

        const errorMessages = {
            'network': 'Lỗi kết nối mạng. Vui lòng kiểm tra internet.',
            'audio-capture': 'Không thể ghi âm. Vui lòng kiểm tra microphone.',
            'not-allowed': 'Bạn chưa cho phép truy cập microphone.',
            'no-speech': 'Không phát hiện tiếng nói. Vui lòng thử lại.'
        };

        const message = errorMessages[event.error] || `Lỗi: ${event.error}`;
        this.showError(message);
    }

    /**
     * Xử lý khi nhận dạng giọng nói kết thúc
     */
    handleRecognitionEnd() {
        console.log('✅ Nhận dạng giọng nói kết thúc');
        this.updateUIState('stopped');
    }

    /**
     * Tạo Blob từ audio chunks
     */
    createAudioBlob() {
        if (this.audioChunks.length === 0) {
            this.showError('Không có dữ liệu audio. Vui lòng thử lại.');
            return;
        }

        const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
        this.audioBlob = audioBlob;

        console.log('🎵 Audio blob created, size:', audioBlob.size, 'bytes');

        // Tạo preview audio player
        const audioUrl = URL.createObjectURL(audioBlob);
        const audioPreview = document.getElementById('audioPreview');
        if (audioPreview) {
            audioPreview.src = audioUrl;
            audioPreview.style.display = 'block';
        }

        this.updateUIState('recorded');
    }

    /**
     * Nộp bài tập nói
     */
    async submitExercise() {
        try {
            // Kiểm tra dữ liệu
            if (!this.audioBlob) {
                this.showError('Vui lòng ghi âm trước khi nộp bài.');
                return;
            }

            if (!this.transcript.trim()) {
                this.showError('Không có transcript. Bạn có thực sự nói không?');
                return;
            }

            // Tắt nút submit
            const submitBtn = document.getElementById('submitSpeakingBtn');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang nộp bài...';
            }

            // Prepare FormData
            const formData = new FormData();
            formData.append('audio', this.audioBlob, 'speaking-exercise.webm');
            formData.append('transcript', this.transcript.trim());

            // Gửi tới backend
            const response = await fetch(
                `/api/speaking/exercises/${this.exerciseId}/submit`,
                {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Authorization': `Bearer ${this.getAuthToken()}`
                    }
                }
            );

            const result = await response.json();

            if (response.ok && result.data) {
                console.log('✅ Submission successful:', result.data);
                this.showSuccess('Nộp bài thành công!');
                this.displayResult(result.data);
            } else {
                console.error('Submission failed:', result);
                this.showError(result.message || 'Nộp bài thất bại. Vui lòng thử lại.');
            }

        } catch (error) {
            console.error('Error submitting exercise:', error);
            this.showError('Lỗi: ' + error.message);
        } finally {
            const submitBtn = document.getElementById('submitSpeakingBtn');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = 'Nộp bài';
            }
        }
    }

    /**
     * Hiển thị kết quả chấm điểm
     */
    displayResult(data) {
        const resultContainer = document.getElementById('speakingResult');
        if (!resultContainer) return;

        const statusIcon = data.passed ? '✅' : '⚠️';
        const statusColor = data.passed ? 'success' : 'warning';

        resultContainer.innerHTML = `
            <div class="card border-${statusColor}">
                <div class="card-header bg-${statusColor} text-white">
                    <h5>${statusIcon} Kết quả chấm điểm</h5>
                </div>
                <div class="card-body">
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <h6>Điểm số</h6>
                            <div class="fs-4 fw-bold text-primary">
                                ${data.score} / ${data.maxScore}
                            </div>
                        </div>
                        <div class="col-md-6">
                            <h6>Độ chính xác</h6>
                            <div class="fs-4 fw-bold text-info">
                                ${data.accuracy.toFixed(1)}%
                            </div>
                            <div class="progress" style="height: 20px;">
                                <div class="progress-bar" role="progressbar" 
                                     style="width: ${data.accuracy}%" 
                                     aria-valuenow="${data.accuracy}" 
                                     aria-valuemin="0" 
                                     aria-valuemax="100"></div>
                            </div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <h6>Nhận xét</h6>
                        <p class="text-muted">${data.feedback}</p>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <h6>Đáp án chuẩn</h6>
                            <div class="alert alert-info">
                                <code>${this.escapeHtml(data.correctAnswer)}</code>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <h6>Bạn nói</h6>
                            <div class="alert alert-light border">
                                <code>${this.escapeHtml(data.userTranscript)}</code>
                            </div>
                        </div>
                    </div>

                    <div class="mt-3">
                        <h6>Audio ghi âm</h6>
                        <audio controls style="width: 100%;">
                            <source src="${data.audioUrl}" type="audio/webm">
                            Trình duyệt của bạn không hỗ trợ phát audio.
                        </audio>
                    </div>

                    <div class="mt-3">
                        <button class="btn btn-primary" id="retrySpeakingBtn">
                            🔄 Làm lại
                        </button>
                        <a href="/dashboard/student/lessons" class="btn btn-secondary ms-2">
                            📚 Về bài học
                        </a>
                    </div>
                </div>
            </div>
        `;

        this.updateUIState('submitted');
        
        // Attach retry listener
        const retryBtn = resultContainer.querySelector('#retrySpeakingBtn');
        if (retryBtn) {
            retryBtn.addEventListener('click', () => this.resetRecording());
        }
    }

    /**
     * Reset recording - quay lại trạng thái ban đầu
     */
    resetRecording() {
        this.audioChunks = [];
        this.transcript = '';
        this.audioBlob = null;
        this.recordingStartTime = null;

        // Clear displays
        const transcriptDisplay = document.getElementById('transcriptDisplay');
        if (transcriptDisplay) {
            transcriptDisplay.innerHTML = '';
        }

        const audioPreview = document.getElementById('audioPreview');
        if (audioPreview) {
            audioPreview.style.display = 'none';
            audioPreview.src = '';
        }

        const resultContainer = document.getElementById('speakingResult');
        if (resultContainer) {
            resultContainer.innerHTML = '';
        }

        this.updateUIState('idle');
        this.showSuccess('Đã reset. Bạn có thể bắt đầu ghi âm lại.');
    }

    /**
     * Bắt đầu đếm thời gian
     */
    startTimer() {
        let seconds = 0;
        const timerDisplay = document.getElementById('recordingTimer');

        this.timerInterval = setInterval(() => {
            seconds++;
            if (timerDisplay) {
                const mins = Math.floor(seconds / 60);
                const secs = seconds % 60;
                timerDisplay.textContent = `⏱️ ${mins}:${secs.toString().padStart(2, '0')}`;
            }
        }, 1000);
    }

    /**
     * Dừng đếm thời gian
     */
    stopTimer() {
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
            this.timerInterval = null;
        }
    }

    /**
     * Cập nhật trạng thái UI
     */
    updateUIState(state) {
        const startBtn = document.getElementById('startRecordingBtn');
        const stopBtn = document.getElementById('stopRecordingBtn');
        const submitBtn = document.getElementById('submitSpeakingBtn');
        const recordingContainer = document.getElementById('recordingContainer');

        switch (state) {
            case 'recording':
                if (startBtn) startBtn.disabled = true;
                if (stopBtn) stopBtn.disabled = false;
                if (submitBtn) submitBtn.disabled = true;
                if (recordingContainer) recordingContainer.classList.add('recording');
                break;

            case 'stopped':
            case 'recorded':
                if (startBtn) startBtn.disabled = false;
                if (stopBtn) stopBtn.disabled = true;
                if (submitBtn) submitBtn.disabled = false;
                if (recordingContainer) recordingContainer.classList.remove('recording');
                break;

            case 'submitted':
                if (startBtn) startBtn.disabled = true;
                if (stopBtn) stopBtn.disabled = true;
                if (submitBtn) submitBtn.disabled = true;
                break;

            case 'idle':
            default:
                if (startBtn) startBtn.disabled = false;
                if (stopBtn) stopBtn.disabled = true;
                if (submitBtn) submitBtn.disabled = true;
                if (recordingContainer) recordingContainer.classList.remove('recording');
                break;
        }
    }

    /**
     * Hiển thị thông báo lỗi
     */
    showError(message) {
        const alertContainer = document.getElementById('alertContainer');
        if (!alertContainer) {
            console.error('Alert container not found');
            return;
        }

        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <strong>❌ Lỗi:</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;

        alertContainer.insertAdjacentHTML('beforeend', alertHtml);

        // Auto-dismiss sau 5 giây
        setTimeout(() => {
            const alert = alertContainer.querySelector('.alert-danger');
            if (alert) {
                alert.remove();
            }
        }, 5000);
    }

    /**
     * Hiển thị thông báo thành công
     */
    showSuccess(message) {
        const alertContainer = document.getElementById('alertContainer');
        if (!alertContainer) {
            console.log('Success:', message);
            return;
        }

        const alertHtml = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <strong>✅ Thành công:</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;

        alertContainer.insertAdjacentHTML('beforeend', alertHtml);

        // Auto-dismiss sau 3 giây
        setTimeout(() => {
            const alert = alertContainer.querySelector('.alert-success');
            if (alert) {
                alert.remove();
            }
        }, 3000);
    }

    /**
     * Lấy auth token từ localStorage hoặc cookie
     */
    getAuthToken() {
        // Thử lấy từ localStorage
        const token = localStorage.getItem('token');
        if (token) return token;

        // Thử lấy từ cookie
        const name = 'token=';
        const decodedCookie = decodeURIComponent(document.cookie);
        const parts = decodedCookie.split(';');

        for (let part of parts) {
            part = part.trim();
            if (part.indexOf(name) === 0) {
                return part.substring(name.length, part.length);
            }
        }

        return '';
    }

    /**
     * Escape HTML để tránh XSS
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// ========== INITIALIZATION ==========
// Khởi tạo khi DOM ready
document.addEventListener('DOMContentLoaded', () => {
    const exerciseId = document.getElementById('exerciseId')?.value;
    if (exerciseId) {
        window.speakingManager = new SpeakingExerciseManager(exerciseId);
        console.log('✅ Speaking Exercise Manager initialized');
    }
});
