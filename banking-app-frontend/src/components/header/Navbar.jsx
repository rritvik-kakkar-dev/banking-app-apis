import profileImg from "../../assets/images/profile_img.jpg";
import { useNavigate, Link } from "react-router-dom";

function Navbar({ accountInfo }) {
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.clear();
        navigate("/login");
    };

    return (
        <>
            <nav className="bg-[#fefefe] border-b border-[#e8e8e3] px-6 py-4 flex justify-between items-center shadow-sm">
                <div className="flex items-center gap-3">

                    <Link
                        to="/dashboard"
                        state={{ accountInfo }}
                        className="flex items-center gap-2"
                    >
                        <div className="w-8 h-8 rounded-xl bg-gradient-to-r from-blue-500 to-purple-500 flex items-center justify-center text-white text-sm font-bold">
                            V
                        </div>
                        <h1 className="text-gray-900 font-bold text-lg">Vaulta</h1>
                    </Link>

                </div>
                <div className="flex items-center gap-4">
                    <Link
                        to="/profile"
                        state={{ accountInfo }}
                        className="flex items-center gap-2"
                    >
                        <img src={profileImg} alt="Profile" className="w-8 h-8 rounded-full" />
                        <span className="text-gray-400 text-sm">
                            {accountInfo?.accountName}
                        </span>
                    </Link>
                    <button
                        onClick={handleLogout}
                        className="text-sm font-medium px-4 py-2 rounded-xl border border-red-100 text-red-400 hover:bg-red-50 transition-all"
                    >
                        Logout
                    </button>
                </div>
            </nav>
        </>
    );
};

export default Navbar;