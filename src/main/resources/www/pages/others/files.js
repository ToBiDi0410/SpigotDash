var curr_path = "./";

function initPage() {}

async function getCurrentData() {
    var files = await getDataFromAPI({ TYPE: "DATA", METHOD: "GET_FILES_IN_PATH", PATH: curr_path });
    for (file of files) {
        if (file.DIR) {
            file.NAME += "/";
        }
    }
    files = files.sort((a, b) => (a.DIR == false) ? 1 : -1);
    return files;
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

async function refreshFiles() {
    document.querySelector(".dataParent[data-callback]").setAttribute("data-lastupdate", 0);
    dynamicDataManager.cache = {};
    dynamicDataManager.dynamicDataTask();
}


async function downloadFile(event) {
    event.stopPropagation();
    var elem = event.target.parentElement.parentElement.querySelector(".buttonDownload");
    elem.classList.add("is-loading");

    try {
        var path = elem.parentElement.parentElement.parentElement.parentElement.getAttribute("data-relative-path");

        var bytes = await getDataFromAPI({ TYPE: "SYSFILE", PATH: path });
        var ascii = new Uint8Array(bytes);

        var a = window.document.createElement('a');
        a.href = window.URL.createObjectURL(new Blob([ascii], { type: 'application/octet-stream' }));
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
    var name = path.split("/").latest();
    var content = "";
    var error = false;
    var error_text = false;

    try {
        if (!(path.includes("yml") || path.includes("yaml") || path.includes("json") || path.includes("txt") || path.includes("propertities"))) {
            throw new Error("%T%ERROR_UNSUPPORTED_FILE_FORMAT%T%");
        }


        var data = await socketIoRequestAwaitFull({ TYPE: "SYSFILE", PATH: path });

        if (data.CODE != 200) {
            throw new Error(await data.DATA);
        }

        content = byteArrayToString(data.DATA);
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
        var menu = new smartMenu("FILEVIEWER", name, name);
        menu.setHTML('<code class="fileView_Content language-' + extension + '">' + content + '</code>');
        menu.open();
        hljs.highlightAll();
    }

}

function goBackPath() {
    if (curr_path.split("/").length < 2) return;
    curr_path = curr_path.split("/")[0];
    if (curr_path == ".") curr_path = "./";
    refreshFiles();
}