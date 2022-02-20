async function INIT_VUE() {
    const app = Vue.createApp({
        components: {
            'main-view': Vue.defineAsyncComponent(() => loadModule('main-view.vue', SFC_OPTIONS))
        },
        template: '<main-view></main-view>'
    });

    app.config.globalProperties.window = window;
    app.mount('#app');
}

async function INIT_VUE_ENVIRONMENT() {
    var store = Vue.reactive({});
    window.store = store;

    window.loadExternalScript = async function(src) {
        let apexScript = document.createElement("script");
        apexScript.setAttribute("src", src);
        var child = document.head.appendChild(apexScript);

        setAppLoading(true);
        var prom = new Promise((resolve, reject) => {
            child.onload = resolve();
            child.onerror = reject();
        });

        prom.then(() => { setAppLoading(false); })
        return prom;
    }
}