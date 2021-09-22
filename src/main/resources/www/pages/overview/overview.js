var dataMan;

async function initPage() {
    curr_task = updaterTask;
}

async function updaterTask() {
    var data = await getDataFromAPI({ TYPE: "PAGEDATA", PAGE: "OVERVIEW" });
    insertObjectIntoHTML(data, document.body);
}