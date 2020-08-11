package com.securestore.service.impl;

import com.securestore.service.UserAccountDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.securestore.domain.CustomSecurityUser;
import com.securestore.domain.UserAccount;
import com.securestore.repository.UserRepository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class UserAccountDetailsServiceImpl implements UserAccountDetailsService {
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserAccount user = userRepository.findByUsername(username);
		if(user==null) {
			throw new UsernameNotFoundException("User account not found in the application");
		}
		return new CustomSecurityUser(user);
		
	}

	@Override
	public List<UserAccount> findAllUsers() {
		List<UserAccount> userAccountList = userRepository.findAll();
		return userAccountList;
	}

	@Override
	public UserAccount findById(long id) {
		Optional<UserAccount> userAccount = userRepository.findById(id);
		if(!userAccount.isPresent()) {
			throw new UsernameNotFoundException("User account not found in the application");
		}
		return userAccount.get();
	}

	@Override
	public boolean isUserExist(UserAccount user) {
		boolean userExists = false;
		if (user.getId() != null && user.getId() != 0) {
			userExists = userRepository.existsById(user.getId());
		} else {
			if (userRepository.findByUsername(user.getUsername()) != null) {
				userExists = true;
			}
		}
		return userExists;
	}

	@Override
	public boolean isUserExistByUsername(String username) {
		boolean userExists = false;
		if(!StringUtils.isEmpty(username)) {
			if (userRepository.findByUsername(username) != null) {
				userExists = true;
			}
		}
		return userExists;
	}

	@Override
	public void saveUser(UserAccount user) {
		userRepository.save(user);
	}

	@Override
	public void updateUser(UserAccount currentUser) {
		userRepository.save(currentUser);
	}

	@Override
	public void deleteUserById(long id) {
		userRepository.deleteById(id);
	}

	@Override
	public void deleteAllUsers() {
		userRepository.deleteAll();
	}
}
