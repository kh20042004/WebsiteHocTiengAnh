/**
 * student-exam.js
 * Module điều khiển giao diện làm bài thi cho học sinh.
 * Xử lý: hiển thị câu hỏi, đồng hồ đếm ngược, lưu câu trả lời, nộp bài.
 */
const ExamApp = (() => {

    // Chỉ số câu hỏi đang hiển thị (bắt đầu từ 0)
    let currentIndex = 0;

    // Mảng lưu câu trả lời của học sinh: index → string
    let answers = {};

    // Biến đếm giây còn lại (null = không giới hạn)
    let remainingSeconds = null;

    // setInterval ID của đồng hồ đếm ngược
    let timerInterval = null;

    // Thời điểm bắt đầu làm bài (để tính timeTakenSeconds khi nộp)
    const startTime = Date.now();

    // Dữ liệu đề thi và câu hỏi từ server (inject bởi Thymeleaf)
    const examData      = window.EXAM_DATA      || {};
    const examQuestions = window.EXAM_QUESTIONS || [];

    // ======================================================================
    // Khởi tạo khi trang tải xong
    // ======================================================================

    /**
     * Khởi tạo giao diện làm bài:
     * 1. Lấy submissionId từ sessionStorage
     * 2. Render bảng điều hướng câu hỏi
     * 3. Hiển thị câu hỏi đầu tiên
     * 4. Bắt đầu đồng hồ đếm ngược (nếu có giới hạn thời gian)
     */
    function init() {
        // Lấy submissionId đã lưu khi vào thi từ trang student/exams.html
        examData.submissionId = sessionStorage.getItem('submissionId_' + examData.examId);
        if (!examData.submissionId) {
            console.warn('Không tìm thấy submissionId trong sessionStorage. Sẽ dùng submissionId từ URL nếu có.');
        }

        // Render bảng điều hướng câu hỏi
        renderNavigationGrid();

        // Hiển thị câu hỏi đầu tiên (nếu có)
        if (examQuestions.length > 0) {
            showQuestion(0);
        } else {
            document.getElementById('questionContainer').innerHTML = `
                <div class="bg-white rounded-xl border border-slate-200 p-12 text-center text-slate-400">
                    <iconify-icon icon="solar:diploma-linear" width="40" class="mb-3 opacity-40"></iconify-icon>
                    <p>Đề thi không có câu hỏi nào.</p>
                </div>`;
        }

        // Bắt đầu đồng hồ nếu có giới hạn thời gian
        if (examData.timeLimitMinutes && examData.timeLimitMinutes > 0) {
            remainingSeconds = examData.timeLimitMinutes * 60;
            startCountdown();
        }
    }

    // ======================================================================
    // Đồng hồ đếm ngược
    // ======================================================================

    /**
     * Bắt đầu đồng hồ đếm ngược.
     * Cập nhật display mỗi giây, đổi màu đỏ khi còn < 60 giây.
     * Tự động nộp bài khi hết giờ.
     */
    function startCountdown() {
        updateTimerDisplay();
        timerInterval = setInterval(() => {
            remainingSeconds--;
            updateTimerDisplay();

            // Cảnh báo còn 1 phút: đổi màu đỏ
            if (remainingSeconds === 60) {
                const timerBlock = document.getElementById('timerBlock');
                if (timerBlock) {
                    timerBlock.classList.remove('bg-slate-100');
                    timerBlock.classList.add('bg-red-100');
                }
                const timerDisplay = document.getElementById('timerDisplay');
                if (timerDisplay) timerDisplay.classList.add('text-red-600');
            }

            // Hết giờ: tự động nộp bài
            if (remainingSeconds <= 0) {
                clearInterval(timerInterval);
                alert('⏰ Hết giờ! Bài làm sẽ được nộp tự động.');
                submitExam(true); // true = auto-submit
            }
        }, 1000);
    }

    /**
     * Cập nhật nội dung hiển thị đồng hồ theo định dạng MM:SS
     */
    function updateTimerDisplay() {
        const display = document.getElementById('timerDisplay');
        if (!display || remainingSeconds === null) return;
        const mins = Math.floor(remainingSeconds / 60);
        const secs = remainingSeconds % 60;
        display.textContent = String(mins).padStart(2, '0') + ':' + String(secs).padStart(2, '0');
    }

    // ======================================================================
    // Render câu hỏi và điều hướng
    // ======================================================================

    /**
     * Render bảng lưới điều hướng câu hỏi bên trái màn hình
     * Mỗi nút hiển thị số thứ tự câu, được highlight khi đã trả lời / đang xem
     */
    function renderNavigationGrid() {
        const nav = document.getElementById('questionNav');
        if (!nav) return;
        nav.innerHTML = '';

        examQuestions.forEach((q, idx) => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.id = 'navBtn_' + idx;
            btn.textContent = idx + 1;
            btn.className = 'h-8 w-8 rounded text-xs font-medium transition-colors '
                + (idx === currentIndex
                    ? 'border-2 border-violet-500 bg-white text-violet-600'
                    : 'bg-slate-200 text-slate-600 hover:bg-slate-300');
            btn.onclick = () => showQuestion(idx);
            nav.appendChild(btn);
        });
    }

    /**
     * Hiển thị câu hỏi tại chỉ số idx
     * @param {number} idx - Chỉ số câu hỏi (0-based)
     */
    function showQuestion(idx) {
        if (idx < 0 || idx >= examQuestions.length) return;
        currentIndex = idx;

        const question = examQuestions[idx];
        const container = document.getElementById('questionContainer');
        if (!container) return;

        // Render HTML câu hỏi theo loại
        container.innerHTML = renderQuestionHtml(question, idx);

        // Khôi phục câu trả lời đã chọn trước đó (nếu có)
        restoreAnswer(question, idx);

        // Cập nhật thanh điều hướng
        updateNavigationHighlight();
        updateProgressText();
        updatePrevNextButtons();
    }

    /**
     * Sinh HTML cho một câu hỏi dựa trên loại (type)
     * @param {Object} question - Dữ liệu câu hỏi
     * @param {number} idx - Chỉ số câu hỏi
     * @returns {string} HTML string
     */
    function renderQuestionHtml(question, idx) {
        const isAnswered = answers[idx] !== undefined && answers[idx] !== '';
        const statusClass = isAnswered ? 'border-violet-200' : 'border-slate-200';

        let inputHtml = '';

        if (question.type === 'MULTIPLE_CHOICE' || question.type === 'TRUE_FALSE') {
            // Render các lựa chọn dạng radio button
            const options = question.options || [];
            inputHtml = `<div class="space-y-3" id="options_${idx}">
                ${options.map((opt, optIdx) => `
                    <label class="flex items-center gap-3 p-3 border border-slate-200 rounded-lg cursor-pointer hover:bg-violet-50 hover:border-violet-200 transition-colors has-[:checked]:bg-violet-50 has-[:checked]:border-violet-400">
                        <input type="radio" name="q_${idx}" value="${escapeAttr(opt)}"
                            class="w-4 h-4 text-violet-600"
                            onchange="ExamApp.saveAnswer(${idx}, this.value)">
                        <span class="text-sm text-slate-700">${escapeHtml(opt)}</span>
                    </label>
                `).join('')}
            </div>`;
        } else if (question.type === 'FILL_IN_BLANK') {
            // Render ô nhập văn bản
            inputHtml = `<div>
                <input type="text"
                    id="fillInput_${idx}"
                    placeholder="Nhập câu trả lời của bạn..."
                    class="w-full max-w-sm px-4 py-3 border-2 border-slate-300 rounded-xl text-sm focus:outline-none focus:border-violet-500 transition-colors"
                    oninput="ExamApp.saveAnswer(${idx}, this.value)">
            </div>`;
        } else {
            // Loại câu hỏi không xác định: fallback nhập text
            inputHtml = `<div>
                <input type="text"
                    id="fillInput_${idx}"
                    placeholder="Nhập câu trả lời..."
                    class="w-full max-w-sm px-4 py-3 border-2 border-slate-300 rounded-xl text-sm focus:outline-none focus:border-violet-500"
                    oninput="ExamApp.saveAnswer(${idx}, this.value)">
            </div>`;
        }

        return `
            <div class="bg-white rounded-xl border ${statusClass} p-6 transition-colors">
                <!-- Header câu hỏi -->
                <div class="flex items-center justify-between mb-5">
                    <span class="px-3 py-1 bg-violet-100 text-violet-700 rounded-full text-sm font-semibold">
                        Câu ${idx + 1} / ${examQuestions.length}
                    </span>
                    <span class="text-xs text-slate-400">${question.type || ''}</span>
                </div>
                <!-- Nội dung câu hỏi -->
                <p class="text-base font-medium text-slate-900 mb-6 leading-relaxed">
                    ${escapeHtml(question.questionText || '')}
                </p>
                <!-- Khu vực trả lời -->
                ${inputHtml}
            </div>`;
    }

    /**
     * Khôi phục câu trả lời đã chọn khi quay lại câu hỏi
     * @param {Object} question - Câu hỏi hiện tại
     * @param {number} idx - Chỉ số câu hỏi
     */
    function restoreAnswer(question, idx) {
        const savedAnswer = answers[idx];
        if (!savedAnswer && savedAnswer !== 0) return;

        if (question.type === 'MULTIPLE_CHOICE' || question.type === 'TRUE_FALSE') {
            // Tìm và tick radio button đã chọn trước đó
            const radios = document.querySelectorAll(`input[name="q_${idx}"]`);
            radios.forEach(radio => {
                if (radio.value === savedAnswer) radio.checked = true;
            });
        } else if (question.type === 'FILL_IN_BLANK') {
            const input = document.getElementById('fillInput_' + idx);
            if (input) input.value = savedAnswer;
        }
    }

    // ======================================================================
    // Lưu câu trả lời
    // ======================================================================

    /**
     * Lưu câu trả lời của học sinh cho câu hỏi tại chỉ số idx
     * Cập nhật màu nút điều hướng để phản ánh đã trả lời
     * @param {number} idx - Chỉ số câu hỏi
     * @param {string} value - Câu trả lời
     */
    function saveAnswer(idx, value) {
        answers[idx] = value;
        updateNavigationHighlight();
    }

    // ======================================================================
    // Cập nhật UI điều hướng
    // ======================================================================

    /** Cập nhật màu các nút trong lưới điều hướng câu hỏi */
    function updateNavigationHighlight() {
        examQuestions.forEach((_, idx) => {
            const btn = document.getElementById('navBtn_' + idx);
            if (!btn) return;
            btn.className = 'h-8 w-8 rounded text-xs font-medium transition-colors ';

            if (idx === currentIndex) {
                // Câu đang xem: viền tím
                btn.className += 'border-2 border-violet-500 bg-white text-violet-600';
            } else if (answers[idx] !== undefined && answers[idx] !== '') {
                // Đã trả lời: nền tím
                btn.className += 'bg-violet-600 text-white';
            } else {
                // Chưa trả lời: nền xám
                btn.className += 'bg-slate-200 text-slate-600 hover:bg-slate-300';
            }
        });
    }

    /** Cập nhật text "X / N" ở giữa 2 nút điều hướng */
    function updateProgressText() {
        const el = document.getElementById('questionProgress');
        if (el) el.textContent = (currentIndex + 1) + ' / ' + examQuestions.length;
    }

    /** Bật/tắt nút Trước / Sau dựa trên vị trí hiện tại */
    function updatePrevNextButtons() {
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');
        if (prevBtn) prevBtn.disabled = currentIndex === 0;
        if (nextBtn) nextBtn.disabled = currentIndex === examQuestions.length - 1;
    }

    // ======================================================================
    // Điều hướng câu hỏi
    // ======================================================================

    /** Chuyển đến câu trước */
    function navigatePrev() {
        if (currentIndex > 0) showQuestion(currentIndex - 1);
    }

    /** Chuyển đến câu sau */
    function navigateNext() {
        if (currentIndex < examQuestions.length - 1) showQuestion(currentIndex + 1);
    }

    // ======================================================================
    // Nộp bài
    // ======================================================================

    /**
     * Mở dialog xác nhận nộp bài với tóm tắt đã trả lời / chưa trả lời
     */
    function confirmSubmit() {
        const answeredCount = Object.values(answers).filter(v => v !== '' && v !== undefined && v !== null).length;
        const totalCount    = examQuestions.length;
        const unanswered    = totalCount - answeredCount;

        let summaryHtml = `<p>Đã trả lời: <strong>${answeredCount}/${totalCount}</strong> câu</p>`;
        if (unanswered > 0) {
            summaryHtml += `<p class="text-amber-600 mt-1">⚠️ Còn ${unanswered} câu chưa trả lời</p>`;
        }

        document.getElementById('submitSummary').innerHTML = summaryHtml;
        document.getElementById('submitDialog').classList.remove('hidden');
    }

    /** Đóng dialog xác nhận nộp bài */
    function closeSubmitDialog() {
        document.getElementById('submitDialog').classList.add('hidden');
    }

    /**
     * Gọi API nộp bài và chuyển đến trang kết quả
     * @param {boolean} isAutoSubmit - true = hết giờ tự động nộp
     */
    async function submitExam(isAutoSubmit = false) {
        // Vô hiệu hóa nút tránh double-submit
        const confirmBtn = document.getElementById('confirmSubmitBtn');
        if (confirmBtn) {
            confirmBtn.disabled = true;
            confirmBtn.textContent = 'Đang nộp bài...';
        }
        // Dừng đồng hồ
        if (timerInterval) clearInterval(timerInterval);

        // Tính thời gian làm bài tính bằng giây
        const timeTakenSeconds = Math.floor((Date.now() - startTime) / 1000);

        const submissionId = examData.submissionId;
        if (!submissionId) {
            alert('Lỗi: Không tìm thấy thông tin bài làm. Vui lòng liên hệ giáo viên.');
            return;
        }

        const payload = {
            answers,      // Map<Integer, String>: questionIndex → answer
            timeTakenSeconds
        };

        try {
            const token = localStorage.getItem('accessToken') || '';
            const res  = await fetch('/api/exams/submissions/' + submissionId + '/submit', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(payload)
            });
            const data = await res.json();

            if (!res.ok || data.code !== 200) {
                throw new Error(data.message || 'Nộp bài thất bại');
            }

            // Xóa submissionId khỏi sessionStorage sau khi nộp thành công
            sessionStorage.removeItem('submissionId_' + examData.examId);

            // Chuyển đến trang kết quả
            window.location.href = '/dashboard/student/exam/' + examData.examId + '/result';

        } catch (err) {
            alert('Lỗi khi nộp bài: ' + err.message);
            if (confirmBtn) {
                confirmBtn.disabled = false;
                confirmBtn.textContent = 'Nộp bài';
            }
        }
    }

    // ======================================================================
    // Hàm tiện ích
    // ======================================================================

    /**
     * Escape HTML để tránh XSS khi inject nội dung người dùng vào innerHTML
     * @param {string} str
     */
    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    /**
     * Escape chuỗi để dùng an toàn trong attribute HTML
     * @param {string} str
     */
    function escapeAttr(str) {
        if (!str) return '';
        return str.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    // ======================================================================
    // Chạy init khi DOM đã sẵn sàng
    // ======================================================================
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose các hàm public
    return {
        saveAnswer,
        navigatePrev,
        navigateNext,
        confirmSubmit,
        closeSubmitDialog,
        submitExam
    };

})();
