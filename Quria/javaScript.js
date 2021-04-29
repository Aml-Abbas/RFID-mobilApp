var place_tag_modal = document.getElementById("place-tag-modal");
var write_item_id_modal = document.getElementById("write-item-id-modal");
var success_modal = document.getElementById("success-modal");
var failed_modal = document.getElementById("failed-modal");
var connection_modal = document.getElementById("connection-modal");
var check_out_modal = document.getElementById("check-out-modal");
var show_patron_modal = document.getElementById("show-patron-modal");
var send_ping_modal = document.getElementById("send-ping-modal");

var place_tag_close = document.getElementById("place-tag-close");
var write_item_id_close = document.getElementById("write-item-id-close");
var success_close = document.getElementById("success-close");
var failed_close = document.getElementById("failed-close");
var connection_close = document.getElementById("connection-close");
var check_out_close = document.getElementById("check-out-close");
var send_ping_close = document.getElementById("send-ping-close");

var success_text = document.getElementById("success-text"); 
var failed_text = document.getElementById("failed-text"); 
var patron_text = document.getElementById("patron"); 
var use_patron_text = document.getElementById("use-patron"); 
var place_tag_text = document.getElementById("place-tag-text"); 
var check_out_text = document.getElementById("check-out-text"); 

var itemIdP = document.getElementById("book_id"); 
var book_image = document.getElementById("book_pic");
var camera = document.getElementById("camera");

var isConnected= false;
use_patron_text.innerHTML="";

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

 var patrons= [
  {
     "item_id": "06025000003961",
     "name": "Marcus Lundberg",
  },
  {
     "item_id": "06038100002654",
     "name": "Elissa Edblad",
  },
  {
     "item_id": "any patron else",
     "name": "Lina Ericsson",
  }
 ];

   
  
 function showItemId(itemId) {
   var found= false;
  for (var i = 0; i < books.length; i++) {
    var book_id=  books[i].item_id
    var bookItemId = itemId.toString().trim();

    if(bookItemId.localeCompare(book_id)== 0){
      book_image.src = 'pic/books/'+books[i].picture;
      found= true;
    break;
    }
  }
  if(!found){
    book_image.src = '';
  }
  itemIdP.innerHTML  = 'item id: '+itemId;
}

function deleteShowedItemId() {
   book_image.src = '';
   itemIdP.innerHTML  = '';
}

function showPlaceTagModal(status, msg){
  if(status.localeCompare('false')==0){
    place_tag_modal.style.display = "none";
  }else{
    place_tag_text.innerHTML= msg;
    place_tag_modal.style.display = "block";
  }
}

function show_status(){
  send_ping_modal.style.display = "block";
}

place_tag_close.onclick = function() {
  showPlaceTagModal('false', '');
  ws.send('{"toDo": "doCheckIn", "value": "null"}');
  ws.send('{"toDo": "doReadTagInfo", "value": "null"}');
  ws.send('{"toDo": "write", "value": "null"}');
}

write_item_id_close.onclick = function() {
  write_item_id_modal.style.display = "none";
}

connection_close.onclick = function() {
  connection_modal.style.display = "none";
}

send_ping_close.onclick = function() {
  send_ping_modal.style.display = "none";
}

success_close.onclick = function() {
  success_modal.style.display = "none";
}

failed_close.onclick = function() {
  failed_modal.style.display = "none";
}

check_out_close.onclick = function() {
  check_out_modal.style.display = "none";
  patron_text.innerHTML="";
  use_patron_text.innerHTML= "";
  ws.send('{"toDo": "doCheckIn", "value": "null"}');
}

function showSuccessModal(status){
  success_text.innerHTML= status;
  success_modal.style.display = "block";
}

function showFailedModal(status){
  failed_text.innerHTML= status;
  failed_modal.style.display = "block";
}

  function show_camera_modal(){
    show_patron_modal.style.display= "block";
  }

function write_item_id() {
  write_item_id_modal.style.display = "none";
  showPlaceTagModal('true', 'Place the smartphone over the item you would like to program');
    var itemId = document.getElementById("item-id-input").value;
    ws.send('{"toDo": "write", "value": "'+itemId+'"}');
  }
  
  function show_write_modal(){
    if(!isConnected){
      connection_modal.style.display = "block";
    }else{
    deleteShowedItemId();
    write_item_id_modal.style.display = "block";
    }
    }

  function do_check_in(value) {
    if(!isConnected){
      connection_modal.style.display = "block";
    }else{
      deleteShowedItemId();
      if(value.localeCompare("true")==0){
        showPlaceTagModal('true', 'Place the smartphone over the item you would like to check in');
      }else{
        check_out_text.innerHTML= 'Place the smartphone over the item you would like to check out';
        check_out_modal.style.display= "block";
      }
    ws.send('{"toDo": "doCheckIn", "value": "'+value+'"}');
    }
    }
  
  function show_item(){
    if(!isConnected){
      connection_modal.style.display = "block";
    }else{
    showPlaceTagModal('true', 'Place the smartphone over the item you would like to show');
    ws.send('{"toDo": "doReadTagInfo", "value": "true"}');
    }
    }

  function sendPing() {
    if(!isConnected){
      connection_modal.style.display = "block";
    }else{
      ws.send('ping');
      send_ping_modal.style.display = "none";
    }
  }

window.onclick = function(event) {
  if (event.target === place_tag_modal) {
    place_tag_modal.style.display = "none";
    ws.send('{"toDo": "doCheckIn", "value": "null"}');
    ws.send('{"toDo": "doReadTagInfo", "value": "null"}');
    ws.send('{"toDo": "write", "value": "null"}');  
  }else if (event.target === check_out_modal) {
    check_out_modal.style.display = "none";
    patron_text.innerHTML="";
    use_patron_text.innerHTML= "";
    ws.send('{"toDo": "doCheckIn", "value": "null"}');
  }else if (event.target === connection_modal) {
    connection_modal.style.display = "none";
  }else if (event.target === write_item_id_modal) {
    write_item_id_modal.style.display = "none";
  }else if (event.target === show_patron_modal) {
    show_patron_modal.style.display = "none";
  }else if (event.target === success_modal) {
    success_modal.style.display = "none";
  }else if (event.target === failed_modal) {
    failed_modal.style.display = "none";
  }else if (event.target === send_ping_modal) {
    send_ping_modal.style.display = "none";
  }
}
  
  var ip = "localhost";
  var port = "8888";
  var ws = new WebSocket("ws://" + ip + ":" + port);
  ws.onopen = function() {
    isConnected= true;
    document.getElementById("ws-status").innerHTML = "CONNECTED";
  };
  ws.onclose = function(event) {
    isConnected= false;
      document.getElementById("ws-status").innerHTML = "DISCONNECTED";
    console.log("WebSocket is closed now.");
  };

  ws.onmessage = function (event) {
    if (event.data === 'echo') {
      alert(event.data);
    }else{
      console.log(event.data);
      var json = JSON.parse(event.data);
      showPlaceTagModal('false', '');
      check_out_modal.style.display= "none";
      patron_text.innerHTML="";
      use_patron_text.innerHTML= "";
          if(json.Done.localeCompare("read_item_id")==0){
        showItemId(json.value);
      }else{
        if(json.value.includes('Failed') || json.value.includes('misslyckades')){
          showFailedModal(json.value);
        }else{
          showSuccessModal(json.value);
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

document.getElementById("check-out").addEventListener("click", function () {
  setUpStepper();
  Quagga.init({
      inputStream: {
          name: "Live",
          type: "LiveStream",
          constraints: {
            height: 200,
          },
          target: document.querySelector('#camera')
      },
      decoder: {
         readers: ["code_128_reader",
          "ean_reader",
          "ean_8_reader",
          "code_39_reader",
          "code_39_vin_reader",
          "codabar_reader",
          "upc_reader",
          "upc_e_reader",
          "i2of5_reader",
          "2of5_reader",
          "code_93_reader",
          "code_32_reader"]
      }
  }, function (err) {
      if (err) {
          console.log(` Quagaa init error ${err}`);
          return
      }
      console.log("Initialization finished. Ready to start");
      Quagga.start();
  });


  Quagga.onDetected(function (data) {
    var found= false;
    for (var i = 0; i < patrons.length; i++) {
      var patron_id=  patrons[i].item_id;
      var current_item_id = data.codeResult.code;

      if(patron_id.localeCompare(current_item_id)=== 0){
        patron_text.innerHTML= 'Patron: '+ patrons[i].name+' id: '+data.codeResult.code;
        use_patron_text.innerHTML= 'Patron: '+ patrons[i].name+'<br> id: '+data.codeResult.code;
        found= true;
      break;
      }
    }
    if(!found){
      patron_text.innerHTML= 'Patron: '+ patrons[2].name+'<br> id: '+data.codeResult.code;
      use_patron_text.innerHTML= 'Patron: '+ patrons[2].name+'<br> id: '+data.codeResult.code;
    }
  });
 
});

document.getElementById("use-patron-btn").addEventListener("click", function () {
  Quagga.stop()
});


function setUpStepper(){
  var stepper = document.querySelector('.stepper');
  var stepperInstace = new MStepper(stepper, {
     firstActive: 0,
     linearStepsNavigation: true,
     autoFocusInput: false,
     showFeedbackPreloader: true,
     validationFunction: validationPatronFunction,
     autoFormCreation: true,
     stepTitleNavigation: true,
  })
}

function validationPatronFunction() {
  var patron= use_patron_text.innerText;
  if(patron.localeCompare("")== 0 || 
  patron.localeCompare("scan a patron to continue check out")== 0){
    use_patron_text.innerHTML="scan a patron to continue check out";
    return false;
  }
  return true;
}
