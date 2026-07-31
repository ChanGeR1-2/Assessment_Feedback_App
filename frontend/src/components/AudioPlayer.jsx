import { useEffect, useState } from "react";
import { Loader, Stack, Text } from "@mantine/core";

function AudioPlayer({ feedbackId, label, refreshKey = 0 }) {
    const [url, setUrl] = useState(null);
    const [state, setState] = useState("loading"); // loading | ready | none | error

    useEffect(() => {
        let objectUrl;
        let cancelled = false;

        (async () => {
            setState("loading");
            try {
                const res = await fetch(`/api/feedback/${feedbackId}/audio?v=${refreshKey}`, {
                    headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                });
                if (res.status === 404) {
                    if (!cancelled) setState("none");
                    return;
                }
                if (!res.ok) throw new Error();
                const blob = await res.blob();
                objectUrl = URL.createObjectURL(blob);
                if (!cancelled) {
                    setUrl(objectUrl);
                    setState("ready");
                }
            } catch {
                if (!cancelled) setState("error");
            }
        })();

        return () => {
            cancelled = true;
            if (objectUrl) URL.revokeObjectURL(objectUrl);
        };
    }, [feedbackId, refreshKey]);

    if (state === "none" || state === "error") return null;
    if (state === "loading") return <Loader size="sm" />;

    return (
        <Stack gap={4}>
            {label && (
                <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
                    {label}
                </Text>
            )}
            <audio controls src={url} />
        </Stack>
    );
}

export default AudioPlayer;