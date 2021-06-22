cd ..

sudo -u postgres psql -d sec1 -a -f secure-server/src/main/java/META-INF/drop-tables.sql
sudo -u postgres psql -d sec2 -a -f secure-server/src/main/java/META-INF/drop-tables.sql
sudo -u postgres psql -d sec3 -a -f secure-server/src/main/java/META-INF/drop-tables.sql
sudo -u postgres psql -d sec4 -a -f secure-server/src/main/java/META-INF/drop-tables.sql
