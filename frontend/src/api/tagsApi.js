import {apiFetch} from "./utils.js";

export async function getAllTags() {
    return await apiFetch(`/api/tags`);
};
