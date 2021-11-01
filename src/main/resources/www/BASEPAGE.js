var currentMenu, contentContainer;

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

var INTEGRATIONS;

async function initBASEPAGE() {
    document.querySelectorAll(".menu-list>li>a").forEach((elem) => {
        elem.addEventListener("click", function(event) {
            openPageInMenu(event.target.getAttribute("data-url").replace(".html", ""));
        });
    });

    openPageInMenu("overview/overview");

    addNewTask("INTEGRATIONSUPDATER", async function() {
        INTEGRATIONS = await getDataFromAPI({ TYPE: "DATA", METHOD: "GET_INTEGRATIONS" });
    }, 1000);
}

async function openPageInMenu(path) {
    path = "pages/" + path;
    console.log("[LOADER] Loading Page '" + path + "'...");

    if (smartMenuHelpers.getConstructedByID(path) != null && !smartMenuHelpers.getConstructedByID(path).closed) {
        console.warn("[LOADER] Page already loaded");
        return;
    }
    await smartMenuHelpers.closeAll();

    var pageName = PAGE_NAMES[path.split("/").latest().toLowerCase()];

    var menu = new smartMenu(path, pageName, pageName);
    menu.open();
    contentContainer = menu.getContentDOM();

    try {
        var HTML = await socketIoRequestAwait({ TYPE: "WEBFILE", PATH: path + ".html" });
        var CSS = await socketIoRequestAwait({ TYPE: "WEBFILE", PATH: path + ".css" });
        var JS = await socketIoRequestAwait({ TYPE: "WEBFILE", PATH: path + ".js" });

        contentContainer.innerHTML = HTML;

        var styleSheet = document.createElement("style");
        styleSheet.type = "text/css";
        styleSheet.innerHTML = CSS;
        contentContainer.appendChild(styleSheet);

        var scriptTag = document.createElement("script");
        scriptTag.innerHTML = JS;
        contentContainer.appendChild(scriptTag);

        hightlightMenu(path + ".html");

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

function hightlightMenu(htmlurl) {
    document.querySelectorAll(".menu-list>li>a").forEach((elem) => {
        elem.classList.remove("is-active");
    });

    document.querySelectorAll(".menu-list>li>a[data-url='" + htmlurl + "']").forEach((elem) => {
        elem.classList.add("is-active");
    });
}

initBASEPAGE();