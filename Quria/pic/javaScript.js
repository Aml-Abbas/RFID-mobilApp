function write_item_id() {
    var itemId = document.getElementById("item-id-input").value;
    ws.send('{"toDo": "write", "value": "'+itemId+'"}');
  }
  
  function do_check_in(value) {
    ws.send('{"toDo": "doCheckIn", "value": "'+value+'"}');
  }
  
  function sendPing() {
    ws.send('ping');
  }
  
  
  var ip = "localhost";
  var port = "8888";
  var ws = new WebSocket("ws://" + ip + ":" + port);
  ws.onopen = function() {
    document.getElementById("ws-status").innerHTML = "CONNECTED";
    var doSendPing = confirm('connected! Send ping? Otherwise we will send "bla bla".');
    if (doSendPing) {
      ws.send('ping');
    } else {
      ws.send('bla bla');
    }
  };
  ws.onclose = function(event) {
      document.getElementById("ws-status").innerHTML = "DISCONNECTED";
    console.log("WebSocket is closed now.");
  };
  ws.onmessage = function (event) {
    if (event.data === 'echo') {
      alert(event.data);
    }else{
      document.getElementById("item_id").innerHTML = event.data;
    }
  }
  
  function onLoad() {
    document.getElementById("ws-status").innerHTML = "DISCONNECTED";
  }
  window.onload=onLoad;
  
  
  function showItemId(itemId) {
      var itemIdP = document.getElementById("item_id");
      itemIdP.textContent = `item id: ${itemId}`;
  }; 