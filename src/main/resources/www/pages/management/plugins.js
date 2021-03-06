function initPage() {}

async function getCurrentData() {
    var data = await getDataFromAPI({ TYPE: "PAGEDATA", PAGE: "PLUGINS" });

    if (INTEGRATIONS["SKRIPT"].ENABLED) {
        for (script of INTEGRATIONS["SKRIPT"].SCRIPTS) {
            data.push({ name: script, authors: ["SKRIPT"], description: "", version: "", enabled: false, showtoggle: false, showstate: false });
        }
    }

    for (value of data) {
        value.enabled_icon = value.enabled ? "done" : "highlight_off";
        value.enabled_text = value.enabled ? "%T%ENABLED%T%" : "%T%DISABLED%T%";
        value.enabled_toggle_text = !value.enabled ? "%T%ENABLE%T%" : "%T%DISABLE%T%";
        value.enabled_toggle_onoff = !value.enabled ? "on" : "off";
        value.enabled_toggle_onoff = "toggle_" + value.enabled_toggle_onoff;

        if (value.showtoggle == null) value.showtoggle = true;
        if (value.showstate == null) value.showstate = true;

        if (!value.description) { value.description = ""; }
        if (!value.website) { value.website = ""; }

        if (value.authors != null) {
            value.authors = value.authors.join(",").substring(0, 40);
        }
    }


    return data;
}

async function togglePlugin(elem) {
    elem.setAttribute("disabled", true);
    elem.classList.add("is-loading");

    var pl = elem.getAttribute("data-plugin");
    var data = await getDataFromAPI({ TYPE: "EXECUTE", METHOD: "TOGGLE_PLUGIN", PLUGIN: pl });

    if (data == "SUCCESS") {
        elem.removeAttribute("disabled");
    } else {
        elem.classList.add("is-danger");
        elem.innerHTML = "Failed";
    }
    elem.classList.remove("is-loading");
}

var currentQuery = "";
var latestUpdateQuery = "NULL";
var pluginIDS = [94842];
var currentPluginInstallMenu = null;

async function togglePluginInstallDialogue() {
    if (currentPluginInstallMenu != null && !currentPluginInstallMenu.closed) {
        currentPluginInstallMenu.close();
        currentPluginInstallMenu = null;
    } else {
        latestUpdateQuery = "THISJUSTSITSHERELIKEADOG";
        currentPluginInstallMenu = new smartMenu("PLUGININSTALL", "Install Plugins", "Install Plugins");
        currentPluginInstallMenu.open();
        dialogueUpdater();
    }
}

async function dialogueUpdater() {
    while (currentPluginInstallMenu != null && !currentPluginInstallMenu.closed) {
        if (currentQuery != latestUpdateQuery) {
            if (currentQuery != "") {
                var data = await fetch("https://api.spiget.org/v2/search/resources/" + currentQuery + "");
                data = await data.json();

            } else {
                var data = await fetch("https://api.spiget.org/v2/resources?sort=-updateDate");
                data = await data.json();
            }

            var entrys_html = "";
            data.forEach((elem) => {
                var insertElem = {};
                insertElem["SUPPORTED_VERSIONS"] = elem.testedVersions.join(", ");
                insertElem["ID"] = elem.id;
                insertElem["NAME"] = elem.name;
                insertElem["LATEST_VERSION"] = elem.version.id;
                insertElem["TAG"] = elem.tag;
                insertElem["BTN_ATTRIBS"] = "";
                insertElem["BTN_TEXT"] = "+ %T%INSTALL%T%";
                insertElem["WARNINGS"] = "";

                if (elem.file.type == ".jar") {

                } else if (elem.file.type == ".sk" && INTEGRATIONS["SKRIPT"].ENABLED) {

                } else {
                    insertElem["BTN_TEXT"] = '<a class="has-text-danger">%T%ERROR_UNSUPPORTED_FILE_FORMAT%T%</a>';
                    insertElem["BTN_ATTRIBS"] = "disabled";
                }

                var INSTALLED = false;

                if (pluginIDS.includes("" + elem.id)) INSTALLED = true;
                if (INTEGRATIONS["SKRIPT"].ENABLED && INTEGRATIONS["SKRIPT"].SCRIPTS.filter(r => r.includes("" + elem.id)).length > 0) INSTALLED = true;

                if (INSTALLED) {
                    insertElem["BTN_ATTRIBS"] = "disabled";
                    insertElem["BTN_TEXT"] = '<span class="material-icons-outlined pr-1">done</span>%T%INSTALLED%T%';
                }

                var html = replaceObjectKeyInString(insertElem, PLUGINS_INSTALL_DIALOGUE_ENTRY);
                entrys_html += html;
            });

            var html = PLUGINS_INSTALL_DIALOGUE.replace("%ENTRYS%", entrys_html).replace("%CURRENT_SEARCH%", currentQuery);
            currentPluginInstallMenu.setHTML(html);
            latestUpdateQuery = currentQuery;
        }
        await timer(1000);
    }
}

async function installPluginButtonClick(elem) {
    if (elem.hasAttribute("disabled")) return;

    var id = elem.getAttribute("data-id");

    var data = await getDataFromAPI({ TYPE: "EXECUTE", METHOD: "INSTALL_PLUGIN", ID: id });
    console.log(data);

    elem.classList.add("is-loading");
    elem.setAttribute("disabled", true);

    await timer(1000);

    elem.classList.remove("is-loading");
    elem.innerHTML = '<span class="material-icons-outlined pr-1">done</span>%T%INSTALLED%T%';
}

var PLUGINS_INSTALL_DIALOGUE = '<div class="w-100">\
<div class="field has-addons w-100 pluginInstallControlsField" style="justify-content: center;">\
    <div class="control">\
        <input class="input is-primary" type="text" placeholder="Name" value="%CURRENT_SEARCH%">\
    </div>\
    <div class="control">\
        <a class="button is-success" onclick="currentQuery = this.parentElement.parentElement.querySelector(\'input\').value;">?</a>\
    </div>\
</div>\
</div>\
\
<div class="pluginInstallList heightFill">\
%ENTRYS%\
</div>'

var PLUGINS_INSTALL_DIALOGUE_ENTRY = '\
<div class="card" style="">\
    <div class="card-content">\
        <div class="media">\
            <div class="media-left">\
                <figure class="image is-48x48">\
                    <img src="https://api.spiget.org/v2/resources/%ID%/icon/data" alt="Placeholder image">\
                </figure>\
            </div>\
            <div class="media-content">\
                <p class="title is-4">%NAME%</p>\
                <p class="subtitle is-7">%LATEST_VERSION%</p>\
            </div>\
        </div>\
\
        <div class="content">\
            %TAG%\
            <br>%WARNINGS%\
        </div>\
    </div>\
    <footer class="card-footer p-2">\
        <div class="level" style="width: 100%;">\
            <div class="level-left">\
                <div class="has-text-danger">%T%SUPPORTS%T%:</div>\
                <div class="has-text-secondary pl-1 pr-1">%SUPPORTED_VERSIONS%</div>\
            </div>\
\
            <div class="level-right">\
                <div class="button is-success align-left" data-id="%ID%" %BTN_ATTRIBS% onclick="installPluginButtonClick(this);">%BTN_TEXT%</div>\
            </div>\
    </div>\
</footer>\
</div>';

var plugins = [11];