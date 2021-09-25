package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
@Rollback(false)
class DataJpaRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void basicCRUD() {
        Member member1 = new Member();
        Member member2 = new Member();

        member1.setUsername("member1");
        member2.setUsername("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);

    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member();
        Member m2 = new Member();
        m1.setUsername("aaa");
        m1.setAge(10);
        m2.setUsername("aaa");
        m2.setAge(20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("aaa", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("aaa");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testQuery() {
        Member m1 = new Member();
        Member m2 = new Member();
        m1.setUsername("aaa");
        m1.setAge(10);
        m2.setUsername("bbb");
        m2.setAge(20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("aaa", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void testFindUsernameList() {
        Member m1 = new Member();
        Member m2 = new Member();
        m1.setUsername("aaa");
        m1.setAge(10);
        m2.setUsername("bbb");
        m2.setAge(20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto() {
        Team team = new Team();
        team.setName("teamA");
        teamRepository.save(team);

        Member m1 = new Member();
        m1.setUsername("aaa");
        m1.setAge(10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() {
        Member m1 = new Member();
        Member m2 = new Member();
        m1.setUsername("aaa");
        m1.setAge(10);
        m2.setUsername("bbb");
        m2.setAge(20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("aaa", "bbb"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void paging() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        // entity 를 반환하지 않고 DTO 로 변환하는 방법
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));


        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        for (Member member : content) {
            System.out.println("member = " + member);
        }
        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void paging2() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        List<Member> page = memberRepository.findAnotherByAge(age, pageRequest);

    }


    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20);
        // bulk 연산 이후에 영속성 컨텍스트를 clear 해주지 않으면
        // 같은 트랜잭션 내에 작업이 남아있는 경우 영향을 줄 수 있다.
        // clear 하는 첫 번째 방법은 EntityManager 를 가져와서 직접 flush, clear 하는 방법
        // 두 번째 방법은 repository 에 @Modifying 할 때 clearAutomatically 값을 true 로 설정 한다. (default 값이 false)
        /*
        em.flush();
        em.clear();
         */

        List<Member> result = memberRepository.findByNames(Arrays.asList("member5"));
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);
        // 지금의 경우 영속성 컨텍스 clear 작업이 없을 경우 age 를 41이 아닌 40으로 가지고 있다.

        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        // member1 -> teamA
        // member2 -> teamB

        Team tramA = new Team("tramA");
        Team tramB = new Team("tramB");
        teamRepository.save(tramA);
        teamRepository.save(tramB);

        Member member1 = new Member("member1", 10, tramA);
        Member member2 = new Member("member2", 10, tramB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // LAZY 로딩을 하기 때문에 fetch join 하지 않으면 N+1 문제 발생, team 조회 할 때마다 추가 쿼리 실행
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }

        em.flush();
        em.clear();
        System.out.println("============================================================================================================================================");

        // fetch join 으로 N+1 문제 해결
        List<Member> memberFetchJoin = memberRepository.findMemberFetchJoin();
        for (Member member : memberFetchJoin) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }

    }
}