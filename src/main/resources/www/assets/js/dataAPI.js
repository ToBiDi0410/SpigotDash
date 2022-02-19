class dataAPI {

    socket;
    serverPair = null;
    clientPair = null;
    currentID = 0;

    constructor() {}

    async init() {
        console.debug("[DATA-API] Connecting Socket...");
        this.socket = io("ws://" + window.location.host + ":81");
    }

    async securityHandshake() {
        console.debug("[DATA-API] Securing connection...");
        var pair = await this.doRequest("ENCRYPTION", { SUBMETHOD: "GET_PAIR" });
        pair = pair.DATA;

        if (pair == null) {
            console.error("[DATA-API] Cannot secure connection!");
            return;
        }

        console.debug("[DATA-API] Got Key from Server");
        var recievedPair = new JSEncrypt();
        recievedPair.setPublicKey(pair.PAIR_PUBLIC);
        this.serverPair = { ID: pair.PAIR_ID, PUBLIC_KEY: recievedPair.getPublicKey(), JSEncryptPair: recievedPair };

        console.debug("[DATA-API] Generating new Key...");
        var generatedPair = new JSEncrypt({ default_key_size: 1024 * 2 });
        this.clientPair = { ID: crypto.randomUUID(), PUBLIC_KEY: generatedPair.getPublicKey(), PRIVATE_KEY: generatedPair.getPrivateKey(), JSEncryptPair: generatedPair };
        await this.doRequest("ENCRYPTION", { SUBMETHOD: "SUGGEST_PAIR", PAIR_ID: this.clientPair.ID, PAIR_PUBLIC: btoa(this.clientPair.PUBLIC_KEY) }, false);

        console.debug("[DATA-API] Handshake done!");
    }

    async doRequest(namespace, payload, isEncrypted) {
        this.currentID++;
        console.debug("[DATA-API] Starting Request: " + this.currentID);

        var encrypted = this.serverPair != null && this.clientPair != null;
        if (isEncrypted != null) encrypted = isEncrypted;

        if (encrypted) {
            var resultProm = new Promise((resolve, reject) => {
                var encryptedMessage = this.serverPair.JSEncryptPair.encrypt(JSON.stringify(payload));
                console.debug("[DATA-API] Encrypted Message for Request: " + this.currentID);
                this.socket.emit("WEBREQ", this.currentID, namespace, encryptedMessage, this.serverPair.ID, this.clientPair.ID, function(e) { resolve(arguments); });
            });
        } else {
            var resultProm = new Promise((resolve, reject) => {
                this.socket.emit("WEBREQ", this.currentID, namespace, JSON.stringify(payload), null, null, function(e) { resolve(arguments); });
            });
        }

        var res = await resultProm;
        console.debug("[DATA-API] Request done: " + this.currentID);

        if (encrypted) {
            var decryptedJSON = this.clientPair.JSEncryptPair.decrypt(res[2]);
            return { CODE: res[1], DATA: JSON.parse(decryptedJSON) };
        } else {
            return { CODE: res[1], DATA: JSON.parse(res[2]) };
        }
    }
}

var globalDataAPI;
(async function() {
    globalDataAPI = new dataAPI();
    await globalDataAPI.init();
    await globalDataAPI.securityHandshake();
})();