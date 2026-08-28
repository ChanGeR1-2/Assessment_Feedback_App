import {useEffect, useState} from "react";
import {getLecturerModules} from "../api/modulesApi.js";
import {notifications} from "@mantine/notifications";
import {getCurrentUser} from "./auth/currentUser.js";
import {Group, Loader} from "@mantine/core";
import ModulesDisplay from "../components/ModulesDisplay.jsx";

const LecturerModulesPage = () => {
    const currentUser = getCurrentUser();
    const [modules, setModules] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        const loadModules = async () => {
            try {
                const data = await getLecturerModules();
                if (!cancelled) setModules(data);
            } catch (error) {
                if (!cancelled) {
                    notifications.show({title: "Error", message: error.message, color: "red"});
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };
        loadModules();
        return () => {
            cancelled = true;
        };
    });

    if (loading) return <Group justify="center" p="xl"><Loader /></Group>;

    return (
        <ModulesDisplay modules={modules} currentUser={currentUser} />
    );
}

export default LecturerModulesPage