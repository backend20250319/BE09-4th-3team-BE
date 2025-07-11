package io.fundy.fundyserver.register.repository;

import io.fundy.fundyserver.register.entity.Address;
import io.fundy.fundyserver.register.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // 특정 유저의 모든 배송지 조회 (기본 배송지 우선, 생성일 역순)
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);

    // 특정 유저의 모든 배송지 조회 (userNo로 조회)
    List<Address> findByUserUserNoOrderByIsDefaultDescCreatedAtDesc(Integer userNo);

    // 기본 배송지 조회
    Address findByUserAndIsDefaultTrue(User user);

    // 기본 배송지 조회 (userNo로 조회)
    Optional<Address> findByUserUserNoAndIsDefaultTrue(Integer userNo);

    // 사용자별 특정 배송지 조회
    Optional<Address> findByIdAndUserUserNo(Long addressId, Integer userNo);

    // 사용자별 기본 배송지 해제
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.userNo = :userNo")
    void clearDefaultAddress(@Param("userNo") Integer userNo);

    // 사용자별 기본 배송지 해제 (User 엔티티로 조회)
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user")
    void clearDefaultAddressByUser(@Param("user") User user);

    // 사용자별 배송지 개수 조회
    long countByUserUserNo(Integer userNo);

    // 사용자별 배송지 개수 조회 (User 엔티티로 조회)
    long countByUser(User user);
}