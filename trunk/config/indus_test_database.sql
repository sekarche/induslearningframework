-- TEST DATABASE FOR INDUS
-- COVERS ALL THE EXAMPLES



-- Create Users

CREATE USER 'ailab'@'localhost' IDENTIFIED BY 'okay';
GRANT ALL PRIVILEGES ON *.* TO 'ailab'@'localhost';
#GRANT ALL PRIVILEGES ON *.* TO 'ailab'@'%' ;
CREATE USER 'indus'@'localhost' IDENTIFIED BY 'indus';
GRANT ALL PRIVILEGES ON *.* TO 'indus'@'localhost' ;
#GRANT ALL PRIVILEGES ON *.* TO 'indus'@'%';


-- Create Databases

CREATE DATABASE ds1;

CREATE DATABASE ds2;

CREATE DATABASE ds3;

CREATE DATABASE indus;

CREATE DATABASE netflix;

CREATE DATABASE blockbuster;


-- Create Tables and Insert Data

use ds1;

CREATE TABLE `ds1_synonyms` (
  `Person` char(100) NOT NULL,
  `Name` varchar(100) NOT NULL,
  `Entry` varchar(100) NOT NULL,
  `PublishedOn` date DEFAULT NULL,
  `Organization` varchar(100) DEFAULT NULL,
  `Address` varchar(100) DEFAULT NULL,
  `ISBN` varchar(20) NOT NULL,
  PRIMARY KEY (`ISBN`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `ds1_table` (
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `status` varchar(255) COLLATE utf8_bin NOT NULL,
  `compensation` float NOT NULL,
  `alias` varchar(255) COLLATE utf8_bin NOT NULL,
  `servicelength` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Indus_Test_Table';

CREATE TABLE `ds1_table_ex1` (
  `movie_id` varchar(255) COLLATE utf8_bin NOT NULL,
  `genre` varchar(255) COLLATE utf8_bin NOT NULL,
  `cost` float NOT NULL,
  `name` varchar(255) COLLATE utf8_bin NOT NULL,
  `week` int(11) NOT NULL,
  PRIMARY KEY (`movie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Indus_Test_Table_RR';


insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Swamy, Mutthu','Hybrid systems','CongressMinutes','2008-08-21 00:00:00','University','ksu manhattan kansas','');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Goldsby, Anthony Lee','Nitrogen source and timing effect on carbohydrate status of bermudagrass and tall fescue ','MScThesis','2009-09-07 00:00:00','University','ksu manhattan kansas','1');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Verma, N','life science','CongressMinutes','2009-03-09 00:00:00','PublishingHouse','klm, delhi','10');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Tott, T','computer security','Brochure','2009-07-06 00:00:00','University','iisc, bangalore','11');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Neeraj','Ontology','StudentReport','2009-10-07 00:00:00','University','KSU','110');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Brown,N','grain science','Brochure','2009-06-23 00:00:00','PublishingHouse','tk, mumbai','12');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Caragea, D','Information Retrieval and Text Mining','CourseMaterial','2009-02-10 00:00:00','University','ksu manhattan kansas','13');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Christopher D. Manning','Introduction to Information Retrieval','CourseMaterial','2008-08-25 00:00:00','PublishingHouse','Cambridge University Press. 2008','14');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Barry, R','Report on Cooling Systems','Deliverable','2009-07-22 00:00:00','University','USC, California','15');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('roshan','Booklet of java','Booklets','2009-10-06 00:00:00','University','KSU, Manhattan','150');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Ahn, Woo-Kyoung','Schema Acquisition from a Single Example','Techinical','2009-06-16 00:00:00','PublishingHouse','Education Resources Information Center','16');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Mike, P','Software Engineering Architecture','Deliverable','2009-06-10 00:00:00','PublishingHouse','Infosys','17');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Murthy, N','Maintenance of Oracle DB','Technical','2008-11-18 00:00:00','University','Stanford Institute','18');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('John, McEnroe','Combined oesophageal manometry and video fluoroscopy','MScThesis','2009-04-21 00:00:00','PublishingHouse','penguine newyork','2');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Graph, Steffie','Tennis Methodology','DoctoraThesis','2008-09-16 00:00:00','PublishingHouse','Mc Graw Hills','3');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Wells, W.G.','Space Topics','DoctoraThesis','2009-09-02 00:00:00','University','Stony brook newyork','4');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Sundar, M','computer graphics','Compilation','2008-10-22 00:00:00','University','ksu manhattan kansas','5');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Pathak, L','Cryptography','Compilation','2009-06-09 00:00:00','PublishingHouse','mark NJ','6');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('John, M','physics','Monograph','2009-09-10 00:00:00','University','MIT, boston','7');
insert into `ds1_synonyms`(`Person`,`Name`,`Entry`,`PublishedOn`,`Organization`,`Address`,`ISBN`) values ('Lewis, T','chemistry topics','Monograph','2009-09-10 00:00:00','PublishingHouse','KT, kansas','8');

insert into `ds1_table`(`id`,`status`,`compensation`,`alias`,`servicelength`) values ('1001','graduate',1450,'neeraj',5);
insert into `ds1_table`(`id`,`status`,`compensation`,`alias`,`servicelength`) values ('1002','undergraduate',0,'remy',3);
insert into `ds1_table`(`id`,`status`,`compensation`,`alias`,`servicelength`) values ('1003','junior',1,'komal',2);
insert into `ds1_table`(`id`,`status`,`compensation`,`alias`,`servicelength`) values ('1004','freshman',10,'sheetal',1);

insert into `ds1_table_ex1`(`movie_id`,`genre`,`cost`,`name`,`week`) values ('101','comedy',100,'liar liar',50);
insert into `ds1_table_ex1`(`movie_id`,`genre`,`cost`,`name`,`week`) values ('102','action',150,'terminator',75);
insert into `ds1_table_ex1`(`movie_id`,`genre`,`cost`,`name`,`week`) values ('103','drama',200,'shakespeare in love',60);
insert into `ds1_table_ex1`(`movie_id`,`genre`,`cost`,`name`,`week`) values ('104','horror',150,'evil dead',90);
insert into `ds1_table_ex1`(`movie_id`,`genre`,`cost`,`name`,`week`) values ('105','romantic',500,'american beauty',100);
insert into `ds1_table_ex1`(`movie_id`,`genre`,`cost`,`name`,`week`) values ('106','war',400,'schindler''s list',200);
insert into `ds1_table_ex1`(`movie_id`,`genre`,`cost`,`name`,`week`) values ('107','creepy',400,'evil dead',300);

use ds2;

CREATE TABLE `ds2_flat` (
  `Writer` char(200) NOT NULL,
  `BookName` varchar(100) NOT NULL,
  `Type` varchar(100) NOT NULL,
  `ReleaseDate` date DEFAULT NULL,
  `Organization` varchar(100) DEFAULT NULL,
  `ID` varchar(20) NOT NULL,
  `Address` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `ds2_table` (
  `ssn` varchar(255) COLLATE utf8_bin NOT NULL,
  `type` varchar(255) COLLATE utf8_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `ds2_table_ex1` (
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `type` varchar(255) COLLATE utf8_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Rohit','OEDS','Type','2009-10-08 00:00:00','college','111','Chicago');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Caragea, D','information retrieval and text mining','Lectures','2009-02-10 00:00:00','university','13','NYC');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Murthy, N','maintenance of oracle db','TechReport','2009-06-16 00:00:00','university','18','Kansas');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Rik, W','brain science','Article','2009-09-16 00:00:00','college','23','MO');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Mik, L','Neurology','Article','2009-09-08 00:00:00','university','24','Milwaukee');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Musa, N','laptop','Booklet','2009-09-01 00:00:00','publishing_house','25','Chicago');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Kirk,T','j2ee','Booklet','2009-07-05 00:00:00','college','26','Dells');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Joseph, L','Global Semiconductor Industry Trend—IDM Versus Foundry Approaches','Proceedings','2009-09-01 00:00:00','publishing_house','27','Wisconsin');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Michael, M','Multiswarms, exclusion, and anti-convergence in dynamic environments','Proceedings','2008-11-19 00:00:00','university','28','Lawrence');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Rohit, P','A New Approach of Integrating PSO & Improved GA for Clustering ','Proceedings','2008-02-04 00:00:00','college','29','Kansas City');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Robby','CIS_771','Lectures','2009-03-10 00:00:00','university','30','Florida');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Cormen','Algorithms','Lectures','2008-11-12 00:00:00','publishing_house','31','Amsterdam');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Applied Biosystems','Sequencer Operation Report','TechReport','2009-03-04 00:00:00','publishing_house','32','Chicago');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Clarke','Software Management and Engineering','TechReport','2009-06-01 00:00:00','university','33','Boston');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Broad, J','Complier Design','TechReport','2009-09-01 00:00:00','college','34','NYC');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Oram, J','FP_Miner for Bio_informatics','Manuals','2009-04-29 00:00:00','Publishing_house','35','Chicago');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Gayle, C','Physics Lab Manual','Manuals','2009-04-01 00:00:00','college','36','DALLAS');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Sandeep, P','Sensor Networks','MastersThesis','2009-04-01 00:00:00','university','37','DC');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('wells, W.G.','space topics','PhdThesis','2009-09-02 00:00:00','university','4','Boston');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Sam, L','network','Chapter','2009-08-30 00:00:00','publishing_house','41','NYC');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Tam, T','.Net','Chapter','2009-09-08 00:00:00','university','42','SF');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Phani','graph theory','Monograph','2009-06-07 00:00:00','college','43','Topeka');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('vikash','information retrieval','Monograph','2009-09-01 00:00:00','publishing_house','44','Puarto Rico');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('John, Lenon','physical world','Collection','2009-09-07 00:00:00','university','45','Alaska');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Viki','culture','Collection','2009-08-31 00:00:00','college','46','Michigan');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('baruah','assamese short stories','PhdThesis','2009-06-08 00:00:00','publishing_house','47','Detroit');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('saikia','space science','PhdThesis','2009-09-07 00:00:00','university','48','Dells');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('mithun','vedic arts','Deliverables','2009-06-28 00:00:00','college','49','Las Vegas');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('srk','bollywood','Deliverables','2009-09-02 00:00:00','publishing_house','50','LA');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('pathak, L','cryptography','Collection','2009-06-09 00:00:00','publishing_house','6','MO');
insert into `ds2_flat`(`Writer`,`BookName`,`Type`,`ReleaseDate`,`Organization`,`ID`,`Address`) values ('Lewis, T','chemistry topics','Monograph','2009-09-10 00:00:00','publishing_house','8','VI');

insert into `ds2_table`(`ssn`,`type`) values ('s1','doctor');
insert into `ds2_table`(`ssn`,`type`) values ('s2','master');
insert into `ds2_table`(`ssn`,`type`) values ('s3','junior');
insert into `ds2_table`(`ssn`,`type`) values ('s4','fresh');

insert into `ds2_table_ex1`(`id`,`type`) values ('imdb101','spoofs');
insert into `ds2_table_ex1`(`id`,`type`) values ('imdb102','romantic');
insert into `ds2_table_ex1`(`id`,`type`) values ('imdb103','war');
insert into `ds2_table_ex1`(`id`,`type`) values ('imdb104','non_war');
insert into `ds2_table_ex1`(`id`,`type`) values ('imdb105','historical');
insert into `ds2_table_ex1`(`id`,`type`) values ('imdb106','non_historical');
insert into `ds2_table_ex1`(`id`,`type`) values ('imdb107','comedy');
insert into `ds2_table_ex1`(`id`,`type`) values ('imdb108','action');
insert into `ds2_table_ex1`(`id`,`type`) values ('imdb109','creepy');


use ds3;

CREATE TABLE `ds3_table` (
  `social` varchar(255) COLLATE utf8_bin NOT NULL,
  `salary` float NOT NULL,
  `nickname` varchar(255) COLLATE utf8_bin NOT NULL,
  `serviceyears` int(11) NOT NULL,
  PRIMARY KEY (`social`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `ds3_table_ex1` (
  `IMDB_id` varchar(255) COLLATE utf8_bin NOT NULL,
  `total_cost` float NOT NULL,
  `alias` varchar(255) COLLATE utf8_bin NOT NULL,
  `days` int(11) NOT NULL,
  PRIMARY KEY (`IMDB_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;




insert into `ds3_table`(`social`,`salary`,`nickname`,`serviceyears`) values ('s1',1500,'raj',5);
insert into `ds3_table`(`social`,`salary`,`nickname`,`serviceyears`) values ('s2',1450,'sheetal',1);
insert into `ds3_table`(`social`,`salary`,`nickname`,`serviceyears`) values ('s3',300,'ishaan',2);
insert into `ds3_table`(`social`,`salary`,`nickname`,`serviceyears`) values ('s4',15,'shikha',1);

insert into `ds3_table_ex1`(`IMDB_id`,`total_cost`,`alias`,`days`) values ('imdb101',300,'american pie1',161);
insert into `ds3_table_ex1`(`IMDB_id`,`total_cost`,`alias`,`days`) values ('imdb102',100,'liar liar',350);
insert into `ds3_table_ex1`(`IMDB_id`,`total_cost`,`alias`,`days`) values ('imdb103',320,'behind enemy lines',420);
insert into `ds3_table_ex1`(`IMDB_id`,`total_cost`,`alias`,`days`) values ('imdb104',150,'terminator',525);
insert into `ds3_table_ex1`(`IMDB_id`,`total_cost`,`alias`,`days`) values ('imdb105',200,'shakespeare in love',420);
insert into `ds3_table_ex1`(`IMDB_id`,`total_cost`,`alias`,`days`) values ('imdb106',250,'mama mia',560);
insert into `ds3_table_ex1`(`IMDB_id`,`total_cost`,`alias`,`days`) values ('imdb107',500,'american beauty',100);
insert into `ds3_table_ex1`(`IMDB_id`,`total_cost`,`alias`,`days`) values ('imdb108',400,'schindler''s list',200);
insert into `ds3_table_ex1`(`IMDB_id`,`total_cost`,`alias`,`days`) values ('imdb109',400,'evil dead',300);


USE netflix;

CREATE TABLE `table1` (
  `movie` varchar(20) NOT NULL,
  `area` varchar(20) NOT NULL,
  `stars` varchar(20) NOT NULL,
  `buy` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Shawshank Redemption','Drama','5','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('The GodFather','Drama','5','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Pulp Fiction','War','5','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Star Wars','Science Fiction','4','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Trek','Spoof','2','no');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Into the Wild','Drama','3','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('The Terminator','War','4','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('The Hitch','ChickFlicks','4','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Planet of the Apes','ScienceFiction','1','no');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Dunston Checkson','Comedy','3','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Commando','War','3','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('RobinHood','Action','1','no');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('The African Queen','Comedy','1','no');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Manhattan','Drama','2','no');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('The Kid','Spoof','2','no');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Leon','ScienceFiction','2','no');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('The Princess Bride','ChickFlicks','3','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('The Excorcist','War','3','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Casino','ChickFlics','1','no');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Country for Old Men','War','4','yes');
insert into `table1`(`movie`,`area`,`stars`,`buy`) values ('Gandhi','spoof','2','no');


USE blockbuster;

CREATE TABLE `table2` (
  `name` varchar(20) NOT NULL,
  `category` varchar(20) NOT NULL,
  `rating` varchar(20) NOT NULL,
  `watch` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Transporter 1','Thriller','5','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Transporter 2','Thriller','3','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Transporter 3','Thriller','1','no');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Desparado','WarMovie','4','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Saving Private Ryan','WarMovie','3','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Pirates of Carrabean','Action','4','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Man from Earth','Drama','3','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('GoodFellas','Drama','2','no');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Philadelophia','Drama','5','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('The Dark Night','Action','4','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('The Usual Suspects','RomanticComedy','3','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Silence of Lambs','Spoof','4','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Memento','Spoof','1','no');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('The Matrix','Spoof','2','no');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Taxi driver','RomanticComedy','3','no');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('ForestGump','Action','4','yes');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('The Sixth Sense','Drama','2','no');
insert into `table2`(`name`,`category`,`rating`,`watch`) values ('Bachelor''s Party','RomanticComedy','4','yes');