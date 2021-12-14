var curr_path = "./";
var editPath = "";
var editType = "";

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

        var bytes = await getDataFromAPI({ TYPE: "SYSFILE", METHOD: "GET", PATH: path });
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

async function deleteFile(event) {
    event.stopPropagation();
    var elem = event.target.parentElement.parentElement.querySelector(".buttonDelete");
    elem.classList.add("is-loading");

    try {
        var path = elem.parentElement.parentElement.parentElement.parentElement.getAttribute("data-relative-path");
        var SWAL_RES = await Swal.fire({
            title: "%T%ARE_YOU_SURE%T%",
            html: "%T%REALLY_DELETE_FILE%T%<br>%T%CANNOT_BE_UNDONE%T%",
            showCancelButton: true,
            cancelButtonText: "%T%NO_WORD%T%".capitalizeFirstLetter(),
            confirmButtonText: "%T%YES_WORD%T%".capitalizeFirstLetter(),
            confirmButtonColor: '#d33',
            cancelButtonColor: 'green',
        });

        if (SWAL_RES.isConfirmed) {
            Swal.fire({
                title: "%T%DELETING_FILE%T%...".capitalizeFirstLetter(),
            });
            Swal.enableLoading();

            var res = await getDataFromAPI({ TYPE: "SYSFILE", METHOD: "DELETE", PATH: path });

            if (res == "DELETED") {
                Swal.fire({
                    title: "%T%DELETED%T%".capitalizeFirstLetter(),
                    icon: "success",
                    html: "%T%FILE_DELETED_SUCCESSFULLY%T%".capitalizeFirstLetter(),
                    timer: 2000
                });
            } else {
                Swal.fire({
                    title: "%T%OH_NO%T%",
                    icon: "error",
                    html: "%T%WERE_NOT_ABLE_TO%T% %T%DELETE_THE_FILE%T%!<br>%T%PLEASE_DO_MANUALLY_OR_TRY_AGAIN%T%".capitalizeFirstLetter(),
                    timer: 2000
                });
            }

            refreshFiles();

        }


    } catch (err) {
        console.error(err);
    }

    elem.classList.remove("is-loading");
}

async function renameFile(event) {
    event.stopPropagation();
    var elem = event.target.parentElement.parentElement.querySelector(".buttonRename");
    elem.classList.add("is-loading");

    try {
        var path = elem.parentElement.parentElement.parentElement.parentElement.getAttribute("data-relative-path");
        var SWAL_RES = await Swal.fire({
            title: "%T%RENAME%T%".capitalizeFirstLetter(),
            html: "%T%HOW_SHOULD_FILE_BE_CALLED%T%",
            input: "text",
            inputValue: path.split("/").latest(),
            inputAttributes: {
                autocapitalize: "off"
            },
            showCancelButton: true,
            cancelButtonText: "%T%CANCEL%T%".capitalizeFirstLetter()
        });

        if (SWAL_RES.isConfirmed) {
            var name = SWAL_RES.value;
            Swal.fire({
                title: "%T%RENAMING_FILE%T%...",
            });
            Swal.enableLoading();

            var res = await getDataFromAPI({ TYPE: "SYSFILE", METHOD: "RENAME", PATH: path, NEWNAME: name });
            if (res == "RENAMED") {
                Swal.fire({
                    title: "%T%RENAMED%T%".capitalizeFirstLetter(),
                    icon: "success",
                    html: "%T%FILE_RENAMED_SUCCESSFULLY%T%",
                    timer: 2000
                });
            } else {
                Swal.fire({
                    title: "%T%OH_NO%T%",
                    icon: "error",
                    html: "%T%WERE_NOT_ABLE_TO%T% %T%RENAME_THE_FILE%T%!<br>%T%PLEASE_DO_MANUALLY_OR_TRY_AGAIN%T%",
                    timer: 2000
                });
            }

            refreshFiles();
        }
    } catch (err) {
        console.error(err);
    }

    elem.classList.remove("is-loading");
}

async function pasteEvent(event) {
    var elem = document.querySelector(".pasteButton");
    elem.classList.add("is-loading");
    var newPath = curr_path + editPath.split("/").latest();

    if (editType == "copy") {
        Swal.fire({
            title: "%T%COPYING_FILE%T%...",
            html: "%T%FILE_IS_BEEING_COPIED_TO%T% <b>" + newPath + "</b>...",
        });
        Swal.enableLoading();

        var res = await getDataFromAPI({ TYPE: "SYSFILE", METHOD: "COPY", PATH: editPath, NEWPATH: newPath });

        if (res == "COPIED") {
            Swal.fire({
                title: "%T%COPIED%T%".capitalizeFirstLetter(),
                icon: "success",
                html: ("%T%FILE_COPIED_SUCCESSFULLY%T% %T%TO%T% <b>" + newPath + "</b>").capitalizeFirstLetter(),
                timer: 2000
            });
        } else {
            Swal.fire({
                title: "%T%OH_NO%T%",
                icon: "error",
                html: "%T%WERE_NOT_ABLE_TO%T% %T%COPY_THE_FILE%T%!<br>%T%PLEASE_DO_MANUALLY_OR_TRY_AGAIN%T%".capitalizeFirstLetter(),
                timer: 2000
            });
        }

        refreshFiles();
    } else if (editType == "cut") {
        Swal.fire({
            title: ("%T%MOVING_FILE%T%...").capitalizeFirstLetter(),
            html: ("%T%FILE_IS_BEEING_MOVED_TO%T% <b>" + newPath + "</b>...").capitalizeFirstLetter(),
        });
        Swal.enableLoading();

        var res = await getDataFromAPI({ TYPE: "SYSFILE", METHOD: "MOVE", PATH: editPath, NEWPATH: newPath });

        if (res == "MOVED") {
            Swal.fire({
                title: "%T%MOVED%T%".capitalizeFirstLetter(),
                icon: "success",
                html: ("%T%FILE_MOVED_SUCCESSFULLY%T% %T%TO%T% <b>" + newPath + "</b>"),
                timer: 2000
            });
            editPath = "";
            editType = "";
            updatePasteButton();
        } else {
            Swal.fire({
                title: "%T%OH_NO%T%",
                icon: "error",
                html: "%T%WERE_NOT_ABLE_TO%T% %T%MOVE_THE_FILE%T%!<br>%T%PLEASE_DO_MANUALLY_OR_TRY_AGAIN%T%",
                timer: 2000
            });
        }

        refreshFiles();
    } else {
        await Swal.fire({
            title: "%T%INVALID%T%".capitalizeFirstLetter(),
            icon: "warning",
            html: "%T%NOTHING_SELECTED_TO_CUT_OR_COPY%T%",
            timer: 2000
        });
    }
    elem.classList.remove("is-loading");
}

async function openFileInViewer(path) {
    if(!PERMISSIONS["FILES_VIEW"]) return;
    var extension = path.split(".").latest();
    var name = path.split("/").latest();
    var content = "";
    var error = false;
    var error_text = false;

    try {
        var TYPE = await getDataFromAPI({ TYPE: "SYSFILE", METHOD: "GET_TYPE", PATH: path });
        console.log(TYPE);
        if (!TYPE.includes("text/") && !TYPE.includes("application/json")) {
            throw new Error("%T%ERROR_UNSUPPORTED_FILE_FORMAT%T%");
        }

        var data = await socketIoRequestAwaitFull({ TYPE: "SYSFILE", METHOD: "GET", PATH: path });

        if (data.CODE != 200) {
            throw new Error(await data.DATA);
        }

        content = byteArrayToString(data.DATA);
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
        //menu.setHTML('<code class="fileView_Content language-' + extension + '">' + content + '</code>');
        //menu.setHTML('<div id="editor" class="heightFill fileView_Content">' + content + '</div>');

        menu.setHTML("<a>Autosave: ON</a>");
        menu.open();

        var editor = new hljsEditor(menu.getContentDOM());

        if(PERMISSIONS["FILES_EDIT"]) {
            editor.registerCustomSaveCallback(async function(text) {
                var res = await getDataFromAPI({ TYPE: "SYSFILE", METHOD: "SAVE_EDIT", PATH: path, TEXT: text });
    
                if (res != "WRITTEN") {
                    alert("Failed to save File! Please be careful when closing the Site!");
                }
            });
        }

        editor.init();
        editor.setLanguage(extension);
        editor.setContent(content);
        editor.autoSaveTask();

        menu.setCloseCallback(async function() {
            editor.destroy();
        });
    }

}

async function uploadFile() {
    var res = await Swal.fire({
        title: "%T%UPLOAD_FILE%T%".capitalizeFirstLetter(),
        input: 'file',
        inputAttributes: {
            'accept': '*',
            'aria-label': '%T%UPLOAD_FILE%T%'
        },
        showCancelButton: true,
        cancelButtonText: "%T%CANCEL%T%".capitalizeFirstLetter()
    });

    if (res.value) {
        Swal.fire({
            title: "%T%UPLOADING_FILE%T%"
        });
        Swal.enableLoading();

        var formData = new FormData();
        formData.append("FILE", res.value);

        var ID = await fetch("../fileupload", { method: "POST", body: formData });
        ID = await ID.json();
        ID = ID.ID;

        var res = await getDataFromAPI({ TYPE: "SYSFILE", METHOD: "UPLOAD", PATH: curr_path, ID: ID });

        if (res == "WRITTEN") {
            Swal.fire({
                title: "%T%UPLOADED%T%".capitalizeFirstLetter(),
                icon: "success",
                html: "%T%FILE_UPLOADED_SUCCESSFULLY%T%",
                timer: 2000
            });
            editPath = "";
            editType = "";
            updatePasteButton();
        } else {
            Swal.fire({
                title: "%T%OH_NO%T%",
                icon: "error",
                html: "%T%WERE_NOT_ABLE_TO%T% %T%UPLOAD_THE_FILE%T%!<br>%T%PLEASE_DO_MANUALLY_OR_TRY_AGAIN%T%",
                timer: 2000
            });
        }

        refreshFiles();
    }
}

async function copyFile(event) {
    event.stopPropagation();
    var elem = event.target.parentElement.parentElement.querySelector(".buttonRename");
    editPath = elem.parentElement.parentElement.parentElement.parentElement.getAttribute("data-relative-path");
    editType = "copy";
    updatePasteButton();
}

async function cutFile(event) {
    event.stopPropagation();
    var elem = event.target.parentElement.parentElement.querySelector(".buttonRename");
    editPath = elem.parentElement.parentElement.parentElement.parentElement.getAttribute("data-relative-path");
    editType = "cut";
    updatePasteButton();
}

function updatePasteButton() {
    document.querySelector(".filePasteID").innerHTML = editPath;
    document.querySelector(".filePasteType").innerHTML = editType;
}

async function readFileAsBytes(file) {
    return new Promise((resolve, reject) => {
        var reader = new FileReader();

        reader.onload = function() {
            resolve(new Uint8Array(this.result))
        }

        reader.onerror = function() {
            resolve(null);
        }

        reader.readAsArrayBuffer(file);
    })
}

function goBackPath() {
    if (curr_path.split("/").length < 2) return;
    curr_path = curr_path.split("/")[0];
    if (curr_path == ".") curr_path = "./";
    refreshFiles();
}