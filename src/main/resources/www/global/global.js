var curr_task = null;
var encryptKey;

var customEncryptor = {
    fetchedKey: null,
    encrypter: null,
    init: async function() {
        this.fetchedKey = (await (await fetch("./encryptKey")).json());
        this.encrypter = new JSEncrypt();
        var formattedKey = "-----BEGIN RSA PRIVATE KEY-----\n" + chunk(this.fetchedKey.KEY, 64).join("\n") + "\n-----END RSA PRIVATE KEY-----";
        this.encrypter.setPublicKey(formattedKey);
        console.log("[SECURITY] RSA Encryptor set up!")
    },
    encryptData: function(data) {
        if (this.fetchedKey) {
            return this.encrypter.encrypt(data);
        } else {
            console.warn("[SECURITY] No Key found to encrypt data! Security issue!");
            return data;
        }
    },
    ready: function() {
        return (this.fetchedKey != null && this.encrypter != null);
    },
    encryptedFetch: function(url, options) {
        if (options.body != null && options.body != "" && this.ready()) {
            options.body = JSON.stringify({
                DATA: customEncryptor.encryptData(options.body),
                PAIR_ID: customEncryptor.fetchedKey.ID
            });
        }
        return fetch(url, options);
    }
}

function replaceObjectKeyInString(object, string) {
    return replaceObjectKeyInStringWithChar(object, string, "%");
}

function chunk(str, n) {
    var ret = [];
    var i;
    var len;

    for (i = 0, len = str.length; i < len; i += n) {
        ret.push(str.substr(i, n))
    }

    return ret
}

function replaceObjectKeyInStringWithChar(object, string, char) {
    for (var [key, value] of Object.entries(object)) {
        if (value == null || value == undefined) {
            value = "";
        }
        string = string.replaceAll(char + key.toUpperCase() + char, value.toString());
    }

    return string;
}

function insertObjectIntoHTML(object, tree) {
    for (var [key, value] of Object.entries(object)) {
        tree.querySelectorAll("*[data-apiobj='" + key.toUpperCase() + "'").forEach((elem) => {
            if (elem.innerHTML != value) {
                elem.innerHTML = value;
            }
        })
    }
}

function heightFillRestClass() {
    document.querySelectorAll(".heightFill").forEach((elem) => {
        elem.style.height = elem.parentElement.offsetHeight - elem.offsetTop + "px";
    });

    document.querySelectorAll(".maxHeightFill").forEach((elem) => {
        elem.style.maxHeight = elem.parentElement.offsetHeight - elem.offsetTop + "px";
    });
}

function byteArrayToString(array) {
    return String.fromCharCode.apply(null, new Uint8Array(array));
}

function arr_diff(a1, a2) {
    var a = [],
        diff = [];

    for (var i = 0; i < a1.length; i++) {
        a[a1[i]] = true;
    }

    for (var i = 0; i < a2.length; i++) {
        if (a[a2[i]]) {
            delete a[a2[i]];
        } else {
            a[a2[i]] = true;
        }
    }

    for (var k in a) {
        diff.push(k);
    }

    return diff;
}

function JSONMatches(objectone, objecttwo) {
    var objoneJSON = JSON.stringify(objectone);
    var objtwoJSON = JSON.stringify(objecttwo);
    return (objoneJSON == objtwoJSON);
}

function toggleExpandCart(elem) {
    var content = elem.querySelector(".card-content");
    content.classList.toggle("is-hiddenc");
    content.classList.toggle("is-expanded");
}

async function getDataFromAPI(body) {
    try {
        /*var data;
        data = await customEncryptor.encryptedFetch(API_URL, {
            method: "POST",
            mode: "cors",
            cache: "no-cache",
            redirect: "follow",
            credentials: "include",
            body: JSON.stringify(body)
        });
        showOffline(false);

        if (data.status == 401) {
            loginRequired();
            return null;
        }

        data = await data.json();
        return data;*/
        data = await socketIoRequestAwait(body);
        return data;
    } catch (err) {
        console.error(err);
        return null;
    }
}

function getIndependentObject(obj) {
    return JSON.parse(JSON.stringify(obj));
}

function showOffline(state) {
    if (state == false) {
        if (Swal.isVisible() && Swal.getTitle().textContent == "%T%RECONNECTING%T%") Swal.close();
        return;
    }

    if (state == true) {
        if (Swal.isVisible() && Swal.getTitle().textContent == "%T%RECONNECTING%T%") return;
        Swal.fire({
            title: "%T%RECONNECTING%T%",
            text: "%T%SERVER_OFFLINE%T%",
            showConfirmButton: false,
            allowOutsideClick: false,
            allowEscapeKey: false
        });
        Swal.showLoading();
        return;
    }
}

function loginRequired() {
    Swal.fire({
        title: "%T%LOGIN_REQUIRED_SHORT%T%",
        html: "%T%LOGIN_REQUIRED_POPUP%T%",
        showCancelButton: false,
        showConfirmButton: false,
        allowOutsideClick: false,
        allowEscapeKey: false
    });

    Swal.showLoading();

    setTimeout(function() {
        window.location.href = "./login.html";
    }, 1000);
}

function loadJSIfExists(url, container) {
    try {
        var s = document.createElement("script");
        s.type = "text/javascript";
        s.src = url;
        s.onload = function() {
            try { initPage(); } catch (err) {
                console.warn("[LOADER] The Page loaded doesn´t provide a valid Javascript file!");
            }
        }
        var appendedjs = container.append(s);
        return true;
    } catch (err) {
        return false;
    }
}

async function loadCSSIfExists(url, container) {
    try {
        var csst = await fetch(url);
        csst = await csst.text();
        container.innerHTML += '<style>' + csst + '</style>';
        return true;
    } catch (err) {
        return false;
    }
}

function copyClipboardElem(elem) {
    var copyTarget = elem.firstChild;
    copyClipboard(copyTarget.innerText);
}

function copyClipboard(text) {
    navigator.clipboard.writeText(text);
}

async function evalAsync(scr) {
    return eval("(async() => {" + scr + "})()");
}

async function evalAsyncWithScope(code, scope) {
    try {
        return eval("var scopeDat = JSON.parse('" + JSON.stringify(scope) + "'); (async(scope) => {" + code + "})(scopeDat);");
    } catch (err) {
        console.warn("[EXEC] Failed to Execute Async Code: " + code);
        console.warn("[EXEC] Provided Scope: " + JSON.stringify(scope));
        console.log(err);
    }
}

async function evalAsyncWithScopeAndElem(code, scope, elem) {
    try {
        var sck = Math.random();
        elem.setAttribute("data-scopeelemkey", sck);

        var TO_EXECUTE = `
            var scopeDat = JSON.parse('` + JSON.stringify(scope) + `');
            var scopeelemkey = "` + sck + `";

            (async(scope,elem) => {
                ` + code + `
            })
            (scopeDat, document.querySelector('*[data-scopeelemkey="' + scopeelemkey + '"]'))
        `;
        //console.log(TO_EXECUTE);

        var res = eval(TO_EXECUTE);
        elem.removeAttribute("data-scopeelemkey");
        return res;
    } catch (err) {
        console.warn("[EXEC] Failed to Execute Async Code: " + code);
        console.warn("[EXEC] Provided Scope: " + JSON.stringify(scope));
        console.warn("[EXEC] Provided Elem: " + elem);
        console.log(err);
    }
}


String.prototype.capitalizeFirstLetter = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

String.prototype.replaceLast = function(search, replace) {
    return this.replace(new RegExp(search + "([^" + search + "]*)$"), replace + "$1");
}

String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
};

String.prototype.isColor = function() {
    const s = new Option().style;
    s.color = this;
    return s.color !== '';
};

Array.prototype.latest = function() {
    return this[this.length - 1];
}

Array.prototype.includesKeyValue = function(key, value) {
    for (var i = 0; i < this.length; i++) {
        var obj = this[i];
        if (obj[key] != null && obj[key] == value) {
            return true;
        }
    }

    return false;
}

Array.prototype.getObjectWithKeyValue = function(key, value) {
    for (var i = 0; i < this.length; i++) {
        var obj = this[i];
        if (obj[key] != null && obj[key] == value) {
            return obj;
        }
    }

    return null;
}

Object.size = sizeObj;

function sizeObj(obj) {
    var size = 0,
        key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
}

function objectToArray(obj) {
    var array = [];
    for (key in obj) {
        var arrayEntry;
        if (obj[key] instanceof Object) {
            arrayEntry = Object.assign(obj[key], { KEY: key });
        } else {
            arrayEntry = Object.assign({ VALUE: obj[key] }, { KEY: key });
        }
        array.push(arrayEntry);
    }
    return array;
}

function getBaseURL() {
    var PARTS = window.location.href.split("/");
    PARTS.pop();

    return PARTS.join("/");
}

var API_URL = "./api";