import { useState } from "react";
import { setGroupLimit } from "../../services/budgetApi";
import { toast } from "react-toastify";

function GroupLimitModal({ groupId, currentLimit, onClose, onSuccess }) {
    const [limit, setLimit] = useState(currentLimit || "");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleSave = async () => {
        // validate limit > 0
        // call setGroupLimit(groupId, Number(limit))
        // toast.success, onSuccess(), onClose()
        // catch: setError
        if (!limit || isNaN(limit) || Number(limit) <= 0) {
            setError("Please enter a valid limit amount");
            return;
        }

        setLoading(true);
        try {
            await setGroupLimit(groupId, Number(limit));

            toast.success(
                `Group spending limit set to ₹${Number(limit).toLocaleString("en-IN")}`
            );

            await onSuccess(); // refresh group budget status in GroupBudgetWidget
            onClose();   // close modal

        } catch (err) {
            toast.error("Failed to set group limit. Please try again.");
            setError(err.response?.data?.message || "Failed to set group limit. Please try again.");
            setLoading(false);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 w-96 shadow-xl">
                {/* Header — "Set Group Limit", close button — same pattern as other modals */}
                <div className="flex justify-between items-center mb-6">
                    <div>
                        <div className="w-8 h-1 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 mb-2" />
                        <h2 className="text-gray-900 font-bold text-lg">Set Group Limit</h2>
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
                    <input
                        type="number"
                        className="w-full bg-[#f0f0eb] border border-[#e8e8e3] focus:border-blue-400 focus:ring-2 focus:ring-blue-100 rounded-xl px-4 py-3 text-gray-900 placeholder-gray-300 outline-none transition-all text-sm"
                        placeholder="Enter monthly spending limit for this group"
                        value={limit}
                        onChange={(e) => setLimit(e.target.value)}
                    />
                </div>

                {/* Error message */}
                {error && <p className="text-red-500 text-xs mb-4">{error}</p>}

                {/* Cancel + Save buttons */}
                <div className="flex justify-end gap-4">
                    <button
                        onClick={onClose}
                        className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl px-4 py-2 text-sm font-medium hover:bg-gray-100 transition-colors"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleSave}
                        disabled={loading}
                        className="bg-blue-500 text-white rounded-2xl px-4 py-2 text-sm font-medium hover:bg-blue-600 transition-colors disabled:bg-blue-300"
                    >
                        {loading ? "Saving..." : "Save"}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default GroupLimitModal;