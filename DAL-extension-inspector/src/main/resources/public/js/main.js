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

class WSSession {
    constructor(path, handler) {
        const setupWebSocket = () => {
            this.socket = new WebSocket((window.location.protocol === "https:" ? "wss:" : "ws:")
                + "//" + window.location.host + path);
            this.socket.onopen = () => console.log("WebSocket connection established");

            this.socket.onerror = err => {
                console.error("WebSocket encountered an error");
                this.socket.close()
            };

            this.socket.onclose = event => {
                console.log("WebSocket closed")
                setTimeout(() => {
                    console.log("Re-setup WebSocket connection")
                    setupWebSocket()
                }, 500);
            };

            this.socket.onmessage = event => {
                handler(xmlToJson(event.data))
            };
        }
        setupWebSocket()
    }
}

const dalInstance = (name) => {
    return {
        result: {
            root: '',
            error: '',
            result: '',
            inspect: ''
        },
        active: 'root',
        code: '',
        name: name
    }
}

const appData = () => {
    return {
        session: '',
        dalInstanceNames: [],
        dalInstances: [dalInstance('Try It!')],
        activeInstance: null,
        exchangeSession: null,
        outputTabs: ['root', 'result', 'error', 'inspect'],
        async handleExchange(message) {
            if (message.instances) {
//            TODO refactor
                message.instances.filter(e => !this.dalInstanceNames.find(i => i.name === e))
                  .forEach(e => this.dalInstanceNames.push({name: e, active: true}))
                await this.exchange()
            }
            if (message.request) {
//            TODO refactor
                const dalInstanceName = this.dalInstanceNames.find(e => e.name === message.request)
                if(dalInstanceName && dalInstanceName.active)
                    await this.request(message.request)
            }
            if(message.session)
                this.session = message.session
        },
        async updateResult(result) {
            const response = await fetch('/api/execute?name=' + result.name, {
                method: 'POST',
                body: result.code
            })
            result.result = xmlToJson(await response.text())
//            TODO use ref switchtab
            result.active = result.result.error ? 'error' : (result.result.result ? 'result' : 'root');
        },
        async request(dalName) {
            const response = await fetch('/api/request?name=' + dalName, { method: 'GET' })
            const code = await response.text()
            if(code && code !== '') {
                let newDalInstance = dalInstance(dalName);
                newDalInstance.code = code
                this.dalInstances.splice(this.dalInstances.length - 1, 0, newDalInstance)
//                TODO should use ref
                this.$nextTick(() => Array.from(document.querySelectorAll('.code-editor'))
                        .filter(editor => editor.getAttribute('name') === dalName)
                        .forEach(editor => {
                            editor.dispatchEvent(new Event('code-update'))
                            this.activeInstance = dalName
                        }))
            }
        },
        async execute(dalName, code) {
            const response = await fetch('/api/execute?name=' + dalName, {
                method: 'POST',
                body: code
            })
            return xmlToJson(await response.text())
        },
        async exchange(dalName) {
//            TODO refactor
            const dalInstanceName = this.dalInstanceNames.filter(e => e.name===dalName);
            if(dalInstanceName) {
                if(!dalInstanceName.active)
                    this.release(dalName)
            }
            if(this.session)
              return await fetch('/api/exchange?session=' + this.session, {
                  method: 'POST',
                  body: this.dalInstanceNames.filter(e => e.active).map(e => e.name).join('\n')
              })
        },
        async release(dalName) {
            const response = await fetch('/api/release?name=' + dalName, { method: 'POST' })
        },
        async releaseAll() {
            const response = await fetch('/api/release-all', { method: 'POST' })
        },
        init() {
            this.exchangeSession = new WSSession('/ws/exchange', this.handleExchange.bind(this))

//            TODO use ref switchtab
            this.$nextTick(() => this.activeInstance = 'Try It!')
        }
    }
};

const tab = () => {
    return {
        nodeContainer(selectors) {
            return this.$root.querySelector(selectors)
        },

        init() {
            const observer = new MutationObserver((mutations) => mutations.forEach((mutation) =>
                  mutation.addedNodes.forEach(node => {
                      if (node.classList.contains('tab-header'))
                          node.addEventListener('click', () => this.switchTab(node.getAttribute('target')));
                  })));

            observer.observe(this.nodeContainer('.tab-headers'), {childList: true});

            Array.from(this.nodeContainer('.tab-headers').children).forEach(header =>
                header.addEventListener('click', () => this.switchTab(header.getAttribute('target'))));
        },

        switchTab(tab) {
            const toggleActive = (container) => Array.from(container.children).forEach(content =>
                content.classList.toggle('active', content.getAttribute('target') === tab));
            toggleActive(this.nodeContainer('.tab-headers'))
            toggleActive(this.nodeContainer('.tab-contents'))
        }
    };
}

const codeEditor = () => {
    return {
        init() {
            let debounceTimer = null
            this.$el.addEventListener('input', () => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(() => this.$el.dispatchEvent(new CustomEvent('code-update')), 500);
                this.$el.style.height = 'auto';
                if (this.$el.scrollHeight > this.$el.clientHeight) {
                    this.$el.style.height = this.$el.scrollHeight + 'px';
                }
            })
        }
    }
}