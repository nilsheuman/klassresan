// Main initialization
function setupEventListeners() {
  document.addEventListener("DOMContentLoaded", function () {
    // View toggles
    viewIds.forEach(id => {
      document.getElementById(id).style.display = "none";
      document
        .getElementById(id + "Toggle")
        .addEventListener("click", () => toggleView(id));
    });
    
    // Mode buttons
    document.getElementById('modeEdit').addEventListener('click', () => setMode('edit'));
    document.getElementById('modeDraw').addEventListener('click', () => setMode('draw'));
    
    // Action buttons
    document.getElementById("clearGraph").addEventListener("click", clearGraph);
    document.getElementById("saveSettings").addEventListener("click", saveSettings);

    // DOT editing
    document.getElementById("renderDot").addEventListener("click", function () {
      const dot = document.getElementById("dotTextarea").value;
      document.getElementById("dot").innerHTML = dot;

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
        
        updateCanvasSize();
      });
    });

    document.getElementById("lockDot").addEventListener("change", function () {
      const textarea = document.getElementById("dotTextarea");
      textarea.dataset.locked = this.checked;
    });

    // Export buttons
    document.getElementById("exportSvg").addEventListener("click", exportToSvg);
    document.getElementById("exportPng").addEventListener("click", exportToPng);
    document.getElementById("exportDot").addEventListener("click", exportToDot);
    document.getElementById("exportExcalidraw").addEventListener("click", async () => {await exportToExcalidraw()});
    
    // Node info panel
    document.getElementById("removeNode").addEventListener("click", removeNode);
    
    // Drawing controls
    document.getElementById("clearDrawing").addEventListener("click", clearDrawing);
    document.getElementById("undoDraw").addEventListener("click", undoDraw);
    
    // Filter updates
    document.getElementById("ignoredClasses").addEventListener("input", function() {
      settings.ignoredClasses = this.value.split(",")
        .map((s) => s.trim())
        .filter(Boolean);
      localStorage.setItem("klassresanSettings", JSON.stringify(settings));
      updateFilterIndicator();
    });
    
    document.getElementById("ignoredProjects").addEventListener("input", function() {
      settings.ignoredProjects = this.value.split(",")
        .map((s) => s.trim())
        .filter(Boolean);
      localStorage.setItem("klassresanSettings", JSON.stringify(settings));
      updateFilterIndicator();
    });
    
    // Initialize drawing
    initDrawing();
  });
}

setupEventListeners();
loadSettings();
connect();