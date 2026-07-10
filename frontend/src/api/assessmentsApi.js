import {handleResponse} from "./utils.js";

export const getAssessmentsByModuleId = async ({moduleId}) => {
    const response = await fetch(`/api/modules/${moduleId}/assessments`);
    return await handleResponse(response);
};

export const getAssessmentById = async ({assessmentId}) => {
    const response = await fetch(`/api/assessments/${assessmentId}`);
    return handleResponse(response);
}