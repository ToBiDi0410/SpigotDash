async function initPage() {
    curr_task = refreshWorlds;
}

async function refreshWorlds() {
    var worldListDom = document.querySelector(".worldlist");

    var data = await getDataFromAPI({ method: "GET_WORLDS" });

    worldListDom.querySelectorAll(".worldlist_entry").forEach((elem) => {
        var obj = data.getObjectWithKeyValue("name", elem.getAttribute("data-id"));
        if (obj != null) {
            var entcount = elem.querySelector(".entitiecount");
            entcount.innerHTML = entcount.innerHTML.replace(entcount.innerHTML.split(" ")[0], obj.entitieCount);

            var chunkcount = elem.querySelector(".chunkcount");
            chunkcount.innerHTML = chunkcount.innerHTML.replace(chunkcount.innerHTML.split(" ")[0], obj.chunkCount);

            var playercount = elem.querySelector(".playercount");
            playercount.innerHTML = playercount.innerHTML.replace(playercount.innerHTML.split(" ")[0], obj.playerCount);
        } else {
            elem.remove();
        }
    });

    for (const elem of data) {
        if (worldListDom.querySelector(".worldlist_entry[data-id='" + elem.name + "']") == null) {
            worldListDom.appendChild(generateWorldEntry(elem));
        }
    }
}

async function openWorldMenu(worldname) {
    var menu = new smartMenu("WOLRD_INFO", worldname, worldname);
    menu.open();
    menu.setHTML(TEMPLATE_WORLD_MENU);

    var data = await getDataFromAPI({ method: "GET_WORLD", world: worldname });

    var entitieCount = menu.getContentDOM().querySelector(".card-header-title.entities");
    var entitieList = menu.getContentDOM().querySelector(".entitieDropDownCont");
    var playerCount = menu.getContentDOM().querySelector(".card-header-title.players");
    var playerList = menu.getContentDOM().querySelector(".playersDropDownCont");
    var chunksCount = menu.getContentDOM().querySelector(".card-header-title.chunks");
    var chunksList = menu.getContentDOM().querySelector(".chunksDropDownCont");

    var weatherDom = menu.getContentDOM().querySelector(".weatherSelector");
    weatherDom.querySelectorAll(".weatherTrigger").forEach((elem) => {
        elem.addEventListener("click", async function() {
            this.classList.add("is-loading");

            var res = await getDataFromAPI({ method: "CONTROL_WORLD", action: "WEATHER", world: data.name, weather: this.getAttribute("data-weather") });
            if (res == "SUCCESS") {
                this.classList.remove("is-loading");
                worldUpdateWeather(this.parentElement.parentElement.parentElement.parentElement, this.getAttribute(("data-weather")));
            }
        })
    });

    var timeDom = menu.getContentDOM().querySelector(".timeslider");
    var daysDom = menu.getContentDOM().querySelector(".days");
    timeDom.addEventListener("input", async function() {
        var res = await getDataFromAPI({ method: "CONTROL_WORLD", action: "TIME", world: data.name, time: this.value });

        if (res == "SUCCESS") {
            this.removeAttribute("disabled");
        }
    });

    var datapackDom = menu.getContentDOM().querySelector(".datapacks");

    while (!menu.closed) {
        data = await getDataFromAPI({ method: "GET_WORLD", world: worldname });

        worldMenuUpdateEntities(entitieList, data.Entities);
        worldMenuUpdatePlayers(playerList, data.Players);
        worldUpdateWeather(weatherDom, data.weather);
        worldUpdateChunks(chunksList, data.Chunks);
        timeDom.value = data.daytime;

        var entitieCountNum = 0;
        for (key in data.Entities) { entitieCountNum += data.Entities[key]; };

        entitieCount.innerHTML = entitieCount.innerHTML.replace(entitieCount.innerHTML.split("(")[1].split(")")[0], entitieCountNum);
        playerCount.innerHTML = playerCount.innerHTML.replace(playerCount.innerHTML.split("(")[1].split(")")[0], data.Players.length);
        chunksCount.innerHTML = chunksCount.innerHTML.replace(chunksCount.innerHTML.split("(")[1].split(")")[0], data.Chunks.length);
        daysDom.innerHTML = daysDom.innerHTML.replace(daysDom.innerHTML.split(": ")[1], data.days);
        datapackDom.innerHTML = datapackDom.innerHTML.replace(datapackDom.innerHTML.split(": ")[1], data.Datapacks.length > 0 ? data.Datapacks.join(",") : "No Datapacks loaded");

        registerEntityKillClicks(menu, data);

        await timer(5000);
    }
}

function registerEntityKillClicks(menu, data) {
    menu.getContentDOM().querySelectorAll(".killEntities").forEach((elem) => {
        elem.addEventListener("click", async function() {
            this.setAttribute("disabled", true);
            this.classList.add("is-loading");
            var res = await getDataFromAPI({ method: "CONTROL_WORLD", action: "KILL_ENTITY_TYPE", world: data.name, "type": this.getAttribute("data-type") });

            if (res == "KILLED") {
                this.removeAttribute("disabled");
                this.classList.remove("is-loading");
                this.parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.remove();
            }
        });
    });
}

function worldUpdateChunks(listDOM, chunks) {
    listDOM.querySelectorAll("[data-id]").forEach((elem) => {
        var id = elem.getAttribute("data-id");
        var selelem = chunks.getObjectWithKeyValue("ID", id);
        if (selelem != null) {
            elem.querySelector(".players").innerHTML = selelem.Players.length;
            elem.querySelector(".entities").innerHTML = Object.size(selelem.Entities);
        } else {
            elem.remove();
        }
    });

    for (const elem of chunks) {
        if (listDOM.querySelector("[data-id='" + elem.ID + "']") == null) {
            listDOM.appendChild(generateWorldChunkEntry(elem));
        }
    }
}

function worldMenuUpdateEntities(listDOM, entities) {
    listDOM.querySelectorAll("[data-id]").forEach((elem) => {
        var id = elem.getAttribute("data-id");
        if (entities[id] != null) {
            elem.querySelector(".subtitle").innerHTML = entities[id];
        } else {
            elem.remove();
        }
    });

    for (const [key, value] of Object.entries(entities)) {
        if (listDOM.querySelector("[data-id='" + key + "']") == null) {
            listDOM.appendChild(generateWorldEntitieEntry(key, value));
        }
    }
}

function worldMenuUpdatePlayers(listDOM, players) {
    if (players.length <= 0) {
        listDOM.innerHTML = '<a class="has-text-danger">%T%NO_PLAYERS_IN_WORLD%T%</a>';
        return;
    } else {
        if (listDOM.innerHTML.includes("%T%NO_PLAYERS_IN_WORLD%T%")) {
            listDOM.innerHTML = "";
        }
    }

    listDOM.querySelectorAll("[data-uuid]").forEach((elem) => {
        var id = elem.getAttribute("data-uuid");
        var obj = players.getObjectWithKeyValue("UUID", id);
        if (obj == null) {
            elem.remove();
        }
    });

    for (const elem of players) {
        if (listDOM.querySelector("[data-uuid='" + elem.UUID + "']") == null) {
            listDOM.appendChild(generatePlayerEntry(elem));
        }
    }


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

var TEMPLATE_WORLD_MENU = '\
<div class="weatherSelector p-2">\
    <div class="level">\
        <div class="level-left">\
            <div class="level-item">\
                <div class="subitle">%T%WEATHER%T%:</div>\
            </div>\
            <div class="level-item">\
                <button class="button transparentButton weatherTrigger" data-weather="Normal"><div class="material-icons-outlined" style="color: yellow;">wb_sunny</div></button>\
            </div>\
            <div class="level-item">\
                <button class="button transparentButton weatherTrigger" data-weather="Rain"><div class="material-icons-outlined" style="color: blue;">water_drop</div></button>\
            </div>\
            <div class="level-item">\
                <button class="button transparentButton weatherTrigger" data-weather="Thunder"><div class="material-icons-outlined" style="color: orange;">bolt</div></button>\
            </div>\
        </div>\
    </div>\
</div>\
\
<div class="p-2">\
    <div class="days">%T%DAY_IN_WORLD%T%: %DAY%</div>\
</div>\
\
<div class="timeSelector p-2">\
    <div class="level">\
        <div class="level-left">\
            <div class="level-item">\
                %T%DAY%T%\
            </div>\
        </div>\
\
        <div class="level-middle">\
            <div class="level-item">\
                %T%TIME%T%\
            </div>\
        </div>\
\
        <div class="level-right">\
            <div class="level-item">\
                %T%NIGHT%T%\
            </div>\
        </div>\
    </div>\
    <input class="timeslider slider is-fullwidth" step="1000" min="0" max="24000" value="0" type="range">\
</div>\
\
<div class="p-2">\
    <div class="datapacks">%T%DATAPACKS%T%: %DATAPACKS%</div>\
</div>\
<div class="card is-fullwidth">\
    <header class="card-header">\
        <p class="card-header-title entities">%T%ENTITIES%T% (0)</p>\
        <a class="card-header-icon card-toggle" onclick="toggleExpandCart(this.parentElement.parentElement);">\
            <span class="material-icons-outlined">expand</span>\
        </a>\
    </header>\
    <div class="card-content is-hiddenc">\
        <div class="content entitieDropDownCont">\
            \
        </div>\
    </div>\
</div>\
\
<div class="card is-fullwidth">\
    <header class="card-header">\
        <p class="card-header-title players">%T%PLAYERS%T% (0)</p>\
        <a class="card-header-icon card-toggle" onclick="toggleExpandCart(this.parentElement.parentElement);">\
            <span class="material-icons-outlined">expand</span>\
        </a>\
    </header>\
    <div class="card-content is-hiddenc">\
        <div class="content playersDropDownCont">\
            \
        </div>\
    </div>\
</div>\
\
<div class="card is-fullwidth">\
    <header class="card-header">\
        <p class="card-header-title chunks">%T%CHUNKS%T% (0)</p>\
        <a class="card-header-icon card-toggle" onclick="toggleExpandCart(this.parentElement.parentElement);">\
            <span class="material-icons-outlined">expand</span>\
        </a>\
    </header>\
    <div class="card-content is-hiddenc">\
        <div class="content chunksDropDownCont">\
            \
        </div>\
    </div>\
</div>';