DELETE FROM nonces;
DELETE FROM Proof;
DELETE FROM Report;
DELETE FROM Client;

INSERT INTO Client (identifier, user_id, is_special_user) VALUES (1, '1', 0);
INSERT INTO Client (identifier, user_id, is_special_user) VALUES (2, '2', 0);
INSERT INTO Client (identifier, user_id, is_special_user) VALUES (3, '3', 0);
INSERT INTO Client (identifier, user_id, is_special_user) VALUES (4, '4', 0);
INSERT INTO Client (identifier, user_id, is_special_user) VALUES (100, '100', 1);
