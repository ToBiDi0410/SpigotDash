var curr_path = "./";

var FILE_TEMPLATE_FOLDER = '<tr onclick="fileListClickEntry(this);" data-relative-path="%RELPATH%">\
    <th><span class="material-icons-outlined">folder</span></th >\
    <td>%NAME%</td>\
    <td>%MODF%</td>\
    <td></td>\
    <tr>';

var FILE_TEMPLATE_FILE = '<tr onclick="fileListClickEntry(this);" data-relative-path="%RELPATH%">\
    <th><span class="material-icons-outlined">insert_drive_file</span></th>\
    <td>%NAME%</td>\
    <td>%MODF%</td>\
    <td><div class="button is-success buttonDownload" onclick="downloadFile(event)" data-relative-path="%RELPATH%"><span class="material-icons-outlined">file_download</span></div></td>\
    <tr>';

var FILE_TEMPLATE_BACK = '<tr onclick="goBackPath();">\
    <th><span class="material-icons-outlined">undo</span></th >\
    <td>%T%PREVIOUS_FOLDER%T%</td>\
    <td></td>\
    <tr>';

async function refreshFiles() {
    var files = await getDataFromAPI({ method: "GET_FILES_IN_PATH", path: curr_path });
    files = files.sort((a, b) => (a.DIR == false) ? 1 : -1);
    var table_body = document.getElementsByTagName("tbody")[0];
    table_body.innerHTML = "";

    files.forEach((elem) => {
        table_body.innerHTML += getHTMLForFile(elem);
    });

    if (curr_path != "./") table_body.innerHTML = FILE_TEMPLATE_BACK + table_body.innerHTML;

    return files;
}

function initPage() {
    refreshFiles();
}

function getHTMLForFile(FILE_OBJ) {
    var html = "";
    if (FILE_OBJ.DIR == true) {
        html = FILE_TEMPLATE_FOLDER;
        FILE_OBJ.NAME += "/";
    } else {
        html = FILE_TEMPLATE_FILE;
    }

    html = html.replaceAll("%NAME%", FILE_OBJ.NAME);
    html = html.replaceAll("%MODF%", (new Date(FILE_OBJ.LAST_CHANGED)).toLocaleString());
    html = html.replaceAll("%RELPATH%", curr_path + FILE_OBJ.NAME);

    return html;
}

async function fileListClickEntry(elem) {
    var path = elem.getAttribute("data-relative-path");
    var path_chars = path.split("");

    if (path_chars[path_chars.length - 1] == "/") {
        curr_path = path;
        refreshFiles();
    } else {
        openFileInViewer(path);
    }
}


async function downloadFile(event) {
    event.stopPropagation();
    var elem = event.target.parentElement.parentElement.querySelector(".buttonDownload");
    elem.classList.add("is-loading");
    console.log(elem);

    try {
        var path = elem.getAttribute("data-relative-path");
        var data = await fetch(API_URL, {
            "headers": {
                "accept": "*/*",
                "accept-language": "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7,nl;q=0.6",
                "cache-control": "no-cache",
                "content-type": "application/json;charset=UTF-8",
            },
            "body": '{\n    "method": "GET_FILE_WITH_PATH", "path": "' + path + '"\n}',
            "method": "POST",
            "Cache-Control": "no-cache"

        });

        var blob = await data.blob();
        var url = window.URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = url;
        a.download = path.split("/").latest();
        document.body.appendChild(a);
        a.click();
        a.remove();
    } catch (err) {
        console.error(err);
    }

    elem.classList.remove("is-loading");
}

async function openFileInViewer(path) {
    var extension = path.split(".").latest();
    var content = "";
    var error = false;
    var error_text = false;

    try {
        if (!(path.includes("yml") || path.includes("yaml") || path.includes("json") || path.includes("txt") || path.includes("propertities"))) {
            throw new Error("%T%Error_Unsupported_File_Format%T%");
        }


        var data = await fetch(API_URL, {
            "headers": {
                "accept": "*/*",
                "accept-language": "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7,nl;q=0.6",
                "cache-control": "no-cache",
                "content-type": "application/json;charset=UTF-8",
            },
            "body": '{\n    "method": "GET_FILE_WITH_PATH", "path": "' + path + '"\n}',
            "method": "POST",
            "Cache-Control": "no-cache"

        });

        if (data.status != 200) {
            throw new Error(await data.text());
        }

        content = await data.text();
        content = content.replaceAll("\n", "<br>")
        extension = extension.replace("yml", "yaml");
    } catch (err) {
        error = true;
        error_text = err.toString();
    }

    if (error) {
        Swal.fire({
            title: path.split("/").latest(),
            customClass: 'swal-fileview',
            icon: "error",
            html: '%T%ERROR_OCCURED%T%<br><div style="background-color: var(--lt-color-gray-400); width: fit-content; padding: 2%; left: 0; right: 0; margin: auto; border-radius: 5%; font-style: italic; font-size: 75%; font-decoration: cursive;">' + error_text + '</div>'
        });
    } else {
        Swal.fire({
            title: path.split("/").latest(),
            customClass: 'swal-fileviewc',
            html: '<code class="fileView_Content language-' + extension + '">' + content + '</code>'
        });
        hljs.highlightAll();
    }

}

function goBackPath() {
    if (curr_path.split("/").length < 2) return;
    curr_path = curr_path.split("/")[0];
    if (curr_path == ".") curr_path = "./";
    refreshFiles();
}

function toggleFileViewer(state) {
    var fileviewer = document.querySelector(".fileview");
    if (!state) {
        fileviewer.classList.remove("shown")
        fileviewer.classList.add("hidden");
    } else {
        fileviewer.classList.add("shown")
        fileviewer.classList.remove("hidden");
    }
}