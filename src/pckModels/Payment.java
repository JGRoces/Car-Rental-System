package pckModels;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment.java
 * Represents a payment record tied to a rental.
 * Maps to the `payments` table.
 *
 * paymentMethod ENUM : CASH, CARD, ONLINE
 * status        ENUM : PAID, PENDING, REFUNDED
 */
public class Payment {

    private int           paymentId;
    private int           rentalId;
    private BigDecimal    amountPaid;
    private LocalDateTime paymentDate;
    private String        paymentMethod;
    private String        status;

    // New payment — before DB insert
    public Payment(int rentalId, BigDecimal amountPaid, String paymentMethod) {
        this.rentalId      = rentalId;
        this.amountPaid    = amountPaid;
        this.paymentMethod = paymentMethod;
        this.status        = "PENDING"; // default
    }

    // Loaded from DB — has paymentId, paymentDate, and status
    public Payment(int paymentId, int rentalId, BigDecimal amountPaid,
                   LocalDateTime paymentDate, String paymentMethod, String status) {
        this.paymentId     = paymentId;
        this.rentalId      = rentalId;
        this.amountPaid    = amountPaid;
        this.paymentDate   = paymentDate;
        this.paymentMethod = paymentMethod;
        this.status        = status;
    }

    public int           getPaymentId()     { return paymentId;     }
    public int           getRentalId()      { return rentalId;      }
    public BigDecimal    getAmountPaid()    { return amountPaid;    }
    public LocalDateTime getPaymentDate()   { return paymentDate;   }
    public String        getPaymentMethod() { return paymentMethod; }
    public String        getStatus()        { return status;        }

    public void setPaymentId(int paymentId)              { this.paymentId     = paymentId;     }
    public void setRentalId(int rentalId)                { this.rentalId      = rentalId;      }
    public void setAmountPaid(BigDecimal amountPaid)     { this.amountPaid    = amountPaid;    }
    public void setPaymentDate(LocalDateTime date)       { this.paymentDate   = date;          }
    public void setPaymentMethod(String paymentMethod)   { this.paymentMethod = paymentMethod; }
    public void setStatus(String status)                 { this.status        = status;        }

    @Override
    public String toString() {
        return "Payment{paymentId=" + paymentId + ", rentalId=" + rentalId +
               ", amountPaid=" + amountPaid + ", method='" + paymentMethod +
               "', status='" + status + "'}";
    }
}