class dataAPI {

    socket;
    serverPair = null;
    clientPair = null;
    currentID = 0;
    cache = {};

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
        var serverPublicKey = forge.pki.publicKeyFromPem(pair.PAIR_PUBLIC);
        this.serverPair = { ID: pair.PAIR_ID, PUBLIC_KEY: pair.PAIR_PUBLIC, ForgeKey: serverPublicKey };

        console.debug("[DATA-API] Generating new Key...");
        var generatedPair = await forge.rsa.generateKeyPair({ bits: 2048, workers: 2 });
        this.clientPair = { ID: crypto.randomUUID(), PUBLIC_KEY: forge.pki.publicKeyToPem(generatedPair.publicKey), ForgePair: generatedPair };
        await this.doRequest("ENCRYPTION", { SUBMETHOD: "SUGGEST_PAIR", PAIR_ID: this.clientPair.ID, PAIR_PUBLIC: btoa(this.clientPair.PUBLIC_KEY) }, false);

        console.debug("[DATA-API] Handshake done!");
    }

    async doRequest(namespace, payload, isEncrypted) {
        this.currentID++;
        console.debug("[DATA-API] Starting Request: " + this.currentID);

        var encrypted = this.serverPair != null && this.clientPair != null;
        if (isEncrypted != null) encrypted = isEncrypted;

        if (encrypted) {
            var encryptedMessage = await this.serverPair.ForgeKey.encrypt(JSON.stringify(payload));
            encryptedMessage = btoa(encryptedMessage);
            console.debug("[DATA-API] Encrypted Message for Request: " + this.currentID);
            var resultProm = new Promise((resolve, reject) => {
                this.socket.emit("WEBREQ", this.currentID, namespace, encryptedMessage, this.serverPair.ID, this.clientPair.ID, function(e) { resolve(arguments); });
            });
        } else {
            var resultProm = new Promise((resolve, reject) => {
                this.socket.emit("WEBREQ", this.currentID, namespace, JSON.stringify(payload), null, null, function(e) { resolve(arguments); });
            });
        }

        var res = await resultProm;
        console.debug("[DATA-API] Request got response: " + this.currentID);

        if (encrypted) {
            var decryptedJSON = await this.clientPair.ForgePair.privateKey.decrypt(atob(res[2]));
            console.debug("[DATA-API] Request done (decrypted): " + this.currentID);
            return { CODE: res[1], DATA: JSON.parse(decryptedJSON) };
        } else {
            console.debug("[DATA-API] Request done: " + this.currentID);
            return { CODE: res[1], DATA: JSON.parse(res[2]) };
        }

    }

    async doRequestOnlyData(namespace, payload, allowCache, cacheDeleteTime) {
        allowCache = allowCache != null && allowCache == true;
        cacheDeleteTime = cacheDeleteTime != null ? cacheDeleteTime : -1;

        if (!this.cache[namespace]) this.cache[namespace] = {};
        var PAYLOAD_STRING = JSON.stringify(payload);

        if (allowCache) {
            var cached = this.cache[namespace][PAYLOAD_STRING];
            if (cached) {
                //console.debug("[DATA-API] Used Cached Object to Fullfill Request");
                return cached.RESULT.DATA;
            }
        }
        var res = await this.doRequest.call(this, namespace, payload, true);

        if (allowCache) {
            this.cache[namespace][PAYLOAD_STRING] = { PAYLOAD: payload, RESULT: res };
            console.debug("[DATA-API] Cached new Object for further use");

            var that = this;
            if (cacheDeleteTime > 0) {
                setTimeout(function() {
                    delete that.cache[namespace][PAYLOAD_STRING]
                    console.debug("[DATA-API] Removed Cached Object!");
                }, cacheDeleteTime);
            }
        }
        return res.DATA;
    }
}

async function INIT_API_DATA() {
    setAppLoading("DATAAPI", true);
    window.store.globalDataAPI = new dataAPI();
    await window.store.globalDataAPI.init();
    await window.store.globalDataAPI.securityHandshake();
    setAppLoading("DATAAPI", false);
}