create table aircrafts(
    aircraft_code char(3) not null primary key,
    model text not null,
    range int not null
);
create table airports(
    airport_code char(3) not null  primary key,
    airport_name text not null,
    city text not null,
    coordinates text not null,
    timezone text not null
);
create table boarding_passes(
    ticket_no char(13) not null,
    flight_id int not null,
    boarding_no int not null,
    seat_no varchar(4) not null
);
create table bookings(
    book_ref char(6) not null primary key,
    book_date timestamp not null,
    total_amount numeric(10,2) not null
);
create table flights(
    flight_id int not null primary key,
    flight_no char(6) not null,
    scheduled_departure timestamp not null,
    scheduled_arrival timestamp not null,
    departure_airport char(3) not null,
    arrival_airport char(3) not null,
    status varchar(20) not null,
    aircraft_code char(3) not null,
    actual_departure timestamp,
    actual_arrival timestamp
);
create table seats(
    aircraft_code char(3) not null,
    seat_no varchar(4) not null,
    fare_conditions varchar(10) not null
);
create table ticket_flights(
    ticket_no char(13) not null,
    flight_id int not null,
    fare_conditions varchar(10) not null,
    amount numeric(10,2) not null
);
create table tickets(
    ticket_no char(13) not null primary key,
    book_ref char(6) not null,
    passenger_id varchar(20) not null,
    passenger_name text not null,
    contact_data text
);