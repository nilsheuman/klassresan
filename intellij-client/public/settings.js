// Settings management
function loadSettings() {
  const raw = localStorage.getItem("klassresanSettings");
  if (raw) {
    try {
      settings = JSON.parse(raw);
    } catch (e) {
      console.error("Failed to parse settings", e);
    }
  }
  document.getElementById("serverUrl").value = settings.websocketUrl || "";
  document.getElementById("intellijServerUrl").value =
    settings.intellijServerUrl || "";
  document.getElementById("ignoredClasses").value = (
    settings.ignoredClasses || []
  ).join(", ");
  document.getElementById("ignoredProjects").value = (
    settings.ignoredProjects || []
  ).join(", ");
  updateFilterIndicator();
}

function saveSettings() {
  settings.websocketUrl = document.getElementById("serverUrl").value.trim();
  settings.intellijServerUrl = document
    .getElementById("intellijServerUrl")
    .value.trim();
  settings.ignoredClasses = document
    .getElementById("ignoredClasses")
    .value.split(",")
    .map((s) => s.trim())
    .filter(Boolean);
  settings.ignoredProjects = document
    .getElementById("ignoredProjects")
    .value.split(",")
    .map((s) => s.trim())
    .filter(Boolean);
  localStorage.setItem("klassresanSettings", JSON.stringify(settings));
  updateFilterIndicator();
  alert("Settings saved. Reload to reconnect.");
}

function updateFilterIndicator() {
  const indicator = document.getElementById('filterIndicator');
  const hasFilters = settings.ignoredClasses.length > 0 || settings.ignoredProjects.length > 0;
  if (hasFilters) {
    indicator.classList.remove('hidden');
  } else {
    indicator.classList.add('hidden');
  }
}

function addNodeToFilter(nodeKey) {
  const frame = nodes.get(nodeKey);
  if (!frame) return;
  
  const className = frame.clazz;
  if (!settings.ignoredClasses.includes(className)) {
    settings.ignoredClasses.push(className);
    document.getElementById("ignoredClasses").value = settings.ignoredClasses.join(", ");
    localStorage.setItem("klassresanSettings", JSON.stringify(settings));
    updateFilterIndicator();
  }
}