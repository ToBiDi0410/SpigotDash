var current_notifications = {};
var current_notifications_raw = {};

async function refreshNotifications() {
    var newNotifications = await getDataFromAPI({ method: "GET_NOTIFICATIONS" });

    if (!JSONMatches(current_notifications_raw, getIndependentObject(newNotifications))) {
        var newNotificationsSaved = getIndependentObject(newNotifications);

        var add = {};
        var remove = [];

        for (const [key, value] of Object.entries(newNotifications)) {
            if (current_notifications[key] == null && !value.closed) {
                value.level = value.level == "DANGER" ? "ERROR" : value.level;
                value.toast = VanillaToasts.create({
                    title: value.title,
                    text: value.message,
                    type: value.level.toLowerCase(),
                    callback: async function() {
                        var data = await getDataFromAPI({ method: "NOTIFICATION_CLOSED", uuid: value.uuid });
                    }
                });
            }
        }

        for (const [key, value] of Object.entries(current_notifications)) {
            if (newNotifications[key] == null) {
                try {
                    VanillaToasts.setTimeout(value.toast.id, 1000);
                } catch (err) {}
            }
        }

        current_notifications = newNotifications;
        current_notifications_raw = newNotificationsSaved;
    }
}