CREATE TABLE user_t (firstname VARCHAR(32) , lastname VARCHAR(32) , email VARCHAR(48) , locale VARCHAR(16) , timezone VARCHAR(32) , password_pw VARCHAR(48) , password_slt VARCHAR(20) , id NUMBER NOT NULL)
ALTER TABLE user_t ADD CONSTRAINT user_t_PK PRIMARY KEY(id)
CREATE SEQUENCE user_t_sequence START WITH 1 INCREMENT BY 1
CREATE OR REPLACE TRIGGER user_t_trigger BEFORE INSERT ON user_t FOR EACH ROW WHEN (new.id is null) BEGIN SELECT user_t_sequence.nextval INTO :new.id FROM DUAL; END;

CREATE TABLE role_t (name VARCHAR(20) , id NUMBER NOT NULL , description CLOB , owner NUMBER)
ALTER TABLE role_t ADD CONSTRAINT role_t_PK PRIMARY KEY(id)
CREATE SEQUENCE role_t_sequence START WITH 1 INCREMENT BY 1
CREATE OR REPLACE TRIGGER role_t_trigger BEFORE INSERT ON role_t FOR EACH ROW WHEN (new.id is null) BEGIN SELECT role_t_sequence.nextval INTO :new.id FROM DUAL; END;

CREATE INDEX user_t_email ON user_t ( email )
CREATE INDEX role_t_owner ON role_t ( owner )

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
