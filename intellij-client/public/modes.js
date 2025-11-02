// Mode handling (open, draw)
let drawingContext = null;
let isDrawing = false;
let lastX = 0;
let lastY = 0;
let currentColor = '#000000';
let drawingHistory = [];
let selectedNodeKey = null;
const lineWidth = 3

function setMode(mode) {
  // Toggle mode - if clicking the same mode, go back to 'open'
  if (currentMode === mode) {
    mode = 'open';
  }

  const removeNode = document.getElementById("removeNode");
  if (mode === 'edit') {
    removeNode.classList.remove('hidden');
  } else {
    removeNode.classList.add('hidden');
  }
  
  currentMode = mode;
  console.log('mode', mode)
  
  // Update button states
  document.querySelectorAll('.modeButton').forEach(btn => {
    btn.classList.remove('active');
  });
  
  if (mode !== 'open') {
    document.querySelector(`[data-mode="${mode}"]`)?.classList.add('active');
  }
  
  // Update UI based on mode
  const canvas = document.getElementById('drawCanvas');
  const drawColorPicker = document.getElementById('drawColorPicker');
  const clearDrawingBtn = document.getElementById('clearDrawing');
  const undoDrawBtn = document.getElementById('undoDraw');
  
  if (mode === 'draw') {
    canvas.classList.add('drawingActive');
    drawColorPicker.classList.remove('hidden');
    clearDrawingBtn.classList.remove('hidden');
    undoDrawBtn.classList.remove('hidden');
  } else {
    canvas.classList.remove('drawingActive');
    drawColorPicker.classList.add('hidden');
    clearDrawingBtn.classList.add('hidden');
    undoDrawBtn.classList.add('hidden');
  }
}

function handleNodeClick(nodeKey) {
  if (currentMode === 'draw') {
    return; // Do nothing in draw mode
  }
  
  // In open mode, show node info and open in editor
  showNodeInfo(nodeKey);
  if (currentMode === 'open') {
      openEditor(nodeKey);
  }
}

function showNodeInfo(nodeKey) {
  const frame = nodes.get(nodeKey);
  if (!frame) return;
  
  selectedNodeKey = nodeKey;
  const info = document.getElementById('selectedNodeInfo');
  const text = document.getElementById('selectedNodeText');
  
  text.textContent = `${frame.clazz}.${frame.method}`;
  info.classList.remove('hidden');
}

function removeNode() {
  if (!selectedNodeKey) return;
  
  // Add to filter
  addNodeToFilter(selectedNodeKey);
  
  // Remove from data structures
  nodes.delete(selectedNodeKey);
  
  // Remove from clusters
  for (const [clusterId, methods] of clusters.entries()) {
    const index = methods.indexOf(selectedNodeKey);
    if (index > -1) {
      methods.splice(index, 1);
      if (methods.length === 0) {
        clusters.delete(clusterId);
      }
    }
  }
  
  // Remove edges
  const edgesToRemove = [];
  for (const edge of edges) {
    if (edge.includes(selectedNodeKey)) {
      edgesToRemove.push(edge);
    }
  }
  edgesToRemove.forEach(edge => edges.delete(edge));
  
  // Hide info and re-render
  document.getElementById('selectedNodeInfo').classList.add('hidden');
  selectedNodeKey = null;
  renderGraph();
}

// Drawing functions
function initDrawing() {
  const canvas = document.getElementById('drawCanvas');
  if (!canvas) return;
  
  drawingContext = canvas.getContext('2d');
  drawingContext.lineCap = 'round';
  drawingContext.lineJoin = 'round';
  drawingContext.lineWidth = lineWidth;
  
  updateCanvasSize();
  
  canvas.addEventListener('mousedown', startDrawing);
  canvas.addEventListener('mousemove', draw);
  canvas.addEventListener('mouseup', stopDrawing);
  canvas.addEventListener('mouseout', stopDrawing);
  
  // Color picker
  document.querySelectorAll('.colorBtn').forEach(btn => {
    btn.addEventListener('click', function() {
      currentColor = this.dataset.color;
      document.querySelectorAll('.colorBtn').forEach(b => b.classList.remove('active'));
      this.classList.add('active');
    });
  });
  
  // Set first color as active
  const firstColor = document.querySelector('.colorBtn');
  if (firstColor) firstColor.classList.add('active');
}

function updateCanvasSize() {
  const canvas = document.getElementById('drawCanvas');
  if (!canvas) return;
  
  const container = document.getElementById('graphContainer');
  const graph = document.getElementById('graph');
  const svg = graph.querySelector('svg');

  const screenHeight = window.innerHeight;
  const minHeight = Math.max(screenHeight * 0.8 - 50, 100);
  const minWidth = Math.floor(window.innerWidth * 0.9)
  
  if (svg) {
    const bbox = svg.getBBox();
    // Don't apply the 0.75 scale here - use actual SVG dimensions
    const actualWidth = bbox.width + bbox.x;
    const actualHeight = bbox.height + bbox.y;
    
    // Add padding to match graph padding
    canvas.width = Math.max(minWidth, actualWidth + 30);
    canvas.height = Math.max(minHeight, actualHeight + 30);
    
    // Position canvas to align with SVG
    canvas.style.width = canvas.width + 'px';
    canvas.style.height = canvas.height + 'px';
  } else {
    canvas.width = minWidth // Math.max(minWidth, container.offsetWidth);
    canvas.height = minHeight // Math.max(minHeight, container.offsetHeight);
  }
  
  // Redraw all saved strokes
  redrawCanvas();
}

function startDrawing(e) {
  if (currentMode !== 'draw') return;
  
  isDrawing = true;
  const rect = e.target.getBoundingClientRect();
  lastX = e.clientX - rect.left;
  lastY = e.clientY - rect.top;
  
  // Start a new stroke
  drawingHistory.push({
    color: currentColor,
    points: [[lastX, lastY]]
  });
}

function draw(e) {
  if (!isDrawing || currentMode !== 'draw') return;
  
  const rect = e.target.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;
  
  drawingContext.strokeStyle = currentColor;
  drawingContext.lineWidth = lineWidth;
  drawingContext.beginPath();
  drawingContext.moveTo(lastX, lastY);
  drawingContext.lineTo(x, y);
  drawingContext.stroke();
  
  // Add point to current stroke
  const currentStroke = drawingHistory[drawingHistory.length - 1];
  currentStroke.points.push([x, y]);
  
  lastX = x;
  lastY = y;
}

function stopDrawing() {
  isDrawing = false;
}

function clearDrawing() {
  const canvas = document.getElementById('drawCanvas');
  if (!canvas || !drawingContext) return;
  
  drawingContext.clearRect(0, 0, canvas.width, canvas.height);
  drawingHistory = [];
}

function undoDraw() {
  if (drawingHistory.length === 0) return;
  
  drawingHistory.pop();
  redrawCanvas();
}

function redrawCanvas() {
  const canvas = document.getElementById('drawCanvas');
  if (!canvas || !drawingContext) return;
  
  drawingContext.clearRect(0, 0, canvas.width, canvas.height);
  drawingContext.lineWidth = lineWidth;
  
  for (const stroke of drawingHistory) {
    drawingContext.strokeStyle = stroke.color;
    drawingContext.beginPath();
    
    if (stroke.points.length > 0) {
      drawingContext.moveTo(stroke.points[0][0], stroke.points[0][1]);
      for (let i = 1; i < stroke.points.length; i++) {
        drawingContext.lineTo(stroke.points[i][0], stroke.points[i][1]);
      }
      drawingContext.stroke();
    }
  }
}