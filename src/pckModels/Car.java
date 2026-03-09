package pckModels;

import java.math.BigDecimal;

/**
 * Car.java
 * Represents a car in the inventory.
 * Maps to the `cars` table.
 *
 * category     ENUM : Sedan, SUV, Van, Truck, Pickup, Coupe, Minivan, MPV
 * transmission ENUM : Automatic, Manual, CVT
 * fuel_type    ENUM : Gasoline, Diesel, Hybrid, Electric
 * status       ENUM : AVAILABLE, RENTED, MAINTENANCE
 */
public class Car {

    private int        carId;
    private String     brand;
    private String     model;
    private int        year;
    private String     plateNumber;
    private String     category;
    private String     transmission;
    private String     fuelType;
    private int        seatCapacity;
    private BigDecimal dailyRate;
    private String     status;
    private String     imagePath;
    private String     color;

    // -------------------------
    // Constructor — New car (before DB insert, no carId yet)
    // -------------------------
    public Car(String brand, String model, int year, String plateNumber,
               String category, String transmission, String fuelType, int seatCapacity,
               BigDecimal dailyRate) {
        this.brand        = brand;
        this.model        = model;
        this.year         = year;
        this.plateNumber  = plateNumber;
        this.category     = category;
        this.transmission = transmission;
        this.fuelType     = fuelType;
        this.seatCapacity = seatCapacity;
        this.dailyRate    = dailyRate;
        this.status       = "AVAILABLE";
        this.imagePath    = null;
        this.color        = null;
    }

    // -------------------------
    // Constructor — Loaded from DB (has carId, status, imagePath, color)
    // -------------------------
    public Car(int carId, String brand, String model, int year, String plateNumber,
               String category, String transmission, String fuelType, int seatCapacity,
               BigDecimal dailyRate, String status, String imagePath, String color) {
        this.carId        = carId;
        this.brand        = brand;
        this.model        = model;
        this.year         = year;
        this.plateNumber  = plateNumber;
        this.category     = category;
        this.transmission = transmission;
        this.fuelType     = fuelType;
        this.seatCapacity = seatCapacity;
        this.dailyRate    = dailyRate;
        this.status       = status;
        this.imagePath    = imagePath;
        this.color        = color;
    }

    // -------------------------
    // Getters
    // -------------------------
    public int        getCarId()        { return carId;        }
    public String     getBrand()        { return brand;        }
    public String     getModel()        { return model;        }
    public int        getYear()         { return year;         }
    public String     getPlateNumber()  { return plateNumber;  }
    public String     getCategory()     { return category;     }
    public String     getTransmission() { return transmission; }
    public String     getFuelType()     { return fuelType;     }
    public int        getSeatCapacity() { return seatCapacity; }
    public BigDecimal getDailyRate()    { return dailyRate;    }
    public String     getStatus()       { return status;       }
    public String     getImagePath()    { return imagePath;    }
    public String     getColor()        { return color;        }

    // -------------------------
    // Setters
    // -------------------------
    public void setCarId(int carId)                  { this.carId        = carId;        }
    public void setBrand(String brand)               { this.brand        = brand;        }
    public void setModel(String model)               { this.model        = model;        }
    public void setYear(int year)                    { this.year         = year;         }
    public void setPlateNumber(String plateNumber)   { this.plateNumber  = plateNumber;  }
    public void setCategory(String category)         { this.category     = category;     }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public void setFuelType(String fuelType)         { this.fuelType     = fuelType;     }
    public void setSeatCapacity(int seatCapacity)    { this.seatCapacity = seatCapacity; }
    public void setDailyRate(BigDecimal dailyRate)   { this.dailyRate    = dailyRate;    }
    public void setStatus(String status)             { this.status       = status;       }
    public void setImagePath(String imagePath)       { this.imagePath    = imagePath;    }
    public void setColor(String color)               { this.color        = color;        }

    // -------------------------
    // Convenience Methods
    // -------------------------

    /** Full display name — used in dropdowns and table rows */
    public String getDisplayName() {
        return year + " " + brand + " " + model + " (" + plateNumber + ")";
    }

    /** Short display name — used in car cards */
    public String getShortName() {
        return brand + " " + model;
    }

    /** Returns true if car can be rented right now */
    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    @Override
    public String toString() {
        return "Car{" +
               "carId="           + carId        +
               ", brand='"        + brand        + '\'' +
               ", model='"        + model        + '\'' +
               ", year="          + year         +
               ", plate='"        + plateNumber  + '\'' +
               ", category='"     + category     + '\'' +
               ", transmission='" + transmission + '\'' +
               ", fuelType='"     + fuelType     + '\'' +
               ", seatCapacity="  + seatCapacity +
               ", dailyRate="     + dailyRate    +
               ", status='"       + status       + '\'' +
               ", imagePath='"    + imagePath    + '\'' +
               ", color='"        + color        + '\'' +
               '}';
    }
}