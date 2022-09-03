getKey = function() {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", '/secure/key', false);
    xmlHttp.send(null);
    return xmlHttp.responseText;
};