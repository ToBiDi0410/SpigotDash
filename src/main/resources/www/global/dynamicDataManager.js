var dynamicDataManager = {
    dynamicDataTask: dynamicDataTask,
    dataCategorys: {},
    DOMParser: new DOMParser(),
    cache: {},
    cacheClearTime: 10,
    defaultInterval: 5,
    ingore: [],
    running: false
};

function fillEmptyWithLoading() {
    for (field of document.querySelectorAll(".dataField:not(.filled)")) {
        field.innerHTML = '<div class="loader is-loading"></div>';
    }
}

async function dynamicDataTask() {
    if (dynamicDataManager.running) return;
    dynamicDataManager.running = true;

    //console.log("[DATA] Updating Dynamic Data...");
    for (parent of document.querySelectorAll(".dataParent[data-callback]")) {
        if (!parent.hasAttribute("data-interval")) parent.setAttribute("data-interval", dynamicDataManager.defaultInterval * 1000);
    }
    fillEmptyWithLoading();

    //---- INGORE PROCESSING ----
    dynamicDataManager.ignore = Array.from(document.querySelectorAll(".IGNORE, .IGNORE_IN_TEMPLATE"));

    //IGNORE INTERVALED FIELDS
    for (intervaled of document.querySelectorAll("*[data-interval]")) {
        if (!dynamicDataManager.ignore.includes(intervaled)) {
            var INTERVAL = intervaled.getAttribute("data-interval");
            var LASTUPDATE = intervaled.getAttribute("data-lastupdate");

            if (LASTUPDATE == null) {
                intervaled.setAttribute("data-lastupdate", Date.now() - INTERVAL);
            } else {
                var DIFF = Date.now() - LASTUPDATE;
                if (DIFF <= INTERVAL) {
                    dynamicDataManager.ignore.push(intervaled);
                } else {
                    intervaled.setAttribute("data-lastupdate", Date.now());
                }
            }
        }
    }

    //ADD CHILDREN OF IGNORED
    for (ignored of dynamicDataManager.ignore) {
        dynamicDataManager.ignore = dynamicDataManager.ignore.concat(Array.from(ignored.querySelectorAll(".dataField, .dataFieldIMG, .dataFieldAttrib, .dataFieldClass, .dataFieldToggle, .dataArray")));
    }


    //---- DATA INSERTION ----
    for (array of document.querySelectorAll(".dataArray")) {
        if (shouldBeUpdated(array)) {
            var lastParent = resolveDataParentWithPath(array);
            var path = lastParent.path;
            lastParent = lastParent.parent;

            var data = await getDataForParent(lastParent);
            var arrayData = resolvePath(data, path.join("."));
            var TEMPLATE_HTML = document.querySelector(".dataArrayTemplate[data-arrayid='" + array.getAttribute("data-arrayid") + "']").innerHTML;

            var i = 0;
            while (i < arrayData.length) {
                if (array.querySelector(".dataParent[data-path='" + i + "']") == null) {
                    var NEW_DOM = document.createElement("div");
                    NEW_DOM.classList.add("dataParent");
                    var TEMPLATE_HTML_DOM = parseHTMLToDOM(TEMPLATE_HTML);
                    NEW_DOM.appendChild(TEMPLATE_HTML_DOM);

                    var APPENDED_DOM = array.appendChild(NEW_DOM);
                    APPENDED_DOM.setAttribute("data-path", i);
                }
                i++;
            }

            array.querySelectorAll(":scope > .dataParent").forEach((elem) => {
                if (elem.getAttribute("data-path") >= i) {
                    elem.remove();
                }
            });
        }
    }

    for (dField of document.querySelectorAll(".dataField")) {
        if (shouldBeUpdated(dField)) {
            var lastParent = resolveDataParentWithPath(dField);
            var path = lastParent.path;
            lastParent = lastParent.parent;

            var data = await getDataForParent(lastParent);
            data = resolvePath(data, path.join("."));
            if (dField.hasAttribute("data-processor")) data = await evalAsyncWithScope(dField.getAttribute("data-processor"), data);

            if (dField.innerHTML != innerDATA || dField.innerHTML == "" || dField.innerHTML == " ") {
                dField.innerHTML = data;
                dField.classList.add("filled");
            }
        }
    }

    for (dField of document.querySelectorAll(".dataFieldIMG")) {
        if (shouldBeUpdated(dField)) {
            var lastParent = resolveDataParentWithPath(dField);
            var path = lastParent.path;
            lastParent = lastParent.parent;

            var data = await getDataForParent(lastParent);
            data = resolvePath(data, path.join("."));
            if (dField.hasAttribute("data-processor")) data = await evalAsyncWithScope(dField.getAttribute("data-processor"), data);

            if (!dField.hasAttribute("data-src") || dField.getAttribute("data-src") != data) {
                dField.setAttribute("data-src", data);
                dField.classList.add("is-loading");
                dField.classList.add("loader");
                dField.classList.add("imgLoadSocket");
                dField.removeAttribute("src");
            }
        }
    }

    for (dField of document.querySelectorAll(".dataFieldAttrib")) {
        if (shouldBeUpdated(dField)) {
            var lastParent = resolveDataParentWithPath(dField.parentElement);
            var path = lastParent.path;
            path.push(dField.getAttribute("data-attribpath"));
            lastParent = lastParent.parent;

            var data = await getDataForParent(lastParent);
            data = resolvePath(data, path.join("."));
            if (dField.hasAttribute("data-processor")) data = await evalAsyncWithScope(dField.getAttribute("data-processor"), data);
            var attribName = dField.getAttribute("data-attrib");

            if (!attribHasValue(dField, attribName, data)) {
                dField.setAttribute(dField.getAttribute("data-attrib"), data);
                if (dField.getAttribute("data-attrib") == "value") {
                    dField.value = data;
                }
            }
        }
    }

    for (dField of document.querySelectorAll(".dataFieldClass")) {
        if (shouldBeUpdated(dField)) {
            var lastParent = resolveDataParentWithPath(dField.parentElement);
            var path = lastParent.path;
            path.push(dField.getAttribute("data-classpath"));
            lastParent = lastParent.parent;

            var data = await getDataForParent(lastParent);
            var innerDATA = resolvePath(data, path.join("."));
            dField.classList.add(innerDATA);
        }
    }

    for (dField of document.querySelectorAll(".dataFieldToggle")) {
        if (shouldBeUpdated(dField)) {
            var lastParent = resolveDataParentWithPath(dField.parentElement);
            var path = lastParent.path;
            path.push(dField.getAttribute("data-togglepath"));
            lastParent = lastParent.parent;

            var data = await getDataForParent(lastParent);
            data = resolvePath(data, path.join("."));

            if (dField.hasAttribute("data-processor")) data = await evalAsyncWithScope(dField.getAttribute("data-processor"), data);

            var CHILDREN = dField.querySelectorAll(".dataFieldToggleEntry");
            for (dFieldEntry of CHILDREN) {
                if (dFieldEntry.hasAttribute("data-toggleCheck")) {
                    if (data == dFieldEntry.getAttribute("data-toggleCheck")) {
                        dFieldEntry.classList.add("active");
                        dFieldEntry.classList.remove("disabled");
                    } else {
                        dFieldEntry.classList.add("disabled");
                        dFieldEntry.classList.remove("active");
                    }
                }
            }
        }
    }

    dynamicDataManager.running = false;
}

function shouldBeUpdated(DOM) {
    return (!dynamicDataManager.ignore.includes(DOM));
}

function attribHasValue(DOM, Attrib, Value) {
    if (Attrib == "src") {
        return (DOM.src == Value);
    }
    return (DOM.hasAttribute(Attrib) && DOM.getAttribute(Attrib) == Value);
}

async function getDataForParent(DOM) {
    if (!DOM.hasAttribute("data-intid")) DOM.setAttribute("data-intid", Math.random());
    var intid = DOM.getAttribute("data-intid");
    if (dynamicDataManager.cache[intid] == null) {
        var evaluatedData = await evalAsync(DOM.getAttribute("data-callback"));
        dynamicDataManager.cache[intid] = { time: Date.now(), data: evaluatedData };
        console.log("[DATA] [CACHE] Built: " + intid);

        var DELETE_TIME = 5000;
        if (DOM.hasAttribute("data-interval")) {
            DELETE_TIME = DOM.getAttribute("data-interval");
        }
        setTimeout(function() {
            delete dynamicDataManager.cache[intid];
            console.log("[DATA] [CACHE] Deleted: " + intid);
        }, DELETE_TIME - 100);
    }
    var finalData = dynamicDataManager.cache[intid].data;
    return finalData;
}

function resolveDataParentWithPath(DOM) {
    var lastParent = DOM;
    var path = [];
    while (!lastParent.classList.contains("dataParent")) {
        if (lastParent.hasAttribute("data-path")) {
            path = [lastParent.getAttribute("data-path")].concat(path);
        }
        lastParent = lastParent.parentElement;
    }

    if (lastParent.hasAttribute("data-path")) {
        path = [lastParent.getAttribute("data-path")].concat(path);
        var temp = resolveDataParentWithPath(lastParent.parentElement);
        lastParent = temp.parent;
        path = temp.path.concat(path);
    }

    return { parent: lastParent, path: path };
}

function resolvePath(dataBase, path) {
    var data = dataBase;
    if (path == "") return dataBase;

    var pathSep = path.split(".");

    for (elem of pathSep) {
        if (!isNaN(elem)) {
            data = data[parseInt(elem)];
        } else if (elem == "" || elem == " " || elem == '') {
            data = data;
        } else {
            try {
                data = data[elem];
            } catch (err) {
                console.warn("[DATA] Failed to Resolve Path: " + path + " with Data: " + JSON.stringify(dataBase));
                return null;
            }
        }
    }

    return data;
}

function getPathToFirstDOMWithClass(name, baseDOM) {
    var firstParent = baseDOM;
    var path = [];
    while (firstParent != null && !firstParent.classList.contains(name)) {
        path = [firstParent].concat(path);
        firstParent = firstParent.parentElement;
    }

    return [firstParent, path];
}

function parseHTMLToDOM(html) {
    return (new DOMParser()).parseFromString(html, "text/html").body.firstChild;
}

function test() {
    return [
        [{ test: "lel" }, { test: "lul" }]
    ];
}