var androiduri;

function getParameterByName(name, url = window.location.href) {
    name = name.replace(/[\[\]]/g, '\\$&');
    var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

function getIdFromParam() {
    var itemIdP = document.getElementById("item-id");
    var itemId = getParameterByName('itemId');
    itemIdP.textContent = `item id: ${getParameterByName('itemId')}`;
}; 
function openAppen(){
    var itemId = document.getElementById("item-id-input").value;
    location.href = "androidrfid://primaryid?itemid=" + itemId;
}

const messageWindow = document.getElementById("messages");

const sendButton = document.getElementById("send");
const messageInput = document.getElementById("message");

const fileInput = document.getElementById("file");
const sendImageButton = document.getElementById("sendImage");

const socket = new WebSocket("ws://localhost:8080/socket");
socket.binaryType = "arraybuffer";

socket.onopen = function (event) {
    addMessageToWindow("Connected");
};

socket.onmessage = function (event) {
    if (event.data instanceof ArrayBuffer) {
        addMessageToWindow('Got Image:');
        addImageToWindow(event.data);
    } else {
        addMessageToWindow(`Got Message: ${event.data}`);
    }
};

sendButton.onclick = function (event) {
    sendMessage(messageInput.value);
    messageInput.value = "";
};

sendImageButton.onclick = function (event) {
    let file = fileInput.files[0];
    sendMessage(file);
    fileInput.value = null;
};

function sendMessage(message) {
    socket.send(message);
    addMessageToWindow("Sent Message: " + message);
}

function addMessageToWindow(message) {
    messageWindow.innerHTML += `<div>${message}</div>`
}

function addImageToWindow(image) {
    let url = URL.createObjectURL(new Blob([image]));
    messageWindow.innerHTML += `<img src="${url}"/>`
}