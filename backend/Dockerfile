# Imagem da API em duas etapas: primeiro compila com Maven, depois copia só o .jar para uma imagem leve.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Baixa as dependências antes de copiar o código: se só o código mudar, essa camada vem do cache.
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Os testes rodam no mvn verify / no CI (eles precisam do Docker), então aqui só empacoto.
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
# Usuário sem privilégios: a API não precisa rodar como root.
RUN useradd --system --no-create-home api
USER api
COPY --from=build /app/target/prova-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
