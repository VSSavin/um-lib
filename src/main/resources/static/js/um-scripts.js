getKey = function() {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", '/um/security/key', false);
    xmlHttp.send(null);
    return xmlHttp.responseText;
};