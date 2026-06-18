import {useEffect, useState} from "react";
import {getUsers} from "../api/usersApi.js";

const UsersPage = () => {
    const [users, setUsers] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [role, setRole] = useState("");

    useEffect(() => {
        async function loadUsers() {
            try {
                const data = await getUsers();
                console.log(data);
                setUsers(data);
                setError("");
            } catch (error) {
                setError(error.message);
                setUsers([]);
            } finally {
                setLoading(false);
            }
        }

        loadUsers();
    }, []);

    if (loading) {
        return (
            <p>Loading... </p>
        )
    }

    if (error) return <p>{error}</p>;

    return (
        <div>
            <h1>Users</h1>
            <label htmlFor="role">Role</label>
            <select name="role" id="role" value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="">All</option>
                <option value="LECTURER">Lecturer</option>
                <option value="ADMIN">Admin</option>
                <option value="STUDENT">Student</option>
            </select>
            <ul>
                {users.filter(user => user.role === role || role === "")
                    .map((user) => (
                        <li key={user.id}>
                            {user.fullName} - {user.email} - {user.role}
                        </li>
                    ))}
            </ul>
        </div>
    )
};

export default UsersPage;