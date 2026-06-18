import {handleResponse} from "./utils.js";

export async function loginUser(loginData) {
    const response = await fetch('/api/auth/login', {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(loginData),
    });

    return handleResponse(response);
}