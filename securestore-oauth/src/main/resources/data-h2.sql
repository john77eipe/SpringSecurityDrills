-- MySQL dump 10.13  Distrib 8.0.13, for macos10.14 (x86_64)
--
-- Host: localhost    Database: securestore
-- ------------------------------------------------------
-- Server version	8.0.15


--
-- Table structure for table `user_account`
--

DROP TABLE IF EXISTS `user_account`;

CREATE TABLE `user_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `age` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;


--
-- Dumping data for table `user_account`
--

LOCK TABLES `user_account` WRITE;
/*!40000 ALTER TABLE `user_account` DISABLE KEYS */;
INSERT INTO `user_account` VALUES (1,12,'john','$2a$10$k6X3EmKLqzJ.cqKyzOqNMOrxfDO8tYYiPkAjigyvvkenw0eW3wIc6','user1'),(2,40,'adminjohn','$2a$10$yDEiqCrIbm71W9AQTcxphOI7EB65o3uur6/hSN96N6GhHHIsr/pC6','admin');
/*!40000 ALTER TABLE `user_account` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `authorities`
--

DROP TABLE IF EXISTS `authorities`;

CREATE TABLE `authorities` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authority` varchar(255) DEFAULT NULL,
  `user_account_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfpwg8eqghxf97xt5xxbgccmmt` (`user_account_id`),
  CONSTRAINT `FKfpwg8eqghxf97xt5xxbgccmmt` FOREIGN KEY (`user_account_id`) REFERENCES `user_account` (`id`)
) ;


--
-- Dumping data for table `authorities`
--

LOCK TABLES `authorities` WRITE;
/*!40000 ALTER TABLE `authorities` DISABLE KEYS */;
INSERT INTO `authorities` VALUES (1,'ROLE_USER',1),(2,'ROLE_USER',2),(3,'ROLE_HRADMIN',2);
/*!40000 ALTER TABLE `authorities` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


-- Dump completed on 2019-08-12 13:59:51
