import { Outlet } from "react-router";
import Navbar from "./Navbar.jsx";
import "./AppLayout.css";

const AppLayout = () => {
    return (
        <div className="app-layout">
            <Navbar />

            <main className="app-main">
                <Outlet />
            </main>
        </div>
    );
};

export default AppLayout;