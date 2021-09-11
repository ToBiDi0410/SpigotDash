var currURLElem = document.querySelector("#currurl");
var currentMenu = null;
var contentContainer = null;

var PAGE_NAMES = {
    overview: "%T%OVERVIEW%T%",
    graphs: "%T%GRAPHS%T%",
    worlds: "%T%WORLDS%T%",
    console: "%T%CONSOLE%T%",
    controls: "%T%CONTROLS%T%",
    plugins: "%T%PLUGINS%T%",
    players: "%T%PLAYERS%T%",
    files: "%T%FILES%T%"
}

async function init() {
    try {
        await customEncryptor.init();
    } catch (err) {
        console.warn("[SECURITY] Failed to setup Encryptor!");
        console.error(err);
        Swal.fire({
            icon: "warning",
            title: "Encryption",
            html: "There was a Problem setting up Encryption for this connection!",
            allowClickOutside: false
        });
    }


    document.querySelectorAll(".menu-list>li>a").forEach((elem) => {
        elem.addEventListener("click", function(event) {
            loadPage(event.target.getAttribute("data-url").replace(".html", ""));
        });
    });

    theme = await getDataFromAPI({ method: "THEME" });

    console.log("[INDEX] Using Theme: " + theme);

    addNewTask("heightFillClass", heightFillRestClass, 1000);
    addNewTask("NOTIFICATIONS", refreshNotifications, 2000);

    loadPage("./pages/overview/overview");
    //loadPage("./pages/performance/worlds");

}

async function loadPage(url) {
    console.log("[LOADER] Loading Page '" + url + "'...");

    if (smartMenuHelpers.getConstructedByID(url) != null && !smartMenuHelpers.getConstructedByID(url).closed) {
        console.warn("[LOADER] Page already loaded");
        return;
    }

    curr_task = null;
    initPage = null;

    await smartMenuHelpers.closeAll();

    var pageName = PAGE_NAMES[url.split("/").latest().toLowerCase()];

    var menu = new smartMenu(url, pageName, pageName);
    menu.open();
    contentContainer = menu.getContentDOM();

    try {
        var data = await fetch("./bundledPage?page=" + url.replace("./", ""));
        data = await data.json();

        contentContainer.innerHTML += data.HTML;

        //CSS
        var styleSheet = document.createElement("style");
        styleSheet.type = "text/css";
        styleSheet.innerHTML = data.CSS;
        contentContainer.appendChild(styleSheet);

        //JS
        var scriptTag = document.createElement("script");
        scriptTag.innerHTML = data.JS;
        contentContainer.appendChild(scriptTag);

        //OTHER
        contentContainer.innerHTML = contentContainer.innerHTML.replace('<progress class="progress is-small is-primary" max="100">15%</progress>', '');
        hightlightMenu(url + ".html");
        heightFillRestClass();

        await timer(100);
        if (initPage != null) { initPage(); }
        //MANAGE DATAREFRESH TASKS
        await stopTask("dataRefresher");
        addNewTask("dataRefresher", function() {
            if (curr_task == null) return;
            curr_task();
        }, 5000 * 2);
    } catch (err) {
        console.error("[LOADER] Page load failed: ");
        console.error(err);
        contentContainer.innerHTML = '<a class="has-text-danger">%T%PAGE_LOAD_FAILED%T%</a>';
    }

}

async function hightlightMenu(htmlurl) {
    document.querySelectorAll(".menu-list>li>a").forEach((elem) => {
        elem.classList.remove("is-active");
    });

    document.querySelectorAll(".menu-list>li>a[data-url='" + htmlurl + "']").forEach((elem) => {
        elem.classList.add("is-active");
    });
}

document.addEventListener("DOMContentLoaded", init);