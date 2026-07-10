import {handleResponse} from "./utils.js";

export async function getUsers({role} = {}) {
    const params = new URLSearchParams();
    if (role) {
        params.append("role", role);
    }
    const query = params.toString()
    const response = await fetch(`/api/users${query ? `?${query}` : ""}`);
    return handleResponse(response);
}

export async function getUserById({userId}) {
    const response = await fetch(`/api/users/${userId}`);
    return handleResponse(response);
}

export async function getStudents({moduleId, lecturerId}) {
    const params = new URLSearchParams();
    params.append("moduleId", moduleId);
    params.append("lecturerId", lecturerId);
    const query = params.toString();


    const enrolmentResponse = await fetch(`/api/enrolments${query ? `?${query}` : ""}`);
    const students =  await handleResponse(enrolmentResponse);

    const feedbackResponse = await fetch(`/api/assessments/${moduleId}/feedback`);
    const feedback =  await handleResponse(feedbackResponse);

    students.map(student => {
        student.feedback = feedback.find(f => f.studentId === student.id);
        return student;
    });

    return students;
}

export async function createUser(userData) {
    const response = await fetch(`/api/users`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify( userData ),
    });

    return handleResponse(response);
}