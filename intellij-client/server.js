const http = require("http");
const fs = require("fs");
const path = require("path");
const WebSocket = require("ws");

const server = http.createServer((req, res) => {
  if (req.method === "GET" && (req.url === "/" || req.url === "/index.html")) {
    // Serve index.html
    const filePath = path.join(__dirname, "index.html");
    fs.readFile(filePath, (err, data) => {
      if (err) {
        res.writeHead(500, { "Content-Type": "text/plain" });
        res.end("Error loading index.html\n");
      } else {
        res.writeHead(200, { "Content-Type": "text/html" });
        res.end(data);
      }
    });
  } else if (req.method === "GET" && (req.url === "/sample.js")) {
    // Serve index.html
    const filePath = path.join(__dirname, "sample.js");
    fs.readFile(filePath, (err, data) => {
      if (err) {
        res.writeHead(500, { "Content-Type": "text/plain" });
        res.end("Error loading sample.js\n");
      } else {
        res.writeHead(200, { "Content-Type": "text/html" });
        res.end(data);
      }
    });
  } else if (req.method === "POST") {
    let body = "";
    req.on("data", chunk => {
      body += chunk.toString();
    });

    req.on("end", () => {
      try {
        const data = JSON.parse(body);
        console.log("POST:", data);

        // broadcast to all websocket clients
        wss.clients.forEach(client => {
          if (client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(data));
          }
        });

        res.writeHead(200, { "Content-Type": "text/plain" });
        res.end("OK\n");
      } catch (e) {
        res.writeHead(400, { "Content-Type": "text/plain" });
        res.end("Invalid JSON\n");
      }
    });
  } else {
    res.writeHead(404, { "Content-Type": "text/plain" });
    res.end("Not found\n");
  }
});

const PORT = process.env.PORT || 8091;
server.listen(PORT, () => {
  console.log(`HTTP+WS server running at http://localhost:${PORT}/`);
});

// attach websocket server to the same http server
const wss = new WebSocket.Server({ server });

wss.on("connection", ws => {
  console.log("WebSocket client connected");
  ws.send(JSON.stringify({ message: "Welcome!" }));
});
