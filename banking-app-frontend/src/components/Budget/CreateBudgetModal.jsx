import { useState, useEffect } from "react";
import { toast } from "react-toastify";
import { getCategories, createBudget, createCategory, updateBudget } from "../../services/budgetApi";
import EmojiPicker from "emoji-picker-react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import Select from "react-select";

function CreateBudgetModal({ budgetGroupId, editingBudget, onClose, onSuccess }) {
    const [categories, setCategories] = useState([]);
    const [categoryId, setCategoryId] = useState(
        editingBudget?.categoryId?.toString() || ""
    );
    const [limitAmount, setLimitAmount] = useState(editingBudget?.limit || "");
    const [period, setPeriod] = useState(editingBudget?.period || "MONTHLY");
    const [customFrom, setCustomFrom] = useState(
        editingBudget?.customFrom ? new Date(editingBudget.customFrom) : null
    );

    const formattedStartDate = customFrom
        ? `${customFrom.getFullYear()}-${String(customFrom.getMonth() + 1).padStart(2, "0")
        }-${String(customFrom.getDate()).padStart(2, "0")}`
        : null;

    const [customTo, setCustomTo] = useState(
        editingBudget?.customTo ? new Date(editingBudget.customTo) : null
    );

    const formattedEndDate = customTo
        ? `${customTo.getFullYear()}-${String(customTo.getMonth() + 1).padStart(2, "0")
        }-${String(customTo.getDate()).padStart(2, "0")}`
        : null;

    const [linkedAccountNumber, setLinkedAccountNumber] = useState(
        editingBudget?.linkedAccountNumber || localStorage.getItem("accountNumber") || ""
    );

    const [showNewCategory, setShowNewCategory] = useState(false);
    const [newCategoryName, setNewCategoryName] = useState("");
    const [newCategoryIcon, setNewCategoryIcon] = useState("");
    const [newCategoryColor, setNewCategoryColor] = useState("#3B82F6");
    const [creatingCategory, setCreatingCategory] = useState(false);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const [showEmojiPicker, setShowEmojiPicker] = useState(false);

    const categoryOptions = categories.map(cat => ({
        value: cat.id,
        label: `${cat.icon} ${cat.name}`
    }));

    const inputClass = "w-full bg-[#f0f0eb] border border-[#e8e8e3] focus:border-blue-400 focus:ring-2 focus:ring-blue-100 rounded-xl px-4 py-3 text-gray-900 placeholder-gray-300 outline-none transition-all text-sm";
    const labelClass = "block text-gray-500 text-xs font-semibold uppercase tracking-wider mb-2";

    useEffect(() => {
        // fetch categories on mount, setCategories(response.data)
        const fetchCategories = async () => {
            try {
                const response = await getCategories();
                setCategories(response.data);
            } catch (error) {
                console.error("Failed to fetch categories", error);
                setError("Failed to load categories. Please try again.");
            }
        };

        fetchCategories();
    }, []);

    const handleCreateBudget = async () => {
        // validate categoryId, limitAmount, linkedAccountNumber
        // call createBudget({...})
        // on success: toast, onSuccess(), onClose()
        // on error: setError
        try {
            setLoading(true);

            if (!categoryId) {
                setError("Please select a category");
                return;
            }
            if (!limitAmount || isNaN(limitAmount) || Number(limitAmount) <= 0) {
                setError("Please enter a valid limit amount");
                return;
            }
            if (!linkedAccountNumber.trim()) {
                setError("Please enter a valid linked account number");
                return;
            }

            const payload = {
                budgetGroupId,
                categoryId: Number(categoryId),
                limitAmount: Number(limitAmount),
                period,
                startDate:
                    period === "CUSTOM" && customFrom
                        ? formattedStartDate
                        : null,

                endDate:
                    period === "CUSTOM" && customTo
                        ? formattedEndDate
                        : null,
                linkedAccountNumber: linkedAccountNumber.trim(),
                alertAt80Percent: true
            };

            if (editingBudget) {
                await updateBudget(editingBudget.budgetId, payload);
                toast.success("Budget updated successfully!");
            } else {
                await createBudget(payload);
                toast.success("Budget created successfully!");
            }

            await onSuccess();
            onClose();

        } catch (error) {
            setError(`Failed to ${editingBudget ? "update" : "create"} budget. Please try again.`);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateCategory = async () => {
        if (!newCategoryName.trim()) {
            setError("Please enter a category name");
            return;
        }

        setCreatingCategory(true);
        setError("");
        try {
            const response = await createCategory({
                name: newCategoryName.trim(),
                icon: newCategoryIcon.trim() || "📁",
                color: newCategoryColor
            });

            const newCategory = response.data;

            setCategories([...categories, newCategory]);
            setCategoryId(newCategory.id);

            setNewCategoryName("");
            setNewCategoryIcon("");
            setNewCategoryColor("#3B82F6");
            setShowNewCategory(false);
        } catch (err) {
            setError("Failed to create category. Please try again.");
        } finally {
            setCreatingCategory(false);
        }
    };



    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 w-full max-w-lg shadow-xl">
                {/* Header with close button — same pattern as CreditModal */}
                <div className="flex justify-between items-center mb-6">
                    <div>
                        <div className="w-8 h-1 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 mb-2" />
                        <h2 className="text-gray-900 font-bold text-lg">
                            {editingBudget ? "Edit Budget" : "Create Budget"}
                        </h2>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 text-xl font-light transition-colors"
                    >
                        ✕
                    </button>
                </div>

                {/* Category select */}
                <div className="mb-4">
                    <label className={labelClass}>Category</label>
                    <Select
                        options={categoryOptions}
                        isSearchable
                        placeholder="Select category..."
                        value={
                            categoryOptions.find(
                                option => option.value === Number(categoryId)
                            )
                        }
                        onChange={(selected) =>
                            setCategoryId(selected?.value.toString())
                        }
                        styles={{
                            control: (base, state) => ({
                                ...base,
                                minHeight: "54px",
                                borderRadius: "20px",
                                borderColor: state.isFocused ? "#3b82f6" : "#e8e8e3",
                                boxShadow: "none",
                                "&:hover": {
                                    borderColor: "#3b82f6"
                                }
                            }),

                            menu: (base) => ({
                                ...base,
                                borderRadius: "20px",
                                overflow: "hidden",
                                padding: "8px"
                            }),

                            menuList: (base) => ({
                                ...base,
                                padding: 0
                            }),

                            option: (base, state) => ({
                                ...base,
                                backgroundColor: state.isSelected
                                    ? "#6366f1"
                                    : state.isFocused
                                        ? "#eef2ff"
                                        : "white",
                                color: state.isSelected ? "white" : "#1f2937",
                                borderRadius: "12px",
                                marginBottom: "4px",
                                cursor: "pointer",
                                padding: "12px 16px"
                            }),

                            placeholder: (base) => ({
                                ...base,
                                color: "#9ca3af"
                            })
                        }}
                    />

                    {!showNewCategory ? (
                        <button
                            type="button"
                            onClick={() => setShowNewCategory(true)}
                            className="text-blue-500 hover:text-purple-500 text-xs font-semibold mt-2"
                        >
                            + Create new category
                        </button>
                    ) : (
                        <div className="bg-[#f0f0eb] border border-[#e8e8e3] rounded-xl p-3 mt-2 space-y-2">
                            <input
                                type="text"
                                className="w-full bg-white border border-[#e8e8e3] rounded-lg px-3 py-2 text-sm outline-none"
                                placeholder="Category name"
                                value={newCategoryName}
                                onChange={(e) => setNewCategoryName(e.target.value)}
                            />
                            <div className="relative">
                                <button
                                    type="button"
                                    onClick={() => setShowEmojiPicker(!showEmojiPicker)}
                                    className="w-1/2 bg-white border border-[#e8e8e3] rounded-lg px-3 py-2 text-sm text-left"
                                >
                                    {newCategoryIcon || "Pick icon"}
                                </button>
                                {showEmojiPicker && (
                                    <div className="absolute z-10 mt-1">
                                        <EmojiPicker
                                            onEmojiClick={(emojiData) => {
                                                setNewCategoryIcon(emojiData.emoji);
                                                setShowEmojiPicker(false);
                                            }}
                                        />
                                    </div>
                                )}
                            </div>
                            <div className="flex gap-2">
                                <button
                                    type="button"
                                    onClick={() => setShowNewCategory(false)}
                                    className="flex-1 bg-white hover:bg-gray-50 text-gray-600 text-xs font-medium py-2 rounded-lg transition-all"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="button"
                                    onClick={handleCreateCategory}
                                    disabled={creatingCategory}
                                    className="flex-1 bg-blue-500 hover:bg-blue-600 disabled:opacity-60 text-white text-xs font-medium py-2 rounded-lg transition-all"
                                >
                                    {creatingCategory ? "Saving..." : "Save Category"}
                                </button>
                            </div>
                        </div>
                    )}
                </div>

                {/* Limit amount <input type="number"> */}
                <div className="mb-4">
                    <label className="block text-gray-700 text-sm font-medium mb-2">
                        Limit Amount
                    </label>
                    <input
                        type="number"
                        value={limitAmount}
                        onChange={(e) => setLimitAmount(e.target.value)}
                        className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 w-full"
                        placeholder="Enter limit amount"
                    />
                </div>

                {/* Period <select> — MONTHLY / ANNUAL options */}
                <div className="mb-4">
                    <label className="block text-gray-700 text-sm font-medium mb-2">
                        Period
                    </label>
                    <select
                        value={period}
                        onChange={(e) => setPeriod(e.target.value)}
                        className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 w-full"
                    >
                        <option value="MONTHLY">Monthly</option>
                        <option value="ANNUAL">Annual</option>
                        <option value="CUSTOM">Custom</option>
                    </select>
                </div>

                {period === "CUSTOM" && (
                    <div className="mb-6">
                        <label className="block text-gray-700 text-sm font-semibold mb-3">
                            Custom Period
                        </label>

                        <div className="bg-[#fafafa] border border-[#e8e8e3] rounded-2xl p-4">
                            <div className="grid grid-cols-2 gap-4">

                                {/* From */}
                                <div>
                                    <p className="text-xs text-gray-400 mb-2">
                                        From
                                    </p>

                                    <DatePicker
                                        selected={customFrom}
                                        onChange={(date) => setCustomFrom(date)}
                                        dateFormat="dd MMM yyyy"
                                        placeholderText="Select date"
                                        className="w-full bg-white border border-[#e8e8e3] rounded-xl px-4 py-3 text-gray-700 outline-none focus:ring-2 focus:ring-purple-500"
                                    />
                                </div>

                                {/* To */}
                                <div>
                                    <p className="text-xs text-gray-400 mb-2">
                                        To
                                    </p>

                                    <DatePicker
                                        selected={customTo}
                                        onChange={(date) => setCustomTo(date)}
                                        dateFormat="dd MMM yyyy"
                                        placeholderText="Select date"
                                        minDate={customFrom}
                                        className="w-full bg-white border border-[#e8e8e3] rounded-xl px-4 py-3 text-gray-700 outline-none focus:ring-2 focus:ring-purple-500"
                                    />
                                </div>

                            </div>
                        </div>
                    </div>
                )}

                {/* Linked account <input type="text"> */}
                <div className="mb-4">
                    <label className="block text-gray-700 text-sm font-medium mb-2">
                        Linked Account Number
                    </label>
                    <input
                        type="text"
                        value={linkedAccountNumber}
                        onChange={(e) => setLinkedAccountNumber(e.target.value)}
                        className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 w-full"
                        placeholder="Enter linked account number"
                    />
                </div>

                {/* Error message */}
                {error && (
                    <div className="text-red-500 text-sm mb-4">{error}</div>
                )}

                {/* Cancel + Submit buttons — same pattern as CreditModal */}
                <div className="flex gap-3">
                    <button
                        onClick={onClose}
                        className="flex-1 bg-[#f0f0eb] hover:bg-[#e8e8e3] text-gray-600 font-medium py-3 rounded-xl transition-all text-sm"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleCreateBudget}
                        disabled={loading}
                        className={`flex-1 bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white font-medium py-3 rounded-xl transition-all ${loading ? "opacity-50 cursor-not-allowed" : ""}`}
                    >
                        {loading ? "Saving..." : editingBudget ? "Update Budget" : "Create Budget"}

                    </button>
                </div>
            </div>
        </div>
    );
}

export default CreateBudgetModal;