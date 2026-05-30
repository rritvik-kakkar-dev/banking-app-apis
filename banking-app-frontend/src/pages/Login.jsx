import { useState } from "react";
import api from "../services/api";

function Login() {

    // 1. State for email, password, and error message
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    // 2. Handle form submit
    const handleLogin = async () => {
        // call POST /api/user/login with { email, password }
        // on success → save token to localStorage → redirect to /dashboard
        // on error → set error message

        try {
            const response = await api.post("/api/user/login", { email, password });
            localStorage.setItem("token", response.data.responseMessage);
            localStorage.setItem("accountNumber", response.data.accountInfo.accountNumber);
            window.location.href = "/dashboard";
        } catch (error) {
            setError("Invalid email or password");
        }
    }

    // 3. Return the JSX (HTML-like UI)
    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-900">
            <div className="bg-slate-800 p-8 rounded-lg shadow-xl w-96 border border-slate-700">
                <h1 className="text-2xl font-bold text-center mb-2 text-white">Banking App</h1>
                <p className="text-center text-slate-400 text-sm mb-6">Sign in to your account</p>

                {/* Email input */}
                <div className="mb-4">
                    <label className="block text-slate-300 text-sm font-medium mb-2">
                        Email
                    </label>
                    <input
                        type="email"
                        className="w-full bg-slate-700 border border-slate-600 rounded-lg py-2.5 px-3 text-white placeholder-slate-400 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                        placeholder="you@example.com"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                </div>

                {/* Password input */}
                <div className="mb-6">
                    <label className="block text-slate-300 text-sm font-medium mb-2">
                        Password
                    </label>
                    <input
                        type="password"
                        className="w-full bg-slate-700 border border-slate-600 rounded-lg py-2.5 px-3 text-white placeholder-slate-400 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                        placeholder="••••••••"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>

                {/* Error message */}
                {error && (
                    <div className="bg-red-900/50 border border-red-500 text-red-300 px-4 py-3 rounded-lg mb-4 text-sm">
                        {error}
                    </div>
                )}

                {/* Login button */}
                <button
                    onClick={handleLogin}
                    className="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-2.5 px-4 rounded-lg transition-colors duration-200"
                >
                    Sign In
                </button>

                <p className="text-center text-slate-400 text-sm mt-4">
                    Don't have an account?{' '}
                    <a href="/register" className="text-blue-400 hover:text-blue-300">
                        Register
                    </a>
                </p>
            </div>
        </div>
    )
}

export default Login