import { useState } from "react";
import { logExpense } from "../../services/budgetApi";
import { toast } from "react-toastify";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCalendar } from '@fortawesome/free-solid-svg-icons';

function LogExpenseModal({ budget, onClose, onSuccess }) {
    const budgetId = budget?.budgetId;
    const categoryName = budget?.categoryName;

    const [amount, setAmount] = useState("");
    const [description, setDescription] = useState("");
    const [date, setDate] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const formattedDate = date
        ? `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")
        }-${String(date.getDate()).padStart(2, "0")}`
        : null;

    const handleLogExpense = async () => {
        // validate amount > 0
        // call logExpense({ budgetId, amount: Number(amount), description, date })
        // on success: toast, onSuccess(), onClose()
        // on error: setError (check for InsufficientBalanceException message)
        if (!amount || isNaN(amount) || Number(amount) <= 0) {
            setError("Please enter a valid amount");
            return;
        }

        setLoading(true);
        try {
            await logExpense({
                budgetId,
                amount: Number(amount),
                description,
                formattedDate
            });

            toast.success(
                `Logged expense of ₹${Number(amount).toLocaleString("en-IN")} for ${categoryName}`
            );

            await onSuccess(); // refresh budget status in BudgetWidget
            onClose();   // close modal

        } catch (err) {
            toast.error("Failed to log expense. Please try again.");
            setError(err.response?.data?.message || "Failed to log expense. Please try again.");
            setLoading(false);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 w-96 shadow-xl">
                {/* Header — title should show categoryName, e.g. "Log Expense — Groceries" */}
                <div className="flex justify-between items-center mb-6">
                    <div>
                        <div className="w-8 h-1 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 mb-2" />
                        <h2 className="text-gray-900 font-bold text-lg">
                            Log Expense — {categoryName}
                        </h2>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 text-xl font-light transition-colors"
                    >
                        ✕
                    </button>
                </div>

                {/* Amount input type="number" */}
                <div className="mb-4">
                    <label className="block text-gray-700 text-sm font-medium mb-2">
                        Amount (₹)
                    </label>
                    <input
                        type="number"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 w-full"
                        placeholder="Enter expense amount"
                    />
                </div>

                {/* Description input type="text" placeholder="e.g. Weekly grocery run" */}
                <div className="mb-4">
                    <label className="block text-gray-700 text-sm font-medium mb-2">
                        Description <span className="text-gray-300 normal-case">(optional)</span>
                    </label>
                    <input
                        type="text"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 w-full"
                        placeholder="e.g. Weekly grocery run"
                    />
                </div>

                {/* Date input type="date" */}
                <div className="mb-6">
                    <label className="block text-gray-700 text-sm font-medium mb-2">
                        Date
                    </label>

                    <div className="relative">
                        <DatePicker
                            selected={date}
                            onChange={(date) => setDate(date)}
                            dateFormat="dd MMM yyyy"
                            placeholderText="Select date"
                            wrapperClassName="w-full"
                            className="w-full bg-white border border-[#e8e8e3] rounded-xl px-4 py-3 pr-12 text-gray-700 outline-none focus:ring-2 focus:ring-purple-500"
                        />

                        <span className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
                            <FontAwesomeIcon icon={faCalendar} />
                        </span>
                    </div>
                </div>

                {/* Error message */}
                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-500 rounded-xl px-4 py-3 text-sm mb-4">
                        {error}
                    </div>
                )}

                {/* Cancel + Submit buttons */}
                <div className="flex gap-3">
                    <button
                        onClick={onClose}
                        className="flex-1 bg-[#f0f0eb] hover:bg-[#e8e8e3] text-gray-600 font-medium py-3 rounded-xl transition-all text-sm"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleLogExpense}
                        disabled={loading}
                        className={`flex-1 bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white font-medium py-3 rounded-xl transition-all text-sm ${loading ? "opacity-50 cursor-not-allowed" : ""}`}
                    >
                        {loading ? "Logging..." : "Log Expense"}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default LogExpenseModal;