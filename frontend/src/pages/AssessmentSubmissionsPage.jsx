import {Link, useNavigate, useParams} from "react-router";
import {useEffect, useState} from "react";
import {getCurrentUser} from "./auth/currentUser.js";
import {getStudents} from "../api/usersApi.js";
import {notifications} from "@mantine/notifications";
import {
    Anchor,
    Badge,
    Breadcrumbs,
    Button,
    Group,
    Loader,
    Modal,
    Paper,
    Select,
    Stack,
    Table,
    Text,
    TextInput,
    Title
} from "@mantine/core";
import {getAssessmentById} from "../api/assessmentsApi.js";
import {getModuleById} from "../api/modulesApi.js";

const AssessmentSubmissionsPage = () => {
    const currentUser = getCurrentUser();
    const navigate = useNavigate();
    const {assessmentId, moduleId} = useParams();
    const [students, setStudents] = useState([]);
    const [assessment, setAssessment] = useState({});
    const [module, setModule] = useState({});
    const [loading, setLoading] = useState(true);
    const [studentNameSearch, setStudentNameSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");

    useEffect(() => {
        let cancelled = false;
        const loadData = async () => {
            try {
                const [students, assessment, module] = await Promise.all([
                    getStudents({moduleId, assessmentId}),
                    getAssessmentById({assessmentId}),
                    getModuleById({moduleId})
                ]);

                if (!cancelled) {
                    setStudents(students);
                    setAssessment(assessment);
                    setModule(module);
                }
            } catch (error) {
                if (!cancelled) {
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
        loadData();
        return () => {
            cancelled = true;
        };

    }, [moduleId, currentUser?.id, assessmentId]);

    const filteredStudents = students.filter((student) => {
        const currentStatus = student.feedback?.status ?? "TODO";
        const statusMatches = statusFilter === "ALL" || currentStatus === statusFilter;
        const nameMatches = student.fullName.toLowerCase().trim()
            .includes(studentNameSearch.toLowerCase().trim());
        return statusMatches && nameMatches;
    });

    const statusColor = { TODO: "gray", DRAFT: "yellow", PUBLISHED: "teal" };

    return (
        <Stack gap="lg">
            <Breadcrumbs>
                <Anchor component={Link} to={`/modules/${moduleId}/assessments`} size="sm">
                    {assessment.moduleTitle ?? "Assessments"}
                </Anchor>
                <Text size="sm" c="dimmed">{assessment.title}</Text>
            </Breadcrumbs>
            <div>
                <Title order={1}>{module.code}-{module.title}-{module.academicYear}: {assessment.title}</Title>
                <Text c="dimmed">View and create feedback</Text>
            </div>

            <Group>
                <TextInput
                    label="Search by student name"
                    placeholder="Andrew Vine.."
                    value={studentNameSearch}
                    onChange={(e) => setStudentNameSearch(e.target.value)}
                />
                <Select
                    label="Status"
                    placeholder="Status"
                    data={['ALL', 'TODO', 'DRAFT', 'PUBLISHED']}
                    value={statusFilter}
                    onChange={setStatusFilter}
                />
            </Group>

            <Paper withBorder radius="md" style={{overflow: "hidden"}}>
                {loading ? (
                    <Group justify="center" p="xl">
                        <Loader/>
                    </Group>
                ) : filteredStudents.length === 0 ? (
                    <Text p="md" c="dimmed">No students found.</Text>
                ) : (
                    <Table striped highlightOnHover>
                        <Table.Thead>
                            <Table.Tr>
                                <Table.Th>Name</Table.Th>
                                <Table.Th>Email</Table.Th>
                                <Table.Th>Status</Table.Th>
                            </Table.Tr>
                        </Table.Thead>
                        <Table.Tbody>
                            {filteredStudents.map((student) => (
                                <Table.Tr
                                    key={student.id}
                                    onClick={() => navigate(`/modules/${moduleId}/assessments/${assessment.id}/students/${student.id}/feedback`)}
                                    style={{cursor: "pointer"}}
                                >
                                    <Table.Td>{student.fullName}</Table.Td>
                                    <Table.Td>{student.email}</Table.Td>
                                    <Table.Td>
                                        <Badge variant="light" color={statusColor[student.feedback?.status ?? "TODO"]}>
                                            {student.feedback?.status ?? "TODO"}
                                        </Badge>
                                    </Table.Td>
                                </Table.Tr>
                            ))}
                        </Table.Tbody>
                    </Table>
                )}
            </Paper>
        </Stack>
    )
};

export default AssessmentSubmissionsPage;