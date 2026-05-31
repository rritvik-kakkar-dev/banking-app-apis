import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8081",
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    }
);

api.interceptors.response.use(
    (response) => response, // pass through successful responses
    (error) => {
        if (error.response && error.response.status === 401) {
            localStorage.clear();
            alert("Session expired. Please login again.");
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);

export default api;