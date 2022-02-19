const store = Vue.reactive({});

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
window.store = store;

const app = Vue.createApp({
    components: {
        'main-view': Vue.defineAsyncComponent(() => loadModule('main-view.vue', SFC_OPTIONS))
    },
    template: '<main-view></main-view>'
});

app.config.globalProperties.window = window;
app.mount('#app');