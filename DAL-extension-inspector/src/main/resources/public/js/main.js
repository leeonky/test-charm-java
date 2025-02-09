function status(s) {
    $('#status').text(s);
}

function execute() {
    $.ajax({
        url: '/api/execute',
        type: 'POST',
        data: getCode(),
        success: function(response) {
            setResult(response)
        },
        error: function() {
        }
    });
}

function setCode(code) {
    $('#code').val(code)
}

function getCode() {
    return $('#code').val()
}

function setResult(result) {
    $('#result').text(result)
}

function switchToTab(tabId) {
    $('.tab').removeClass('active');
    $('[data-tab="'+tabId+'"]').addClass('active')
    $('.tab-content').removeClass('active');
    $('#' + tabId).addClass('active');
}

function setupEditor() {
    var debounceTimer = null;
    $('#code').on('input', function() {
        clearTimeout(debounceTimer);
        status('Waiting for input...');
        debounceTimer = setTimeout(function() {
            execute()
        }, 500);
    });
}

function createWS(uri) {
    socket = new WebSocket(uri);
    socket.onopen = function() {
        console.log("WebSocket connection established");
    };

    socket.onerror = function(err) {
        console.error("WebSocket encountered an error");
        socket.close()
    };

    socket.onclose = function(event) {
        console.log("WebSocket closed")
        setTimeout(function() {
            console.log("Re-setup WebSocket connection")
            createWS(uri)
        }, 500);
    };
}

function setupWS() {
    const loc = window.location;
    let newUri;
    if (loc.protocol === "https:")
        newUri = "wss:";
    else
        newUri = "ws:";
    newUri += "//" + loc.host+"/ws/ping";
    createWS(newUri)
}

var socket = null

$(document).ready(function() {
    setupEditor()

    setupWS()

    $.ajax({
        url: '/api/fetch-code',
        type: 'GET',
        success: function(response) {
            setCode(response)
            execute()
        },
        error: function() {
        }
    });

    switchToTab("tab-result")

    $('.tab').on('click', function() {
        switchToTab($(this).data('tab'))
    });
});