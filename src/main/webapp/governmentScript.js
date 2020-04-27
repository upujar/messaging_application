let ws;
var heading=document.getElementById("log");
var heading2=document.getElementById("log2");
heading.innerHTML="Wire Tapping" +"\n";
heading2.innerHTML="Search Results" + "\n";


function getMsgs() {
    const from = document.getElementById("from").value;
    const start = document.getElementById("start").value;
    const end = document.getElementById("end").value;

    let request = new XMLHttpRequest();
    const url = "http://localhost:8080/prattle/rest/gov/query?from=" + start + "&to=" + end + "&user=" + from;
    request.open('GET', url, true);
    request.send();

    request.onload = function () {
        if (request.response===409){
            console.log("Error");
        }
        else{
            const data = JSON.parse(request.responseText)
            for (var i = 0; i < data.length; i++) {
                console.log("Processing Message");
                var log2 = document.getElementById("log2");
                var content =data[i].content;
                // var decrypted = content.split("").reverse().join("");
                let to = "";
                let timestamp = "";
                if (data[i].to !== null) {
                    to = " to " + data[i].to;
                    timestamp = new Date(data[i].timestamp);
                }
                if (content.includes("ERROR"))
                    alert(content);
                else
                    log2.innerHTML += data[i].from + to + " : " + content + "       " +timestamp+"\n";
            }
        }
    }
}

function connect() {
    const userId = document.getElementById("gov_id").value;
    const pwd = document.getElementById("password").value;

    if (userId === 'gov' && pwd === 'gov'){
        var host = document.location.host;
        var pathname = document.location.pathname;
        const url = "ws://" + "localhost:8080/prattle/" + "chat/" + userId
        createWebsocket(userId,url)
    }

    else
        alert("Reenter credentials");
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
        var log = document.getElementById("log");
        console.log(event.data);
        var message = JSON.parse(event.data);
        var content = message.content;
        // var decrypted = content.split("").reverse().join("");
        let to = "";
        let timestamp = "";
        if (message.to !== null) {
            to = " to " + message.to;
            timestamp = new Date(message.timestamp);
        }
        if (content.includes("ERROR"))
            alert(content);
        else
            log.innerHTML += message.from + to + " : " + content + "      "+timestamp+"\n";
    };


}