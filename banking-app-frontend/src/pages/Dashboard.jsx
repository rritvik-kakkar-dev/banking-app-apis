import { useState, useEffect } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import api from "../services/api";

import CreditModal from "../components/Modals/CreditModal";
import DebitModal from "../components/Modals/DebitModal";
import TransferModal from "../components/Modals/TransferModal";
import TransactionHistoryList from "../components/Listing/TransactionHistoryList";

function Dashboard() {
    const navigate = useNavigate();

    const accountNumber = localStorage.getItem("accountNumber");

    const {
        accountInfo,
        refreshAccountInfo
    } = useOutletContext();

    const [error, setError] = useState("");
    const [transactionsLoading, setTransactionsLoading] =
        useState(false);

    const [activeModal, setActiveModal] = useState(null);

    const [page, setPage] = useState(0);
    const [transactions, setTransactions] = useState([]);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        fetchTransactionsHistory();
    }, [page]);

    const fetchTransactionsHistory = async () => {
        try {
            setTransactionsLoading(true);

            const response = await api.get(
                "/api/bankStatement/transaction-history",
                {
                    params: {
                        accountNumber,
                        page,
                        limit: 5,
                        sortBy: "createdAt",
                        sortOrder: "DESC"
                    }
                }
            );

            setTransactions(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            setError("Failed to fetch transactions history");
        } finally {
            setTransactionsLoading(false);
        }
    };

    const refreshDashboard = async () => {
        await refreshAccountInfo();

        if (page === 0) {
            await fetchTransactionsHistory();
        } else {
            setPage(0);
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate("/login");
    };

    return (
        <>
            {/* Credit Modal */}
            {activeModal === "credit" && (
                <CreditModal
                    onClose={() => setActiveModal(null)}
                    onSuccess={refreshDashboard}
                />
            )}

            {/* Debit Modal */}
            {activeModal === "debit" && (
                <DebitModal
                    onClose={() => setActiveModal(null)}
                    onSuccess={refreshDashboard}
                />
            )}

            {/* Transfer Modal */}
            {activeModal === "transfer" && (
                <TransferModal
                    onClose={() => setActiveModal(null)}
                    onSuccess={refreshDashboard}
                />
            )}

            <div className="max-w-4xl mx-auto px-6 py-8">

                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-500 rounded-xl px-4 py-3 text-sm mb-6">
                        {error}
                    </div>
                )}

                {!accountInfo ? (
                    <div className="flex flex-col items-center justify-center mt-32 gap-4">
                        <svg
                            className="animate-spin h-8 w-8 text-blue-400"
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 24 24"
                        >
                            <circle
                                className="opacity-25"
                                cx="12"
                                cy="12"
                                r="10"
                                stroke="currentColor"
                                strokeWidth="4"
                            />
                            <path
                                className="opacity-75"
                                fill="currentColor"
                                d="M4 12a8 8 0 018-8v8z"
                            />
                        </svg>

                        <p className="text-gray-400 text-xs uppercase tracking-widest">
                            Loading account...
                        </p>
                    </div>
                ) : (
                    <>
                        {/* Balance Card */}
                        <div className="bg-gradient-to-r from-blue-500 to-purple-600 rounded-3xl p-6 mb-6 shadow-xl shadow-blue-200/40">
                            <p className="text-blue-100 text-xs font-semibold uppercase tracking-wider mb-3">
                                Total Balance
                            </p>

                            <h2 className="text-white text-5xl font-bold tracking-tight mb-6">
                                ₹{accountInfo.accountBalance.toLocaleString("en-IN")}
                            </h2>

                            <div className="flex justify-between">
                                <div>
                                    <p className="text-blue-200 text-xs uppercase tracking-wider mb-1">
                                        Account Number
                                    </p>

                                    <p className="text-white text-sm font-medium">
                                        {accountInfo.accountNumber}
                                    </p>
                                </div>

                                <div className="text-right">
                                    <p className="text-blue-200 text-xs uppercase tracking-wider mb-1">
                                        Account Holder
                                    </p>

                                    <p className="text-white text-sm font-medium">
                                        {accountInfo.accountName}
                                    </p>
                                </div>
                            </div>
                        </div>

                        {/* Quick Actions */}
                        <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-3">
                            Quick Actions
                        </p>

                        <div className="grid grid-cols-3 gap-4 mb-6">
                            <button
                                onClick={() => setActiveModal("credit")}
                                className="bg-[#fefefe] hover:bg-[#f0f0eb] border border-[#e8e8e3] rounded-2xl p-4 text-center transition-all shadow-sm hover:shadow-md"
                            >
                                <div className="text-2xl mb-2">💰</div>
                                <p className="text-gray-700 text-sm font-medium">
                                    Credit
                                </p>
                            </button>

                            <button
                                onClick={() => setActiveModal("debit")}
                                className="bg-[#fefefe] hover:bg-[#f0f0eb] border border-[#e8e8e3] rounded-2xl p-4 text-center transition-all shadow-sm hover:shadow-md"
                            >
                                <div className="text-2xl mb-2">💸</div>
                                <p className="text-gray-700 text-sm font-medium">
                                    Debit
                                </p>
                            </button>

                            <button
                                onClick={() => setActiveModal("transfer")}
                                className="bg-[#fefefe] hover:bg-[#f0f0eb] border border-[#e8e8e3] rounded-2xl p-4 text-center transition-all shadow-sm hover:shadow-md"
                            >
                                <div className="text-2xl mb-2">🔄</div>
                                <p className="text-gray-700 text-sm font-medium">
                                    Transfer
                                </p>
                            </button>
                        </div>

                        {/* Recent Transactions */}
                        <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 shadow-sm">
                            <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-6">
                                Recent Transactions
                            </p>

                            <TransactionHistoryList
                                transactions={transactions}
                                loading={transactionsLoading}
                                page={page}
                                totalPages={totalPages}
                                onPageChange={setPage}
                            />
                        </div>
                    </>
                )}
            </div>
        </>
    );
}

export default Dashboard;