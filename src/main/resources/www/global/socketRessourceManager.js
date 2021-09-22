var socketRessourceManager = {
    refreshImages: async function() {
        for (elem of document.querySelectorAll("img.imgLoadSocket")) {
            var src = elem.getAttribute("data-src");
            if (elem.src == null || elem.src == "" || elem.src == undefined) {
                var bytes = await socketIoRequestAwait({ TYPE: "WEBFILE", PATH: src });
                var ascii = new Uint8Array(bytes);
                var base64encoded = btoa(String.fromCharCode.apply(null, ascii));

                base64 = 'data:image/png;base64, ' + base64encoded;
                elem.src = base64;

                elem.classList.remove("loader");
                elem.classList.remove("is-loading");
            }
        }
    },
    update: async function() {
        socketRessourceManager.refreshImages();
    }
}

addNewTask("SOCRES", socketRessourceManager.update, 500);