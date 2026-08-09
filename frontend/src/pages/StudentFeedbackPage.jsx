import {useParams} from "react-router";
import {useEffect, useState} from "react";
import {getFeedbackById} from "../api/feedbackApi.js";
import {notifications} from "@mantine/notifications";
import {Group, Loader, Stack, Title} from "@mantine/core";
import FeedbackQuerySection from "../components/FeedbackQuerySection.jsx";
import {StudentFeedbackBreadcrumbs} from "../components/FeedbackBreadcrumbs.jsx";
import FeedbackDetail from "../components/FeedbackDetail.jsx";

const StudentFeedbackPage = () => {
    const {id} = useParams();
    const [loading, setLoading] = useState(true);
    const [feedback, setFeedback] = useState(null);
    const [notFound, setNotFound] = useState(false);

    useEffect(() => {
        let cancelled = false;
        const load = async () => {
            setLoading(true);
            try {
                const data = await getFeedbackById({feedbackId: id});
                if (!cancelled) {
                    setFeedback(data);
                }
            } catch (error) {
                if (!cancelled) {
                    if (error.status === 404) {
                        setNotFound(true);
                    }
                    notifications.show({
                        title: "Error",
                        message: error.message,
                        color: "red"
                    })
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        }
        load();
    }, [id]);

    if (loading) {
        return (
            <Group justify="center" p="xl">
                <Loader />
            </Group>
        );
    }

    if (notFound) {
        return (
            <Group justify = "center" p = "xl">
                <Title>Feedback not found</Title>
            </Group>
        )
    }
    return (
        <Stack gap="lg">
            <StudentFeedbackBreadcrumbs feedback={feedback} />
            <FeedbackDetail feedback={feedback} />
            <FeedbackQuerySection feedbackId={feedback.id} />
        </Stack>
    )
}
export default StudentFeedbackPage;