import Navbar from "../components/layout/Navbar.jsx";
import {Outlet} from "react-router";

const AdminDashboard = () => {
    return (
        <main className="App">
            <Navbar />
            <Outlet />
        </main>
    )
};

export default AdminDashboard;