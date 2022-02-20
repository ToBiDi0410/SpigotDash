const SFC_OPTIONS = {
    moduleCache: {
        vue: Vue
    },

    async getFile(url) {
        var res;
        setAppLoading("SFC", true);
        console.debug("[RESSOURCES] Resolving Ressource URL: " + url);

        if (url.includes("@assets")) {
            url = url.replace("@assets", "../../assets")
        }

        if (!url.includes("://")) {
            console.debug("[RESSOURCES] Loading Ressource (Local): " + url);
            res = await fetch("./assets/vueComponents/" + url);
        } else {
            console.debug("[RESSOURCES] Loading Ressource (Cross): " + url);
            res = await fetch(url);
        }

        setAppLoading("SFC", false);
        if (!res.ok)
            throw Object.assign(new Error(res.statusText + ' ' + url), { res });
        return {
            getContentData: asBinary => asBinary ? res.arrayBuffer() : res.text(),
        }
    },

    addStyle(textContent) {
        const style = Object.assign(document.createElement('style'), { textContent });
        const ref = document.head.getElementsByTagName('style')[0] || null;
        document.head.insertBefore(style, ref);
    },
}

const { loadModule } = window['vue3-sfc-loader'];