# LibraryManagementSystem-PostgreSQL
A Library Management System in PostgreSQL stores and manages data about books, authors, and patrons. 
It lets users add, view, update, or delete this information easily. To learn how to use SQL to create tables, 
link them with relationships, and run advanced queries.

To create table Author:
-
CREATE TABLE Authors (

    id INT PRIMARY KEY,
	
    name VARCHAR(100),
	
    nationality VARCHAR(255),
	
    birth_year INT,
	
    death_year INT
);
