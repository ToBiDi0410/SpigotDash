var HTMLParser = new DOMParser();

function isValid(key, value) {
    return !(key == "" || key == undefined || key == null || value == "" || value == undefined || value == null);
}

function getDOMObject(html) {
    return HTMLParser.parseFromString(html, 'text/html').body.firstChild;
}

// NOTIFICATION
var TEMPLATE_NOTIFICATION = '\
<div class="notification is-%LEVEL%" data-uuid=%UUID%>\
    <div class="NOTIFICATION_TITLE"><b>%TITLE%</b></div> \
    <div class="NOTIFICATION_CONTENT">%MESSAGE%</div> \
    <div class="NOTIFICATION_INITIATOR">\
        <div><b>%INITIATOR%</b> at %LOCALECREATED%</div>\
    </div>\
</div>\
';

function generateNotificationHTML(id, data) {
    if (!isValid(id, data)) return "";

    data.localecreated = new Date(data.created).toLocaleTimeString();
    data.uuid = id;
    data.level = data.level.toLowerCase();

    var html = replaceObjectKeyInString(data, TEMPLATE_NOTIFICATION);

    return html;
}

// LOG
var TEMPLATE_LOGLIST_ENTRY = '<li class="console_messagelist_entry">%MESSAGE%</li>';

function generateLogListEntry(message) {
    var html = TEMPLATE_LOGLIST_ENTRY;
    html = html.replace("%MESSAGE%", minecraftStringToHTMLString(message));
    return getDOMObject(html);
}

//PLUGINS
var TEMPLATE_PLUGIN_ENTRY = '\
<div class="">\
    <div class="plugin-title">%NAME%</div>\
    <div class="plugin-authors">%T%FROM%T% %AUTHORS%</div>\
    <div class="plugin-state %ENABLED%"><span class="material-icons-outlined">%ENABLED_ICON%</span> (%ENABLED_TEXT%)</div>\
    <div class="plugin-version">%VERSION%</div>\
    <div class="plugin-description">%DESCRIPTION%</div>\
    <a class="plugin-website" href="%WEBSITE%">%WEBSITE%</a>\
    <div class="plugin-toggle button is-warning" data-plugin="%NAME%" onclick="togglePlugin(this);"><span class="material-icons-outlined">toggle_%ENABLED_TOGGLE_ONOFF%</span> %ENABLED_TOGGLE_TEXT%</div>\
</div>\
';

function generatePluginEntry(value) {
    var html = TEMPLATE_PLUGIN_ENTRY;

    value.enabled_icon = value.enabled ? "done" : "highlight_off";
    value.enabled_text = value.enabled ? "%T%ENABLED%T%" : "%T%DISABLED%T%";
    value.enabled_toggle_text = !value.enabled ? "%T%ENABLE%T%" : "%T%DISABLE%T%";
    value.enabled_toggle_onoff = !value.enabled ? "on" : "off";
    if (value.authors != null) {
        value.authors = value.authors.join(",").substring(0, 40);
    }

    html = replaceObjectKeyInString(value, html);
    return getDOMObject(html);
}

//PLAYERS

var TEMPLATE_PLAYER_ENTRY = '\
<div class="player" data-uuid="%UUID%">\
    <img class="head" src="https://crafatar.com/avatars/%UUID%">\
    <div class="details">\
        <div>%DISPLAYNAME%</div>\
        <div>%UUID%</div>\
    </div>\
</div>\
';

function generatePlayerEntry(value) {
    var html = TEMPLATE_PLAYER_ENTRY;

    value.x = value.Location.X;
    value.y = value.Location.Y;
    value.z = value.Location.Z;
    value.world = value.Location.WORLD;
    value.Displayname = minecraftStringToHTMLString(value.Displayname);

    html = replaceObjectKeyInString(value, html);
    return getDOMObject(html);
}

//WORLDS

var TEMPLATE_WORLD_ENTRY = '\
<div class="worldlist_entry box" data-id="%NAME%" onclick="openWorldMenu(\'%NAME%\');">\
        <div class="tile is-ancestor">\
            <div class="tile is-1">\
                <img src="./global/icons/%ICON%.png">\
            </div>\
            <div class="tile is-1"></div>\
            <div class="tile is-3">\
                <div class="title">%NAME%</div>\
            </div>\
            <div class="tile is-2">\
                <div class="subtitle chunkcount">%CHUNKCOUNT% %T%CHUNKS%T%</div>\
            </div>\
            <div class="tile is-2">\
                <div class="subtitle playercount">%PLAYERCOUNT% %T%PLAYERS%T%</div>\
            </div>\
            <div class="tile is-2">\
                <div class="subtitle entitiecount">%ENTITIECOUNT% %T%ENTITIES%T%</div>\
            </div>\
            <div class="tile is-1">\
                <span class="material-icons-outlined" style="text-align: right; width: 90%;">\
                    arrow_right_alt\
                </span>\
            </div>\
        </div>\
    </div>\
</div>\
';

function generateWorldEntry(value) {
    var html = TEMPLATE_WORLD_ENTRY;

    value.icon = "ICON_WORLD_" + value.type.toUpperCase();

    html = replaceObjectKeyInString(value, html);
    return getDOMObject(html);
}

var TEMPLATE_WORLD_ENTITIE_ENTRY = '\
<div class="tile is-ancestor" data-id="%ID%">\
    <div class="tile is-parent">\
        <article class="tile is-child">\
            <div class="content">\
                <div class="level">\
                    <div class="level-left">\
                        <div class="level-item" style="width: 48px">\
                            <img style="max-width: 48px; max-height: 48px;" src="./global/icons/MOBS/%ICON%.png">\
                        </div>\
                        <div class="level-item">\
                            <div class="title" style="font-size: 120%;">%NAME%</div>\
                        </div>\
                    </div>\
\
                    <div class="level-right">\
                        <div class="level-item">\
                            <div class="subtitle">%COUNT%</div>\
                        </div>\
                        <div class="level-item"><div class="button is-danger killEntities" data-type="%NAME%">%T%KILL%T%</div></div>\
                        <div class="level-item"></div>\
                    </div>\
                </div>\
            </div>\
        </article>\
    </div>\
</div>'

function generateWorldEntitieEntry(name, count) {
    var html = TEMPLATE_WORLD_ENTITIE_ENTRY;
    var value = { name: name, count: count, id: name };
    value.icon = name.toLowerCase().capitalizeFirstLetter();


    html = replaceObjectKeyInString(value, html);
    return getDOMObject(html);
}

var TEMPLATE_WORLD_CHUNK_ENTRY = '\
<div class="tile is-ancestor" data-id="%ID%">\
    <div class="tile is-parent">\
        <article class="tile is-child">\
            <div class="content">\
                <div class="level">\
                    <div class="level-left">\
                        <div class="level-item">\
                            <div class="title" style="font-size: 120%;">%ID%</div>\
                        </div>\
                    </div>\
\
                    <div class="level-right">\
                        <div class="level-item">\
                            <div class="subtitle">%T%PLAYERS%T%: <div class="players">%PLAYERCOUNT%</div></div>\
                        </div>\
                        <div class="level-item">\
                            <div class="subtitle">%T%ENTITIES%T%: <div class="entities">%ENTITIECOUNT%</div></div>\
                        </div>\
                        <div class="level-item"></div>\
                        <div class="level-item"></div>\
                    </div>\
                </div>\
            </div>\
        </article>\
    </div>\
</div>'

function generateWorldChunkEntry(chunk) {
    var html = TEMPLATE_WORLD_CHUNK_ENTRY;
    var value = { id: chunk.ID, playercount: chunk.Players.length, entitiecount: Object.size(chunk.Entities) };

    html = replaceObjectKeyInString(value, html);
    return getDOMObject(html);
}