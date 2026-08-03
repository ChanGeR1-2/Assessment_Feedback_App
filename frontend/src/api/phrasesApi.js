import {apiFetch} from "./utils.js";

export const getMyPhrases = () => {
    return apiFetch(`/api/phrases`);
};

export const createPhrase = ({phrase}) => {
    return apiFetch(`/api/phrases`, {
        method: "POST",
        body: JSON.stringify(phrase)
    });
};

export const updatePhrase = ({phraseId, phrase}) => {
    return apiFetch(`/api/phrases/${phraseId}`, {
        method: "PUT",
        body: JSON.stringify(phrase)
    });
};

export const deletePhrase = ({phraseId}) => {
    return apiFetch(`/api/phrases/${phraseId}`, {
        method: "DELETE"
    });
}
