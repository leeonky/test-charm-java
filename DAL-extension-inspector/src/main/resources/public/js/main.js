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

$(document).ready(function() {
    var debounceTimer = null;
    $('#code').on('input', function() {
        clearTimeout(debounceTimer);
        status('Waiting for input...');
        debounceTimer = setTimeout(function() {
            execute()
        }, 500);
    });

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
});