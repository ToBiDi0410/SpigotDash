var dataMan;

async function initPage() {
    curr_task = updaterTask;
}

async function updaterTask() {
    var data = await getDataFromAPI({ method: "GET_OVERVIEW" });
    insertObjectIntoHTML(data, document.body);
}