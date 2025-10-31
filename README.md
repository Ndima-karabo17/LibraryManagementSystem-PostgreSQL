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


To create table Books
-
CREATE TABLE Books (

    id INT PRIMARY KEY,
	
    title VARCHAR(255),
	
    author_id INT REFERENCES Authors(id),
	
    genres TEXT[],
	
    published_year INT,
	
    available BOOLEAN
);
-
To insert values in Books table
-
INSERT INTO Books (id, title, author_id, genres, published_year, available) VALUES 

(1, '1984', 1, ARRAY['Dystopian', 'Political Fiction'], 1949, TRUE),

(2, 'To Kill a Mockingbird', 2, ARRAY['Southern Gothic', 'Bildungsroman'], 1960, TRUE),

(3, 'The Great Gatsby', 3, ARRAY['Tragedy'], 1925, TRUE),

(4, 'Brave New World', 4, ARRAY['Dystopian', 'Science Fiction'], 1932, TRUE),

(5, 'The Catcher in the Rye', 5, ARRAY['Realist Novel', 'Bildungsroman'], 1951, TRUE),

(6, 'Moby-Dick', 6, ARRAY['Adventure Fiction'], 1851, TRUE),

(7, 'Pride and Prejudice', 7, ARRAY['Romantic Novel'], 1813, TRUE),

(8, 'War and Peace', 8, ARRAY['Historical Novel'], 1869, TRUE),

(9, 'Crime and Punishment', 9, ARRAY['Philosophical Novel'], 1866, TRUE),

(10, 'The Hobbit', 10, ARRAY['Fantasy'], 1937, TRUE);

To create patrons table 
-

CREATE TABLE Patrons (

    id SERIAL PRIMARY KEY,
	
    name VARCHAR(100),
	
    email VARCHAR(100),
	
    borrowed_books INT[]
);
-
To insert values in Patrons table
-
INSERT INTO Patrons (id, name, email, borrowed_books) VALUES

(1, 'Alice Johnson', 'alice@example.com', ARRAY[]::INT[]),

(2, 'Bob Smith', 'bob@example.com', ARRAY[1, 2]),

(3, 'Carol White', 'carol@example.com', ARRAY[]::INT[]),

(4, 'David Brown', 'david@example.com', ARRAY[3]),

(5, 'Eve Davis', 'eve@example.com', ARRAY[]::INT[]),

(6, 'Frank Moore', 'frank@example.com', ARRAY[4, 5]),

(7, 'Grace Miller', 'grace@example.com', ARRAY[]::INT[]),

(8, 'Hank Wilson', 'hank@example.com', ARRAY[6]),

(9, 'Ivy Taylor', 'ivy@example.com', ARRAY[]::INT[]),

(10, 'Jack Anderson', 'jack@example.com', ARRAY[7, 8]);




## Sprint 3: Read Operations (Queries)

Get all books
-
<img width="1500" height="800" alt="get all books" src="https://github.com/user-attachments/assets/46774d80-ee55-4402-a316-331681d00d9d" />

Get a book by title
-
<img width="1500" height="800" alt="get a book by title" src="https://github.com/user-attachments/assets/5c946106-276f-4963-8753-8a842bab7697" />

Get all books by a specific author
-

<img width="1500" height="800" alt="get all books aby a specific author" src="https://github.com/user-attachments/assets/f62a4c56-c9e8-4130-a0b1-8333ba7b63e5" />
