import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Dashboard() {
    const navigate = useNavigate();
    const accountNumber = localStorage.getItem("accountNumber");

    const [accountInfo, setAccountInfo] = useState(null);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchAccountInfo();
    }, []);

    const fetchAccountInfo = async () => {
        try {
            const response = await api.get("/api/user/balanceEnquiry", {
                params: { accountNumber }
            });
            setAccountInfo(response.data.accountInfo);
        } catch (error) {
            setError("Failed to fetch account info");
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate("/login");
    };

    return (
        <div className="min-h-screen bg-slate-900">

            {/* Navbar */}
            <nav className="bg-slate-800 border-b border-slate-700 px-6 py-4 flex justify-between items-center">
                <h1 className="text-white font-bold text-xl">🏦 Banking App</h1>
                <div className="flex items-center gap-4">
                    <span className="text-slate-300 text-sm">
                        {accountInfo ? accountInfo.accountName : ""}
                    </span>
                    <button
                        onClick={handleLogout}
                        className="bg-red-600 hover:bg-red-500 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
                    >
                        Logout
                    </button>
                </div>
            </nav>

            {/* Main content */}
            <div className="max-w-4xl mx-auto px-6 py-8">

                {error && (
                    <div className="bg-red-900/50 border border-red-500 text-red-300 px-4 py-3 rounded-lg mb-6 text-sm">
                        {error}
                    </div>
                )}

                {loading ? (
                    <div className="text-slate-400 text-center mt-20">Loading...</div>
                ) : accountInfo ? (
                    <>
                        {/* Balance Card */}
                        <div className="bg-gradient-to-r from-blue-600 to-blue-800 rounded-2xl p-6 mb-6 shadow-xl">
                            <p className="text-blue-200 text-sm mb-1">Total Balance</p>
                            <h2 className="text-white text-4xl font-bold mb-4">
                                ₹{accountInfo.accountBalance.toLocaleString("en-IN")}
                            </h2>
                            <div className="flex justify-between">
                                <div>
                                    <p className="text-blue-200 text-xs">Account Number</p>
                                    <p className="text-white text-sm font-medium">{accountInfo.accountNumber}</p>
                                </div>
                                <div className="text-right">
                                    <p className="text-blue-200 text-xs">Account Holder</p>
                                    <p className="text-white text-sm font-medium">{accountInfo.accountName}</p>
                                </div>
                            </div>
                        </div>

                        {/* Quick Actions */}
                        <div className="grid grid-cols-3 gap-4 mb-6">
                            <button className="bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl p-4 text-center transition-colors">
                                <div className="text-2xl mb-2">💰</div>
                                <p className="text-white text-sm font-medium">Credit</p>
                            </button>
                            <button className="bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl p-4 text-center transition-colors">
                                <div className="text-2xl mb-2">💸</div>
                                <p className="text-white text-sm font-medium">Debit</p>
                            </button>
                            <button className="bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl p-4 text-center transition-colors">
                                <div className="text-2xl mb-2">🔄</div>
                                <p className="text-white text-sm font-medium">Transfer</p>
                            </button>
                        </div>

                        {/* Recent Transactions placeholder */}
                        <div className="bg-slate-800 rounded-xl border border-slate-700 p-6">
                            <h3 className="text-white font-semibold mb-4">Recent Transactions</h3>
                            <p className="text-slate-400 text-sm text-center py-8">
                                Transaction history coming soon
                            </p>
                        </div>
                    </>
                ) : null}
            </div>
        </div>
    );
}

export default Dashboard;