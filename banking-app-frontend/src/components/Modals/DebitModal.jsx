import { useState } from "react";
import api from "../../services/api";
import { toast } from "react-toastify";

function DebitModal({ onClose, onSuccess }) {
    const [amount, setAmount] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const accountNumber = localStorage.getItem("accountNumber");

    const handleDebit = async () => {
        if (!amount || isNaN(amount) || Number(amount) <= 0) {
            setError("Please enter a valid amount");
            return;
        }

        setLoading(true);
        try {
            await api.post("/api/user/debit", {
                accountNumber,
                amount: Number(amount)
            });
            toast.success(
                `₹${Number(amount).toLocaleString("en-IN")} debited successfully`
            );

            await onSuccess(); // refresh balance on dashboard
            onClose();   // close modal

        } catch (err) {
            toast.error("Failed to debit amount");
            setError("Failed to debit amount. Try again.");
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 w-96 shadow-xl">

                {/* Header */}
                <div className="flex justify-between items-center mb-6">
                    <div>
                        <div className="w-8 h-1 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 mb-2" />
                        <h2 className="text-gray-900 font-bold text-lg">Debit Amount</h2>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 text-xl font-light transition-colors"
                    >
                        ✕
                    </button>
                </div>

                {/* Amount input */}
                <div className="mb-6">
                    <label className="block text-gray-500 text-xs font-semibold uppercase tracking-wider mb-2">
                        Amount (₹)
                    </label>
                    <input
                        type="number"
                        className="w-full bg-[#f0f0eb] border border-[#e8e8e3] focus:border-blue-400 focus:ring-2 focus:ring-blue-100 rounded-xl px-4 py-3 text-gray-900 placeholder-gray-300 outline-none transition-all text-sm"
                        placeholder="Enter amount"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                    />
                </div>

                {/* Error */}
                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-500 rounded-xl px-4 py-3 text-sm mb-4">
                        {error}
                    </div>
                )}

                {/* Buttons */}
                <div className="flex gap-3">
                    <button
                        onClick={onClose}
                        className="flex-1 bg-[#f0f0eb] hover:bg-[#e8e8e3] text-gray-600 font-medium py-3 rounded-xl transition-all text-sm"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleDebit}
                        disabled={loading}
                        className="flex-1 bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-all text-sm flex items-center justify-center gap-2"
                    >
                        {loading ? (
                            <>
                                <svg className="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                                </svg>
                                Processing...
                            </>
                        ) : "Debit ₹" + (amount || "0")}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default DebitModal;