import {apiFetch} from "./utils.js";

export async function getAllTags() {
    return await apiFetch(`/api/tags`);
};

export async function getStudentTagCount({studentId}) {
    return await apiFetch(`/api/students/${studentId}/tag-summary`);
}
