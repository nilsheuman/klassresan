// WebSocket connection management
function connect() {
  const url = settings.websocketUrl
    ? settings.websocketUrl
    : defaultWebsocketUrl;
  ws = new WebSocket(`${url}`);
  ws.onopen = () => setStatus(true);
  ws.onclose = () => {
    setStatus(false);
    setTimeout(connect, 2000);
  };
  ws.onmessage = (event) => {
    try {
      handleMessage(JSON.parse(event.data));
    } catch (err) {
      console.error("Invalid message", err);
    }
  };
}

function setStatus(ok) {
  document.getElementById("status").className = ok
    ? "connected"
    : "disconnected";
}

function handleMessage(trace) {
  if (!Array.isArray(trace) || trace.length === 0) return;
  for (const frame of trace) {
    if (
      settings.ignoredProjects.some(
        (pkg) => frame.pkg && frame.pkg.startsWith(pkg)
      )
    )
      return;
  }
  const validTrace = trace.filter(
    (node) =>
      !settings.ignoredClasses.includes(node.clazz) &&
      node.method !== "unknown_method" &&
      node.line !== -1
  );
  if (validTrace.length === 0) return;

  const topFrame = validTrace[0];
  switch (topFrame.source) {
    case "debugger":
      activeDebuggerNode = getKey(topFrame);
      break;
    case "hierarchy":
      activeHierarchyNode = getKey(topFrame);
      break;
    case "editor":
      activeEditorNode = getKey(topFrame);
      break;
  }

  for (let i = 0; i < validTrace.length; i++) {
    const frame = validTrace[i];
    const id = getClusterId(frame);
    const key = getKey(frame);
    if (i > 0) edges.add(getEdgeKey(validTrace[i - 1], validTrace[i]));
    if (!nodes.has(key)) nodes.set(key, frame);
    if (!clusters.has(id)) clusters.set(id, []);
    if (!clusters.get(id).includes(key)) clusters.get(id).push(key);
  }
  renderGraph();
}