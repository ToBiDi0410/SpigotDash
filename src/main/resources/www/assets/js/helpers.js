function timeSince(date) {
    var seconds = Math.floor((new Date() - date) / 1000);

    var interval = seconds / 31536000;

    if (interval > 1) {
        return Math.floor(interval) + " y";
    }
    interval = seconds / 2592000;
    if (interval > 1) {
        return Math.floor(interval) + " m";
    }
    interval = seconds / 86400;
    if (interval > 1) {
        return Math.floor(interval) + " d";
    }
    interval = seconds / 3600;
    if (interval > 1) {
        return Math.floor(interval) + " h";
    }
    interval = seconds / 60;
    if (interval > 1) {
        return Math.floor(interval) + " min";
    }
    return Math.floor(seconds) + " s";
}

function wait(time) {
    return new Promise((resolve, reject) => {
        setTimeout(resolve, time);
    })
}

function INIT_HELPERS() {
    window.helpers = {
        timeSince: timeSince
    };
}