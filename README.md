# Klassresan

Tools for visualizing code, focus on java/kotlin in intellij

## [Intellj Plugin](intellij-plugin/)
Is a extension for [Intellij](https://www.jetbrains.com/idea/) that hooks into the debugger and posts stack frames over http.

## [Intellj Client](intellij-client/)
Is a nodejs server that receives those frames and exposes a websocket, that a simple web page draws graphs with [graphvis](https://github.com/mdaines/viz-js)

# How it works

```mermaid
sequenceDiagram
    autonumber
    participant Plugin as IntelliJ Plugin
    participant Debugger as IntelliJ Debugger
    participant NodeServer as Node.js WebServer
    participant Browser as Web Browser (WebSocket + Viz.js)

    Plugin->>Debugger: Hook into debugger events
    Debugger-->>Plugin: Provides current file, line, method info
    Plugin->>NodeServer: POST JSON {file, line, method}
    NodeServer-->>Plugin: HTTP 200 OK

    Browser->>NodeServer: Connect via WebSocket
    NodeServer-->>Browser: Push new frame data as JSON
    Browser->>Browser: Parse JSON
    Browser->>Browser: Render graph using Viz.js

```

Inspired from https://github.com/timKraeuter/VisualDebugger