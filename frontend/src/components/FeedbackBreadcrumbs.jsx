import { Anchor, Breadcrumbs, Text } from "@mantine/core";
import { Link } from "react-router";

export const LecturerFeedbackBreadcrumbs = ({ feedback }) => (
    <Breadcrumbs>
        <Anchor component={Link} to={`/modules/${feedback.moduleId}/assessments`} size="sm">
            {feedback.moduleTitle ?? "Assessments"}
        </Anchor>
        <Anchor
            component={Link}
            to={`/modules/${feedback.moduleId}/assessments/${feedback.assessmentId}/students`}
            size="sm"
        >
            {feedback.assessmentTitle ?? "Submissions"}
        </Anchor>
        <Text size="sm" c="dimmed">{feedback.studentFullName ?? "Student"}</Text>
    </Breadcrumbs>
);

export const StudentFeedbackBreadcrumbs = ({ feedback }) => (
    <Breadcrumbs>
        <Anchor component={Link} to="/my-modules" size="sm">Modules</Anchor>
        <Anchor component={Link} to={`/my-modules/${feedback.moduleId}/feedback`} size="sm">
            {feedback.moduleTitle ?? "Assessments"}
        </Anchor>
        <Text size="sm" c="dimmed">{feedback.assessmentTitle}</Text>
    </Breadcrumbs>
);