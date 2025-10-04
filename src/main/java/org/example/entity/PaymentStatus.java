package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

@Data
@AllArgsConstructor
public class PaymentStatus {
    private long id;
    private long paymentId;
    private Status status;
    private int retries;
    private String history;
    private Instant createdAt;
    private Instant updatedAt;

    public enum Status {
        CREATED, PROCESSING, RETRY, PROCESSED, NOTIFYING, RETRY_NOTIFY, NOTIFIED, ERROR
    }

    public static PaymentStatus fromResultSet(ResultSet rs) throws SQLException {
        return new PaymentStatus(
                rs.getLong("id"),
                rs.getLong("payment_id"),
                Status.valueOf(rs.getString("status")),
                rs.getInt("retries"),
                rs.getString("history"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
