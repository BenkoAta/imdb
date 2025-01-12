alter table users add column email_verified bit not null;
alter table users add column delete_code int not null;
alter table users add column reset_password_code int;
alter table users add column reset_password_until datetime;