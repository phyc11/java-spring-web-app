/**
 * Navbar Component
 */
export function renderNavbar(onNewTaskClick) {
    const header = document.createElement('header');
    header.className = 'header-navbar';
    header.innerHTML = `
        <div class="brand">
            <div class="brand-icon">TC</div>
            <div>
                <span class="brand-title">TaskCraft</span>
                <span class="brand-badge">Spring Boot + Java 11</span>
            </div>
        </div>
        <button class="btn btn-primary" id="btn-create-task">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
            Tạo Task Mới
        </button>
    `;

    header.querySelector('#btn-create-task').addEventListener('click', onNewTaskClick);
    return header;
}
