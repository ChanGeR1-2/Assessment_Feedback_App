export async function handleResponse(res) {
    const contentType = res.headers.get("content-type");
    const hasJsonBody = contentType && contentType.includes("application/json");

    const data = hasJsonBody ? await res.json() : null;

    if (!res.ok) {
        const message = data?.message || "Something went wrong";
        throw new Error(message);
    }

    return data;
}