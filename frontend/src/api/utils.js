

export async function apiFetch(url, options = {}) {
    const token = localStorage.getItem("token");

    let response;
    try {
        response = await fetch(url, {
            headers: {
                "Content-Type": "application/json",
                ...(token && { Authorization: `Bearer ${token}` }),
                ...options.headers,
            },
            ...options,
        });
    } catch {
        throw Object.assign(new Error("Unable to reach the server."), { status: 0 });
    }

    if (response.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("currentUser");
        window.location.href = "/login";
        return;
    }

    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw Object.assign(new Error(body.message ?? "Something went wrong"), {
            status: response.status,
            fieldErrors: body.errors,
        });
    }

    return response.status === 204 ? null : response.json();
}