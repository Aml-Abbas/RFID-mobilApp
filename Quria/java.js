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

var ws;

    $("#connect").click(function(e)
    {

            var ip = $("#address").val();
            ws = new WebSocket("ws://" + ip);
            ws.onopen = function()
            {
                 alert("connected!");
            };

    });