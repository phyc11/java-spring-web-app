import { ApiService } from './api.js';
import { renderNavbar } from './components/Navbar.js';
import { renderStatsCards } from './components/StatsCard.js';
import { renderTaskFilter } from './components/TaskFilter.js';
import { renderTaskCard } from './components/TaskCard.js';
import { renderTaskModal } from './components/TaskModal.js';

class Application {
    constructor() {
        this.appContainer = document.getElementById('app');
        this.categories = [];
        this.tasks = [];
        this.stats = {};
        this.currentFilter = {};
    }

    async init() {
        try {
            // Load initial metadata
            this.categories = await ApiService.getCategories();
            
            // Build UI Skeleton
            this.appContainer.innerHTML = '';
            
            // Render Navbar
            this.modalComponent = renderTaskModal(this.categories, (data) => this.handleSaveTask(data));
            document.body.appendChild(this.modalComponent.element);

            const navbar = renderNavbar(() => this.modalComponent.open());
            this.appContainer.appendChild(navbar);

            // Stats Container Slot
            this.statsSlot = document.createElement('div');
            this.appContainer.appendChild(this.statsSlot);

            // Filter Bar
            const filterBar = renderTaskFilter(this.categories, (filters) => this.handleFilterChange(filters));
            this.appContainer.appendChild(filterBar);

            // Tasks Container Grid Slot
            this.tasksGridSlot = document.createElement('div');
            this.tasksGridSlot.className = 'tasks-grid';
            this.appContainer.appendChild(this.tasksGridSlot);

            // Initial Data Load
            await this.refreshData();

        } catch (err) {
            console.error("Initialization error:", err);
        }
    }

    async refreshData() {
        this.stats = await ApiService.getStats();
        this.tasks = await ApiService.getTasks(this.currentFilter);
        this.renderStats();
        this.renderTasks();
    }

    renderStats() {
        this.statsSlot.innerHTML = '';
        this.statsSlot.appendChild(renderStatsCards(this.stats));
    }

    renderTasks() {
        this.tasksGridSlot.innerHTML = '';

        if (this.tasks.length === 0) {
            this.tasksGridSlot.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📌</div>
                    <h3>Không tìm thấy công việc nào</h3>
                    <p>Hãy bấm nút <strong>"Tạo Task Mới"</strong> để thêm công việc đầu tiên!</p>
                </div>
            `;
            return;
        }

        this.tasks.forEach(task => {
            const card = renderTaskCard(task, {
                onStatusToggle: (id, newStatus) => this.handleStatusToggle(id, newStatus),
                onEdit: (taskData) => this.modalComponent.open(taskData),
                onDelete: (id) => this.handleDeleteTask(id)
            });
            this.tasksGridSlot.appendChild(card);
        });
    }

    async handleFilterChange(filters) {
        this.currentFilter = filters;
        this.tasks = await ApiService.getTasks(this.currentFilter);
        this.renderTasks();
    }

    async handleSaveTask(taskData) {
        if (taskData.id) {
            await ApiService.updateTask(taskData.id, taskData);
        } else {
            await ApiService.createTask(taskData);
        }
        await this.refreshData();
    }

    async handleStatusToggle(id, newStatus) {
        await ApiService.updateTaskStatus(id, newStatus);
        await this.refreshData();
    }

    async handleDeleteTask(id) {
        if (confirm("Bạn có chắc chắn muốn xóa task này?")) {
            await ApiService.deleteTask(id);
            await this.refreshData();
        }
    }
}

// Bootstrap Application on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    const app = new Application();
    app.init();
});
