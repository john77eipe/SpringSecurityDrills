package com.securestore.service;

import com.securestore.domain.UserAccount;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface UserAccountDetailsService extends UserDetailsService {
    List<UserAccount> findAllUsers();

    UserAccount findById(long id);

    boolean isUserExist(UserAccount user);

    boolean isUserExistByUsername(String username);

    void saveUser(UserAccount user);

    void updateUser(UserAccount currentUser);

    void deleteUserById(long id);

    void deleteAllUsers();

    /**
     * This will act as the data fetcher instead of the
     * UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
     * from UserDetailsService interface
     * @param username
     * @param domain
     * @return
     * @throws UsernameNotFoundException
     */
    UserDetails loadUserByUsernameAndDomain(String username, String domain) throws UsernameNotFoundException;
}
