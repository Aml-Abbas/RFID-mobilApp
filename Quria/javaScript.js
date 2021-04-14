var place_tag_modal = document.getElementById("place-tag-modal");
var write_item_id_modal = document.getElementById("write-item-id-modal");
var success_modal = document.getElementById("success-modal");
var failed_modal = document.getElementById("failed-modal");

var place_tag_close = document.getElementById("place-tag-close");
var write_item_id_close = document.getElementById("write-item-id-close");
var success_close = document.getElementById("success-close");
var failed_close = document.getElementById("failed-close");

var success_text = document.getElementById("success-text"); 
var failed_text = document.getElementById("failed-text"); 

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

function showPlaceTagModal(status){
  if(status.localeCompare('false')==0){
    place_tag_modal.style.display = "none";
  }else{
    place_tag_modal.style.display = "block"
  }
}
place_tag_close.onclick = function() {
  showPlaceTagModal('false');
}

write_item_id_close.onclick = function() {
  write_item_id_modal.style.display = "none";
}

success_close.onclick = function() {
  success_modal.style.display = "none";
}
failed_close.onclick = function() {
  failed_modal.style.display = "none";
}

function showSuccessModal(status){
  success_text.innerHTML= status;
  success_modal.style.display = "block";
}

function showFailedModal(status){
  failed_text.innerHTML= status;
  failed_modal.style.display = "block";
}

  
function write_item_id() {
  write_item_id_modal.style.display = "none";
  showPlaceTagModal('true');
    var itemId = document.getElementById("item-id-input").value;
    ws.send('{"toDo": "write", "value": "'+itemId+'"}');
  }
  
  function show_write_modal(){
    write_item_id_modal.style.display = "block";
  }

  function do_check_in(value) {
    showPlaceTagModal('true');
    ws.send('{"toDo": "doCheckIn", "value": "'+value+'"}');
  }
  
  function show_item(){
    showPlaceTagModal('true');
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
      showPlaceTagModal('false');
      if(json.Done.localeCompare("read_item_id")==0){
        showItemId(json.value);
      }else{
        if(json.value.includes('Success')){
          showSuccessModal(json.value);
        }else{
          showFailedModal(json.value);
        }
      }
    }
  }
  
  function onLoad() {
    document.getElementById("ws-status").innerHTML = "DISCONNECTED";
  }
  window.onload=onLoad;

  function createCircleChart(percent, color, size, stroke) {
    let svg = `<svg class="mkc_circle-chart" viewbox="0 0 36 36" width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg">
        <path class="mkc_circle-bg" stroke="#eeeeee" stroke-width="${stroke * 0.5}" fill="none" d="M18 2.0845
              a 15.9155 15.9155 0 0 1 0 31.831
              a 15.9155 15.9155 0 0 1 0 -31.831"/>
        <path class="mkc_circle" stroke="${color}" stroke-width="${stroke}" stroke-dasharray="${percent},100" stroke-linecap="round" fill="none"
            d="M18 2.0845
              a 15.9155 15.9155 0 0 1 0 31.831
              a 15.9155 15.9155 0 0 1 0 -31.831" />
        <text class="mkc_info" x="50%" y="50%" alignment-baseline="central" text-anchor="middle" font-size="8">${percent}%</text>
    </svg>`;
    return svg;
}

let charts_success = document.getElementsByClassName('success-div');
let charts_failed = document.getElementsByClassName('failed-div');

for(let i=0;i<charts_success.length;i++) {
    let chart = charts_success[i];
    let percent = chart.dataset.percent;
    let color = ('color' in chart.dataset) ? chart.dataset.color : "#2F4F4F";
    let size = ('size' in chart.dataset) ? chart.dataset.size : "100";
    let stroke = ('stroke' in chart.dataset) ? chart.dataset.stroke : "1";
    charts_success[i].innerHTML = createCircleChart(percent, color, size, stroke);
}
for(let i=0;i<charts_failed.length;i++) {
  let chart = charts_failed[i];
  let percent = chart.dataset.percent;
  let color = ('color' in chart.dataset) ? chart.dataset.color : "#2F4F4F";
  let size = ('size' in chart.dataset) ? chart.dataset.size : "100";
  let stroke = ('stroke' in chart.dataset) ? chart.dataset.stroke : "1";
  charts_failed[i].innerHTML = createCircleChart(percent, color, size, stroke);
}
