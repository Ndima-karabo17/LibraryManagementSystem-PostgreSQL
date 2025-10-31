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

Get all available books
-
<img width="1500" height="800" alt="get all available books" src="https://github.com/user-attachments/assets/4aab0685-7931-46ea-af17-485553cc30e1" />

## Sprint 4: Update Operations

Mark a book as borrowed (set available = false).
-

<img width="1500" height="800" alt="update available to false" src="https://github.com/user-attachments/assets/41b4a3ed-769d-4dfa-ac74-e9f27a676c96" />

<img width="1500" height="800" alt="how it looks like" src="https://github.com/user-attachments/assets/b7f84f9f-62e7-4782-9f75-bf04c95dfb5a" />

Add a new genre to an existing book
-
<img width="1500" height="800" alt="adding new genre" src="https://github.com/user-attachments/assets/c1ddf89f-faa4-488d-acd1-ebe4e01fca2d" />

<img width="1500" height="800" alt="hoow it looks like" src="https://github.com/user-attachments/assets/a2b4f96d-8a7b-4603-b394-5c3e43351214" />


Add a borrowed book to a patron’s record
-
<img width="1500" height="800" alt="add a borrowed book to a patrons record" src="https://github.com/user-attachments/assets/087bef8c-6b8b-4bbd-b6d2-717f1b00d6fe" />

<img width="1500" height="800" alt="how it looks like" src="https://github.com/user-attachments/assets/4b3d3d5c-ada5-44d1-9ccd-754930192d8d" />

## Sprint 5: Delete Operations

Delete a book by title
-

<img width="1500" height="800" alt="delete book by title" src="https://github.com/user-attachments/assets/2fdabb83-2218-49f0-b68f-5c967ba9679b" />

<img width="1500" height="800" alt="how it looks like" src="https://github.com/user-attachments/assets/034f4ffd-1c66-4f3b-afba-30307c39bf65" />

Delete an author by ID
-
<img width="1500" height="800" alt="delete an author by id" src="https://github.com/user-attachments/assets/1afb909c-3da8-4b26-bc79-c70457454d71" />
<img width="1500" height="800" alt="how it looks like" src="https://github.com/user-attachments/assets/48e28fa8-2171-4383-b551-073ee9d4f8ec" />



## Sprint 6: Advanced Queries

Find books published after 1950
-
<img width="1500" height="800" alt="All books published after 1950" src="https://github.com/user-attachments/assets/3d51c77f-9085-4f7e-acdd-0c27e7b5f4b9" />

Find all American authors
-
<img width="1500" height="800" alt="All americans author" src="https://github.com/user-attachments/assets/0da952f3-a944-4dd6-8fa8-88c47b674624" />

Set all books as available
-
<img width="1500" height="800" alt="books to be available" src="https://github.com/user-attachments/assets/4bb88048-4e35-4e98-b985-dcbb618722b3" />

<img width="1500" height="800" alt="all books are available to be booked" src="https://github.com/user-attachments/assets/a3442cd5-bc7e-46e5-83ad-5d8c2133c94d" />

Find all books that are available AND published after 1950
-
<img width="1500" height="800" alt="available books and published after 1950" src="https://github.com/user-attachments/assets/e1d3e2f7-3fdf-49fe-806b-87ece7f82ca8" />

Find authors whose names contain "George"
-
<img width="1500" height="800" alt="authors whose names contain George" src="https://github.com/user-attachments/assets/21cfa8a3-a90b-4775-9a2a-108a4f8e328f" />

Increment the published year 1869 by 1
-
<img width="1500" height="800" alt="move from 1869 to 1870" src="https://github.com/user-attachments/assets/dab160e4-7ef9-4841-84c8-56be2777e522" />
<img width="1499" height="837" alt="updated one" src="https://github.com/user-attachments/assets/473149e3-7f52-43c2-ba24-314ec3a523e9" />
