import {Link, useParams} from "react-router";
import { useEffect, useState } from "react";
import { getFeedbackByStudentIdAndAssessmentId } from "../api/feedbackApi.js";
import { notifications } from "@mantine/notifications";
import {Anchor, Breadcrumbs, Button, Group, Loader, Paper, Stack, Text} from "@mantine/core";
import FeedbackForm from "../components/FeedbackForm.jsx";
import { getAssessmentById } from "../api/assessmentsApi.js";
import { getUserById } from "../api/usersApi.js";
import FeedbackDetail from "../components/FeedbackDetail.jsx";
import {LecturerFeedbackBreadcrumbs} from "../components/FeedbackBreadcrumbs.jsx";
import NotFoundPage from "./NotFoundPage.jsx";
import UnauthorisedPage from "./auth/UnauthorisedPage.jsx";

const LecturerFeedbackPage = () => {
    const { assessmentId, studentId } = useParams();
    const [loading, setLoading] = useState(true);
    const [feedback, setFeedback] = useState(null);
    const [assessment, setAssessment] = useState({ markingItems: [] });
    const [student, setStudent] = useState({});
    const [notFound, setNotFound] = useState(false);
    const [forbidden, setForbidden] = useState(false);

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
                    if (error.status === 404) { setNotFound(true); return; }
                    if (error.status === 403) { setForbidden(true); return; }
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
        return <Group justify="center" p="xl"><Loader /></Group>;
    }

    if (notFound) {
        return <NotFoundPage message="That feedback doesn't exist, or you don't have access to it." />;
    }

    if (forbidden) {
        return <UnauthorisedPage />;
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
            <Stack gap="lg">
                <Breadcrumbs>
                    <Anchor component={Link} to={`/modules/${assessment.moduleId}/assessments`} size="sm">
                        {assessment.moduleTitle ?? "Assessments"}
                    </Anchor>
                    <Text size="sm" c="dimmed">{assessment.title}</Text>
                </Breadcrumbs>

                <Paper withBorder radius="md" p="xl">
                    <Stack align="center" gap="xs">
                        <Text fw={600}>No marking scheme yet</Text>
                        <Text size="sm" c="dimmed" ta="center">
                            Feedback for this assessment is recorded against its marking criteria.
                            Add them before marking any students.
                        </Text>
                        <Button
                            component={Link}
                            to={`/modules/${assessment.moduleId}/assessments/${assessmentId}`}
                            mt="sm"
                        >
                            Set up marking scheme
                        </Button>
                    </Stack>
                </Paper>
            </Stack>
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