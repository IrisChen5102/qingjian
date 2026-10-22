const base = '/api';

async function request(path, options = {}) {
  const res = await fetch(base + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    let msg = res.statusText;
    try {
      const data = await res.json();
      msg = data.message || msg;
    } catch (e) {
      // 非 JSON 错误体，沿用状态文本
    }
    throw new Error(msg);
  }
  return res.json();
}

export const api = {
  getDay: (date) => request(`/day/${date}`),
  saveSummary: (date, data) => request(`/day/${date}/summary`, { method: 'PUT', body: JSON.stringify(data) }),

  createTodo: (data) => request('/todos', { method: 'POST', body: JSON.stringify(data) }),
  updateTodo: (id, data) => request(`/todos/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteTodo: (id) => request(`/todos/${id}`, { method: 'DELETE' }),
  setTodoStatus: (id, status) => request(`/todos/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
  postponeTodo: (id, date) => request(`/todos/${id}/postpone`, { method: 'PUT', body: JSON.stringify({ date }) }),

  createTimeline: (data) => request('/timeline', { method: 'POST', body: JSON.stringify(data) }),
  updateTimeline: (id, data) => request(`/timeline/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteTimeline: (id) => request(`/timeline/${id}`, { method: 'DELETE' }),

  listNotes: (month) => request(`/notes${month ? `?month=${month}` : ''}`),
  getNote: (id) => request(`/notes/${id}`),
  createNote: (data) => request('/notes', { method: 'POST', body: JSON.stringify(data) }),
  updateNote: (id, data) => request(`/notes/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteNote: (id) => request(`/notes/${id}`, { method: 'DELETE' }),

  createPomodoro: (data) => request('/pomodoros', { method: 'POST', body: JSON.stringify(data) }),

  getCalendar: (month) => request(`/calendar?month=${month}`),
  getStats: (month) => request(`/stats?month=${month}`),
  exportAll: () => request('/export'),
  importAll: (data) => request('/import', { method: 'POST', body: JSON.stringify(data) }),
};
