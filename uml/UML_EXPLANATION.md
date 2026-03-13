# UML Diagrams — Explanation Guide

## Overview

This document explains each UML diagram created for the **Car Rental System** project.
The diagrams are located in the `uml/` folder and can be rendered using the PlantUML VS Code extension.

| File | Diagram Type | Purpose |
|---|---|---|
| `01_ClassDiagram.puml` | Class Diagram | All classes, fields, methods, and relationships |
| `02_ObjectDiagram.puml` | Object Diagram | Runtime snapshot with real data |
| `03_ExceptionHandlingFlow.puml` | Activity Diagram | Exception flow across all layers |
| `04_ExceptionHandlingClasses.puml` | Class Diagram | Per-class exception handling summary |
| `05_Architecture.puml` | Component Diagram | 4-layer architecture and dependencies |

---

## 1. Class Diagram

**File:** `uml/01_ClassDiagram.puml`

### What It Shows

The Class Diagram shows all the classes in the system, their attributes, methods, and how they
relate to each other. It is organized into 4 packages following a standard layered architecture.

---

### pckModels — The Data Layer

This package contains the model classes that represent real-world objects in the system.

**Inheritance Hierarchy:**

```
User (base class)
├── Admin
├── Customer
└── Driver
```

`User` is the base class. `Admin`, `Customer`, and `Driver` all **extend** it — this is called
**inheritance**. They share common attributes like `userId`, `email`, and `password`, but each
has its own specific data:

- `Driver` has `licenseNumber`, `licenseExpiry`, and `status` (PENDING / VERIFIED / REJECTED)
- `Customer` has `phone` and `photoPath`
- `Admin` has `adminId`

**Other Models:**

| Class | Description |
|---|---|
| `Car` | Represents a vehicle in the fleet with attributes like `brand`, `model`, `dailyRate`, and `status` |
| `Rental` | Links a Customer, Car, and optionally a Driver for a specific date range |
| `Payment` | Represents a payment record tied to a Rental |

**Relationships:**

| Relationship | Type | Meaning |
|---|---|---|
| Customer → Rental | One-to-Many | A customer can place many rentals |
| Car → Rental | One-to-Many | A car can be assigned to many rentals |
| Driver → Rental | One-to-Many | A driver can be assigned to many rentals |
| Rental → Payment | One-to-Zero-or-One | A rental has at most one payment |

---

### pckDatabase — The Data Access Layer

This package contains the DAO (Data Access Object) classes. Each DAO is responsible for all
SQL queries for one specific database table.

| DAO Class | Table | Responsibility |
|---|---|---|
| `UserDAO` | `users` | Login queries |
| `CustomerDAO` | `customers` | Customer account creation and lookup |
| `DriverDAO` | `drivers` | Driver registration, status updates |
| `CarDAO` | `cars` | Vehicle CRUD, availability by date |
| `RentalDAO` | `rentals` | Booking creation, status updates, date conflict checks |
| `PaymentDAO` | `payments` | Payment insertion and approval |

All DAOs depend on `DatabaseConnection` which manages the single shared MySQL connection.
If the connection fails, `DatabaseConnection` throws a `RuntimeException` so callers
never receive a `null` connection silently.

---

### pckServices — The Business Logic Layer

The service layer sits between the GUI and the DAOs. It contains the business rules.
The GUI never talks directly to the database — it always goes through a service.

| Service | Responsibility |
|---|---|
| `AuthService` | Login and logout, session management |
| `CustomerService` | Customer registration with validation |
| `DriverService` | Driver registration, admin verify/reject |
| `CarService` | Vehicle management with input validation |
| `RentalService` | Booking, cancellation, date availability check, payment approval |

**Example:** `RentalService.bookRental()` does not just insert a record — it first calls
`RentalDAO.isCarAvailableForDates()` to check for overlapping bookings. If the car is
already reserved, it returns `false` without touching the database.

---

### pckUtils — Utilities

| Class | Purpose |
|---|---|
| `SessionManager` | Tracks the currently logged-in user across the application |
| `AppConfig` | Manages all file paths for uploaded photos (vehicles, drivers, customers) |
| `CalendarPicker` | Custom Swing date picker component used in reservation forms |
| `DateUtil` | Helper for calculating the number of days between two dates |

---

## 2. Object Diagram

**File:** `uml/02_ObjectDiagram.puml`

### What It Shows

While the Class Diagram shows the **blueprint**, the Object Diagram shows a **real snapshot**
of the system at runtime — actual instances with real data values.

### Scenario: Customer Juan Books a Toyota VIOS with Driver Pedro

| Object | Instance Name | Key Values |
|---|---|---|
| Customer | `juan` | customerId=1, fullName="Juan Dela Cruz" |
| Car | `vios` | carId=1, brand="Toyota", model="VIOS", status="RENTED" |
| Driver | `pedro` | driverId=1, fullName="Pedro Santos", status="VERIFIED" |
| Rental | `rental1` | rentalId=1, startDate=2026-03-01, endDate=2026-03-05, totalAmount=8000.00 |
| Payment | `payment1` | paymentId=1, amountPaid=8000.00, method="CASH", status="PAID" |
| SessionManager | `session` | currentUser=juan, isLoggedIn=true |

### How the Objects Connect

```
juan ──places──► rental1 ──paid via──► payment1
vios ──assigned to──► rental1
pedro ──drives──► rental1
session ──tracks──► juan
```

This diagram makes the abstract class relationships concrete and easy to visualize.
It shows exactly how the foreign key relationships in the database translate to
object references at runtime.

---

## 3. Exception Handling Flow Diagram

**File:** `uml/03_ExceptionHandlingFlow.puml`

### What It Shows

This Activity Diagram shows how the system handles errors and exceptions as they flow
across the three layers — GUI, Service, and DAO.

### Flow Walkthrough

**Step 1 — GUI Layer: User triggers an action**
> The user clicks a button (e.g., Login, Book, Save Vehicle). The GUI collects the input
> and calls the appropriate service method.

**Step 2 — Service Layer: Input validation**
> The service validates the input first. If something is missing or invalid (e.g., empty
> email, password too short), it returns an error result immediately without touching the
> database. The GUI then shows a warning message to the user.

**Step 3 — DAO Layer: Database connection**
> The DAO calls `DatabaseConnection.getInstance().getConnection()`. If the connection
> fails (e.g., MySQL is not running), `DatabaseConnection` throws a `RuntimeException`
> with a clear message. The GUI catches this and shows an error dialog.

**Step 4 — DAO Layer: SQL execution**
> If the connection succeeds, the DAO executes the SQL query using a `PreparedStatement`.
> If a `SQLException` occurs (e.g., duplicate key, constraint violation):
> - The error is logged with `System.err.println` and `e.printStackTrace()`
> - If a transaction is active (e.g., creating an account across two tables), it is **rolled back**
> - The DAO returns `null`, `false`, or `-1` to the service

**Step 5 — Service Layer: Business rule check**
> The service applies business rules. For example, `RentalService.bookRental()` checks
> if the car is available for the requested dates. If not, it returns `false` and the
> GUI shows "Car not available for selected dates."

**Step 6 — GUI Layer: Success**
> If everything passes, the GUI shows a success message and refreshes the UI.

### Why This Design?

- **No exceptions leak to the GUI** — the GUI only receives `boolean`, `null`, or enum results
- **Transactions are safe** — rollback ensures the database stays consistent on failure
- **Clear error messages** — each layer logs enough context to diagnose the problem

---

## 4. Exception Handling Classes Diagram

**File:** `uml/04_ExceptionHandlingClasses.puml`

### What It Shows

This diagram gives a **class-by-class breakdown** of exactly which exceptions each class
handles and what it does with them.

### Key Classes and Their Handling

**`DatabaseConnection`**
- Throws `RuntimeException` if the JDBC driver is not found or the connection fails
- Reason: returning `null` would cause silent `NullPointerException` in every DAO

**`CustomerDAO` and `DriverDAO`** (Transaction methods)
- Catch `SQLException` → rollback the transaction → log the error
- `finally` block always resets `autoCommit(true)` to leave the connection clean

**`DriverDAO.convertDateToSql()`**
- Catches broad `Exception` → logs the bad input value → returns the original string as fallback
- Reason: a bad date format should not crash the entire registration flow

**`RentalDAO` and `PaymentDAO`** (Query methods)
- Catch `SQLException` → log with `e.printStackTrace()`
- Return safe defaults: `false`, `-1`, or empty `List`

**`AppConfig.copyFile()`**
- Catches `IOException` specifically (not broad `Exception`)
- Logs both the source and destination path for easier debugging
- Returns `null` on failure — callers check the return value

**`VehiclesTab` and `BrowseCarsGUI`** (Image loading)
- `InterruptedException` from `MediaTracker.waitForAll()` is handled **separately**
- Calls `Thread.currentThread().interrupt()` to restore the interrupt flag — Java best practice
- `IOException` is caught separately for file read failures

**`SettingsPanel`**
- Catches `SQLException` specifically when checking DB connectivity
- Returns `false` — the UI shows a "disconnected" indicator

**Service Layer**
- Intentionally has **no exception handling**
- Returns `boolean`, `int`, or enum results instead
- Keeps business logic clean and lets the GUI decide how to present errors

---

## 5. Architecture Diagram

**File:** `uml/05_Architecture.puml`

### What It Shows

This Component Diagram shows the **4-layer architecture** of the application and how
data flows between the layers.

### The 4 Layers

**Layer 1 — Presentation Layer (GUI)**

The top layer contains all the GUI classes the user sees and interacts with.

| Class | Role |
|---|---|
| `LoginGUI` | Entry point — routes to the correct dashboard based on role |
| `AdminDashboardGUI` | Full admin control panel |
| `CustomerDashboardGUI` | Customer portal — browse, reserve, manage bookings |
| `DriverDashboardGUI` | Driver portal — view assignments |
| `BrowseCarsGUI` | Car browsing with filters and date availability |
| `MakeReservationPanel` | Reservation form with calendar date pickers |

**Layer 2 — Service Layer (Business Logic)**

The service layer enforces business rules. The GUI never calls a DAO directly.

| Service | Key Business Rule |
|---|---|
| `AuthService` | Validates credentials, manages session |
| `CustomerService` | Validates registration fields, checks email uniqueness |
| `DriverService` | Validates license, handles admin verify/reject |
| `CarService` | Validates car data before insert/update |
| `RentalService` | Checks date availability before booking |

**Layer 3 — DAO Layer (Data Access)**

Each DAO handles SQL for exactly one table. No business logic lives here.

**Layer 4 — Database**

MySQL database `car_rental_db` with 6 normalized tables:
`users`, `customers`, `drivers`, `cars`, `rentals`, `payments`

**Utilities (Cross-cutting)**

The `pckUtils` package supports all layers:
- `SessionManager` — shared state for the logged-in user
- `UIAssets` — consistent fonts, colors, and component styles
- `AppConfig` — centralized file path management
- `DatabaseConnection` — single shared JDBC connection with reconnect logic
- `CalendarPicker` — reusable date picker used in multiple panels

### Why This Architecture?

| Benefit | Explanation |
|---|---|
| **Separation of concerns** | Each layer has one job — GUI shows data, Service validates, DAO queries |
| **Maintainability** | Changing the database only requires updating the DAO layer |
| **Testability** | Services can be tested independently of the GUI |
| **Consistency** | All UI components use `UIAssets` so the look is uniform |

---

## Common Questions and Answers

| Question | Answer |
|---|---|
| Why use inheritance for User? | Admin, Customer, and Driver share login data (`email`, `password`, `role`). Inheritance avoids duplicating these fields in every class. |
| Why separate Services from DAOs? | Separation of concerns — DAOs only do SQL, Services only do business logic. This makes the code easier to maintain and test. |
| Why throw RuntimeException in DatabaseConnection? | A null connection would cause NullPointerExceptions everywhere. A RuntimeException with a clear message is easier to debug and handle. |
| Why roll back transactions? | Creating a Customer requires inserting into both `users` and `customers` tables. If the second insert fails, we must undo the first to keep the database consistent. |
| Why handle InterruptedException separately? | Calling `Thread.currentThread().interrupt()` restores the interrupt flag so the thread's interrupted state is not silently lost — this is a Java concurrency best practice. |
| What is CalendarPicker? | A custom Swing component we built that shows a popup calendar when clicked, used for date selection in reservations and booking modifications. |
| Why check car availability in the Service layer? | Business rules belong in the Service layer, not the DAO. The DAO provides the `isCarAvailableForDates()` query, but the decision to block the booking is made in `RentalService`. |
| What does the Object Diagram show that the Class Diagram doesn't? | The Object Diagram shows actual runtime instances with real values, making it easier to understand how the abstract class relationships work in a concrete scenario. |

---

## Presentation Order (Recommended)

1. **Start with `05_Architecture.puml`** — gives the big picture before diving into details
2. **Then `01_ClassDiagram.puml`** — explain models first, then DAOs, then services
3. **Then `02_ObjectDiagram.puml`** — make it concrete with real data
4. **Then `03_ExceptionHandlingFlow.puml`** — show how the system handles failures
5. **End with `04_ExceptionHandlingClasses.puml`** — detailed reference per class
