import {apiFetch} from "./utils.js";

export async function getUsers({role} = {}) {
    const params = new URLSearchParams();
    if (role) {
        params.append("role", role);
    }
    const query = params.toString()
    return await apiFetch(`/api/users${query ? `?${query}` : ""}`);
}

export async function getUserById({userId}) {
    return apiFetch(`/api/users/${userId}`)
}

export async function getStudents({moduleId, assessmentId}) {
    const params = new URLSearchParams();
    params.append("moduleId", moduleId);
    const query = params.toString();

    const students =  await apiFetch(`/api/enrolments${query ? `?${query}` : ""}`);
    const feedback =  await apiFetch(`/api/assessments/${assessmentId}/feedback`);

    students.map(student => {
        student.feedback = feedback.find(f => f.studentId === student.id);
        return student;
    });

    return students;
}

export async function createUser(userData) {
    return await apiFetch(`/api/users`, {
        method: "POST",
        body: JSON.stringify( userData ),
    });

}