import {apiFetch} from "./utils.js";

export async function getAllTags() {
    return await apiFetch(`/api/tags`);
}

export async function getStudentTagCount({studentId}) {
    return await apiFetch(`/api/students/${studentId}/tag-summary`);
}

export async function getLecturerTagCount({ moduleId } = {}) {
    const params = new URLSearchParams();
    if (moduleId) params.append("moduleId", moduleId);
    const query = params.toString();
    return apiFetch(`/api/lecturer/tag-summary${query ? `?${query}` : ""}`);
}
