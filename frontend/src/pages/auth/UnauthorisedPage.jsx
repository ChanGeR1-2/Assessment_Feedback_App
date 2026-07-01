import { Link } from "react-router";

const UnauthorisedPage = () => {
    return (
        <main>
            <h1>Unauthorised</h1>
            <p>You do not have permission to view this page.</p>
            <Link className="button-link" to="/login">
                Go to login
            </Link>
        </main>
    );
};

export default UnauthorisedPage;