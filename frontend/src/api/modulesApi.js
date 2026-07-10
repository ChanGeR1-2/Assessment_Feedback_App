import {handleResponse} from "./utils.js";

export async function getModules({lecturerId} = {}) {
    const params = new URLSearchParams();
    if (lecturerId) {
        params.append("lecturerId", lecturerId);
    }
    const query = params.toString()
    const response = await fetch(`/api/modules${query ? `?${query}` : ""}`);
    return handleResponse(response);
}

export async function getModuleById({moduleId}) {
    const response = await fetch(`/api/modules/${moduleId}`);
    return handleResponse(response);
}