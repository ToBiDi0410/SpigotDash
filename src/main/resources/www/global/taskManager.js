var tasks = [];

function internalTimer() {
    for (const [key, value] of Object.entries(tasks)) {
        if (value.exit) {
            tasks[key].exited = true;
        } else {
            if ((value.last_executed + value.refreshDelay) <= Date.now()) {
                try {
                    value.callable();
                } catch (err) {
                    console.warn("[TASKMANAGER] Task with ID '" + key + "' generated an Exception:\n" + err);
                }
                tasks[key].last_executed = Date.now();
            }
        }
    }

    setTimeout(internalTimer, 10);
}

function addNewTask(id, callback, refreshDelay) {
    tasks[id] = { callable: callback, refreshDelay: refreshDelay, last_executed: 0, exit: false, exited: false };
}

async function stopTask(id) {
    if (tasks[id] == null) return true;
    tasks[id].exit = true;
    while (tasks[id].exited == false) {
        await timer(10);
    }
    return true;
}

internalTimer();