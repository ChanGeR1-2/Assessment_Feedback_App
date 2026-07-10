import {handleResponse} from "./utils.js";

export async function getStudentFeedback ({assessmentId, studentId}){
    const response = await fetch(`/api/assessments/${assessmentId}/students/${studentId}/feedback`);
    const data = await handleResponse(response);
    return data.message ? null: data;
}

export async function submitFeedback ({feedback, lecturerId}){
    const params = new URLSearchParams();
    params.append("lecturerId", lecturerId);
    const query = params.toString();
    const response = await fetch(`/api/feedback${query ? `?${query}` : ""}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify( feedback ),
    });

    return handleResponse(response);
}

