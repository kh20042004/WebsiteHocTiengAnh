/**
 * Teacher Assignment Manager
 * Manages creation and editing of assignments
 */

// Prevent duplicate loading
if (typeof AssignmentManager === 'undefined') {

const AssignmentManager = (() => {
    let selectedExercises = [];

    /**
     * Load lessons based on selected unit
     */
    async function loadLessons() {
        const unitId = document.getElementById('unitSelect').value;
        const lessonSelect = document.getElementById('lessonSelect');

        if (!unitId) {
            lessonSelect.innerHTML = '<option value="">-- Chọn Unit trước --</option>';
            return;
        }

        try {
            const response = await fetch(`/api/units/${unitId}/lessons`);
            const result = await response.json();
            
            // Extract lessons from ApiResponseDTO wrapper
            const lessons = result.data || [];

            lessonSelect.innerHTML = '<option value="">-- Tất cả bài tập từ Unit --</option>';
            lessons.forEach(lesson => {
                const option = document.createElement('option');
                option.value = lesson.id;
                option.textContent = `Lesson ${lesson.orderIndex} - ${lesson.title}`;
                lessonSelect.appendChild(option);
            });

            // Load exercises for this unit
            loadExercises();
        } catch (error) {
            NotificationManager.error('Lỗi khi tải danh sách bài học');
            console.error('Error loading lessons:', error);
        }
    }

    /**
     * Load exercises based on selected unit/lesson
     */
    async function loadExercises() {
        const unitId = document.getElementById('unitSelect').value;
        const lessonId = document.getElementById('lessonSelect').value;

        if (!unitId) {
            document.getElementById('exercisesList').innerHTML = 
                '<p class="text-slate-500 text-center py-8">Chọn Unit để xem danh sách bài tập</p>';
            return;
        }

        try {
            let url = `/api/units/${unitId}/exercises`;
            if (lessonId) {
                url = `/api/lessons/${lessonId}/exercises`;
            }

            const response = await fetch(url);
            const result = await response.json();
            
            // Extract exercises from ApiResponseDTO wrapper
            const exercises = result.data || [];

            renderExercises(exercises);
        } catch (error) {
            NotificationManager.error('Lỗi khi tải danh sách bài tập');
            console.error('Error loading exercises:', error);
        }
    }

    /**
     * Render exercises list with checkboxes
     */
    function renderExercises(exercises) {
        const container = document.getElementById('exercisesList');

        if (!exercises || exercises.length === 0) {
            container.innerHTML = '<p class="text-slate-500 text-center py-8">Không có bài tập nào</p>';
            return;
        }

        let html = '';
        exercises.forEach((exercise, index) => {
            const isSelected = selectedExercises.includes(exercise.id);
            const typeColor = getTypeColor(exercise.type);
            
            html += `
                <label class="flex items-start gap-3 p-3 border border-slate-200 rounded-lg cursor-pointer hover:bg-slate-50 transition-colors">
                    <input type="checkbox" class="exercise-checkbox w-4 h-4 mt-1" 
                        value="${exercise.id}" ${isSelected ? 'checked' : ''}
                        onchange="toggleExercise('${exercise.id}')">
                    <div class="flex-1">
                        <div class="flex items-start justify-between gap-2">
                            <div>
                                <p class="font-medium text-slate-900">${exercise.title}</p>
                                <p class="text-sm text-slate-600 mt-1">${exercise.instruction || 'Không có mô tả'}</p>
                            </div>
                            <span class="flex-shrink-0 text-xs px-2 py-1 rounded-full font-medium ${typeColor}">
                                ${exercise.type}
                            </span>
                        </div>
                        <div class="flex gap-4 mt-2 text-xs text-slate-500">
                            <span>📝 ${exercise.totalQuestions || 0} câu</span>
                            <span>⏱️ ${exercise.estimatedDurationMinutes || '?'} phút</span>
                            <span>⭐ ${exercise.maxScore || 0} điểm</span>
                        </div>
                    </div>
                </label>
            `;
        });

        container.innerHTML = html;
    }

    /**
     * Get color class for exercise type
     */
    function getTypeColor(type) {
        const colors = {
            'VOCABULARY': 'bg-purple-100 text-purple-700',
            'GRAMMAR': 'bg-blue-100 text-blue-700',
            'READING': 'bg-amber-100 text-amber-700',
            'LISTENING': 'bg-teal-100 text-teal-700',
            'WRITING': 'bg-pink-100 text-pink-700',
            'SPEAKING': 'bg-green-100 text-green-700'
        };
        return colors[type] || 'bg-slate-100 text-slate-700';
    }

    /**
     * Toggle exercise selection
     */
    function toggleExercise(exerciseId) {
        const index = selectedExercises.indexOf(exerciseId);
        if (index > -1) {
            selectedExercises.splice(index, 1);
        } else {
            if (selectedExercises.length < 20) {
                selectedExercises.push(exerciseId);
            } else {
                NotificationManager.warning('Tối đa 20 bài tập', 'Giới hạn bài tập');
                document.querySelector(`input[value="${exerciseId}"]`).checked = false;
            }
        }
    }

    /**
     * Validate form before submission
     */
    function validateForm() {
        const title = document.getElementById('assignmentTitle').value.trim();
        const type = document.getElementById('assignmentType').value;
        const unitId = document.getElementById('unitSelect').value;
        const deadlineDate = document.getElementById('deadlineDate').value;
        const deadlineTime = document.getElementById('deadlineTime').value;
        const selectedClasses = document.querySelectorAll('input[name="selectedClasses"]:checked').length;

        if (!title) {
            NotificationManager.error('Vui lòng nhập tiêu đề bài tập');
            return false;
        }

        if (title.length > 100) {
            NotificationManager.error('Tiêu đề không được vượt quá 100 ký tự');
            return false;
        }

        if (!unitId) {
            NotificationManager.error('Vui lòng chọn Unit');
            return false;
        }

        if (!type) {
            NotificationManager.error('Vui lòng chọn loại bài tập');
            return false;
        }

        if (selectedExercises.length === 0) {
            NotificationManager.error('Vui lòng chọn ít nhất 1 bài tập');
            return false;
        }

        if (!deadlineDate) {
            NotificationManager.error('Vui lòng chọn ngày hạn nộp');
            return false;
        }

        // Check deadline is in future
        const deadline = new Date(`${deadlineDate}T${deadlineTime}`);
        if (deadline <= new Date()) {
            NotificationManager.error('Ngày hạn nộp phải là ngày trong tương lai');
            return false;
        }

        if (selectedClasses === 0) {
            NotificationManager.error('Vui lòng chọn ít nhất 1 lớp để giao');
            return false;
        }

        return true;
    }

    /**
     * Create assignment
     */
    async function createAssignment(event) {
        event.preventDefault();

        if (!validateForm()) {
            return;
        }

        const title = document.getElementById('assignmentTitle').value.trim();
        const description = document.getElementById('assignmentDescription').value.trim();
        const type = document.getElementById('assignmentType').value;
        const unitId = document.getElementById('unitSelect').value;
        const deadlineDate = document.getElementById('deadlineDate').value;
        const deadlineTime = document.getElementById('deadlineTime').value;
        const gradingMode = document.querySelector('input[name="gradingMode"]:checked').value;
        const selectedClasses = Array.from(
            document.querySelectorAll('input[name="selectedClasses"]:checked')
        ).map(input => input.value);

        // Convert deadline to epoch millis
        const deadlineDateTime = new Date(`${deadlineDate}T${deadlineTime}`);
        const dueDate = deadlineDateTime.getTime();

        const payload = {
            title,
            description,
            type,
            unitId,
            dueDate,
            exerciseIds: selectedExercises,
            gradingMode,
            classroomIds: selectedClasses
        };

        const button = event.target.closest('button');

        try {
            button.disabled = true;
            button.innerHTML = '<iconify-icon icon="mdi:loading" width="18" class="animate-spin"></iconify-icon> Đang tạo...';

            const response = await fetch('/api/assignments', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Có lỗi xảy ra');
            }

            const data = await response.json();
            NotificationManager.success(`Bài tập "${title}" đã được tạo thành công!`);

            // Redirect to assignments page after 1s
            setTimeout(() => {
                window.location.href = '/dashboard/teacher/assignments';
            }, 1000);
        } catch (error) {
            button.disabled = false;
            button.innerHTML = '<iconify-icon icon="mdi:check" width="18"></iconify-icon> Tạo Bài Tập';
            NotificationManager.error(error.message || 'Lỗi khi tạo bài tập');
            console.error('Error creating assignment:', error);
        }
    }

    // Public API
    return {
        loadLessons,
        loadExercises,
        toggleExercise,
        createAssignment
    };
})();

// Expose functions to global scope for inline HTML event handlers
window.loadLessons = AssignmentManager.loadLessons;
window.loadExercises = AssignmentManager.loadExercises;
window.toggleExercise = AssignmentManager.toggleExercise;
window.createAssignment = AssignmentManager.createAssignment;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Set minimum date to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('deadlineDate').setAttribute('min', today);

    // Load units if available
    const unitSelect = document.getElementById('unitSelect');
    if (unitSelect.options.length === 1) {
        // Units will be loaded via Thymeleaf th:each
    }
});

} // Close the if (typeof AssignmentManager === 'undefined') block
