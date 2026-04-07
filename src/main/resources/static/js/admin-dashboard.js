(function () {
    const state = {
        token: localStorage.getItem('token') || localStorage.getItem('accessToken') || '',
        users: [],
        pendingAction: null,
        activeSection: 'users'
    };

    const contentState = {
        items: [],
        stats: null,
        filter: {
            contentType: '',
            status: '',
            keyword: '',
            createdBy: ''
        },
        page: 0,
        size: 20,
        sortBy: 'createdAt',
        sortOrder: 'desc',
        total: 0
    };

    const classroomState = {
        items: [],
        stats: null,
        filter: {
            teacherKeyword: '',
            grade: '',
            status: '',
            keyword: ''
        },
        page: 0,
        size: 10,
        sortBy: 'createdAt',
        sortOrder: 'desc',
        total: 0
    };

    const activityState = {
        items: [],
        page: 0,
        size: 10,
        total: 0,
        lookbackDays: 7
    };

    const learningState = {
        overview: null
    };

    const el = {
        authWarning: document.getElementById('authWarning'),
        totalUsers: document.getElementById('totalUsers'),
        activeUsers: document.getElementById('activeUsers'),
        teacherUsers: document.getElementById('teacherUsers'),
        studentUsers: document.getElementById('studentUsers'),
        newUsersLastWeek: document.getElementById('newUsersLastWeek'),
        totalUnits: document.getElementById('totalUnits'),
        totalClassrooms: document.getElementById('totalClassrooms'),
        totalAssignments: document.getElementById('totalAssignments'),
        totalExams: document.getElementById('totalExams'),
        loadingState: document.getElementById('loadingState'),
        errorState: document.getElementById('errorState'),
        usersTableBody: document.getElementById('usersTableBody'),
        filterForm: document.getElementById('filterForm'),
        roleFilter: document.getElementById('roleFilter'),
        statusFilter: document.getElementById('statusFilter'),
        keywordFilter: document.getElementById('keywordFilter'),
        refreshButton: document.getElementById('refreshButton'),
        toast: document.getElementById('toast'),
        modal: document.getElementById('adminConfirmModal'),
        modalTitle: document.getElementById('modalTitle'),
        modalMessage: document.getElementById('modalMessage'),
        modalConfirm: document.getElementById('modalConfirm'),
        modalCancel: document.getElementById('modalCancel'),
        contentFilterForm: document.getElementById('contentFilterForm'),
        contentTypeFilter: document.getElementById('contentTypeFilter'),
        contentStatusFilter: document.getElementById('contentStatusFilter'),
        contentCreatorFilter: document.getElementById('contentCreatorFilter'),
        contentKeywordFilter: document.getElementById('contentKeywordFilter'),
        contentRefreshButton: document.getElementById('contentRefreshButton'),
        contentLoadingState: document.getElementById('contentLoadingState'),
        contentErrorState: document.getElementById('contentErrorState'),
        contentTableBody: document.getElementById('contentTableBody'),
        contentPageInfo: document.getElementById('contentPageInfo'),
        contentPrevPage: document.getElementById('contentPrevPage'),
        contentNextPage: document.getElementById('contentNextPage'),
        contentStatsTotal: document.getElementById('contentStatsTotal'),
        contentTypeCounts: document.getElementById('contentTypeCounts'),
        contentStatusBreakdown: document.getElementById('contentStatusBreakdown'),
        userPanel: document.getElementById('userPanel'),
        contentPanel: document.getElementById('contentPanel'),
        classroomFilterForm: document.getElementById('classroomFilterForm'),
        classroomTeacherFilter: document.getElementById('classroomTeacherFilter'),
        classroomStatusFilter: document.getElementById('classroomStatusFilter'),
        classroomGradeFilter: document.getElementById('classroomGradeFilter'),
        classroomKeywordFilter: document.getElementById('classroomKeywordFilter'),
        classroomRefreshButton: document.getElementById('classroomRefreshButton'),
        classroomLoadingState: document.getElementById('classroomLoadingState'),
        classroomErrorState: document.getElementById('classroomErrorState'),
        classroomTableBody: document.getElementById('classroomTableBody'),
        classroomPageInfo: document.getElementById('classroomPageInfo'),
        classroomPrevPage: document.getElementById('classroomPrevPage'),
        classroomNextPage: document.getElementById('classroomNextPage'),
        classroomTotal: document.getElementById('classroomTotal'),
        classroomTeacherCount: document.getElementById('classroomTeacherCount'),
        classroomActiveTeacherCount: document.getElementById('classroomActiveTeacherCount'),
        classroomAvgStudents: document.getElementById('classroomAvgStudents'),
        classroomStatusBreakdown: document.getElementById('classroomStatusBreakdown'),
        activitySummaryList: document.getElementById('activitySummaryList'),
        activitySummaryHint: document.getElementById('activitySummaryHint'),
        activityLoadingState: document.getElementById('activityLoadingState'),
        activityErrorState: document.getElementById('activityErrorState'),
        activityTableBody: document.getElementById('activityTableBody'),
        activityPageInfo: document.getElementById('activityPageInfo'),
        activityPrevPage: document.getElementById('activityPrevPage'),
        activityNextPage: document.getElementById('activityNextPage'),
        activityRefreshButton: document.getElementById('activityRefreshButton'),
        activityWindowLabel: document.getElementById('activityWindowLabel'),
        monitoringPanel: document.getElementById('monitoringPanel'),
        learningPanel: document.getElementById('learningPanel'),
        learningRefreshButton: document.getElementById('learningRefreshButton'),
        learningStatsGrid: document.getElementById('learningStatsGrid'),
        learningTopList: document.getElementById('learningTopList'),
        learningLoadingState: document.getElementById('learningLoadingState'),
        learningErrorState: document.getElementById('learningErrorState'),
        classroomPanel: document.getElementById('classroomPanel')
    };

    const navButtons = Array.from(document.querySelectorAll('.admin-nav .nav-link'));

    bindEvents();
    loadAll();

    function bindEvents() {
        el.filterForm.addEventListener('submit', function (event) {
            event.preventDefault();
            loadUsers();
        });

        el.refreshButton.addEventListener('click', function () {
            loadAll();
        });

        el.usersTableBody.addEventListener('click', function (event) {
            const target = event.target;
            if (!(target instanceof HTMLElement)) {
                return;
            }

            const userId = target.dataset.userId;
            if (!userId) {
                return;
            }

            if (target.dataset.action === 'toggle-status') {
                promptToggleStatus(userId);
            }

            if (target.dataset.action === 'change-role') {
                promptChangeRole(userId);
            }

            if (target.dataset.action === 'delete-user') {
                promptDeleteUser(userId);
            }
        });

        el.modalCancel.addEventListener('click', closeModal);
        el.modalConfirm.addEventListener('click', async function () {
            if (typeof state.pendingAction !== 'function') {
                return;
            }
            el.modalConfirm.disabled = true;
            try {
                await state.pendingAction();
                closeModal();
            } catch (error) {
                el.modalConfirm.disabled = false;
            }
        });

        window.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeModal();
            }
        });

        navButtons.forEach(function (button) {
            button.addEventListener('click', function () {
                const section = button.dataset.section;
                if (section) {
                    switchSection(section);
                }
            });
        });

        switchSection(state.activeSection);

        function switchSection(section) {
            if (!section) {
                return;
            }
            state.activeSection = section;
            navButtons.forEach(function (button) {
                button.classList.toggle('active', button.dataset.section === section);
            });
            if (el.userPanel) {
                el.userPanel.classList.toggle('hidden', section !== 'users');
            }
            if (el.contentPanel) {
                el.contentPanel.classList.toggle('hidden', section !== 'content');
            }
            if (el.classroomPanel) {
                el.classroomPanel.classList.toggle('hidden', section !== 'classrooms');
            }
            if (el.monitoringPanel) {
                el.monitoringPanel.classList.toggle('hidden', section !== 'monitoring');
            }
            if (el.learningPanel) {
                el.learningPanel.classList.toggle('hidden', section !== 'learning');
            }
        }

        el.contentFilterForm.addEventListener('submit', function (event) {
            event.preventDefault();
            contentState.page = 0;
            syncContentFilter();
            loadContentList();
        });

        el.contentRefreshButton.addEventListener('click', function () {
            loadContentStats();
            loadContentList();
        });

        el.contentPrevPage.addEventListener('click', function () {
            if (contentState.page > 0) {
                contentState.page -= 1;
                loadContentList();
            }
        });

        el.contentNextPage.addEventListener('click', function () {
            const pageCount = Math.ceil(contentState.total / contentState.size);
            if (contentState.page < pageCount - 1) {
                contentState.page += 1;
                loadContentList();
            }
        });

        el.contentTableBody.addEventListener('click', function (event) {
            const target = event.target;
            if (!(target instanceof HTMLElement)) {
                return;
            }

            if (target.dataset.action === 'toggle-content-status') {
                const contentId = target.dataset.contentId;
                const item = contentState.items.find(function (entry) {
                    return entry.id === contentId;
                });
                if (!item) {
                    showToast('Nội dung không tìm thấy.', true);
                    return;
                }
                promptToggleContentStatus(item);
            }

            if (target.dataset.action === 'delete-content') {
                const contentId = target.dataset.contentId;
                const contentType = target.dataset.contentType;
                const item = contentState.items.find(function (entry) {
                    return entry.id === contentId;
                });
                if (!item) {
                    showToast('Nội dung không tìm thấy.', true);
                    return;
                }
                promptDeleteContent(item);
            }
        });

        el.classroomFilterForm.addEventListener('submit', function (event) {
            event.preventDefault();
            classroomState.page = 0;
            syncClassroomFilter();
            loadClassrooms();
        });

        el.classroomRefreshButton.addEventListener('click', function () {
            loadClassroomStats();
            loadClassrooms();
        });

        el.classroomPrevPage.addEventListener('click', function () {
            if (classroomState.page > 0) {
                classroomState.page -= 1;
                loadClassrooms();
            }
        });

        el.classroomNextPage.addEventListener('click', function () {
            const pageCount = Math.ceil(classroomState.total / classroomState.size);
            if (classroomState.page < pageCount - 1) {
                classroomState.page += 1;
                loadClassrooms();
            }
        });

        el.classroomTableBody.addEventListener('click', function (event) {
            const target = event.target;
            if (!(target instanceof HTMLElement)) {
                return;
            }
            if (target.dataset.action === 'update-classroom-status') {
                const classroomId = target.dataset.classroomId;
                promptUpdateClassroomStatus(classroomId);
            }
            if (target.dataset.action === 'delete-classroom') {
                const classroomId = target.dataset.classroomId;
                promptDeleteClassroom(classroomId);
            }
        });

        if (el.activityRefreshButton) {
            el.activityRefreshButton.addEventListener('click', function () {
                loadActivitySummary();
                loadActivities();
            });
        }

        if (el.activityPrevPage) {
            el.activityPrevPage.addEventListener('click', function () {
                if (activityState.page > 0) {
                    activityState.page -= 1;
                    loadActivities();
                }
            });
        }

        if (el.activityNextPage) {
            el.activityNextPage.addEventListener('click', function () {
                const pageCount = Math.ceil(activityState.total / activityState.size);
                if (activityState.page < pageCount - 1) {
                    activityState.page += 1;
                    loadActivities();
                }
            });
        }

        if (el.learningRefreshButton) {
            el.learningRefreshButton.addEventListener('click', function () {
                loadLearningOverview();
            });
        }
    }

    async function loadAll() {
        showError('');
        setLoading(true);
        try {
            await Promise.all([
                loadStats(),
                loadUsers(),
                loadContentStats(),
                loadContentList(),
                loadClassroomStats(),
                loadClassrooms(),
                loadActivitySummary(),
                loadActivities(),
                loadLearningOverview()
            ]);
        } finally {
            setLoading(false);
        }
    }

    async function loadStats() {
        const response = await apiRequest('/api/admin/stats');
        const stats = response.data || {};
        const users = stats.users || {};
        const content = stats.content || {};

        el.totalUsers.textContent = number(users.totalUsers);
        el.activeUsers.textContent = number(users.activeUsers);
        el.teacherUsers.textContent = number(users.teacherUsers);
        el.studentUsers.textContent = number(users.studentUsers);
        el.newUsersLastWeek.textContent = number(users.newUsersLast7Days);

        el.totalUnits.textContent = 'Units: ' + number(content.totalUnits);
        el.totalClassrooms.textContent = 'Lớp học: ' + number(content.totalClassrooms);
        el.totalAssignments.textContent = 'Bài tập: ' + number(content.totalAssignments);
        el.totalExams.textContent = 'Đề thi: ' + number(content.totalExams);
    }

    async function loadUsers() {
        const params = new URLSearchParams();
        if (el.roleFilter.value) {
            params.set('role', el.roleFilter.value);
        }
        if (el.statusFilter.value !== '') {
            params.set('isActive', el.statusFilter.value);
        }
        if (el.keywordFilter.value.trim()) {
            params.set('keyword', el.keywordFilter.value.trim());
        }

        const query = params.toString();
        const response = await apiRequest('/api/admin/users' + (query ? '?' + query : ''));

        const payload = response.data || {};
        state.users = payload.users || [];
        renderUsers(state.users);
    }

    function renderUsers(users) {
        el.usersTableBody.innerHTML = '';

        if (!users.length) {
            el.usersTableBody.innerHTML = '<tr><td colspan="6">Không có người dùng phù hợp bộ lọc.</td></tr>';
            return;
        }

        users.forEach(function (user) {
            const tr = document.createElement('tr');
            tr.innerHTML = [
                '<td>',
                '<p class="user-name">' + escapeHtml(user.fullName || 'Chưa đặt tên') + '</p>',
                '<p class="user-email">' + escapeHtml(user.email || '') + '</p>',
                '</td>',
                '<td><span class="badge ' + roleClass(user.role) + '">' + escapeHtml(user.role || 'N/A') + '</span></td>',
                '<td><span class="badge ' + (user.isActive ? 'badge-active' : 'badge-inactive') + '">' + (user.isActive ? 'Hoạt động' : 'Đã khóa') + '</span></td>',
                '<td>' + number(user.totalXP) + ' XP<br><small>' + escapeHtml(user.level || 'N/A') + '</small></td>',
                '<td>' + formatDate(user.createdAt) + '</td>',
                '<td>',
                '<div class="actions">',
                '<button class="small-btn toggle" data-action="toggle-status" data-user-id="' + escapeHtml(user.id || '') + '">',
                user.isActive ? 'Khóa' : 'Mở khóa',
                '</button>',
                '<div class="actions-row">',
                '<select id="role-select-' + escapeHtml(user.id || '') + '">',
                optionTag('ADMIN', user.role),
                optionTag('TEACHER', user.role),
                optionTag('STUDENT', user.role),
                '</select>',
                '<button class="small-btn role" data-action="change-role" data-user-id="' + escapeHtml(user.id || '') + '">Đổi quyền</button>',
                '</div>',
                '<button class="small-btn btn-danger" style="margin-top:4px;" data-action="delete-user" data-user-id="' + escapeHtml(user.id || '') + '">Xóa</button>',
                '</div>',
                '</td>'
            ].join('');

            el.usersTableBody.appendChild(tr);
        });
    }

    function promptToggleStatus(userId) {
        const user = state.users.find(function (u) {
            return u.id === userId;
        });
        if (!user) {
            showToast('Không tìm thấy người dùng.', true);
            return;
        }
        const actionLabel = user.isActive ? 'khóa' : 'mở khóa';
        openModal({
            title: 'Xác nhận thay đổi trạng thái',
            message: 'Bạn có chắc muốn ' + actionLabel + ' ' + (user.fullName || user.email) + '?',
            confirmLabel: user.isActive ? 'Khóa' : 'Mở khóa',
            onConfirm: function () {
                return changeUserStatus(userId, !user.isActive);
            }
        });
    }

    async function changeUserStatus(userId, nextStatus) {
        const target = state.users.find(function (u) {
            return u.id === userId;
        });
        if (!target) {
            showToast('Không tìm thấy người dùng.', true);
            return;
        }

        try {
            const response = await apiRequest('/api/admin/users/' + encodeURIComponent(userId) + '/status', {
                method: 'PUT',
                body: JSON.stringify({ isActive: nextStatus })
            });
            showToast(response.message || 'Cập nhật trạng thái thành công.');
            await loadAll();
        } catch (error) {
            handleApiError(error);
            throw error;
        }
    }

    function promptChangeRole(userId) {
        const select = document.getElementById('role-select-' + userId);
        if (!select) {
            return;
        }

        const desiredRole = select.value;
        if (!desiredRole) {
            showToast('Vui lòng chọn quyền mới.', true);
            return;
        }

        openModal({
            title: 'Xác nhận thay đổi quyền',
            message: 'Bạn có chắc muốn cấp ' + desiredRole + ' cho người dùng này?',
            confirmLabel: 'Đổi quyền',
            onConfirm: function () {
                return changeUserRole(userId, desiredRole);
            }
        });
    }

    async function changeUserRole(userId, role) {
        try {
            const response = await apiRequest('/api/admin/users/' + encodeURIComponent(userId) + '/role', {
                method: 'PUT',
                body: JSON.stringify({ role: role })
            });
            showToast(response.message || 'Cập nhật quyền thành công.');
            await loadAll();
        } catch (error) {
            handleApiError(error);
            throw error;
        }
    }

    async function loadContentStats() {
        showContentError('');
        try {
            const response = await apiRequest('/api/admin/content/stats');
            const stats = response.data || {};
            contentState.stats = stats;
            renderContentStats(stats);
        } catch (error) {
            showContentError(error.message);
        }
    }

    async function loadContentList() {
        setContentLoading(true);
        showContentError('');
        try {
            const query = buildContentQuery();
            const response = await apiRequest('/api/admin/content' + (query ? '?' + query : ''));
            const payload = response.data || {};
            contentState.items = Array.isArray(payload.items) ? payload.items : [];
            contentState.page = typeof payload.page === 'number' ? payload.page : contentState.page;
            contentState.size = typeof payload.size === 'number' ? payload.size : contentState.size;
            contentState.total = typeof payload.total === 'number' ? payload.total : contentState.total;
            renderContentRows(contentState.items);
            updateContentPagination();
        } catch (error) {
            renderContentRows([]);
            showContentError(error.message);
        } finally {
            setContentLoading(false);
        }
    }

    function buildContentQuery() {
        const params = new URLSearchParams();
        if (contentState.filter.contentType) {
            params.set('contentType', contentState.filter.contentType);
        }
        if (contentState.filter.status) {
            params.set('status', contentState.filter.status);
        }
        if (contentState.filter.keyword) {
            params.set('keyword', contentState.filter.keyword);
        }
        if (contentState.filter.createdBy) {
            params.set('createdBy', contentState.filter.createdBy);
        }
        params.set('page', contentState.page);
        params.set('size', contentState.size);
        params.set('sortBy', contentState.sortBy);
        params.set('sortOrder', contentState.sortOrder);
        return params.toString();
    }

    function syncContentFilter() {
        contentState.filter.contentType = el.contentTypeFilter.value;
        contentState.filter.status = el.contentStatusFilter.value;
        contentState.filter.keyword = el.contentKeywordFilter.value.trim();
        contentState.filter.createdBy = el.contentCreatorFilter.value.trim();
    }

    function renderContentRows(items) {
        el.contentTableBody.innerHTML = '';

        if (!items.length) {
            el.contentTableBody.innerHTML = '<tr><td colspan="6">Không có nội dung phù hợp bộ lọc.</td></tr>';
            return;
        }

        items.forEach(function (item) {
            const tr = document.createElement('tr');
            const metadataHtml = formatContentMetadata(item);
            tr.innerHTML = [
                '<td>',
                '<p class="content-title">' + escapeHtml(item.title || 'Nội dung không tên') + '</p>',
                '<p class="content-subtitle">' + escapeHtml(item.subtitle || '') + '</p>',
                metadataHtml,
                '</td>',
                '<td><span class="badge badge-role-admin">' + escapeHtml(formatContentType(item.contentType)) + '</span></td>',
                '<td><span class="badge ' + (item.isActive ? 'badge-active' : 'badge-inactive') + '">' + escapeHtml(item.status || (item.isActive ? 'ACTIVE' : 'HIDDEN')) + '</span></td>',
                '<td>' + escapeHtml(item.createdBy || 'Hệ thống') + '</td>',
                '<td>' + formatDate(item.createdAt) + '</td>',
                '<td>',
                '<button class="small-btn toggle" data-action="toggle-content-status" data-content-id="' + escapeHtml(item.id || '') + '" data-content-type="' + escapeHtml(item.contentType || '') + '">',
                item.isActive ? 'Ẩn' : 'Hiển thị',
                '</button>',
                '<button class="small-btn btn-danger" style="margin-left:4px;" data-action="delete-content" data-content-id="' + escapeHtml(item.id || '') + '" data-content-type="' + escapeHtml(item.contentType || '') + '">Xóa</button>',
                '</td>'
            ].join('');

            el.contentTableBody.appendChild(tr);
        });
    }

    function formatContentMetadata(item) {
        const parts = [];
        if (item.unitTitle) {
            parts.push('<span class="content-meta">Unit: ' + escapeHtml(item.unitTitle) + '</span>');
        }
        if (item.lessonTitle) {
            parts.push('<span class="content-meta">Bài học: ' + escapeHtml(item.lessonTitle) + '</span>');
        }
        if (item.metadata) {
            Object.entries(item.metadata).forEach(function ([key, value]) {
                if (value == null) {
                    return;
                }
                parts.push('<span class="content-meta">' + escapeHtml(humanize(key)) + ': ' + escapeHtml(String(value)) + '</span>');
            });
        }
        if (!parts.length) {
            return '<p class="content-meta-empty">&nbsp;</p>';
        }
        return '<div class="content-meta-list">' + parts.join('') + '</div>';
    }

    function promptToggleContentStatus(item) {
        const actionLabel = item.isActive ? 'ẩn' : 'hiển thị';
        openModal({
            title: 'Xác nhận trạng thái nội dung',
            message: 'Bạn có muốn ' + actionLabel + ' "' + (item.title || 'nội dung') + '"?',
            confirmLabel: item.isActive ? 'Ẩn' : 'Hiển thị',
            onConfirm: function () {
                return updateContentStatus(item);
            }
        });
    }

    async function updateContentStatus(item) {
        const nextState = !item.isActive;
        try {
            const response = await apiRequest(
                '/api/admin/content/' + encodeURIComponent(item.contentType) + '/' + encodeURIComponent(item.id) + '/status',
                {
                    method: 'PUT',
                    body: JSON.stringify({ isActive: nextState })
                }
            );
            showToast(response.message || 'Cập nhật nội dung thành công.');
            await Promise.all([
                loadContentStats(),
                loadContentList()
            ]);
        } catch (error) {
            handleApiError(error);
            throw error;
        }
    }

    function renderContentStats(stats) {
        if (!stats) {
            return;
        }
        el.contentStatsTotal.textContent = number(stats.total);
        renderStatList(stats.counts, el.contentTypeCounts);
        renderStatList(stats.statusBreakdown, el.contentStatusBreakdown);
    }

    function renderStatList(source, container) {
        container.innerHTML = '';
        if (!source || typeof source !== 'object') {
            return;
        }
        Object.entries(source).forEach(function ([key, value]) {
            const badge = document.createElement('span');
            badge.className = 'content-stat-item';
            badge.textContent = humanize(key) + ': ' + number(value);
            container.appendChild(badge);
        });
    }

    async function loadClassroomStats() {
        try {
            const response = await apiRequest('/api/admin/classrooms/stats');
            const stats = response.data || {};
            classroomState.stats = stats;
            renderClassroomStats(stats);
        } catch (error) {
            showClassroomError(error.message);
        }
    }

    async function loadClassrooms() {
        setClassroomLoading(true);
        showClassroomError('');
        try {
            const query = buildClassroomQuery();
            const response = await apiRequest('/api/admin/classrooms' + (query ? '?' + query : ''));
            const payload = response.data || {};
            classroomState.items = Array.isArray(payload.items) ? payload.items : [];
            classroomState.page = typeof payload.page === 'number' ? payload.page : classroomState.page;
            classroomState.size = typeof payload.size === 'number' ? payload.size : classroomState.size;
            classroomState.total = typeof payload.total === 'number' ? payload.total : classroomState.total;
            renderClassroomRows(classroomState.items);
            updateClassroomPagination();
        } catch (error) {
            renderClassroomRows([]);
            showClassroomError(error.message);
        } finally {
            setClassroomLoading(false);
        }
    }

    function buildClassroomQuery() {
        const params = new URLSearchParams();
        if (classroomState.filter.teacherKeyword) {
            params.set('teacherKeyword', classroomState.filter.teacherKeyword);
        }
        if (classroomState.filter.status) {
            params.set('status', classroomState.filter.status);
        }
        if (classroomState.filter.grade) {
            params.set('grade', classroomState.filter.grade);
        }
        if (classroomState.filter.keyword) {
            params.set('keyword', classroomState.filter.keyword);
        }
        params.set('page', classroomState.page);
        params.set('size', classroomState.size);
        params.set('sortBy', classroomState.sortBy);
        params.set('sortOrder', classroomState.sortOrder);
        return params.toString();
    }

    function syncClassroomFilter() {
        classroomState.filter.teacherKeyword = el.classroomTeacherFilter.value.trim();
        classroomState.filter.status = el.classroomStatusFilter.value;
        classroomState.filter.grade = el.classroomGradeFilter.value.trim();
        classroomState.filter.keyword = el.classroomKeywordFilter.value.trim();
    }

    function renderClassroomRows(items) {
        el.classroomTableBody.innerHTML = '';
        if (!items.length) {
            el.classroomTableBody.innerHTML = '<tr><td colspan="6">Không có lớp học phù hợp bộ lọc.</td></tr>';
            return;
        }

        items.forEach(function (item) {
            const tr = document.createElement('tr');
            const metaParts = [];
            if (item.classCode) {
                metaParts.push('<p class="classroom-meta-line">Mã: ' + escapeHtml(item.classCode) + '</p>');
            }
            if (item.schedule) {
                metaParts.push('<p class="classroom-meta-line">' + escapeHtml(item.schedule) + '</p>');
            }
            const teacherLabel = escapeHtml(item.teacherName || 'Chưa gán giáo viên');
            const teacherEmail = escapeHtml(item.teacherEmail || '');
            const gradeText = escapeHtml(item.grade || '—');
            const statusBadge = item.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive';
            const statusLabel = formatClassroomStatus(item.status);
            const maxStudents = item.maxStudents != null ? number(item.maxStudents) : '—';
            const currentStudents = number(item.studentCount);
            const optionsHtml = buildClassroomStatusOptions(item.status);

            tr.innerHTML = [
                '<td>',
                '<p class="classroom-title">' + escapeHtml(item.name || 'Lớp chưa đặt tên') + '</p>',
                metaParts.join(''),
                '</td>',
                '<td>',
                '<p class="classroom-meta-line">' + teacherLabel + '</p>',
                '<p class="classroom-meta-line">' + teacherEmail + '</p>',
                '</td>',
                '<td>' + gradeText + '</td>',
                '<td><span class="badge ' + statusBadge + '">' + escapeHtml(statusLabel) + '</span></td>',
                '<td>' + currentStudents + ' / ' + maxStudents + '</td>',
                '<td>',
                '<div class="classroom-actions">',
                '<select id="classroom-status-select-' + escapeHtml(item.id || '') + '" class="classroom-status-select" data-classroom-id="' + escapeHtml(item.id || '') + '">',
                optionsHtml,
                '</select>',
                '<button data-action="update-classroom-status" data-classroom-id="' + escapeHtml(item.id || '') + '">Cập nhật</button>',
                '<button class="btn btn-danger" style="padding:4px 8px;font-size:0.875rem;" data-action="delete-classroom" data-classroom-id="' + escapeHtml(item.id || '') + '">Xóa</button>',
                '</div>',
                '</td>'
            ].join('');

            el.classroomTableBody.appendChild(tr);
        });
    }

    function renderClassroomStats(stats) {
        if (!stats) {
            return;
        }
        el.classroomTotal.textContent = number(stats.totalClassrooms);
        el.classroomTeacherCount.textContent = number(stats.totalTeachers);
        el.classroomActiveTeacherCount.textContent = 'Trong đó ' + number(stats.activeTeachers) + ' đang hoạt động';
        el.classroomAvgStudents.textContent = formatDecimal(stats.averageStudents) + ' học sinh';
        renderClassroomStatusBreakdown(stats.statusCounts);
    }

    function renderClassroomStatusBreakdown(counts) {
        el.classroomStatusBreakdown.innerHTML = '';
        if (!counts || typeof counts !== 'object') {
            return;
        }
        Object.entries(counts).forEach(function ([key, value]) {
            const badge = document.createElement('span');
            badge.className = 'content-stat-item';
            badge.textContent = formatClassroomStatus(key) + ': ' + number(value);
            el.classroomStatusBreakdown.appendChild(badge);
        });
    }

    async function loadActivitySummary() {
        if (!el.activitySummaryList) {
            return;
        }
        try {
            const response = await apiRequest('/api/admin/classrooms/activities/summary?days=' + activityState.lookbackDays);
            const summary = Array.isArray(response.data) ? response.data : [];
            renderActivitySummary(summary);
        } catch (error) {
            el.activitySummaryList.innerHTML = '<li>Không tải được báo cáo hoạt động.</li>';
            if (el.activitySummaryHint) {
                el.activitySummaryHint.textContent = error.message || 'Không tải được báo cáo hoạt động.';
            }
        }
    }

    async function loadActivities() {
        if (!el.activityTableBody) {
            return;
        }
        setActivityLoading(true);
        showActivityError('');
        try {
            const query = buildActivityQuery();
            const response = await apiRequest('/api/admin/classrooms/activities' + (query ? '?' + query : ''));
            const payload = response.data || {};
            activityState.items = Array.isArray(payload.items) ? payload.items : [];
            activityState.page = typeof payload.page === 'number' ? payload.page : activityState.page;
            activityState.size = typeof payload.size === 'number' ? payload.size : activityState.size;
            activityState.total = typeof payload.total === 'number' ? payload.total : activityState.total;
            renderActivityRows(activityState.items);
            updateActivityPagination();
        } catch (error) {
            renderActivityRows([]);
            showActivityError(error.message || 'Không tải được dữ liệu hoạt động.');
        } finally {
            setActivityLoading(false);
        }
    }

    async function loadLearningOverview() {
        if (!el.learningStatsGrid) {
            return;
        }
        setLearningLoading(true);
        showLearningError('');
        try {
            const response = await apiRequest('/api/admin/learning-data/overview');
            const overview = response.data || null;
            learningState.overview = overview;
            renderLearningOverview(overview);
        } catch (error) {
            renderLearningOverview(null);
            showLearningError(error.message || 'Không tải được dữ liệu học tập.');
        } finally {
            setLearningLoading(false);
        }
    }

    function buildActivityQuery() {
        const params = new URLSearchParams();
        params.set('page', activityState.page);
        params.set('size', activityState.size);
        return params.toString();
    }

    function renderActivityRows(items) {
        el.activityTableBody.innerHTML = '';
        if (!items.length) {
            el.activityTableBody.innerHTML = '<tr><td colspan="5">Không có hoạt động trong khoảng thời gian này.</td></tr>';
            return;
        }
        items.forEach(function (item) {
            const tr = document.createElement('tr');
            const adminName = item.adminName;
            const adminEmail = item.adminEmail;
            const adminId = item.adminId;
            const adminLines = [];
            if (adminName) {
                adminLines.push('<p class="activity-admin-name">' + escapeHtml(adminName) + '</p>');
            }
            if (adminEmail) {
                adminLines.push('<p class="activity-admin-email">' + escapeHtml(adminEmail) + '</p>');
            }
            if (!adminLines.length) {
                adminLines.push('<p class="activity-admin-id">' + escapeHtml(adminId || '—') + '</p>');
            } else if (adminId) {
                adminLines.push('<p class="activity-admin-id">ID: ' + escapeHtml(adminId) + '</p>');
            }
            tr.innerHTML = [
                '<td class="activity-admin-cell">' + adminLines.join('') + '</td>',
                '<td>' + escapeHtml(item.action ? humanize(item.action) : '—') + '</td>',
                '<td><strong>' + escapeHtml(item.targetType || '—') + '</strong><br>' + escapeHtml(item.targetId || '') + '</td>',
                '<td>' + formatActivityMetadata(item.metadata) + '</td>',
                '<td>' + formatDate(item.createdAt) + '</td>'
            ].join('');
            el.activityTableBody.appendChild(tr);
        });
    }

    function renderLearningOverview(overview) {
        if (!el.learningStatsGrid) {
            return;
        }
        if (!overview) {
            el.learningStatsGrid.innerHTML = '<p>Không có dữ liệu học tập để hiển thị.</p>';
            renderLearningTopList([]);
            return;
        }
        const stats = [
            {
                label: 'Bản ghi tiến độ',
                value: number(overview.lessonProgressRecords),
                hint: 'Số dòng LessonProgress'
            },
            {
                label: 'Học sinh theo dõi',
                value: number(overview.trackedStudents),
                hint: 'Học sinh có tiến độ'
            },
            {
                label: 'Bài học hoàn thành',
                value: number(overview.completedLessons),
                hint: 'Chỉ đếm trạng thái COMPLETED'
            },
            {
                label: 'Tiến độ trung bình',
                value: formatDecimal(overview.averageLessonProgress) + '%',
                hint: 'Trung bình % của các LessonProgress'
            },
            {
                label: 'Bài tập đã gửi',
                value: number(overview.totalExerciseSubmissions),
                hint: 'ExerciseSubmission tổng'
            },
            {
                label: 'Điểm trung bình',
                value: formatDecimal(overview.averageExerciseScorePercent) + '%',
                hint: 'Tính từ bài tập COMPLETED'
            },
            {
                label: 'Thời gian học',
                value: formatDuration(overview.totalStudyTimeMs),
                hint: 'Tổng thời gian học tính bằng phút'
            }
        ];
        el.learningStatsGrid.innerHTML = stats.map(function (stat) {
            return '<article class="stat-card learning-stat">' +
                '<h3>' + stat.label + '</h3>' +
                '<p>' + stat.value + '</p>' +
                '<small>' + stat.hint + '</small>' +
                '</article>';
        }).join('');
        renderLearningTopList(overview.topStudents || []);
    }

    function renderLearningTopList(students) {
        if (!el.learningTopList) {
            return;
        }
        el.learningTopList.innerHTML = '';
        if (!students.length) {
            el.learningTopList.innerHTML = '<li>Chưa có dữ liệu học sinh nổi bật.</li>';
            return;
        }
        students.forEach(function (student, index) {
            const li = document.createElement('li');
            li.className = 'learning-top-item';
            const name = student.fullName || student.studentId || 'Học sinh';
            const progress = typeof student.averageProgress === 'number' ? formatDecimal(student.averageProgress) + '%' : '—';
            li.innerHTML = [
                '<span class="learning-top-rank">' + (index + 1) + '</span>',
                '<div class="learning-top-content">',
                '<strong>' + escapeHtml(name) + '</strong>',
                '<span>ID: ' + escapeHtml(student.studentId || '—') + ' • ' + progress + '</span>',
                '</div>'
            ].join('');
            el.learningTopList.appendChild(li);
        });
    }

    function setLearningLoading(isLoading) {
        if (el.learningLoadingState) {
            el.learningLoadingState.classList.toggle('hidden', !isLoading);
        }
        if (el.learningRefreshButton) {
            el.learningRefreshButton.disabled = isLoading;
        }
    }

    function showLearningError(message) {
        if (!el.learningErrorState) {
            return;
        }
        if (!message) {
            el.learningErrorState.classList.add('hidden');
            el.learningErrorState.textContent = '';
            return;
        }
        el.learningErrorState.textContent = message;
        el.learningErrorState.classList.remove('hidden');
    }

    function renderActivitySummary(summary) {
        if (!el.activitySummaryList) {
            return;
        }
        el.activitySummaryList.innerHTML = '';
        if (!summary.length) {
            el.activitySummaryList.innerHTML = '<li>Chưa có hành động nào được ghi nhận trong ' + activityState.lookbackDays + ' ngày gần đây.</li>';
        } else {
            summary.forEach(function (entry) {
                const li = document.createElement('li');
                li.className = 'content-stat-item activity-summary-item';
                li.innerHTML = '<span>' + escapeHtml(entry.action ? humanize(entry.action) : 'Không rõ') + '</span>' +
                    '<strong>' + number(entry.count) + '</strong>';
                el.activitySummaryList.appendChild(li);
            });
        }
        if (el.activityWindowLabel) {
            el.activityWindowLabel.textContent = activityState.lookbackDays + ' ngày';
        }
        if (el.activitySummaryHint) {
            el.activitySummaryHint.textContent = 'Dữ liệu cập nhật theo cửa sổ ' + activityState.lookbackDays + ' ngày gần nhất.';
        }
    }

    function formatActivityMetadata(metadata) {
        if (!metadata || typeof metadata !== 'object') {
            return '—';
        }
        const pairs = [];
        Object.entries(metadata).forEach(function ([key, value]) {
            if (value == null) {
                return;
            }
            pairs.push('<span>' + escapeHtml(humanize(key)) + ': ' + escapeHtml(String(value)) + '</span>');
        });
        return pairs.length ? pairs.join('<br>') : '—';
    }

    function updateActivityPagination() {
        const pageCount = Math.max(1, Math.ceil(activityState.total / activityState.size));
        if (el.activityPageInfo) {
            el.activityPageInfo.textContent = 'Trang ' + (activityState.page + 1) + ' / ' + pageCount;
        }
        if (el.activityPrevPage) {
            el.activityPrevPage.disabled = activityState.page <= 0;
        }
        if (el.activityNextPage) {
            el.activityNextPage.disabled = activityState.page >= pageCount - 1;
        }
    }

    function setActivityLoading(isLoading) {
        if (el.activityLoadingState) {
            el.activityLoadingState.classList.toggle('hidden', !isLoading);
        }
        if (el.activityPrevPage) {
            el.activityPrevPage.disabled = isLoading || activityState.page <= 0;
        }
        if (el.activityNextPage) {
            el.activityNextPage.disabled = isLoading;
        }
        if (el.activityRefreshButton) {
            el.activityRefreshButton.disabled = isLoading;
        }
    }

    function showActivityError(message) {
        if (!el.activityErrorState) {
            return;
        }
        if (!message) {
            el.activityErrorState.classList.add('hidden');
            el.activityErrorState.textContent = '';
            return;
        }
        el.activityErrorState.textContent = message;
        el.activityErrorState.classList.remove('hidden');
    }

    function buildClassroomStatusOptions(current) {
        const statuses = ['ACTIVE', 'UPCOMING', 'COMPLETED'];
        const normalized = (current || '').toString().toUpperCase();
        const options = statuses.map(function (value) {
            const label = formatClassroomStatus(value);
            const selected = value === normalized ? ' selected' : '';
            return '<option value="' + value + '"' + selected + '>' + escapeHtml(label) + '</option>';
        }).join('');
        if (current && !statuses.includes(normalized)) {
            return options + '<option value="' + escapeHtml(current) + '" selected>' + escapeHtml(formatClassroomStatus(current)) + '</option>';
        }
        return options;
    }

    function updateClassroomPagination() {
        const pageCount = Math.max(1, Math.ceil(classroomState.total / classroomState.size));
        el.classroomPageInfo.textContent = 'Trang ' + (classroomState.page + 1) + ' / ' + pageCount;
        el.classroomPrevPage.disabled = classroomState.page <= 0;
        el.classroomNextPage.disabled = classroomState.page >= pageCount - 1;
    }

    function setClassroomLoading(isLoading) {
        el.classroomLoadingState.classList.toggle('hidden', !isLoading);
        el.classroomRefreshButton.disabled = isLoading;
        el.classroomPrevPage.disabled = isLoading || classroomState.page <= 0;
        el.classroomNextPage.disabled = isLoading;
    }

    function showClassroomError(message) {
        if (!message) {
            el.classroomErrorState.classList.add('hidden');
            el.classroomErrorState.textContent = '';
            return;
        }
        el.classroomErrorState.textContent = message;
        el.classroomErrorState.classList.remove('hidden');
    }

    function promptUpdateClassroomStatus(classroomId) {
        if (!classroomId) {
            return;
        }
        const select = document.getElementById('classroom-status-select-' + classroomId);
        const item = classroomState.items.find(function (entry) {
            return entry.id === classroomId;
        });
        if (!select || !item) {
            showToast('Không tìm thấy lớp học.', true);
            return;
        }
        const desiredStatus = select.value;
        if (!desiredStatus) {
            showToast('Vui lòng chọn trạng thái mới.', true);
            return;
        }
        openModal({
            title: 'Xác nhận trạng thái lớp học',
            message: 'Bạn có chắc muốn đặt lớp "' + (item.name || 'lớp học') + '" thành ' + formatClassroomStatus(desiredStatus) + '?',
            confirmLabel: 'Cập nhật',
            onConfirm: function () {
                return updateClassroomStatus(classroomId, desiredStatus);
            }
        });
    }

    async function updateClassroomStatus(classroomId, newStatus) {
        try {
            const response = await apiRequest('/api/admin/classrooms/' + encodeURIComponent(classroomId) + '/status', {
                method: 'PUT',
                body: JSON.stringify({ status: newStatus })
            });
            showToast(response.message || 'Cập nhật lớp học thành công.');
            await Promise.all([
                loadClassroomStats(),
                loadClassrooms()
            ]);
        } catch (error) {
            handleApiError(error);
            throw error;
        }
    }

    function humanize(value) {
        return String(value)
            .replace(/_/g, ' ')
            .toLowerCase()
            .split(' ')
            .map(function (segment) {
                return segment.charAt(0).toUpperCase() + segment.slice(1);
            })
            .join(' ');
    }

    function formatContentType(type) {
        if (!type) {
            return 'N/A';
        }
        return type.charAt(0).toUpperCase() + type.slice(1).toLowerCase();
    }

    function updateContentPagination() {
        const pageCount = Math.max(1, Math.ceil(contentState.total / contentState.size));
        el.contentPageInfo.textContent = 'Trang ' + (contentState.page + 1) + ' / ' + pageCount;
        el.contentPrevPage.disabled = contentState.page <= 0;
        el.contentNextPage.disabled = contentState.page >= pageCount - 1;
    }

    function setContentLoading(isLoading) {
        el.contentLoadingState.classList.toggle('hidden', !isLoading);
        el.contentRefreshButton.disabled = isLoading;
        el.contentPrevPage.disabled = isLoading || contentState.page <= 0;
        el.contentNextPage.disabled = isLoading;
    }

    function showContentError(message) {
        if (!message) {
            el.contentErrorState.classList.add('hidden');
            el.contentErrorState.textContent = '';
            return;
        }
        el.contentErrorState.textContent = message;
        el.contentErrorState.classList.remove('hidden');
    }

    function openModal(config) {
        state.pendingAction = config.onConfirm;
        el.modalTitle.textContent = config.title;
        el.modalMessage.textContent = config.message;
        el.modalConfirm.textContent = config.confirmLabel || 'Xác nhận';
        el.modalConfirm.disabled = false;
        el.modal.classList.remove('hidden');
        el.modal.setAttribute('aria-hidden', 'false');
    }

    function closeModal() {
        state.pendingAction = null;
        el.modal.classList.add('hidden');
        el.modal.setAttribute('aria-hidden', 'true');
    }

    async function apiRequest(url, options) {
        const config = options || {};
        const headers = Object.assign({}, config.headers || {});
        if (state.token) {
            headers.Authorization = 'Bearer ' + state.token;
        }

        if (config.body) {
            headers['Content-Type'] = 'application/json';
        }

        const response = await fetch(url, {
            method: config.method || 'GET',
            headers: headers,
            body: config.body,
            credentials: 'same-origin'
        });

        let payload;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok) {
            const message = (payload && (payload.message || payload.error)) || 'Yêu cầu thất bại';
            throw new Error(message);
        }

        if (!payload) {
            throw new Error('Phản hồi không hợp lệ từ máy chủ.');
        }

        return payload;
    }

    function setLoading(isLoading) {
        el.loadingState.classList.toggle('hidden', !isLoading);
        el.refreshButton.disabled = isLoading;
    }

    function showError(message) {
        if (!message) {
            el.errorState.classList.add('hidden');
            el.errorState.textContent = '';
            return;
        }

        el.errorState.textContent = message;
        el.errorState.classList.remove('hidden');
    }

    function handleApiError(error) {
        showError(error.message || 'Có lỗi xảy ra.');
        showToast(error.message || 'Có lỗi xảy ra.', true);
    }

    function showToast(message, isError) {
        el.toast.textContent = message;
        el.toast.classList.remove('hidden');
        el.toast.style.background = isError ? '#b12538' : '#0b4fb2';

        window.clearTimeout(showToast.timer);
        showToast.timer = window.setTimeout(function () {
            el.toast.classList.add('hidden');
        }, 2800);
    }

    function number(value) {
        return new Intl.NumberFormat('vi-VN').format(value || 0);
    }

    function formatDate(timestamp) {
        if (!timestamp) {
            return 'N/A';
        }
        const date = new Date(timestamp);
        if (Number.isNaN(date.getTime())) {
            return 'N/A';
        }
        return date.toLocaleDateString('vi-VN');
    }

    function formatDecimal(value) {
        if (value == null || Number.isNaN(value)) {
            return '0';
        }
        return new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 1 }).format(value);
    }

    function formatDuration(ms) {
        if (!ms || ms <= 0) {
            return '0 phút';
        }
        const minutes = Math.floor(ms / 60000);
        if (minutes < 60) {
            return minutes + ' phút';
        }
        const hours = Math.floor(minutes / 60);
        const remaining = minutes % 60;
        return hours + ' giờ ' + remaining + ' phút';
    }

    function roleClass(role) {
        if (role === 'ADMIN') {
            return 'badge-role-admin';
        }
        if (role === 'TEACHER') {
            return 'badge-role-teacher';
        }
        return 'badge-role-student';
    }

    function formatClassroomStatus(status) {
        if (!status) {
            return 'Chưa xác định';
        }
        switch (status.toUpperCase()) {
            case 'ACTIVE':
                return 'Đang hoạt động';
            case 'UPCOMING':
                return 'Sắp khai giảng';
            case 'COMPLETED':
                return 'Đã kết thúc';
            default:
                return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
        }
    }

    function optionTag(value, currentRole) {
        const selected = value === currentRole ? ' selected' : '';
        return '<option value="' + value + '"' + selected + '>' + value + '</option>';
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function promptDeleteUser(userId) {
        const user = state.users.find(function (u) {
            return u.id === userId;
        });
        if (!user) {
            showToast('Không tìm thấy người dùng.', true);
            return;
        }
        openModal({
            title: 'Xóa người dùng',
            message: 'Ban có chắc muốn xóa người dùng ' + (user.fullName || user.email) + ' vĩnh viễn không?',
            confirmLabel: 'Xóa',
            onConfirm: function () {
                return deleteUser(userId);
            }
        });
    }

    async function deleteUser(userId) {
        try {
            const response = await apiRequest('/api/admin/users/' + encodeURIComponent(userId), {
                method: 'DELETE'
            });
            showToast(response.message || 'Xóa người dùng thành công.');
            await loadAll();
        } catch (error) {
            handleApiError(error);
            throw error;
        }
    }

    function promptDeleteContent(item) {
        openModal({
            title: 'Xóa nội dung',
            message: 'Bạn có chắc muốn xóa vĩnh viễn "' + (item.title || 'nội dung') + '" không?',
            confirmLabel: 'Xóa',
            onConfirm: function () {
                return deleteContent(item);
            }
        });
    }

    async function deleteContent(item) {
        try {
            const response = await apiRequest(
                '/api/admin/content/' + encodeURIComponent(item.contentType) + '/' + encodeURIComponent(item.id),
                {
                    method: 'DELETE'
                }
            );
            showToast(response.message || 'Xóa nội dung thành công.');
            await Promise.all([
                loadContentStats(),
                loadContentList()
            ]);
        } catch (error) {
            handleApiError(error);
            throw error;
        }
    }

    function promptDeleteClassroom(classroomId) {
        const classroom = classroomState.items.find(c => c.id === classroomId);
        if(!classroom) return;
        openModal({
            title: 'Xóa lớp học',
            message: 'Bạn có chắc chắn muốn xóa vĩnh viễn lớp "' + (classroom.name || 'chưa đặt tên') + '" không?',
            confirmLabel: 'Xóa',
            onConfirm: function () {
                return deleteClassroom(classroomId);
            }
        });
    }

    async function deleteClassroom(classroomId) {
        try {
            const response = await apiRequest(
                '/api/admin/classrooms/' + encodeURIComponent(classroomId),
                {
                    method: 'DELETE'
                }
            );
            showToast(response.message || 'Xóa lớp học thành công.');
            await Promise.all([
                loadClassroomStats(),
                loadClassrooms()
            ]);
        } catch (error) {
            handleApiError(error);
            throw error;
        }
    }
})();
