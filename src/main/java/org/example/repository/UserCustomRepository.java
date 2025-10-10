package org.example.repository;

import org.example.dto.internal.exceptions.BadConfigurationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserCustomRepository extends BaseDao {

    public UserCustomRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Transactional
    public void deleteAllDataRelatedToUser(long userId) {
        executeUpdate("""
                    delete ps from payments_status ps
                              join payments p on ps.payment_id = p.id
                             where p.user_id = ?
                """, stmt -> stmt.setLong(1, userId));
        executeUpdate("delete from payments where user_id = ?", stmt -> stmt.setLong(1, userId));
        executeUpdate("delete from banking_details where user_id = ?", stmt -> stmt.setLong(1, userId));
    }

    public int findPendingPaymentsAmount(long userId) {
        List<Integer> result = executeQuery(
                """
                        select count(1) as count
                          from payments p
                          join payments_status ps on ps.payment_id = p.id
                         where p.user_id = ?
                           and ps.status not in ('NOTIFIED', 'ERROR')
                        """,
                stmt -> stmt.setLong(1, userId),
                rs -> rs.getInt("count")
        );

        if (result == null || result.size() != 1)
            throw new BadConfigurationException("findPendingPaymentsAmount query is broken");
        return result.getFirst();
    }
}
