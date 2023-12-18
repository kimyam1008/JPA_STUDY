package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class japMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            //비영속
            Member member = new Member();
            member.setId(101L);
            member.setName("HelloJPA");

            //영속
            System.out.println("=== BEFORE ===");
            em.persist(member); // 1차 캐시 조회
            System.out.println("=== AFTER ===");
            
            Member findMember = em.find(Member.class , 101L);

            System.out.println("findMember.id = " + findMember.getId());
            System.out.println("findMember.name = " + findMember.getName());

            /********** 23.12.17 JPA 매커니즘 이해
            //Insert
            Member member = new Member();
            member.setId(2L);
            member.setName("HelloB");

            //persist - 저장
            em.persist(member);

            //select
            Member findMember = em.find(Member.class, 1L);
            System.out.println("findMember.getId() = " + findMember.getId());
            System.out.println("findMember.getName() = " + findMember.getName());

            //Delect
            Member findMember = em.find(Member.class, 1L);
            em.remove(findMember);

            //Update - persist(저장) 안해도 됨
            Member findMember = em.find(Member.class, 1L);
            findMember.setName("HelloJPA");

            //Paging
             List<Member> result = em.createQuery("select m from Member as m", Member.class)
                     .setFirstResult(5) //페이징
                     .setMaxResults(8)  //Oracle 로 변경하면 알아서 rownum 으로 변경해줌 / MySQL 은 limit
                     .getResultList();

            for (Member member : result) {
                System.out.println("member.getName() = " + member.getName());
            }
            **********/

            tx.commit();
        }catch (Exception e){
            tx.rollback();
        }finally {
            em.close();
        }

        emf.close();

    }
}
