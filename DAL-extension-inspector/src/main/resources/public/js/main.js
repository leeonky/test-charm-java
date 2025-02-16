function xmlToJson(xmlStr) {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlStr, "text/xml");
    const rootNode = xmlDoc.documentElement;

    function parseNode(nodes) {
      const children = Array.from(nodes);
      let obj
      children.forEach(child => {
        let childObj = parseNode(child.childNodes);
        if (child.nodeName === '__item') {
          obj = (obj || [])
          obj.push(childObj);
        } else if (child.nodeType === Node.TEXT_NODE) {
          obj = child.textContent.trim();
          return
        } else {
          obj = (obj || {})
          obj[child.nodeName] = childObj
        }
      });
      return obj
    }
    return parseNode(rootNode.childNodes)
}
/*
function execute() {
    statusExecuting()
    $.ajax({
        url: '/api/execute',
        type: 'POST',
        data: getCode(),
        success: function(response) {
            const data = xmlToJson(response)
            setRoot(data.root)
            setResult(data.result)
            const error = data.error
            setError(error)
            setInspect(data.inspect)

            if(error != "" && error != null)
                showError()
            else
                showResult()
        },
        error: function() {
        }
    });
}

function showError() {
    switchToTab("tab-error")
    statusError()
}

function showResult() {
    switchToTab("tab-result")
    statusSuccess()
}

function setCode(code) {
    $('#code').val(code)
}

function getCode() {
    return $('#code').val()
}

function setError(result) {
    $('#error').text(result)
}

function setResult(result) {
    $('#result').text(result)
}

function setInspect(result) {
    $('#inspect').text(result)
}

function setRoot(result) {
    $('#root').text(result)
}

function statusEditing() {
    $('#code').attr('class', 'code-editing');
}

function statusError() {
    $('#code').attr('class', 'code-error');
}

function statusSuccess() {
    $('#code').attr('class', 'code-success');
}

function statusExecuting() {
    $('#code').attr('class', 'code-executing');
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
        statusEditing();
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(function() {
            execute()
        }, 500);
        this.style.height = 'auto';
        if (this.scrollHeight > this.clientHeight) {
            this.style.height = this.scrollHeight + 'px';
        }
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

    socket.onmessage = function(event) {
        if(event.data === 'start')
            sync()
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

function getAllInstanceSwitches() {
    var $checkboxes = $('.dal-instance input[type="checkbox"]');
    var values = {};
    $checkboxes.each(function() {
      values[this.id] = this.checked;
    });
  console.log(values)
    return values;
}

function setInstances(names) {
  var instanceSwitches = getAllInstanceSwitches();
  var $container = $('.dal-instances-switches');
  console.log(names)
  $container.empty()
  $.each(names, function(index, name) {
      var check = (instanceSwitches[name] === undefined) || instanceSwitches[name]
      var $label = $('<label>', { 'class': 'dal-instance switch' });
      var $input = $('<input>', { type: 'checkbox', id: name, hidden: true, checked: check });
      var $customCheckbox = $('<span>', { 'class': 'custom-checkbox' });
      var $spanLabel = $('<span>').text(name);

      $label.append($input);
      $label.append($customCheckbox);
      $label.append($spanLabel);

      $container.append($label);
  });
}

function setCurrent(text) {
  $('#current').text(text)
}

function getCurrent() {
  var current = $('#current').text()
  return current
}

function needInspect() {
  var inspect = getAllInstanceSwitches()[getCurrent()]
  console.log('--------------------')
  console.log(inspect)
  if(inspect)
    console.log('inspect')
  else
    console.log('not-inspect')

  return inspect
}

function sync() {
    $.ajax({
        url: '/api/sync',
        type: 'GET',
        success: function(response) {
            const data = xmlToJson(response)
            setInstances(data.instances)
            setCode(data.code)
            setCurrent(data.current)
            if(needInspect())
              execute()
            else
              resume()
        },
        error: function() {
        }
    });
}

var socket = null

function resume() {
    $.ajax({
        url: '/api/resume',
        type: 'POST',
        success: function(response) {
          statusEditing();
        },
        error: function() {
        }
    });
}

$(document).ready(function() {
    setupEditor()
    setupWS()
    sync();

    $('.tab').on('click', function() {
        switchToTab($(this).data('tab'))
    });

    $('#resume').on('click', function() {
        resume()
    })
});
*/