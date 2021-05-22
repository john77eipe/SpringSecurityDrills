package com.securestore.service.impl;

import com.securestore.domain.Authorities;
import com.securestore.domain.CustomSecurityUser;
import com.securestore.domain.UserAccount;
import com.securestore.repository.UserRepository;
import com.securestore.service.UserAccountDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service("mockUserAccountDetailsService")
public class UserAccountDetailsMockServiceImpl implements UserAccountDetailsService {

	private static final AtomicLong counter = new AtomicLong();

	private static Map<Long, UserAccount> users;

	static{
		users= populateDummyUsers();
	}

	private static Map<Long, UserAccount> populateDummyUsers(){
		Map<Long, UserAccount> users = new HashMap();

		long key = counter.incrementAndGet();
		UserAccount userAccount = new UserAccount(key,"user1","$2a$10$k6X3EmKLqzJ.cqKyzOqNMOrxfDO8tYYiPkAjigyvvkenw0eW3wIc6", "John", 23, null);
		Authorities userAuthorities = new Authorities();
		userAuthorities.setAuthority("ROLE_USER");
		userAuthorities.setUserAccount(userAccount);
		Set<Authorities> authorities = new HashSet<Authorities>();
		authorities.add(userAuthorities);

		userAccount.setAuthorities(authorities);
		users.put(key, userAccount);

		key = counter.incrementAndGet();
		userAccount = new UserAccount(key,"admin","$2a$10$yDEiqCrIbm71W9AQTcxphOI7EB65o3uur6/hSN96N6GhHHIsr/pC6", "Admin", 23, null);
		userAuthorities = new Authorities();
		userAuthorities.setAuthority("ROLE_USER");
		userAuthorities.setUserAccount(userAccount);
		authorities.add(userAuthorities);
		userAuthorities = new Authorities();
		userAuthorities.setAuthority("ROLE_ADMIN");
		userAuthorities.setUserAccount(userAccount);
		authorities = new HashSet<Authorities>();
		authorities.add(userAuthorities);

		userAccount.setAuthorities(authorities);
		users.put(key, userAccount);

		return users;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		for (UserAccount userAccount: users.values()) {
			if(userAccount.getUsername().equals(username)){
				return new CustomSecurityUser(userAccount);
			}
		}
		return null;
		
	}

	@Override
	public List<UserAccount> findAllUsers() {
		List<UserAccount> listOfUsers = Collections.emptyList();
		listOfUsers.addAll(users.values());
		return listOfUsers;
	}

	@Override
	public UserAccount findById(long id) {
		return users.get(id);
	}

	@Override
	public boolean isUserExist(UserAccount user) {
		return users.containsKey(user.getId());
	}

	@Override
	public void saveUser(UserAccount user) {
		System.out.println("Sorry cannot be saved as of now");
	}

	@Override
	public void updateUser(UserAccount currentUser) {
		System.out.println("Sorry cannot be updated as of now");
	}

	@Override
	public void deleteUserById(long id) {
		System.out.println("Sorry cannot be deleted as of now");
	}

	@Override
	public void deleteAllUsers() {
		System.out.println("Sorry cannot be deleted as of now");
	}
}
