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
    constructor(path, handler, clearHandler) {
        const setupWebSocket = () => {
            this.socket = new WebSocket((window.location.protocol === "https:" ? "wss:" : "ws:")
                + "//" + window.location.host + path);
            this.socket.onopen = () => console.log("WebSocket connection established");
            this.socket.onerror = err => {
                this.socket.close()
            };
            this.socket.onclose = event => {
                if (clearHandler)
                    clearHandler()
                setTimeout(() => {
                    setupWebSocket()
                }, 50);
            };
            this.socket.onmessage = event => {
                handler(xmlToJson(event.data))
            };
        }
        setupWebSocket()
    }
}

const Workspace = (code, name) => {
    return Alpine.reactive({
        result: {
            root: '',
            error: '',
            result: '',
            inspect: ''
        },
        active: '',
        code: code,
        closable: false,
        setResult(result) {
            this.result = result;
            this.active = result.error ? 'error' : (result.result ? 'result' : 'root');
        },
        __executing: false,
        async run() {
            this.__executing = true
            const response = await fetch('/api/execute?name=' + name, {
                method: 'POST',
                body: this.code
            })
            if (response.ok) {
                this.__executing = false
                this.setResult(xmlToJson(await response.text()))
            }
        },
        editorStatus(connected) {
            if (this.__executing)
                return "executing"
            if (this.name != "Try It!" && !connected)
                return "disconnected"
            return this.active
        },
    })
}

const DalInstance = (name) => {
    return {
        workspaces: [Workspace('', name)],
        activeWorkspace: null,
        name: name,
        connected: true,
        async attach(code) {
            this.workspaces[0].code = code
            this.connected = true
            await this.workspaces[0].run()
        },
        syncWorkspaceIndex(workspace) {
          this.activeWorkspace = this.workspaces.indexOf(workspace)
        },
        duplicateWorkspace(workspace) {
          this.syncWorkspaceIndex(workspace)
          const newWorkspace = Workspace(workspace.code, name)
          newWorkspace.closable = true
          this.workspaces.splice(this.activeWorkspace + 1, 0, newWorkspace)
          this.activeWorkspace += 1
        },
        dismiss(workspace) {
          this.syncWorkspaceIndex(workspace)
          this.workspaces.splice(this.activeWorkspace, 1)
          this.activeWorkspace -= 1
        }
    }
}

const appData = () => {
    return {
        session: '',
        autoExecute: true,
        dalMonitorConfigs: [],
        dalInstances: [DalInstance('Try It!')],
        activeInstance: null,
        exchangeSession: null,
        outputTabs: ['root', 'result', 'error', 'inspect'],
        disconnectByName(dalName) {
            this.dalInstances.filter(e => e.name === dalName).forEach(dalInstance => dalInstance.connected = false)
        },
        async handleExchange(message) {
            if (message.session)
                this.session = message.session
            if (message.instances) {
//            TODO refactor
                message.instances.filter(e => !this.dalMonitorConfigs.find(i => i.name === e))
                    .forEach(e => this.dalMonitorConfigs.push({name: e, active: true}))
                await this.exchange()
            }
            if (message.request) {
//            TODO refactor
                const dalConfig = this.dalMonitorConfigs.find(e => e.name === message.request)
                if (dalConfig && dalConfig.active)
                    await this.request(message.request)
            }
        },
        async request(dalName) {
            const response = await fetch('/api/request?name=' + dalName, {method: 'GET'})
            const code = await response.text()
            if (code) {
                let target = this.dalInstances.find(e => e.name === dalName)
                if(!target) {
                    target = DalInstance(dalName);
                    this.dalInstances = this.dalInstances.filter(e => e.name !== dalName)
                    this.dalInstances.splice(this.dalInstances.length - 1, 0, target)
                }
                await target.attach(code)
                this.activeInstance = dalName
                this.activeWorkspace = 0
            }
        },
        async exchange(dalName) {
//            TODO refactor
            const dalConfig = this.dalMonitorConfigs.find(e => e.name === dalName);
            if (dalConfig && !dalConfig.active)
                    await this.release(dalName)
            if (this.session)
                return await fetch('/api/exchange?session=' + this.session, {
                    method: 'POST',
                    body: this.dalMonitorConfigs.filter(e => e.active).map(e => e.name).join('\n')
                })
        },
        async pass(dalName) {
            fetch('/api/pass?name=' + dalName, {method: 'POST'})
            this.disconnectByName(dalName)
        },
        async release(dalName) {
            fetch('/api/release?name=' + dalName, {method: 'POST'})
            this.disconnectByName(dalName)
        },
        async releaseAll() {
            fetch('/api/release-all', {method: 'POST'})
            this.clearStates();
        },
        init() {
            this.exchangeSession = new WSSession('/ws/exchange', this.handleExchange.bind(this), this.clearStates.bind(this))
            this.$nextTick(() => this.activeInstance = 'Try It!')
        },
        clearStates() {
            this.dalInstances.forEach(dalInstance => dalInstance.connected = false)
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
//            TODO refactor
                    if (node.classList.contains('tab-header'))
                        node.addEventListener('click', () => this.switchTab(node.getAttribute('target')));
                })));

            observer.observe(this.nodeContainer('.tab-headers'), {childList: true});

//            TODO refactor
            Array.from(this.nodeContainer('.tab-headers').children).forEach(header => {
                if (header.classList.contains('tab-header'))
                    header.addEventListener('click', () => this.switchTab(header.getAttribute('target')))
            });
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
        adjustHeight() {
            this.$el.style.height = 'auto'
            if (this.$el.scrollHeight > this.$el.clientHeight)
                this.$el.style.height = this.$el.scrollHeight + 'px'
        },

        init() {
            let debounceTimer = null
            this.$el.addEventListener('input', () => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(() => this.$el.dispatchEvent(new CustomEvent('code-update')), 500);
                this.adjustHeight()
            })

            const modelKey = this.$el.getAttribute('x-model');
            if (modelKey)
                this.$watch(modelKey, () => this.adjustHeight());
        }
    }
}