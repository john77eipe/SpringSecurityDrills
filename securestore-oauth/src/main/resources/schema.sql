-- MySQL dump 10.13  Distrib 8.0.13, for macos10.14 (x86_64)
--
-- Host: localhost    Database: securestore
-- ------------------------------------------------------
-- Server version	8.0.15


--
-- Table structure for table `user_account`
--

DROP TABLE IF EXISTS user_account;

CREATE TABLE user_account (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `age` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;



--
-- Table structure for table `authorities`
--

DROP TABLE IF EXISTS authorities;

CREATE TABLE authorities (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authority` varchar(255) DEFAULT NULL,
  `user_account_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;



-- Dump completed on 2019-08-12 13:59:51
