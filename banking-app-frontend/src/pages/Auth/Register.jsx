import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Select from "react-select";
import api from "../../services/api";
import { toast } from "react-toastify";

const genderOptions = [
    { value: "Male", label: "Male" },
    { value: "Female", label: "Female" },
    { value: "Other", label: "Other" }
];

const selectStyles = {
    control: (base, state) => ({
        ...base,
        backgroundColor: "#f0f0eb",
        border: `1px solid ${state.isFocused ? "#93c5fd" : "#e8e8e3"}`,
        borderRadius: "0.75rem",
        padding: "4px 8px",
        boxShadow: state.isFocused ? "0 0 0 2px #bfdbfe" : "none",
        "&:hover": { borderColor: "#93c5fd" }
    }),
    option: (base, state) => ({
        ...base,
        backgroundColor: state.isSelected ? "#3b82f6" : state.isFocused ? "#eff6ff" : "white",
        color: state.isSelected ? "white" : "#111827",
        cursor: "pointer",
        borderRadius: "0.5rem",
    }),
    placeholder: (base) => ({ ...base, color: "#d1d5db", fontSize: "0.875rem" }),
    singleValue: (base) => ({ ...base, color: "#111827", fontSize: "0.875rem" }),
    indicatorSeparator: () => ({ display: "none" }),
    menu: (base) => ({ ...base, borderRadius: "0.75rem", overflow: "hidden" })
};

function Register() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        firstName: "", lastName: "", otherName: "",
        gender: "", address: "", stateOfOrigin: "",
        email: "", password: "", phoneNumber: "", alternativePhoneNumber: ""
    });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleRegister = async () => {
        setLoading(true);
        try {
            await api.post("/api/user", form);
            toast.success("Account created successfully!");
            navigate("/login", { state: { message: "Account created! Please login." } });
        } catch (error) {
            setError("Registration failed. Please check your details.");
            setLoading(false);
        }
    };

    const inputClass = "w-full bg-[#f0f0eb] border border-[#e8e8e3] focus:border-blue-400 focus:ring-2 focus:ring-blue-100 rounded-xl px-4 py-3 text-gray-900 placeholder-gray-300 outline-none transition-all text-sm";
    const labelClass = "block text-gray-500 text-xs font-semibold uppercase tracking-wider mb-2";

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#f5f5f0] py-10">
            <div className="bg-[#fefefe] border border-[#e8e8e3] rounded-3xl p-8 w-full max-w-2xl shadow-xl shadow-[#d8d8d3]/60">

                {/* Gradient header accent */}
                <div className="w-12 h-1 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 mb-6" />

                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-2xl font-bold text-gray-900 mb-1">Create your account</h1>
                    <p className="text-gray-400 text-sm">Join Vaulta — your personal finance platform</p>
                </div>

                {/* Section — Personal Info */}
                <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-4">
                    Personal Information
                </p>
                <div className="grid grid-cols-2 gap-4 mb-4">
                    <div>
                        <label className={labelClass}>First Name</label>
                        <input className={inputClass} placeholder="First name" name="firstName" value={form.firstName} onChange={handleChange} />
                    </div>
                    <div>
                        <label className={labelClass}>Last Name</label>
                        <input className={inputClass} placeholder="Last name" name="lastName" value={form.lastName} onChange={handleChange} />
                    </div>
                    <div>
                        <label className={labelClass}>Other Name <span className="text-gray-300 normal-case font-normal">(optional)</span></label>
                        <input className={inputClass} placeholder="Middle name" name="otherName" value={form.otherName} onChange={handleChange} />
                    </div>
                    <div>
                        <label className={labelClass}>Gender</label>
                        <Select
                            options={genderOptions}
                            styles={selectStyles}
                            placeholder="Select gender"
                            onChange={(selected) => setForm({ ...form, gender: selected?.value || "" })}
                            value={genderOptions.find(opt => opt.value === form.gender) || null}
                        />
                    </div>
                </div>

                {/* Section — Contact */}
                <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-4 mt-6">
                    Contact & Location
                </p>
                <div className="grid grid-cols-2 gap-4 mb-4">
                    <div className="col-span-2">
                        <label className={labelClass}>Address</label>
                        <input className={inputClass} placeholder="Your full address" name="address" value={form.address} onChange={handleChange} />
                    </div>
                    <div>
                        <label className={labelClass}>State of Origin</label>
                        <input className={inputClass} placeholder="State" name="stateOfOrigin" value={form.stateOfOrigin} onChange={handleChange} />
                    </div>
                    <div>
                        <label className={labelClass}>Phone Number</label>
                        <input className={inputClass} placeholder="+91 XXXXX XXXXX" name="phoneNumber" value={form.phoneNumber} onChange={handleChange} />
                    </div>
                    <div className="col-span-2">
                        <label className={labelClass}>Alternative Phone <span className="text-gray-300 normal-case font-normal">(optional)</span></label>
                        <input className={inputClass} placeholder="Alternative number" name="alternativePhoneNumber" value={form.alternativePhoneNumber} onChange={handleChange} />
                    </div>
                </div>

                {/* Section — Account */}
                <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-4 mt-6">
                    Account Credentials
                </p>
                <div className="grid grid-cols-2 gap-4 mb-6">
                    <div>
                        <label className={labelClass}>Email</label>
                        <input type="email" className={inputClass} placeholder="you@example.com" name="email" value={form.email} onChange={handleChange} />
                    </div>
                    <div>
                        <label className={labelClass}>Password</label>
                        <input type="password" className={inputClass} placeholder="••••••••" name="password" value={form.password} onChange={handleChange} />
                    </div>
                </div>

                {/* Error */}
                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-500 rounded-xl px-4 py-3 text-sm mb-4">
                        {error}
                    </div>
                )}

                {/* Submit */}
                <button
                    onClick={handleRegister}
                    disabled={loading}
                    className="w-full bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold py-3 rounded-xl transition-all shadow-lg shadow-blue-200 mb-6 flex items-center justify-center gap-2"
                >
                    {loading ? (
                        <>
                            <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                            </svg>
                            Creating account...
                        </>
                    ) : "Create Account"}
                </button>

                <p className="text-center text-gray-400 text-sm">
                    Already have an account?{' '}
                    <a href="/login" className="text-blue-500 hover:text-purple-500 font-medium transition-colors">
                        Login
                    </a>
                </p>
            </div>
        </div>
    );
}

export default Register;