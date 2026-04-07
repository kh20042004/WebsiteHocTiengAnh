/**
 * ============================================
 * TEACHER EXERCISE MANAGER
 * ============================================
 * 
 * Quản lý tạo câu hỏi (Exercise) cho các loại khác nhau:
 * - Multiple Choice (Trắc nghiệm)
 * - Fill in the Blank (Điền từ vào chỗ trống)
 * - True/False (Câu đúng sai)
 * - Matching (Ghép đôi từ/cụm từ)
 * - Short Answer (Trả lời ngắn)
 */

// Protect từ double loading (tránh khai báo 2 lần)
if (typeof ExerciseManager === 'undefined') {

    const ExerciseManager = (() => {
        /**
         * State để lưu thông tin câu hỏi hiện tại
         */
        let currentQuestionType = 'MULTIPLE_CHOICE';
        let questionData = {};

        /**
         * Chuyển đổi loại câu hỏi khi người dùng chọn
         * @param {String} type - Loại câu hỏi (MULTIPLE_CHOICE, FILL_BLANK, etc.)
         */
        function switchQuestionType(type) {
            console.log(`📝 Chuyển loại câu hỏi sang: ${type}`);
            currentQuestionType = type;
            
            // Xoá các dữ liệu từ loại câu hỏi cũ
            questionData = {};
            
            // Render giao diện input tương ứng với loại câu hỏi
            renderQuestionDetails(type);
        }

        /**
         * Render giao diện nhập liệu dựa vào loại câu hỏi
         * @param {String} type - Loại câu hỏi
         */
        function renderQuestionDetails(type) {
            const container = document.getElementById('questionDetailsContainer');
            let html = '';

            switch (type) {
                case 'MULTIPLE_CHOICE':
                    html = renderMultipleChoice();
                    break;
                case 'FILL_BLANK':
                    html = renderFillBlank();
                    break;
                case 'TRUE_FALSE':
                    html = renderTrueFalse();
                    break;
                case 'MATCHING':
                    html = renderMatching();
                    break;
                case 'SHORT_ANSWER':
                    html = renderShortAnswer();
                    break;
                default:
                    html = '<p class="text-red-500">Loại câu hỏi không được hỗ trợ</p>';
            }

            container.innerHTML = html;
        }

        /**
         * ============================================
         * MULTIPLE CHOICE - Trắc nghiệm (A, B, C, D)
         * ============================================
         */
        function renderMultipleChoice() {
            return `
                <h2 class="text-lg font-semibold text-slate-900 pb-4 border-b border-slate-200">
                    <iconify-icon icon="mdi:list-box" width="20" class="inline mr-2"></iconify-icon>
                    Chi tiết câu hỏi trắc nghiệm
                </h2>

                <div class="bg-emerald-50 border border-emerald-200 rounded-lg p-4 mb-6">
                    <p class="text-sm text-emerald-800">
                        💡 <strong>Hướng dẫn:</strong> Thêm 3-5 đáp án khác nhau. 
                        Chỉ định 1 đáp án đúng. Các đáp án khác sẽ là "nhiễu".
                    </p>
                </div>

                <!-- Nội dung câu hỏi -->
                <div class="mb-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Nội dung câu hỏi <span class="text-red-500">*</span>
                    </label>
                    <textarea id="mcQuestionContent" rows="3" required
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="Nhập nội dung câu hỏi đầy đủ..."></textarea>
                </div>

                <!-- Danh sách đáp án -->
                <div class="space-y-3 mb-6">
                    <label class="block text-sm font-medium text-slate-900">
                        Đáp án <span class="text-red-500">*</span>
                    </label>
                    
                    <!-- Đáp án A -->
                    <div class="flex items-start gap-3 p-3 border border-slate-200 rounded-lg">
                        <div class="flex items-center gap-2 pt-1">
                            <input type="radio" name="mcCorrectAnswer" value="A" 
                                class="w-4 h-4 cursor-pointer" required>
                            <span class="font-semibold text-slate-400">A.</span>
                        </div>
                        <input type="text" id="mcAnswerA" placeholder="Nhập đáp án A"
                            class="flex-1 px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-emerald-500"
                            required>
                    </div>

                    <!-- Đáp án B -->
                    <div class="flex items-start gap-3 p-3 border border-slate-200 rounded-lg">
                        <div class="flex items-center gap-2 pt-1">
                            <input type="radio" name="mcCorrectAnswer" value="B" 
                                class="w-4 h-4 cursor-pointer" required>
                            <span class="font-semibold text-slate-400">B.</span>
                        </div>
                        <input type="text" id="mcAnswerB" placeholder="Nhập đáp án B"
                            class="flex-1 px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-emerald-500"
                            required>
                    </div>

                    <!-- Đáp án C -->
                    <div class="flex items-start gap-3 p-3 border border-slate-200 rounded-lg">
                        <div class="flex items-center gap-2 pt-1">
                            <input type="radio" name="mcCorrectAnswer" value="C" 
                                class="w-4 h-4 cursor-pointer" required>
                            <span class="font-semibold text-slate-400">C.</span>
                        </div>
                        <input type="text" id="mcAnswerC" placeholder="Nhập đáp án C"
                            class="flex-1 px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-emerald-500"
                            required>
                    </div>

                    <!-- Đáp án D -->
                    <div class="flex items-start gap-3 p-3 border border-slate-200 rounded-lg">
                        <div class="flex items-center gap-2 pt-1">
                            <input type="radio" name="mcCorrectAnswer" value="D" 
                                class="w-4 h-4 cursor-pointer" required>
                            <span class="font-semibold text-slate-400">D.</span>
                        </div>
                        <input type="text" id="mcAnswerD" placeholder="Nhập đáp án D"
                            class="flex-1 px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-emerald-500"
                            required>
                    </div>

                    <!-- Tùy chọn: Thêm đáp án E -->
                    <div class="flex items-start gap-3 p-3 border border-slate-200 rounded-lg">
                        <div class="flex items-center gap-2 pt-1">
                            <input type="radio" name="mcCorrectAnswer" value="E" 
                                class="w-4 h-4 cursor-pointer">
                            <span class="font-semibold text-slate-400">E.</span>
                        </div>
                        <input type="text" id="mcAnswerE" placeholder="Nhập đáp án E (tuỳ chọn)"
                            class="flex-1 px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-emerald-500">
                    </div>
                </div>

                <!-- Giải thích đáp án -->
                <div>
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Giải thích (tuỳ chọn - hiển thị sau khi học sinh trả lời)
                    </label>
                    <textarea id="mcExplanation" rows="2"
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="Giải thích tại sao đáp án này đúng..."></textarea>
                </div>
            `;
        }

        /**
         * ============================================
         * FILL IN THE BLANK - Điền từ vào chỗ trống
         * ============================================
         */
        function renderFillBlank() {
            return `
                <h2 class="text-lg font-semibold text-slate-900 pb-4 border-b border-slate-200">
                    <iconify-icon icon="mdi:pencil" width="20" class="inline mr-2"></iconify-icon>
                    Chi tiết câu hỏi điền từ
                </h2>

                <div class="bg-emerald-50 border border-emerald-200 rounded-lg p-4 mb-6">
                    <p class="text-sm text-emerald-800">
                        💡 <strong>Hướng dẫn:</strong> Nhập câu với dấu [...] để chỉ vị trí cần điền.
                        <br>VD: "She [...] a teacher." → Đáp án đúng: "is"
                    </p>
                </div>

                <!-- Câu hỏi với chỗ trống -->
                <div class="mb-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Nội dung câu hỏi <span class="text-red-500">*</span>
                        <span class="text-xs text-slate-500">(dùng [...] để chỉ chỗ cần điền)</span>
                    </label>
                    <textarea id="fbQuestionContent" rows="3" required
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="VD: She [...] a doctor."></textarea>
                </div>

                <!-- Từ đúng cần điền -->
                <div class="mb-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Từ/cụm từ đúng cần điền <span class="text-red-500">*</span>
                    </label>
                    <div class="flex gap-3">
                        <input type="text" id="fbCorrectAnswer" required
                            class="flex-1 px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                            placeholder="VD: is">
                        <button type="button" onclick="window.ExerciseManager.addAlternativeAnswer()"
                            class="px-4 py-3 bg-slate-100 hover:bg-slate-200 rounded-lg text-sm font-medium transition-colors">
                            + Thêm đáp án khác
                        </button>
                    </div>
                    <p class="text-xs text-slate-500 mt-1">
                        💡 Bạn có thể thêm nhiều đáp án khác nhau được coi là đúng (ví dụ: "is" và "is going to be")
                    </p>
                </div>

                <!-- Danh sách đáp án khác -->
                <div id="alternativeAnswersContainer" class="mb-6 space-y-3 hidden">
                    <label class="block text-sm font-medium text-slate-900">
                        Đáp án khác được chấp nhận
                    </label>
                    <!-- Sẽ được thêm động -->
                </div>

                <!-- Giải thích -->
                <div>
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Giải thích (tuỳ chọn)
                    </label>
                    <textarea id="fbExplanation" rows="2"
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="Giải thích ngữ pháp, từ vựng..."></textarea>
                </div>
            `;
        }

        /**
         * ============================================
         * TRUE/FALSE - Câu đúng/sai
         * ============================================
         */
        function renderTrueFalse() {
            return `
                <h2 class="text-lg font-semibold text-slate-900 pb-4 border-b border-slate-200">
                    <iconify-icon icon="mdi:check-circle" width="20" class="inline mr-2"></iconify-icon>
                    Chi tiết câu hỏi đúng/sai
                </h2>

                <div class="bg-emerald-50 border border-emerald-200 rounded-lg p-4 mb-6">
                    <p class="text-sm text-emerald-800">
                        💡 <strong>Hướng dẫn:</strong> Tạo một câu phát biểu và xác định nó Đúng hay Sai.
                    </p>
                </div>

                <!-- Câu phát biểu -->
                <div class="mb-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Câu phát biểu <span class="text-red-500">*</span>
                    </label>
                    <textarea id="tfQuestionContent" rows="3" required
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="Nhập câu phát biểu..."></textarea>
                </div>

                <!-- Đáp án đúng -->
                <div class="mb-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Câu này là <span class="text-red-500">*</span>
                    </label>
                    <div class="flex gap-4">
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="tfCorrectAnswer" value="TRUE" required
                                class="w-4 h-4">
                            <span class="text-sm font-medium">✓ Đúng (True)</span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="tfCorrectAnswer" value="FALSE" required
                                class="w-4 h-4">
                            <span class="text-sm font-medium">✗ Sai (False)</span>
                        </label>
                    </div>
                </div>

                <!-- Giải thích -->
                <div>
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Giải thích (tuỳ chọn)
                    </label>
                    <textarea id="tfExplanation" rows="2"
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="Giải thích tại sao câu này đúng/sai..."></textarea>
                </div>
            `;
        }

        /**
         * ============================================
         * MATCHING - Ghép đôi
         * ============================================
         */
        function renderMatching() {
            return `
                <h2 class="text-lg font-semibold text-slate-900 pb-4 border-b border-slate-200">
                    <iconify-icon icon="mdi:link" width="20" class="inline mr-2"></iconify-icon>
                    Chi tiết câu hỏi ghép đôi
                </h2>

                <div class="bg-emerald-50 border border-emerald-200 rounded-lg p-4 mb-6">
                    <p class="text-sm text-emerald-800">
                        💡 <strong>Hướng dẫn:</strong> Tạo danh sách từ trái (cần ghép) và từ phải (lựa chọn).
                        <br>Ví dụ: Ghép "apple" với "một loại trái cây"
                    </p>
                </div>

                <div class="grid grid-cols-2 gap-6">
                    <!-- Cột trái (Items to match) -->
                    <div>
                        <h3 class="font-semibold text-slate-900 mb-3">Cột trái</h3>
                        <label class="text-sm text-slate-600 mb-2 block">
                            Nhập từng mục và nhấn [Enter] hoặc [+]
                        </label>
                        <div id="matchingLeftItems" class="space-y-2 border-l-4 border-emerald-300 pl-4">
                            <div class="flex gap-2">
                                <input type="text" placeholder="Mục 1..." 
                                    class="flex-1 px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-emerald-500"
                                    onkeypress="if(event.key==='Enter') window.ExerciseManager.addMatchingLeft(event)">
                                <button type="button" onclick="window.ExerciseManager.addMatchingLeft(event)"
                                    class="px-2 py-2 bg-emerald-500 text-white rounded hover:bg-emerald-600">+</button>
                            </div>
                        </div>
                    </div>

                    <!-- Cột phải (Options) -->
                    <div>
                        <h3 class="font-semibold text-slate-900 mb-3">Cột phải</h3>
                        <label class="text-sm text-slate-600 mb-2 block">
                            Nhập từng tùy chọn và nhấn [Enter] hoặc [+]
                        </label>
                        <div id="matchingRightItems" class="space-y-2 border-r-4 border-blue-300 pr-4">
                            <div class="flex gap-2">
                                <input type="text" placeholder="Tùy chọn 1..." 
                                    class="flex-1 px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-emerald-500"
                                    onkeypress="if(event.key==='Enter') window.ExerciseManager.addMatchingRight(event)">
                                <button type="button" onclick="window.ExerciseManager.addMatchingRight(event)"
                                    class="px-2 py-2 bg-blue-500 text-white rounded hover:bg-blue-600">+</button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Mapping (ghép nối) -->
                <div class="mt-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Ghép nối đúng <span class="text-red-500">*</span>
                    </label>
                    <div id="matchingPairs" class="space-y-3 p-4 border-2 border-dashed border-slate-300 rounded-lg bg-slate-50">
                        <p class="text-slate-500 text-sm">Thêm ít nhất 3 mục ở mỗi cột để bắt đầu ghép nối</p>
                    </div>
                </div>

                <!-- Giải thích -->
                <div class="mt-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Giải thích (tuỳ chọn)
                    </label>
                    <textarea id="matchingExplanation" rows="2"
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="Giải thích mối liên hệ giữa các từ..."></textarea>
                </div>
            `;
        }

        /**
         * ============================================
         * SHORT ANSWER - Trả lời ngắn
         * ============================================
         */
        function renderShortAnswer() {
            return `
                <h2 class="text-lg font-semibold text-slate-900 pb-4 border-b border-slate-200">
                    <iconify-icon icon="mdi:text-box" width="20" class="inline mr-2"></iconify-icon>
                    Chi tiết câu hỏi trả lời ngắn
                </h2>

                <div class="bg-emerald-50 border border-emerald-200 rounded-lg p-4 mb-6">
                    <p class="text-sm text-emerald-800">
                        💡 <strong>Hướng dẫn:</strong> Học sinh sẽ gõ một câu trả lời ngắn. 
                        Cần phải chấm tay (không tự động).
                    </p>
                </div>

                <!-- Câu hỏi -->
                <div class="mb-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Câu hỏi <span class="text-red-500">*</span>
                    </label>
                    <textarea id="saQuestionContent" rows="3" required
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="VD: What is your name?"></textarea>
                </div>

                <!-- Đáp án mẫu -->
                <div class="mb-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Đáp án mẫu (để tham khảo khi chấm) <span class="text-red-500">*</span>
                    </label>
                    <textarea id="saSampleAnswer" rows="2" required
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="VD: My name is John."></textarea>
                    <p class="text-xs text-slate-500 mt-1">
                        💡 Đây là gợi ý cho giáo viên khi chấm. Học sinh không nhìn thấy.
                    </p>
                </div>

                <!-- Độ dài dự kiến -->
                <div class="mb-6">
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Độ dài dự kiến của câu trả lời
                    </label>
                    <div class="flex gap-4">
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="saLength" value="SHORT" checked
                                class="w-4 h-4">
                            <span class="text-sm">📝 Ngắn (1-2 từ)</span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="saLength" value="MEDIUM"
                                class="w-4 h-4">
                            <span class="text-sm">📄 Trung bình (1-2 câu)</span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="saLength" value="LONG"
                                class="w-4 h-4">
                            <span class="text-sm">📋 Dài (đoạn văn)</span>
                        </label>
                    </div>
                </div>

                <!-- Tiêu chí chấm điểm -->
                <div>
                    <label class="block text-sm font-medium text-slate-900 mb-2">
                        Tiêu chí chấm điểm (tuỳ chọn)
                    </label>
                    <textarea id="saGradingCriteria" rows="3"
                        class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                        placeholder="VD: 
- Đáp án chính xác: 5 điểm
- Ngữ pháp Ok nhưng chính tả lỗi: 3 điểm
- Phần nào sai mấy diểm..."></textarea>
                </div>
            `;
        }

        /**
         * Cập nhật thông tin Lesson được chọn
         * (T tương lai có thể thêm logic để load settings từ Lesson)
         */
        function updateLessonInfo() {
            const lessonId = document.getElementById('lessonSelect').value;
            console.log(`📚 Chọn Lesson: ${lessonId}`);
            // Logic thêm ở đây nếu cần
        }

        /**
         * Thêm đáp án khác cho câu hỏi Fill Blank
         */
        function addAlternativeAnswer() {
            const container = document.getElementById('alternativeAnswersContainer');
            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'flex-1 px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent';
            input.placeholder = 'Nhập đáp án khác...';
            
            const wrapper = document.createElement('div');
            wrapper.className = 'flex gap-2';
            wrapper.innerHTML = `<input type="text" placeholder="Nhập đáp án khác..." class="flex-1 px-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent">
                <button type="button" class="px-3 py-2 bg-red-500 text-white rounded hover:bg-red-600 text-sm" onclick="this.parentElement.remove()">
                    Xoá
                </button>`;
            
            container.classList.remove('hidden');
            container.appendChild(wrapper);
        }

        /**
         * Thêm mục vào cột trái (Matching - Items to match)
         */
        function addMatchingLeft(event) {
            event.preventDefault();
            const input = event.target.closest('.flex').querySelector('input');
            const value = input.value.trim();
            
            if (!value) {
                alert('Vui lòng nhập nội dung');
                return;
            }

            const container = document.getElementById('matchingLeftItems');
            const item = document.createElement('div');
            item.className = 'flex items-center gap-2 p-2 bg-emerald-100 rounded';
            item.innerHTML = `
                <span class="flex-1 text-sm">${value}</span>
                <button type="button" class="text-red-600 hover:text-red-800 text-sm" onclick="this.parentElement.remove()">
                    ✕
                </button>
            `;
            
            // Thêm trước input nếu input là phần tử cuối cùng
            const inputs = container.querySelectorAll('.flex');
            if (inputs.length > 0) {
                inputs[inputs.length - 1].parentElement.insertBefore(item, inputs[inputs.length - 1]);
            }
            
            input.value = '';
            input.focus();
        }

        /**
         * Thêm mục vào cột phải (Matching - Options)
         */
        function addMatchingRight(event) {
            event.preventDefault();
            const input = event.target.closest('.flex').querySelector('input');
            const value = input.value.trim();
            
            if (!value) {
                alert('Vui lòng nhập nội dung');
                return;
            }

            const container = document.getElementById('matchingRightItems');
            const item = document.createElement('div');
            item.className = 'flex items-center gap-2 p-2 bg-blue-100 rounded';
            item.innerHTML = `
                <span class="flex-1 text-sm">${value}</span>
                <button type="button" class="text-red-600 hover:text-red-800 text-sm" onclick="this.parentElement.remove()">
                    ✕
                </button>
            `;
            
            const inputs = container.querySelectorAll('.flex');
            if (inputs.length > 0) {
                inputs[inputs.length - 1].parentElement.insertBefore(item, inputs[inputs.length - 1]);
            }
            
            input.value = '';
            input.focus();
        }

        /**
         * Validate và thu thập dữ liệu form trước khi submit
         * @returns {Object|null} Dữ liệu validate hoặc null nếu lỗi
         */
        function getFormData() {
            const baseData = {
                // Thông tin cơ bản
                lessonId: document.getElementById('lessonSelect').value,
                title: document.getElementById('questionTitle').value.trim(),
                instruction: document.getElementById('instruction').value.trim(),
                maxScore: parseInt(document.getElementById('maxScore').value) || 10,
                timeLimitMinutes: parseInt(document.getElementById('estimatedDuration').value) || 5, // mapped to timeLimitMinutes
                xpReward: 5
            };

            if (!baseData.lessonId) throw new Error('Vui lòng chọn Lesson');
            if (!baseData.title) throw new Error('Vui lòng nhập tiêu đề bài tập');

            let specificData = {};

            switch (currentQuestionType) {
                case 'MULTIPLE_CHOICE': specificData = getMultipleChoiceData(); break;
                case 'FILL_BLANK': specificData = getFillBlankData(); break;
                case 'TRUE_FALSE': specificData = getTrueFalseData(); break;
                case 'MATCHING': specificData = getMatchingData(); break;
                case 'SHORT_ANSWER': specificData = getShortAnswerData(); break;
            }

            return { ...baseData, ...specificData };
        }

        function getMultipleChoiceData() {
            const content = document.getElementById('mcQuestionContent').value.trim();
            const correctAnswer = document.querySelector('input[name="mcCorrectAnswer"]:checked')?.value;
            const explanation = document.getElementById('mcExplanation').value.trim();

            if (!content) throw new Error('Vui lòng nhập nội dung câu hỏi');
            if (!correctAnswer) throw new Error('Vui lòng chọn đáp án đúng');

            const options = [];
            for (let letter of ['A', 'B', 'C', 'D', 'E']) {
                const ans = document.getElementById(`mcAnswer${letter}`)?.value.trim();
                if (ans) options.push(ans);
            }

            if (options.length < 2) throw new Error('Cần ít nhất 2 đáp án');

            return {
                type: 'MULTIPLE_CHOICE',
                questions: [{
                    questionIndex: 1,
                    questionText: content,
                    options: options,
                    correctAnswer: correctAnswer,
                    explanation: explanation
                }]
            };
        }

        function getFillBlankData() {
            const content = document.getElementById('fbQuestionContent').value.trim();
            const correctAnswer = document.getElementById('fbCorrectAnswer').value.trim();
            const explanation = document.getElementById('fbExplanation').value.trim();

            if (!content) throw new Error('Vui lòng nhập nội dung câu hỏi');
            if (!correctAnswer) throw new Error('Vui lòng nhập từ/cụm từ đúng');
            if (!content.match(/\[\.+\]/)) throw new Error('Câu hỏi phải chứa [...] (3 dấu chấm) để chỉ vị trí cần điền');

            return {
                type: 'FILL_IN_BLANK',
                questions: [{
                    questionIndex: 1,
                    questionText: content,
                    correctAnswer: correctAnswer,
                    explanation: explanation
                }]
            };
        }

        function getTrueFalseData() {
            const content = document.getElementById('tfQuestionContent').value.trim();
            const correctAnswer = document.querySelector('input[name="tfCorrectAnswer"]:checked')?.value;
            const explanation = document.getElementById('tfExplanation').value.trim();

            if (!content) throw new Error('Vui lòng nhập nội dung câu phát biểu');
            if (!correctAnswer) throw new Error('Vui lòng chọn Đúng hoặc Sai');

            return {
                type: 'TRUE_FALSE',
                questions: [{
                    questionIndex: 1,
                    questionText: content,
                    correctAnswer: correctAnswer === 'TRUE' ? 'True' : 'False',
                    explanation: explanation
                }]
            };
        }

        function getMatchingData() {
            const leftItems = Array.from(document.querySelectorAll('#matchingLeftItems > div:not(:last-child) span:first-child')).map(el => el.textContent.trim());
            const rightItems = Array.from(document.querySelectorAll('#matchingRightItems > div:not(:last-child) span:first-child')).map(el => el.textContent.trim());
            const explanation = document.getElementById('matchingExplanation').value.trim();

            if (leftItems.length < 3) throw new Error('Cần ít nhất 3 mục ở cột trái');
            if (rightItems.length < 3) throw new Error('Cần ít nhất 3 tùy chọn ở cột phải');

            return {
                type: 'MATCHING',
                questions: [{
                    questionIndex: 1,
                    questionText: leftItems.join(' | ') + ' === ' + rightItems.join(' | '),
                    correctAnswer: 'Matching Exercise', // Placeholder
                    explanation: explanation
                }]
            };
        }

        function getShortAnswerData() {
            const content = document.getElementById('saQuestionContent').value.trim();
            const sampleAnswer = document.getElementById('saSampleAnswer').value.trim();
            const explanation = document.getElementById('saGradingCriteria').value.trim();

            if (!content) throw new Error('Vui lòng nhập câu hỏi');
            if (!sampleAnswer) throw new Error('Vui lòng nhập đáp án mẫu');

            return {
                type: 'SHORT_ANSWER',
                questions: [{
                    questionIndex: 1,
                    questionText: content,
                    correctAnswer: sampleAnswer,
                    explanation: explanation
                }]
            };

        }

        /**
         * Xử lý form submission
         * Gửi dữ liệu tới backend API
         */
        document.addEventListener('DOMContentLoaded', () => {
            const form = document.getElementById('createExerciseForm');
            
            if (form) {
                form.addEventListener('submit', async (event) => {
                    event.preventDefault();
                    
                    try {
                        console.log('📤 Đang xử lý...');
                        const formData = getFormData();
                        console.log('📦 Dữ liệu ghửi:', formData);

                        // Gửi tới API
                        const response = await fetch('/api/exercises', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify(formData)
                        });

                        if (!response.ok) {
                            const error = await response.json();
                            throw new Error(error.message || 'Lỗi khi tạo câu hỏi');
                        }

                        const result = await response.json();
                        NotificationManager.success(`✅ Câu hỏi "${formData.title}" đã được tạo thành công!`);

                        // Redirect về trang content sau 1 giây
                        setTimeout(() => {
                            window.location.href = '/dashboard/teacher/content';
                        }, 1500);

                    } catch (error) {
                        console.error('❌ Lỗi:', error.message);
                        NotificationManager.error(error.message || 'Có lỗi xảy ra');
                    }
                });
            }

            // Render loại câu hỏi mặc định
            renderQuestionDetails('MULTIPLE_CHOICE');
        });

        /**
         * Public API - Các hàm được expose ra global scope
         */
        return {
            switchQuestionType,
            updateLessonInfo,
            addAlternativeAnswer,
            addMatchingLeft,
            addMatchingRight,
            getFormData
        };
    })();

    // Expose functions sang global scope để HTML inline event handlers có thể gọi
    window.ExerciseManager = ExerciseManager;

} // Kết thúc if block để tránh double loading
