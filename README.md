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
-
To Insert in values in authors table
-
INSERT INTO Authors (id, name, nationality, birth_year, death_year) VALUES

(1, 'George Orwell', 'British', 1903, 1950),

(2, 'Harper Lee', 'American', 1926, 2016),

(3, 'F. Scott Fitzgerald', 'American', 1896, 1940),

(4, 'Aldous Huxley', 'British', 1894, 1963),

(5, 'J.D. Salinger', 'American', 1919, 2010),

(6, 'Herman Melville', 'American', 1819, 1891),

(7, 'Jane Austen', 'British', 1775, 1817),

(8, 'Leo Tolstoy', 'Russian', 1828, 1910),

(9, 'Fyodor Dostoevsky', 'Russian', 1821, 1881),

(10, 'J.R.R. Tolkien', 'British', 1892, 1973);
