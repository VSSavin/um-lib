$(function() {
    $('.selectpicker').on('change', function(){
        var selected = []; //array to store value
        $(this).find("option:selected").each(function(key,value){
            selected.push(value.innerHTML); //push the text to array
        });

        var languagesJSON = $.getLanguages();
        var languagesMap = new Map(Object.entries(languagesJSON));
        var lang = $.urlParam('lang');
        var error = $.urlParam('error');

        var selectedString = $.getKeyByValue(languagesMap, selected.join());
        var windowHref = '//' + location.host + location.pathname;
        if (lang != null) {
            windowHref += "?lang=" + selectedString;
        }

        if (error) {
            if (lang != null) {
                windowHref += "&error=true";
            }
            else {
                windowHref += "?error=true";
            }
        }
        window.location.href = windowHref;
    });
});

$.getKeyByValue = function (map, val) {
    for(let [key, value] of map.entries()) {
        if (value === val) {
            return key;
        }
    }
}

$(document).ready(function() {
    var lang = $.urlParam('lang');
    var pickerValues = []; //array to store value

    $('.selectpicker').each(function() {
        pickerValues.push($(this).val());
    });

    var languagesJSON = $.getLanguages();
    var languagesMap = new Map(Object.entries(languagesJSON));

    var windowHref = '//' + location.host + location.pathname + location.search;

    if (lang == null) {
        var browserLang = navigator.language || navigator.userLanguage;
        browserLang = browserLang.substring(0,2);
        if(location.search.length == 0) {
            if (languagesMap.has(browserLang)) window.location.href = windowHref + "?lang=" + browserLang;
            else {
                window.location.href = windowHref + "?lang=" + languagesMap.keys().next().value;
            }
        }
        else {
            if (languagesMap.has(browserLang)) window.location.href = windowHref + "&lang=" + browserLang;
            else {
                window.location.href = windowHref + "&lang=" + languagesMap.keys().next().value;
            }
        }
    }
    else {
        $('.selectpicker').selectpicker('val',languagesMap.get(lang));
    }
});

$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results==null) {
        return null;
    }
    return decodeURI(results[1]) || 0;
}

$.getLanguage = function(lang){
    if(lang == "ru") return "Русский";
    else if(lang == "en") return "English";
}

$.langToLocale = function(lang){
    if (lang.includes("Рус")) return "ru";
    else if (lang.includes("En")) return "en";
}

$.getLanguages = function() {
    var retval = {};
    $.ajax({
        url : '/um/languages',
        type: 'GET',
        async: false,
        dataType: 'json'

    }).done(function(response){
        retval = response;
    }).fail(function(jqXHR, textStatus, errorThrown) {
        retval = null;
    });

    return retval;
};