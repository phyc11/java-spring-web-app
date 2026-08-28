/**
 * Comment & Threaded Discussion Component
 */
import { ApiService } from '../../core/api.js';

export function createCommentSection(taskId) {
    const container = document.createElement('div');
    container.className = 'comment-section';
    container.style.marginTop = '24px';
    container.style.paddingTop = '16px';
    container.style.borderTop = '1px solid #e2e8f0';

    container.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <h4 style="font-size:1.05rem; font-weight:600; color:#1e293b; display:flex; align-items:center; gap:6px;">
                💬 Thảo Luận & Bình Luận
            </h4>
        </div>

        <div style="display:flex; gap:8px; margin-bottom:16px;">
            <input type="text" id="comment-input-root" class="form-input" placeholder="Viết bình luận của bạn..." style="flex:1; font-size:0.9rem;">
            <button class="btn btn-primary btn-sm" id="btn-post-comment" style="white-space:nowrap;">Gửi 🚀</button>
        </div>

        <div id="comments-tree-list" style="display:flex; flex-direction:column; gap:12px; max-height:280px; overflow-y:auto; padding-right:4px;">
            <div style="color:#94a3b8; font-size:0.85rem; text-align:center; padding:12px;">Đang tải thảo luận...</div>
        </div>
    `;

    const inputEl = container.querySelector('#comment-input-root');
    const postBtn = container.querySelector('#btn-post-comment');
    const listEl = container.querySelector('#comments-tree-list');

    postBtn.addEventListener('click', async () => {
        const text = inputEl.value.trim();
        if (!text) return;
        postBtn.disabled = true;
        try {
            await ApiService.postComment({ taskId, content: text });
            inputEl.value = '';
            await loadComments();
        } catch (err) {
            alert("Lỗi đăng bình luận: " + err.message);
        } finally {
            postBtn.disabled = false;
        }
    });

    async function loadComments() {
        if (!taskId) {
            listEl.innerHTML = `<div style="color:#94a3b8; font-size:0.85rem; text-align:center; padding:12px;">Lưu task trước khi đăng bình luận.</div>`;
            return;
        }
        try {
            const comments = await ApiService.getTaskComments(taskId);
            if (!comments || comments.length === 0) {
                listEl.innerHTML = `<div style="color:#94a3b8; font-size:0.85rem; text-align:center; padding:12px;">Chưa có bình luận nào. Hãy là người đầu tiên thảo luận!</div>`;
                return;
            }

            listEl.innerHTML = comments.map(c => renderCommentItem(c, 0)).join('');
            attachCommentEvents(listEl);
        } catch (err) {
            listEl.innerHTML = `<div style="color:#ef4444; font-size:0.85rem;">Lỗi tải bình luận: ${err.message}</div>`;
        }
    }

    function renderCommentItem(c, depth = 0) {
        const marginLeft = depth > 0 ? `${depth * 24}px` : '0px';
        const hasReplies = c.replies && c.replies.length > 0;

        return `
            <div class="comment-card" data-id="${c.id}" style="margin-left:${marginLeft}; background:${depth > 0 ? '#f8fafc' : '#ffffff'}; padding:10px 12px; border-radius:8px; border:1px solid #e2e8f0;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                    <div style="display:flex; align-items:center; gap:6px;">
                        <span style="width:22px; height:22px; border-radius:50%; background:${c.authorAvatarColor || '#6366f1'}; color:#fff; font-size:0.75rem; display:inline-flex; align-items:center; justify-content:center; font-weight:bold;">
                            ${c.author ? c.author.charAt(0).toUpperCase() : 'U'}
                        </span>
                        <strong style="font-size:0.88rem; color:#334155;">${escapeHtml(c.author || 'User')}</strong>
                    </div>
                    <span style="font-size:0.75rem; color:#94a3b8;">${formatTime(c.createdAt)}</span>
                </div>
                
                <p style="font-size:0.88rem; color:#475569; margin:4px 0 6px 0; line-height:1.4;">
                    ${escapeHtml(c.content)}
                </p>

                <div style="display:flex; gap:12px; align-items:center;">
                    <button class="btn-reply-toggle" data-id="${c.id}" style="font-size:0.75rem; color:#6366f1; background:none; border:none; padding:0; cursor:pointer; font-weight:600;">
                        💬 Phản hồi
                    </button>
                    <button class="btn-delete-comment" data-id="${c.id}" style="font-size:0.75rem; color:#ef4444; background:none; border:none; padding:0; cursor:pointer; font-weight:600;">
                        🗑️ Xóa
                    </button>
                </div>

                <div class="reply-box" id="reply-box-${c.id}" style="display:none; margin-top:8px; gap:6px;">
                    <input type="text" class="form-input reply-input" placeholder="Viết câu trả lời..." style="font-size:0.82rem; padding:4px 8px;">
                    <button class="btn btn-primary btn-sm btn-submit-reply" data-id="${c.id}" style="font-size:0.78rem; padding:4px 8px;">Gửi</button>
                </div>

                ${hasReplies ? `<div class="replies-container" style="margin-top:8px; display:flex; flex-direction:column; gap:8px;">
                    ${c.replies.map(r => renderCommentItem(r, depth + 1)).join('')}
                </div>` : ''}
            </div>
        `;
    }

    function attachCommentEvents(parentEl) {
        parentEl.querySelectorAll('.btn-reply-toggle').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = btn.getAttribute('data-id');
                const box = parentEl.querySelector(`#reply-box-${id}`);
                if (box) box.style.display = box.style.display === 'none' ? 'flex' : 'none';
            });
        });

        parentEl.querySelectorAll('.btn-submit-reply').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const parentId = btn.getAttribute('data-id');
                const box = parentEl.querySelector(`#reply-box-${parentId}`);
                const input = box.querySelector('.reply-input');
                const text = input.value.trim();
                if (!text) return;
                btn.disabled = true;
                try {
                    await ApiService.postComment({ taskId, content: text, parentId: parseInt(parentId) });
                    await loadComments();
                } catch (err) {
                    alert("Lỗi gửi phản hồi: " + err.message);
                }
            });
        });

        parentEl.querySelectorAll('.btn-delete-comment').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = btn.getAttribute('data-id');
                if (confirm("Xóa bình luận này?")) {
                    try {
                        await ApiService.deleteComment(id);
                        await loadComments();
                    } catch (err) {
                        alert("Không thể xóa: " + err.message);
                    }
                }
            });
        });
    }

    loadComments();

    return {
        element: container,
        loadComments
    };
}

function formatTime(isoStr) {
    if (!isoStr) return '';
    try {
        const d = new Date(isoStr);
        return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' ' + d.toLocaleDateString();
    } catch {
        return isoStr;
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>'"]/g, 
        tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag)
    );
}
