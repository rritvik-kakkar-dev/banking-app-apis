import { Outlet, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import Navbar from "./header/Navbar";
import api from "../services/api";

function Layout() {
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) {
            navigate("/");
        }
    }, []);

    const [accountInfo, setAccountInfo] = useState(null);

    const fetchAccountInfo = async () => {
        try {
            const accountNumber = localStorage.getItem("accountNumber");

            const response = await api.get(
                "/api/user/balanceEnquiry",
                {
                    params: { accountNumber }
                }
            );

            setAccountInfo(response.data.accountInfo);
        } catch (error) {
            console.error("Failed to fetch account info", error);
        }
    };

    useEffect(() => {
        fetchAccountInfo();
    }, []);

    return (
        <>
            <Navbar accountInfo={accountInfo} />

            <Outlet
                context={{
                    accountInfo,
                    refreshAccountInfo: fetchAccountInfo
                }}
            />
        </>
    );
}

export default Layout;