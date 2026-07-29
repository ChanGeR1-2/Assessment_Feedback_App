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
}