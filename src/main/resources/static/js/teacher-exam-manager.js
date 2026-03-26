/**
 * teacher-exam-manager.js
 * Module quản lý giao diện tạo đề thi cho giáo viên.
 * Xử lý: chuyển tab, tải câu hỏi từ ngân hàng, thêm câu hỏi tự soạn, submit form.
 */
const ExamManager = (() => {

    // Danh sách câu hỏi tự soạn (sẽ gửi lên API khi submit)
    const customQuestions = [];

    // Danh sách exerciseIds được chọn từ ngân hàng
    const selectedExerciseIds = new Set();

    // ======================================================================
    // Chuyển tab: Ngân hàng / Tự soạn
    // ======================================================================

    /**
     * Chuyển đổi giữa tab "Chọn từ ngân hàng" và "Câu hỏi tự soạn"
     * @param {string} tab - 'bank' hoặc 'custom'
     */
    function switchTab(tab) {
        const tabBank   = document.getElementById('tabBank');
        const tabCustom = document.getElementById('tabCustom');
        const panelBank   = document.getElementById('panelBank');
        const panelCustom = document.getElementById('panelCustom');

        if (tab === 'bank') {
            // Highlight tab ngân hàng
            tabBank.classList.add('bg-white', 'text-slate-900', 'shadow-sm');
            tabBank.classList.remove('text-slate-500', 'hover:text-slate-700');
            tabCustom.classList.remove('bg-white', 'text-slate-900', 'shadow-sm');
            tabCustom.classList.add('text-slate-500', 'hover:text-slate-700');
            panelBank.classList.remove('hidden');
            panelCustom.classList.add('hidden');
        } else {
            // Highlight tab tự soạn
            tabCustom.classList.add('bg-white', 'text-slate-900', 'shadow-sm');
            tabCustom.classList.remove('text-slate-500', 'hover:text-slate-700');
            tabBank.classList.remove('bg-white', 'text-slate-900', 'shadow-sm');
            tabBank.classList.add('text-slate-500', 'hover:text-slate-700');
            panelCustom.classList.remove('hidden');
            panelBank.classList.add('hidden');
        }
    }

    // ======================================================================
    // Ngân hàng câu hỏi: tải lesson và exercise theo unit đã chọn
    // ======================================================================

    /**
     * Tải danh sách bài học (Lesson) theo Unit được chọn
     * Gọi API: GET /api/units/{unitId}/lessons
     * @param {string} unitId - ID của Unit đã chọn
     */
    async function loadLessons(unitId) {
        const lessonSelect = document.getElementById('lessonSelect');
        lessonSelect.disabled = true;
        lessonSelect.innerHTML = '<option value="">Đang tải...</option>';

        // Reset danh sách exercise khi đổi unit
        document.getElementById('exerciseList').innerHTML = `
            <p class="text-sm text-slate-400 text-center py-8">
                <iconify-icon icon="solar:library-linear" width="32" class="inline mb-2 opacity-40 block mx-auto"></iconify-icon>
                Chọn bài học để xem các exercise
            </p>`;

        if (!unitId) {
            lessonSelect.innerHTML = '<option value="">-- Chọn bài học --</option>';
            return;
        }

        try {
            const res  = await fetch('/api/units/' + unitId + '/lessons');
            const data = await res.json();
            const lessons = data.data || [];

            lessonSelect.innerHTML = '<option value="">-- Chọn bài học --</option>';
            lessons.forEach(lesson => {
                const opt = document.createElement('option');
                opt.value   = lesson.id;
                opt.textContent = lesson.title + (lesson.type ? ' [' + lesson.type + ']' : '');
                lessonSelect.appendChild(opt);
            });
            lessonSelect.disabled = false;
        } catch (err) {
            lessonSelect.innerHTML = '<option value="">Lỗi khi tải dữ liệu</option>';
            console.error('Lỗi khi tải danh sách bài học:', err);
        }
    }

    /**
     * Tải danh sách Exercise theo Lesson được chọn
     * Gọi API: GET /api/lessons/{lessonId}/exercises
     * @param {string} lessonId - ID của Lesson đã chọn
     */
    async function loadExercises(lessonId) {
        const exerciseList = document.getElementById('exerciseList');
        exerciseList.innerHTML = '<p class="text-sm text-slate-400 text-center py-4">Đang tải...</p>';

        if (!lessonId) {
            exerciseList.innerHTML = `
                <p class="text-sm text-slate-400 text-center py-8">
                    Chọn bài học để xem các exercise
                </p>`;
            return;
        }

        try {
            const res  = await fetch('/api/lessons/' + lessonId + '/exercises');
            const data = await res.json();
            const exercises = data.data || [];

            if (exercises.length === 0) {
                exerciseList.innerHTML = '<p class="text-sm text-slate-400 text-center py-8">Bài học này chưa có exercise nào.</p>';
                return;
            }

            // Render từng exercise với checkbox chọn
            exerciseList.innerHTML = exercises.map(ex => `
                <div class="border border-slate-200 rounded-lg p-4 hover:border-violet-300 transition-colors bg-white">
                    <label class="flex items-start gap-3 cursor-pointer">
                        <input type="checkbox" value="${ex.id}"
                            class="mt-1 exercise-checkbox w-4 h-4 text-violet-600 rounded"
                            onchange="ExamManager.toggleExercise('${ex.id}', this.checked)"
                            ${selectedExerciseIds.has(ex.id) ? 'checked' : ''}>
                        <div class="flex-1 min-w-0">
                            <p class="font-medium text-slate-800 text-sm">${ex.title || 'Exercise không có tiêu đề'}</p>
                            <p class="text-xs text-slate-500 mt-0.5">
                                Loại: ${ex.type || 'N/A'}
                                &nbsp;•&nbsp; ${(ex.questions || []).length} câu hỏi
                                &nbsp;•&nbsp; ${ex.maxScore || 0} điểm tối đa
                            </p>
                        </div>
                    </label>
                </div>
            `).join('');

        } catch (err) {
            exerciseList.innerHTML = '<p class="text-sm text-red-400 text-center py-4">Lỗi khi tải exercise.</p>';
            console.error('Lỗi khi tải danh sách exercise:', err);
        }
    }

    /**
     * Bật/tắt exercise trong danh sách đã chọn
     * Cập nhật input ẩn và tóm tắt câu hỏi
     * @param {string} exerciseId - ID exercise
     * @param {boolean} isChecked - true = chọn, false = bỏ chọn
     */
    function toggleExercise(exerciseId, isChecked) {
        if (isChecked) {
            selectedExerciseIds.add(exerciseId);
        } else {
            selectedExerciseIds.delete(exerciseId);
        }
        // Cập nhật input ẩn để form đọc được khi submit
        document.getElementById('selectedExerciseIds').value = [...selectedExerciseIds].join(',');
        updateQuestionSummary();
    }

    // ======================================================================
    // Tab câu hỏi tự soạn
    // ======================================================================

    /**
     * Hiển thị/ẩn khu vực lựa chọn tùy theo loại câu hỏi
     * MULTIPLE_CHOICE và TRUE_FALSE cần danh sách options
     * FILL_IN_BLANK không cần options
     */
    function onQuestionTypeChange() {
        const type = document.getElementById('newQuestionType').value;
        const optionsSection = document.getElementById('optionsSection');
        if (type === 'FILL_IN_BLANK') {
            optionsSection.classList.add('hidden');
        } else {
            optionsSection.classList.remove('hidden');
            // Gợi ý cho TRUE_FALSE
            if (type === 'TRUE_FALSE') {
                document.getElementById('newQuestionOptions').placeholder = 'True\nFalse';
                document.getElementById('newQuestionAnswer').placeholder = 'true hoặc false';
            } else {
                document.getElementById('newQuestionOptions').placeholder = 'A. Lựa chọn A\nB. Lựa chọn B\nC. Lựa chọn C\nD. Lựa chọn D';
                document.getElementById('newQuestionAnswer').placeholder = 'VD: A';
            }
        }
    }

    /**
     * Thêm câu hỏi tự soạn vào danh sách
     * Validate input rồi render ra UI và lưu vào mảng customQuestions
     */
    function addCustomQuestion() {
        const text        = document.getElementById('newQuestionText').value.trim();
        const type        = document.getElementById('newQuestionType').value;
        const optionsRaw  = document.getElementById('newQuestionOptions').value.trim();
        const answer      = document.getElementById('newQuestionAnswer').value.trim();
        const explanation = document.getElementById('newQuestionExplanation').value.trim();
        const score       = parseInt(document.getElementById('newQuestionScore').value) || 1;

        // Validate dữ liệu bắt buộc
        if (!text) { alert('Vui lòng nhập nội dung câu hỏi'); return; }
        if (!answer) { alert('Vui lòng nhập đáp án đúng'); return; }

        // Tách options từ textarea (mỗi dòng 1 option)
        const options = optionsRaw ? optionsRaw.split('\n').map(o => o.trim()).filter(o => o) : [];

        const question = {
            questionIndex: customQuestions.length,
            questionText: text,
            type,
            options,
            correctAnswer: answer,
            explanation: explanation || null,
            score,
            sourceExerciseId: null
        };

        customQuestions.push(question);
        renderCustomQuestion(question, customQuestions.length - 1);
        clearCustomQuestionForm();
        updateQuestionSummary();
    }

    /**
     * Render một câu hỏi tự soạn vào danh sách trên UI
     * @param {Object} q - Đối tượng câu hỏi
     * @param {number} idx - Chỉ số trong mảng
     */
    function renderCustomQuestion(q, idx) {
        const list = document.getElementById('customQuestionList');
        const div  = document.createElement('div');
        div.id = 'customQ_' + idx;
        div.className = 'border border-slate-200 rounded-lg p-4 bg-white';
        div.innerHTML = `
            <div class="flex items-start justify-between gap-3">
                <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 mb-1">
                        <span class="px-2 py-0.5 bg-violet-100 text-violet-700 rounded text-xs font-medium">Câu ${idx + 1}</span>
                        <span class="px-2 py-0.5 bg-slate-100 text-slate-600 rounded text-xs">${q.type}</span>
                    </div>
                    <p class="text-sm font-medium text-slate-800">${escapeHtml(q.questionText)}</p>
                    <p class="text-xs text-emerald-600 mt-1">✓ Đáp án: ${escapeHtml(q.correctAnswer)}</p>
                    ${q.explanation ? `<p class="text-xs text-slate-400 mt-0.5">💡 ${escapeHtml(q.explanation)}</p>` : ''}
                </div>
                <!-- Nút xóa câu hỏi này -->
                <button type="button" onclick="ExamManager.removeCustomQuestion(${idx})"
                    class="text-slate-400 hover:text-red-500 transition-colors shrink-0">
                    <iconify-icon icon="solar:trash-bin-2-linear" width="18"></iconify-icon>
                </button>
            </div>`;
        list.appendChild(div);
    }

    /**
     * Xóa câu hỏi tự soạn khỏi danh sách (ẩn UI, đánh dấu null trong mảng)
     * @param {number} idx - Chỉ số câu hỏi cần xóa
     */
    function removeCustomQuestion(idx) {
        customQuestions[idx] = null; // Dùng null để giữ chỉ số ổn định
        const el = document.getElementById('customQ_' + idx);
        if (el) el.remove();
        updateQuestionSummary();
    }

    /** Xóa form nhập câu hỏi sau khi thêm thành công */
    function clearCustomQuestionForm() {
        document.getElementById('newQuestionText').value = '';
        document.getElementById('newQuestionOptions').value = '';
        document.getElementById('newQuestionAnswer').value = '';
        document.getElementById('newQuestionExplanation').value = '';
        document.getElementById('newQuestionScore').value = '1';
    }

    // ======================================================================
    // Cập nhật tóm tắt số câu hỏi đã chọn
    // ======================================================================

    /** Cập nhật badge hiển thị tổng số câu hỏi (từ ngân hàng + tự soạn) */
    function updateQuestionSummary() {
        const bankCount   = selectedExerciseIds.size; // Số exercise từ ngân hàng
        const customCount = customQuestions.filter(q => q !== null).length;
        const total = bankCount + customCount;

        const summary = document.getElementById('questionSummary');
        const text    = document.getElementById('questionSummaryText');
        if (total > 0) {
            let parts = [];
            if (bankCount > 0)   parts.push(bankCount + ' exercise từ ngân hàng');
            if (customCount > 0) parts.push(customCount + ' câu tự soạn');
            text.textContent = 'Đã chọn: ' + parts.join(' + ');
            summary.classList.remove('hidden');
        } else {
            summary.classList.add('hidden');
        }
    }

    // ======================================================================
    // Submit tạo đề thi
    // ======================================================================

    /**
     * Gọi API tạo đề thi khi giáo viên nhấn nút "Tạo đề thi"
     * Gom câu hỏi tự soạn + exerciseIds rồi POST lên /api/exams
     * @param {Event} event - Submit event từ form
     */
    async function createExam(event) {
        event.preventDefault();
        const form = event.target;

        // Lấy dữ liệu từ form
        const title       = form.title.value.trim();
        const description = form.description.value.trim();
        const classroomId = form.classroomId.value;
        const timeLimitRaw = parseInt(form.timeLimitMinutes.value);
        const timeLimitMinutes = isNaN(timeLimitRaw) || timeLimitRaw <= 0 ? null : timeLimitRaw;

        // Kiểm tra bắt buộc
        if (!title)       { alert('Vui lòng nhập tiêu đề đề thi'); return; }
        if (!classroomId) { alert('Vui lòng chọn lớp học'); return; }

        // Câu hỏi tự soạn (lọc bỏ các phần tử null đã xóa)
        const filteredCustom = customQuestions.filter(q => q !== null);

        // Kiểm tra phải có ít nhất 1 câu hỏi từ một trong hai nguồn
        if (selectedExerciseIds.size === 0 && filteredCustom.length === 0) {
            alert('Vui lòng thêm ít nhất 1 câu hỏi (từ ngân hàng hoặc tự soạn)');
            return;
        }

        // Vô hiệu hóa nút submit tránh double click
        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<iconify-icon icon="solar:refresh-linear" width="18" class="animate-spin inline mr-1"></iconify-icon> Đang tạo...';

        const payload = {
            title,
            description: description || null,
            classroomId,
            timeLimitMinutes,
            questions: filteredCustom,
            exerciseIds: [...selectedExerciseIds]
        };

        try {
            const token = localStorage.getItem('accessToken') || '';
            const res  = await fetch('/api/exams', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(payload)
            });
            const data = await res.json();

            if (!res.ok || data.statusCode !== 200) {
                throw new Error(data.message || 'Tạo đề thi thất bại');
            }

            // Hiển thị modal với mã PIN vừa tạo
            document.getElementById('createdPinCode').textContent = data.data.pinCode;
            document.getElementById('pinModal').classList.remove('hidden');

        } catch (err) {
            alert('Lỗi: ' + err.message);
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<iconify-icon icon="solar:diploma-linear" width="18"></iconify-icon> Tạo đề thi';
        }
    }

    /**
     * Sao chép mã PIN vừa tạo vào clipboard
     */
    function copyCreatedPin() {
        const pin = document.getElementById('createdPinCode').textContent;
        navigator.clipboard.writeText(pin).then(() => {
            alert('Đã sao chép mã PIN: ' + pin);
        }).catch(() => {
            // Fallback cho trình duyệt không hỗ trợ clipboard
            const input = document.createElement('input');
            input.value = pin;
            document.body.appendChild(input);
            input.select();
            document.execCommand('copy');
            document.body.removeChild(input);
            alert('Đã sao chép mã PIN: ' + pin);
        });
    }

    // ======================================================================
    // Hàm tiện ích
    // ======================================================================

    /**
     * Escape HTML để tránh XSS khi render nội dung người dùng nhập vào DOM
     * @param {string} str - Chuỗi cần escape
     */
    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // Expose các hàm public ra ngoài module
    return {
        switchTab,
        loadLessons,
        loadExercises,
        toggleExercise,
        onQuestionTypeChange,
        addCustomQuestion,
        removeCustomQuestion,
        createExam,
        copyCreatedPin
    };

})();
