import './LandingPage.css'
import {NavLink} from "react-router";

function LandingPage() {
    return (
        <main>
            <h1>Assessment Feedback Application</h1>

            <p>
                A university assessment feedback system for students,
                lecturers and administrators.
            </p>

            <div>
                <NavLink to="/login">Login</NavLink>
            </div>
        </main>
    );
}

export default LandingPage;
