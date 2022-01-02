async function initPage() {
    //curr_task = updatePlayerList;
}

async function getCurrentData() {
    var data = await getDataFromAPI({ TYPE: "ACCOUNT", METHOD: "GET_USERS" });
    GROUP_DATA = await getDataFromAPI({ TYPE: "ACCOUNT", METHOD: "GET_GROUPS" });
    GROUP_DATA["ignoreIDs"] = function(toIgnore) {
        var NEW_IDS = [];
        for (const [key, value] of Object.entries(this)) {
            if (!toIgnore.includes(key) && !(value instanceof Function)) {
                NEW_IDS.push(key);
            }
        }

        return NEW_IDS;
    }

    for (user of data) {
        user.nothasroles = GROUP_DATA.ignoreIDs(user.roles);
    }

    return {
        users: data,
        byName: function(n) {
            for (entry of this.users) {
                if (entry.name == n) {
                    console.log(entry);
                    return entry;
                }
            }
            return null;
        }
    };
}

var GROUP_DATA;

function getGroupData() {
    var NEW_DATA = {};
    for (const [key, value] of Object.entries(GROUP_DATA)) {
        if (!(value instanceof Function)) {
            NEW_DATA[key] = value;
        }
    }
    return NEW_DATA;
}


async function startEdit(userName) {
    var user = (await getCurrentData()).byName(userName);
    var menu = new smartMenu("USERINFO", userName, user.name);
    menu.open();

    menu.setHTML(document.querySelector(".userEditDialogueTemplate").innerHTML.replace("%USERNAME%", user.name));

    menu.getContentDOM().querySelector(".saveButton").onclick = function() {
        var contDOM = menu.getContentDOM();

        var NEW_PERM_SET = dynamicDataManager.objectizeCheckboxSync(contDOM.querySelector(".permList"));
        var NEW_NAME = menu.getContentDOM().querySelector("input[data-attribpath='name']").value;
        var NEW_PASS = menu.getContentDOM().querySelector("input[data-attribpath='passwordStarred']").value;

        var NEW_GROUPS = dynamicDataManager.objectizeCheckboxSync(menu.getContentDOM().querySelector(".groupsField"));
        var NEW_GROUPS_ARRAY = [];
        for (const [key, value] of Object.entries(NEW_GROUPS)) {
            if (value) NEW_GROUPS_ARRAY.push(key);
        }

        NEW_PASS = (NEW_PASS == user.passwordStarred) ? null : NEW_PASS;

        updateUser(user.name, NEW_NAME, NEW_PERM_SET, NEW_GROUPS_ARRAY, NEW_PASS);
    }
}

async function updateUser(NAME, NEWNAME, PERMS, ROLES, PASSWORD) {
    console.log(PASSWORD);
    console.log(ROLES);
    var SWAL_RES = await Swal.fire({
        title: "%T%ARE_YOU_SURE%T%",
        html: "%T%REALLY_APPLY_CHANGES%T%",
        showCancelButton: true,
        cancelButtonText: "%T%CANCEL%T%".capitalizeFirstLetter(),
        confirmButtonText: "%T%CONFIRM%T%".capitalizeFirstLetter(),
        confirmButtonColor: '#d33',
        cancelButtonColor: 'green',
    });

    if (SWAL_RES.isConfirmed) {
        Swal.fire({
            title: "%T%PROCESSING%T%...".capitalizeFirstLetter(),
        });

        var res = await getDataFromAPI({ TYPE: "ACCOUNT", METHOD: "UPDATE_USER", NAME: NAME, NEWNAME: NEWNAME, PERMS: PERMS, ROLES: ROLES, PASSWORD: PASSWORD });
        console.log(res);

        if (res == "UPDATED") {
            Swal.fire({
                title: "%T%CHANGED%T%".capitalizeFirstLetter(),
                icon: "success",
                html: "%T%USER_CHANGED_SUCCESSFULLY%T%<br>%T%MIGHT_REQUIRE_NEW_LOGIN%T%".capitalizeFirstLetter(),
                timer: 2000
            });
        } else {
            Swal.fire({
                title: "%T%OH_NO%T%",
                icon: "error",
                html: "%T%WERE_NOT_ABLE_TO%T% %T%PROCESS_CHANGES%T%".capitalizeFirstLetter(),
                timer: 2000
            });
        }
    }
}

async function deleteUser(userName) {
    var SWAL_RES = await Swal.fire({
        title: "%T%ARE_YOU_SURE%T%",
        html: "%T%REALLY_DELETE_USER%T%<br>%T%CAN_HAVE_BIG_EFFECTS%T%",
        showCancelButton: true,
        cancelButtonText: "%T%CANCEL%T%".capitalizeFirstLetter(),
        confirmButtonText: "%T%CONFIRM%T%".capitalizeFirstLetter(),
        confirmButtonColor: '#d33',
        cancelButtonColor: 'green',
    });

    if (SWAL_RES.isConfirmed) {
        Swal.fire({
            title: "%T%PROCESSING%T%...".capitalizeFirstLetter(),
        });

        var res = await getDataFromAPI({ TYPE: "ACCOUNT", METHOD: "DELETE_USER", NAME: userName });

        if (res == "DELETED") {
            Swal.fire({
                title: "%T%DELETED%T%".capitalizeFirstLetter(),
                icon: "success",
                html: "%T%USER_DELETED_SUCCESSFULLY%T%<br>%T%MIGHT_REQUIRE_NEW_LOGIN%T%".capitalizeFirstLetter(),
                timer: 2000
            });
        } else {
            Swal.fire({
                title: "%T%OH_NO%T%",
                icon: "error",
                html: "%T%WERE_NOT_ABLE_TO%T% %T%PROCESS_CHANGES%T%".capitalizeFirstLetter(),
                timer: 2000
            });
        }
    }
}

async function startCreateProcess() {
    // STEP 1: NAME
    var res1 = await Swal.fire({
        title: "%T%CREATE_USER%T%",
        progressSteps: ["A", "B", "C", "D", "E"],
        currentProgressStep: 0,
        progressStepsDistance: 1,
        confirmButtonText: "➔",
        cancelButtonText: "%T%ABORT%T%",
        showCancelButton: true,
        input: 'text',
        inputLabel: '%T%NAME%T%',
        preConfirm: () => {
            if (Swal.getInput().value == null || Swal.getInput().value == '' || Swal.getInput().value == undefined) {
                Swal.showValidationMessage("This Field cannot be empty");
                return false;
            }
            return undefined;
        }
    });
    if (res1.isDismissed) return;

    // STEP 2: GROUPS
    var res2 = await Swal.fire({
        title: "%T%CREATE_USER%T%",
        progressSteps: ["A", "B", "C", "D", "E"],
        currentProgressStep: 1,
        progressStepsDistance: 1,
        confirmButtonText: "➔",
        cancelButtonText: "%T%ABORT%T%",
        showCancelButton: true,
        html: `
        <div>%T%GROUPS%T%</div>
        <div class="dataParent groupList" data-callback="return getGroupData();">
            <div class="dataObjectArray" data-path="." data-arrayid="userCreateRoles"></div>
            <div class="dataObjectArrayTemplate IGNORE" data-arrayid="userCreateRoles">
                <label class="checkbox">
                    <input type="checkbox" class="dataFieldAttrib" data-attribpath="ID" data-attrib="data-checkpath">
                    <div class="dataField inline" data-path="name"></div>
                </label>
            </div>
        </div>`,
        preConfirm: () => {
            return dynamicDataManager.objectizeCheckboxSync(Swal.getHtmlContainer().querySelector(".groupList"));
        }
    });
    if (res2.isDismissed) return;

    // STEP 3: PERMS
    var res3 = await Swal.fire({
        title: "%T%CREATE_USER%T%",
        progressSteps: ["A", "B", "C", "D", "E"],
        currentProgressStep: 2,
        progressStepsDistance: 1,
        cancelButtonText: "%T%ABORT%T%",
        showCancelButton: true,
        confirmButtonText: "➔",
        html: `
        <div>%T%PERMISSIONS%T%</div>
        <div class="dataParent" data-callback="return { perms: {} };">
            <div class="loadFromTemplate" data-url="pages/others/permissionList.html"></div>
        </div>
        `,
        preConfirm: () => {
            return dynamicDataManager.objectizeCheckboxSync(Swal.getHtmlContainer().querySelector(".permList"));
        }
    });
    if (res3.isDismissed) return;

    // STEP 4: Inital Password
    var res4 = await Swal.fire({
        title: "%T%CREATE_USER%T%",
        progressSteps: ["A", "B", "C", "D", "E"],
        currentProgressStep: 3,
        progressStepsDistance: 1,
        confirmButtonText: "➔",
        cancelButtonText: "%T%ABORT%T%",
        showCancelButton: true,
        input: 'text',
        inputLabel: '%T%INITIAL_PASSWORD%T%',
        preConfirm: () => {
            if (Swal.getInput().value == null || Swal.getInput().value == '' || Swal.getInput().value == undefined) {
                Swal.showValidationMessage("This Field cannot be empty");
                return false;
            }
            return undefined;
        }
    });
    if (res4.isDismissed) return;

    // STEP 5: Confirm
    var res5 = await Swal.fire({
        title: "%T%CREATE_USER%T%",
        progressSteps: ["A", "B", "C", "D", "E"],
        currentProgressStep: 4,
        progressStepsDistance: 1,
        confirmButtonText: "%T%CREATE%T%",
        showCancelButton: true,
        cancelButtonText: "%T%ABORT%T%",
        text: "%T%REALLY_CREATE_USER%T%"
    });

    if (res5.isDismissed) return;

    var NAME = res1.value;
    var GROUPS = res2.value;
    var GROUPS_ARRAY = [];
    for (const [key, value] of Object.entries(GROUPS)) {
        if (value) GROUPS_ARRAY.push(key);
    }
    var PERMS = res3.value;
    var PASS = res4.value;

    var createRes = await getDataFromAPI({ TYPE: "ACCOUNT", METHOD: "CREATE_USER_ADV", NAME: NAME, PASSWORD: PASS, ROLES: GROUPS_ARRAY, PERMS: PERMS });
    if (createRes.includes("CREATED")) {
        Swal.fire({
            title: "%T%CREATED%T%".capitalizeFirstLetter(),
            icon: "success",
            html: "%T%USER_CREATED_SUCCESSFULLY%T%".capitalizeFirstLetter(),
            timer: 2000
        });
    } else {
        Swal.fire({
            title: "%T%OH_NO%T%",
            icon: "error",
            html: "%T%WERE_NOT_ABLE_TO%T% '%T%CREATE_USER%T%'".capitalizeFirstLetter(),
            timer: 2000
        });
        console.error(createRes);
    }
}