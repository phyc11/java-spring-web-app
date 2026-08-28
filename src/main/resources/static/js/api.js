/**
 * TaskCraft API Service Module
 * Handles all backend communication with Spring Boot REST API
 */
const API_BASE = '/api';

export const ApiService = {
    // Fetch all tasks with optional filters
    async getTasks(params = {}) {
        const query = new URLSearchParams();
        if (params.status) query.append('status', params.status);
        if (params.priority) query.append('priority', params.priority);
        if (params.categoryId) query.append('categoryId', params.categoryId);
        if (params.search) query.append('search', params.search);

        const url = `${API_BASE}/tasks${query.toString() ? '?' + query.toString() : ''}`;
        const res = await fetch(url);
        const json = await res.json();
        return json.data || [];
    },

    // Fetch dashboard stats
    async getStats() {
        const res = await fetch(`${API_BASE}/tasks/stats`);
        const json = await res.json();
        return json.data || {};
    },

    // Fetch all categories
    async getCategories() {
        const res = await fetch(`${API_BASE}/categories`);
        const json = await res.json();
        return json.data || [];
    },

    // Create a new task
    async createTask(taskData) {
        const res = await fetch(`${API_BASE}/tasks`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskData)
        });
        const json = await res.json();
        return json.data;
    },

    // Update existing task
    async updateTask(id, taskData) {
        const res = await fetch(`${API_BASE}/tasks/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskData)
        });
        const json = await res.json();
        return json.data;
    },

    // Update task status (TODO, IN_PROGRESS, COMPLETED)
    async updateTaskStatus(id, status) {
        const res = await fetch(`${API_BASE}/tasks/${id}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        const json = await res.json();
        return json.data;
    },

    // Delete task
    async deleteTask(id) {
        const res = await fetch(`${API_BASE}/tasks/${id}`, {
            method: 'DELETE'
        });
        const json = await res.json();
        return json.success;
    }
};
