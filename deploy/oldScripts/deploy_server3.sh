cd ..
sudo -u postgres psql -d sec3 -a -f secure-server/src/main/java/META-INF/drop-tables.sql

java -jar secure-server/target/secure-server-1.0.jar --server.port=9203 3 4 --spring.datasource.url=jdbc:postgresql://localhost:5432/sec3 | tee deploy/logs/server3_log.txt &

# Wait for the server to initiate the database before inserting the data
sleep 15
sudo -u postgres psql -d sec3 -a -f secure-server/src/main/java/META-INF/load-script.sql
echo "SERVERs DEPLOY DONE, START CLIENTS NOW"
read CONT

ps axf | grep secure-server | grep -v grep | awk '{print "kill -9 " $1}' | sh
