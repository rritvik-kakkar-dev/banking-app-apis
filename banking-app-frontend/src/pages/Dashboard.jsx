import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import CreditModal from "../components/Modals/CreditModal";
import DebitModal from "../components/Modals/DebitModal";
import TransferModal from "../components/Modals/TransferModal";
import TransactionHistoryList from "../components/Listing/TransactionHistoryList";

function Dashboard() {
    const navigate = useNavigate();
    const accountNumber = localStorage.getItem("accountNumber");

    const [accountInfo, setAccountInfo] = useState(null);
    const [error, setError] = useState("");
    const [accountLoading, setAccountLoading] = useState(true);
    const [transactionsLoading, setTransactionsLoading] = useState(false);

    const [activeModal, setActiveModal] = useState(null);

    const [page, setPage] = useState(0);
    const [limit] = useState(10);
    const [transactions, setTransactions] = useState([]);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        fetchTransactionsHistory();
    }, [page]);

    useEffect(() => {
        fetchAccountInfo();
    }, []);

    const fetchAccountInfo = async () => {
        try {
            setAccountLoading(true);
            const response = await api.get("/api/user/balanceEnquiry", {
                params: { accountNumber }
            });
            setAccountInfo(response.data.accountInfo);
        } catch (error) {
            setError("Failed to fetch account info");
        } finally {
            setAccountLoading(false);
        }
    };

    const fetchTransactionsHistory = async () => {
        try {
            setTransactionsLoading(true);
            const response = await api.get("/api/bankStatement/transaction-history", {
                params: {
                    accountNumber,
                    page,
                    limit: 10,
                    sortBy: "createdAt",
                    sortOrder: "DESC",
                }
            });
            setTransactions(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            setError("Failed to fetch transactions history");
        } finally {
            setTransactionsLoading(false);
        }
    }

    const refreshDashboard = async () => {
        await fetchAccountInfo();

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
        <div className="min-h-screen bg-[#f5f5f0]">

            {/* Credit Modal */}
            {
                activeModal === 'credit' && (
                    <CreditModal
                        onClose={() => setActiveModal(null)}
                        onSuccess={refreshDashboard}
                    />
                )
            }

            {/* Debit Modal */}
            {
                activeModal === 'debit' && (
                    <DebitModal
                        onClose={() => setActiveModal(null)}
                        onSuccess={refreshDashboard}
                    />
                )
            }

            {/* Transfer Modal */}
            {
                activeModal === 'transfer' && (
                    <TransferModal
                        onClose={() => setActiveModal(null)}
                        onSuccess={refreshDashboard}
                    />
                )
            }
            {/* Navbar */}
            <nav className="bg-[#fefefe] border-b border-[#e8e8e3] px-6 py-4 flex justify-between items-center shadow-sm">
                <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-xl bg-gradient-to-r from-blue-500 to-purple-500 flex items-center justify-center text-white text-sm font-bold">
                        V
                    </div>
                    <h1 className="text-gray-900 font-bold text-lg">Vaulta</h1>
                </div>
                <div className="flex items-center gap-4">
                    <span className="text-gray-400 text-sm">
                        {accountInfo ? accountInfo.accountName : ""}
                    </span>
                    <button
                        onClick={handleLogout}
                        className="text-sm font-medium px-4 py-2 rounded-xl border border-red-100 text-red-400 hover:bg-red-50 transition-all"
                    >
                        Logout
                    </button>
                </div>
            </nav>

            {/* Main content */}
            <div className="max-w-4xl mx-auto px-6 py-8">

                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-500 rounded-xl px-4 py-3 text-sm mb-6">
                        {error}
                    </div>
                )}

                {accountLoading ? (
                    <div className="flex flex-col items-center justify-center mt-32 gap-4">
                        <svg className="animate-spin h-8 w-8 text-blue-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                        </svg>
                        <p className="text-gray-400 text-xs uppercase tracking-widest">Loading account...</p>
                    </div>
                ) : accountInfo ? (
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
                            <button onClick={() => setActiveModal('credit')} className="bg-[#fefefe] hover:bg-[#f0f0eb] border border-[#e8e8e3] rounded-2xl p-4 text-center transition-all shadow-sm hover:shadow-md">
                                <div className="text-2xl mb-2">💰</div>
                                <p className="text-gray-700 text-sm font-medium">Credit</p>
                            </button>
                            <button onClick={() => setActiveModal('debit')} className="bg-[#fefefe] hover:bg-[#f0f0eb] border border-[#e8e8e3] rounded-2xl p-4 text-center transition-all shadow-sm hover:shadow-md">
                                <div className="text-2xl mb-2">💸</div>
                                <p className="text-gray-700 text-sm font-medium">Debit</p>
                            </button>
                            <button onClick={() => setActiveModal('transfer')} className="bg-[#fefefe] hover:bg-[#f0f0eb] border border-[#e8e8e3] rounded-2xl p-4 text-center transition-all shadow-sm hover:shadow-md">
                                <div className="text-2xl mb-2">🔄</div>
                                <p className="text-gray-700 text-sm font-medium">Transfer</p>
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
                ) : null}
            </div>
        </div >
    );
}

export default Dashboard;