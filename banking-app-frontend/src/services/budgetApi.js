import api from "./api";

export const createBudgetGroup = (data) => api.post("/api/budget/groups", data);
export const deleteBudgetGroup = (groupId) => api.delete(`/api/budget/groups/${groupId}`);
export const getCategories = () => api.get("/api/budget/categories");
export const createCategory = (data) => api.post("/api/budget/categories", data);
export const createBudget = (data) => api.post("/api/budget", data);
export const logExpense = (data) => api.post("/api/budget/expenses", data);
export const getBudgetStatus = (params) => api.get("/api/budget/status", { params });
export const getExpenseHistory = (budgetId) => api.get("/api/budget/expenses", { params: { budgetId } });
export const getMyBudgetGroups = () => api.get("/api/budget/groups/my");
export const setGroupLimit = (groupId, limitAmount) => api.put(`/api/budget/groups/${groupId}/limit`, null, { params: { limitAmount } });
export const getGroupBudgetStatus = (groupId, year, month) => api.get(`/api/budget/groups/${groupId}/group-status`, { params: { year, month } });
export const getAnnualBudgetStatus = (groupId, year) => api.get("/api/budget/status", { params: { groupId, year } });
export const updateBudget = (budgetId, data) => api.put(`/api/budget/${budgetId}`, data);
export const deleteBudget = (budgetId) => api.delete(`/api/budget/${budgetId}`);
export const getCustomBudgetStatus = (groupId, from, to) => api.get("/api/budget/status", { params: { groupId, from, to } });