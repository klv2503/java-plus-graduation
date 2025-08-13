INSERT INTO category(name) VALUES
('Category'),
('New Wave'),
('Something');

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