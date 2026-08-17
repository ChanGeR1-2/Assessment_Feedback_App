import { useRef, useState } from "react";

export function useAudioRecorder() {
    const [recording, setRecording] = useState(false);
    const [audioBlob, setAudioBlob] = useState(null);
    const [error, setError] = useState(null);
    const recorderRef = useRef(null);
    const chunksRef = useRef([]);

    const start = async () => {
        setError(null);
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            const recorder = new MediaRecorder(stream);
            chunksRef.current = [];
            recorder.ondataavailable = (e) => {
                if (e.data.size > 0) chunksRef.current.push(e.data);
            };
            recorder.onstop = () => {
                setAudioBlob(new Blob(chunksRef.current, { type: "audio/webm" }));
                stream.getTracks().forEach((t) => t.stop());
            };
            recorder.start();
            recorderRef.current = recorder;
            setRecording(true);
        } catch (e) {
            setError("Microphone access was denied or is unavailable.");
        }
    };

    const stop = () => {
        recorderRef.current?.stop();
        setRecording(false);
    };

    const reset = () => setAudioBlob(null);

    return { recording, audioBlob, error, start, stop, reset };
}