import { useOutletContext, useNavigate } from "react-router-dom";

function Profile() {
    const { accountInfo } = useOutletContext();
    const navigate = useNavigate();

    return (
        <div className="max-w-2xl mx-auto px-6 py-8">
            <div className="flex justify-between items-center mb-6">
                <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-6">
                    Profile
                </p>
                <button
                    // navigate back to dashboard on click
                    onClick={() => navigate("/dashboard")}
                    className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-4 text-center hover:text-purple-500 text-xs font-semibold transition-colors mb-6 cursor-pointer hover:bg-purple-100"
                >
                    Back
                </button>
            </div>

            {
                accountInfo ? (
                    <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-2xl p-6 shadow-sm">
                        <div className="flex items-center gap-4 mb-6 pb-6 border-b border-[#e8e8e3]">
                            <div className="w-16 h-16 rounded-2xl bg-gradient-to-r from-blue-500 to-purple-500 flex items-center justify-center text-white text-2xl font-bold">
                                {accountInfo.accountName?.charAt(0)}
                            </div>
                            <div>
                                <h2 className="text-gray-900 font-bold text-xl">{accountInfo.accountName}</h2>
                                <p className="text-gray-400 text-sm">Account Holder</p>
                            </div>
                        </div>

                        <div className="space-y-4">
                            <div className="flex justify-between items-center py-3 border-b border-[#e8e8e3]">
                                <p className="text-gray-500 text-xs font-semibold uppercase tracking-wider">Account Number</p>
                                <p className="text-gray-900 font-medium">{accountInfo.accountNumber}</p>
                            </div>
                            <div className="flex justify-between items-center py-3 border-b border-[#e8e8e3]">
                                <p className="text-gray-500 text-xs font-semibold uppercase tracking-wider">Balance</p>
                                <p className="text-gray-900 font-bold">₹{accountInfo.accountBalance?.toLocaleString("en-IN")}</p>
                            </div>
                            <div className="flex justify-between items-center py-3">
                                <p className="text-gray-500 text-xs font-semibold uppercase tracking-wider">Status</p>
                                <span className="bg-green-100 text-green-700 text-xs font-medium px-3 py-1 rounded-full">Active</span>
                            </div>
                        </div>
                    </div>
                ) : (
                    <p className="text-gray-400 text-sm">No profile data found.</p>
                )
            }
        </div >
    );
}

export default Profile;