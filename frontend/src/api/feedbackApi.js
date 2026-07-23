import {apiFetch} from "./utils.js";

export async function getFeedbackByStudentIdAndAssessmentId ({assessmentId, studentId}){
    return await apiFetch(`/api/assessments/${assessmentId}/students/${studentId}/feedback`);
}

export async function getFeedbackById({feedbackId}){
    return await apiFetch(`/api/feedback/${feedbackId}`);
}

export async function getFeedbackByStudentId ({studentId}){
    return await apiFetch(`/api/students/${studentId}/feedback`);
}

export async function submitFeedback ({feedback, lecturerId}){
    const params = new URLSearchParams();
    params.append("lecturerId", lecturerId);
    const query = params.toString();

    return await apiFetch(`/api/feedback${query ? `?${query}` : ""}`, {
        method: "POST",
        body: JSON.stringify( feedback )
    });

}

