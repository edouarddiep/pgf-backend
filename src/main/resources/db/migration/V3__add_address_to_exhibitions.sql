-- Migration pour ajouter le champ address à la table exhibitions
ALTER TABLE exhibitions ADD COLUMN address VARCHAR(500);