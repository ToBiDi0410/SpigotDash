var socket;
var STYLESHEETS = ["global.css", "minecraftColors.css", "smartMenu.css", "other-license/bulma.min.css", "other-license/bulma-extensions.min.css", "other-license/hightlightjs.railcasts.min.css", "other-license/materialIcons.css", "other-license/vanillatoasts.css"];
var SCRIPTS = ["global.js", "taskManager.js", "componentGenerator.js", "smartMenu.js", "minecraftColors.js", "other-license/apexcharts.js", "other-license/bulma-extensions.min.js", "other-license/highlight.min.js", "other-license/jsencrypt.min.js", "other-license/sweetalert2.min.js", "other-license/vanillatoasts.js", "socketRessourceManager.js", "notificationManager.js"]
var MAX_SOCKET_TRIES = 15;
var theme;

async function init() {
    var loaderMessageDOM = document.querySelector('#pageInitLoader_MESSAGE');
    var loaderProgressDOM = document.querySelector('#pageInitLoader_PROGRESS');

    loaderMessageDOM.innerHTML = "Establishing Socket connection...";
    socket = io("ws://localhost:9678");

    var SOCKET_TRIES = 0;
    while (!socket.connected) {
        loaderMessageDOM.innerHTML = "Establishing Socket connection...";
        SOCKET_TRIES++;
        await timer(1000);

        if (SOCKET_TRIES >= MAX_SOCKET_TRIES) {
            loaderMessageDOM.innerHTML = "Failed to establish connection!";
            loaderMessageDOM.classList.add("has-text-danger");
            loaderProgressDOM.value = 100;
            loaderProgressDOM.classList.add("is-danger");
            return;
        }
    }

    loaderMessageDOM.innerHTML = "Waiting for Authentication...";
    await requireAuth();
    loaderMessageDOM = document.querySelector('#pageInitLoader_MESSAGE');
    loaderProgressDOM = document.querySelector('#pageInitLoader_PROGRESS');

    loaderMessageDOM.innerHTML = "Loading Styles...";
    theme = await socketIoRequestAwait({ TYPE: "DATA", METHOD: "THEME" });

    if (theme == "dark") {
        STYLESHEETS.push("other-license/bulmaswatch.min.css");
        STYLESHEETS.push("other-license/sweet_dark.css");
        STYLESHEETS.push("dark.css");
    }

    for (elem of STYLESHEETS) {
        loaderMessageDOM.innerHTML = "Loading Style (" + elem + ")...";
        var css = await socketIoRequestAwait({ TYPE: "WEBFILE", PATH: "global/" + elem }),
            head = document.head || document.getElementsByTagName('head')[0],
            style = document.createElement('style');
        head.appendChild(style);
        style.type = 'text/css';
        if (style.styleSheet) { style.styleSheet.cssText = css; } else { style.appendChild(document.createTextNode(css)); }

        loaderProgressDOM.value += 33.3 / STYLESHEETS.length;
        await timer(25);
    }

    loaderMessageDOM.innerHTML = "Loading Scripts...";
    for (elem of SCRIPTS) {
        loaderMessageDOM.innerHTML = "Loading Script (" + elem + ")...";
        var js = await socketIoRequestAwait({ TYPE: "WEBFILE", PATH: "global/" + elem }),
            head = document.head || document.getElementsByTagName('head')[0];

        appendScript(js, head);

        loaderProgressDOM.value += 33.3 / SCRIPTS.length;
        await timer(25);
    }

    addNewTask("HEIGHTFILL", heightFillRestClass, 500);
    addNewTask("NOTIFICATIONS", refreshNotifications, 2000);

    loaderMessageDOM.innerHTML = "Loading Page (BASEPAGE)...";
    var BASEPAGE_HTML = await socketIoRequestAwait({ TYPE: "WEBFILE", PATH: "BASEPAGE.html" });
    loaderProgressDOM.value += 33.3 / 3;
    var BASEPAGE_JS = await socketIoRequestAwait({ TYPE: "WEBFILE", PATH: "BASEPAGE.js" });
    loaderProgressDOM.value += 33.3 / 3;
    document.querySelector(".BASEPAGE").innerHTML = BASEPAGE_HTML;
    appendScript(BASEPAGE_JS, document.querySelector(".BASEPAGE"));
    loaderProgressDOM.value += 33.3 / 3;

    await timer(100);
    document.querySelector("#pageInitLoader").remove();
}

async function requireAuth() {
    var MSGDOM = document.querySelector("#pageInitLoader_MESSAGEDOM");
    var OLD_CONT = MSGDOM.innerHTML;

    MSGDOM.innerHTML = LOGIN_HTML;

    while ((await socketIoRequestAwait({ METHOD: "STATE" }, "AUTH") == false)) {
        await timer(1000);
    }

    MSGDOM.innerHTML = OLD_CONT;

    await timer(1000);
}

async function tryAuth() {
    var USERNAME = "ADMIN";
    var PASSWORD = document.querySelector("input[type='password']").value;

    var res = await socketIoRequestAwaitFull({ METHOD: "AUTHENTICATE", USERNAME: USERNAME, PASSWORD: PASSWORD }, "AUTH");

    if (res.CODE != 200) {
        document.getElementById("ERR_FIELD").innerHTML = res.DATA.replace("ERR_WRONG_NAME_OR_PASSWORD", "%T%WRONG_NAME_OR_PASSWORD%T%");
    } else {
        document.getElementById("ERR_FIELD").innerHTML = "%T%LOGIN_SUCCESS%T%";
        document.getElementById("ERR_FIELD").classList.remove("is-danger");
        document.getElementById("ERR_FIELD").classList.add("is-success");
    }
}

async function socketIoRequestAwait(data, INTERNAL_METHOD = "REQUEST") {
    return (await socketIoRequestAwaitFull(data, INTERNAL_METHOD)).DATA;
}

function socketIoRequestAwaitFull(data, INTERNAL_METHOD = "REQUEST") {
    data = JSON.stringify(data);
    //console.log(INTERNAL_METHOD + " --> " + data);
    return new Promise((resolve, reject) => {
        socket.emit(INTERNAL_METHOD, data, function(respdata) {
            var parsedResp = JSON.parse(respdata);
            resolve(parsedResp);
        });
    });
}

function appendScript(js, dom) {
    var script = document.createElement('script');
    script.innerHTML = js;
    script.type = "text/javascript";
    return dom.appendChild(script);
}

const timer = function(time) {
    return new Promise((resolve, reject) => {
        setTimeout(resolve, time);
    });
}

init();

var LOGIN_HTML = '<div class="message-header">\
<p>Login</p>\
</div>\
<div class="message-body has-text-centered">\
<div class="subtitle has-text-danger"><b>%T%LOGIN_REQUIRED%T%</b></div>\
<div class="content">\
<div class="field">\
    <label class="label">%T%PASSWORD%T%</label>\
    <div class="control has-icons-left has-icons-right">\
        <input class="input is-success" type="password" placeholder="SuperSecure178" id="PWORD">\
        <span class="icon is-small is-left">\
        <i class="fas fa-key"></i>\
      </span>\
    </div>\
    <p class="help is-danger" id="ERR_FIELD"></p>\
\
</div>\
\
<div class="field">\
    <div class="control">\
        <button class="button is-link" onclick="tryAuth();">%T%LOGIN%T%</button>\
    </div>\
</div>\
</div>\
</div>\
\
';