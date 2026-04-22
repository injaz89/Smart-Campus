# Smart Campus API

## Introduction
The **Smart Campus API** is a robust, scalable RESTful web service engineered for university facilities management. It provides a centralized interface for tracking real-world campus entities like rooms and deploying environmental sensors (such as CO2 monitors, occupancy trackers, and smart lighting systems). By leveraging JAX-RS standards and operating in an embedded server state, it gives facilities managers detailed views of current resources and sensor historical states.

## Main Features
* **Built on JAX-RS Specifications:** Pure Java enterprise specifications prioritizing standardized decoupled routes with zero dependency bloat.
* **Thread-Safe Data Processing:** Employs natively asynchronous Singleton configurations via `ConcurrentHashMap` handling heavy concurrent client interactions safely.
* **Deep Nested Sub-Resources:** Uses Sub-Resource Locator patterns to smoothly branch URI flows (e.g. tracking readings nested inside isolated sensor paths).
* **Predictive Error Containment:** Implements granular `ExceptionMapper` hooks intercepting logical violations instantly preventing default framework stack trace leaks.
* **AOP Global Request Logging:** Integrates request/response tracking natively in the JAX-RS lifecycle context without diluting controller logic.

## Package Structure
```text
src/main/java/com/smartcampus
├── Main.java                 # Grizzly HTTP Server Launcher
├── config/                   # System & Security Settings
│   ├── ApplicationConfig.java                # JAX-RS Root Application Path /api/v1
│   ├── GenericExceptionMapper.java           # Security Net (Global 500 handler)
│   ├── LoggingFilter.java                    # Request/Response Observability Filter
│   └── (*ExceptionMapper.java)               # Granular Status Code Providers
├── model/                    # Data Entities
│   ├── Room.java                 
│   ├── Sensor.java               
│   ├── SensorReading.java        
│   └── errors/                               # Defined Java Runtime Exceptions
├── resource/                 # Endpoint REST Controllers
│   ├── DiscoveryResource.java                # API Metadata Root (HATEOAS concept)
│   ├── RoomResource.java                     
│   ├── SensorResource.java                   # Contains Sub-Resource dispatchers
│   └── SensorReadingResource.java            # Dynamic nested execution
└── store/                    # Simulation Database 
    └── CampusDataStore.java                  # Thread-Safe DB Singleton
```

## Setup and Build Instructions (NetBeans)
1. **Open the Project:**
   - Launch **Apache NetBeans**.
   - Go to `File` -> `Open Project...` and navigate to the extracted `CSA-Coursework` folder (NetBeans will recognize it automatically via the `pom.xml`).
2. **Build the Project:**
   - Right-click the `CSA-Coursework` project node in the Projects window and select **Clean and Build**. This allows Maven to download all necessary dependencies.
3. **Run the Server:**
   - Either expand `Source Packages` -> `com.smartcampus` package, right-click on `Main.java` and select **Run File**.
   - Or hit the **Run Project (Play icon)** located in the main NetBeans header.
   - Upon execution, the output component will render: `Smart Campus API started with endpoints available at http://localhost:8080/api/v1/`

## API Endpoints Reference

| Endpoint Path | HTTP Method | Action Description | Accepted Payload (JSON) |
|---------------|-------------|--------------------|-------------------------|
| `/api/v1/` | **GET** | Root discovery payload showing links. | *None* |
| `/api/v1/rooms/` | **GET** | Return array of all Rooms. | *None* |
| `/api/v1/rooms/` | **POST** | Add a newly registered Room. | `{"name": "Library", "capacity": 55}` |
| `/api/v1/rooms/{id}` | **GET** | Extract single Room metrics map. | *None* |
| `/api/v1/rooms/{id}` | **DELETE** | Remove Room (Safety bounded). | *None* |
| `/api/v1/sensors/` | **GET** | Returns sensors. Options: `?type=CO2` | *None* |
| `/api/v1/sensors/` | **POST** | Activate new Sensor bound to a Room. | `{"roomId": "abc...", "type": "CO2", "status":"ACTIVE"}` |
| `/api/v1/sensors/{id}` | **GET** | Retrieve targeted Sensor model. | *None* |
| `/api/v1/sensors/{id}/readings`| **GET** | Historical timeline data dump. | *None* |
| `/api/v1/sensors/{id}/readings`| **POST** | Insert measurement. *Syncs state*. | `{"timestamp": 12837498, "value": 40.5}` |

---

## API Error Responses

To maintain API structural fluidity, customized `ExceptionMappers` were defined to convert deep Java issues into logical HTTP outputs transparently:

| HTTP Status / Title | Exception Triggered | Underlying Scenario Mapping |
|---------------------|---------------------|-----------------------------|
| **403 Forbidden** | `SensorUnavailableException` | Initiating POST measurements to a physical Sensor currently blocked by `MAINTENANCE` status constraint. |
| **404 Not Found** | Runtime Engine Handler | Polling entities by ID that are provably unlisted within bounds. |
| **409 Conflict** | `RoomNotEmptyException` | Processing DELETE target against a primary Room entity actively hosting un-deleted local Sensors. |
| **415 Unsupported Media Mode** | System Rejection Engine | Supplying XML/Text payloads towards endpoints natively bounded by `@Consumes(MediaType.APPLICATION_JSON)` boundaries. |
| **422 Unprocessable Entity** | `LinkedResourceNotFoundException` | Syntactically correct JSON missing critical logical validation (e.g., matching a Sensor to a nonexistent `roomId` primary key). |
| **500 Internal Server Error** | `GenericExceptionMapper` | Defensive net intercepting unhandled JVM anomalies (`Throwable`) stripping trace prints from hostile network spaces. |

---

## Report / Final Submission Answers

### Part 1: Service Architecture & Setup

**Question: 1**
In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race con ditions.
**Answer:** By default, JAX-RS resource classes operate under a per-request lifecycle, meaning a new class object is instantiated for every incoming HTTP request and destroyed once the response is finalized. Because state within resource properties gets reset, an external Singleton data store class must be implemented. In our design, `CampusDataStore` serves this purpose. Since multiple thread-based request permutations interact with it simultaneously, the data store uses intrinsic thread-safe collections (`ConcurrentHashMap` and `CopyOnWriteArrayList`) to maintain critical section sync, preventing lost updates and race condition vulnerabilities during memory transactions.

**Question: 2**
Why is the provision of “Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?
**Answer:** Hypermedia as the Engine of Application State (HATEOAS) provides dynamic URL transitions embedded natively into system responses. Rather than forcing clients to hard-code endpoint path rules based on offline static API documents, HATEOAS navigates the client logically based strictly on the current state boundaries. This provides resilience where the server can organically evolve route paths behind the scenes and the dynamic linked responses will automatically update integration pathways seamlessly for the clients.

### Part 2: Room Management

**Question: 1**
When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.
**Answer:** Returning only IDs minimizes immediate overhead, vastly improving payload speed over large lists and heavily preserving network bandwidth. However, this shifts burdens client-side, causing an N+1 query issue where the client has to recursively call back individual entity fetch endpoints. Alternatively, returning full room objects guarantees processing simplicity at the client (they instantly have all context) but generates very dense payloads that monopolize greater bandwidth and memory footprint on both sides. A modern compromise involves tailored QueryParams or pagination to control granularity.

**Question: 2**
Is the DELETE operation idempotent in your implementation? Provide detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple time.
**Answer:** Yes, the fundamental behavior mirrors idempotency objectives. If a client mistakenly sends consecutive DELETE calls for the exact same target, the server's definitive end state remains conceptually identical, the room is entirely non-existent on the server. The first valid call actively wipes the resource. A subsequent call targeting the already missing entity will predictably fail (either throwing a strict 404 or our mapped conflict if constraints apply), preserving idempotency as the secondary request cannot mutate server stability or spawn redundant state changes.

### Part 3: Sensor Operations & Linking

**Question: 1**
We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?
**Answer:** Specifying ‘@Consumes’ rigorously protects the endpoint handler. If a client transmits ‘text/plain’ or ‘application/xml’ payloads facing a JSON requirement, JAX-RS executes strict Content Negotiation before method dispatch. Detecting the mismatch, JAX-RS preemptively denies entry into the Resource block and automatically broadcasts an integrated ‘HTTP 415 Unsupported Media Type’ response natively, entirely bypassing the local JVM's computational logic strings.

**Question: 2**
You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?
**Answer:** Paths define foundational entity taxonomy. Query parameters identify mutable refinements mapped to existing boundaries. Mapping filters natively into URLs (‘/sensors/type/CO2’) inherently incorrectly defines “type” as a child endpoint resource tier within a hierarchical boundary constraint, increasing controller endpoint rigidness. Conversely, type=CO2 adheres precisely to search characteristics and effortlessly adapts for concurrent scaling cleanly circumventing messy rigid permutations of path routing patterns.

### Part 4: Deep Nesting with Sub- Resources

**Question: 1**
Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con troller class?
**Answer:** Monolithic controller groupings inevitably bloat as domain layers extend. Sub-Resource Locators isolate and dispatch deeply ingrained REST paths to targeted subsidiary handler classes seamlessly. In this architecture, it compartmentalizes the context execution (SensorReadingResource). It decouples lifecycle tracking limits, enhances direct interface cohesiveness, enables object instantiation isolation, and sharply curbs unmaintainable "God" classes from mutating.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Question: 1**
WhyisHTTP422oftenconsideredmoresemanticallyaccurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?
**Answer:** HTTP 404 conveys natively that the targeted URI pathway simply isn't accessible or is wrong. However, when the URI works but a structurally sound JSON payload holds logic errors like validating against a foreign key ‘roomId’ parent that disappear HTTP 422 Processable Entity accurately tells the client that while the formatting is fine and the gateway is working, the domain rules and business constraints prevent the entity context from successfully parsing dependencies internally. 

**Question: 2** 
From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?
**Answer:** Exposing raw Java stack errors severely acts as an involuntary Information Disclosure vulnerability. Trace trails expose framework dependency structures, specific library versions, deep file-system hierarchies, unmasked data validation thresholds, and potential SQL handler dialects. Hackers utilize footprinting to map known public CVE exploit catalogs directly to these exposed framework tiers to optimize remote command execution injection schemes targeted explicitly toward your identified internal stack.

**Question: 3**
Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single re source method?
**Answer:** Applying Container filters employs Aspect-Oriented configuration. Logging is an orthogonal (cross-cutting) function; embedding ‘Logger.info ()’ randomly into disparate domain handlers bloat business logic, generates massive repetitive code overhead, and exposes human vulnerabilities where new endpoint mappings simply forget injection logging coverage. Filters natively wrap the entire inbound/outbound context lifecycle implicitly, assuring uniform logging integrity decoupled entirely from the actual functional code domains.
