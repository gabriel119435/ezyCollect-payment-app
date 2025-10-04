create table if not exists users (
    id bigint auto_increment primary key,
    username varchar(50) not null unique,
    password varchar(100) not null,
    created_at timestamp(6) not null default current_timestamp(6)
);

create table if not exists banking_details (
    id bigint auto_increment primary key,
    user_id bigint not null,
    account_number varchar(50) not null,
    routing_number varchar(50) not null,
    bank_name varchar(100) not null,
    webhook_url varchar(200) not null,
    body_template varchar(500) not null,
    created_at timestamp(6) not null default current_timestamp(6),
    foreign key (user_id) references users(id)
);

create table if not exists payments (
    id bigint auto_increment primary key,
    user_id bigint not null,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    zip_code varchar(20) not null,
    card_info varchar(200) not null,
    payment_value decimal(15,5) not null,
    created_at timestamp(6) not null default current_timestamp(6),
    foreign key (user_id) references users(id),
    index idx_payments_created_at (created_at)
);

create table if not exists payments_status (
    id bigint auto_increment primary key,
    payment_id bigint not null,
    status varchar(20) not null,
    retries int not null default 0,
    history text,
    created_at timestamp(6) not null default current_timestamp(6),
    updated_at timestamp(6) not null default current_timestamp(6),
    foreign key (payment_id) references payments(id),
    index idx_payments_status_created_at (updated_at),
    index idx_payments_status_status (status)
);