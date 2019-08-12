package com.securestore.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.securestore.domain.CustomSecurityUser;
import com.securestore.domain.UserAccount;
import com.securestore.service.UserAccountDetailsService;
import com.securestore.service.impl.UserAccountDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.List;


@Controller
@RequestMapping("/rest")
public class UserResourceController {

    @Autowired
    @Qualifier("userAccountDetailsService")
    UserAccountDetailsService userAccountDetailsService;  //Service which will do all data retrieval/manipulation work

    @RequestMapping("/user/me")
    public Principal user(Principal principal, UserDetailsService userAccountDetailsService) {

        /**
         * it is not necessary to hit the userAccountdetailsService
         * just doing it again for fun
         */
        CustomSecurityUser userDetails = (CustomSecurityUser)userAccountDetailsService.loadUserByUsername(principal.getName());

        if (userDetails == null) {
            System.out.println("User with name " + userDetails.getName() + " not found");
            //return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        System.out.println("----principal----");
        System.out.println("userDetails.toString() : " + userDetails.toString());
        System.out.println("userDetails.getName() : " + userDetails.getName());
        System.out.println();

        System.out.println("----authentication----");
        System.out.println("userDetails.getAuthorities() : " + userDetails.getAuthorities());
        System.out.println("userDetails.getUsername() : " + userDetails.getUsername());
        System.out.println("userDetails.getPassword : " + userDetails.getPassword());
        System.out.println();

        System.out.println("----custom authentication----");
        System.out.println("userDetails.getName() : " + userDetails.getName());
        System.out.println("userDetails.getAge() : " + userDetails.getAge());

        return principal;
    }

    @RequestMapping("/user/also-me")
    public String test(Principal user) {
        return "The principal's name is: " + user.getName();
    }

    //-------------------Retrieve All Users--------------------------------------------------------

    @RequestMapping(value = "/user/all", method = RequestMethod.GET)
    @PreAuthorize("hasRole('HRADMIN')")
    public ResponseEntity<List<UserAccount>> listAllUsers() {
        List<UserAccount> users = userAccountDetailsService.findAllUsers();
        if (users.isEmpty()) {
            return new ResponseEntity<List<UserAccount>>(HttpStatus.NO_CONTENT);//You many decide to return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<List<UserAccount>>(users, HttpStatus.OK);
    }


    //-------------------Retrieve Single User--------------------------------------------------------

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('USER')")
    @JsonIgnore
    public ResponseEntity<UserAccount> getUser(@PathVariable("id") long id) {
        System.out.println("Fetching User with id " + id);
        UserAccount user = userAccountDetailsService.findById(id);
        if (user == null) {
            System.out.println("User with id " + id + " not found");
            return new ResponseEntity<UserAccount>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<UserAccount>(user, HttpStatus.OK);
    }


    //-------------------Create a User--------------------------------------------------------

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @PreAuthorize("hasRole('HRADMIN')")
    public ResponseEntity<Void> createUser(@RequestBody UserAccount user, UriComponentsBuilder ucBuilder) {
        System.out.println("Creating User " + user.getName());

        if (userAccountDetailsService.isUserExist(user)) {
            System.out.println("A User with name " + user.getName() + " already exist");
            return new ResponseEntity<Void>(HttpStatus.CONFLICT);
        }

        userAccountDetailsService.saveUser(user);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(user.getId()).toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }


    //------------------- Update a User --------------------------------------------------------

    @RequestMapping(value = "/user/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserAccount> updateUser(@PathVariable("id") long id, @RequestBody UserAccount user) {
        System.out.println("Updating User " + id);

        UserAccount currentUser = userAccountDetailsService.findById(id);

        if (currentUser == null) {
            System.out.println("User with id " + id + " not found");
            return new ResponseEntity<UserAccount>(HttpStatus.NOT_FOUND);
        }

        currentUser.setName(user.getName());
        currentUser.setAge(user.getAge());

        userAccountDetailsService.updateUser(currentUser);
        return new ResponseEntity<UserAccount>(currentUser, HttpStatus.OK);
    }

    //------------------- Delete a User --------------------------------------------------------

    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('HRUSER')")
    public ResponseEntity<UserAccount> deleteUser(@PathVariable("id") long id) {
        System.out.println("Fetching & Deleting User with id " + id);

        UserAccount user = userAccountDetailsService.findById(id);
        if (user == null) {
            System.out.println("Unable to delete. User with id " + id + " not found");
            return new ResponseEntity<UserAccount>(HttpStatus.NOT_FOUND);
        }

        userAccountDetailsService.deleteUserById(id);
        return new ResponseEntity<UserAccount>(HttpStatus.NO_CONTENT);
    }


    //------------------- Delete All Users --------------------------------------------------------

    @RequestMapping(value = "/user", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<User> deleteAllUsers() {
        System.out.println("Deleting All Users");

        userAccountDetailsService.deleteAllUsers();
        return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
    }

}
