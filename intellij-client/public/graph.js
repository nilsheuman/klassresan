// Graph rendering
function openEditor(nodeKey) {
  const frame = nodes.get(nodeKey);
  if (!frame) return;

  const baseUrl = settings.intellijServerUrl || defaultIntellijUrl;

  let url;
  if (frame.pkg) {
    url = `${baseUrl}/open?fq=${frame.pkg}.${frame.clazz}&line=${frame.line}`;
  } else {
    url = `${baseUrl}/open?path=${frame.clazz}&line=${frame.line}`;
  }

  const xhr = new XMLHttpRequest();
  xhr.open("GET", url, true);
  xhr.responseType = "text";
  xhr.send();
}

function renderGraph() {
  let dot = "digraph G {\n  rankdir=LR;\n  node [shape=ellipse];\n\n";
  dot += "__caller [style=invis]\n";

  // resources forced out to edges with invisible caller, not really working
  for (const [key, frame] of nodes.entries()) {
    if (frame.clazz.includes("Resource")) {
      dot += `__caller -> "${key}" [color=\"#cccccc\" penwidth=0]\n`;
    }
  }

  // clusters
  for (const [clusterId, methods] of clusters.entries()) {
    let activeClusterEditor = false;
    let activeClusterHierarchy = false;
    for (const key of methods) {
      activeClusterEditor = activeClusterEditor || key === activeEditorNode;
      activeClusterHierarchy =
        activeClusterHierarchy || key === activeHierarchyNode;
    }

    const clusterIdTag = `cluster_${sanitize(clusterId)}`;
    const clusterName = clusterId.split(":")[1];
    dot += `  subgraph ${clusterIdTag} {\n    label="${clusterName}";\n`;

    if (activeClusterEditor) {
      dot +=
        'style=filled; color="#ff0000"; fillcolor="#ffdddd"; penwidth=2;\n';
    } else if (activeClusterHierarchy) {
      dot +=
        'style=filled; color="#00aa00"; fillcolor="#eeffee"; penwidth=2;\n';
    }

    dot += "    { rank=same\n      edge [constraint=false];\n";
    for (const key of methods) {
      const frame = nodes.get(key);
      const activeDebugger = key === activeDebuggerNode;
      const activeEditor = key === activeEditorNode;
      const isResource = frame.clazz.includes("Resource");

      const color = activeDebugger
        ? "color=blue"
        : activeEditor
        ? "color=red"
        : "";
      const fillcolor = activeDebugger
        ? 'fillcolor="#00ccff"'
        : activeEditor
        ? 'fillcolor="#ffcccc"'
        : isResource
        ? 'fillcolor="#cccccc"'
        : "";
      const penwidth =
        activeEditor && activeDebugger
          ? "penwidth=4"
          : activeEditor || activeDebugger
          ? "penwidth=2"
          : "";
      const style =
        activeDebugger || activeEditor || isResource ? "style=filled" : "";
      const shape = isResource ? "shape=box" : "";

      dot += `      "${key}" [label="${frame.method}" ${color} ${penwidth} ${fillcolor} ${style} ${shape}];\n`;
    }
    dot += "    }\n  }\n\n";
  }

  // edges
  for (const edge of edges) {
    const [from, to] = edge.split("->");
    dot += `  "${from}" -> "${to}";\n`;
  }
  dot += "}";

  const editView = document.getElementById("edit");
  const dotTextarea = document.getElementById("dotTextarea");
  if (dotTextarea && !dotTextarea.dataset.locked) {
    dotTextarea.value = dot;
  }

  document.getElementById("dot").innerHTML = dot;
  try {
    const viz = new Viz();

    viz.renderSVGElement(dot).then((svg) => {
      const graphDiv = document.getElementById("graph");
      graphDiv.innerHTML = "";
      graphDiv.appendChild(svg);

      const svgNodes = svg.querySelectorAll("g.node");
      svgNodes.forEach((node) => {
        const title = node.querySelector("title")?.textContent;
        if (title) {
          node.style.cursor = "pointer";
          node.addEventListener("click", () => handleNodeClick(title));
        }
      });

      // Update canvas size to match graph
      if (drawingContext) {
        updateCanvasSize();
      }
    });
  } catch (e) {
    console.error("DOT build failed", e);
  }
}
