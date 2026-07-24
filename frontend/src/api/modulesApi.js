import {apiFetch} from "./utils.js";

export async function getLecturerModules() {
    return await apiFetch(`/api/modules`)
}

export async function getModuleById({moduleId}) {
    return await apiFetch(`/api/modules/${moduleId}`);
}

export async function getStudentModules({studentId}) { return await apiFetch(`/api/students/${studentId}/modules`); }