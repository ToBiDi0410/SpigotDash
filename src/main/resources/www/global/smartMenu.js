var smartMenuHelpers = {
    MENU_PATH: [],
    HTMLParser: new DOMParser(),
    currentlyConstructed: [],
    getConstructedByID: function(id) {
        for (const val of this.currentlyConstructed) {
            if (val.id == id) {
                return val;
            }
        }
        return null;
    },
    closeAll: async function() {
        for (const elem of this.currentlyConstructed.reverse()) {
            await elem.close();
            this.currentlyConstructed = this.currentlyConstructed.filter((elemf) => { return elemf != elem; });
        }
        MENU_PATH = [];
    }
}

var TEMPLATE_MENU_LAYER = '\
<div class="menuLayer">\
    <div class="navigation">\
        <div class="path">\
            %PATH%\
        </div>\
        <div class="hiddenFontHeighter">X</div>\
        <div class="buttons">\
            ← %T%BACK%T%\
        </div>\
    </div>\
    <div class="ctitle">\
        %TITLE%\
    </div>\
    <div class="content heightFill">\
        %HTML%\
    </div>\
</div>';

class smartMenu {
    constructor(id, path, title) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.html = '<progress class="progress is-small is-primary" max="100">15%</progress>';
        this.opened = false;
        this.closed = false;
        this.DOMChild = null;
    }

    open() {
        if (smartMenuHelpers.MENU_PATH.latest() == this.path) return;
        //FIX MENU DUPLICATION

        smartMenuHelpers.MENU_PATH[smartMenuHelpers.MENU_PATH.length] = this.path;
        var cont = document.querySelector(".menuLayerContainer");

        var DOM = HTMLParser.parseFromString(TEMPLATE_MENU_LAYER, 'text/html').body.firstChild;
        DOM.querySelector(".path").innerHTML = smartMenuHelpers.MENU_PATH.join(" → ");
        DOM.querySelector(".ctitle").innerHTML = this.title;

        DOM.querySelector(".content").innerHTML = this.html;

        if (smartMenuHelpers.MENU_PATH.length <= 1) {
            DOM.querySelector(".buttons").classList.add("disabled");
        } else {
            const elem = this;
            DOM.querySelector(".buttons").addEventListener("click", function() {
                elem.close();
            });
        }

        DOM.setAttribute("data-id", this.id);
        DOM.setAttribute("data-number", smartMenuHelpers.MENU_PATH.length);
        var child = cont.appendChild(DOM);

        this.opened = true;
        this.DOMChild = child;
        smartMenuHelpers.currentlyConstructed.push(this);
    }

    async close() {
        if (this.opened) {
            this.DOMChild.classList.add("disappear");
            await timer(500);
            this.DOMChild.remove();
            this.DOMChild = null;
        }

        smartMenuHelpers.MENU_PATH = smartMenuHelpers.MENU_PATH.filter((f) => { return f != this.path });

        this.opened = false;
        this.closed = true;
    }

    setHTML(html) {
        this.html = html;
        this.update();
    }

    update() {
        if (this.opened && this.DOMChild != null) {
            this.DOMChild.querySelector(".path").innerHTML = smartMenuHelpers.MENU_PATH.join(" → ");
            this.DOMChild.querySelector(".ctitle").innerHTML = this.title;
            this.DOMChild.querySelector(".content").innerHTML = this.html;
        } else {}
    }

    getContentDOM() {
        if (this.DOMChild != null) return this.DOMChild.querySelector(".content");
        return null;
    }
}


document.addEventListener("DOMContentLoadede", async function() {
    var base = new smartMenu(Math.random(), "Overview", "Example");
    base.open();

    await timer(1000);

    var menu = new smartMenu(Math.random(), "Players", "Players Extended");
    menu.open();

    while (!menu.closed) {
        menu.setHTML((Math.random() + "").repeat(1000));
        await timer(10);
    }

    console.log("CLOSED");
});