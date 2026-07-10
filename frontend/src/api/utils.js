export async function handleResponse(res) {
    const contentType = res.headers.get("content-type");
    const hasJsonBody = contentType && contentType.includes("application/json");

    return hasJsonBody ? await res.json() : null;
}