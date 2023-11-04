DROP table IF EXISTS rememberme_tokens;
DROP table IF EXISTS csrf_tokens;

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

create table IF NOT EXISTS events(
 id SERIAL primary key,
 user_id SERIAL not null,
 event_type varchar(255) not null,
 event_timestamp timestamp not null,
 event_message varchar(255) not null,
 foreign key (user_id) references users(id)
);

create table IF NOT EXISTS rememberme_tokens(
 id SERIAL primary key,
 user_id SERIAL not null,
 token varchar(255) not null,
 foreign key (user_id) references users(id)
);

create table IF NOT EXISTS csrf_tokens(
 id SERIAL primary key,
 user_id SERIAL not null,
 token varchar(255) not null,
 expiration_date timestamp not null,
 foreign key (user_id) references users(id)
);

insert into users(login, name, password, email, authority, expiration_date)
select 'admin', 'admin', E'$2a$10$YdgnnXcd4W1IV2bXx9j8BevMDvfhHU1wNM5Puhmsbu1eknmqEsyCK', 'admin@example.com',
'ROLE_ADMIN', '2099-01-01 00:00:00'
where not exists (select 1 from users where login = 'admin');
