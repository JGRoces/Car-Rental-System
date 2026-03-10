# 🚗 Car Rental System — Project Context Document
> **For Group Members:** Copy and paste this entire document into your Claude chat as your first message to give Claude full context about this project before asking for help.

---

## 📌 Project Overview

We are building a **desktop-based Car Rental Management System** as a school group project. The application is built entirely in **Java (Java 21)** using **Java Swing** for the GUI, and **MySQL** for the database. The IDE used is **VSCode**, and we use **GitHub Desktop** for version control.

The system has **two actors:**
- **Admin** — Manages cars, customers, rentals, and generates reports
- **Customer** — Browses cars, makes reservations, views rental history, and makes payments

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| GUI Framework | Java Swing |
| Database | MySQL 8.0+ |
| DB Driver | mysql-connector-j-9.6.0.jar |
| IDE | VSCode |
| Version Control | Git + GitHub Desktop |

---

## 📁 Full Project File Structure

```
CAR-RENTAL-SYSTEM/
│
├── assets/
│   ├── images/
│   │   ├── bydshowcase.jpg
│   │   └── logo.png
│   ├── icons/
│   │   ├── nav/
│   │   │   ├── overview.png
│   │   │   ├── management.png
│   │   │   ├── payments.png
│   │   │   ├── reports.png
│   │   │   ├── account.png
│   │   │   ├── settings.png
│   │   │   ├── signout.png
│   │   │   ├── sidebar-close.png          ← new
│   │   │   └── sidebar-open.png           ← new
│   │   └── action/
│   │       ├── add.png
│   │       ├── edit.png
│   │       ├── delete.png
│   │       ├── verify.png
│   │       ├── reject.png
│   │       ├── search.png
│   │       ├── filter.png
│   │       ├── refresh.png
│   │       ├── export.png
│   │       ├── upload.png
│   │       ├── calendar.png
│   │       └── car.png
│   └── defaults/
│       ├── default-avatar.png
│       └── default-vehicle.png
│
├── database/
│   └── car_rental_db.sql
│
├── lib/
│   └── mysql-connector-j-9.6.0.jar        ← NOT pushed to GitHub
│
└── src/
    │
    ├── pckMain/
    │   ├── Main.java
    │   ├── LoginGUI.java
    │   └── SignUpChoiceGUI.java
    │
    ├── pckModels/
    │   ├── User.java
    │   ├── Admin.java
    │   ├── Customer.java
    │   ├── Driver.java
    │   ├── Car.java
    │   ├── Rental.java
    │   └── Payment.java
    │
    ├── pckDatabase/
    │   ├── DatabaseConnection.java
    │   ├── UserDAO.java
    │   ├── CustomerDAO.java               ← updated (+ getAllCustomers)
    │   ├── DriverDAO.java
    │   ├── CarDAO.java
    │   └── RentalDAO.java                 ← updated (+ getAllRentals, hasActiveRental)
    │
    ├── pckAdmin/
    │   ├── AdminDashboardGUI.java
    │   │
    │   ├── panels/
    │   │   ├── OverviewPanel.java         ✅ done
    │   │   ├── ManagementPanel.java       🔲 next
    │   │   ├── PaymentsPanel.java         🔲 stub
    │   │   ├── ReportsPanel.java          🔲 stub
    │   │   ├── AccountPanel.java          🔲 stub
    │   │   └── SettingsPanel.java         🔲 stub
    │   │
    │   ├── tabs/                          ← new folder
    │   │   ├── VehiclesTab.java           ✅ done
    │   │   ├── DriversTab.java            ✅ done
    │   │   ├── CustomersTab.java          ✅ done
    │   │   └── RentalsTab.java            ✅ done
    │   │
    │   ├── dialogs/                       ← new folder
    │   │   └── AddVehicleDialog.java      🔲 next
    │   │
    │   └── shared/
    │       └── AdminUIHelper.java
    │
    ├── pckCustomer/
    │   ├── CustomerDashboardGUI.java      🔲 skeleton only
    │   └── CustomerSignUpGUI.java
    │
    ├── pckDriver/
    │   └── DriverSignUpGUI.java
    │
    ├── pckServices/
    │   ├── AuthService.java
    │   ├── CustomerService.java
    │   └── DriverService.java
    │
    └── pckUtils/
        ├── SessionManager.java
        ├── UIAssets.java
        ├── CustomTitleBar.java
|       └── AppConfig.java
│
├── .gitignore
├── CONTRIBUTING.md
├── LICENSE
└── README.md
```

---

## 🗄️ Database Schema

**Database name:** `car_rental_db`
**MySQL user for the Java app:** `carrentaluser` / `carrentalpass`

### Tables

#### `users` — Base account for both actors
```sql
user_id    INT AUTO_INCREMENT PRIMARY KEY
full_name  VARCHAR(100) NOT NULL
email      VARCHAR(100) NOT NULL UNIQUE
password   VARCHAR(255) NOT NULL         -- plain text for now, hash later
role       ENUM('ADMIN', 'CUSTOMER') NOT NULL
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

#### `customers` — Extended profile for Customer accounts only
```sql
customer_id    INT AUTO_INCREMENT PRIMARY KEY
user_id        INT NOT NULL UNIQUE         -- FK → users (one-to-one)
phone_number   VARCHAR(20)
address        VARCHAR(255)
license_number VARCHAR(50) NOT NULL UNIQUE
```

#### `cars` — Car inventory
```sql
car_id        INT AUTO_INCREMENT PRIMARY KEY
brand         VARCHAR(50) NOT NULL
model         VARCHAR(50) NOT NULL
year          YEAR NOT NULL
plate_number  VARCHAR(20) NOT NULL UNIQUE
category      ENUM('Sedan','SUV','Van','Truck','Pickup','Coupe','Minivan','MPV') NOT NULL
transmission  ENUM('Automatic','Manual','CVT') NOT NULL
fuel_type     ENUM('Gasoline','Diesel','Hybrid','Electric') NOT NULL DEFAULT 'Gasoline'
seat_capacity INT NOT NULL
daily_rate    DECIMAL(10,2) NOT NULL
status        ENUM('AVAILABLE','RENTED','MAINTENANCE') DEFAULT 'AVAILABLE'
image_path    VARCHAR(255)               -- filename only, resolved to assets/images/<filename>
color         VARCHAR(30)                -- e.g. "Red", "Pearl White", "Midnight Black"
created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

**Migration SQL** (run once if the table already exists without fuel_type):
```sql
ALTER TABLE cars ADD COLUMN fuel_type ENUM('Gasoline','Diesel','Hybrid','Electric') NOT NULL DEFAULT 'Gasoline' AFTER transmission;
ALTER TABLE cars MODIFY COLUMN category ENUM('Sedan','SUV','Van','Truck','Pickup','Coupe','Minivan','MPV') NOT NULL;
ALTER TABLE cars MODIFY COLUMN transmission ENUM('Automatic','Manual','CVT') NOT NULL;
```

#### `rentals` — Rental transactions
```sql
rental_id    INT AUTO_INCREMENT PRIMARY KEY
customer_id  INT NOT NULL                  -- FK → customers
car_id       INT NOT NULL                  -- FK → cars
start_date   DATE NOT NULL
end_date     DATE NOT NULL
total_amount DECIMAL(10,2) DEFAULT 0.00   -- calculated by RentalService
status       ENUM('PENDING','ACTIVE','COMPLETED','CANCELLED') DEFAULT 'PENDING'
created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

#### `payments` — Payment records
```sql
payment_id     INT AUTO_INCREMENT PRIMARY KEY
rental_id      INT NOT NULL               -- FK → rentals
amount_paid    DECIMAL(10,2) NOT NULL
payment_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
payment_method ENUM('CASH','CARD','ONLINE') NOT NULL
status         ENUM('PAID','PENDING','REFUNDED') DEFAULT 'PENDING'
```

---

## ✅ Completed Files — What They Do

### `Main.java` (pckMain)
Entry point. Launches `LoginGUI` on the Swing Event Dispatch Thread.
```java
SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
```

---

### `LoginGUI.java` (pckMain)
Two-panel login screen: **1200 x 800**, not resizable.
- **Left panel (60%)** — Dark background (`#141414`), 4-color accent bar, app branding, car image placeholder
- **Right panel (40%)** — White, login form with email + password fields, show/hide password checkbox, Sign In button
- Calls `AuthService.login()` on submit, routes to correct dashboard via `User.isAdmin()`
- Status label shows errors in red, warnings in yellow

**To swap in the car image**, replace the placeholder block in `buildLeftPanel()` with:
```java
ImageIcon raw = new ImageIcon("assets/images/login-car.png");
Image scaled = raw.getImage().getScaledInstance(560, 400, Image.SCALE_SMOOTH);
JLabel imgLabel = new JLabel(new ImageIcon(scaled));
imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
inner.add(imgLabel);
```
Recommended image: landscape orientation, at least 560x400px, close to 4:3 or 16:9 ratio.

---

### `User.java` (pckModels)
Base class for both actors. Fields: `userId`, `fullName`, `email`, `password`, `role`.
Two constructors: one for new users (no ID), one for loading from DB (with ID).
Key helper methods:
```java
user.isAdmin()    // returns true if role == "ADMIN"
user.isCustomer() // returns true if role == "CUSTOMER"
```

---

### `DatabaseConnection.java` (pckDatabase)
Singleton pattern. One shared connection for the whole app.
```java
// How every DAO gets the connection:
Connection conn = DatabaseConnection.getInstance().getConnection();
```
Auto-reconnects if connection drops. Prints `[DB]` status messages to console.
**⚠️ Never push real credentials to GitHub.** Before committing, replace with placeholders:
```java
private static final String URL  = "jdbc:mysql://localhost:3306/car_rental_db";
private static final String USER = "your_username";
private static final String PASS = "your_password";
```

---

### `UserDAO.java` (pckDatabase)
Two methods:
- `getUserByEmailAndPassword(email, password)` — Used by AuthService for login
- `getUserByEmail(email)` — Used to check if email already exists

---

### `Car.java` (pckModels)
Model class for a car. Maps directly to the `cars` table.

**Fields:** `carId`, `brand`, `model`, `year`, `plateNumber`, `category`, `transmission`, `fuelType`, `seatCapacity`, `dailyRate`, `status`, `imagePath`, `color`

**Two constructors:**
- New car (before DB insert) — no `carId`, 9 params: brand/model/year/plate/category/transmission/fuelType/seatCapacity/dailyRate. Defaults `status` to `"AVAILABLE"`, `imagePath` and `color` to `null`
- Loaded from DB — all 13 params including `carId`, `fuelType`, `status`, `imagePath`, `color`

**Convenience methods:**
```java
car.getDisplayName()  // "2023 Toyota Vios (ABC-123)" — for dropdowns/tables
car.getShortName()    // "Toyota Vios" — for car cards
car.isAvailable()     // true if status == "AVAILABLE"
car.getFuelType()     // "Gasoline", "Diesel", "Hybrid", or "Electric"
car.getColor()        // e.g. "Red", "Pearl White"
```

---

### `AuthService.java` (pckServices)
Utility class (never instantiated). Two static methods:
```java
// Login — returns User on success, null on failure. Also sets SessionManager.
User user = AuthService.login(email, password);

// Logout — clears the session
AuthService.logout();
```

---

### `SessionManager.java` (pckUtils)
Utility class that holds the currently logged-in user in memory for the entire session.
```java
SessionManager.setCurrentUser(user);        // Set on login
SessionManager.getCurrentUser();            // Get anywhere in the app
SessionManager.isLoggedIn();                // Check if someone is logged in
SessionManager.clearSession();              // Clear on logout
```

---

### `AdminDashboardGUI.java` (pckAdmin)
Full dashboard for Admin. **1600 x 900**, resizable (minimum 1024 x 600).

**Layout:**
```
┌──────────────────────────────────────────────────┐
│                   TOP BAR (64px)                 │
├──────────────┬───────────────────────────────────┤
│              │                                   │
│  SIDEBAR     │        CONTENT AREA               │
│  220px /     │       (CardLayout)                │
│  60px        │                                   │
└──────────────┴───────────────────────────────────┘
```

**Top Bar:** 4-color accent dots + app name on left. Admin name, role, and avatar circle (draws first initial from SessionManager) on right.

**Sidebar:** Dark `#121212`, collapsible via `◀` / `▶` toggle button. Collapses from 220px (icons + text) to 60px (icons only). Nav items: Overview, Manage Cars, Manage Customers, Manage Rentals, Reports. Logout pinned to bottom with confirm dialog.

**Content Area (CardLayout):** Swaps panels without creating new windows. Panel keys: `OVERVIEW`, `CARS`, `CUSTOMERS`, `RENTALS`, `REPORTS`.

**Overview Panel (default landing view):**
- *At a Glance* — 4 stat cards: Total Cars (blue), Active Rentals (green), Customers (yellow), Total Revenue (red). Values show `—` until DAO queries are connected.
- *Recent Rentals* — Empty table with correct column headers. TODO: populate from RentalDAO.
- *Quick Actions* — 4 colored buttons that switch to their respective panel via `switchPanel(index)`.

**Placeholder panels** for Manage Cars, Customers, Rentals, Reports show "under construction" until those GUIs are built.

**How to switch panels from code:**
```java
switchPanel(0); // Overview
switchPanel(1); // Manage Cars
switchPanel(2); // Manage Customers
switchPanel(3); // Manage Rentals
switchPanel(4); // Reports
```

---

### `CarDAO.java` (pckDatabase)
Full CRUD for the `cars` table. Used exclusively by `CarService` — never called directly from GUI.

**Methods:**
- `getAllCars()` — Returns all cars in inventory
- `getAvailableCars()` — Returns only `AVAILABLE` cars, ordered by brand/model
- `getCarById(int carId)` — Returns a single car or null
- `getCarsByCategory(String category)` — Filters by category
- `getCarsByStatus(String status)` — Filters by status
- `plateExists(String plateNumber)` — Duplicate check before insert
- `insertCar(Car car)` — Inserts new car, returns generated `car_id` or -1
- `updateCar(Car car)` — Updates all fields including `image_path`
- `updateCarStatus(int carId, String status)` — Status-only update
- `deleteCar(int carId)` — Deletes by ID (will fail if FK rental constraint exists)

**Additional method:**
- `getDistinctBrands()` — Returns sorted list of distinct brand names from `cars` table. Used by `CustomerDashboardGUI` dropdown to build the By Brand filter dynamically.

**Note:** All read methods use a shared `mapRow(ResultSet)` helper that reads all 13 columns including `fuel_type` and `color`. `insertCar()` uses 12 parameters (1-12). `updateCar()` uses 13 parameters (1-12 fields + 13 = car_id WHERE clause). Parameters are strictly sequential — no duplicates.

---

### `CarService.java` (pckServices)
Business logic layer for all car operations. Static utility class — never instantiated. All GUI classes must call this, never `CarDAO` directly.

**Methods mirror CarDAO but add validation:**
- `addCar(Car car)` — Validates all fields + checks duplicate plate before inserting
- `updateCar(Car car)` — Same validations, requires valid `carId`
- `updateCarStatus(int carId, String status)` — Validates status is one of `AVAILABLE`, `RENTED`, `MAINTENANCE`
- `deleteCar(int carId)` — Validates ID before deleting
- Read methods (`getAllCars`, `getAvailableCars`, `getCarById`, `getCarsByCategory`, `getCarsByStatus`) — pass-through to CarDAO
- `getDistinctBrands()` — pass-through to `carDAO.getDistinctBrands()`. Used by `CustomerDashboardGUI` for the dynamic brand dropdown.

---

### `BrowseCarsGUI.java` (pckCustomer)
Customer sub-panel for browsing cars. Extends `JPanel` — embedded in `CustomerDashboardGUI` via CardLayout (key: `BROWSE_CARS`).

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  "Browse Cars" + results count          [Refresh]    │  ← HEADER
├──────────────────────────────────────────────────────┤
│  🔍 Search bar                                       │  ← SEARCH
├─────────────────────────────────────┬────────────────┤
│  [ Card ][ Card ][ Card ][ Card ]   │  Detail Panel  │
│  [ Card ][ Card ][ Card ][ Card ]   │  (slide-out)   │  ← BODY
└─────────────────────────────────────┴────────────────┘
```

**Car Cards Grid:** 5-column responsive grid. Each card is 280px tall, white with rounded corners, drop shadow, and blue hover/selected border. Shows car image (or emoji fallback), car name, year/category, transmission/seats, a color swatch circle + color name, and daily rate.

**Color Display:** Two places show the car's color:
- **Card info swatch** — a small filled circle + color name text in the card info section
- **Detail panel spec row** — a "Color" row in the Specifications section with a 12px swatch circle next to the name

Note: The card image stripe was removed. Color is shown only via swatch dot + label.

**`parseColor(String)`** — Converts common car color name strings (e.g. `"Pearl White"`, `"Midnight Black"`, `"Azure Blue"`) to `java.awt.Color` values for rendering. Falls back to neutral gray for unrecognized names. Both the card stripe/swatch and detail row use this method.

**Detail Panel:** 540px wide slide-in panel on the right. Animated open/close (45px step, 8ms delay). Shows large car image, specs (plate, transmission, seats, category, **color**), daily rate breakdown with VAT (12%), availability status, and a "Rent Now" button (green if available, gray if not). "Rent Now" triggers the `onRentNow` callback set by `CustomerDashboardGUI`.

**Search:** Filters by brand, model, or plate number on Enter key.

**Image Loading (`loadImageRaw`):** Loads a raw `BufferedImage` from `assets/images/<filename>`. The `image_path` column in the DB stores the **filename only** (e.g. `honda-civic.png`). Images are painted directly inside `paintComponent` using **cover scaling** (`Math.max` scale factor) — fills the placeholder fully, preserves aspect ratio, crops excess. No pre-scaling or `JLabel` wrappers. Falls back to a category emoji on a plain `#EBEEf5` background if the file is missing or null. Rounded corners on card images are applied via `g2.setClip(RoundRectangle2D)` inside `paintComponent`.

**Stacked Filter System:** Multiple filters can be active simultaneously (AND logic). Filters are stored in `activeFilters` — a `LinkedHashMap<String, String>` mapping category → value. Each `filterBy...()` call adds to the stack without clearing others. `applyFilters()` applies all active filters together.

**Chip Row:** Sits inside the header below the title/results count (pinned via `BorderLayout.NORTH` + `BorderLayout.CENTER`). Hidden when no filters active. Each chip is white with a blue border and a `×` (`×`) button to remove just that filter. "Clear All" label appears when 2+ filters are active. Header max height is 90px.

**Live Search:** Fires on every keystroke via `DocumentListener` — no Enter key needed. Splits query into individual words (`split("\s+")`) and builds a single lowercase haystack from all visible card fields. Every word must appear somewhere in the haystack (AND logic). Searches: brand, model, year, category, transmission, fuel type, color, seat capacity, daily rate. Case-insensitive. Examples: "Toyota Red", "Red Toyota", "Diesel SUV 7" all work.

**Public filter methods:**
```java
filterByType(String type)
filterByBrand(String brand)
filterByPriceRange(String range)    // "Under ₱1,000" | "₱1,000 – ₱2,000" | "Above ₱2,000"
filterByTransmission(String trans)
filterByFuelType(String fuelType)   // "Gasoline" | "Diesel" | "Hybrid" | "Electric"
showAll()                            // clears all active filters
getActiveFilterForCategory(String category)  // returns active value for that category, or null
refresh()                            // reloads from DB
```

**Chip styling:** White background (`Color.WHITE`), `chip.setOpaque(true)`, blue border via `createCompoundBorder(LineBorder(CLR_BLUE), EmptyBorder(4,8,4,8))`. Text and `×` button both use `CLR_BLUE`. Uses `×` character for the close button.

**Fuel type display:** Shown in card spec line as `Automatic · Gasoline · 5 seats` and as a dedicated "Fuel Type" row in the detail panel specs section.

**Callback wiring in CustomerDashboardGUI:**
```java
browseCarsPanel.setOnRentNow(() -> switchPanel(1)); // index 1 = Make a Reservation
```

---

### `CustomerDashboardGUI.java` (pckCustomer)
Full dashboard for Customer. **1600 x 900**, resizable (minimum 1024 x 600).

**Layout:**
```
┌──────────────────────────────────────────────────┐
│              TITLE BAR (64px) — white            │
├──────────────────────────────────────────────────┤
│              MENU BAR (48px) — dark              │
├──────────────────────────────────────────────────┤
│                                                  │
│             CONTENT AREA (CardLayout)            │
│                                                  │
└──────────────────────────────────────────────────┘
```

**Title Bar (div2):** White, 64px tall. Left side has 4-color accent dots + "CarRentals — Customer Portal" label. Right side shows customer name (from SessionManager), "Customer" role label, and a green avatar circle with first initial.

**Menu Bar (div3):** Dark `#121212`, 48px tall. Horizontal nav buttons on the left: **Browse Cars, Make a Reservation, My Rentals, Payment**. Logout button pinned to the right. Active nav item has a blue background + light blue underline. Inactive items have hover effect. Note: there is no separate "Dashboard" panel — the app opens directly on Browse Cars.

**Dropdown Filter System:** Hovering over "Browse Cars" in the menu bar opens a `JWindow` dropdown below it with filter columns. Each column maps to a filter category with clickable item rows. Active filter for each column is indicated by a ✓ checkmark. Dropdown background is **white** with a light gray border `(220,220,220)`. Item text is near-black `(18,18,18)`, active item in blue `(37,99,235)`. Hover highlight is light blue `(240,245,255)`. Each column panel is white and opaque with a fixed `preferredSize` width of 140px. Item rows use `FlowLayout.CENTER`. Section headers use `SwingConstants.CENTER`.

**Dropdown columns and items:**
```
{"By Type",      "Sedan", "SUV", "MPV", "Van", "Pickup"}
{"By Brand",     — fetched dynamically from DB via CarService.getDistinctBrands() —}
{"By Price",     "Under ₱1,000", "₱1,000 – ₱2,000", "Above ₱2,000"}
{"Transmission", "Automatic", "Manual", "CVT"}
{"Fuel Type",    "Gasoline", "Diesel", "Hybrid", "Electric"}
```
Price range strings use en-dash `–` — must match `matchesPriceRange()` in BrowseCarsGUI exactly.
By Brand column is built dynamically every time the dropdown opens — new brands added to the DB appear automatically without code changes.

**Dropdown close logic:** Polling timer every 40ms. Waits until `mouseEnteredDropdown[0]` is true (mouse confirmed inside), then closes only when mouse leaves bounds. `mousePressed` used for item clicks (more reliable than `mouseClicked`).

**Content Area (CardLayout):** Swaps panels without creating new windows. Panel keys: `BROWSE_CARS`, `MAKE_RESERVATION`, `MY_RENTALS`, `PAYMENT`.

**Placeholder panels** for Make a Reservation, My Rentals, and Payment show "under construction" until those GUIs are built.

**How to switch panels from code:**
```java
switchPanel(0); // Browse Cars
switchPanel(1); // Make a Reservation
switchPanel(2); // My Rentals
switchPanel(3); // Payment
```

**`applyBrowseFilter(String category, String value)`** — Called by dropdown item clicks. Switches to Browse Cars panel then calls the appropriate `BrowseCarsGUI` filter method:
```java
case "By Type"      -> browseCarsPanel.filterByType(value);
case "By Brand"     -> browseCarsPanel.filterByBrand(value);
case "By Price"     -> browseCarsPanel.filterByPriceRange(value);
case "Transmission" -> browseCarsPanel.filterByTransmission(value);
case "Fuel Type"    -> browseCarsPanel.filterByFuelType(value);
```

---

## 🎨 Design System

All GUI files follow the same design language. **Always use these exact values** for consistency:

### Color Palette
```java
// Base
Color CLR_BG     = new Color(245, 245, 245); // Off-white background
Color CLR_WHITE  = Color.WHITE;
Color CLR_BLACK  = new Color(18, 18, 18);    // Near-black text
Color CLR_GRAY   = new Color(120, 120, 120); // Subtext
Color CLR_BORDER = new Color(220, 220, 220); // Dividers and outlines

// Sidebar / Menu Bar
Color CLR_SIDEBAR        = new Color(18, 18, 18);  // Dark background
Color CLR_SIDEBAR_HOVER  = new Color(32, 32, 32);
Color CLR_SIDEBAR_ACTIVE = new Color(37, 99, 235); // Blue highlight

// Accent Colors
Color CLR_BLUE   = new Color(37, 99, 235);
Color CLR_GREEN  = new Color(22, 163, 74);
Color CLR_YELLOW = new Color(234, 179, 8);
Color CLR_RED    = new Color(220, 38, 38);

// Light versions (for card backgrounds)
Color CLR_BLUE_LIGHT   = new Color(219, 234, 254);
Color CLR_GREEN_LIGHT  = new Color(220, 252, 231);
Color CLR_YELLOW_LIGHT = new Color(254, 249, 195);
Color CLR_RED_LIGHT    = new Color(254, 226, 226);
```

### Fonts (always Segoe UI)
```java
Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  20); // Page titles
Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12); // Sub-labels
Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,  12); // Form labels
Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 14); // Input fields
Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD,  14); // Buttons
Font FONT_NAV      = new Font("Segoe UI", Font.PLAIN, 13); // Nav items
Font FONT_NAV_BOLD = new Font("Segoe UI", Font.BOLD,  13); // Active nav
Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11); // Fine print
```

### Window Sizes
| Screen | Size | Resizable |
|--------|------|-----------|
| LoginGUI | 1200 x 800 | No |
| AdminDashboardGUI | 1600 x 900 | Yes (min 1024x600) |
| CustomerDashboardGUI | 1600 x 900 | Yes (min 1024x600) |
| Sub-panels (Manage Cars, etc.) | Embedded — no separate window | — |

---

## 🔄 Application Flow

```
Main.java
  └── LoginGUI
        └── AuthService.login(email, password)
              └── UserDAO.getUserByEmailAndPassword()
                    └── MySQL → users table
              └── SessionManager.setCurrentUser(user)
        └── user.isAdmin() ?
              ├── TRUE  → AdminDashboardGUI
              └── FALSE → CustomerDashboardGUI

AdminDashboardGUI
  └── Sidebar click → switchPanel(index) → CardLayout shows panel
  └── Logout → AuthService.logout() → LoginGUI

CustomerDashboardGUI
  └── Menu bar click → switchPanel(index) → CardLayout shows panel
  └── Logout → AuthService.logout() → LoginGUI
```

---

## 📐 Architecture Pattern

The project follows a **layered architecture:**

| Layer | Package | Responsibility |
|-------|---------|---------------|
| GUI | pckMain, pckAdmin, pckCustomer | All Swing UI — no DB logic here |
| Services | pckServices | Business logic, validation, calculations |
| DAO | pckDatabase | All SQL queries — one class per table |
| Models | pckModels | Plain Java objects mirroring DB tables |
| Utils | pckUtils | Shared helpers (session, validation, dates) |

**Rule:** GUI classes should never talk to DAO classes directly. Always go through a Service class in between.

```
GUI → Service → DAO → Database
```

---

## 🔲 What Still Needs To Be Built

### Models (pckModels)
- `Customer.java` — extends User, adds phone, address, licenseNumber, customerId
- `Admin.java` — extends User (can be minimal, just the constructor)
- `Rental.java` — fields: rentalId, customerId, carId, startDate, endDate, totalAmount, status
- `Payment.java` — fields: paymentId, rentalId, amountPaid, paymentDate, paymentMethod, status

### DAOs (pckDatabase)
- `CustomerDAO.java` — CRUD for customers table
- `RentalDAO.java` — CRUD + get by customer, get active rentals
- `PaymentDAO.java` — Insert payment, get by rental

### Services (pckServices)
- `RentalService.java` — Calculate total amount (days × daily rate), validate dates, check car availability
- `PaymentService.java` — Process payment, calculate late fees
- `ReportService.java` — Aggregate queries for reports

### Admin Sub-panels (pckAdmin)
- `ManageCarsGUI.java` — Table of cars + Add/Edit/Delete + status filter
- `ManageCustomersGUI.java` — Table of customers + Add/Edit/Delete
- `ManageRentalsGUI.java` — Table of rentals + status management
- `ReportsGUI.java` — Summary stats, revenue reports

### Customer Sub-panels (pckCustomer)
- `MakeReservationGUI.java` — Date picker + car selection + confirmation
- `MyRentalsGUI.java` — Customer's rental history table
- `PaymentGUI.java` — Payment form

### Utils (pckUtils)
- `ValidationUtil.java` — Email format, empty fields, date range checks
- `DateUtil.java` — Date formatting, calculate days between dates
- `Constants.java` — App-wide constants (DB name, default rates, date formats)

---

## 🔧 GitHub Workflow for the Group

Always follow this order every session:
```bash
# Before coding
git pull origin main

# After coding
git add .
git commit -m "add: brief description of what you did"
git push origin main
```

### Commit Message Prefixes
| Prefix | Use for |
|--------|---------|
| `add:` | New files or features |
| `fix:` | Bug fixes |
| `update:` | Modifying existing code |
| `remove:` | Deleting unused code |
| `docs:` | README or documentation |

### ⚠️ Before Every Push
Make sure `DatabaseConnection.java` credentials are replaced with placeholders, OR uncheck the file in GitHub Desktop before committing.

---

## 💡 Tips for Asking Claude for Help

When asking Claude to help with a specific file, always mention:
1. **Which file** you're working on and its package
2. **What layer** it belongs to (GUI / Service / DAO / Model)
3. **What it connects to** (e.g. "CarDAO needs to talk to the cars table and be called by RentalService")
4. **The design system** — remind Claude to use the color palette and fonts listed above for any GUI work

**Example prompt addition:**
> "I'm working on `CarDAO.java` in `pckDatabase`. It should connect using `DatabaseConnection.getInstance().getConnection()` and handle CRUD operations for the `cars` table. The model class is `Car.java` in `pckModels`."