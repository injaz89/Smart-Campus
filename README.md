# Smart Campus API

## Overview
The Smart Campus API is a robust, highly available RESTful web service built with JAX-RS (Jersey) and Grizzly. Intended for use by campus facilities managers and automated building systems, it facilitates comprehensive room, sensor, and sensor reading history management. The architecture implements industry-standard RESTful designs matching resource nesting techniques (Sub-Resource Locators) and robust custom ExceptionMappers for granular error containment. The data store uses a thread-safe singleton paradigm utilizing `ConcurrentHashMap` and `CopyOnWriteArrayList` to ensure highly accurate parallel processing under stress.

## Setup and Build Instructions
1. Ensure you have **Java JDK 17** (or above) and **Maven** installed on your system.
2. Clone or extract the repository onto your local machine.
3. Open a terminal or command prompt and navigate to the project's root directory (where `pom.xml` resides).
4. Build the application using maven:
   ```bash
   mvn clean package
   ```
5. Launch the application server locally by finding the compiled main execution (the project does not presently define a maven exec plugin natively, but the server can be run if Main is invoked). If you have configured an executable via an IDE, run `com.smartcampus.Main`. 

## Sample Requests (curl)

**1. Discovery Endpoint**
```bash
curl -i -X GET http://localhost:8080/api/v1/
```

**2. Create a new Room**
```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Library Study Zone 1", "capacity": 25}'
```

**3. Get a specific Room by ID**
*(Assuming the ID created above is generic uuid e.g. `<room-id>`)*
```bash
curl -i -X GET http://localhost:8080/api/v1/rooms/<room-id>
```

**4. Register a new Sensor linked to the Room**
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"roomId": "<room-id>", "type": "CO2", "status": "ACTIVE"}'
```

**5. Find Sensors by filtering their Type**
```bash
curl -i -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

**6. Submit a Reading to a Sensor**
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/<sensor-id>/readings \
  -H "Content-Type: application/json" \
  -d '{"timestamp": 1698234850021, "value": 415.5}'
```

---

## Report / Requirements Questions Answered

### Part 1, Task 1
**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** By default, JAX-RS resource classes operate under a **per-request lifecycle**, meaning a new class object is instantiated for every incoming HTTP request and destroyed once the response is finalized. Because state within resource properties gets reset, an external Singleton data store class must be implemented. In our design, `CampusDataStore` serves this purpose. Since multiple thread-based request permutations interact with it simultaneously, the data store uses intrinsic thread-safe collections (`ConcurrentHashMap` and `CopyOnWriteArrayList`) to maintain critical section sync, preventing lost updates and race condition vulnerabilities during memory transactions.

### Part 1, Task 2
**Question:** Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:** Hypermedia as the Engine of Application State (HATEOAS) provides dynamic URL transitions embedded natively into system responses. Rather than forcing clients to hard-code endpoint path rules based on offline static API documents, HATEOAS navigates the client logically based strictly on the current state boundaries. This provides resilience where the server can organically evolve route paths behind the scenes and the dynamic linked responses will automatically update integration pathways seamlessly for the clients.

### Part 2, Task 1
**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer:** 
Returning only IDs minimizes immediate overhead, vastly improving payload speed over large lists and heavily preserving network bandwidth. However, this shifts processing burdens client-side, causing an N+1 query issue where the client has to recursively call back individual entity fetch endpoints. Alternatively, returning full room objects guarantees processing simplicity at the client (they instantly have all context) but generates very dense payloads that monopolize greater bandwidth and memory footprint on both sides. A modern compromise involves tailored QueryParams or pagination to control granularity.

### Part 2, Task 2
**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:** Yes, the fundamental behavior mirrors idempotency objectives. If a client mistakenly sends consecutive DELETE calls for the exact same target, the server's definitive end state remains conceptually identical—the room is entirely non-existent on the server. The first valid call actively wipes the resource. A subsequent call targeting the already missing entity will predictably fail (either throwing a strict 404 or our mapped conflict if constraints apply), preserving idempotency as the secondary request cannot mutate server stability or spawn redundant state changes.

### Part 3, Task 1
**Question:** We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:** Specifying `@Consumes` rigorously protects the endpoint handler. If a client transmits `text/plain` or `application/xml` payloads facing a JSON requirement, JAX-RS executes strict Content Negotiation before method dispatch. Detecting the mismatch, JAX-RS preemptively denies entry into the Resource block and automatically broadcasts an integrated `HTTP 415 Unsupported Media Type` response natively, entirely bypassing the local JVM's computational logic strings.

### Part 3, Task 2
**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:** Paths define foundational entity taxonomy. Query parameters identify mutable refinements mapped to existing boundaries. Mapping filters natively into URLs (`/sensors/type/CO2`) inherently incorrectly defines “type” as a child endpoint resource tier within a hierarchical boundary constraint, increasing controller endpoint rigidness. Conversely, `?type=CO2` adheres precisely to search characteristics and effortlessly adapts for concurrent scaling (e.g., `?type=CO2&status=ACTIVE`) cleanly circumventing messy rigid permutations of path routing patterns.

### Part 4, Task 1
**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer:** Monolithic controller groupings inevitably bloat as domain layers extend. Sub-Resource Locators isolate and dispatch deeply ingrained REST paths to targeted subsidiary handler classes seamlessly. In this architecture, it compartmentalizes the context execution (`SensorReadingResource`). It decouples lifecycle tracking limits, enhances direct interface cohesiveness, enables object instantiation isolation, and sharply curbs unmaintainable "God" classes from mutating.

### Part 5, Task 2
**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:** HTTP 404 conveys natively that the targeted URI pathway simply isn't accessible or is wrong. However, when the URI works but a structurally sound JSON payload holds logic errors—like validating against a foreign-key `roomId` parent that disappeared—HTTP 422 Unprocessable Entity accurately tells the client that while the formatting is fine and the gateway is working, the domain rules and business constraints prevent the entity context from successfully parsing dependencies internally. 

### Part 5, Task 4
**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:** Exposing raw Java stack errors severely acts as an involuntary Information Disclosure vulnerability. Trace trails expose framework dependency structures, specific library versions, deep file-system hierarchies, unmasked data validation thresholds, and potential SQL handler dialects. Hackers utilize footprinting to map known public CVE exploit catalogs directly to these exposed framework tiers (like Jersey 2 or Tomcat version chains) to optimize remote command execution injection schemes targeted explicitly toward your identified internal stack.

### Part 5, Task 5
**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer:** Applying Container filters employs Aspect-Oriented configuration. Logging is an orthogonal (cross-cutting) function; embedding `Logger.info()` randomly into disparate domain handlers bloats business logic, generates massive repetitive code overhead, and exposes human vulnerabilities where new endpoint mappings simply forget injection logging coverage. Filters natively wrap the entire inbound/outbound context lifecycle implicitly, assuring uniform logging integrity decoupled entirely from the actual functional code domains.
