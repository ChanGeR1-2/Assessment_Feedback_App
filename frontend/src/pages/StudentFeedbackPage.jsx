import {useParams} from "react-router";
import {getCurrentUser} from "./auth/currentUser.js";
import {useEffect, useState} from "react";
import {getStudentFeedback} from "../api/feedbackApi.js";
import {notifications} from "@mantine/notifications";
import {Loader} from "@mantine/core";
import StudentFeedbackSection from "../components/StudentFeedbackSection.jsx";
import CreateFeedbackForm from "../components/CreateFeedbackForm.jsx";
import {getAssessmentById} from "../api/assessmentsApi.js";
import {getUserById} from "../api/usersApi.js";

const StudentFeedbackPage = () => {
    const currentUser = getCurrentUser();
    const { assessmentId, moduleId, studentId} = useParams();
    const [loading, setLoading] = useState(true);
    const [feedback, setFeedback] = useState(null);
    const [assessment, setAssessment] = useState({});
    const [student, setStudent] = useState({});

    useEffect(() => {
        const loadFeedback = async () => {
            setLoading(true);
            try {
                const feedbackData = await getStudentFeedback({assessmentId, studentId});
                setFeedback(feedbackData);
                console.log(feedbackData);
                const assessmentData = await getAssessmentById({assessmentId});
                setAssessment(assessmentData);
                const student = await getUserById({userId: studentId});
                setStudent(student);
                setLoading(false);
            } catch (error) {
                notifications.show({
                    title: "Error",
                    message: error.message,
                    color: "red"
                });
            }
        }
        loadFeedback();
    }, [assessmentId, moduleId, studentId]);

    const onFormSubmit = (newFeedback) => {
        setFeedback(newFeedback);
        notifications.show({
            title: "Feedback submitted",
            message: "Your feedback has been submitted successfully."
        });
    }

    return loading ? (
        <Loader />
    ) : feedback ? (
        <StudentFeedbackSection feedback={feedback} />
    ) : (
        <CreateFeedbackForm assessmentTitle={assessment.title} studentFullName={student.fullName} onSubmit={onFormSubmit} assessmentId={assessmentId} studentId={studentId} lecturerId={currentUser?.id} />
    );
}
export default StudentFeedbackPage;