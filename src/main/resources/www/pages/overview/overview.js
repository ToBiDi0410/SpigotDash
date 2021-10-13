async function initPage() {
    //curr_task = updaterTask;
}

async function getCurrentData() {
    return (await getDataFromAPI({ TYPE: "PAGEDATA", PAGE: "OVERVIEW" }));
}