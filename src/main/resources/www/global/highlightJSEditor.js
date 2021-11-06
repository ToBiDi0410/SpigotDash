var LAST_SAVED = 0;

class hljsEditor {
    constructor(DOM) {
        this.DOM = DOM;
        this.inited = false;
        this.callbacks = [];
    }

    init() {
        if (this.DOM != null && !this.inited) {
            this.DOM = this.DOM.appendChild(new DOMParser().parseFromString(HLJSEDITOR_TEMPLATE, "text/html").body.firstChild);

            var OUTPUT = this.getHighlightingContentDOM();
            var CALLBACKS = this.callbacks;
            this.getEditAreaDOM().oninput = function() {
                var highlightHTML = this.value.replaceAll("\n", "<br>");
                if (highlightHTML.endsWith("<br>")) highlightHTML = highlightHTML.replaceLast("<br>", "<br>&nbsp;");
                OUTPUT.innerHTML = highlightHTML;
                hljs.highlightAll();
            }

            this.getEditAreaDOM().onscroll = function() {
                OUTPUT.scrollTop = this.scrollTop;
                OUTPUT.scrollLeft = this.scrollLeft;
            }

            this.inited = true;
        }
    }

    async autoSaveTask() {
        this.save();
        await timer(5000);
        this.autoSaveTask();
    }

    save() {
        for (var callb of this.callbacks) {
            callb(this.getEditAreaDOM().value);
        }
    }

    registerCustomSaveCallback(cb) {
        this.callbacks.push(cb);
    }

    setContent(text) {
        if (this.DOM != null && this.inited) {
            this.getEditAreaDOM().value = text;
            var highlightHTML = text.replaceAll("\n", "<br>");
            if (highlightHTML.endsWith("<br>")) highlightHTML = highlightHTML.replaceLast("<br>", "<br>&nbsp;");
            this.getHighlightingContentDOM().innerHTML = highlightHTML;
        }
        hljs.highlightAll();
    }

    setLanguage(name) {
        if (this.DOM != null && this.inited) {
            this.getHighlightingContentDOM().classList.add("language-" + name);
        }
    }

    getEditAreaDOM() {
        return this.DOM.querySelector(".hljsEditor_EDITAREA");
    }

    getHighlightingContentDOM() {
        return this.DOM.querySelector(".hljsEditor_HIGHLIGHT_CONTENT");
    }

    destroy() {
        this.save();
        this.DOM.remove();
        this.inited = false;
    }
}

var HLJSEDITOR_TEMPLATE = '\
<div class="hljsEditor">\
    <textarea class="hljsEditor_EDITAREA"></textarea>\
    \
    <code class="hljsEditor_HIGHLIGHT_CONTENT"></code>\
    \
</div>\
';