import { Link, useNavigate, useParams } from "react-router";
import { useEffect, useState } from "react";
import { notifications } from "@mantine/notifications";
import {
    Anchor, Badge, Breadcrumbs, Group, Loader, Paper, Select,
    Stack, Table, Text, TextInput, Title
} from "@mantine/core";
import { getStudents } from "../api/usersApi.js";
import { getAssessmentById } from "../api/assessmentsApi.js";
import { getModuleById } from "../api/modulesApi.js";
import NotFoundPage from "./NotFoundPage.jsx";
import UnauthorisedPage from "./auth/UnauthorisedPage.jsx";

const STATUS_COLOURS = { TODO: "gray", DRAFT: "yellow", PUBLISHED: "teal" };

const AssessmentSubmissionsPage = () => {
    const navigate = useNavigate();
    const { assessmentId, moduleId } = useParams();

    const [students, setStudents] = useState([]);
    const [assessment, setAssessment] = useState({});
    const [module, setModule] = useState({});
    const [loading, setLoading] = useState(true);
    const [studentNameSearch, setStudentNameSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [notFound, setNotFound] = useState(false);
    const [forbidden, setForbidden] = useState(false);

    useEffect(() => {
        let cancelled = false;

        const loadData = async () => {
            try {
                const [studentData, assessmentData, moduleData] = await Promise.all([
                    getStudents({ moduleId, assessmentId }),
                    getAssessmentById({ assessmentId }),
                    getModuleById({ moduleId }),
                ]);
                if (!cancelled) {
                    setStudents(studentData);
                    setAssessment(assessmentData);
                    setModule(moduleData);
                }
            } catch (error) {
                if (!cancelled) {
                    if (error.status === 404) { setNotFound(true); return; }
                    if (error.status === 403) { setForbidden(true); return; }
                    notifications.show({ title: "Error", message: error.message, color: "red" });
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        loadData();
        return () => { cancelled = true; };
    }, [moduleId, assessmentId]);

    const statusOf = (student) => student.feedback?.status ?? "TODO";

    // Unmarked students first — the ones still needing work — then alphabetically.
    const sorted = [...students].sort((a, b) => {
        const order = { TODO: 0, DRAFT: 1, PUBLISHED: 2 };
        const diff = order[statusOf(a)] - order[statusOf(b)];
        return diff !== 0 ? diff : a.fullName.localeCompare(b.fullName);
    });

    const filtered = sorted.filter((student) => {
        const statusMatches = statusFilter === "ALL" || statusOf(student) === statusFilter;
        const nameMatches = student.fullName.toLowerCase().trim()
            .includes(studentNameSearch.toLowerCase().trim());
        return statusMatches && nameMatches;
    });

    const filtersActive = statusFilter !== "ALL" || studentNameSearch.trim() !== "";

    const published = students.filter((s) => statusOf(s) === "PUBLISHED").length;
    const drafts = students.filter((s) => statusOf(s) === "DRAFT").length;

    const feedbackDue = assessment.feedbackDueDate
        ? new Date(assessment.feedbackDueDate).toLocaleDateString(undefined, {
            day: "numeric", month: "long", year: "numeric",
        })
        : null;

    if (loading) {
        return <Group justify="center" p="xl"><Loader /></Group>;
    }

    if (notFound) {
        return <NotFoundPage message="That assessment doesn't exist, or you don't have access to it." />;
    }

    if (forbidden) {
        return <UnauthorisedPage />;
    }

    return (
        <Stack gap="lg">
            <Breadcrumbs>
                <Anchor component={Link} to={`/modules/${moduleId}/assessments`} size="sm">
                    {module.title ?? "Assessments"}
                </Anchor>
                <Text size="sm" c="dimmed">{assessment.title}</Text>
            </Breadcrumbs>

            <div>
                <Title order={1}>{assessment.title}</Title>
                <Text c="dimmed">
                    {module.code} {module.title} ({module.academicYear})
                </Text>

                <Text size="sm" c="dimmed" mt={4}>
                    {published} of {students.length} marked
                    {drafts > 0 && `, ${drafts} in draft`}
                    {feedbackDue && ` · feedback due ${feedbackDue}`}
                </Text>

            </div>

            <Group align="flex-end" wrap="wrap">
                <TextInput
                    label="Search by student name"
                    placeholder="Search..."
                    value={studentNameSearch}
                    onChange={(e) => setStudentNameSearch(e.target.value)}
                    style={{ flex: "1 1 200px" }}
                />
                <Select
                    label="Status"
                    data={["ALL", "TODO", "DRAFT", "PUBLISHED"]}
                    value={statusFilter}
                    onChange={(value) => setStatusFilter(value ?? "ALL")}
                    allowDeselect={false}
                    w={160}
                />
            </Group>

            <Paper withBorder>
                {filtered.length === 0 ? (
                    <Text p="md" c="dimmed">
                        {filtersActive
                            ? "No students match these filters."
                            : "No students are enrolled on this module."}
                    </Text>
                ) : (
                    <Table.ScrollContainer minWidth={700}>
                        <Table striped highlightOnHover verticalSpacing="sm">
                            <Table.Thead>
                                <Table.Tr>
                                    <Table.Th>Name</Table.Th>
                                    <Table.Th>Email</Table.Th>
                                    <Table.Th w={120} ta="right">Mark</Table.Th>
                                    <Table.Th w={130}>Status</Table.Th>
                                </Table.Tr>
                            </Table.Thead>
                            <Table.Tbody>
                                {filtered.map((student) => {
                                    const feedback = student.feedback;
                                    const status = statusOf(student);
                                    const percentage =
                                        feedback?.totalMark
                                            ? Math.round((feedback.mark / feedback.totalMark) * 100)
                                            : null;

                                    return (
                                        <Table.Tr
                                            key={student.id}
                                            onClick={() =>
                                                navigate(`/modules/${moduleId}/assessments/${assessmentId}/students/${student.id}/feedback`)}
                                            style={{ cursor: "pointer" }}
                                        >
                                            <Table.Td fw={500}>{student.fullName}</Table.Td>
                                            <Table.Td c="dimmed">{student.email}</Table.Td>
                                            <Table.Td ta="right">
                                                {feedback ? (
                                                    <>
                                                        <Text size="sm" fw={500}>
                                                            {feedback.mark} / {feedback.totalMark}
                                                        </Text>
                                                        {percentage !== null && (
                                                            <Text size="xs" c="dimmed">{percentage}%</Text>
                                                        )}
                                                    </>
                                                ) : (
                                                    <Text c="dimmed">—</Text>
                                                )}
                                            </Table.Td>
                                            <Table.Td>
                                                <Badge variant="light" color={STATUS_COLOURS[status]}>
                                                    {status}
                                                </Badge>
                                            </Table.Td>
                                        </Table.Tr>
                                    );
                                })}
                            </Table.Tbody>
                        </Table>
                    </Table.ScrollContainer>

                )}
            </Paper>
        </Stack>
    );
};

export default AssessmentSubmissionsPage;