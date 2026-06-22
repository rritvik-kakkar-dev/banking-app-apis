import { useState } from "react";
import { createBudgetGroup } from "../../services/budgetApi";
import { toast } from "react-toastify";

function SetupBudgetModal({ onClose, onSuccess }) {
    const [groupName, setGroupName] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const [type, setType] = useState("SOLO");
    const [partnerEmail, setPartnerEmail] = useState("");

    const labelClass = "block text-gray-500 text-xs font-semibold uppercase tracking-wider mb-2";
    const inputClass = "w-full bg-[#f0f0eb] border border-[#e8e8e3] focus:border-blue-400 focus:ring-2 focus:ring-blue-100 rounded-xl px-4 py-3 text-gray-900 placeholder-gray-300 outline-none transition-all text-sm";

    const handleCreateBudgetGroup = async () => {
        if (!groupName.trim()) {
            setError("Please enter a valid group name");
            return;
        }
        setLoading(true);
        setError("");
        try {
            const response = await createBudgetGroup({
                name: groupName.trim(),
                type,
                partnerEmail: type === "COUPLE" ? partnerEmail.trim() : null
            });

            if (response.data) {
                toast.success("Budget group created successfully!");
                onSuccess(response.data);
                onClose();
            }
        } catch (error) {
            console.error("Failed to create budget group", error);
            setError("Failed to create budget group. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 w-96 shadow-xl">
                <div className="flex justify-between items-center mb-6">
                    <div>
                        <div className="w-8 h-1 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 mb-2" />
                        <h2 className="text-gray-900 font-bold text-lg">Set Up Budget</h2>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 text-xl font-light transition-colors"
                    >
                        ✕
                    </button>
                </div>

                <div className="mb-6">
                    <input
                        type="text"
                        className="w-full bg-[#f0f0eb] border border-[#e8e8e3] focus:border-blue-400 focus:ring-2 focus:ring-blue-100 rounded-xl px-4 py-3 text-gray-900 placeholder-gray-300 outline-none transition-all text-sm"
                        placeholder="Enter budget group name"
                        value={groupName}
                        onChange={(e) => setGroupName(e.target.value)}
                    />
                    {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
                </div>

                <div className="mb-4">
                    <label className={labelClass}>Group Type</label>
                    <div className="flex gap-2">
                        <button
                            type="button"
                            onClick={() => setType("SOLO")}
                            className={type === "SOLO" ? "flex-1 bg-blue-500 text-white text-sm py-2 rounded-xl" : "flex-1 bg-[#f0f0eb] text-gray-600 text-sm py-2 rounded-xl"}
                        >
                            Solo
                        </button>
                        <button
                            type="button"
                            onClick={() => setType("COUPLE")}
                            className={type === "COUPLE" ? "flex-1 bg-blue-500 text-white text-sm py-2 rounded-xl" : "flex-1 bg-[#f0f0eb] text-gray-600 text-sm py-2 rounded-xl"}
                        >
                            Couple
                        </button>
                    </div>
                </div>

                {type === "COUPLE" && (
                    <div className="mb-4">
                        <label className={labelClass}>Partner Email</label>
                        <input
                            type="email"
                            className={inputClass}
                            placeholder="partner@example.com"
                            value={partnerEmail}
                            onChange={(e) => setPartnerEmail(e.target.value)}
                        />
                    </div>
                )}

                <button
                    onClick={handleCreateBudgetGroup}
                    disabled={loading}
                    className={`w-full bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white text-sm font-medium px-4 py-2 rounded-xl transition-all ${loading ? "opacity-50 cursor-not-allowed" : ""}`}
                >
                    {loading ? "Creating..." : "Create Budget Group"}
                </button>
            </div>
        </div>
    );
}

export default SetupBudgetModal;