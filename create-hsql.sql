CREATE TABLE user_t (id INTEGER IDENTITY, firstname VARCHAR(32) , lastname VARCHAR(32) , email VARCHAR(48) , locale VARCHAR(16) , timezone VARCHAR(32) , password_pw VARCHAR(48) , password_slt VARCHAR(20) );
CREATE TABLE role_t (id INTEGER IDENTITY, name VARCHAR(20),  description CLOB , owner INTEGER);

--REM INSERTING into USER_T
Insert into USER_T (FIRSTNAME,LASTNAME,EMAIL,LOCALE,TIMEZONE,PASSWORD_PW,PASSWORD_SLT,ID) values ('Jack','Nicholson','Jack.Nicholson@gmail.com','en_AU','Australia/Brisbane','password','52SCGCW2WMI1JUUV',1);
Insert into USER_T (FIRSTNAME,LASTNAME,EMAIL,LOCALE,TIMEZONE,PASSWORD_PW,PASSWORD_SLT,ID) values ('Robert','Redford','Robert.Redford@gmail.com','en_AU',null,'password',null,2);
Insert into USER_T (FIRSTNAME,LASTNAME,EMAIL,LOCALE,TIMEZONE,PASSWORD_PW,PASSWORD_SLT,ID) values ('Micheal','Caine','Micheal.Caine@gmail.com','en_AU',null,'password',null,3);
--REM INSERTING into ROLE_T
Insert into ROLE_T (ID,NAME,OWNER) values (1,'Batman',1);
Insert into ROLE_T (ID,NAME,OWNER) values (2,'One flew over the cuckoos nest',1);
Insert into ROLE_T (ID,NAME,OWNER) values (3,'Sneakers',2);
Insert into ROLE_T (ID,NAME,OWNER) values (4,'The Italian job',3);
Insert into ROLE_T (ID,NAME,OWNER) values (5,'Cider house rules',3);
Insert into ROLE_T (ID,NAME,OWNER) values (6,'Cider house rules',3);
