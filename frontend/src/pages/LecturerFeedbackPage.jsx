import { useParams } from "react-router";
import { useEffect, useState } from "react";
import { getFeedbackByStudentIdAndAssessmentId } from "../api/feedbackApi.js";
import { notifications } from "@mantine/notifications";
import {Group, Loader, Stack, Text} from "@mantine/core";
import FeedbackForm from "../components/FeedbackForm.jsx";
import { getAssessmentById } from "../api/assessmentsApi.js";
import { getUserById } from "../api/usersApi.js";
import FeedbackDetail from "../components/FeedbackDetail.jsx";
import {LecturerFeedbackBreadcrumbs} from "../components/FeedbackBreadcrumbs.jsx";

const LecturerFeedbackPage = () => {
    const { assessmentId, studentId } = useParams();
    const [loading, setLoading] = useState(true);
    const [feedback, setFeedback] = useState(null);
    const [assessment, setAssessment] = useState({ markingItems: [] });
    const [student, setStudent] = useState({});

    useEffect(() => {
        let cancelled = false;

        const load = async () => {
            setLoading(true);
            try {
                const [assessmentData, studentData, feedbackData] = await Promise.all([
                    getAssessmentById({ assessmentId }),
                    getUserById({ userId: studentId }),
                    getFeedbackByStudentIdAndAssessmentId({ assessmentId, studentId })
                ]);

                if (!cancelled) {
                    setFeedback(feedbackData);
                    setAssessment(assessmentData);
                    setStudent(studentData);
                }
            } catch (error) {
                if (!cancelled) {
                    notifications.show({
                        title: "Error",
                        message: error.message,
                        color: "red",
                    });
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        load();
        return () => {
            cancelled = true;
        };
    }, [assessmentId, studentId]);

    const onFormSubmit = (newFeedback) => {
        setFeedback(newFeedback);
        notifications.show({
            title: "Feedback submitted",
            message: "Your feedback has been submitted successfully.",
        });
    };

    const markingItems = assessment.markingItems ?? [];

    if (loading) {
        return (
            <Group justify="center" p="xl">
                <Loader />
            </Group>
        );
    }

    if (feedback?.status === "PUBLISHED") {
        return (
            <Stack gap="lg">
                <LecturerFeedbackBreadcrumbs feedback={feedback} />
                <FeedbackDetail feedback={feedback} />
            </Stack>
        )
    }

    if (markingItems.length === 0) {
        return (
            <Text c="dimmed">
                This assessment has no marking items yet. Add them before giving feedback.
            </Text>
        );
    }

    return (
        <FeedbackForm
            key={feedback?.id ?? "new"}
            assessmentId={assessmentId}
            assessmentTitle={assessment.title}
            studentId={studentId}
            studentFullName={student.fullName}
            markingItems={markingItems}
            onSubmit={onFormSubmit}
            feedback={feedback}
            moduleId={assessment.moduleId}
            moduleTitle={assessment.moduleTitle}
        />
    );
};

export default LecturerFeedbackPage;