import { ApiService } from './core/api.js';
import { renderNavbar } from './core/Navbar.js';
import { renderStatsCards } from './modules/task/StatsCard.js';
import { renderTaskFilter } from './modules/task/TaskFilter.js';
import { renderTaskCard } from './modules/task/TaskCard.js';
import { renderTaskModal } from './modules/task/TaskModal.js';
import { renderKanbanBoard } from './modules/kanban/KanbanBoard.js';
import { renderAuthModal } from './modules/auth/AuthModal.js';
import { renderProfileModal } from './modules/auth/ProfileModal.js';
import { renderAuditLogDrawer } from './modules/audit/AuditLogDrawer.js';
import { renderChartDashboard } from './modules/analytics/ChartDashboard.js';
import { createNotificationModal } from './modules/notification/NotificationModal.js';

class Application {
    constructor() {
        this.appContainer = document.getElementById('app');
        this.currentUser = null;
        this.currentView = 'kanban';
        this.categories = [];
        this.tasks = [];
        this.stats = {};
        this.currentFilter = {};
        this.unreadNotifications = 0;
        this.sseEventSource = null;
    }

    async init() {
        try {
            // Check Auth Token & Current User
            this.currentUser = await ApiService.getMe();

            // Initialize Modals & Drawers
            this.authModal = renderAuthModal({
                onLoginSuccess: (user) => {
                    this.currentUser = user;
                    this.refreshAll();
                }
            });
            document.body.appendChild(this.authModal.element);

            this.profileModal = renderProfileModal({
                currentUser: this.currentUser,
                onProfileUpdated: (updatedUser) => {
                    this.currentUser = updatedUser;
                    this.refreshAll();
                }
            });
            document.body.appendChild(this.profileModal.element);

            this.notificationModal = createNotificationModal({
                onNotificationRead: () => this.refreshNotificationCount()
            });
            document.body.appendChild(this.notificationModal.element);

            this.auditLogDrawer = renderAuditLogDrawer();
            document.body.appendChild(this.auditLogDrawer.element);

            this.chartDashboard = renderChartDashboard();
            document.body.appendChild(this.chartDashboard.element);

            this.categories = await ApiService.getCategories();

            this.taskModal = renderTaskModal(this.categories, (data) => this.handleSaveTask(data));
            document.body.appendChild(this.taskModal.element);

            // If not logged in, show Auth Modal automatically
            if (!this.currentUser) {
                this.authModal.open();
            } else {
                this.initSseStream();
            }

            // Build UI Layout
            this.renderLayout();
            await this.refreshData();

        } catch (err) {
            console.error("Initialization error:", err);
        }
    }

    initSseStream() {
        if (!this.currentUser) return;
        if (this.sseEventSource) this.sseEventSource.close();

        try {
            this.sseEventSource = new EventSource('/api/notifications/stream');
            this.sseEventSource.addEventListener('NOTIFICATION', (event) => {
                const dto = JSON.parse(event.data);
                this.refreshNotificationCount();
                if (this.notificationModal) this.notificationModal.loadNotifications();
            });
        } catch (e) {
            console.warn("SSE connection error:", e);
        }
    }

    async refreshNotificationCount() {
        if (this.currentUser) {
            this.unreadNotifications = await ApiService.getUnreadNotificationCount();
            this.renderLayout();
        }
    }

    renderLayout() {
        this.appContainer.innerHTML = '';

        // Render Navbar
        const navbar = renderNavbar({
            currentUser: this.currentUser,
            currentView: this.currentView,
            unreadNotifications: this.unreadNotifications,
            onViewChange: (view) => {
                this.currentView = view;
                this.renderLayout();
                this.renderContent();
            },
            onNewTaskClick: () => this.taskModal.open(),
            onNotificationClick: () => this.notificationModal.show(),
            onAuditLogClick: () => this.auditLogDrawer.open(),
            onAnalyticsClick: () => this.chartDashboard.open(),
            onProfileClick: () => this.profileModal.open(this.currentUser),
            onAuthClick: () => this.authModal.open(),
            onLogoutClick: () => {
                ApiService.logout();
                this.currentUser = null;
                if (this.sseEventSource) this.sseEventSource.close();
                this.refreshAll();
            }
        });
        this.appContainer.appendChild(navbar);

        // Stats Container Slot
        this.statsSlot = document.createElement('div');
        this.appContainer.appendChild(this.statsSlot);

        // Filter Bar Slot
        const filterBar = renderTaskFilter(this.categories, (filters) => this.handleFilterChange(filters));
        this.appContainer.appendChild(filterBar);

        // Content Area Slot (Grid or Kanban)
        this.contentSlot = document.createElement('div');
        this.appContainer.appendChild(this.contentSlot);
    }

    async refreshAll() {
        this.categories = await ApiService.getCategories();
        if (this.currentUser) {
            this.unreadNotifications = await ApiService.getUnreadNotificationCount();
            this.initSseStream();
        }
        this.renderLayout();
        await this.refreshData();
    }

    async refreshData() {
        if (!this.currentUser) {
            this.stats = {};
            this.tasks = [];
            this.renderStats();
            this.renderContent();
            return;
        }

        this.stats = await ApiService.getStats();
        this.tasks = await ApiService.getTasks(this.currentFilter);
        this.unreadNotifications = await ApiService.getUnreadNotificationCount();
        this.renderStats();
        this.renderContent();
    }

    renderStats() {
        if (this.statsSlot) {
            this.statsSlot.innerHTML = '';
            this.statsSlot.appendChild(renderStatsCards(this.stats));
        }
    }

    renderContent() {
        if (!this.contentSlot) return;
        this.contentSlot.innerHTML = '';

        if (!this.currentUser) {
            this.contentSlot.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🔒</div>
                    <h3>Yêu cầu đăng nhập</h3>
                    <p>Vui lòng đăng nhập hoặc đăng ký tài khoản để xem và quản lý công việc của bạn!</p>
                </div>
            `;
            return;
        }

        if (this.currentView === 'kanban') {
            const kanbanEl = renderKanbanBoard(this.tasks, {
                onTaskDrop: (id, targetStatus) => this.handleTaskDrop(id, targetStatus),
                onEdit: (taskData) => this.taskModal.open(taskData),
                onDelete: (id) => this.handleDeleteTask(id)
            });
            this.contentSlot.appendChild(kanbanEl);
        } else {
            // Render Grid Cards
            const gridContainer = document.createElement('div');
            gridContainer.className = 'tasks-grid';

            if (this.tasks.length === 0) {
                gridContainer.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">📌</div>
                        <h3>Không tìm thấy công việc nào</h3>
                        <p>Hãy bấm nút <strong>"Tạo Task"</strong> để bắt đầu!</p>
                    </div>
                `;
            } else {
                this.tasks.forEach(task => {
                    const card = renderTaskCard(task, {
                        onStatusToggle: (id, newStatus) => this.handleStatusToggle(id, newStatus),
                        onEdit: (taskData) => this.taskModal.open(taskData),
                        onDelete: (id) => this.handleDeleteTask(id)
                    });
                    gridContainer.appendChild(card);
                });
            }
            this.contentSlot.appendChild(gridContainer);
        }
    }

    async handleFilterChange(filters) {
        this.currentFilter = filters;
        if (this.currentUser) {
            this.tasks = await ApiService.getTasks(this.currentFilter);
            this.renderContent();
        }
    }

    async handleSaveTask(taskData) {
        if (taskData.id) {
            await ApiService.updateTask(taskData.id, taskData);
            await ApiService.sendTestNotification("Công Việc Cập Nhật", `Task "${taskData.title}" đã được chỉnh sửa.`);
        } else {
            await ApiService.createTask(taskData);
            await ApiService.sendTestNotification("Task Mới Đã Tạo", `Task "${taskData.title}" vừa được thêm vào hệ thống.`);
        }
        await this.refreshData();
    }

    async handleStatusToggle(id, newStatus) {
        await ApiService.updateTaskStatus(id, newStatus);
        await ApiService.sendTestNotification("Đổi Trạng Thái Task", `Task ID #${id} chuyển sang trạng thái ${newStatus}`);
        await this.refreshData();
    }

    async handleTaskDrop(id, targetStatus) {
        await ApiService.moveTask(id, targetStatus);
        await ApiService.sendTestNotification("Di Chuyển Thẻ Kanban", `Task ID #${id} được thả sang cột ${targetStatus}`);
        await this.refreshData();
    }

    async handleDeleteTask(id) {
        if (confirm("Bạn có chắc chắn muốn xóa task này?")) {
            await ApiService.deleteTask(id);
            await ApiService.sendTestNotification("Xóa Task", `Task ID #${id} đã bị xóa khỏi hệ thống.`);
            await this.refreshData();
        }
    }
}

// Bootstrap Application
document.addEventListener('DOMContentLoaded', () => {
    const app = new Application();
    app.init();
});
