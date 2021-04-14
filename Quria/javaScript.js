var place_tag_modal = document.getElementById("place-tag-modal");
var write_item_id_modal = document.getElementById("write-item-id-modal");
var place_tag_close = document.getElementById("place-tag-close");
var write_item_id_close = document.getElementById("write-item-id-close");
var itemIdP = document.getElementById("book_id"); 
var book_image = document.getElementById("book_pic");
var book_name = document.getElementById("book_name");


var books= [
  {
     "item_id": "28",
     "name": "Harry Potter",
     "picture":"harrypotter.jpg"
  },
  {
     "item_id": "18",
     "name": "Spegelmannen",
     "picture":"spegelmannen.jpg"
  },
  {
     "item_id": "14",
     "name": "Vi får väl trösta varandra",
     "picture":"varandra.jpg"
  },
  {
     "item_id": "36",
     "name": "Kungariket",
     "picture":"kungariket.jpg"
  }
 ];
 
   
  
 function showItemId(itemId) {

  for (var i = 0; i < books.length; i++) {
    var book_id=  books[i].item_id
    var bookItemId = itemId.toString().trim();

    if(bookItemId.localeCompare(book_id)== 0){

      itemIdP.innerHTML  = 'item id: '+book_id;
      book_name.innerHTML=  'name of the book is: '+books[i].name;
      book_image.src = 'pic/books/'+books[i].picture;
    break;
    }
  }
}

function showModal(status){
  if(status.localeCompare('false')==0){
    place_tag_modal.style.display = "none";
  }else{
    place_tag_modal.style.display = "block"
  }
}
place_tag_close.onclick = function() {
showModal('false');
}

write_item_id_close.onclick = function() {
  write_item_id_modal.style.display = "none";
}

  
function write_item_id() {
  write_item_id_modal.style.display = "none";
    showModal('true');
    var itemId = document.getElementById("item-id-input").value;
    ws.send('{"toDo": "write", "value": "'+itemId+'"}');
  }
  
  function show_write_modal(){
    write_item_id_modal.style.display = "block";
  }

  function do_check_in(value) {
    showModal('true');
    ws.send('{"toDo": "doCheckIn", "value": "'+value+'"}');
  }
  
  function show_item(){
    showModal('true');
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
      console.log(event.data);
      var json = JSON.parse(event.data);
      showModal('false');
      if(json.Done.localeCompare("read_item_id")==0){
        showItemId(json.value);
      }else{
        alert(json.value);
      }
    }
  }
  
  function onLoad() {
    document.getElementById("ws-status").innerHTML = "DISCONNECTED";
  }
  window.onload=onLoad;
