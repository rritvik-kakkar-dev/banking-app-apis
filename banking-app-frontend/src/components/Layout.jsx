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
    const [accountLoading, setAccountLoading] = useState(true);

    const resolveAccount = async () => {
        const storedAccountNumber = localStorage.getItem("accountNumber");
        const storedAccountId = localStorage.getItem("accountId");
        if (storedAccountNumber && storedAccountId) {
            return { accountNumber: storedAccountNumber, id: storedAccountId };
        }

        try {
            const response = await api.get("/api/accounts", {
                params: {
                    page: 0,
                    limit: 10,
                    sortBy: "createdAt",
                    sortOrder: "DESC"
                }
            });

            const accountNumber = response.data?.content?.[0]?.accountNumber;
            if (accountNumber) {
                localStorage.setItem("accountNumber", String(accountNumber));
                localStorage.setItem("accountId", String(response.data?.content?.[0]?.id));
                return response.data?.content?.[0];
            }
        } catch (error) {
            console.error("Failed to resolve account number", error);
        }

        return null;
    };

    const fetchAccountInfo = async (selectedAccountNumber = null) => {
        setAccountLoading(true);

        try {
            let targetAccountNumber = selectedAccountNumber || localStorage.getItem("accountNumber");

            if (!targetAccountNumber) {
                const account = await resolveAccount();
                targetAccountNumber = account?.accountNumber;
            }

            if (!targetAccountNumber) {
                setAccountInfo(null);
                return;
            }

            const response = await api.get("/api/accounts", {
                params: {
                    page: 0,
                    limit: 10,
                    sortBy: "createdAt",
                    sortOrder: "DESC"
                }
            });

            const accounts = response.data?.content || [];
            const matchedAccount = accounts.find(
                (account) => String(account.accountNumber) === String(targetAccountNumber)
            ) || accounts[0];

            if (!matchedAccount) {
                setAccountInfo(null);
                return;
            }

            localStorage.setItem("accountNumber", String(matchedAccount.accountNumber));
            localStorage.setItem("accountId", String(matchedAccount.id));
            setAccountInfo(matchedAccount);
        } catch (error) {
            console.error("Failed to fetch account info", error);
            setAccountInfo(null);
        } finally {
            setAccountLoading(false);
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
                    accountLoading,
                    refreshAccountInfo: fetchAccountInfo
                }}
            />
        </>
    );
}

export default Layout;