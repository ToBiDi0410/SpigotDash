var players = [{ Location: { x: 0, y: 0, z: 0, YAW: 0, PITCH: 0, WORLD: "TEST" } }];

async function initPage() {
    //curr_task = updatePlayerList;
}

async function getCurrentData() {
    var data = await getDataFromAPI({ TYPE: 'PAGEDATA', PAGE: 'PLAYERS' });
    return { players: data };
}

async function showInfos(uuid) {
    var player = await getCurrentData();
    player = player.players.find(function(elem) { return (elem.UUID == uuid); });
    var menu = new smartMenu("PLAYERINFO", player.Name, minecraftStringToHTMLString(player.Displayname));
    menu.open();

    var TEMPLATE_DOM = (new DOMParser()).parseFromString(TEMPLATE_PLAYER_MENU.innerHTML, "text/html");
    TEMPLATE_DOM.querySelectorAll(".IGNORE_IN_TEMPLATE").forEach((elem) => { elem.classList.remove("IGNORE_IN_TEMPLATE"); });
    TEMPLATE_DOM = TEMPLATE_DOM.body.firstChild;

    TEMPLATE_DOM.setAttribute("data-processor", TEMPLATE_DOM.getAttribute("data-processor").replace("%UUID%", uuid));
    menu.setHTML(TEMPLATE_DOM.outerHTML);
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
            var reqres = await getDataFromAPI({ TYPE: "EXECUTE", METHOD: "PLAYER_ACTION", ACTION: "MESSAGE", MESSAGE: message, PLAYER: uuid })
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
            var reqres = await getDataFromAPI({ TYPE: "EXECUTE", METHOD: "PLAYER_ACTION", ACTION: "KICK", MESSAGE: message, PLAYER: uuid })
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

var TEMPLATE_PLAYER_MENU = document.querySelector(".playerMenuTemplate");