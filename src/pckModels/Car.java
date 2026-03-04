package pckModels;

import java.math.BigDecimal;

/**
 * Car.java
 * Represents a car in the inventory.
 * Maps to the `cars` table.
 *
 * category ENUM : Sedan, SUV, Van, Truck
 * status   ENUM : AVAILABLE, RENTED, MAINTENANCE
 */
public class Car {

    private int        carId;
    private String     brand;
    private String     model;
    private int        year;
    private String     plateNumber;
    private String     category;
    private BigDecimal dailyRate;
    private String     status;

    // New car — before DB insert
    public Car(String brand, String model, int year, String plateNumber,
               String category, BigDecimal dailyRate) {
        this.brand       = brand;
        this.model       = model;
        this.year        = year;
        this.plateNumber = plateNumber;
        this.category    = category;
        this.dailyRate   = dailyRate;
        this.status      = "AVAILABLE"; // default
    }

    // Loaded from DB — has carId and status
    public Car(int carId, String brand, String model, int year, String plateNumber,
               String category, BigDecimal dailyRate, String status) {
        this.carId       = carId;
        this.brand       = brand;
        this.model       = model;
        this.year        = year;
        this.plateNumber = plateNumber;
        this.category    = category;
        this.dailyRate   = dailyRate;
        this.status      = status;
    }

    public int        getCarId()       { return carId;       }
    public String     getBrand()       { return brand;       }
    public String     getModel()       { return model;       }
    public int        getYear()        { return year;        }
    public String     getPlateNumber() { return plateNumber; }
    public String     getCategory()    { return category;    }
    public BigDecimal getDailyRate()   { return dailyRate;   }
    public String     getStatus()      { return status;      }

    public void setCarId(int carId)              { this.carId       = carId;       }
    public void setBrand(String brand)           { this.brand       = brand;       }
    public void setModel(String model)           { this.model       = model;       }
    public void setYear(int year)                { this.year        = year;        }
    public void setPlateNumber(String plate)     { this.plateNumber = plate;       }
    public void setCategory(String category)     { this.category    = category;    }
    public void setDailyRate(BigDecimal rate)    { this.dailyRate   = rate;        }
    public void setStatus(String status)         { this.status      = status;      }

    // Convenience — display name for dropdowns/tables
    public String getDisplayName() {
        return year + " " + brand + " " + model + " (" + plateNumber + ")";
    }

    @Override
    public String toString() {
        return "Car{carId=" + carId + ", brand='" + brand + "', model='" + model +
               "', year=" + year + ", plate='" + plateNumber + "', category='" +
               category + "', dailyRate=" + dailyRate + ", status='" + status + "'}";
    }
}