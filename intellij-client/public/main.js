let ws;
let nodes = new Map();
let edges = new Set();
let clusters = new Map();
let activeDebuggerNode = null;
let activeEditorNode = null;
let activeHierarchyNode = null;

let settings = {
  serverUrl: "",
  intellijServerUrl: "",
  ignoredClasses: [],
  ignoredProjects: []
};

const defaultWebsocketUrl = "ws://localhost:8091"
const defaultIntellijUrl = "http://localhost:8093"

function loadSettings() {
  const raw = localStorage.getItem("klassresanSettings");
  if (raw) {
    try { settings = JSON.parse(raw); } 
    catch (e) { console.error("Failed to parse settings", e); }
  }
  document.getElementById("serverUrl").value = settings.websocketUrl || "";
  document.getElementById("intellijServerUrl").value = settings.intellijServerUrl || "";
  document.getElementById("ignoredClasses").value = (settings.ignoredClasses || []).join(", ");
  document.getElementById("ignoredProjects").value = (settings.ignoredProjects || []).join(", ");
}

function saveSettings() {
  settings.websocketUrl = document.getElementById("serverUrl").value.trim();
  settings.intellijServerUrl = document.getElementById("intellijServerUrl").value.trim();
  settings.ignoredClasses = document.getElementById("ignoredClasses").value.split(",").map(s => s.trim()).filter(Boolean);
  settings.ignoredProjects = document.getElementById("ignoredProjects").value.split(",").map(s => s.trim()).filter(Boolean);
  localStorage.setItem("klassresanSettings", JSON.stringify(settings));
  alert("Settings saved. Reload to reconnect.");
}

function toggleSettings() {
  const el = document.getElementById("settings");
  el.style.display = el.style.display === "none" ? "block" : "none";
}
function toggleEdit() {
  const el = document.getElementById("edit");
  el.style.display = el.style.display === "none" ? "block" : "none";
}
function toggleExport() {
  const el = document.getElementById("export");
  el.style.display = el.style.display === "none" ? "block" : "none";
}

function connect() {
  const url = settings.websocketUrl ? settings.websocketUrl : defaultWebsocketUrl;
  ws = new WebSocket(`${url}`);
  ws.onopen = () => setStatus(true);
  ws.onclose = () => { setStatus(false); setTimeout(connect, 2000); };
  ws.onmessage = (event) => {
    try { handleMessage(JSON.parse(event.data)); }
    catch (err) { console.error("Invalid message", err); }
  };
}

function setStatus(ok) {
  document.getElementById("status").className = ok ? "connected" : "disconnected";
}

function clearGraph() {
  nodes = new Map();
  edges = new Set();
  clusters = new Map();
  activeDebuggerNode = null;
  activeHierarchyNode = null;
  activeEditorNode = null;
  renderGraph();
}

function getClusterId(frame) { return `${frame.pkg}:${frame.clazz}`; }
function getKey(frame) { return `${frame.pkg}.${frame.clazz}:${frame.method}`; }
function getEdgeKey(frame0, frame1) { return `${getKey(frame1)}->${getKey(frame0)}`; }

function handleMessage(trace) {
  if (!Array.isArray(trace) || trace.length === 0) return;
  for (const frame of trace) {
    if (settings.ignoredProjects.some(pkg => frame.pkg && frame.pkg.startsWith(pkg))) return;
  }
  const validTrace = trace.filter(node =>
    !settings.ignoredClasses.includes(node.clazz) &&
    node.method !== 'unknown_method' &&
    node.line !== -1
  );
  if (validTrace.length === 0) return;

  const topFrame = validTrace[0];
  switch (topFrame.source) {
    case "debugger": activeDebuggerNode = getKey(topFrame); break;
    case "hierarchy": activeHierarchyNode = getKey(topFrame); break;
    case "editor": activeEditorNode = getKey(topFrame); break;
  }

  for (let i = 0; i < validTrace.length; i++) {
    const frame = validTrace[i];
    const id = getClusterId(frame);
    const key = getKey(frame);
    if (i > 0) edges.add(getEdgeKey(validTrace[i-1], validTrace[i]));
    if (!nodes.has(key)) nodes.set(key, frame);
    if (!clusters.has(id)) clusters.set(id, []);
    if (!clusters.get(id).includes(key)) clusters.get(id).push(key);
  }
  renderGraph();
}

function sanitize(str) { return str.replace(/[^a-zA-Z0-9_]/g, "_"); }



function openEditor(nodeKey) {
  const frame = nodes.get(nodeKey);
  if (!frame) return;

  const baseUrl = settings.intellijServerUrl || defaultIntellijUrl
  
  let url;
  if (frame.pkg) {
    url = `${baseUrl}/open?fq=${frame.pkg}.${frame.clazz}&line=${frame.line}`;
  } else {
    url = `${baseUrl}/open?path=${frame.clazz}&line=${frame.line}`;
  }
  
  const xhr = new XMLHttpRequest();
  xhr.open('GET', url, true);
  xhr.send();
}

function renderGraph() {
  let dot = 'digraph G {\n  rankdir=LR;\n  node [shape=ellipse];\n\n';
  dot+='__caller [style=invis]\n';

  // resources forced out to edges with invisible caller, not really working
  for (const [key, frame] of nodes.entries()) {
    if (frame.clazz.includes('Resource')) {
      dot+= `__caller -> "${key}" [color=\"#cccccc\" penwidth=0]\n`;
    }
  }

  // clusters
  for (const [clusterId, methods] of clusters.entries()) {
    let activeClusterEditor = false;
    let activeClusterHierarchy = false;
    for (const key of methods) {
      activeClusterEditor = activeClusterEditor || key === activeEditorNode;
      activeClusterHierarchy = activeClusterHierarchy || key === activeHierarchyNode;
    }

    const clusterIdTag = `cluster_${sanitize(clusterId)}`;
    const clusterName = clusterId.split(':')[1];
    dot += `  subgraph ${clusterIdTag} {\n    label="${clusterName}";\n`;

    if (activeClusterEditor) {
      dot+="style=filled; color=\"#ff0000\"; fillcolor=\"#ffdddd\"; penwidth=2;\n";
    } else if (activeClusterHierarchy) {
      dot+="style=filled; color=\"#00aa00\"; fillcolor=\"#eeffee\"; penwidth=2;\n";
    }
    
    dot+='    { rank=same\n      edge [constraint=false];\n';
    for (const key of methods) {
      const frame = nodes.get(key);
      const activeDebugger = key === activeDebuggerNode;
      const activeEditor = key === activeEditorNode;
      const isResource = frame.clazz.includes('Resource');

      const color = activeDebugger ? "color=blue" : activeEditor ? "color=red" : "";
      const fillcolor = activeDebugger ? "fillcolor=\"#00ccff\"" :
                        activeEditor ? "fillcolor=\"#ffcccc\"" :
                        isResource ? "fillcolor=\"#cccccc\"" : "";
      const penwidth = activeEditor && activeDebugger ? "penwidth=4" :
                       activeEditor || activeDebugger ? "penwidth=2" : "";
      const style = (activeDebugger || activeEditor || isResource) ? "style=filled" : "";
      const shape = isResource ? "shape=box" : "";

      dot += `      "${key}" [label="${frame.method}" ${color} ${penwidth} ${fillcolor} ${style} ${shape}];\n`;
    }
    dot += '    }\n  }\n\n';
  }

  // edges
  for (const edge of edges) {
    const [from, to] = edge.split("->");
    dot += `  "${from}" -> "${to}";\n`;
  }
  dot += '}';

  document.getElementById('dot').innerHTML = dot;
  try {
    const viz = new Viz();
    viz.renderSVGElement(dot).then(svg => {
      const graphDiv = document.getElementById("graph");
      graphDiv.innerHTML = "";
      graphDiv.appendChild(svg);

      const nodes = svg.querySelectorAll('g.node');
      nodes.forEach(node => {
        // console.log('node', node)
        const title = node.querySelector('title')?.textContent;
        if (title) {
          // console.log('title', title)
          node.style.cursor = 'pointer';
          node.addEventListener('click', () => openEditor(title));
        }
      });
    });
  } catch (e) { console.error("DOT build failed", e); }
}

loadSettings();
connect();