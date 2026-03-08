package pckDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pckModels.Car;

/**
 * CarDAO.java
 * Data Access Object for the `cars` table.
 * All SQL queries related to cars go here — no business logic.
 *
 * Used by: CarService.java
 *
 * Table: cars
 *   car_id        INT AUTO_INCREMENT PK
 *   brand         VARCHAR(50)
 *   model         VARCHAR(50)
 *   year          YEAR
 *   plate_number  VARCHAR(20) UNIQUE
 *   category      ENUM('Sedan','SUV','Van','Truck','Pickup','Coupe','Minivan')
 *   transmission  ENUM('Automatic','Manual')
 *   seat_capacity INT
 *   daily_rate    DECIMAL(10,2)
 *   status        ENUM('AVAILABLE','RENTED','MAINTENANCE')
 *   image_path    VARCHAR(255)
 *   color         VARCHAR(30)
 *   created_at    TIMESTAMP
 */
public class CarDAO {

    // ─────────────────────────────────────────────
    //  Shared helper — map a ResultSet row to a Car
    // ─────────────────────────────────────────────
    private Car mapRow(ResultSet rs) throws SQLException {
        return new Car(
            rs.getInt("car_id"),
            rs.getString("brand"),
            rs.getString("model"),
            rs.getInt("year"),
            rs.getString("plate_number"),
            rs.getString("category"),
            rs.getString("transmission"),
            rs.getInt("seat_capacity"),
            rs.getBigDecimal("daily_rate"),
            rs.getString("status"),
            rs.getString("image_path"),
            rs.getString("color")
        );
    }

    // ====================================================
    //  READ — Get all cars
    // ====================================================
    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                cars.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in getAllCars(): " + e.getMessage());
            e.printStackTrace();
        }
        return cars;
    }

    // ====================================================
    //  READ — Get only available cars
    // ====================================================
    public List<Car> getAvailableCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars WHERE status = 'AVAILABLE' ORDER BY brand, model";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cars.add(mapRow(rs));
            }
            System.out.println("[CarDAO] getAvailableCars() → " + cars.size() + " rows");

        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in getAvailableCars(): " + e.getMessage());
            e.printStackTrace();
        }
        return cars;
    }

    // ====================================================
    //  READ — Get one car by ID
    // ====================================================
    public Car getCarById(int carId) {
        String sql = "SELECT * FROM cars WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[CarDAO] getCarById(" + carId + ") → found");
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in getCarById(): " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[CarDAO] getCarById(" + carId + ") → not found");
        return null;
    }

    // ====================================================
    //  READ — Get cars by category
    // ====================================================
    public List<Car> getCarsByCategory(String category) {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars WHERE category = ? ORDER BY brand, model";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cars.add(mapRow(rs));
            }
            System.out.println("[CarDAO] getCarsByCategory(" + category + ") → " + cars.size() + " rows");

        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in getCarsByCategory(): " + e.getMessage());
            e.printStackTrace();
        }
        return cars;
    }

    // ====================================================
    //  READ — Get cars by status
    // ====================================================
    public List<Car> getCarsByStatus(String status) {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars WHERE status = ? ORDER BY brand, model";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cars.add(mapRow(rs));
            }
            System.out.println("[CarDAO] getCarsByStatus(" + status + ") → " + cars.size() + " rows");

        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in getCarsByStatus(): " + e.getMessage());
            e.printStackTrace();
        }
        return cars;
    }

    // ====================================================
    //  READ — Check if plate number already exists
    // ====================================================
    public boolean plateExists(String plateNumber) {
        String sql = "SELECT COUNT(*) FROM cars WHERE plate_number = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plateNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in plateExists(): " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ====================================================
    //  CREATE — Insert a new car
    //  Returns the generated car_id, or -1 on failure
    // ====================================================
    public int insertCar(Car car) {
        String sql = """
            INSERT INTO cars
                (brand, model, year, plate_number, category,
                 transmission, seat_capacity, daily_rate, status, image_path, color)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1,     car.getBrand());
            ps.setString(2,     car.getModel());
            ps.setInt(3,        car.getYear());
            ps.setString(4,     car.getPlateNumber());
            ps.setString(5,     car.getCategory());
            ps.setString(6,     car.getTransmission());
            ps.setInt(7,        car.getSeatCapacity());
            ps.setBigDecimal(8, car.getDailyRate());
            ps.setString(9,     car.getStatus());
            ps.setString(10,    car.getImagePath());
            ps.setString(11,    car.getColor());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        System.out.println("[CarDAO] insertCar() → car_id = " + id);
                        return id;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in insertCar(): " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // ====================================================
    //  UPDATE — Update all fields of an existing car
    //  Returns true on success
    // ====================================================
    public boolean updateCar(Car car) {
        String sql = """
            UPDATE cars SET
                brand         = ?,
                model         = ?,
                year          = ?,
                plate_number  = ?,
                category      = ?,
                transmission  = ?,
                seat_capacity = ?,
                daily_rate    = ?,
                status        = ?,
                image_path    = ?,
                color         = ?
            WHERE car_id = ?
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,     car.getBrand());
            ps.setString(2,     car.getModel());
            ps.setInt(3,        car.getYear());
            ps.setString(4,     car.getPlateNumber());
            ps.setString(5,     car.getCategory());
            ps.setString(6,     car.getTransmission());
            ps.setInt(7,        car.getSeatCapacity());
            ps.setBigDecimal(8, car.getDailyRate());
            ps.setString(9,     car.getStatus());
            ps.setString(10,    car.getImagePath());
            ps.setString(11,    car.getColor());
            ps.setInt(12,       car.getCarId());

            int rows = ps.executeUpdate();
            System.out.println("[CarDAO] updateCar(id=" + car.getCarId() + ") → " + rows + " row(s) updated");
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in updateCar(): " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ====================================================
    //  UPDATE — Change only the status of a car
    //  Returns true on success
    // ====================================================
    public boolean updateCarStatus(int carId, String status) {
        String sql = "UPDATE cars SET status = ? WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2,    carId);

            int rows = ps.executeUpdate();
            System.out.println("[CarDAO] updateCarStatus(id=" + carId + ", status=" + status + ") → " + rows + " row(s)");
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in updateCarStatus(): " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ====================================================
    //  DELETE — Remove a car by ID
    //  Returns true on success
    //  ⚠ Will fail if the car has linked rentals (FK constraint)
    // ====================================================
    public boolean deleteCar(int carId) {
        String sql = "DELETE FROM cars WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, carId);
            int rows = ps.executeUpdate();
            System.out.println("[CarDAO] deleteCar(id=" + carId + ") → " + rows + " row(s) deleted");
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[CarDAO] ERROR in deleteCar(): " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}