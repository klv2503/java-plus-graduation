INSERT INTO locations(lat, lon) VALUES
(37, 33);

INSERT INTO events(title, annotation, category_id, description, event_date, location_id, is_paid, participant_limit, request_moderation, publication_date, state, views, initiator_id) VALUES
('title', 'annotation', 1, 'description', '2025-10-15 15:00:00', 1, true, 50, true, '2025-01-01 15:00', 'PUBLISHED', 30, 3),
('title1', 'annotation2', 1, 'description2', '2025-05-10 12:00:00', 1, true, 70, true, '2025-01-01 15:00', 'PUBLISHED', 17, 4),
('title3', 'annotation3', 1, 'description3', '2025-03-14 10:00:00', 1, false, 100, true, '2025-01-01 15:00', 'PUBLISHED', 22, 7),
('title4', 'annotation4', 1, 'description4', '2025-03-14 10:00:00', 1, false, 100, true, '2025-01-01 15:00', 'PUBLISHED', 22, 7);
