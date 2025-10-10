package org.example.repository;

import org.example.entity.PaymentStatus;
import org.example.entity.PaymentStatus.Status;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * due to incompatibility between java's enum, jpa and mysql, it was decided to write this with prepared statement
 */
@Component
public class PaymentStatusRepository extends BaseDao {

    public PaymentStatusRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Transactional
    public void create(long paymentId) {
        String sql = """
                insert into payments_status (payment_id, status, retries, history, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?)
                """;
        Instant now = Instant.now();
        executeUpdate(sql, stmt -> {
            int i = 1;
            stmt.setLong(i++, paymentId);
            stmt.setString(i++, Status.CREATED.name());
            stmt.setInt(i++, 0);
            stmt.setString(i++, "");
            stmt.setTimestamp(i++, Timestamp.from(now));
            stmt.setTimestamp(i, Timestamp.from(now));
        });
    }

    public List<PaymentStatus> findByStatus(Status status, int limit) {
        String sql = """
                  select id, payment_id, status, retries, history, created_at, updated_at
                    from payments_status
                   where status = ?
                order by updated_at asc
                   limit ?
                """;
        return executeQuery(sql,
                stmt -> {
                    stmt.setString(1, status.name());
                    stmt.setInt(2, limit);
                },
                PaymentStatus::fromResultSet
        );
    }

    @Transactional
    public int tryToUpdate(Status newStatus, int retries, String history, Instant newUpdatedAt, long id, Set<Status> oldStatusSet, Instant oldUpdatedAt) {
        if (oldStatusSet.isEmpty()) return 0;

        String inClause = oldStatusSet.stream().map(s -> "?").collect(Collectors.joining(", "));
        String sql = """
                update payments_status
                   set status      = ?,
                       retries     = ?,
                       history     = ?,
                       updated_at  = ?
                 where id          = ?
                   and status     in (%s)
                   and updated_at  = ?
                """.formatted(inClause);

        return executeUpdate(sql, stmt -> {
            int i = 1;
            stmt.setString(i++, newStatus.name());
            stmt.setInt(i++, retries);
            stmt.setString(i++, history);
            stmt.setTimestamp(i++, Timestamp.from(newUpdatedAt));
            stmt.setLong(i++, id);
            for (Status oldStatus : oldStatusSet) stmt.setString(i++, oldStatus.name());
            stmt.setTimestamp(i, Timestamp.from(oldUpdatedAt));
        });
    }
}