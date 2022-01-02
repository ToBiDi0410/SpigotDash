var current_edit_group = '';

async function initPage() {}

async function getCurrentData() {
    var data = await getDataFromAPI({ TYPE: "ACCOUNT", METHOD: "GET_GROUPS" });
    return { groups: data };
}

async function startEdit(groupID) {
    var GROUP = (await getCurrentData()).groups[groupID];
    var menu = new smartMenu("GROUPINFO", groupID, GROUP.name);
    menu.open();

    current_edit_group = groupID;
    menu.setHTML(document.querySelector(".groupEditDialogueTemplate").innerHTML);
    var picker = initPicker(menu.getContentDOM().querySelector(".groupColor"), GROUP.html_color);

    menu.getContentDOM().querySelector(".saveButton").onclick = function() {
        var NEW_NAME = menu.getContentDOM().querySelector("input[data-attribpath='name']").value;
        var NEW_COLOR = picker.color.hex;
        var NEW_PERM_SET = dynamicDataManager.objectizeCheckboxSync(menu.getContentDOM().querySelector(".permList"));
        updateGroup(groupID, NEW_NAME, NEW_COLOR, NEW_PERM_SET);
    }
}

function initPicker(DOM, startcolor) {
    var picker = new Picker(DOM);
    picker.setColor(startcolor);
    picker.onDone = function(color) {
        DOM.style.backgroundColor = color.hex;
        DOM.innerHTML = color.hex;
    };
    DOM.innerHTML = startcolor;
    DOM.style.backgroundColor = startcolor;

    return picker;
}

async function deleteGroup(groupID) {
    var SWAL_RES = await Swal.fire({
        title: "%T%ARE_YOU_SURE%T%",
        html: "%T%REALLY_DELETE_GROUP%T%<br>%T%CAN_HAVE_BIG_EFFECTS%T%",
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

        var res = await getDataFromAPI({ TYPE: "ACCOUNT", METHOD: "DELETE_GROUP", GROUP: groupID });

        if (res == "DELETED") {
            Swal.fire({
                title: "%T%DELETED%T%".capitalizeFirstLetter(),
                icon: "success",
                html: "%T%GROUP_DELETED_SUCCESSFULLY%T%<br>%T%MIGHT_REQUIRE_NEW_LOGIN%T%".capitalizeFirstLetter(),
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

async function updateGroup(ID, NAME, COLOR, PERMS) {
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

        var res = await getDataFromAPI({ TYPE: "ACCOUNT", METHOD: "UPDATE_GROUP", GROUP: ID, PERMS: PERMS, HTML_COLOR: COLOR, NAME: NAME });

        if (res == "FIELDS_UPDATED") {
            Swal.fire({
                title: "%T%CHANGED%T%".capitalizeFirstLetter(),
                icon: "success",
                html: "%T%GROUP_CHANGED_SUCCESSFULLY%T%<br>%T%MIGHT_REQUIRE_NEW_LOGIN%T%".capitalizeFirstLetter(),
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
    var res1 = await Swal.fire({
        title: "%T%CREATE_GROUP%T%",
        progressSteps: ["A", "B", "C", "D"],
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

    if (!res1.isDismissed) {
        var res2 = await Swal.fire({
            title: "%T%CREATE_GROUP%T%",
            progressSteps: ["A", "B", "C", "D"],
            currentProgressStep: 1,
            progressStepsDistance: 1,
            confirmButtonText: "➔",
            cancelButtonText: "%T%ABORT%T%",
            showCancelButton: true,
            input: 'text',
            inputLabel: '%T%COLOR%T% (HTML/CSS)',
            inputValue: '#000',
            preConfirm: () => {
                if (Swal.getInput().value == null || Swal.getInput().value == '' || Swal.getInput().value == undefined) {
                    Swal.showValidationMessage("This Field cannot be empty");
                    return false;
                }

                if (!Swal.getInput().value.isColor()) {
                    Swal.showValidationMessage("Invalid Value for this Field");
                    return false;
                }
                return undefined;
            }
        });

        if (!res2.isDismissed) {
            var res3 = await Swal.fire({
                title: "%T%CREATE_GROUP%T%",
                progressSteps: ["A", "B", "C", "D"],
                currentProgressStep: 1,
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

            if (!res3.isDismissed) {
                var res4 = await Swal.fire({
                    title: "%T%CREATE_GROUP%T%",
                    progressSteps: ["A", "B", "C", "D"],
                    currentProgressStep: 2,
                    progressStepsDistance: 1,
                    confirmButtonText: "%T%CREATE%T%",
                    showCancelButton: true,
                    cancelButtonText: "%T%ABORT%T%",
                    text: "%T%REALLY_CREATE_GROUP%T%"
                });

                if (!res4.isDismissed) {
                    var NAME = res1.value;
                    var COLOR = res2.value;
                    var PERMS = res3.value;
                    console.log(NAME);
                    console.log(COLOR);
                    console.log(PERMS);

                    var rescreate = await getDataFromAPI({ TYPE: "ACCOUNT", METHOD: "CREATE_GROUP", NAME: NAME, HTML_COLOR: COLOR, PERMS: PERMS });
                    if (rescreate.includes("CREATED")) {
                        Swal.fire({
                            title: "%T%CREATED%T%".capitalizeFirstLetter(),
                            icon: "success",
                            html: "%T%GROUP_CREATED_SUCCESSFULLY%T%".capitalizeFirstLetter(),
                            timer: 2000
                        });
                    } else {
                        Swal.fire({
                            title: "%T%OH_NO%T%",
                            icon: "error",
                            html: "%T%WERE_NOT_ABLE_TO%T% '%T%CREATE_GROUP%T%'".capitalizeFirstLetter(),
                            timer: 2000
                        });
                        console.error(rescreate);
                    }
                }
            }
        }
    }
}