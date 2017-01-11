-- phpMyAdmin SQL Dump
-- version 4.2.11
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jan 05, 2017 at 03:55 PM
-- Server version: 5.6.21
-- PHP Version: 5.6.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `simpleaa`
--

-- --------------------------------------------------------

--
-- Table structure for table `attribute_types`
--

CREATE TABLE IF NOT EXISTS `attribute_types` (
`id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `friendly_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `attribute_types`
--

INSERT INTO `attribute_types` (`id`, `name`, `friendly_name`) VALUES
(1, 'urn:oid:0.9.2342.19200300.100.1.1', 'uid'),
(2, 'urn:oid:0.9.2342.19200300.100.1.3', 'mail'),
(3, 'urn:oid:0.9.2342.19200300.100.1.4', 'info'),
(4, 'urn:oid:0.9.2342.19200300.100.1.5', 'drink'),
(5, 'urn:oid:0.9.2342.19200300.100.1.6', 'roomNumber'),
(6, 'urn:oid:0.9.2342.19200300.100.1.7', 'photo'),
(7, 'urn:oid:0.9.2342.19200300.100.1.8', 'userClass'),
(8, 'urn:oid:0.9.2342.19200300.100.1.9', 'host'),
(9, 'urn:oid:0.9.2342.19200300.100.1.10', 'manager'),
(10, 'urn:oid:0.9.2342.19200300.100.1.11', 'documentIdentifier'),
(11, 'urn:oid:0.9.2342.19200300.100.1.12', 'documentTitle'),
(12, 'urn:oid:0.9.2342.19200300.100.1.13', 'documentVersion'),
(13, 'urn:oid:0.9.2342.19200300.100.1.14', 'documentAuthor'),
(14, 'urn:oid:0.9.2342.19200300.100.1.15', 'documentLocation'),
(15, 'urn:oid:0.9.2342.19200300.100.1.20', 'homePhone'),
(16, 'urn:oid:0.9.2342.19200300.100.1.21', 'secretary'),
(17, 'urn:oid:0.9.2342.19200300.100.1.25', 'dc'),
(18, 'urn:oid:0.9.2342.19200300.100.1.37', 'associatedDomain'),
(19, 'urn:oid:0.9.2342.19200300.100.1.38', 'associatedName'),
(20, 'urn:oid:0.9.2342.19200300.100.1.39', 'homePostalAddress'),
(21, 'urn:oid:0.9.2342.19200300.100.1.40', 'personalTitle'),
(22, 'urn:oid:0.9.2342.19200300.100.1.41', 'mobile'),
(23, 'urn:oid:0.9.2342.19200300.100.1.42', 'pager'),
(24, 'urn:oid:0.9.2342.19200300.100.1.43', 'co'),
(25, 'urn:oid:0.9.2342.19200300.100.1.44', 'uniqueIdentifier'),
(26, 'urn:oid:0.9.2342.19200300.100.1.45', 'organizationalStatus'),
(27, 'urn:oid:0.9.2342.19200300.100.1.48', 'buildingName'),
(28, 'urn:oid:0.9.2342.19200300.100.1.55', 'audio'),
(29, 'urn:oid:0.9.2342.19200300.100.1.56', 'documentPublisher'),
(30, 'urn:oid:0.9.2342.19200300.100.1.60', 'jpegPhoto'),
(31, 'urn:oid:1.3.6.1.1.4', 'vendorName'),
(32, 'urn:oid:1.3.6.1.1.5', 'vendorVersion'),
(33, 'urn:oid:1.3.6.1.1.16.4', 'entryUUID'),
(34, 'urn:oid:1.3.6.1.1.20', 'entryDN'),
(35, 'urn:oid:1.3.6.1.4.1.250.1.57', 'labeledURI'),
(36, 'urn:oid:1.3.6.1.4.1.453.16.2.103', 'numSubordinates'),
(37, 'urn:oid:1.3.6.1.4.1.1466.101.120.5', 'namingContexts'),
(38, 'urn:oid:1.3.6.1.4.1.1466.101.120.6', 'altServer'),
(39, 'urn:oid:1.3.6.1.4.1.1466.101.120.7', 'supportedExtension'),
(40, 'urn:oid:1.3.6.1.4.1.1466.101.120.13', 'supportedControl'),
(41, 'urn:oid:1.3.6.1.4.1.1466.101.120.14', 'supportedSASLMechanisms'),
(42, 'urn:oid:1.3.6.1.4.1.1466.101.120.15', 'supportedLDAPVersion'),
(43, 'urn:oid:1.3.6.1.4.1.1466.101.120.16', 'ldapSyntaxes'),
(44, 'urn:oid:1.3.6.1.4.1.4203.1.3.3', 'supportedAuthPasswordSchemes'),
(45, 'urn:oid:1.3.6.1.4.1.4203.1.3.4', 'authPassword'),
(46, 'urn:oid:1.3.6.1.4.1.4203.1.3.5', 'supportedFeatures'),
(47, 'urn:oid:1.3.6.1.4.1.7628.5.4.1', 'inheritable'),
(48, 'urn:oid:1.3.6.1.4.1.7628.5.4.2', 'blockInheritance'),
(49, 'urn:oid:2.5.4.0', 'objectClass'),
(50, 'urn:oid:2.5.4.1', 'aliasedObjectName'),
(51, 'urn:oid:2.5.4.3', 'cn'),
(52, 'urn:oid:2.5.4.4', 'sn'),
(53, 'urn:oid:2.5.4.5', 'serialNumber'),
(54, 'urn:oid:2.5.4.6', 'c'),
(55, 'urn:oid:2.5.4.7', 'l'),
(56, 'urn:oid:2.5.4.8', 'st'),
(57, 'urn:oid:2.5.4.9', 'street'),
(58, 'urn:oid:2.5.4.10', 'o'),
(59, 'urn:oid:2.5.4.11', 'ou'),
(60, 'urn:oid:2.5.4.12', 'title'),
(61, 'urn:oid:2.5.4.13', 'description'),
(62, 'urn:oid:2.5.4.14', 'searchGuide'),
(63, 'urn:oid:2.5.4.15', 'businessCategory'),
(64, 'urn:oid:2.5.4.16', 'postalAddress'),
(65, 'urn:oid:2.5.4.17', 'postalCode'),
(66, 'urn:oid:2.5.4.18', 'postOfficeBox'),
(67, 'urn:oid:2.5.4.19', 'physicalDeliveryOfficeName'),
(68, 'urn:oid:2.5.4.20', 'telephoneNumber'),
(69, 'urn:oid:2.5.4.21', 'telexNumber'),
(70, 'urn:oid:2.5.4.22', 'teletexTerminalIdentifier'),
(71, 'urn:oid:2.5.4.23', 'facsimileTelephoneNumber'),
(72, 'urn:oid:2.5.4.24', 'x121Address'),
(73, 'urn:oid:2.5.4.25', 'internationalISDNNumber'),
(74, 'urn:oid:2.5.4.26', 'registeredAddress'),
(75, 'urn:oid:2.5.4.27', 'destinationIndicator'),
(76, 'urn:oid:2.5.4.28', 'preferredDeliveryMethod'),
(77, 'urn:oid:2.5.4.31', 'member'),
(78, 'urn:oid:2.5.4.32', 'owner'),
(79, 'urn:oid:2.5.4.33', 'roleOccupant'),
(80, 'urn:oid:2.5.4.34', 'seeAlso'),
(81, 'urn:oid:2.5.4.35', 'userPassword'),
(82, 'urn:oid:2.5.4.36', 'userCertificate'),
(83, 'urn:oid:2.5.4.37', 'cACertificate'),
(84, 'urn:oid:2.5.4.38', 'authorityRevocationList'),
(85, 'urn:oid:2.5.4.39', 'certificateRevocationList'),
(86, 'urn:oid:2.5.4.40', 'crossCertificatePair'),
(87, 'urn:oid:2.5.4.41', 'name'),
(88, 'urn:oid:2.5.4.42', 'givenName'),
(89, 'urn:oid:2.5.4.43', 'initials'),
(90, 'urn:oid:2.5.4.44', 'generationQualifier'),
(91, 'urn:oid:2.5.4.45', 'x500UniqueIdentifier'),
(92, 'urn:oid:2.5.4.46', 'dnQualifier'),
(93, 'urn:oid:2.5.4.47', 'enhancedSearchGuide'),
(94, 'urn:oid:2.5.4.49', 'distinguishedName'),
(95, 'urn:oid:2.5.4.50', 'uniqueMember'),
(96, 'urn:oid:2.5.4.51', 'houseIdentifier'),
(97, 'urn:oid:2.5.4.52', 'supportedAlgorithms'),
(98, 'urn:oid:2.5.4.53', 'deltaRevocationList'),
(99, 'urn:oid:2.5.18.1', 'createTimestamp'),
(100, 'urn:oid:2.5.18.2', 'modifyTimestamp'),
(101, 'urn:oid:2.5.18.3', 'creatorsName'),
(102, 'urn:oid:2.5.18.4', 'modifiersName'),
(103, 'urn:oid:2.5.18.10', 'subschemaSubentry'),
(104, 'urn:oid:2.5.21.1', 'dITStructureRules'),
(105, 'urn:oid:2.5.21.2', 'dITContentRules'),
(106, 'urn:oid:2.5.21.4', 'matchingRules'),
(107, 'urn:oid:2.5.21.5', 'attributeTypes'),
(108, 'urn:oid:2.5.21.6', 'objectClasses'),
(109, 'urn:oid:2.5.21.7', 'nameForms'),
(110, 'urn:oid:2.5.21.8', 'matchingRuleUse'),
(111, 'urn:oid:2.5.21.9', 'structuralObjectClass'),
(112, 'urn:oid:2.5.21.10', 'governingStructureRule'),
(113, 'urn:oid:2.16.840.1.113730.3.1.1', 'carLicense'),
(114, 'urn:oid:2.16.840.1.113730.3.1.2', 'departmentNumber'),
(115, 'urn:oid:2.16.840.1.113730.3.1.3', 'employeeNumber'),
(116, 'urn:oid:2.16.840.1.113730.3.1.4', 'employeeType'),
(117, 'urn:oid:2.16.840.1.113730.3.1.5', 'changeNumber'),
(118, 'urn:oid:2.16.840.1.113730.3.1.6', 'targetDN'),
(119, 'urn:oid:2.16.840.1.113730.3.1.7', 'changeType'),
(120, 'urn:oid:2.16.840.1.113730.3.1.8', 'changes'),
(121, 'urn:oid:2.16.840.1.113730.3.1.9', 'newRDN'),
(122, 'urn:oid:2.16.840.1.113730.3.1.10', 'deleteOldRDN'),
(123, 'urn:oid:2.16.840.1.113730.3.1.11', 'newSuperior'),
(124, 'urn:oid:2.16.840.1.113730.3.1.34', 'ref'),
(125, 'urn:oid:2.16.840.1.113730.3.1.35', 'changelog'),
(126, 'urn:oid:2.16.840.1.113730.3.1.39', 'preferredLanguage'),
(127, 'urn:oid:2.16.840.1.113730.3.1.40', 'userSMIMECertificate'),
(128, 'urn:oid:2.16.840.1.113730.3.1.55', 'aci'),
(129, 'urn:oid:2.16.840.1.113730.3.1.216', 'userPKCS12'),
(130, 'urn:oid:2.16.840.1.113730.3.1.241', 'displayName'),
(131, 'member-of', 'member-of');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
`id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `user_attributes`
--

CREATE TABLE IF NOT EXISTS `user_attributes` (
  `user_id` int(11) NOT NULL,
  `attribute_id` int(11) NOT NULL,
  `value` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `attribute_types`
--
ALTER TABLE `attribute_types`
 ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
 ADD PRIMARY KEY (`id`);

--
-- Indexes for table `user_attributes`
--
ALTER TABLE `user_attributes`
 ADD PRIMARY KEY (`user_id`,`attribute_id`,`value`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `attribute_types`
--
ALTER TABLE `attribute_types`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
