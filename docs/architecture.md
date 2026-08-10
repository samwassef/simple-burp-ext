# Architecture and processing flow

This document is the editable diagram source for **JS in Scope**. Mermaid-enabled Markdown viewers
render the diagrams directly. They reflect `src/main/java/dev/burpext/jsinscope`.

## Class diagram

```mermaid
classDiagram
    class BurpExtension {
        <<Montoya interface>>
        +initialize(MontoyaApi)
    }
    class HttpHandler {
        <<Montoya interface>>
        +handleHttpRequestToBeSent(request)
        +handleHttpResponseReceived(response)
    }
    class AbstractTableModel {
        <<Swing base class>>
    }
    class DocumentListener {
        <<Swing interface>>
    }
    class Extension {
        -MontoyaApi api
        -JavaScriptPanel panel
        +initialize(MontoyaApi)
        -refreshFromSiteMap()
    }
    class JavaScriptHttpHandler {
        -MontoyaApi api
        -JavaScriptPanel panel
        +handleHttpRequestToBeSent(request)
        +handleHttpResponseReceived(response)
    }
    class JavaScriptPanel {
        -JavaScriptTableModel model
        +addOrUpdate(JavaScriptEntry)
    }
    class JavaScriptTableModel {
        -Map entriesByUrl
        -List rows
        +addOrUpdate(JavaScriptEntry)
        +clear()
        +getValueAt(row, column)
    }
    class JavaScriptEntry {
        <<record>>
        +String url
        +String method
        +int statusCode
        +String mimeType
        +String source
        +Instant lastSeen
    }
    class JavaScriptUrl {
        <<utility>>
        +isJavaScriptFile(String) boolean
        +identity(String) String
    }
    class SimpleDocumentListener {
        -Runnable action
    }

    BurpExtension <|.. Extension
    HttpHandler <|.. JavaScriptHttpHandler
    AbstractTableModel <|-- JavaScriptTableModel
    DocumentListener <|.. SimpleDocumentListener
    Extension *-- JavaScriptPanel : creates
    Extension ..> JavaScriptHttpHandler : registers
    Extension ..> JavaScriptEntry : creates from site map
    Extension ..> JavaScriptUrl : checks
    JavaScriptHttpHandler --> JavaScriptPanel : publishes entries
    JavaScriptHttpHandler ..> JavaScriptEntry : creates from responses
    JavaScriptHttpHandler ..> JavaScriptUrl : checks
    JavaScriptPanel *-- JavaScriptTableModel
    JavaScriptPanel ..> SimpleDocumentListener : filters table
    JavaScriptTableModel o-- JavaScriptEntry : stores
    JavaScriptTableModel ..> JavaScriptUrl : normalizes identity
```

## Component diagram

```mermaid
flowchart LR
    burp[Burp Suite] -->|initialize| lifecycle[Extension lifecycle]
    lifecycle -->|register suite tab| ui[Swing UI]
    lifecycle -->|scan existing items| siteMap[Burp site map]
    burp -->|HTTP callbacks| handler[Passive HTTP handler]
    siteMap --> detector[Scope and JavaScript URL checks]
    handler --> detector
    detector -->|matching metadata| entry[JavaScriptEntry snapshot]
    entry -->|EDT hand-off| model[De-duplicating table model]
    model --> ui
    ui -->|refresh request| lifecycle
    ui -->|filter and clear| model
```

## Resource processing flow

Startup/site-map refreshes and newly received responses share the same validation path. Site-map rows
use `SITE_MAP` as their source; live rows use the name of Burp's originating tool.

```mermaid
flowchart TD
    trigger{Processing trigger}
    trigger -->|startup or Refresh| scan[Read site-map request and response pairs]
    trigger -->|new HTTP response| live[Read request and response metadata]
    scan --> valid{Request and response present?}
    valid -->|no| skip[Ignore item]
    valid -->|yes| scope{URL currently in Burp scope?}
    live --> scope
    scope -->|no| continueTraffic[Keep traffic unchanged]
    scope -->|yes| extensionCheck{JavaScript file extension?}
    extensionCheck -->|no| continueTraffic
    extensionCheck -->|yes| snapshot[Create immutable JavaScriptEntry]
    snapshot --> edt[Queue update on Swing event thread]
    edt --> identity[Remove fragment and preserve query for identity]
    identity --> known{Identity already stored?}
    known -->|yes| update[Replace row and update last-seen metadata]
    known -->|no| insert[Append row in insertion order]
    update --> display[Refresh sortable and filterable table]
    insert --> display
```

## Threading model

- Burp initialization creates the panel synchronously on Swing's Event Dispatch Thread (EDT).
- Site-map scans run on a daemon worker thread to avoid freezing the interface.
- Burp invokes HTTP callbacks on its own worker threads.
- Both producers call `JavaScriptPanel.addOrUpdate`, which queues model mutation on the EDT.
- The extension observes traffic only and returns every original request and response unchanged.
