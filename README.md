# 🚗 Car Rental System

A desktop-based **Car Rental Management System** built with **Java** and **MySQL**, developed as a group project. The system supports two types of users — **Admin** and **Customer** — each with their own dedicated interface and features.

---

## 👥 Group Members

| Name | GitHub Username | Role |
|------|----------------|------|
| Joseph Gabriel A. Roces | @JGR-Dev-Scholar | Quality Engineer |
| Sean Patrick Brix V. Salamera | @username | Data Engineer |
| Marvin Karl R. Sangco | @username | SWE - BackEnd |
| Leonard Vincent L. Camat | @username | SWE - FrontEnd |

> Fill in your names, GitHub usernames, and assigned roles as a group.

---

## 📋 Project Overview

This system allows customers to browse available cars, make reservations, and manage their rentals. Admins can manage the car inventory, oversee customer accounts, handle rentals, and generate reports.

### Actors
- **Admin** — Manages cars, customers, rentals, and generates reports
- **Customer** — Browses cars, makes reservations, and tracks rental history

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Programming Language | Java |
| GUI Framework | Java Swing |
| Database | MySQL |
| IDE | IntelliJ IDEA / Eclipse |
| Version Control | Git & GitHub |

---

## 📁 Project Structure

```
src/
├── pckMain/
│   ├── Main.java                        // Entry point
│   └── LoginGUI.java                    // Shared login screen
│
├── pckModels/
│   ├── User.java                        // Base user class
│   ├── Customer.java                    // Extends User
│   ├── Admin.java                       // Extends User
│   ├── Car.java
│   ├── Rental.java
│   └── Payment.java
│
├── pckDatabase/
│   ├── DatabaseConnection.java          // Singleton DB connection
│   ├── UserDAO.java                     // Auth queries
│   ├── CustomerDAO.java
│   ├── CarDAO.java
│   ├── RentalDAO.java
│   └── PaymentDAO.java
│
├── pckAdmin/
│   ├── AdminDashboardGUI.java
│   ├── ManageCarsGUI.java
│   ├── ManageCustomersGUI.java
│   ├── ManageRentalsGUI.java
│   └── ReportsGUI.java
│
├── pckCustomer/
│   ├── CustomerDashboardGUI.java
│   ├── BrowseCarsGUI.java
│   ├── MyRentalsGUI.java
│   ├── MakeReservationGUI.java
│   └── PaymentGUI.java
│
├── pckServices/
│   ├── AuthService.java
│   ├── RentalService.java
│   ├── PaymentService.java
│   └── ReportService.java
│
└── pckUtils/
    ├── ValidationUtil.java
    ├── DateUtil.java
    ├── Constants.java
    └── SessionManager.java
```

---

## ⚙️ Setup Instructions

### Prerequisites
- Java JDK 17 or later
- MySQL Server 8.0 or later
- IntelliJ IDEA or Eclipse
- MySQL JDBC Driver (`mysql-connector-j`)

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR-USERNAME/car-rental-system.git
cd car-rental-system
```

### 2. Set Up the Database
- Open MySQL and create a new database:
```sql
CREATE DATABASE car_rental_db;
```
- Import the provided SQL file:
```bash
mysql -u root -p car_rental_db < database/car_rental_db.sql
```

### 3. Configure the Database Connection
- Open `src/pckDatabase/DatabaseConnection.java`
- Update the following fields with your local MySQL credentials:
```java
private static final String URL = "jdbc:mysql://localhost:3306/car_rental_db";
private static final String USER = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";
```
> ⚠️ Never push your credentials to GitHub. This file is listed in `.gitignore`.

### 4. Add the JDBC Driver
- Download `mysql-connector-j` from [MySQL official site](https://dev.mysql.com/downloads/connector/j/)
- Add it to your project's build path / libraries in your IDE

### 5. Run the Application
- Locate and run `src/pckMain/Main.java`

---

## 🔄 Git Workflow (For Group Members)

To avoid conflicts, always follow this order:

```bash
# Before coding
git pull origin main

# After coding
git add .
git commit -m "brief description of what you did"
git push origin main
```

> 📌 Coordinate with your team on who is working on which file to avoid merge conflicts.

---

## 📌 Project Status

- [x] Conceptualization & Diagrams
- [x] File Structure Planning
- [ ] Database Schema
- [ ] Model Classes
- [ ] Database Connection & DAO Layer
- [ ] Service Layer
- [ ] Admin GUI
- [ ] Customer GUI
- [ ] Testing

---

## 📄 License

This project is for academic purposes only.
