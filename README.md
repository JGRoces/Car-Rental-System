# Car Rental System

A desktop-based **Car Rental Management System** built with **Java** and **MySQL**, developed as a group project. The system supports three types of users — **Admin**, **Customer**, and **Driver** — each with their own dedicated interface and features.

---

## Project Overview

This system allows customers to browse available cars and make reservations, drivers to register and manage their availability, and admins to oversee the entire operation — managing vehicles, customers, drivers, rentals, payments, and reports.

### Actors

| Actor | Description |
|-------|-------------|
| **Admin** | Manages cars, customers, drivers, rentals, payments, and generates reports |
| **Customer** | Browses available cars, makes reservations, and tracks rental history |
| **Driver** | Registers for the platform, awaits admin verification, and manages their profile |

---

## Authors

| Name | GitHub Username | Role |
|------|----------------|------|
| Joseph Gabriel A. Roces | @JGR-Dev-Scholar | Quality Engineer |
| Sean Patrick Brix V. Salamera | @aseanpat | Data Engineer |
| Marvin Karl R. Sangco | @Marvin-Sangco | SWE - BackEnd |
| Leonard Vincent L. Camat | @Pan-2006 | SWE - FrontEnd |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Programming Language | Java |
| GUI Framework | Java Swing |
| Database | MySQL |
| JDBC Driver | mysql-connector-j-9.6.0 (included in `/lib`) |
| IDE | VSCode |
| Version Control | Git & GitHub |

---

## Project Structure

```
Car-Rental-System/
├── assets/
│   ├── defaults/                       # Fallback images (avatar, vehicle)
│   ├── icons/
│   │   ├── nav/                        # Sidebar navigation icons
│   │   └── action/                     # Button/action icons
│   └── images/                         # Branding (logo, showcase)
│
├── database/
│   └── car_rental_db.sql               # Full database schema + seed data
│
├── lib/
│   └── mysql-connector-j-9.6.0.jar     # JDBC driver (pre-included)
│
└── src/
    ├── pckMain/
    │   ├── Main.java                   # Entry point — launches LoginGUI on EDT
    │   ├── LoginGUI.java               # Shared login screen for all roles
    │   └── SignUpChoiceGUI.java        # Role selection before sign-up
    │
    ├── pckModels/
    │   ├── User.java                   # Base class (userId, name, email, role)
    │   ├── Admin.java                  # Extends User
    │   ├── Customer.java               # Extends User (phone, address, photoPath)
    │   ├── Driver.java                 # Extends User (license, status: PENDING/VERIFIED/REJECTED)
    │   ├── Car.java                    # Vehicle data (type, plate, rate, status)
    │   ├── Rental.java                 # Rental record (dates, car, customer, driver)
    │   └── Payment.java                # Payment record linked to a rental
    │
    ├── pckDatabase/
    │   ├── DatabaseConnection.java     # Singleton MySQL connection
    │   ├── UserDAO.java                # Auth queries (login by email + password)
    │   ├── CustomerDAO.java            # Customer CRUD
    │   ├── DriverDAO.java              # Driver CRUD + status updates
    │   ├── CarDAO.java                 # Vehicle CRUD
    │   ├── RentalDAO.java              # Rental CRUD
    │   └── PaymentDAO.java             # Payment CRUD
    │
    ├── pckServices/
    │   ├── AuthService.java            # Login/logout logic → routes to correct dashboard
    │   ├── CustomerService.java        # Customer registration + validation
    │   ├── DriverService.java          # Driver registration + admin verify/reject
    │   ├── CarService.java             # Vehicle availability + management logic
    │   └── RentalService.java          # Booking, return, and pricing logic
    │
    ├── pckAdmin/
    │   ├── AdminDashboardGUI.java      # Main admin window (sidebar + content area)
    │   ├── panels/
    │   │   ├── OverviewPanel.java      # Dashboard summary cards and stats
    │   │   ├── ManagementPanel.java    # Tab container for all management views
    │   │   ├── PaymentsPanel.java      # Payment records and history
    │   │   ├── ReportsPanel.java       # Report generation
    │   │   ├── AccountPanel.java       # Admin account settings
    │   │   └── SettingsPanel.java      # Application-level settings
    │   ├── tabs/
    │   │   ├── VehiclesTab.java        # Vehicle listing inside ManagementPanel
    │   │   ├── CustomersTab.java       # Customer listing inside ManagementPanel
    │   │   ├── DriversTab.java         # Driver listing + verify/reject actions
    │   │   └── RentalsTab.java         # Rental records inside ManagementPanel
    │   ├── vehicle/
    │   │   ├── AddVehiclePanel.java    # Form to add a new vehicle
    │   │   ├── EditVehiclePanel.java   # Form to edit an existing vehicle
    │   │   └── RemoveVehiclePanel.java # Confirmation panel to delete a vehicle
    │   └── shared/
    │       └── AdminUIHelper.java      # Shared UI builders (tables, buttons, avatars)
    │
    ├── pckCustomer/
    │   ├── CustomerDashboardGUI.java   # Main customer window
    │   ├── CustomerSignUpGUI.java      # Customer registration form
    │   ├── BrowseCarsGUI.java          # Available cars grid/list view
    │   └── MakeReservationPanel.java   # Date picker + booking confirmation
    │
    ├── pckDriver/
    │   ├── DriverDashboardGUI.java     # Driver home screen (status, profile)
    │   └── DriverSignUpGUI.java        # Driver registration form (license, photo)
    │
    └── pckUtils/
        ├── AppConfig.java              # Central file paths (assets + upload folders)
        ├── SessionManager.java         # Tracks currently logged-in user (singleton)
        ├── ValidationUtil.java         # Reusable input validation helpers
        ├── DateUtil.java               # Date formatting and calculation helpers
        ├── CalendarPicker.java         # Custom date picker component
        ├── CustomTitleBar.java         # Custom frameless window title bar
        ├── ConstantsUtil.java          # App-wide string/int constants
        └── UIAssets.java               # Icon and image loading helpers
```

---

## Key Features

**Admin**
- Overview dashboard with summary statistics
- Add, edit, and remove vehicles (with photo upload)
- View and manage all customer accounts
- Verify or reject driver applications
- Track all rentals and payment records
- Generate system reports

**Customer**
- Register and log in with a personal account
- Browse available vehicles with photos and rates
- Make reservations with a date picker
- View rental and payment history

**Driver**
- Register with license details and a profile photo
- Account starts as `PENDING` until approved by an admin
- Once `VERIFIED`, driver can log in and view their dashboard
- Admin can also `REJECT` an application

---

## Setup Instructions

### Prerequisites
- Java JDK 17 or later
- MySQL Server 8.0 or later
- IntelliJ IDEA or Eclipse
- The JDBC driver is already included at `lib/mysql-connector-j-9.6.0.jar` — no separate download needed

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR-USERNAME/car-rental-system.git
cd Car-Rental-System
```

### 2. Set Up the Database
Open MySQL and run the provided SQL file:
```bash
mysql -u root -p < database/car_rental_db.sql
```
This creates the `car_rental_db` database and all tables automatically.

### 3. Configure the Database Connection
Open `src/pckDatabase/DatabaseConnection.java` and update your local credentials:
```java
private static final String URL  = "jdbc:mysql://localhost:3306/car_rental_db";
private static final String USER = "your_mysql_username";
private static final String PASS = "your_mysql_password";
```
> ⚠️ Never commit your credentials to GitHub. `DatabaseConnection.java` is listed in `.gitignore`.

### 4. Add the JDBC Driver to Your IDE
- **IntelliJ IDEA**: `File → Project Structure → Libraries → + → Java` → select `lib/mysql-connector-j-9.6.0.jar`
- **Eclipse**: Right-click project → `Build Path → Add External Archives` → select the same `.jar`

### 5. Run the Application
Locate and run `src/pckMain/Main.java`. The login window will open, and all upload folders will be created automatically on first launch (handled by `AppConfig.init()`).

---

## Upload Folder Structure

User photos and vehicle images are stored **outside** the project directory to keep them out of Git. On first run, `AppConfig.init()` creates the following structure automatically beside the project folder:

```
../car-rental-uploads/
├── users/
│   ├── customers/          # customer_<id>.png
│   └── drivers/            # driver_<id>.png
└── vehicles/
    ├── sedan/
    ├── suv/
    ├── coupe/
    ├── hatchback/
    ├── van/
    ├── truck/
    └── motorcycle/         # vehicle_<id>.<ext>
```

---

## Git Workflow (For Group Members)

To avoid conflicts, always follow this order:

```bash
# Before coding
git pull origin main

# After coding
git add .
git commit -m "brief description of what you did"
git push origin main
```

> Coordinate with the team on who is working on which file to avoid merge conflicts.
