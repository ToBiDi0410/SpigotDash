var currentNotifications = {};
var newNotifications = null;

async function refreshNotifications() {
    if (newNotifications == null) {
        newNotifications = await socketIoRequestAwait({ TYPE: "DATA", METHOD: "GET_NOTIFICATIONS" });
    }

    for (const [key, value] of Object.entries(newNotifications)) {
        if (currentNotifications[key] == null && !value.closed) {
            console.log("[NOTIFICATIONS] OPENING: " + key);

            value.level = value.level == "DANGER" ? "ERROR" : value.level;
            value.toast = VanillaToasts.create({
                title: value.title,
                text: value.message,
                type: value.level.toLowerCase(),
                callback: async function() {
                    var data = await getDataFromAPI({ TYPE: "EXECUTE", METHOD: "NOTIFICATION_CLOSED", UUID: value.uuid });
                }
            });
            currentNotifications[key] = value;
        }
    }

    for (const [key, value] of Object.entries(currentNotifications)) {
        if (newNotifications[key] == null || newNotifications[key].closed) {
            console.log("[NOTIFICATIONS] CLOSING: " + key);
            VanillaToasts.setTimeout(value.toast.id, 1000);
            delete currentNotifications[key];
        }
    }
}

socket.on("NOTIFICATIONS", function(...args) {
    newNotifications = JSON.parse(args[0]);
});