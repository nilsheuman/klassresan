function exportToSvg() {
  const dot = document.getElementById("dot").textContent;
  const viz = new Viz();
  viz.renderSVGElement(dot).then((svg) => {
    const svgData = new XMLSerializer().serializeToString(svg);
    const blob = new Blob([svgData], { type: "image/svg+xml" });
    const url = URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = "graph.svg";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  });
}

function exportToPng() {
  const dot = document.getElementById("dot").textContent;
  const viz = new Viz();

  viz
    .renderImageElement(dot)
    .then((img) => {
      return new Promise((resolve, reject) => {
        if (img.complete) {
          return resolve(img);
        }
        img.onload = () => resolve(img);
        img.onerror = (err) => reject(err);
      });
    })
    .then((img) => {
      const canvas = document.createElement("canvas");
      const ctx = canvas.getContext("2d");

      const width = img.naturalWidth || img.width;
      const height = img.naturalHeight || img.height;
      canvas.width = width;
      canvas.height = height;

      ctx.drawImage(img, 0, 0, width, height);

      const pngData = canvas.toDataURL("image/png");

      const a = document.createElement("a");
      a.href = pngData;
      a.download = "graph.png";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    })
    .catch((error) => {
      console.error("Error exporting PNG:", error);
      alert("Failed to export PNG: " + error.message);
    });
}

function exportToDot() {
  const dot = document.getElementById("dot").textContent;
  const blob = new Blob([dot], { type: "text/plain" });
  const url = URL.createObjectURL(blob);

  const a = document.createElement("a");
  a.href = url;
  a.download = "graph.dot";
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

// excalidraw
const SCALE = 2; // adjust for spacing

async function exportToExcalidraw() {
  const viz = new Viz();
  const dot = document.getElementById("dot").textContent;
  const svg = await viz.renderSVGElement(dot);
  const nodePositions = extractNodePositions(svg);
  const edges = extractEdges(svg);
  
  // Merge positions into global nodes
  for (const [id, pos] of nodePositions.entries()) {
    const frame = nodes.get(id);
    if (frame) Object.assign(frame, pos);
  }
  
  const excalidraw = toExcalidraw(edges);
  const blob = new Blob([JSON.stringify(excalidraw, null, 2)], {
    type: "application/json",
  });
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = "graph.excalidraw";
  a.click();
}

function rectColor(frame) {
  return frame.clazz?.includes("Resource")
        ? "#dddddd"
        : "transparent"
}

// ---- Convert nodes to Excalidraw elements ----
function toExcalidraw(edges) {
  const elements = [];
  const nodeMap = new Map();
  let index = 0;

  // ---- Rectangles + text ----
  for (const [id, frame] of nodes.entries()) {
    const rectId = crypto.randomUUID();
    const textId = crypto.randomUUID();

    // Rectangle
    const rect = {
      id: rectId,
      type: "rectangle",
      x: frame.x - frame.w / 2,
      // y: frame.y - frame.h / 2 + frame.h / 4,
      y: frame.y - frame.h / 2,
      width: frame.w,
      // height: frame.h / 2,
      height: frame.h,
      strokeColor: frame.color || "#1e1e1e",
      backgroundColor: rectColor(frame),
      fillStyle: "solid",
      strokeWidth: 2,
      roughness: 1,
      opacity: 100,
      groupIds: [],
      frameId: null,
      index: `a${index++}`,
      seed: Math.floor(Math.random() * 1e9),
      version: 1,
      versionNonce: Math.floor(Math.random() * 1e9),
      isDeleted: false,
      boundElements: [{ type: "text", id: textId }],
      updated: Date.now(),
      link: null,
      locked: false,
    };

    // Text
    const text = {
      id: textId,
      type: "text",
      x: frame.x - frame.w / 4,
      y: frame.y - 12,
      width: frame.w / 2,
      height: 24,
      text: frame.method || frame.id,
      fontSize: 14,
      // fontSize: 20, // medium
      fontFamily: 8, // code
      textAlign: "center",
      verticalAlign: "middle",
      containerId: rectId,
      strokeColor: "#1e1e1e",
      backgroundColor: "transparent",
      fillStyle: "solid",
      strokeWidth: 1,
      roughness: 1,
      opacity: 100,
      groupIds: [],
      frameId: null,
      index: `a${index++}`,
      seed: Math.floor(Math.random() * 1e9),
      version: 1,
      versionNonce: Math.floor(Math.random() * 1e9),
      isDeleted: false,
      boundElements: null,
      updated: Date.now(),
      link: null,
      locked: false,
    };

    nodeMap.set(id, { rectId });
    elements.push(rect, text);
  }

  // ---- Cluster labels as text elements ----
  for (const [clusterId, methods] of clusters.entries()) {
    const clusterName = clusterId.split(":")[1];
    if (!clusterName) continue;
    
    // Find the first node in the cluster to position the label
    let firstNode = null;
    for (const method of methods) {
      if (nodes.has(method)) {
        firstNode = nodes.get(method);
        break;
      }
    }
    
    if (firstNode) {
      const textId = crypto.randomUUID();
      const clusterLabel = {
        id: textId,
        type: "text",
        x: firstNode.x - 100,
        y: firstNode.y - 50,
        width: 200,
        height: 30,
        text: clusterName,
        fontSize: 16,
        fontFamily: 8, // code
        textAlign: "center",
        verticalAlign: "middle",
        strokeColor: "#1e1e1e",
        backgroundColor: "transparent",
        fillStyle: "solid",
        strokeWidth: 1,
        roughness: 1,
        opacity: 100,
        groupIds: [],
        frameId: null,
        index: `a${index++}`,
        seed: Math.floor(Math.random() * 1e9),
        version: 1,
        versionNonce: Math.floor(Math.random() * 1e9),
        isDeleted: false,
        boundElements: null,
        updated: Date.now(),
        link: null,
        locked: false,
      };
      
      elements.push(clusterLabel);
    }
  }

  // ---- Arrows ----
  for (const edge of edges) {
    const src = nodeMap.get(edge.from);
    const dst = nodeMap.get(edge.to);
    if (!src || !dst) continue;

    const srcFrame = nodes.get(edge.from);
    const dstFrame = nodes.get(edge.to);
    
    // Calculate edge intersection points
    const getEdgePoint = (fromX, fromY, fromW, fromH, toX, toY) => {
      const dx = toX - fromX;
      const dy = toY - fromY;
      const halfW = fromW / 2;
      const halfH = fromH / 2;
      
      // Find which edge the line intersects
      if (Math.abs(dx) * halfH > Math.abs(dy) * halfW) {
        // Intersects left or right edge
        const sign = dx > 0 ? 1 : -1;
        return {
          x: fromX + sign * halfW,
          y: fromY + (dy * halfW) / Math.abs(dx)
        };
      } else {
        // Intersects top or bottom edge
        const sign = dy > 0 ? 1 : -1;
        return {
          x: fromX + (dx * halfH) / Math.abs(dy),
          y: fromY + sign * halfH
        };
      }
    };
    
    const start = getEdgePoint(srcFrame.x, srcFrame.y, srcFrame.w, srcFrame.h, dstFrame.x, dstFrame.y);
    const end = getEdgePoint(dstFrame.x, dstFrame.y, dstFrame.w, dstFrame.h, srcFrame.x, srcFrame.y);
    
    const width = Math.abs(end.x - start.x);
    const height = Math.abs(end.y - start.y);
    const minX = Math.min(start.x, end.x);
    const minY = Math.min(start.y, end.y);

    const arrowId = crypto.randomUUID();
    const arrowElement = {
      id: arrowId,
      type: "arrow",
      x: minX,
      y: minY,
      width: width,
      height: height,
      points: [[start.x - minX, start.y - minY], [end.x - minX, end.y - minY]],
      strokeColor: "#1e1e1e",
      strokeWidth: 2,
      roughness: 1,
      opacity: 100,
      startBinding: {
        elementId: src.rectId,
        focus: 0,
        gap: 1,
      },
      endBinding: {
        elementId: dst.rectId,
        focus: 0,
        gap: 1,
      },
      endArrowhead: "arrow",
      groupIds: [],
      frameId: null,
      index: `a${index++}`,
      seed: Math.floor(Math.random() * 1e9),
      version: 1,
      versionNonce: Math.floor(Math.random() * 1e9),
      isDeleted: false,
      updated: Date.now(),
      link: null,
      locked: false,
    };

    // Bind arrows to nodes
    const srcNode = elements.find((el) => el.id === src.rectId);
    const dstNode = elements.find((el) => el.id === dst.rectId);
    if (srcNode) srcNode.boundElements.push({ type: "arrow", id: arrowId });
    if (dstNode) dstNode.boundElements.push({ type: "arrow", id: arrowId });

    elements.push(arrowElement);
  }

  return {
    type: "excalidraw",
    version: 2,
    source: "graphviz-export",
    elements,
    appState: {
      gridSize: 20,
      gridStep: 5,
      gridModeEnabled: false,
      viewBackgroundColor: "#ffffff",
    },
    files: {},
  };
}

// ---- Extract node positions from SVG ----
function extractNodePositions(svg) {
  const map = new Map();

  svg.querySelectorAll("g.node").forEach((g) => {
    const title = g.querySelector("title")?.textContent;
    if (!title) return;

    const ellipse = g.querySelector("ellipse");
    const polygon = g.querySelector("polygon");
    const rect = g.querySelector("rect");

    let x, y, w, h;

    if (ellipse) {
      x = parseFloat(ellipse.getAttribute("cx"));
      y = parseFloat(ellipse.getAttribute("cy"));
      w = parseFloat(ellipse.getAttribute("rx")) * 2;
      // h = parseFloat(ellipse.getAttribute("ry")) * 2;
      h = parseFloat(ellipse.getAttribute("ry")) * 2 / 2; // scale height to half
    } else if (rect) {
      x = parseFloat(rect.getAttribute("x")) + parseFloat(rect.getAttribute("width")) / 2;
      y = parseFloat(rect.getAttribute("y")) + parseFloat(rect.getAttribute("height")) / 2;
      w = parseFloat(rect.getAttribute("width"));
      // h = parseFloat(rect.getAttribute("height"));
      h = parseFloat(rect.getAttribute("height")) / 2;  // scale height to half
    } else if (polygon) {
      const pts = polygon
        .getAttribute("points")
        .trim()
        .split(" ")
        .map((p) => p.split(",").map(Number));
      const xs = pts.map((p) => p[0]);
      const ys = pts.map((p) => p[1]);
      x = (Math.min(...xs) + Math.max(...xs)) / 2;
      y = (Math.min(...ys) + Math.max(...ys)) / 2;
      w = Math.max(...xs) - Math.min(...xs);
      // h = Math.max(...ys) - Math.min(...ys);
      h = (Math.max(...ys) - Math.min(...ys)) / 2; // scale height to half
    }

    if (x != null && y != null) {
      map.set(title, {
        x: x * SCALE,
        y: y * SCALE,
        w: w * SCALE,
        h: h * SCALE,
      });
    }
  });

  return map;
}

// ---- Extract edges from SVG ----
function extractEdges(svg) {
  const edges = [];

  svg.querySelectorAll("g.edge, g[id^='edge']").forEach((g) => {
    const titleText = g.querySelector("title")?.textContent;
    if (!titleText) return;

    const [fromRaw, toRaw] = titleText.split("->").map((s) => s.trim());
    if (!fromRaw || !toRaw) return;

    const from = fromRaw.replace(/^"+|"+$/g, "");
    const to = toRaw.replace(/^"+|"+$/g, "");

    edges.push({ from, to });
  });

  return edges;
}