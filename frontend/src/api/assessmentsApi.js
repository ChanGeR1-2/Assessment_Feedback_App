import {apiFetch} from "./utils.js";

export const getAssessmentsByModuleId = async ({ moduleId }) =>
    apiFetch(`/api/modules/${moduleId}/assessments`);

export const getAssessmentById = async ({ assessmentId }) =>
    apiFetch(`/api/assessments/${assessmentId}`);


export const createMarkingItem = async ({assessmentId,  markingItem}) => {
    return await apiFetch(`/api/assessments/${assessmentId}/marking-items`, {
        method: "POST",
        body: JSON.stringify( markingItem ),
    });
};

export const reorderMarkingItems = async({assessmentId, orderedIds}) => {
    console.log(orderedIds);
    return await apiFetch(`/api/assessments/${assessmentId}/marking-items/order`, {
        method: "PATCH",
        body: JSON.stringify({orderedIds} ),
    });
};

export const editMarkingItem = async ({assessmentId, markingItemId, markingItem}) => {
    return await apiFetch(`/api/assessments/${assessmentId}/marking-items/${markingItemId}`, {
        method: "PATCH",
        body: JSON.stringify( markingItem ),
    });
};

export const deleteMarkingItem = async ({assessmentId, markingItemId}) => {
    return await apiFetch(`/api/assessments/${assessmentId}/marking-items/${markingItemId}`, {
        method: "DELETE",
    });
};