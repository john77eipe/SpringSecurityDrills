-- MySQL dump 10.13  Distrib 8.0.13, for macos10.14 (x86_64)
--
-- Host: localhost    Database: securestore
-- ------------------------------------------------------
-- Server version	8.0.15





--
-- Dumping data for table `user_account`
--


INSERT INTO user_account VALUES (1,12,'john','$2a$10$k6X3EmKLqzJ.cqKyzOqNMOrxfDO8tYYiPkAjigyvvkenw0eW3wIc6','user1'),
                                (2,40,'adminjohn','$2a$10$yDEiqCrIbm71W9AQTcxphOI7EB65o3uur6/hSN96N6GhHHIsr/pC6','admin');




--
-- Dumping data for table `authorities`
--


INSERT INTO authorities VALUES (1,'ROLE_USER',1),
                               (2,'ROLE_USER',2),
                               (3,'ROLE_HRADMIN',2);



-- Dump completed on 2019-08-12 13:59:51
