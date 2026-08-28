import { useEffect, useState } from "react";
import { Group, Loader } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { getCurrentUser } from "./auth/currentUser.js";
import {getStudentModules} from "../api/modulesApi.js";
import ModulesDisplay from "../components/ModulesDisplay.jsx";

const StudentModulesPage = () => {
    const currentUser = getCurrentUser();
    const [modules, setModules] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        getStudentModules({ studentId: currentUser?.id })
            .then((data) => { if (!cancelled) setModules(data); })
            .catch((e) => { if (!cancelled) notifications.show({ title: "Error", message: e.message, color: "red" }); })
            .finally(() => { if (!cancelled) setLoading(false); });
        return () => { cancelled = true; };
    }, [currentUser?.id]);

    if (loading) return <Group justify="center" p="xl"><Loader /></Group>;

    return (
        <ModulesDisplay modules={modules} currentUser={currentUser} />
    );
};

export default StudentModulesPage;