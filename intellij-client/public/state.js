// Application state
let ws;
let nodes = new Map();
let edges = new Set();
let clusters = new Map();
let activeDebuggerNode = null;
let activeEditorNode = null;
let activeHierarchyNode = null;
let currentMode = "open"; // open, edit, draw

let settings = {
  websocketUrl: "",
  intellijServerUrl: "",
  ignoredClasses: [],
  ignoredProjects: [],
};

const defaultWebsocketUrl = "ws://localhost:8091";
const defaultIntellijUrl = "http://localhost:8093";

// View management
const viewIds = ["settings", "edit", "export"];

function toggleView(toggleId) {
  viewIds.forEach((id) => {
    const el = document.getElementById(id);
    const toggleEl = document.getElementById(id + "Toggle");
    if (id === toggleId) {
      const show = el.style.display === "none";
      if (show) {
        el.style.display = "block";
        toggleEl.classList.add('active');
      } else {
        el.style.display = "none";
        toggleEl.classList.remove('active');
      }
    } else {
      el.style.display = "none";
      toggleEl.classList.remove('active');
    }
  });
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

// Helper functions
function getClusterId(frame) {
  return `${frame.pkg}:${frame.clazz}`;
}

function getKey(frame) {
  return `${frame.pkg}.${frame.clazz}:${frame.method}`.trim();
}

function getEdgeKey(frame0, frame1) {
  return `${getKey(frame1)}->${getKey(frame0)}`.trim();
}

function sanitize(str) {
  return str.replace(/[^a-zA-Z0-9_]/g, "_");
}
