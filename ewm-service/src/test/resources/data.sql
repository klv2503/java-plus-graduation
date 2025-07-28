INSERT INTO users(name, email) VALUES
('user1', 'user1@user.ru'),
('user2', 'user2@user.ru'),
('user3', 'user3@user.ru'),
('user4', 'user4@user.ru'),
('user5', 'user5@user.ru'),
('user6', 'user6@user.ru'),
('user7', 'user7@user.ru'),
('user8', 'user8@user.ru'),
('user9', 'user9@user.ru'),
('user10', 'user10@user.ru'),
('user11', 'user11@user.ru'),
('user12', 'user12@user.ru');

INSERT INTO category(name) VALUES
('Category'),
('New Wave'),
('Something');

INSERT INTO locations(lat, lon) VALUES
(37, 33);

INSERT INTO events(title, annotation, category_id, description, event_date, is_paid, participant_limit, request_moderation, publication_date, state, views, initiator_id) VALUES
('title', 'annotation', 1, 'description', '2025-10-15 15:00:00', true, 50, true, '2025-01-01 15:00', 'PUBLISHED', 30, 3),
('title1', 'annotation2', 1, 'description2', '2025-05-10 12:00:00', true, 70, true, '2025-01-01 15:00', 'PUBLISHED', 17, 4),
('title3', 'annotation3', 1, 'description3', '2025-03-14 10:00:00', false, 100, true, '2025-01-01 15:00', 'PUBLISHED', 22, 7),
('title4', 'annotation4', 1, 'description4', '2025-03-14 10:00:00', false, 100, true, '2025-01-01 15:00', 'PUBLISHED', 22, 7);

INSERT INTO compilations(pinned, title) VALUES
(false, 'compilation1'),
(true, 'compilation2'),
(true, 'compilation3');

INSERT INTO compilation_events(compilation_id, event_id) VALUES
(1, 1),
(2, 1),
(3, 2);

INSERT INTO comments(user_id, event_id, text, created, status) VALUES
(1, 2, 'Комментарий номер один', '2025-12-15 15:00:00', 'PUBLISHED'),
(1, 2, 'Комментарий номер два', '2025-12-15 15:00:00', 'DELETED'),
(1, 2, 'Комментарий номер два', '2025-12-15 15:00:00', 'PUBLISHED'),
(1, 2, 'Комментарий номер два', '2025-12-15 15:00:00', 'BANNED');