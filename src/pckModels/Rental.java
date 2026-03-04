package pckModels;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Rental.java
 * Represents a rental transaction.
 * Maps to the `rentals` table.
 *
 * status ENUM : PENDING, ACTIVE, COMPLETED, CANCELLED
 */
public class Rental {

    private int        rentalId;
    private int        customerId;
    private int        carId;
    private LocalDate  startDate;
    private LocalDate  endDate;
    private BigDecimal totalAmount;
    private String     status;

    // New rental — before DB insert
    public Rental(int customerId, int carId, LocalDate startDate, LocalDate endDate) {
        this.customerId  = customerId;
        this.carId       = carId;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.totalAmount = BigDecimal.ZERO; // calculated by RentalService
        this.status      = "PENDING";       // default
    }

    // Loaded from DB — has rentalId, totalAmount, and status
    public Rental(int rentalId, int customerId, int carId,
                  LocalDate startDate, LocalDate endDate,
                  BigDecimal totalAmount, String status) {
        this.rentalId    = rentalId;
        this.customerId  = customerId;
        this.carId       = carId;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.totalAmount = totalAmount;
        this.status      = status;
    }

    public int        getRentalId()    { return rentalId;    }
    public int        getCustomerId()  { return customerId;  }
    public int        getCarId()       { return carId;       }
    public LocalDate  getStartDate()   { return startDate;   }
    public LocalDate  getEndDate()     { return endDate;     }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String     getStatus()      { return status;      }

    public void setRentalId(int rentalId)          { this.rentalId    = rentalId;    }
    public void setCustomerId(int customerId)      { this.customerId  = customerId;  }
    public void setCarId(int carId)                { this.carId       = carId;       }
    public void setStartDate(LocalDate startDate)  { this.startDate   = startDate;   }
    public void setEndDate(LocalDate endDate)      { this.endDate     = endDate;     }
    public void setTotalAmount(BigDecimal amount)  { this.totalAmount = amount;      }
    public void setStatus(String status)           { this.status      = status;      }

    @Override
    public String toString() {
        return "Rental{rentalId=" + rentalId + ", customerId=" + customerId +
               ", carId=" + carId + ", startDate=" + startDate +
               ", endDate=" + endDate + ", totalAmount=" + totalAmount +
               ", status='" + status + "'}";
    }
}