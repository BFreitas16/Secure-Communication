cd ..

java -jar secure-server/target/secure-server-1.0.jar --server.port=9201 1 4 --spring.datasource.url=jdbc:postgresql://localhost:5432/sec1 | tee deploy/logs/server1_log.txt &
java -jar secure-server/target/secure-server-1.0.jar --server.port=9202 2 4 --spring.datasource.url=jdbc:postgresql://localhost:5432/sec2 | tee deploy/logs/server2_log.txt &
java -jar secure-server/target/secure-server-1.0.jar --server.port=9203 3 4 --spring.datasource.url=jdbc:postgresql://localhost:5432/sec3 | tee deploy/logs/server3_log.txt &
java -jar secure-server/target/secure-server-1.0.jar --server.port=9204 4 4 --spring.datasource.url=jdbc:postgresql://localhost:5432/sec4 | tee deploy/logs/server4_log.txt &



# Wait for the server to initiate the database before inserting the data
#sleep 60
#sudo -u postgres psql -d sec1 -a -f secure-server/src/main/java/META-INF/load-script.sql
#sudo -u postgres psql -d sec2 -a -f secure-server/src/main/java/META-INF/load-script.sql
#sudo -u postgres psql -d sec3 -a -f secure-server/src/main/java/META-INF/load-script.sql
#sudo -u postgres psql -d sec4 -a -f secure-server/src/main/java/META-INF/load-script.sql

read CONT

ps axf | grep secure-server | grep -v grep | awk '{print "kill -9 " $1}' | sh
