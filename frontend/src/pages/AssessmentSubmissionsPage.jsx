import {useNavigate, useParams} from "react-router";
import {useEffect, useState} from "react";
import {getCurrentUser} from "./auth/currentUser.js";
import {getStudents} from "../api/usersApi.js";
import {notifications} from "@mantine/notifications";
import {Badge, Button, Group, Loader, Modal, Paper, Select, Stack, Table, Text, Title} from "@mantine/core";
import {getAssessmentById} from "../api/assessmentsApi.js";
import {getModuleById} from "../api/modulesApi.js";

const AssessmentSubmissionsPage = () => {
    const currentUser = getCurrentUser();
    const navigate = useNavigate();
    const { assessmentId, moduleId} = useParams();
    const [students, setStudents] = useState([]);
    const [assessment, setAssessment] = useState({});
    const [module, setModule] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadData = async () => {
            setLoading(true);
            try {
                const studentsData = await getStudents({moduleId, lecturerId: currentUser.id});
                setStudents(studentsData);
                console.log(studentsData);
                const assessmentData = await getAssessmentById({assessmentId});
                setAssessment(assessmentData);
                const moduleData = await getModuleById({moduleId});
                setModule(moduleData);
            } catch(error) {
                notifications.show({
                    title: "Error",
                    message: error.message,
                    color: "red"
                })
            } finally {
                setLoading(false);
            }
        }
        loadData();
    }, [moduleId, currentUser?.id, assessmentId]);

    return (
        <Stack gap="lg">
            <div>
                <Title order={1}>{module.code}-{module.title}-{module.academicYear}:  {assessment.title}</Title>
                <Text c="dimmed">View and create feedback</Text>
            </div>

            <Paper withBorder radius="md" style={{ overflow: "hidden" }}>
                {loading ? (
                    <Group justify="center" p="xl">
                        <Loader />
                    </Group>
                ) : students.length === 0 ? (
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
                            {students.map((student) => (
                                <Table.Tr
                                    key={student.id}
                                    onClick={() => navigate(`/modules/${moduleId}/assessments/${assessment.id}/students/${student.id}/feedback`)}
                                    style={{cursor: "pointer"}}
                                >
                                    <Table.Td>{student.fullName}</Table.Td>
                                    <Table.Td>{student.email}</Table.Td>
                                    <Table.Td>
                                        <Badge variant="light">{student.feedback ? "COMPLETE" : "TODO"}</Badge>
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