# LibraryManagementSystem-PostgreSQL
A Library Management System in PostgreSQL stores and manages data about books, authors, and patrons. 
It lets users add, view, update, or delete this information easily. To learn how to use SQL to create tables, 
link them with relationships, and run advanced queries.

To create table Author:
-
CREATE TABLE Authors(
	nationality VARCHAR(255),
	lifespan INT,
	name VARCHAR(100)
);
