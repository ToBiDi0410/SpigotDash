async function initPage() {
    curr_task = updateLog;
    document.querySelector(".consolein").addEventListener("keyup", event => {
        if (event.key !== "Enter") return; // Use `.key` instead.
        document.querySelector(".consolebtn").click(); // Things you want to do.
        event.preventDefault(); // No need to `return false;`.
    });
}

async function executeCommand(elem) {
    var input = elem.parentElement.firstElementChild;
    var command = input.value;
    var btn = elem;

    input.setAttribute("disabled", "");
    btn.setAttribute("disabled", "");
    btn.classList.add("is-loading");

    var data = await getDataFromAPI({ method: "EXEC_COMMAND", command: command });

    input.removeAttribute("disabled");
    btn.removeAttribute("disabled");
    btn.classList.remove("is-loading");
    input.value = "";

    updateLog();
}

async function updateLog() {
    var data = await getDataFromAPI({ method: "GET_LOG" });
    if (!JSONMatches(data, currentData)) {
        var messagelist = document.querySelector(".console_messagelist");
        var newLines = data.filter((elem) => { return !currentData.includes(elem); });

        for (const elem of newLines) {
            var appended = messagelist.appendChild(generateLogListEntry(elem));
            setTimeout(function(elem) {
                elem.classList.add("FADEIN");
            }, 100, appended);

            await timer(10);
            messagelist.scrollTop = messagelist.scrollHeight;
        }

    }

    currentData = data;
}

var currentData = [];