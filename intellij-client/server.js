const http = require("http");
const fs = require("fs");
const path = require("path");
const WebSocket = require("ws");

const server = http.createServer((req, res) => {
  if (req.method === "GET") {
    let filePath;
    if (req.url === "/" || req.url === "/index.html") {
      filePath = path.join(__dirname, "public", "index.html");
    } else {
      filePath = path.join(__dirname, "public", req.url);
    }
    console.log('GET', filePath)

    fs.readFile(filePath, (err, data) => {
      if (err) {
        res.writeHead(404, { "Content-Type": "text/plain" });
        res.end("File not found\n");
      } else {
        const ext = path.extname(filePath);
        let contentType = "text/plain";
        if (ext === ".html") contentType = "text/html";
        if (ext === ".css") contentType = "text/css";
        if (ext === ".js") contentType = "application/javascript";
        if (ext === ".png") contentType = "image/png";
        if (ext === ".jpg" || ext === ".jpeg") contentType = "image/jpeg";
        
        res.writeHead(200, { "Content-Type": contentType });
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