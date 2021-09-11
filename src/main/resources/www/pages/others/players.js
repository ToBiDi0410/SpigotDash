var players = [{ Location: { x: 0, y: 0, z: 0, YAW: 0, PITCH: 0, WORLD: "TEST" } }];

async function initPage() {
    curr_task = updatePlayerList;
}

async function updatePlayerList() {
    var temp_players = await getDataFromAPI({ method: "GET_PLAYERS" });
    temp_players.forEach(generatePlayerEntry);
    if (JSONMatches(temp_players, players)) return;
    players = temp_players;


    var cont = document.querySelector(".players");

    if (players.length <= 0) {
        cont.innerHTML = '<a class="has-text-danger">%T%NO_PLAYERS_ONLINE%T%</a>';
        return;
    }

    if (cont.innerHTML.includes('%T%NO_PLAYERS_ONLINE%T%')) {
        cont.innerHTML = "";
    }

    var DOMsToRemove = Array.from(cont.querySelectorAll(".player"));

    players.forEach((elem) => {
        var currDom = cont.querySelector(".player[data-uuid='" + elem.UUID + "']");

        if (currDom == null) {
            //IF PLAYER NOT IN LSIT
            var child = cont.appendChild(generatePlayerEntry(elem));
            child.addEventListener("click", function() {
                showInfos(this.getAttribute("data-uuid"));
            });
        } else {
            //IF PLAYER IN LIST, REMOVE OUT OF DELETION BECAUSE THE PLAYER IS ONLINE (PLAYERS CONTAINS HIM)
            DOMsToRemove = DOMsToRemove.filter(function(el) { return el != currDom });
        }
    });

    DOMsToRemove.forEach((elem) => { elem.remove() });
    return;
}

async function showInfos(uuid) {
    var player = players.find(function(elem) { return (elem.UUID == uuid); });
    var new_html = replaceObjectKeyInString(player, TEMPLATE_PLAYER_MENU);

    var menu = new smartMenu("PLAYERINFO", player.Name, player.Displayname);
    menu.open();
    menu.setHTML(new_html);
    menu.update();

    while (!menu.closed) {
        var thisPlayer = players.find(function(elem) { return (elem.UUID == player.UUID); });
        if (thisPlayer == null || thisPlayer == undefined) {
            menu.setHTML(menu.html + '<br><a class="has-text-danger"><b>%T%PLAYER_LEFT_SERVER%T%</b></a>');
            break;
        }

        if (!JSONMatches(thisPlayer, player)) {
            menu.setHTML(replaceObjectKeyInString(thisPlayer, TEMPLATE_PLAYER_MENU));
            player = thisPlayer;
        }

        await timer(1000);
    }
};

async function sendMessageClick(uuid) {
    var res = await Swal.fire({
        title: "%T%SEND_INGAME_MESSAGE%T%",
        text: "%T%COLOR_CODES_SUPPORTED%T%",
        input: "text",
        confirmButtonText: "%T%SEND_MESSAGE%T%",
        showCancelButton: true,
        backdrop: true,
        allowOutsideClick: () => !Swal.isLoading(),
        preConfirm: async function(message) {
            var reqres = await getDataFromAPI({ method: "PLAYER_ACTION", action: "MESSAGE", message: message, player: uuid })
            if (reqres == "SUCCESS") {
                return true;
            } else {
                Swal.showValidationMessage("Failed: " + reqres);
            }
        }
    });

    if (res.isConfirmed) {
        Swal.fire({
            title: "%T%SENT%T%",
            text: "%T%INGAME_MESSAGE_SENT%T%",
            icon: "success",
            timer: 2000,
            timerProgressBar: true
        });
    }
}

async function kickClick(uuid) {
    var res = await Swal.fire({
        title: "%T%KICK_PLAYER%T%",
        text: "%T%COLOR_CODES_SUPPORTED%T%",
        input: "text",
        confirmButtonText: "%T%KICK_PLAYER%T%",
        showCancelButton: true,
        backdrop: true,
        allowOutsideClick: () => !Swal.isLoading(),
        preConfirm: async function(message) {
            var reqres = await getDataFromAPI({ method: "PLAYER_ACTION", action: "KICK", message: message, player: uuid })
            if (reqres == "SUCCESS") {
                return true;
            } else {
                Swal.showValidationMessage("Failed: " + reqres);
            }
        }
    });

    if (res.isConfirmed) {
        Swal.fire({
            title: "%T%KICKED%T%",
            text: "%T%PLAYER_KICKED%T%",
            icon: "success",
            timer: 2000,
            timerProgressBar: true
        });
    }
}

var TEMPLATE_PLAYER_MENU = '\
<a><b>%T%DISPLAYNAME%T%: </b>%DISPLAYNAME%</a><span class="material-icons-outlined copyClipboardBtn" onclick="copyClipboard(\'%NAME%\');">content_copy</span><br>\
<a><b>UUID: </b>%UUID%</a><span class="material-icons-outlined copyClipboardBtn" onclick="copyClipboard(\'%UUID%\');">content_copy</span><br>\
<a><b>%T%GAMEMODE%T%: </b>%GAMEMODE%</a><br><br>\
<a><b>%T%POSITION%T%: </b>X: %X%, Y: %Y%, Z: %Z% %T%IN%T% %WORLD%</a><br>\
<a><b>%T%HEALTH%T%: </b>%HEALTH%/%HEALTH_MAX%</a><br>\
<a><b>%T%FOOD%T%: </b>%FOOD%/20</a><br>\
<a><b>%T%XP%T%: </b>%XPLEVEL% %T%LEVEL%T% (%T%XP_REQUIRED_FOR_NEXT_LEVEL%T%: %XPHASFORNEXTLEVEL%/%XPFORNEXTLEVEL% %T%XP%T%)</a><br>\
<a><b>%T%ALT_ACCOUNTS%T%: </b>%ALTS%</a><br>\
<div class="button is-info m-1" onclick="sendMessageClick(this.getAttribute(\'data-uuid\'))" data-uuid="%UUID%">%T%ACTION_MESSAGE%T%</div>\
<div class="button is-danger m-1" onclick="kickClick(this.getAttribute(\'data-uuid\'))" data-uuid="%UUID%">%T%ACTION_KICK%T%</div>\
';