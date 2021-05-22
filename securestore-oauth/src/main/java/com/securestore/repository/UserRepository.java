package com.securestore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.securestore.domain.UserAccount;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long>{

	@Query("select u from UserAccount u"
		      + " left join fetch u.authorities"
		      + " where u.username = :username")
	UserAccount findByUsername(@Param("username") String username);

}
