package study.datajpa.auditing;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberJpaRepository;

@SpringBootTest
@Transactional
@Rollback(false)
public class JpaAuditingTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void jpaBaseEntity() throws Exception {
        Member member = new Member("member1", 20);
        memberJpaRepository.save(member);   // @PrePersist

        Thread.sleep(1000);
        member.setUsername("member2");

        em.flush();
        em.clear();

        Member findMember = memberJpaRepository.findById(member.getId()).get();

        System.out.println("findMember create = " + findMember.getCreatedDate());
        System.out.println("findMember update = " + findMember.getUpdatedDate());

    }
}
