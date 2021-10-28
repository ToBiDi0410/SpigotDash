function showPluginInfo() {
    Swal.fire({
        title: "About SpigotDash",
        html: "\
Thank you for using SpigotDash <div style=\"color:red; display: inline-block;\"><3</div><br><br>\
If you are missing a Feautre or something does not work as intended I´m open to new suggestions on my Discord!<br>\
<br>\
<b>Technical Details:</b><br>\
Author: MTDev / Tobias Dickes<br>\
Screen: " + document.body.clientWidth + "x" + document.body.clientHeight + "<br>\
URL: " + window.location.href + "<br>\
<br>\
<b>Discord: </b><a href=\"https://discord.gg/VvUXunkyMU\">https://discord.gg/VvUXunkyMU</a>"
    });
}