import { useState, useEffect } from "react";
import { getBudgetStatus, getGroupBudgetStatus, getAnnualBudgetStatus, getCustomBudgetStatus } from "../../services/budgetApi";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPenToSquare, faEllipsisVertical, faTrash, faPlus } from '@fortawesome/free-solid-svg-icons';


function BudgetWidget({ budgetGroupId, onSetupClick, onAddBudgetClick, onLogExpenseClick, onViewHistoryClick, onSetGroupLimitClick, onEditClick, onDeleteClick }) {
    const [budgetStatus, setBudgetStatus] = useState([]);
    const [loading, setLoading] = useState(true);
    const [groupStatus, setGroupStatus] = useState(null);

    const [viewMode, setViewMode] = useState("MONTHLY"); // MONTHLY | ANNUAL | CUSTOM
    const [customFrom, setCustomFrom] = useState("");
    const [customTo, setCustomTo] = useState("");

    const [activeMenu, setActiveMenu] = useState(null);

    useEffect(() => {
        if (budgetGroupId) {
            fetchStatus();
        }
    }, [budgetGroupId, viewMode, customFrom, customTo]);

    useEffect(() => {
        const handleOutsideClick = () => setActiveMenu(null);
        window.addEventListener("click", handleOutsideClick);
        return () => window.removeEventListener("click", handleOutsideClick);
    }, []);

    const fetchStatus = async () => {
        setLoading(true);
        try {
            let response;
            const year = new Date().getFullYear();
            const month = new Date().getMonth() + 1;

            if (viewMode === "MONTHLY") {
                response = await getBudgetStatus({ groupId: budgetGroupId, year, month });
            } else if (viewMode === "ANNUAL") {
                response = await getAnnualBudgetStatus(budgetGroupId, year);
            } else if (viewMode === "CUSTOM" && customFrom && customTo) {
                response = await getCustomBudgetStatus(budgetGroupId, customFrom, customTo);
            } else {
                setLoading(false);
                return; // custom mode without dates selected yet — wait
            }

            setBudgetStatus(response.data);

            // group status — only fetch for monthly (your backend group-status endpoint only supports year+month)
            if (viewMode === "MONTHLY") {
                const groupResponse = await getGroupBudgetStatus(budgetGroupId, year, month);
                setGroupStatus(groupResponse.data);
            } else {
                setGroupStatus(null);
            }
        } catch (error) {
            console.error("Failed to fetch budget status", error);
        } finally {
            setLoading(false);
        }
    };


    if (!budgetGroupId) {
        return (
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 shadow-sm text-center">
                <p className="text-gray-400 text-sm mb-3">No budget set up yet</p>
                <button
                    onClick={onSetupClick}
                    className="bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white text-sm font-medium px-4 py-2 rounded-xl transition-all"
                >
                    Set Up Budget
                </button>
            </div>
        );
    }

    if (loading) {
        return (
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 shadow-sm text-center">
                <p className="text-gray-400 text-sm">Loading budget...</p>
            </div>
        );
    }

    // render budgetStatus list with progress bars
    return (

        <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 shadow-sm">
            <div className="flex justify-between items-center mb-6">
                <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider">
                    Budget Status
                </p>
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => onSetGroupLimitClick(groupStatus?.groupLimit)}
                        className="text-gray-400 hover:text-blue-500 text-xs font-semibold transition-colors cursor-pointer"
                    >
                        ⚙️ Set Limit
                    </button>
                    <button
                        onClick={onAddBudgetClick}
                        className="text-blue-500 hover:text-purple-500 text-xs font-semibold transition-colors cursor-pointer"
                    >
                        + Add Budget Category
                    </button>
                </div>
            </div>

            <div className="flex gap-2 mb-4">
                {["MONTHLY", "ANNUAL", "CUSTOM"].map((mode) => (
                    <button
                        key={mode}
                        onClick={() => setViewMode(mode)}
                        className={
                            viewMode === mode
                                ? "bg-blue-500 text-white text-xs font-medium px-3 py-1.5 rounded-lg"
                                : "bg-[#f0f0eb] text-gray-500 text-xs font-medium px-3 py-1.5 rounded-lg hover:bg-[#e8e8e3]"
                        }
                    >
                        {mode.charAt(0) + mode.slice(1).toLowerCase()}
                    </button>
                ))}
            </div>

            {viewMode === "CUSTOM" && (
                <div className="flex gap-2 mb-4">
                    <input
                        type="date"
                        value={customFrom}
                        onChange={(e) => setCustomFrom(e.target.value)}
                        className="flex-1 bg-[#f0f0eb] border border-[#e8e8e3] rounded-lg px-3 py-2 text-sm outline-none"
                    />
                    <input
                        type="date"
                        value={customTo}
                        onChange={(e) => setCustomTo(e.target.value)}
                        className="flex-1 bg-[#f0f0eb] border border-[#e8e8e3] rounded-lg px-3 py-2 text-sm outline-none"
                    />
                </div>
            )}

            {groupStatus && groupStatus.groupLimit && (
                <div className="mb-6 pb-6 border-b border-[#e8e8e3]">
                    <div className="flex justify-between mb-1">
                        <p className="text-gray-900 text-sm font-bold">
                            {groupStatus.groupName} — Total
                        </p>
                        <p className="text-gray-700 text-sm font-medium">
                            ₹{groupStatus.totalSpent} / ₹{groupStatus.groupLimit}
                        </p>
                    </div>
                    <div className="w-full bg-[#f0f0eb] rounded-full h-3">
                        <div
                            className={`h-3 rounded-full transition-all ${groupStatus.percentUsed >= 100
                                ? "bg-red-500"
                                : groupStatus.percentUsed >= 80
                                    ? "bg-orange-400"
                                    : "bg-blue-500"
                                }`}
                            style={{ width: `${Math.min(groupStatus.percentUsed, 100)}%` }}
                        />
                    </div>
                    <p className="text-xs text-gray-400 mt-1">
                        {groupStatus.percentUsed.toFixed(0)}% of group limit used
                    </p>
                </div>
            )}

            {budgetStatus.length === 0 ? (
                <p className="text-gray-400 text-sm text-center py-4">No budgets created yet</p>
            ) : (
                budgetStatus.map((item) => (
                    <div key={item.categoryName} className="mb-4">
                        <div className="flex justify-between mb-1 items-center">
                            <p
                                onClick={() => onViewHistoryClick(item)}
                                className="text-sm font-medium cursor-pointer hover:text-blue-500 transition-colors"
                                style={{ color: item.categoryColor }}
                            >
                                {item.categoryIcon} {item.categoryName}
                            </p>
                            <div className="flex items-center gap-4 relative">
                                <p className="text-gray-700 text-sm font-medium">
                                    ₹{item.spent} / ₹{item.limit}
                                </p>

                                {/* The Interactive Menu Trigger Button */}
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation(); // Stop click from immediately closing the dropdown
                                        setActiveMenu(activeMenu === item.categoryName ? null : item.categoryName);
                                    }}
                                    className="text-gray-400 hover:text-gray-600 p-1 rounded-lg transition-colors cursor-pointer"
                                >
                                    <FontAwesomeIcon icon={faEllipsisVertical} />
                                </button>

                                {/* The Floating Action List Dropdown */}
                                {activeMenu === item.categoryName && (
                                    <div className="absolute right-0 top-8 z-10 w-44 bg-white border border-gray-200 rounded-xl shadow-lg py-1 text-left">
                                        <button
                                            onClick={() => onLogExpenseClick(item)}
                                            className="w-full text-left px-4 py-2 text-xs font-semibold text-blue-500 hover:bg-gray-50 flex items-center gap-2 cursor-pointer"
                                        >
                                            <FontAwesomeIcon icon={faPlus} className="w-3" /> Log Expense
                                        </button>
                                        <button
                                            onClick={() => onEditClick(item)}
                                            className="w-full text-left px-4 py-2 text-xs font-medium text-gray-700 hover:bg-gray-50 flex items-center gap-2 cursor-pointer"
                                        >
                                            <FontAwesomeIcon icon={faPenToSquare} className="w-3" /> Edit Category
                                        </button>
                                        <button
                                            onClick={() => onDeleteClick(item)}
                                            className="w-full text-left px-4 py-2 text-xs font-medium text-red-500 hover:bg-red-50 flex items-center gap-2 cursor-pointer"
                                        >
                                            <FontAwesomeIcon icon={faTrash} className="w-3" /> Delete
                                        </button>
                                    </div>
                                )}
                            </div>

                        </div>

                        {/* Progress bar */}
                        <div className="w-full bg-[#f0f0eb] rounded-full h-3">
                            <div
                                className={`h-3 rounded-full transition-all ${item.percentUsed >= 100
                                    ? "bg-red-500"
                                    : item.percentUsed >= 80
                                        ? "bg-orange-400"
                                        : "bg-green-500"
                                    }`}
                                style={{ width: `${Math.min(item.percentUsed, 100)}%` }}
                            />
                        </div>

                        <p className="text-xs text-gray-400 mt-1">
                            {item.percentUsed.toFixed(0)}% used
                        </p>
                    </div >
                ))
            )
            }
        </div >
    );
}

export default BudgetWidget;