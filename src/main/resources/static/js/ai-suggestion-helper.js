/**
 * AI Suggestion Helper - Tích hợp AI vào trang tạo bài tập/kiểm tra
 * File này hỗ trợ gọi API AI và điền dữ liệu vào form
 */

class AISuggestionHelper {
    constructor() {
        this.isLoading = false;
        this.lastSuggestion = null;
    }

    /**
     * Mở modal gợi ý bài tập
     */
    openExerciseSuggestionModal() {
        const modal = document.getElementById('aiExerciseModal');
        if (modal) {
            modal.classList.remove('hidden');
        }
    }

    /**
     * Mở modal gợi ý bài kiểm tra
     */
    openExamSuggestionModal() {
        const modal = document.getElementById('aiExamModal');
        if (modal) {
            modal.classList.remove('hidden');
        }
    }

    /**
     * Đóng modal
     */
    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('hidden');
        }
    }

    /**
     * Gợi ý bài tập từ AI
     */
    async suggestExercise() {
        if (this.isLoading) return;

        const unit = document.getElementById('aiExerciseUnit')?.value;
        const skillLevel = document.getElementById('aiExerciseSkillLevel')?.value;
        const exerciseType = document.getElementById('aiExerciseType')?.value;
        const customPrompt = document.getElementById('aiExerciseCustomPrompt')?.value;

        if (!unit) {
            alert('Vui lòng nhập Unit');
            return;
        }

        this.isLoading = true;
        const loading = document.getElementById('aiExerciseLoading');
        const button = document.querySelector('#aiExerciseModal button[onclick*="suggestExercise"]');
        
        if (loading) loading.classList.remove('hidden');
        if (button) button.disabled = true;

        try {
            const response = await fetch('/api/ai/suggest-exercises', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': this.getCsrfToken()
                },
                body: JSON.stringify({
                    type: 'exercise',
                    unit: unit,
                    skillLevel: skillLevel || 'B1',
                    exerciseType: exerciseType || 'multiple_choice',
                    quantity: 1,
                    customPrompt: customPrompt
                })
            });

            const data = await response.json();

            if (data.status === 'success' && data.suggestions && data.suggestions.length > 0) {
                const exercise = data.suggestions[0];
                this.fillExerciseForm(exercise);
                this.lastSuggestion = exercise;
                this.closeModal('aiExerciseModal');
                alert('✓ Bài tập gợi ý đã được điền vào form');
            } else {
                alert('Lỗi: ' + (data.message || 'Không thể gợi ý'));
            }
        } catch (error) {
            console.error('Lỗi:', error);
            alert('Lỗi kết nối AI: ' + error.message);
        } finally {
            this.isLoading = false;
            if (loading) loading.classList.add('hidden');
            if (button) button.disabled = false;
        }
    }

    /**
     * Gợi ý bài kiểm tra từ AI
     */
    async suggestExam() {
        if (this.isLoading) return;

        const unit = document.getElementById('aiExamUnit')?.value;
        const duration = document.getElementById('aiExamDuration')?.value;
        const totalQuestions = document.getElementById('aiExamTotalQuestions')?.value;
        const skillLevel = document.getElementById('aiExamSkillLevel')?.value;

        if (!unit) {
            alert('Vui lòng nhập Unit');
            return;
        }

        this.isLoading = true;
        const loading = document.getElementById('aiExamLoading');
        const button = document.querySelector('#aiExamModal button[onclick*="suggestExam"]');
        
        if (loading) loading.classList.remove('hidden');
        if (button) button.disabled = true;

        try {
            const response = await fetch('/api/ai/suggest-exam', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': this.getCsrfToken()
                },
                body: JSON.stringify({
                    type: 'exam',
                    unit: unit,
                    skillLevel: skillLevel || 'B1',
                    duration: parseInt(duration) || 45,
                    totalQuestions: parseInt(totalQuestions) || 20
                })
            });

            const data = await response.json();

            if (data.status === 'success' && data.suggestions && data.suggestions.length > 0) {
                const exam = data.suggestions[0];
                this.fillExamForm(exam);
                this.lastSuggestion = exam;
                this.closeModal('aiExamModal');
                alert('✓ Bài kiểm tra gợi ý đã được tạo. Kiểm tra chi tiết bên dưới.');
            } else {
                alert('Lỗi: ' + (data.message || 'Không thể gợi ý'));
            }
        } catch (error) {
            console.error('Lỗi:', error);
            alert('Lỗi kết nối AI: ' + error.message);
        } finally {
            this.isLoading = false;
            if (loading) loading.classList.add('hidden');
            if (button) button.disabled = false;
        }
    }

    /**
     * Điền dữ liệu gợi ý vào form bài tập
     */
    fillExerciseForm(exercise) {
        // Điền tiêu đề câu hỏi
        const questionTitleElement = document.getElementById('questionTitle');
        if (questionTitleElement) {
            questionTitleElement.value = exercise.question || '';
            questionTitleElement.dispatchEvent(new Event('change'));
        }

        // Chọn loại câu hỏi (nếu có)
        if (exercise.type === 'multiple_choice') {
            const radioButton = document.querySelector('input[name="questionType"][value="MULTIPLE_CHOICE"]');
            if (radioButton) {
                radioButton.checked = true;
                radioButton.dispatchEvent(new Event('change'));
            }
        }

        // Điền các đáp án (nếu là multiple choice)
        if (exercise.options && exercise.options.length > 0) {
            exercise.options.forEach((option, idx) => {
                const optionInput = document.getElementById(`option${idx + 1}`);
                if (optionInput) {
                    optionInput.value = option;
                    optionInput.dispatchEvent(new Event('change'));
                }
            });

            // Chọn đáp án đúng
            if (exercise.correctAnswerIndex !== undefined) {
                const correctRadio = document.querySelector(
                    `input[name="correctAnswer"][value="${exercise.correctAnswerIndex + 1}"]`
                );
                if (correctRadio) {
                    correctRadio.checked = true;
                }
            }
        }

        // Điền giải thích
        const explanationElement = document.getElementById('explanation');
        if (explanationElement) {
            explanationElement.value = exercise.explanation || '';
            explanationElement.dispatchEvent(new Event('change'));
        }

        // Cuộn lên đầu form
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    /**
     * Điền dữ liệu gợi ý vào form bài kiểm tra
     * Note: Cần custom implementation tùy vào cấu trúc form create-exam.html
     */
    fillExamForm(exam) {
        // Điền tiêu đề
        const titleInput = document.querySelector('input[name="title"]');
        if (titleInput) {
            titleInput.value = exam.title || '';
            titleInput.dispatchEvent(new Event('change'));
        }

        // Điền hướng dẫn
        const descriptionInput = document.querySelector('textarea[name="description"]');
        if (descriptionInput) {
            descriptionInput.value = exam.instructions || 'Đọc kỹ câu hỏi trước khi trả lời.';
            descriptionInput.dispatchEvent(new Event('change'));
        }

        // Nếu exam có sections với questions, có thể tự động thêm câu hỏi
        // (tùy cần thiết - có thể bỏ qua nếu giao diện không hỗ trợ)
        
        // Hiển thị thông tin gợi ý
        console.log('Bài kiểm tra gợi ý:', exam);
        
        // Cuộn lên đầu form
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    /**
     * Lấy CSRF token từ meta tag hoặc cookie
     */
    getCsrfToken() {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        if (token) return token;
        
        // Fallback: tìm trong meta tag khác
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        if (header) {
            return document.querySelector(`[${header}]`)?.getAttribute(header);
        }
        
        return '';
    }

    /**
     * Copy suggest data vào clipboard
     */
    copySuggestion() {
        if (!this.lastSuggestion) {
            alert('Chưa có gợi ý nào');
            return;
        }

        const text = JSON.stringify(this.lastSuggestion, null, 2);
        navigator.clipboard.writeText(text).then(() => {
            alert('✓ Dữ liệu gợi ý đã copy');
        }).catch(err => {
            console.error('Lỗi copy:', err);
        });
    }
}

// Tạo instance global
const aiHelper = new AISuggestionHelper();
