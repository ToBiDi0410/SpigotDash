async function loadPage() {
    setAppLoading("LOADER", true);
    await wait(500);
    await INIT_VUE_ENVIRONMENT();
    await INIT_HELPERS();
    await INIT_API_DATA();
    await INIT_VUE();
    setAppLoading("LOADER", false);
    //var worker = new Worker("./assets/js/jsEncryptWorker.js");
    //worker.postMessage({ TYPE: "GEN_PAIR", JSEncrypt: JSEncrypt });
    //console.log(await waitForWorkerResponse(worker));
    //forge.pki.publicKeyFromPem();

}

async function waitForWorkerResponse(worker) {
    return new Promise((resolve, reject) => {
        worker.onmessage = function(e) {
            resolve(e);
        }
    })
}

var appLoadingVars = {};
const setAppLoading = function(namespace, val) {
    if (!val) {
        if (appLoadingVars[namespace]) delete appLoadingVars[namespace];
    } else {
        appLoadingVars[namespace] = true;
    }

    var foundLoading = Object.values(appLoadingVars).find((elem) => elem == true) != null;
    if (foundLoading) {
        if (document.querySelector("#app").style.display != "none") document.querySelector("#app").style.display = "none";
        if (document.querySelector("#appLoader").style.display != "block") document.querySelector("#appLoader").style.display = "block";
    } else {
        if (document.querySelector("#app").style.display != "block") document.querySelector("#app").style.display = "block";
        if (document.querySelector("#appLoader").style.display != "none") document.querySelector("#appLoader").style.display = "none";
    }
}

loadPage();