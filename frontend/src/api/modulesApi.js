import {apiFetch} from "./utils.js";

export async function getLecturerModules({lecturerId} = {}) {
    const params = new URLSearchParams();
    if (lecturerId) {
        params.append("lecturerId", lecturerId);
    }
    const query = params.toString()
    return await apiFetch(`/api/modules${query ? `?${query}` : ""}`)
}

export async function getModuleById({moduleId}) {
    return await apiFetch(`/api/modules/${moduleId}`);
}

export async function getStudentModules({studentId}) { return await apiFetch(`/api/students/${studentId}/modules`); }