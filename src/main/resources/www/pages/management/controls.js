async function initPage() {
    curr_task = updateTask;
}

async function updateTask() {
    var data = await getDataFromAPI({ method: "GET_CONTROLS" });

    if (JSONMatches(data, CURR_DATA)) {
        return;
    }

    CURR_DATA = getIndependentObject(data);

    data["WHITELIST_TOGGLE_TEXT"] = data.whitelist ? "%T%DISABLE_WHITELIST%T%" : "%T%ENABLE_WHITELIST%T%";
    data["END_TOGGLE_TEXT"] = data.end ? "%T%DISABLE_END%T%" : "%T%ENABLE_END%T%";
    data["NETHER_TOGGLE_TEXT"] = data.nether ? "%T%DISABLE_NETHER%T%" : "%T%ENABLE_END%T%";

    insertObjectIntoHTML(data, contentContainer);
}

async function executeAPIRequestWithButton(btn, args, verifyCallback) {
    btn.classList.add("is-loading");
    btn.setAttribute("disabled", true);

    var data = await getDataFromAPI(args);
    btn.classList.remove("is-loading");

    if (data != null && verifyCallback(data)) {
        btn.removeAttribute("disabled");
        updateTask();
    } else {
        btn.classList.add("is-danger");
        btn.innerHTML = "%T%ERROR%T%";
    }
}

async function addToWhitelist(elem) {
    var main = elem.parentElement.parentElement;
    var input = main.querySelector("input");
    var icon = main.querySelector("img");
    var uuid = icon.src.split("/");

    uuid = uuid[uuid.length - 1];
    elem.classList.add("is-loading");

    var data = await getDataFromAPI({ method: "CONTROL", action: "WHITELIST_ADD", player: input.value });

    if (data == null || data != "SUCCESS") {
        input.value = "%T%ADD_FAILED%T%";
        input.oninput();
    }

    elem.classList.remove("is-loading");
    updateTask();
}

async function removeFromWhitelist(elem) {
    var main = elem.parentElement.parentElement;
    var uuid = main.getAttribute("data-uuid");

    elem.classList.add("is-loading");

    var data = await getDataFromAPI({ method: "CONTROL", action: "WHITELIST_REMOVE", player: uuid });

    if (data != "SUCCESS") {
        elem.innerHTML = "%T%ERROR%T%";
        elem.setAttribute("disabled", true);
    }

    elem.classList.remove("is-loading");
    updateTask();
}

var last_input = 0;

async function inputWhitelist(elem) {
    if (elem.value == "") return;
    var main = elem.parentElement.parentElement;
    var btn = main.querySelector(".button");
    var val = elem.value;
    var icon = main.querySelector("img");

    //elem.setAttribute("disabled", true);
    btn.classList.add("is-loading");

    var data = { status: "ERR" };

    if (!elem.value.includes(" ")) {
        data = await fetch("https://api.minetools.eu/uuid/" + val);
        data = await data.json();
    }


    if (data.status != "ERR") {
        btn.removeAttribute("disabled");
        btn.classList.remove("is-danger");
        btn.innerHTML = "+";

        icon.src = "https://crafatar.com/avatars/" + data.id;
        icon.style.display = "block";
    } else {
        btn.classList.add("is-danger");
        btn.setAttribute("disabled", true);
        btn.innerHTML = "X";
        icon.style.display = "none";
    }

    elem.removeAttribute("disabled");
    btn.classList.remove("is-loading");

    elem.value = val;
}

async function showWhiteListEditor() {
    var html = getWhiteListEditorHTML();
    if (!Swal.isVisible() || Swal.getTitle() != "%T%EDIT_WHITELIST%T%") {
        Swal.fire({
            title: "%T%EDIT_WHITELIST%T%",
            html: html
        });
        updateWhiteListEditorUntilClosed();
    }


}

function getWhiteListEditorHTML() {
    var html = CONTROLS_WHITELIST_DIALOGUE;
    var ENTRYS_HTML = "";
    CURR_DATA.whitelistEntrys.forEach((elem) => {
        ENTRYS_HTML += replaceObjectKeyInString(elem, CONTROLS_WHITELIST_DIALOGUE_ENTRY);
    });
    html = html.replace("%ENTRYS%", ENTRYS_HTML);

    return html;
}

function getWhiteListEditorEntrysHTML() {
    var ENTRYS_HTML = "";
    CURR_DATA.whitelistEntrys.forEach((elem) => {
        ENTRYS_HTML += replaceObjectKeyInString(elem, CONTROLS_WHITELIST_DIALOGUE_ENTRY);
    });
    return ENTRYS_HTML;
}

async function updateWhiteListEditorUntilClosed() {
    while (Swal.isVisible() || Swal.getTitle() == "%T%EDIT_WHITELIST%T%") {
        var html = getWhiteListEditorEntrysHTML();

        var playerList = Swal.getHtmlContainer().querySelector(".simplePlayerList");
        if (playerList.innerHTML != html) {
            playerList.innerHTML = html;
        }
        await timer(1000);
    }
}

//OPERATOR LIST

async function showOperatorEditor() {
    var html = getOperatorEditorHTML();
    if (!Swal.isVisible() || Swal.getTitle() != "%T%EDIT_OPERATORS%T%") {
        Swal.fire({
            title: "%T%EDIT_OPERATORS%T%",
            html: html
        });
        updateOperatorEditorUntilClosed();
    }


}

function getOperatorEditorHTML() {
    var html = CONTROLS_OPERATOR_DIALOGUE;
    var ENTRYS_HTML = "";
    CURR_DATA.operatorEntrys.forEach((elem) => {
        ENTRYS_HTML += replaceObjectKeyInString(elem, CONTROLS_OPERATOR_DIALOGUE_ENTRY);
    });
    html = html.replace("%ENTRYS%", ENTRYS_HTML);

    return html;
}

function getOperatorEditorEntrysHTML() {
    var ENTRYS_HTML = "";
    CURR_DATA.operatorEntrys.forEach((elem) => {
        ENTRYS_HTML += replaceObjectKeyInString(elem, CONTROLS_OPERATOR_DIALOGUE_ENTRY);
    });
    return ENTRYS_HTML;
}

async function updateOperatorEditorUntilClosed() {
    while (Swal.isVisible() || Swal.getTitle() == "%T%EDIT_OPERATORS%T%") {
        var html = getOperatorEditorEntrysHTML();

        var playerList = Swal.getHtmlContainer().querySelector(".simplePlayerList");
        if (playerList.innerHTML != html) {
            playerList.innerHTML = html;
        }
        await timer(1000);
    }
}

async function addToOperators(elem) {
    var main = elem.parentElement.parentElement;
    var input = main.querySelector("input");
    var icon = main.querySelector("img");
    var uuid = icon.src.split("/");

    uuid = uuid[uuid.length - 1];
    elem.classList.add("is-loading");

    var data = await getDataFromAPI({ method: "CONTROL", action: "OPERATOR_ADD", player: input.value });

    if (data == null || data != "SUCCESS") {
        input.value = "%T%ADD_FAILED%T%";
        input.oninput();
    }

    elem.classList.remove("is-loading");
    updateTask();
}

async function removeFromOperators(elem) {
    var main = elem.parentElement.parentElement;
    var uuid = main.getAttribute("data-uuid");

    elem.classList.add("is-loading");

    var data = await getDataFromAPI({ method: "CONTROL", action: "OPERATOR_REMOVE", player: uuid });

    if (data != "SUCCESS") {
        elem.innerHTML = "%T%ERROR%T%";
        elem.setAttribute("disabled", true);
    }

    elem.classList.remove("is-loading");
    updateTask();
}

var CURR_DATA = [11];

var CONTROLS_WHITELIST_DIALOGUE = '<div class="w-100">\
<div class="field has-addons w-100" style="justify-content: center;">\
    <div class="control">\
        <img style="height: 40px; width: 40px; margin-right: 1rem; display: none;" alt="Head">\
    </div>\
    <div class="control">\
        <input class="input is-primary" type="text" placeholder="Name" oninput="inputWhitelist(this);">\
    </div>\
    <div class="control">\
        <a class="button is-success" disabled="true" onclick="addToWhitelist(this)">+</a>\
    </div>\
</div>\
</div>\
\
<div class="simplePlayerList">\
%ENTRYS%\
</div>';

var CONTROLS_WHITELIST_DIALOGUE_ENTRY = '<div class="box playerListBox" data-uuid="%UUID%">\
<div class="playerListHead"><img src="https://crafatar.com/avatars/%UUID%"></div>\
<div class="playerListName">%NAME%</div>\
<div class="playerListActions">\
    <div class="button is-danger" onclick="removeFromWhitelist(this);">%T%REMOVE%T%</div>\
</div>\
</div>';

var CONTROLS_OPERATOR_DIALOGUE = '<div class="w-100">\
<div class="field has-addons w-100" style="justify-content: center;">\
    <div class="control">\
        <img style="height: 40px; width: 40px; margin-right: 1rem; display: none;" alt="Head">\
    </div>\
    <div class="control">\
        <input class="input is-primary" type="text" placeholder="Name" oninput="inputWhitelist(this);">\
    </div>\
    <div class="control">\
        <a class="button is-success" disabled="true" onclick="addToOperators(this)">+</a>\
    </div>\
</div>\
</div>\
\
<div class="simplePlayerList">\
%ENTRYS%\
</div>';

var CONTROLS_OPERATOR_DIALOGUE_ENTRY = '<div class="box playerListBox" data-uuid="%UUID%">\
<div class="playerListHead"><img src="https://crafatar.com/avatars/%UUID%"></div>\
<div class="playerListName">%NAME%</div>\
<div class="playerListActions">\
    <div class="button is-danger" onclick="removeFromOperators(this);">%T%REMOVE%T%</div>\
</div>\
</div>';