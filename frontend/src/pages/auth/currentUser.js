export function getCurrentUser() {
    const storedUser = localStorage.getItem("currentUser");

    if (!storedUser) {
        return null;
    }

    try {
        return JSON.parse(storedUser);
    } catch {
        localStorage.removeItem("currentUser");
        return null;
    }
}

export function setCurrentUser(user) {
    localStorage.setItem("currentUser", JSON.stringify(user));
}

export function clearCurrentUser() {
    localStorage.removeItem("currentUser");
}