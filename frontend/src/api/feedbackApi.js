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

export async function submitFeedback ({feedback, publish}){
    const params = new URLSearchParams();
    params.append("publish", publish);
    const query = params.toString();
    return await apiFetch(`/api/feedback${query ? `?${query}` : ""}`, {
        method: "POST",
        body: JSON.stringify( feedback )
    });
}

export async function updateFeedback ({feedback, feedbackId, publish}){
    const params = new URLSearchParams();
    params.append("publish", publish);
    const query = params.toString();
    return await apiFetch(`/api/feedback/${feedbackId}${query ? `?${query}` : ""}`, {
        method: "PUT",
        body: JSON.stringify( feedback )
    });
}

export async function publishFeedback ({feedbackId}){
    return await apiFetch(`/api/feedback/${feedbackId}/publish`, {
        method: "PUT"
    });
}

export async function createFeedbackQuery({feedbackId, query}){
    console.log(query);
    return await apiFetch(`/api/feedback/${feedbackId}/feedback-queries`, {
        method: "POST",
        body: JSON.stringify({query})
    });
}

export async function getFeedbackQueryByFeedbackId({feedbackId}){
    return await apiFetch(`/api/feedback/${feedbackId}/feedback-queries`);
}

export const getUnansweredQueries = async () =>
    apiFetch("/api/lecturer/feedback-queries");

export const answerFeedbackQuery = async ({ feedbackQueryId, answer }) => {
    return apiFetch(`/api/feedback-queries/${feedbackQueryId}/answer`, {
        method: "POST",
        body: JSON.stringify({ answer }),
    });
};

export const publishFeedbackList = async ({ studentIds, assessmentId }) => {
    return apiFetch(`/api/assessments/${assessmentId}/feedback/publish`, {
        method: "PATCH",
        body: JSON.stringify({ studentIds }),
    });
};

export const getStudentFeedbackQueries = async ({ studentId }) => apiFetch(`/api/students/${studentId}/feedback-queries`);