import { useState, useEffect, useRef } from "react";
import api from "../../services/api";

function ShowAllTransactionsModal({ onClose }) {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    const accountNumber = localStorage.getItem("accountNumber");

    const loaderRef = useRef(null);

    useEffect(() => {
        fetchTransactions(page);
    }, [page]);

    const fetchTransactions = async (currentPage) => {
        if (loading) return;

        try {
            setLoading(true);

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

            setTransactions(prev => [
                ...prev,
                ...response.data.content
            ]);

            setTotalPages(response.data.totalPages);

        } catch (error) {
            console.error("Failed to fetch transactions", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const observer = new IntersectionObserver(
            entries => {
                if (
                    entries[0].isIntersecting &&
                    !loading &&
                    page < totalPages - 1
                ) {
                    setPage(prev => prev + 1);
                }
            },
            { threshold: 1 }
        );

        if (loaderRef.current) {
            observer.observe(loaderRef.current);
        }

        return () => observer.disconnect();
    }, [loading, page, totalPages]);

    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 w-96 max-h-[80vh] overflow-y-auto shadow-xl">

                {/* Header */}
                <div className="flex justify-between items-center mb-6">
                    <div>
                        <div className="w-8 h-1 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 mb-2" />
                        <h2 className="text-gray-900 font-bold text-lg">
                            All Transactions
                        </h2>
                    </div>

                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 text-xl"
                    >
                        ✕
                    </button>
                </div>

                {transactions.length === 0 && !loading ? (
                    <p className="text-gray-500 text-center">
                        No transactions found.
                    </p>
                ) : (
                    <>
                        {transactions.map(transaction => (
                            < div
                                key={transaction.transactionId}
                                className="flex justify-between items-center py-3 border-b border-[#e8e8e3]"
                            >
                                <div>
                                    <p className="text-gray-700 text-sm font-medium">
                                        {transaction.counterpartySource || "No description"}
                                    </p>

                                    <p className="text-xs text-gray-400 mt-1">
                                        {new Date(
                                            transaction.createdAt
                                        ).toLocaleDateString("en-IN", {
                                            day: "2-digit",
                                            month: "short",
                                            year: "numeric"
                                        })}
                                    </p>
                                </div>

                                <p
                                    className={`text-sm font-bold ${transaction.type === "CREDIT"
                                        ? "text-green-500"
                                        : "text-red-500"
                                        }`}
                                >
                                    {transaction.type === "CREDIT"
                                        ? `+₹${transaction.amount}`
                                        : `-₹${transaction.amount}`}
                                </p>
                            </div>
                        ))}

                        {/* Loading spinner */}
                        {loading && (
                            <p className="text-center text-gray-400 py-4">
                                Loading...
                            </p>
                        )}

                        {/* End message */}
                        {!loading && page >= totalPages - 1 && (
                            <p className="text-center text-gray-400 py-4 text-sm">
                                No more transactions
                            </p>
                        )}

                        {/* Trigger */}
                        <div ref={loaderRef}></div>
                    </>
                )}
            </div>
        </div >
    );
}

export default ShowAllTransactionsModal;