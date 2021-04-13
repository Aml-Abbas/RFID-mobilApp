var popup_modal = document.getElementById("popup-modal");
var close = document.getElementsByClassName("close")[0];

var books= [
  {
     "item_id": "28",
     "name": "Harry Potter",
     "picture":"harrypotter.jfif"
  },
  {
     "item_id": "18",
     "name": "Spegelmannen",
     "picture":"spegelmannen.jfif"
  },
  {
     "item_id": "14",
     "name": "Vi får väl trösta varandra",
     "picture":"varandra.jpg"
  },
  {
     "item_id": "36",
     "name": "Kungariket",
     "picture":"kungariket.jfif"
  }
 ];
 
   
  
 function showItemId(itemId) {

  var itemIdP = document.getElementsByClassName("item_id"); 
  var image = document.getElementsByClassName("image");
  var name = document.getElementsByClassName("name");

  for (var i = 0; i < books.length; i++) {
    var book_id=  books[i].item_id.trim();

    console.log("item_nbr: "+ itemId);
    console.log("item_nbr for varje book: "+ book_id);
    console.log("is it equals: "+ (itemId.trim() == book_id.trim()));
    if(itemId.trim() == book_id.trim()){
      console.log("found it ");

      itemIdP.innerHTML = `item id: `+books[i].item_id;
      image.innerHTML= `<img src="pic/books/`+books[i].picture+`>`;
      name.innerHTML=  `name of the book is: `+books[i].name;
    }
  }
}

close.onclick = function() {
  popup_modal.style.display = "none";
}

window.onclick = function(event) {
  if (event.target == popup_modal) {
    popup_modal.style.display = "none";
  }
}

function write_item_id() {
  popup_modal.style.display = "block";
    var itemId = document.getElementById("item-id-input").value;
    ws.send('{"toDo": "write", "value": "'+itemId+'"}');
  }
  
  function do_check_in(value) {
    popup_modal.style.display = "block";
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
    }else if(event.data.includes("item id is:")){
      var item_nbr = event.data.replace( /^\D+/g, '');
      document.getElementsByClassName("item_id").innerHTML = item_nbr;
      showItemId(item_nbr);
    }else{
      popup_modal.style.display = "none";
      alert(event.data);
    }
  }
  
  function onLoad() {
    document.getElementById("ws-status").innerHTML = "DISCONNECTED";
  }
  window.onload=onLoad;