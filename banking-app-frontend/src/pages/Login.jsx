import { useState } from "react";
import api from "../services/api";

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleLogin = async () => {
        setLoading(true);
        try {
            const response = await api.post("/api/user/login", { email, password });
            localStorage.setItem("token", response.data.responseMessage);
            localStorage.setItem("accountNumber", response.data.accountInfo.accountNumber);
            window.location.href = "/dashboard";
        } catch (error) {
            setError("Invalid email or password");
            setLoading(false);
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#f5f5f0]">
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-3xl p-8 w-96 shadow-xl shadow-[#d8d8d3]/60">

                {/* Gradient header accent */}
                <div className="w-12 h-1 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 mb-6" />

                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-2xl font-bold text-gray-900 mb-1">Welcome to Volta</h1>
                    <p className="text-gray-400 text-sm">Sign in to your account</p>
                </div>

                {/* Email input */}
                <div className="mb-4">
                    <label className="block text-gray-500 text-xs font-semibold uppercase tracking-wider mb-2">
                        Email
                    </label>
                    <input
                        type="email"
                        className="w-full bg-[#f0f0eb] border border-[#e8e8e3] focus:border-blue-400 focus:ring-2 focus:ring-blue-100 rounded-xl px-4 py-3 text-gray-900 placeholder-gray-300 outline-none transition-all text-sm"
                        placeholder="you@example.com"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                </div>

                {/* Password input */}
                <div className="mb-6">
                    <label className="block text-gray-500 text-xs font-semibold uppercase tracking-wider mb-2">
                        Password
                    </label>
                    <input
                        type="password"
                        className="w-full bg-[#f0f0eb] border border-[#e8e8e3] focus:border-blue-400 focus:ring-2 focus:ring-blue-100 rounded-xl px-4 py-3 text-gray-900 placeholder-gray-300 outline-none transition-all text-sm"
                        placeholder="••••••••"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>

                {/* Error message */}
                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-500 rounded-xl px-4 py-3 text-sm mb-4">
                        {error}
                    </div>
                )}

                {/* Login button */}
                <button
                    onClick={handleLogin}
                    disabled={loading}
                    className="w-full bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold py-3 rounded-xl transition-all shadow-lg shadow-blue-200 mb-6 flex items-center justify-center gap-2"
                >
                    {loading ? (
                        <>
                            <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                            </svg>
                            Signing in...
                        </>
                    ) : "Sign In"}
                </button>

                <p className="text-center text-gray-400 text-sm">
                    Don't have an account?{' '}
                    <a href="/register" className="text-blue-500 hover:text-purple-500 font-medium transition-colors">
                        Register
                    </a>
                </p>
            </div>
        </div>
    )
}

export default Login