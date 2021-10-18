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
    return data;
}

async function openWorldMenu(worldname) {
    var menu = new smartMenu("WOLRD_INFO", worldname, worldname);
    menu.open();

    var TEMPLATE_DOM = (new DOMParser()).parseFromString(TEMPLATE_WORLD_MENU.innerHTML, "text/html");
    TEMPLATE_DOM = TEMPLATE_DOM.body.firstChild;

    TEMPLATE_DOM.setAttribute("data-callback", "return getWorldData('" + worldname + "');");
    TEMPLATE_DOM.classList.remove("IGNORE");
    menu.setHTML(TEMPLATE_DOM.outerHTML);
}

function worldUpdateWeather(weatherDOM, weather) {
    weatherDOM.querySelectorAll(".button").forEach((elem) => {
        if (elem.getAttribute("data-weather") != weather) {
            elem.classList.add("disabled");
        } else {
            elem.classList.remove("disabled");
        }
    })
}

var TEMPLATE_WORLD_MENU = document.querySelector(".worldMenuTemplate");