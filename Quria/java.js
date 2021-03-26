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

var ip = "localhost";
var port = "8888";
var ws = new WebSocket("ws://" + ip + ":" + port);
ws.onopen = function() {
  var doSendPing = confirm('connected! Send ping? Otherwise we will send "bla bla".');
  if (doSendPing) {
    ws.send('ping');
  } else {
    ws.send('bla bla');
  }
};
ws.onmessage = function (event) {
  if (event.data === 'echo') {
    alert(event.data);
  }
}

