import { useState, useEffect } from "react";
import { getExpenseHistory } from "../../services/budgetApi";

function ExpenseHistoryModal({ budget, onClose }) {
    const [expenses, setExpenses] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchExpenses();
    }, []);

    const fetchExpenses = async () => {
        try {
            const response = await getExpenseHistory(budget.budgetId);
            setExpenses(response.data);
        } catch (error) {
            console.error("Failed to fetch expense history", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 w-96 max-h-[80vh] overflow-y-auto shadow-xl">

                {/* Header — show budget.categoryIcon + budget.categoryName, close button */}
                <div className="flex justify-between items-center mb-6">
                    <div>
                        <div className="w-8 h-1 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 mb-2" />
                        <h2 className="text-gray-900 font-bold text-lg">
                            {budget.categoryIcon} {budget.categoryName}
                        </h2>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 text-xl font-light transition-colors"
                    >
                        ✕
                    </button>
                </div>

                {/* Loading state */}
                {loading ? (
                    <p className="text-gray-500 text-center">Loading expenses...</p>
                ) : (
                    <>
                        {/* Empty state — "No expenses logged yet" */}
                        {expenses.length === 0 ? (
                            <p className="text-gray-500 text-center">No expenses logged yet.</p>
                        ) : (
                            expenses.map((expense) => (
                                <div key={expense.id} className="flex justify-between items-center py-3 border-b border-[#e8e8e3]">
                                    <div>
                                        <p className="text-gray-700 text-sm font-medium">{expense.description || "No description"}</p>
                                        <p className="text-xs text-gray-400 mt-1">
                                            {new Date(
                                                expense.date
                                            ).toLocaleDateString("en-IN", {
                                                day: "2-digit",
                                                month: "short",
                                                year: "numeric"
                                            })}
                                        </p>
                                    </div>
                                    <p className="text-red-500 text-sm font-bold">-₹{expense.amount}</p>
                                </div>
                            ))
                        )}
                    </>
                )}
            </div>
        </div>
    );
}

export default ExpenseHistoryModal;