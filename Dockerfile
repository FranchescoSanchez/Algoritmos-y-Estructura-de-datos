# ===== Etapa 1: compilar el proyecto con Maven (JDK 21) =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# ===== Etapa 2: desplegar en Tomcat 11 (Jakarta EE 11) =====
FROM tomcat:11-jdk21
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/target/MiPortafolio.war /usr/local/tomcat/webapps/ROOT.war

# Variables de entorno (sobrescribir al ejecutar el contenedor)
ENV DB_HOST="" \
    DB_PORT="5432" \
    DB_NAME="postgres" \
    DB_USER="postgres" \
    DB_PASSWORD="" \
    SUPABASE_URL="" \
    SUPABASE_KEY="" \
    SUPABASE_BUCKET="portafolio-archivos"

EXPOSE 8080
CMD ["catalina.sh", "run"]
