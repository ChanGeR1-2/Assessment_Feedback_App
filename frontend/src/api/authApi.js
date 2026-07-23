import {apiFetch} from "./utils.js";

export async function loginUser(loginData) {
    return await apiFetch(`api/auth/login`, {
        method: "POST",
        body: JSON.stringify(loginData)
    })
}