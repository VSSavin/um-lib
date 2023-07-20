create table IF NOT EXISTS users(
 id SERIAL primary key,
 login varchar(50) not null,
 name varchar(100) not null,
 password varchar(60) not null,
 email varchar(50) not null,
 authority varchar(50) not null,
 expiration_date timestamp not null,
 verification_id varchar(50),
 account_locked smallint DEFAULT 0,
 credentials_expired smallint DEFAULT 0,
 enabled smallint DEFAULT 1
 );

insert into users(login, name, password, email, authority, expiration_date)
SELECT 'admin', 'admin', E'$2a$10$YdgnnXcd4W1IV2bXx9j8BevMDvfhHU1wNM5Puhmsbu1eknmqEsyCK', 'admin@example.com',
'ROLE_ADMIN', '2099-01-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'admin');
