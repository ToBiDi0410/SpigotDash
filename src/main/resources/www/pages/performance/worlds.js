async function initPage() {}


async function getCurrentData() {
    var data = await getDataFromAPI({ TYPE: "PAGEDATA", PAGE: "WORLDS" });
    for (elem of data) {
        elem.icon = "global/icons/ICON_WORLD_" + elem.type + ".png";
    }
    return data;
}

async function getWorldData(name) {
    var data = await getDataFromAPI({ TYPE: "DATA", METHOD: "GET_WORLD", WORLD: name });
    data.Entities = objectToArray(data.Entities);
    data.EntitieCount = 0;
    for (elem of data.Entities) {
        data.EntitieCount += elem.VALUE;
    }
    return data;
}

async function openWorldMenu(worldname) {
    var menu = new smartMenu("WOLRD_INFO", worldname, worldname);
    menu.open();

    var TEMPLATE_DOM = (new DOMParser()).parseFromString(TEMPLATE_WORLD_MENU.innerHTML, "text/html");
    TEMPLATE_DOM.querySelectorAll(".IGNORE_IN_TEMPLATE").forEach((elem) => { elem.classList.remove("IGNORE_IN_TEMPLATE"); });
    TEMPLATE_DOM = TEMPLATE_DOM.body.firstChild;

    TEMPLATE_DOM.setAttribute("data-callback", "return getWorldData('" + worldname + "');");
    await timer(100);
    menu.setHTML(TEMPLATE_DOM.outerHTML);
}

async function changeWeather(toggleEntry) {
    var BUTTON = toggleEntry.querySelector("button");
    BUTTON.classList.add("is-loading");

    var res = await getDataFromAPI({ TYPE: "EXECUTE", METHOD: "CONTROL_WORLD", ACTION: "WEATHER", WORLD: toggleEntry.parentElement.parentElement.getAttribute("data-worldname"), WEATHER: toggleEntry.getAttribute("data-toggleCheck") });
    if (res == "SUCCESS") {
        BUTTON.classList.remove("is-loading");
        toggleEntry.parentElement.parentElement.querySelectorAll(".dataFieldToggleEntry").forEach((elem) => {
            elem.classList.add("disabled");
            elem.classList.remove("active");
        });

        toggleEntry.classList.remove("disabled");
        toggleEntry.classList.add("active");
    }
}

async function changeTime(timeSlider) {
    var res = await getDataFromAPI({ TYPE: "EXECUTE", METHOD: "CONTROL_WORLD", ACTION: "TIME", WORLD: timeSlider.parentElement.getAttribute("data-worldname"), TIME: timeSlider.value });
}

async function killEntities(killButton) {
    killButton.classList.add("is-loading");
    killButton.setAttribute("disabled", true);
    var WORLD = killButton.parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.getAttribute("data-worldname");

    var res = await getDataFromAPI({ TYPE: "EXECUTE", METHOD: "CONTROL_WORLD", ACTION: "KILL_ENTITY_TYPE", WORLD: WORLD, ENTTYPE: killButton.getAttribute("data-type") });
    console.log(res);

    killButton.classList.remove("is-loading");

    if (res == "KILLED") {
        killButton.removeAttribute("disabled");
        killButton.parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.remove();
    }
}

var TEMPLATE_WORLD_MENU = document.querySelector(".worldMenuTemplate");