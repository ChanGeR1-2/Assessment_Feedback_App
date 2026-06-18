import {NavLink} from "react-router";
import "./Navbar.css";

const Navbar = () => {
    const currentUser = JSON.parse(localStorage.getItem("currentUser"));
    return (
        <header className="navbar">
            <div className="navbar-brand">
                Assessment Feedback
            </div>

            <div>
                Logged in as {currentUser?.role}
            </div>

            <nav className="navbar-links">
                {currentUser?.role === "ADMIN" && (
                    <>
                        <NavLink to={"/admin"}>Dashboard</NavLink>
                        <NavLink to={"/admin/users"}>Users</NavLink>
                    </>
                )}
            </nav>
        </header>
    )
};

export default Navbar;