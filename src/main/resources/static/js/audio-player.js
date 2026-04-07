/**
 * Enhanced Audio Player with Controls
 * Features: Speed control, Loop, Download, Transcription toggle
 */

class EnhancedAudioPlayer {
    constructor(audioElement) {
        // audioElement should be the actual <audio> tag
        this.audio = audioElement;
        this.container = audioElement.parentElement;
        this.setupControls();
    }

    /**
     * Setup the enhanced controls
     */
    setupControls() {
        // Create controls container
        const controlsContainer = document.createElement('div');
        controlsContainer.className = 'audio-player-controls mt-4 space-y-3';
        controlsContainer.innerHTML = `
            <!-- Speed Control -->
            <div class="flex items-center gap-2 flex-wrap">
                <span class="text-sm font-medium text-slate-600">Tốc độ:</span>
                <div class="flex gap-1">
                    <button class="speed-btn px-3 py-1 rounded-lg border border-slate-300 hover:bg-blue-50 text-sm transition" data-speed="0.75">0.75x</button>
                    <button class="speed-btn px-3 py-1 rounded-lg border border-slate-300 hover:bg-blue-50 text-sm transition active bg-blue-500 text-white" data-speed="1" style="background-color: #3b82f6; color: white;">1x</button>
                    <button class="speed-btn px-3 py-1 rounded-lg border border-slate-300 hover:bg-blue-50 text-sm transition" data-speed="1.25">1.25x</button>
                    <button class="speed-btn px-3 py-1 rounded-lg border border-slate-300 hover:bg-blue-50 text-sm transition" data-speed="1.5">1.5x</button>
                    <button class="speed-btn px-3 py-1 rounded-lg border border-slate-300 hover:bg-blue-50 text-sm transition" data-speed="2">2x</button>
                </div>
            </div>

            <!-- Loop Control -->
            <div class="flex items-center gap-3">
                <label class="flex items-center gap-2 cursor-pointer">
                    <input type="checkbox" id="loop-audio" class="w-4 h-4 rounded">
                    <span class="text-sm font-medium text-slate-600">Lặp lại liên tục</span>
                </label>
            </div>

            <!-- Download Button -->
            <div class="flex gap-2">
                <button id="download-audio" class="flex items-center gap-2 px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg transition font-medium text-sm">
                    <iconify-icon icon="mdi:download"></iconify-icon>
                    Tải audio xuống
                </button>
            </div>

            <!-- Transcription Toggle (if available) -->
            <div id="transcription-section" class="hidden">
                <button id="toggle-transcription" class="flex items-center gap-2 px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg transition font-medium text-sm">
                    <iconify-icon icon="mdi:file-document-outline"></iconify-icon>
                    Hiển thị nội dung
                </button>
                <div id="transcription-content" class="hidden mt-3 p-4 bg-slate-50 border border-slate-200 rounded-lg max-h-52 overflow-y-auto text-sm text-slate-700 leading-relaxed"></div>
            </div>
        `;

        this.container.appendChild(controlsContainer);

        // Setup event listeners
        this.setupSpeedControl();
        this.setupLoopControl();
        this.setupDownload();
        this.setupTranscription();
    }

    /**
     * Setup speed control buttons
     */
    setupSpeedControl() {
        const speedButtons = this.container.querySelectorAll('.speed-btn');

        speedButtons.forEach(button => {
            button.addEventListener('click', () => {
                const speed = parseFloat(button.getAttribute('data-speed'));

                // Update audio playback rate
                this.audio.playbackRate = speed;

                // Update button states
                speedButtons.forEach(btn => {
                    btn.classList.remove('active');
                    btn.style.backgroundColor = '';
                    btn.style.color = '';
                });

                button.classList.add('active');
                button.style.backgroundColor = '#3b82f6';
                button.style.color = 'white';

                // Show notification
                this.showNotification(`Tốc độ: ${speed}x`);
            });
        });
    }

    /**
     * Setup loop control
     */
    setupLoopControl() {
        const loopCheckbox = this.container.querySelector('#loop-audio');

        loopCheckbox.addEventListener('change', () => {
            if (loopCheckbox.checked) {
                this.audio.loop = true;
                this.showNotification('Bật lặp lại');
            } else {
                this.audio.loop = false;
                this.showNotification('Tắt lặp lại');
            }
        });
    }

    /**
     * Setup download button
     */
    setupDownload() {
        const downloadBtn = this.container.querySelector('#download-audio');
        const audioSrc = this.audio.src;

        downloadBtn.addEventListener('click', () => {
            if (!audioSrc) {
                this.showNotification('Không có audio để tải', 'warning');
                return;
            }

            // Create download link
            const link = document.createElement('a');
            link.href = audioSrc;
            link.download = `lesson-audio-${Date.now()}.mp3`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            this.showNotification('Đang tải audio...');
        });
    }

    /**
     * Setup transcription toggle
     */
    setupTranscription() {
        const transcriptionContent = this.container.getAttribute('data-transcription');

        if (transcriptionContent) {
            const transcriptionSection = this.container.querySelector('#transcription-section');
            const toggleBtn = this.container.querySelector('#toggle-transcription');
            const content = this.container.querySelector('#transcription-content');

            transcriptionSection.classList.remove('hidden');

            // Set content (it's HTML-encoded from server)
            content.innerHTML =transcriptionContent;

            toggleBtn.addEventListener('click', () => {
                if (content.classList.contains('hidden')) {
                    content.classList.remove('hidden');
                    toggleBtn.innerHTML = `
                        <iconify-icon icon="mdi:file-document"></iconify-icon>
                        Ẩn nội dung
                    `;
                } else {
                    content.classList.add('hidden');
                    toggleBtn.innerHTML = `
                        <iconify-icon icon="mdi:file-document-outline"></iconify-icon>
                        Hiển thị nội dung
                    `;
                }
            });
        }
    }

    /**
     * Show notification
     */
    showNotification(message, type = 'info') {
        const toast = document.createElement('div');
        const bgColor = type === 'warning' ? 'bg-yellow-500' : 'bg-blue-500';
        toast.className = `fixed bottom-4 left-4 ${bgColor} text-white px-4 py-2 rounded-lg shadow-lg text-sm z-40`;
        toast.textContent = message;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('fade-out');
            setTimeout(() => toast.remove(), 300);
        }, 2000);
    }
}

/**
 * Initialize all audio players on page load
 */
document.addEventListener('DOMContentLoaded', () => {
    const audioElements = document.querySelectorAll('audio[data-enhanced-player="true"]');

    audioElements.forEach(audio => {
        // Pass the audio element itself, not its parent
        new EnhancedAudioPlayer(audio);
    });
});

// Helper function to get audio element
function getAudioPlayer(selector = 'audio') {
    return document.querySelector(selector);
}

// Helper function to play audio programmatically
function playAudio(selector = 'audio') {
    const audio = getAudioPlayer(selector);
    if (audio) {
        audio.play();
    }
}

// Helper function to pause audio
function pauseAudio(selector = 'audio') {
    const audio = getAudioPlayer(selector);
    if (audio) {
        audio.pause();
    }
}

// Helper function to set playback rate
function setPlaybackRate(rate, selector = 'audio') {
    const audio = getAudioPlayer(selector);
    if (audio) {
        audio.playbackRate = rate;
    }
}
