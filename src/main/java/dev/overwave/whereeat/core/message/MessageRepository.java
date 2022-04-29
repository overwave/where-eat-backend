package dev.overwave.whereeat.core.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    List<Message> findAllByGroupIdAndHiddenIsFalse(String groupId);

    @Query(nativeQuery = true,
            value = "" +
                    "select ordered_groups.group_id " +
                    "from (select distinct m.group_id " +
                    "      from message m " +
                    "      where m.hidden = false) as all_groups " +
                    "         left join lateral ( " +
                    "    select message.id, message.group_id " +
                    "    from message " +
                    "    where message.group_id = all_groups.group_id " +
                    "    limit 1 " +
                    "    ) ordered_groups on true " +
                    "order by ordered_groups.id",
            countQuery = "select count(distinct message.group_id) from message")
    Page<String> findGroups(Pageable pageable);
}
