const TransactionHistoryList = ({
    transactions,
    loading,
    page,
    totalPages,
    onPageChange
}) => {
    if (loading) {
        return (
            <div className="py-8 text-center">
                <svg
                    className="animate-spin h-6 w-6 mx-auto"
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

                <p className="mt-2 text-gray-500">
                    Loading transactions...
                </p>
            </div>
        );
    }

    if (transactions.length === 0) {
        return (
            <p className="text-gray-500 text-sm italic">
                No transactions found
            </p>
        );
    }

    return (
        <>
            <div className="space-y-4">
                {transactions.map((transaction) => (
                    <div
                        key={transaction.transactionReference}
                        className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm hover:shadow-md transition"
                    >
                        <div className="flex items-center justify-between">

                            {/* Left Section */}
                            <div className="flex items-center gap-4">

                                <div
                                    className={`w-12 h-12 rounded-full flex items-center justify-center text-xl ${transaction.transactionType === "CREDIT"
                                        ? "bg-green-100 text-green-600"
                                        : "bg-red-100 text-red-600"
                                        }`}
                                >
                                    {transaction.transactionType === "CREDIT"
                                        ? "↓"
                                        : "↑"}
                                </div>

                                <div>
                                    <p className="font-semibold text-gray-900">
                                        {transaction.transactionType === "CREDIT"
                                            ? "Money Received"
                                            : "Money Sent"}
                                    </p>

                                    <p className="text-sm text-gray-500">
                                        {transaction.transactionType === "DEBIT"
                                            ? transaction.counterpartySource
                                                ? `To • ${transaction.counterpartySource}`
                                                : "Direct Debit"
                                            : transaction.counterpartySource
                                                ? `From • ${transaction.counterpartySource}`
                                                : "Direct Credit"
                                        }
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
                            </div>

                            {/* Right Section */}
                            <div className="text-right">
                                <p
                                    className={`text-lg font-bold ${transaction.transactionType === "CREDIT"
                                        ? "text-green-600"
                                        : "text-red-600"
                                        }`}
                                >
                                    {transaction.transactionType === "CREDIT"
                                        ? "+"
                                        : "-"}
                                    ₹{Number(transaction.amount).toLocaleString("en-IN")}
                                </p>

                                <span
                                    className={`inline-block mt-1 px-2 py-1 rounded-full text-xs font-medium ${transaction.status === "SUCCESS"
                                        ? "bg-green-100 text-green-700"
                                        : "bg-yellow-100 text-yellow-700"
                                        }`}
                                >
                                    {transaction.status}
                                </span>
                            </div>
                        </div>

                        {/* Bottom Row */}
                        <div className="mt-3 pt-3 border-t border-gray-100">
                            <p className="text-xs text-gray-400 break-all">
                                Ref: {transaction.transactionReference}
                            </p>
                        </div>
                    </div>
                ))}
            </div>
        </>
    );
};

export default TransactionHistoryList;