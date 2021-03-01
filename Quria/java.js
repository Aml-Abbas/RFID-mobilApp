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
 //   createLink(itemId);
}; 
function openAppen(){
    var itemId = document.getElementById("item-id-input").value;

    location.href = "androidrfid://primaryid?itemid=" + itemId;

}



/*  function createLink(itemId) {
    androiduri = "androidrfid://primaryid?itemid=" + itemId;
    var aTag = document.getElementById("link");
    aTag.setAttribute("href", androiduri);
} 

function writeToTag() {
    var itemId = document.getElementById("item-id-input").value;
    var itemIdP = document.getElementById("item-id");
    itemIdP.textContent = `item id: ` + itemId;
    createLink(itemId);
} */
