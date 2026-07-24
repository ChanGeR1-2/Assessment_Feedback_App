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

export async function submitFeedback ({feedback}){
    return await apiFetch(`/api/feedback`, {
        method: "POST",
        body: JSON.stringify( feedback )
    });

}

