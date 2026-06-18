import {handleResponse} from "./utils.js";

export async function getUsers() {
    const response = await fetch(`/api/users`);
    return handleResponse(response);
}

export async function createUser(userData) {
    const response = await fetch(`/api/users`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify( userData ),
    });

    return handleResponse(response);
}