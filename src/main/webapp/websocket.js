let ws;
let userId;

let groups;
let friends;

function openTab(evt, tab) {

    let i, tabcontent, tablinks;

    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    document.getElementById(tab).style.display = "block";
    evt.currentTarget.className += " active";
}

function initializeChatScreen() {
    let dropdown = document.getElementById('to-dropdown');
    dropdown.length = 0;

    let defaultOption = document.createElement('option');
    defaultOption.text = 'Choose user';

    dropdown.add(defaultOption);
    dropdown.selectedIndex = 0;

    const chatTypeDropdown = document.getElementById('chatType');
    const chatType = chatTypeDropdown.options[chatTypeDropdown.selectedIndex].value;

    getMembers(chatType, dropdown);
}

function getMembers(type, dropdown) {
    const request = new XMLHttpRequest();
    let url = type === 'group' ?
        'http://localhost:8080/prattle/rest/group/get_groups?userid=' + userId :
        'http://localhost:8080/prattle/rest/user/get_friends?userid=' + userId;
    request.open('GET', url, true);

    request.onload = function () {
        if (request.status === 200) {
            console.log(request.responseText);
            let data;
            if (type === 'group') {
                groups = JSON.parse(request.responseText);
                data = groups;
            } else {
                friends = JSON.parse(request.responseText);
                data = friends;
            }

            if (dropdown !== undefined || dropdown !== null) {
                let option;
                for (let i = 0; i < data.length; i++) {
                    option = document.createElement('option');
                    option.text = data[i];
                    option.value = data[i];
                    dropdown.add(option);
                }
            }
        } else {
            alert("Couldn't get members");
        }
    };

    request.onerror = function () {
        console.error('An error occurred fetching the JSON from ' + url);
    };

    request.send();
}

function createWebsocket(user, url) {

    this.ws = new WebSocket(url);
    console.log("new ws created");
    this.ws.onerror = function (ev) {
        console.log("error");
        console.log(ev);
    };

    this.ws.onmessage = function (event) {
        console.log("event occurred");
        const log = document.getElementById("log");
        console.log(event.data);
        const message = JSON.parse(event.data);
        const content = message.content;
        const decrypted = Array.from(content).reverse().join("");;
        let to = "";
        let timestamp = "<br>";
        if (message.to !== null) {
            to = " to " + message.to;
            timestamp += new Date(message.timestamp);
        }

        if (content.includes("ERROR"))
            alert(content);
        else {

            log.innerHTML += "<br>" + " "+message.from + to + " : " + decrypted + timestamp;

        }
    };

    document.getElementsByClassName('tablinks')[1].click();

}

function Login() {
    userId = document.getElementById("user_id").value;
    const pwd = document.getElementById("password").value;
    const request = new XMLHttpRequest();
    request.open('GET', 'http://localhost:8080/prattle/rest/user/query?userid=' + userId, true);
    request.send(userId);
    console.log(userId);
    request.onload = function () {
        if (request.status === 409) {
            alert("User not found");
        } else if (request.status === 200) {
            if (pwd !== request.responseText) {
                alert("wrong password");
            } else {
                const header = document.getElementById("header-container");
                header.innerHTML += "\n" + userId + " Logged in\n";
                const host = document.location.host;
                const pathname = document.location.pathname;
                const url = "ws://" + host + pathname + "chat/" + userId;
                createWebsocket(userId, url);
            }
        } else if (request.status === 500)
            alert("Server busy");
    }

}

function send() {
    if (this.ws === undefined)
        alert("Please connect to the application first");
    else {
        const messageBox = document.getElementsByClassName("emoji-wysiwyg-editor");
        const to = document.getElementById("to-dropdown");
        const msgTo = to.options[to.selectedIndex].value;
        console.log(messageBox[0].textContent);
        const content = messageBox[0].textContent;
        const encrypted = Array.from(content).reverse().join("");;
        console.log(encrypted);
        const user = document.getElementById("user_id").value;
        const json = JSON.stringify({
            "to": msgTo,
            "content": encrypted,
            "from": user
        });
        console.log(json);
        this.ws.send(json);
        console.log("sent msg to websocket");
    }
}

function createUser() {
    var request = new XMLHttpRequest();
    request.open('POST', 'http://localhost:8080/prattle/rest/user/create', true);
    var newUserId = document.getElementById("RegisterID").value;
    var pwd = document.getElementById("RegisterPassword").value;
    var name = document.getElementById("RegisterName").value;
    var profileName = document.getElementById("RegisterProfile").value;
    // var isPrivateAccount = document.getElementById("RegisterPrivateAccount").value;
    var currentTimeStamp = new Date().toISOString().slice(0, 19);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    var json = JSON.stringify({
        "memberId": newUserId,
        "name": name,
        "password": pwd,
        "profile": {
            "displayName": profileName
        },
        "userAccountSetting": {
            "devices": [{
                "deviceType": "mobile",
                "lastLogin": currentTimeStamp
            }]

        }
    });
    console.log(json);
    request.send(json);
    request.onload = function () {
        if (request.status === 409) {
            alert("User already present");
        } else if (request.status === 500)
            alert("Server busy");
        else if (request.status === 200) {
            alert("User Created");
        }
    }


}

function addUserToGroup(user, group) {
    const request = new XMLHttpRequest();
    request.open('POST', 'http://localhost:8080/prattle/rest/group/addUser', true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    let memberID = user || document.getElementById("MemberID").value;
    let groupId = group || document.getElementById("AddMemberToGroup").value;
    console.log(groupId);
    const addUserJson = JSON.stringify({
        "memberId": memberID,
        "groupId": groupId
    });
    console.log(addUserJson);
    request.send(addUserJson);
    request.onload = function () {
        if (request.status === 200)
            alert("User added");
        else if (request.status === 408)
            alert("Group not present");
        else if (request.status === 407)
            alert("User not present");
        else if (request.status === 500)
            alert("Server busy");
    };

}

function addModeratorToGroup() {
    const request = new XMLHttpRequest();
    request.open('POST', 'http://localhost:8080/prattle/rest/group/addModerator', true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    let moderatorId = document.getElementById("moderatorId").value;
    let groupId = document.getElementById("moderator-group").value;

    const json = JSON.stringify({
            "moderatorId": moderatorId,
            "groupId": groupId,
            "initiatorId": userId
        }
    );
    console.log(json);
    request.send(json);
    request.onload = function () {
        if (request.status === 200)
            alert("Moderator added");
        else if (request.status === 409)
            alert("You are not a moderator to be able to add another user as a moderator");
        else if (request.status === 500)
            alert("Server busy")
    };

}

function createGroup() {
    const request = new XMLHttpRequest();
    request.open('POST', 'http://localhost:8080/prattle/rest/group/create', true);
    const groupId = document.getElementById("GroupID").value;

    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    const reqJson = JSON.stringify({
        "memberId": groupId,
        "moderatorIds": userId
    });
    console.log(reqJson);
    request.send(reqJson);
    request.onload = function () {
        if (request.status === 200) {
            alert("Group Created");
            addUserToGroup(userId, groupId);
        } else if (request.status === 408) {
            alert("Wrong Moderator")
        } else if (request.status === 409) {
            alert("Group Already Exists")
        } else if (request.status === 500)
            alert("Server busy");

    };

}

function deleteGroup() {
    var request = new XMLHttpRequest();
    var groupId = document.getElementById("GroupToDelete").value;
    var url = 'http://localhost:8080/prattle/rest/group/delete';
    //request.setRequestHeader("Content-Type", "text/plain");
    request.open('POST', url, true);
    console.log(groupId);
    request.send(groupId);
    request.onload = function () {
        if (request.status === 409)
            alert("Group not found");
        else if (request.status === 200)
            alert("Group " + groupId + " deleted");
        else if (request.status === 500)
            alert("Server busy");
    }
}

function sendInvitation(f) {
    var request = new XMLHttpRequest();
    request.open('POST', 'http://localhost:8080/prattle/rest/user/invite', true);
    var fromUser = document.getElementById("user_id").value;

    var toGroup = document.getElementById("invite_group").value;
    console.log(toGroup);

    var toMember = f ? document.getElementById("to_request").value : document.getElementById("to_invite").value;

    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    var json1 = JSON.stringify({
        "fromInvite": fromUser,
        "toInvite": toMember,
        "groupId": toGroup,
        "isAccepted": 0
    });
    console.log(json1);

    request.send(json1);

    request.onload = function () {
        if (request.status === 200)
            alert("Request sent");
        else
            alert("Error sending request");
    }

}

function acceptUserInvitation(fromUser) {
    var request = new XMLHttpRequest();
    request.open('POST', 'http://localhost:8080/prattle/rest/user/invitation_respond', true);

    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    var json1 = JSON.stringify({
        "inviteId": fromUser + userId,
        "fromInvite": fromUser,
        "toInvite": userId,
        "groupId": "",
        "approverId": userId,
        "isAccepted": 1
    });
    console.log(json1);

    request.send(json1);


    let ret = false;
    request.onload = function () {
        if (request.status === 200) {
            ret = true;
            alert("Invitation accepted");
        } else if (request.status === 500)
            alert("Server busy");
        else
            alert("Error accepting request");
    };
    return ret;

}

function acceptGroupInvitation(from, to, group) {
    let request = new XMLHttpRequest();
    request.open('POST', 'http://localhost:8080/prattle/rest/group/invitation_respond', true);

    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    let json1 = JSON.stringify({
        "inviteId": from + to + group,
        "fromInvite": from,
        "toInvite": to,
        "groupId": group,
        "approverId": userId,
        "isAccepted": 1
    });
    console.log(json1);

    request.send(json1);

    request.onload = function () {
        if (request.status === 200) {
            alert("Invitation accepted");
        } else if (request.status === 500)
            alert("Server busy");
        else
            alert("Error sending request");
    };


}

function getModerators(group, dd) {
    const xhr = new XMLHttpRequest();
    const groupToEvict = dd ? document.getElementById('eviction_request_group').value : group;
    let moderator;
    let ret = null;
    xhr.open('GET', 'http://localhost:8080/prattle/rest/group/get_moderators?groupid=' + groupToEvict, true);
    xhr.onload = function () {
        if (xhr.status === 200) {
            moderator = xhr.responseText.split(',');
            console.log(moderator);
            if (dd) {
                const dropdown = document.getElementById("moderator-dropdown");
                dropdown.length = 0;
                let option;
                for (let i = 0; i < moderator.length; i++) {
                    option = document.createElement('option');
                    option.text = moderator[i];
                    option.value = moderator[i];
                    dropdown.add(option);
                }
            } else
                ret = moderator;
        } else {
            alert("Moderator Error");
        }
    };
    xhr.send();
    if (dd) return ret;
}

function getMembersOfGroup() {
    const xhr = new XMLHttpRequest();
    const groupToEvict = document.getElementById('eviction_request_group').value;
    let members;
    const dropdown = document.getElementById("members-dropdown");
    dropdown.length = 0;
    xhr.open('GET', 'http://localhost:8080/prattle/rest/group/get_all_members?groupid=' + groupToEvict, true);
    xhr.onload = function () {
        if (xhr.status === 200) {
            members = JSON.parse(xhr.responseText);
            console.log(members);
            let option;
            for (let i = 0; i < members.length; i++) {
                option = document.createElement('option');
                option.text = members[i];
                option.value = members[i];
                dropdown.add(option);
            }
        } else {
            alert("Member Error");
        }
    };
    xhr.send();
}

function evictGroupRequest() {
    const groupToEvict = document.getElementById('eviction_request_group').value;
    const approver = document.getElementById("moderator-dropdown");
    const app = approver.options[approver.selectedIndex].value;

    const regarding = document.getElementById("members-dropdown");
    console.log(regarding);
    const reg = regarding.options[regarding.selectedIndex].value;
    console.log(reg);

    const request = new XMLHttpRequest();
    const url = 'http://localhost:8080/prattle/rest/group/createEvictRequest';
    request.open('POST', url, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    const json = JSON.stringify({
        "initiator": userId,
        "regarding": reg,
        "approver": app,
        "place": groupToEvict

    });
    console.log(json);

    request.onload = function () {
        if (request.status === 200) {
            alert("Eviction request sent for " + reg);
        } else {
            alert("Error sending eviction request");
        }
    };

    request.send(json);
}

function getAllInvites() {

    const req = document.getElementById('friend-requests');
    const gReq = document.getElementById('group-requests');
    const eReq = document.getElementById('evict-requests');
    req.innerText = "";
    gReq.innerText = "";
    eReq.innerText = "";

    getFriendRequests(req);

    getEvictionRequests(eReq);

    getGroupRequests(gReq);


}

function getFriendRequests(req) {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', 'http://localhost:8080/prattle/rest/user/get_user_invitation?userid=' + userId, true);
    xhr.onload = function () {
        if (xhr.status === 200) {
            const data = JSON.parse(xhr.responseText)[0];
            console.log(data);
            if (data !== undefined) {
                const from = data.fromInvite;

                const acceptBtn = document.createElement("button");
                acceptBtn.innerHTML = "Accept";
                acceptBtn.addEventListener("click", function () {
                    if (acceptUserInvitation(from))
                        req.innerHTML = "";
                });
                req.innerHTML += "\n" + from + " sent you a friend request  \n";
                req.appendChild(acceptBtn);

            } else
                alert("no friend requests")
        } else {
            alert("Couldn't get requests");
        }
    };
    xhr.send();
}

function getGroupRequests(gReq) {
    const groupRequest = new XMLHttpRequest();

    groupRequest.open('GET', 'http://localhost:8080/prattle/rest/group/get_all_invitation?userid=' + userId, true);
    groupRequest.onload = function () {
        if (groupRequest.status === 200) {
            const res = JSON.parse(groupRequest.responseText)[0];
            if (res !== undefined) {
                const groupId = res.groupId;
                const to = res.toInvite;
                const from = res.fromInvite;
                const acceptBtn = document.createElement("button");
                acceptBtn.innerHTML = "Accept";
                acceptBtn.addEventListener("click", function () {
                    acceptGroupInvitation(from, to, groupId);
                });
                gReq.innerHTML += "\n" + from + " requested " + to + " to join " + groupId + " \n";
                gReq.appendChild(acceptBtn);
            } else {
                alert("No group invites to approve");
            }
        } else
            alert("Error getting group requests");
    };
    groupRequest.send();

}

function getEvictionRequests(eReq) {
    const evictReq = new XMLHttpRequest();
    evictReq.open('GET', 'http://localhost:8080/prattle/rest/group/requests?memberId=\'' + userId + '\'', true);
    evictReq.onload = function () {
        if (evictReq.status === 200) {
            console.log(evictReq.responseText);
            const res = JSON.parse(evictReq.responseText)[0];
            if (res !== undefined) {
                const reqId = res.requestId;
                const initiator = res.initiator;
                const regarding = res.regarding;
                const grp = res.place;
                const acceptBtn = document.createElement("button");
                const rejBtn = document.createElement("button");
                acceptBtn.innerHTML = "Approve Eviction";
                rejBtn.innerHTML = "Reject Eviction";
                acceptBtn.addEventListener("click", function () {
                    approveEvictRequest(reqId, initiator, regarding, grp, true);
                });
                rejBtn.addEventListener("click", function () {
                    approveEvictRequest(reqId, initiator, regarding, grp, false);
                });
                eReq.innerHTML += initiator + " sent you a group eviction request for user " + regarding + " for group " + grp;
                eReq.appendChild(acceptBtn);
                eReq.appendChild(rejBtn);
            } else {
                alert("No eviction requests to approve");
            }
        } else if (evictReq.status === 500) {
            eReq.innerHTML += "\nYou are not a moderator for any groups\n";
        } else
            alert("Error getting eviction requests");
    };
    evictReq.send();
}

function approveEvictRequest(reqID, initiator, regarding, grp, approve) {
    let request = new XMLHttpRequest();
    const url = approve ?
        'http://localhost:8080/prattle/rest/group/approveRequest' :
        'http://localhost:8080/prattle/rest/group/rejectRequest';
    request.open('POST', url, true);

    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    let json = JSON.stringify({
            "requestId": reqID,
            "initiator": initiator,
            "regarding": regarding,
            "approver": userId,
            "place": grp,
            "type": "E"
        }
    );
    console.log(json);

    request.send(json);

    request.onload = function () {
        if (request.status === 200)
            alert("Eviction request approved");
        else
            alert("Error approving eviction request");
    }
}